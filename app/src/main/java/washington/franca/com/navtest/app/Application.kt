package washington.franca.com.navtest.app

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager

class Application : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        val preferenceManager = PreferenceManager.getDefaultSharedPreferences(this)
        when(preferenceManager.getBoolean("dark_theme", false)) {
            true-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            false-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}