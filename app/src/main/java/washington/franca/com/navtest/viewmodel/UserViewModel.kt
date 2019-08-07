package washington.franca.com.navtest.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import washington.franca.com.navtest.repository.UserRepository
import washington.franca.com.navtest.util.Event
import java.security.AuthProvider

class UserViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        fun create(fragmentActivity: FragmentActivity) : UserViewModel {
            return ViewModelProviders.of(fragmentActivity).get(UserViewModel::class.java)
        }
    }

    enum class AuthState {
        UNKNOWN,
        AUTHENTICATED,
        UNAUTHENTICATED
    }

    private val repository = UserRepository(application)

    private val _authState = MutableLiveData<Event<AuthState>>()
    val authState:LiveData<Event<AuthState>> = _authState

    private val _user = MutableLiveData<FirebaseUser>()
    val user:LiveData<FirebaseUser> = _user

    private val _profilePhoto = MutableLiveData<Bitmap?>()
    val profilePhoto:LiveData<Bitmap?> = _profilePhoto

    fun googleSignInClient():GoogleSignInClient = repository.googleSignInClient

    init {
        _authState.value = Event(AuthState.UNKNOWN)
        _profilePhoto.value = null
    }

    private fun postCurrentUser() {
        repository.currentUser({
            _authState.postValue(Event(AuthState.AUTHENTICATED))
            showMessage("Logged in")
            updateUser()
        }, {
            _authState.postValue(Event(AuthState.UNAUTHENTICATED))
            showErrorMessage(it)
        })
    }

    private fun updateUser() {
        repository.currentUser({
            _user.postValue(it)
            loadProfilePhoto()
        }, {
            _authState.postValue(Event(AuthState.UNAUTHENTICATED))
            showErrorMessage(it)
        })
    }

    fun verifyCurrentAccount() = postCurrentUser()

    fun signInWithEmail(email:String?, password:String?) {
        repository.signInWithEmail(email, password, {
            postCurrentUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun signInWithGoogle(account: GoogleSignInAccount?) {
        repository.signInWithGoogle(account, {
            postCurrentUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun signInWithFacebook(loginResult: LoginResult?) {
        repository.signInWithFacebook(loginResult, {
            postCurrentUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun signUp(email: String?, password: String?, name:String?) {
        repository.signUp(email, password, name, {
            postCurrentUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun sendPasswordResetEmail(email: String?, callback:()->Unit) {
        repository.sendPasswordResetEmail(email, callback, {
            showErrorMessage(it)
        })
    }

    fun updatePassword(password: String?, needReauthenticateCallback: (() -> Unit)?) {
        try {
            repository.updatePassword(password, {
                updateUser()
            }, {
                if(it is FirebaseAuthRecentLoginRequiredException) {
                    needReauthenticateCallback?.invoke()
                } else {
                    showErrorMessage(it)
                }
            })
        }catch (e:Exception) {
            showErrorMessage(e)
        }
    }

    fun linkWithCredential(auth: AuthCredential, needReauthenticateCallback: (() -> Unit)?) {
        try {
            repository.linkWithCredential(auth, {
                updateUser()
            }, {
                if(it is FirebaseAuthRecentLoginRequiredException) {
                    needReauthenticateCallback?.invoke()
                } else {
                    showErrorMessage(it)
                }
            })
        }catch (e:Exception) {
            showErrorMessage(e)
        }
    }

    fun reauthenticate(auth:AuthCredential, callback: ((FirebaseUser) -> Unit)?) {
        try {
            repository.reauthenticate(auth, callback, {
                showErrorMessage(it)
            })
        }catch (e:Exception) {
            showErrorMessage(e)
        }
    }

    fun unlinkProvider(providerId:String) {
        repository.unlinkProvider(providerId, {
            updateUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun signOut() {
        repository.signOut( {
            _authState.postValue(Event(AuthState.UNAUTHENTICATED))
            _profilePhoto.postValue(null)
        }, {
            showErrorMessage(it)
        })
    }

    fun delete(callback: (() -> Unit)?) {
        repository.delete({
            _authState.postValue(Event(AuthState.UNAUTHENTICATED))
            _profilePhoto.postValue(null)
        }, {
            if(it is FirebaseAuthRecentLoginRequiredException) {
                callback?.invoke()
            } else {
                showErrorMessage(it)
            }
        })
    }

    private fun loadProfilePhoto() {
        repository.loadProfilePhoto({
            _profilePhoto.postValue(it)
        }, {
            showErrorMessage(it)
        })
    }
}