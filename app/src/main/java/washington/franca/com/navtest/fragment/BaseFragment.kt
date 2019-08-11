package washington.franca.com.navtest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import washington.franca.com.navtest.R
import washington.franca.com.navtest.util.RootNavigation

open class BaseFragment : Fragment() {
    private lateinit var progressView:View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as? ViewGroup)?.let {
            progressView = LayoutInflater.from(requireContext()).inflate(R.layout.progress_indicator, it, false)
            it.addView(progressView)
            progressView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView?.visibility = if(isBottomNavigationVisible()){
            View.VISIBLE
        } else {
            View.GONE
        }
        val toolbar = getToolBar()
        val collapsingToolbar = getCollapsingToolBar()
        @Suppress("CAST_NEVER_SUCCEEDS")
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            if(toolbar != null && collapsingToolbar != null) {
                setupWithNavController(collapsingToolbar, toolbar, findNavController(), RootNavigation.appBarConfiguration())
            }
            else if(toolbar != null) {
                setupWithNavController(toolbar, findNavController(), RootNavigation.appBarConfiguration())
            }
        }

    }
    open val bottomNavigationView:BottomNavigationView?
        get(){
            return activity?.findViewById(R.id.bottom_nav)
        }

    open fun getToolBar():Toolbar? {
        return null
    }

    open fun getCollapsingToolBar():CollapsingToolbarLayout? {
        return null
    }

    open fun isBottomNavigationVisible():Boolean {
        return true
    }

    open fun showProgress(show:Boolean, text:CharSequence?=null) {
        if(show) {
            progressView.visibility = View.VISIBLE
            progressView.findViewById<TextView>(R.id.progress_indicator_text)?.text = text
        }
        else {
            progressView.visibility = View.GONE
        }
    }
}