package washington.franca.com.navtest.fragment.login

import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.pd.chocobar.ChocoBar
import kotlinx.android.synthetic.main.fragment_authenticate_dialog.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.fragment.tintIcons
import washington.franca.com.navtest.util.SoftKeyboard

class AuthenticateDialogFragment : DialogFragment() {
    var emailInputVisible:Boolean = true
    var passwordInputVisible:Boolean = true
    var forgotButtonVisible:Boolean = true
    var positiveClickListener: OnPositiveClickListener?=null
    var positiveButtonText:CharSequence?=null
    var cancelListener:OnCancelListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_authenticate_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "Password"
        toolbar.setNavigationOnClickListener { cancelListener?.onCancel(this) }
        context?.let { context->
            val typedValue = TypedValue()
            context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
            val color = typedValue.data
            toolbar.navigationIcon?.let {
                DrawableCompat.setTint(it, color)
            }
        }

        password_edit_text.setOnEditorActionListener { _, actionId, event ->
            try {
                if(actionId == EditorInfo.IME_ACTION_DONE ||
                    event.action == KeyEvent.ACTION_DOWN &&
                    event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    submit()
                    return@setOnEditorActionListener  true
                }
            }catch (e:Exception) {
                e.printStackTrace()
            }
            return@setOnEditorActionListener false
        }

        sign_in_button.setOnClickListener {
            submit()
        }

        var focused:View? = null
        if(emailInputVisible) {
            email_input_layout.visibility = View.VISIBLE
            if(focused == null) focused = email_edit_text
        } else {
            email_input_layout.visibility = View.GONE
        }

        if(passwordInputVisible) {
            password_input_layout.visibility = View.VISIBLE
            if(focused == null) focused = password_edit_text
        } else {
            password_input_layout.visibility = View.GONE
        }

        if(forgotButtonVisible) {
            forgot_password_button.visibility = View.VISIBLE
        } else {
            forgot_password_button.visibility = View.GONE
        }

        if(positiveButtonText != null) {
            sign_in_button.text = positiveButtonText
        } else {
            sign_in_button.setText(R.string.login_authenticate_button)
        }

        focused?.postDelayed({
            focused.requestFocus()
            SoftKeyboard.show(focused)
        }, 10)
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }

    private fun submit() {
        sign_in_button.isEnabled = false
        SoftKeyboard.hide(activity)
        progress_bar.isIndeterminate = true
        email_input_layout.error = null
        password_input_layout.error = null
        positiveClickListener?.onClick(this, email_edit_text.editableText.toString(), password_edit_text.editableText.toString())
    }

    fun setError(error:Throwable?) {
        sign_in_button.isEnabled = true
        progress_bar.isIndeterminate = false
        email_input_layout.error = null
        password_input_layout.error = null
        when(error) {
            is FirebaseAuthEmailException, is FirebaseAuthUserCollisionException -> {
                email_input_layout.error = error.localizedMessage
                email_edit_text.requestFocus()
                SoftKeyboard.show(email_edit_text)
            }
            is FirebaseAuthWeakPasswordException -> {
                password_input_layout.error = error.localizedMessage
                password_edit_text.requestFocus()
                SoftKeyboard.show(password_edit_text)
            }
            else -> {
                error?.localizedMessage?.let { message ->
                    ChocoBar.builder().setView(view)
                        .setText(message)
                        .setDuration(ChocoBar.LENGTH_SHORT)
                        .red()
                        .show()
                }
            }
        }
    }

    class Builder {
        var emailInputVisible:Boolean = true
        var passwordInputVisible:Boolean = true
        var forgotButtonVisible:Boolean = true
        var positiveClickListener:OnPositiveClickListener?=null
        var positiveButtonText:CharSequence?=null
        var cancelListener:OnCancelListener?=null

        fun isEmailInputVisible(visible:Boolean):Builder {
            emailInputVisible = visible
            return this
        }
        fun isPasswordInputVisible(visible: Boolean):Builder {
            passwordInputVisible = visible
            return this
        }
        fun isForgotButtonVisible(visible:Boolean):Builder {
            forgotButtonVisible = visible
            return this
        }
        fun setPositiveButton(text: CharSequence?, listener:OnPositiveClickListener?):Builder {
            this.positiveButtonText = text
            this.positiveClickListener = listener
            return this
        }
        fun setPositiveButton(text: CharSequence?, listener:(AuthenticateDialogFragment, String?, String?)->Unit):Builder {
            this.positiveButtonText = text
            this.positiveClickListener = object:OnPositiveClickListener {
                override fun onClick(dialog: AuthenticateDialogFragment, email: String?, password: String?) {
                    listener(dialog, email, password)
                }
            }
            return this
        }
        fun setCancelListener(listener:OnCancelListener?) : Builder{
            this.cancelListener = listener
            return this
        }
        fun setCancelListener(listener:(AuthenticateDialogFragment)->Unit) : Builder{
            this.cancelListener = object:OnCancelListener {
                override fun onCancel(dialog: AuthenticateDialogFragment) {
                    listener(dialog)
                }
            }
            return this
        }

        fun build():AuthenticateDialogFragment {
            return AuthenticateDialogFragment().apply {
                this.emailInputVisible = this@Builder.emailInputVisible
                this.passwordInputVisible = this@Builder.passwordInputVisible
                this.forgotButtonVisible = this@Builder.forgotButtonVisible
                this.positiveButtonText = this@Builder.positiveButtonText
                this.positiveClickListener = this@Builder.positiveClickListener
                this.cancelListener = this@Builder.cancelListener
            }
        }
    }

    interface OnPositiveClickListener {
        fun onClick(dialog:AuthenticateDialogFragment, email:String?, password:String?)
    }

    interface OnCancelListener {
        fun onCancel(dialog:AuthenticateDialogFragment)
    }
}
