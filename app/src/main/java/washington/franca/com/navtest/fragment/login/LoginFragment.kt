package washington.franca.com.navtest.fragment.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.android.synthetic.main.fragment_login.*
import washington.franca.com.navtest.R


private const val RC_SIGN_IN = 9001

class LoginFragment : BaseLoginFragment() {
    private lateinit var facebookLoginManger: LoginManager
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        facebookLoginManger = LoginManager.getInstance()
        facebookLoginManger.registerCallback(callbackManager, object:FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                try {
                    showProgress(true, "Connecting...")
                    userViewModel.signInWithFacebook(result)
                }catch (e:Exception) {
                    userViewModel.showErrorMessage(e)
                }
            }

            override fun onCancel() {}

            override fun onError(error: FacebookException?) {
                userViewModel.showErrorMessage(error)
            }
        })



        sign_in_button.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionGlobalToDestSignIn(null, null))
        }

        sign_up_button.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionDestLoginToDestSignUp(null, null))
        }

        sign_in_with_google_button.setOnClickListener {
            val signInIntent = userViewModel.googleSignInClient().signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        sign_in_with_facebook_button.setOnClickListener {
            facebookLoginManger.logIn(this, ArrayList<String>().apply{
                add("email")
                add("public_profile")
            })
        }

        privacy_policy_button.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            RC_SIGN_IN ->{
                if(resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        showProgress(true, "Connecting...")
                        userViewModel.signInWithGoogle(task.getResult(ApiException::class.java))

                    } catch (e: ApiException) {
                        // Google Sign In failed, update UI appropriately
                        userViewModel.showErrorMessage(e)
                    }
                }
            }
            else-> {
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
