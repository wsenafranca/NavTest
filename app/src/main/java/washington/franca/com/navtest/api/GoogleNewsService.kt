package washington.franca.com.navtest.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import washington.franca.com.navtest.repository.NewsRepository

interface GoogleNewsService {
    @GET("top-headlines")
    fun topHeadlines(@Query("country") country:String, @Query("page") page: Int): Call<NewsRepository.NewsResponse>
    @GET("top-headlines")
    fun topHeadlines(@Query("country") country:String, @Query("category") category:String, @Query("page") page: Int): Call<NewsRepository.NewsResponse>
    @GET("everything")
    fun search(@Query("q") query: String, @Query("language") language:String, @Query("page") page: Int, @Query("sortBy") sortBy:String): Call<NewsRepository.NewsResponse>
}