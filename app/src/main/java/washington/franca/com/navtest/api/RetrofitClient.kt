package washington.franca.com.navtest.api

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import washington.franca.com.navtest.R

object RetrofitClient {
    private var sGoogleNewsService:GoogleNewsService? = null

    @Synchronized
    fun googleNewsService(context:Context):GoogleNewsService {
        if(sGoogleNewsService == null) {
            val httpClient = OkHttpClient.Builder().addInterceptor(Interceptor {chain->
                val original = chain.request()
                val originalHttpUrl = original.url()

                val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("apiKey", context.getString(R.string.google_news_api_key))
                    .build()

                // Request customization: add request headers
                val requestBuilder = original.newBuilder().url(url)

                val request = requestBuilder.build()
                return@Interceptor chain.proceed(request)
            }).build()

            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create()

            val client = Retrofit.Builder()
                .baseUrl("https://newsapi.org/v2/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()
            sGoogleNewsService = client.create(GoogleNewsService::class.java)
        }
        return sGoogleNewsService!!
    }
}