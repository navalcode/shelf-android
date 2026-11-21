package dev.navalcode.shelf.data

import dev.navalcode.shelf.domain.SearchException
import dev.navalcode.shelf.domain.SearchFailureKind
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit

@OptIn(ExperimentalCoroutinesApi::class)
class OpenLibraryTest {
    private val json =
        """{"docs":[{"key":"/works/OL1W","title":"A book","author_name":["A"],"first_publish_year":1999,"edition_count":3}]}"""

    private fun source(server: MockWebServer) =
        OpenLibrarySource(openLibraryApi(Retrofit.Builder().baseUrl(server.url("/"))), { 0 }, 0)

    @Test
    fun `HTTP query limit fields and mapping use real Retrofit`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(json))
            val book = source(server).search("a & b").single()
            val request = server.takeRequest(2, TimeUnit.SECONDS)!!
            assertEquals("a & b", request.requestUrl!!.queryParameter("q"))
            assertEquals("20", request.requestUrl!!.queryParameter("limit"))
            assertTrue(request.requestUrl!!.queryParameter("fields")!!.contains("edition_count"))
            assertEquals(1999, book.firstPublished)
            assertEquals(3, book.editionCount)
            assertEquals(listOf("A"), book.authors)
        }
    }

    @Test
    fun `optional fields remain absent and duplicate IDs are removed`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setBody(
                        """{"docs":[{"key":"/works/OL1W","title":"A"},{"key":"/works/OL1W","title":"A"}]}""",
                    ),
            )
            val book = source(server).search("a").single()
            assertNull(book.firstPublished)
            assertNull(book.editionCount)
            assertTrue(book.authors.isEmpty())
        }
    }

    @Test
    fun `invalid payload never becomes cached empty success and next request can recover`() =
        runBlocking {
            MockWebServer().use { server ->
                val invalid =
                    listOf(
                        "{}",
                        "{\"docs\":[null]}",
                        "{\"docs\":[{\"key\":\"invalid\",\"title\":\"A\"}]}",
                        "{\"docs\":[{\"key\":\"/works/OL1W\",\"title\":\"  \"}]}",
                        "{\"docs\":42}",
                        "{\"docs\":[",
                        "<html>unavailable</html>",
                    )
                for ((index, body) in invalid.withIndex()) {
                    server.enqueue(MockResponse().setBody(body))
                    server.enqueue(MockResponse().setBody(json))
                    val repository = CachedBookRepository(source(server), { 0 })
                    try {
                        repository.search("query$index")
                        fail(body)
                    } catch (error: SearchException) {
                        assertEquals(body, SearchFailureKind.INVALID_DATA, error.kind)
                    }
                    assertEquals("A book", repository.search("query$index").books.single().title)
                }
                assertEquals(invalid.size * 2, server.requestCount)
            }
        }

    @Test
    fun `HTTP statuses become stable domain failures and are not cached`() = runBlocking {
        MockWebServer().use { server ->
            for (code in listOf(429, 503)) {
                server.enqueue(MockResponse().setResponseCode(code))
                server.enqueue(MockResponse().setBody(json))
                val repository = CachedBookRepository(source(server), { 0 })
                try {
                    repository.search("a")
                    fail()
                } catch (error: SearchException) {
                    assertEquals(
                        if (code == 429) SearchFailureKind.RATE_LIMIT
                        else SearchFailureKind.SERVICE,
                        error.kind,
                    )
                }
                assertEquals("A book", repository.search("a").books.single().title)
            }
            assertEquals(4, server.requestCount)
        }
    }

    @Test
    fun `empty successful HTTP body is invalid data`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(204))
            try {
                source(server).search("a")
                fail()
            } catch (error: SearchException) {
                assertEquals(SearchFailureKind.INVALID_DATA, error.kind)
            }
        }
    }

    @Test
    fun `transport IOException becomes network failure with cause retained`() = runTest {
        val offline = IOException("offline")
        val api =
            object : OpenLibraryApi {
                override suspend fun search(
                    query: String,
                    limit: Int,
                    fields: String,
                ): Response<SearchDto> = throw offline
            }
        try {
            OpenLibrarySource(api, { 0 }, 0).search("a")
            fail()
        } catch (error: SearchException) {
            assertEquals(SearchFailureKind.NETWORK, error.kind)
            assertSame(offline, error.cause)
        }
    }

    @Test
    fun `cancelling coroutine cancels Retrofit call`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(json).setBodyDelay(1, TimeUnit.SECONDS))
            val observed = AtomicReference<Call>()
            val client =
                OkHttpClient.Builder()
                    .eventListenerFactory { call ->
                        observed.set(call)
                        EventListener.NONE
                    }
                    .build()
            val source =
                OpenLibrarySource(
                    openLibraryApi(Retrofit.Builder().baseUrl(server.url("/")).client(client)),
                    { 0 },
                    0,
                )
            val job = launch(Dispatchers.Default) { source.search("old") }
            withContext(Dispatchers.IO) { assertNotNull(server.takeRequest(2, TimeUnit.SECONDS)) }
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertTrue("Underlying HTTP call must be cancelled", observed.get().isCanceled())
            server.enqueue(MockResponse().setBody(json))
            assertEquals("A book", source(server).search("new").single().title)
        }
    }

    @Test
    fun `source spaces requests and waiting request remains cancellable`() = runTest {
        val starts = mutableListOf<Long>()
        val api =
            object : OpenLibraryApi {
                override suspend fun search(
                    query: String,
                    limit: Int,
                    fields: String,
                ): Response<SearchDto> {
                    starts += testScheduler.currentTime
                    return Response.success(SearchDto(emptyList()))
                }
            }
        val source = OpenLibrarySource(api, { testScheduler.currentTime })
        source.search("one")
        val cancelled = launch { source.search("two") }
        runCurrent()
        cancelled.cancelAndJoin()
        source.search("three")
        assertEquals(listOf(0L, 1100L), starts)
    }

    @Test
    fun `client bounds results even if the service ignores limit`() = runBlocking {
        MockWebServer().use { server ->
            val docs = (1..25).joinToString { """{"key":"/works/OL${it}W","title":"Book $it"}""" }
            server.enqueue(MockResponse().setBody("{\"docs\":[$docs]}"))
            val books = source(server).search("books")
            assertEquals(20, books.size)
            assertEquals("/works/OL20W", books.last().id)
        }
    }
}
