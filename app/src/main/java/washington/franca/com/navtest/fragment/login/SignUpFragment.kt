package washington.franca.com.navtest.fragment.login


import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.util.SoftKeyboard

class SignUpFragment : BaseLoginFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            email_edit_text.setText(it.getString("email"))
            password_edit_text.setText(it.getString("password"))
        }

        sign_up_button.setOnClickListener {
            SoftKeyboard.hide(activity)
            showProgress(true, "Connecting...")
            userViewModel.signUp(email_edit_text.editableText.toString(),
                                    password_edit_text.editableText.toString(),
                                    name_edit_text.editableText.toString())
        }

        sign_in_button.setOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.actionDestSignUpToDestSignIn(email_edit_text.editableText.toString(), password_edit_text.editableText.toString()))
        }

        privacy_policy_button.setOnClickListener {
            openPrivacyPolicy()
        }
    }
}
