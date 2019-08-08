package washington.franca.com.navtest.fragment.login

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_sign_in.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentSignInBinding
import washington.franca.com.navtest.util.EventObserver
import washington.franca.com.navtest.util.SoftKeyboard
import java.lang.Exception

class SignInFragment : BaseLoginFragment() {
    lateinit var binding:FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        userViewModel.error.observe(viewLifecycleOwner, EventObserver{
            progress_bar.isIndeterminate = false
            sign_in_button.isEnabled = true
            email_input_layout.error = it?.localizedMessage
            email_edit_text.requestFocus()
            SoftKeyboard.show(email_edit_text)
        })

        email_edit_text.setOnEditorActionListener { _, actionId, event ->
            try {
                if(actionId == EditorInfo.IME_ACTION_NEXT ||
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    submit()
                    return@setOnEditorActionListener true
                }
            }catch (e:Exception) {
                e.printStackTrace()
            }
            return@setOnEditorActionListener false
        }
        email_edit_text.requestFocus()
        SoftKeyboard.show(email_edit_text)

        sign_in_button.setOnClickListener {
            submit()
        }

        /*
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
        */
    }

    private fun submit() {
        sign_in_button.isEnabled = false
        SoftKeyboard.hide(activity)
        progress_bar.isIndeterminate = true
        email_input_layout.error = null
        userViewModel.verifyEmail(email_edit_text.editableText.toString())
    }
}
