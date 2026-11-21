package dev.navalcode.shelf

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

class ShelfTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, ControlledHttpApplication::class.java.name, context)
}

/** Reuses production composition, timeout and User-Agent; no request can reach the internet. */
class ControlledHttpApplication : ShelfApplication() {
    override fun createHttpClient(): OkHttpClient = super.createHttpClient().newBuilder()
        .addInterceptor { chain ->
            ControlledHttp.requests += chain.request()
            val reply = checkNotNull(ControlledHttp.replies.poll()) { "Unexpected HTTP request" }
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(reply.code).message("Controlled response")
                .body(reply.body.toResponseBody("application/json".toMediaType())).build()
        }.build()
}

internal object ControlledHttp {
    data class Reply(val code: Int = 200, val body: String)
    val replies = ConcurrentLinkedQueue<Reply>()
    val requests = CopyOnWriteArrayList<Request>()
    fun reset() { replies.clear(); requests.clear() }
}
