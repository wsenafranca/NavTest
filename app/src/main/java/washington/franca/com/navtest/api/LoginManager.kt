package washington.franca.com.navtest.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import washington.franca.com.navtest.LoginNavGraphDirections
import washington.franca.com.navtest.R
import washington.franca.com.navtest.fragment.login.SignInFragment

private const val RC_GOOGLE_SIGN_IN = 9002

class LoginManager {
    private var facebookLoginManger: LoginManager? = null
    private var facebookCallback: CallbackManager? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var googleCallback:GoogleCallback? = null
    private var emailCallback: SignInFragment.Callback? = null
    private val auth = FirebaseAuth.getInstance()

    private fun setupEmailCallback(callback: Callback, action:(AuthCredential)-> Task<AuthResult>) {
        emailCallback = object: SignInFragment.Callback {
            override fun onSubmit(email: String?, password: String?) {
                try {
                    action(EmailAuthProvider.getCredential(email!!, password!!)).addOnCompleteListener {
                        try {
                            if(it.isSuccessful) {
                                callback.onSuccess(auth.currentUser!!)
                            } else {
                                callback.onError(it.exception)
                            }
                        }catch (e:Exception) {
                            callback.onError(e)
                        }
                    }
                }catch (e:Exception) {
                    callback.onError(e)
                }
            }
        }
    }

    private fun setupGoogleCallback(context: Context, callback:Callback, action:(AuthCredential)-> Task<AuthResult>) {
        if(googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }

        googleCallback = object :GoogleCallback() {
            override fun onSuccess(account: GoogleSignInAccount) {
                action(GoogleAuthProvider.getCredential(account.idToken, null)).addOnCompleteListener {
                    try {
                        if(it.isSuccessful) {
                            callback.onSuccess(auth.currentUser!!)
                        }else {
                            callback.onError(it.exception)
                        }
                    }catch (e:Exception) {
                        callback.onError(e)
                    }
                }
            }

            override fun onCancel() {
                callback.onCancel()
            }

            override fun onError(error: Throwable?) {
                callback.onError(error)
            }
        }
    }

    private fun setupFacebookCallback(callback: Callback, action:(AuthCredential)->Task<AuthResult>) {
        if(facebookLoginManger == null) {
            facebookLoginManger = LoginManager.getInstance()
        }
        if(facebookCallback == null) {
            facebookCallback = CallbackManager.Factory.create()
        }
        facebookLoginManger?.registerCallback(facebookCallback, object:FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                try {
                    val token = result!!.accessToken.token
                    val credential = FacebookAuthProvider.getCredential(token)
                    action(credential).addOnCompleteListener {
                        try {
                            if(it.isSuccessful) {
                                callback.onSuccess(auth.currentUser!!)
                            } else {
                                callback.onError(it.exception)
                            }
                        }catch (e:Exception) {
                            callback.onError(e)
                        }
                    }
                }catch (e:Exception) {
                    callback.onError(e)
                }
            }

            override fun onCancel() {
                callback.onCancel()
            }

            override fun onError(error: FacebookException?) {
                callback.onError(error)
            }
        })
    }

    fun signInWithFacebook(fragment: Fragment, callback: Callback) {
        setupFacebookCallback(callback) {
            return@setupFacebookCallback auth.signInWithCredential(it)
        }
        facebookLoginManger?.logIn(fragment, ArrayList<String>().apply{
            add("email")
            add("public_profile")
        })
    }

    fun signInWithGoogle(fragment: Fragment, callback: Callback) {
        setupGoogleCallback(fragment.requireContext(), callback) {
            return@setupGoogleCallback auth.signInWithCredential(it)
        }
        googleSignInClient?.signInIntent.let {
            fragment.startActivityForResult(it, RC_GOOGLE_SIGN_IN)
        }
    }

    fun signInWithEmail(fragment: Fragment, callback: Callback) {
        setupEmailCallback(callback) {
            return@setupEmailCallback auth.signInWithCredential(it)
        }
        fragment.findNavController().navigate(LoginNavGraphDirections.actionGlobalToDestSignIn(null, null, false))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(facebookCallback?.onActivityResult(requestCode, resultCode, data) != false) {
            facebookLoginManger?.unregisterCallback(facebookCallback)
            facebookCallback = null
        } else if(googleCallback?.onActivityResult(requestCode, resultCode, data) != false) {
            googleCallback = null
        }
    }

    abstract class GoogleCallback {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?):Boolean {
            return if(requestCode == RC_GOOGLE_SIGN_IN) {
                if(resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        onSuccess(task.getResult(ApiException::class.java)!!)
                    } catch (e: ApiException) {
                        onError(e)
                    }
                } else {
                    onCancel()
                }
                true
            } else {
                false
            }
        }

        abstract fun onSuccess(account: GoogleSignInAccount)
        abstract fun onCancel()
        abstract fun onError(error:Throwable?)
    }

    interface Callback {
        fun onSuccess(user:FirebaseUser)
        fun onCancel()
        fun onError(e:Throwable?)
        fun onNeedReauthenticate()
    }
}