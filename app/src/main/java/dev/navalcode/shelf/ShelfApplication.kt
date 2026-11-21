package dev.navalcode.shelf

import android.app.Application
import android.os.SystemClock
import dev.navalcode.shelf.data.*
import dev.navalcode.shelf.domain.SearchBooks
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

open class ShelfApplication : Application() {
    val searchBooks by lazy {
        val clock = MonotonicClock { SystemClock.elapsedRealtime() }
        SearchBooks(CachedBookRepository(OpenLibrarySource(openLibraryApi(
            Retrofit.Builder().baseUrl("https://openlibrary.org/").client(createHttpClient())), clock), clock))
    }

    // The instrumentation runner replaces only HTTP; the production graph stays intact.
    protected open fun createHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder()
                .header("User-Agent", "ShelfAndroid/1.0 (https://github.com/navalcode/shelf-android)")
                .build())
        }.build()
}
