package washington.franca.com.navtest.fragment.login


import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.pd.chocobar.ChocoBar
import kotlinx.android.synthetic.main.fragment_sign_up.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentSignUpBinding
import washington.franca.com.navtest.util.EventObserver
import washington.franca.com.navtest.util.SoftKeyboard

class SignUpFragment : BaseLoginFragment() {
    lateinit var binding:FragmentSignUpBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        userViewModel.error.observe(viewLifecycleOwner, EventObserver{
            sign_up_button.isEnabled = true
            progress_bar.isIndeterminate = false
            when(it) {
                is FirebaseAuthEmailException, is FirebaseAuthUserCollisionException -> {
                    email_input_layout.error = it.localizedMessage
                    email_edit_text.requestFocus()
                    SoftKeyboard.show(email_edit_text)
                }
                is FirebaseAuthWeakPasswordException -> {
                    password_input_layout.error = it.localizedMessage
                    password_edit_text.requestFocus()
                    SoftKeyboard.show(password_edit_text)
                }
                else -> {
                    it?.localizedMessage?.let { message ->
                        ChocoBar.builder().setView(view)
                            .setText(message)
                            .setDuration(ChocoBar.LENGTH_SHORT)
                            .red()
                            .show()
                    }
                }
            }
        })
        email_edit_text.setText(userViewModel.email)
        when {
            TextUtils.isEmpty(email_edit_text.editableText) -> {
                email_edit_text.requestFocus()
                SoftKeyboard.show(email_edit_text)
            }
            TextUtils.isEmpty(name_edit_text.editableText) -> {
                name_edit_text.requestFocus()
                SoftKeyboard.show(name_edit_text)
            }
            TextUtils.isEmpty(password_edit_text.editableText) -> {
                password_edit_text.requestFocus()
                SoftKeyboard.show(password_edit_text)
            }
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

        sign_up_button.setOnClickListener {
            submit()
        }

        privacy_policy_button.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    private fun submit() {
        sign_up_button.isEnabled = false
        SoftKeyboard.hide(activity)
        progress_bar.isIndeterminate = true
        email_input_layout.error = null
        name_input_layout.error = null
        password_input_layout.error = null
        val email = email_edit_text.editableText.toString()
        val password = password_edit_text.editableText.toString()
        val name = name_edit_text.editableText.toString()
        var focused:View? = null
        if(TextUtils.isEmpty(email)) {
            email_input_layout.error = "email cannot be empty."
            if(focused == null) focused = email_edit_text
        }

        if(TextUtils.isEmpty(name)) {
            name_input_layout.error = "name cannot be empty."
            if(focused == null) focused = name_edit_text
        }

        if(TextUtils.isEmpty(password)) {
            password_input_layout.error = "password cannot be empty."
            if(focused == null) focused = password_edit_text
        }

        if(focused == null) {
            userViewModel.createUser(email, password, name)
        } else {
            sign_up_button.isEnabled = true
            progress_bar.isIndeterminate = false
            focused.requestFocus()
            SoftKeyboard.show(focused)
        }
    }
}
