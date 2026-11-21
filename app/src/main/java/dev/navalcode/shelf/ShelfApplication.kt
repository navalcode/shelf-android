package dev.navalcode.shelf

import android.app.Application
import android.os.SystemClock
import dev.navalcode.shelf.data.CachedBookRepository
import dev.navalcode.shelf.data.MonotonicClock
import dev.navalcode.shelf.data.OpenLibrarySource
import dev.navalcode.shelf.data.openLibraryApi
import dev.navalcode.shelf.domain.SearchBooks
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit

open class ShelfApplication : Application() {
    val searchBooks by lazy {
        val clock = MonotonicClock { SystemClock.elapsedRealtime() }
        val retrofit =
            Retrofit.Builder().baseUrl("https://openlibrary.org/").client(createHttpClient())
        val source = OpenLibrarySource(openLibraryApi(retrofit), clock)
        val repository = CachedBookRepository(source, clock)
        SearchBooks(repository)
    }

    // The instrumentation runner replaces only HTTP; the production graph stays intact.
    protected open fun createHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "ShelfAndroid/1.0 (https://github.com/navalcode/shelf-android)",
                        )
                        .build()
                chain.proceed(request)
            }
            .build()
}
