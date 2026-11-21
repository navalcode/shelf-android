package dev.navalcode.shelf

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class ShelfTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, ControlledHttpApplication::class.java.name, context)
}

/** Reuses production composition, timeout and User-Agent; no request can reach the internet. */
class ControlledHttpApplication : ShelfApplication() {
    override fun createHttpClient(): OkHttpClient =
        super.createHttpClient()
            .newBuilder()
            .addInterceptor { chain ->
                ControlledHttp.requests += chain.request()
                val reply = ControlledHttp.reply(chain.request())
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(reply.code)
                    .message("Controlled response")
                    .body(reply.body.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
}

internal object ControlledHttp {
    data class Reply(val code: Int = 200, val body: String)

    val replies = ConcurrentLinkedQueue<Reply>()
    val requests = CopyOnWriteArrayList<Request>()
    @Volatile var handler: ((Request) -> Reply)? = null

    fun reply(request: Request): Reply =
        handler?.invoke(request) ?: checkNotNull(replies.poll()) { "Unexpected HTTP request" }

    fun reset() {
        replies.clear()
        requests.clear()
        handler = null
    }
}

/**
 * Original fixture serialization follows the public HTTP contract, not a replacement repository.
 */
internal fun booksReply(vararg books: dev.navalcode.shelf.domain.Book): ControlledHttp.Reply {
    val docs = org.json.JSONArray()
    for (book in books) {
        val dto =
            org.json
                .JSONObject()
                .put("key", book.id)
                .put("title", book.title)
                .put("author_name", org.json.JSONArray(book.authors))
        book.firstPublished?.let { dto.put("first_publish_year", it) }
        book.editionCount?.let { dto.put("edition_count", it) }
        docs.put(dto)
    }
    return ControlledHttp.Reply(body = org.json.JSONObject().put("docs", docs).toString())
}
