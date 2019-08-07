package washington.franca.com.navtest.fragment.login

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_in.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.util.SoftKeyboard

class SignInFragment : BaseLoginFragment() {
    var callback:Callback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            email_edit_text.setText(it.getString("email"))
            password_edit_text.setText(it.getString("password"))
        }

        sign_in_button.setOnClickListener {
            SoftKeyboard.hide(activity)
            showProgress(true, "Connecting...")
            if(arguments?.getBoolean("reauthenticate", false) != false) {
                callback?.onSubmit(email_edit_text.editableText.toString(), password_edit_text.editableText.toString())
            } else {
                userViewModel.signInWithEmail(email_edit_text.editableText.toString(), password_edit_text.editableText.toString())
            }
        }

        sign_up_button.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionDestSignInToDestSignUp(email_edit_text.editableText.toString(), password_edit_text.editableText.toString()))
        }

        forgot_password_button.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionDestSignInToDestForgotPassword(email_edit_text.editableText.toString()))
        }

        privacy_policy_button.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    override fun onDestroyView() {
        callback = null
        super.onDestroyView()
    }

    interface Callback {
        fun onSubmit(email:String?, password: String?)
    }
}
