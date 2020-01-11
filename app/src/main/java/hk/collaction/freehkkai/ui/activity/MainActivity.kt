package hk.collaction.freehkkai.ui.activity

import android.os.Handler
import android.widget.Toast
import androidx.fragment.app.Fragment
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.ui.fragment.MainFragment


class MainActivity : BaseFragmentActivity() {
    private var doubleBackToExitPressedOnce = false

    override var fragment: Fragment? = MainFragment()
    override var titleId: Int? = R.string.app_name


    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish()
        } else {
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "請按多次返回鍵以離開程式", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }
}