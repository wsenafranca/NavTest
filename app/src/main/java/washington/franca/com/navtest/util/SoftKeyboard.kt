package washington.franca.com.navtest.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT

object SoftKeyboard {
    fun show(view: View?) {
        (view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(view, SHOW_IMPLICIT)
    }
    fun hide(activity: Activity?) {
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { inputMethodManager->
            activity.currentFocus?.windowToken?.let {
                inputMethodManager.hideSoftInputFromWindow(it, 0)
            }
        }
    }
}