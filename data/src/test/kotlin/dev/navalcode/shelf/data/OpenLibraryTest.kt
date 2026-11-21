package dev.navalcode.shelf.data

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.OkHttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit

@OptIn(ExperimentalCoroutinesApi::class)
class OpenLibraryTest {
    private val json = """{"docs":[{"key":"/works/OL1W","title":"A book","author_name":["A"],"first_publish_year":1999,"edition_count":3}]}"""
    private fun source(server: MockWebServer) = OpenLibrarySource(openLibraryApi(Retrofit.Builder().baseUrl(server.url("/"))), { 0 }, 0)
    @Test fun `HTTP query limit fields and mapping use real Retrofit`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(json))
            val book = source(server).search("a & b").single()
            val request = server.takeRequest(2, TimeUnit.SECONDS)!!
            assertEquals("a & b", request.requestUrl!!.queryParameter("q"))
            assertEquals("20", request.requestUrl!!.queryParameter("limit"))
            assertTrue(request.requestUrl!!.queryParameter("fields")!!.contains("edition_count"))
            assertEquals(1999, book.firstPublished); assertEquals(3, book.editionCount)
            assertEquals(listOf("A"), book.authors)
        }
    }
    @Test fun `optional fields remain absent and duplicate IDs are removed`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""{"docs":[{"key":"/works/OL1W","title":"A"},{"key":"/works/OL1W","title":"A"}]}"""))
            val book = source(server).search("a").single()
            assertNull(book.firstPublished); assertNull(book.editionCount); assertTrue(book.authors.isEmpty())
        }
    }
    @Test fun `invalid payload is rejected instead of cached as empty success`() = runBlocking {
        MockWebServer().use { server ->
            for (body in listOf("{}", "{\"docs\":[null]}", "{\"docs\":[{\"key\":\"invalid\",\"title\":\"A\"}]}")) {
                server.enqueue(MockResponse().setBody(body))
                try { source(server).search("a"); fail(body) } catch (_: IOException) { }
            }
        }
    }
    @Test fun `HTTP errors are preserved`() = runBlocking {
        MockWebServer().use { server ->
            for (code in listOf(429, 503)) {
                server.enqueue(MockResponse().setResponseCode(code))
                try { source(server).search("a"); fail() } catch (error: HttpException) { assertEquals(code, error.code()) }
            }
        }
    }
    @Test fun `cancelling coroutine cancels Retrofit call`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(json).setBodyDelay(1, TimeUnit.SECONDS))
            val observed = AtomicReference<Call>()
            val client = OkHttpClient.Builder().eventListenerFactory { call ->
                observed.set(call)
                EventListener.NONE
            }.build()
            val source = OpenLibrarySource(openLibraryApi(Retrofit.Builder().baseUrl(server.url("/")).client(client)), { 0 }, 0)
            val job = launch(Dispatchers.Default) { source.search("old") }
            withContext(Dispatchers.IO) { assertNotNull(server.takeRequest(2, TimeUnit.SECONDS)) }
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertTrue("Underlying HTTP call must be cancelled", observed.get().isCanceled())
            server.enqueue(MockResponse().setBody(json))
            assertEquals("A book", source(server).search("new").single().title)
        }
    }
    @Test fun `source spaces requests and waiting request remains cancellable`() = runTest {
        val starts = mutableListOf<Long>()
        val api = object : OpenLibraryApi {
            override suspend fun search(query: String, limit: Int, fields: String): SearchDto {
                starts += testScheduler.currentTime
                return SearchDto(emptyList())
            }
        }
        val source = OpenLibrarySource(api, { testScheduler.currentTime })
        source.search("one")
        val cancelled = launch { source.search("two") }; runCurrent(); cancelled.cancelAndJoin()
        source.search("three")
        assertEquals(listOf(0L, 1100L), starts)
    }
}
