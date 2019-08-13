package washington.franca.com.navtest.repository

import android.content.Context
import android.text.TextUtils
import io.reactivex.Maybe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import washington.franca.com.navtest.api.RetrofitClient
import washington.franca.com.navtest.db.Database
import washington.franca.com.navtest.model.News
import washington.franca.com.navtest.model.NewsSuggestion
import java.util.*

class NewsRepository(context: Context) {
    private val service = RetrofitClient.googleNewsService(context)
    private val db = Database.get(context)

    fun search(query:String, page:Int, callback:(List<News>)->Unit, errorCallback:(Throwable?)->Unit) {
        service.search(query, Locale.getDefault().language, page, "publishedAt").enqueue(object: Callback<NewsResponse>{
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                errorCallback(t)
            }

            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if(response.isSuccessful) {
                    try {
                        val body = response.body()!!
                        callback(body.articles ?: emptyList())
                    }catch (e:Exception) {
                        errorCallback(e)
                    }
                } else {
                    errorCallback(HttpException(response))
                }
            }

        })
    }

    fun getHeadLines(country:String, category:String?, page:Int, callback:(List<News>)->Unit, errorCallback:(Throwable?)->Unit) {
        val endpoint = if(TextUtils.isEmpty(category)) {
            service.topHeadlines(country, page)
        } else {
            service.topHeadlines(country, category!!, page)
        }
        endpoint.enqueue(object: Callback<NewsResponse>{
            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                errorCallback(t)
            }

            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if(response.isSuccessful) {
                    try {
                        val body = response.body()!!
                        callback(body.articles ?: emptyList())
                    }catch (e:Exception) {
                        errorCallback(e)
                    }
                } else {
                    errorCallback(HttpException(response))
                }
            }
        })
    }

    private fun getSuggestionCall(query:String?):Maybe<List<NewsSuggestion>> {
        return if(query != null) {
            db.newsSuggestionDao().search(query)
        } else {
            db.newsSuggestionDao().getAll()
        }
    }

    fun getSuggestions(query:String?, callback: (List<String>) -> Unit) {
        try {
            var disposable: Disposable? = null
            disposable = getSuggestionCall(if(query == null) null else "$query%")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({list->
                    callback(list.map {
                        it.text }
                    )
                    disposable?.dispose()
                }, {
                    callback(emptyList())
                    disposable?.dispose()
                })
        }catch (e:Exception) {
            callback(emptyList())
        }
    }

    fun addSuggestion(text:String) {
        try {
            var disposable: Disposable? = null
            disposable = db.newsSuggestionDao().add(NewsSuggestion(text))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    disposable?.dispose()
                }
        }catch (e:Exception) { }
    }

    fun removeSuggestion(query:String?, text: String, callback: (List<String>) -> Unit) {
        try {
            var disposable: Disposable? = null
            disposable = db.newsSuggestionDao().add(NewsSuggestion(text))
                .andThen(getSuggestionCall(query))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({list->
                    callback(list.map { it.text })
                    disposable?.dispose()
                }, {
                    callback(emptyList())
                    disposable?.dispose()
                })
        }catch (e:Exception) {
            callback(emptyList())
        }
    }

    data class NewsResponse(
        var status:String? = null,
        var totalResults:Int = 0,
        var articles:List<News>? = null,
        var message:String? = null
    )
}