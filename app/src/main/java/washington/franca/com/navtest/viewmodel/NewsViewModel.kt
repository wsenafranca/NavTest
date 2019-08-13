package washington.franca.com.navtest.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import washington.franca.com.navtest.model.News
import washington.franca.com.navtest.repository.NewsRepository
import java.util.*

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        fun create(fragmentActivity: FragmentActivity):NewsViewModel {
            return ViewModelProviders.of(fragmentActivity).get(NewsViewModel::class.java)
        }

        fun create(fragment: Fragment):NewsViewModel {
            return ViewModelProviders.of(fragment).get(NewsViewModel::class.java)
        }
    }

    private val repository = NewsRepository(application)

    val news = MutableLiveData<List<News>>()
    val suggestions = MutableLiveData<List<String>>()
    val error = MutableLiveData<Throwable?>()
    var query:String? = null

    fun headLines(page:Int) {
        repository.getHeadLines(Locale.getDefault().country, query, page, {
            news.postValue(it)
        }, {
            it?.printStackTrace()
            error.postValue(it)
        })
    }

    fun search(page:Int) {
        try {
            repository.search(query!!, page, {
                news.postValue(it)
            }, {
                it?.printStackTrace()
                error.postValue(it)
            })
        }catch (e:Exception) {
            error.postValue(e)
        }
    }

    fun getSuggestions() {
        repository.getSuggestions(query) {
            suggestions.postValue(it)
        }
    }

    fun addSuggestion(suggestion:String) {
        repository.addSuggestion(suggestion)
    }

    fun removeSuggestion(suggestion:String) {
        repository.removeSuggestion(query, suggestion) {
            suggestions.postValue(it)
        }
    }
}