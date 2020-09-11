package hk.collaction.freehkkai.ui.main

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.fragment.app.Fragment
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.ui.base.BaseFragmentActivity


class MainActivity : BaseFragmentActivity() {
    private var doubleBackToExitPressedOnce = false

    override var fragment: Fragment? = MainFragment()
    override var titleId: Int? = R.string.app_name


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
        } else {
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "請按多次返回鍵以離開程式", Toast.LENGTH_SHORT).show()
            Handler(mainLooper).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }
}