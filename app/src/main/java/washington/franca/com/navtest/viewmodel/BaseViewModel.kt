package washington.franca.com.navtest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import washington.franca.com.navtest.util.Event

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _error = MutableLiveData<Event<Throwable?>>()
    private val _message = MutableLiveData<Event<CharSequence?>>()

    open val error:LiveData<Event<Throwable?>> = _error
    open val message:LiveData<Event<CharSequence?>> = _message

    open fun showErrorMessage(e:Throwable?) {
        _error.postValue(Event(e))
    }

    open fun showMessage(message: CharSequence?) {
        _message.postValue(Event(message))
    }
}