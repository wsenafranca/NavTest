package washington.franca.com.navtest.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
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
import washington.franca.com.navtest.R
import washington.franca.com.navtest.fragment.login.AuthenticateDialogFragment
import java.lang.RuntimeException

private const val RC_GOOGLE_SIGN_IN = 9002

class UserRepository {
    private var facebookLoginManger: LoginManager? = null
    private var facebookCallback: CallbackManager? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var googleCallback: GoogleCallback? = null
    private val auth = FirebaseAuth.getInstance()

    private fun setupGoogleCallback(context: Context, callback: Callback<FirebaseUser>, action:(AuthCredential)-> Task<AuthResult>) {
        if(googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }

        googleCallback = object : GoogleCallback() {
            override fun onSuccess(account: GoogleSignInAccount) {
                action(GoogleAuthProvider.getCredential(account.idToken, null)).addOnCompleteListener {
                    googleCallback = null
                    try {
                        if(it.isSuccessful) {
                            callback.onSuccess(auth.currentUser!!)
                        }else {
                            val e = it.exception
                            if(e is FirebaseAuthRecentLoginRequiredException) {
                                callback.onNeedReauthenticate()
                            } else {
                                callback.onError(it.exception)
                            }
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

    private fun setupFacebookCallback(callback: Callback<FirebaseUser>, action:(AuthCredential)->Task<AuthResult>) {
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
                        facebookLoginManger?.unregisterCallback(facebookCallback)
                        facebookCallback = null
                        try {
                            if(it.isSuccessful) {
                                callback.onSuccess(auth.currentUser!!)
                            } else {
                                val e = it.exception
                                if(e is FirebaseAuthRecentLoginRequiredException) {
                                    callback.onNeedReauthenticate()
                                } else {
                                    callback.onError(it.exception)
                                }
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

    fun signInWithFacebook(fragment: Fragment, callback: Callback<FirebaseUser>) {
        setupFacebookCallback(callback) {
            return@setupFacebookCallback auth.signInWithCredential(it)
        }
        facebookLoginManger?.logIn(fragment, ArrayList<String>().apply{
            add("email")
            add("public_profile")
        })
    }

    fun linkWithFacebook(fragment: Fragment, callback: Callback<FirebaseUser>) {
        setupFacebookCallback(callback) {
            auth.currentUser!!.linkWithCredential(it)
        }
        facebookLoginManger?.logIn(fragment, ArrayList<String>().apply{
            add("email")
            add("public_profile")
        })
    }

    fun reauthenticateWithFacebook(fragment: Fragment, callback: Callback<FirebaseUser>) {
        setupFacebookCallback(callback) {
            auth.currentUser!!.reauthenticateAndRetrieveData(it)
        }
        facebookLoginManger?.logIn(fragment, ArrayList<String>().apply{
            add("email")
            add("public_profile")
        })
    }

    fun unlinkWithFacebook(callback: Callback<FirebaseUser>) {
        try {
            auth.currentUser!!.unlink(FacebookAuthProvider.PROVIDER_ID).addOnCompleteListener {
                if(it.isSuccessful) {
                    facebookLoginManger?.logOut()
                    callback.onSuccess(auth.currentUser!!)
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun signInWithGoogle(fragment: Fragment, callback: Callback<FirebaseUser>) {
        setupGoogleCallback(fragment.requireContext(), callback) {
            return@setupGoogleCallback auth.signInWithCredential(it)
        }
        googleSignInClient?.signInIntent.let {
            fragment.startActivityForResult(it, RC_GOOGLE_SIGN_IN)
        }
    }

    fun linkWithGoogle(fragment: Fragment, callback: Callback<FirebaseUser>) {
        setupGoogleCallback(fragment.requireContext(), callback) {
            auth.currentUser!!.linkWithCredential(it)
        }
        googleSignInClient?.signInIntent.let {
            fragment.startActivityForResult(it, RC_GOOGLE_SIGN_IN)
        }
    }

    fun reauthenticateWithGoogle(fragment: Fragment, callback: Callback<FirebaseUser>) {
        setupGoogleCallback(fragment.requireContext(), callback) {
            auth.currentUser!!.reauthenticateAndRetrieveData(it)
        }
        googleSignInClient?.signInIntent.let {
            fragment.startActivityForResult(it, RC_GOOGLE_SIGN_IN)
        }
    }

    fun unlinkWithGoogle(callback: Callback<FirebaseUser>) {
        try {
            auth.currentUser!!.unlink(FacebookAuthProvider.PROVIDER_ID).addOnCompleteListener {
                if(it.isSuccessful) {
                    googleSignInClient?.signOut()
                    callback.onSuccess(auth.currentUser!!)
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun verifyEmail(email: String?, callback: Callback<List<String>>) {
        try {
            auth.fetchSignInMethodsForEmail(email!!).addOnCompleteListener {
                if(it.isSuccessful) {
                    callback.onSuccess(it.result?.signInMethods?.toList() ?: emptyList<String>())
                }else {
                    callback.onError(it.exception)
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun signInWithEmail(email:String?, password:String?, callback: Callback<FirebaseUser>) {
        try {
            auth.signInWithCredential(EmailAuthProvider.getCredential(email!!, password!!)).addOnCompleteListener {
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

    fun reauthenticateWithEmail(fragment: Fragment, callback: Callback<FirebaseUser>) {
        try {
            AuthenticateDialogFragment.Builder()
                .setPositiveButton(null){dialog, email, password ->
                    try {
                        auth.currentUser!!.reauthenticateAndRetrieveData(EmailAuthProvider.getCredential(email!!, password!!)).addOnCompleteListener {
                            try {
                                if(it.isSuccessful) {
                                    callback.onSuccess(auth.currentUser!!)
                                    dialog.dismiss()
                                } else {
                                    val e = it.exception
                                    if(e is FirebaseAuthRecentLoginRequiredException) {
                                        callback.onNeedReauthenticate()
                                    } else {
                                        dialog.setError(e)
                                    }
                                }
                            }catch (e:Exception) {
                                callback.onError(e)
                            }
                        }
                    }catch (e:Exception) {
                        dialog.setError(e)
                    }
                }
                .setCancelListener{
                    it.dismiss()
                }
                .build()
                .show(fragment.fragmentManager!!, null)
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun sendPasswordResetEmail(email: String?, callback: Callback<FirebaseUser>) {
        try {
            auth.sendPasswordResetEmail(email!!).addOnCompleteListener {
                if(it.isSuccessful) {
                    callback.onSuccess(auth.currentUser!!)
                } else {
                    callback.onError(it.exception)
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun currentUser(callback: Callback<FirebaseUser>) {
        try {
            callback.onSuccess(auth.currentUser!!)
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun updatePassword(fragment: Fragment, callback: Callback<FirebaseUser>) {
        try {
            val isNewPassword = !auth.currentUser!!.providerData.map { it.providerId }.contains(EmailAuthProvider.PROVIDER_ID)
            val buttonRes = if(isNewPassword) R.string.login_create_password_button else R.string.login_update_password_button
            AuthenticateDialogFragment.Builder()
                .isEmailInputVisible(false)
                .isForgotButtonVisible(false)
                .setPositiveButton(fragment.getText(buttonRes)) { dialog, _, password->
                    password?.let {
                        try {
                            auth.currentUser!!.updatePassword(password).addOnCompleteListener {
                                if(it.isSuccessful) {
                                    callback.onSuccess(auth.currentUser!!)
                                    dialog.dismiss()
                                } else {
                                    val e = it.exception
                                    if(e is FirebaseAuthRecentLoginRequiredException) {
                                        callback.onNeedReauthenticate()
                                    } else {
                                        dialog.setError(e)
                                    }
                                }
                            }
                        }catch (e:Exception) {
                            dialog.setError(e)
                        }
                    }
                }
                .setCancelListener{
                    it.dismiss()
                }
                .build().show(fragment.fragmentManager!!, null)
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun signOut(callback: Callback<Void?>) {
        try {
            auth.currentUser?.let { user->
                for(profile in user.providerData) {
                    try {
                        when(profile.providerId) {
                            GoogleAuthProvider.PROVIDER_ID -> {
                                googleSignInClient?.signOut()
                            }
                            FacebookAuthProvider.PROVIDER_ID -> {
                                LoginManager.getInstance().logOut()
                            }
                        }
                    }catch (e:Exception) {
                        callback.onError(e)
                    }
                }
            }
            auth.signOut()
            callback.onSuccess(null)
        }
        catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun getProvider():String {
        val user = auth.currentUser!!
        val providers:List<String> = user.providerData.map {
            it.providerId
        }
        return when {
            providers.contains(EmailAuthProvider.PROVIDER_ID) -> {
                EmailAuthProvider.PROVIDER_ID
            }
            providers.contains(GoogleAuthProvider.PROVIDER_ID) -> {
                GoogleAuthProvider.PROVIDER_ID
            }
            providers.contains(FacebookAuthProvider.PROVIDER_ID) -> {
                FacebookAuthProvider.PROVIDER_ID
            }
            else -> throw RuntimeException("Provider not found.")
        }
    }

    fun createUser(email: String?, password: String?, name:String?, callback: Callback<FirebaseUser>) {
        try {
            auth.createUserWithEmailAndPassword(email!!, password!!).addOnCompleteListener {
                try {
                    if(it.isSuccessful) {
                        val profile = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        updateProfile(profile, callback)
                    } else {
                        val e = it.exception
                        if(e is FirebaseAuthRecentLoginRequiredException) {
                            callback.onNeedReauthenticate()
                        } else {
                            callback.onError(it.exception)
                        }
                    }
                }catch (e:Exception) {
                    callback.onError(e)
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun deleteUser(callback: Callback<Void?>) {
        try {
            val user = auth.currentUser!!
            user.delete().addOnCompleteListener {
                if(it.isSuccessful) {
                    callback.onSuccess(null)
                } else {
                    val e = it.exception
                    if(e is FirebaseAuthRecentLoginRequiredException) {
                        callback.onNeedReauthenticate()
                    } else {
                        callback.onError(it.exception)
                    }
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun loadProfilePhoto(context: Context?, callback: Callback<Bitmap?>) {
        try {
            val photoUrl = auth.currentUser!!.photoUrl
            var glide = Glide.with(context!!).asBitmap()
            glide = if(photoUrl == null) {
                glide.load(R.drawable.placeholder)
            } else {
                glide.load(photoUrl)
            }
            glide.signature(ObjectKey(auth.currentUser!!.uid))
                .apply(RequestOptions.circleCropTransform())
                .addListener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        callback.onError(e)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        try {
                            callback.onSuccess(resource)
                        }catch (e:Exception) {
                            callback.onError(e)
                        }
                        return true
                    }
                }).preload(128, 128)
        }
        catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun updateProfile(profile:UserProfileChangeRequest, callback: Callback<FirebaseUser>) {
        try {
            auth.currentUser!!.updateProfile(profile).addOnCompleteListener {
                if(it.isSuccessful) {
                    callback.onSuccess(auth.currentUser!!)
                } else {
                    callback.onError(it.exception)
                }
            }
        }catch (e:Exception) {
            callback.onError(e)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?):Boolean {
        if(facebookCallback?.onActivityResult(requestCode, resultCode, data) == true) {
            return true
        }
        if(googleCallback?.onActivityResult(requestCode, resultCode, data) == true) {
            return true
        }
        return false
    }

    private abstract class GoogleCallback {
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

    abstract class Callback<T> {
        abstract fun onSuccess(result:T)
        open fun onCancel() {}
        abstract fun onError(e:Throwable?)
        open fun onNeedReauthenticate() {}
    }
}