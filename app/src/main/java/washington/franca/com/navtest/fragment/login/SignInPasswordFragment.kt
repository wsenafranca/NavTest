package washington.franca.com.navtest.fragment.login


import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_sign_in_password.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentSignInPasswordBinding
import washington.franca.com.navtest.util.EventObserver
import washington.franca.com.navtest.util.SoftKeyboard

class SignInPasswordFragment : BaseLoginFragment() {
    lateinit var binding:FragmentSignInPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in_password, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        userViewModel.error.observe(viewLifecycleOwner, EventObserver{
            sign_in_button.isEnabled = true
            progress_bar.isIndeterminate = false
            password_input_layout.error = it?.localizedMessage
            password_edit_text.requestFocus()
            SoftKeyboard.show(password_edit_text)
        })
        if(userViewModel.isNewUser) {
            welcome_back_message_layout.visibility = View.GONE
        } else {
            welcome_back_message_layout.visibility = View.VISIBLE
            welcome_back_message_text.text = HtmlCompat.fromHtml(getString(R.string.login_sign_in_password_welcome_back_message, userViewModel.email), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        password_edit_text.setOnEditorActionListener { _, actionId, event ->
            try {
                if(actionId == EditorInfo.IME_ACTION_DONE ||
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
        password_edit_text.requestFocus()
        SoftKeyboard.show(password_edit_text)

        sign_in_button.setOnClickListener {
            submit()
        }

        forgot_password_button.setOnClickListener {
            userViewModel.recoverPassword()
        }
    }

    private fun submit() {
        sign_in_button.isEnabled = false
        SoftKeyboard.hide(activity)
        progress_bar.isIndeterminate = true
        password_input_layout.error = null
        userViewModel.signInWithEmail(userViewModel.email, password_edit_text.editableText.toString())
    }
}
