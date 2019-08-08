package washington.franca.com.navtest.fragment.login


import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.android.synthetic.main.fragment_forgot_password.email_edit_text
import kotlinx.android.synthetic.main.fragment_forgot_password.email_input_layout
import kotlinx.android.synthetic.main.fragment_forgot_password.progress_bar

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentForgotPasswordBinding
import washington.franca.com.navtest.util.EventObserver
import washington.franca.com.navtest.util.SoftKeyboard

class ForgotPasswordFragment : BaseLoginFragment() {
    lateinit var binding: FragmentForgotPasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        email_edit_text.setText(userViewModel.email)

        userViewModel.error.observe(viewLifecycleOwner, EventObserver{
            progress_bar.isIndeterminate = false
            reset_password_button.isEnabled = true
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

        if(!TextUtils.isEmpty(userViewModel.email)) {
            email_edit_text.requestFocus()
            SoftKeyboard.show(email_edit_text)
        }

        reset_password_button.setOnClickListener {
            submit()
        }
    }

    private fun submit() {
        reset_password_button.isEnabled = false
        SoftKeyboard.hide(activity)
        progress_bar.isIndeterminate = true
        email_input_layout.error = null
        userViewModel.sendPasswordResetEmail(email_edit_text.editableText.toString())
    }
}
