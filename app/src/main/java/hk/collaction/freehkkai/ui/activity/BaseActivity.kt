package hk.collaction.freehkkai.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdView
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.helper.UtilHelper
import hk.collaction.freehkkai.helper.UtilHelper.Companion.detectLanguage
import kotlinx.android.synthetic.main.activity_container_adview.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Created by himphen on 21/5/16.
 */
abstract class BaseActivity : AppCompatActivity() {
    open var isAdViewPreserveSpace = false

    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detectLanguage(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun initActionBar(ab: ActionBar?, title: String?, subtitle: String? = null): ActionBar? {
        if (ab != null) {
            ab.elevation = 100f
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeButtonEnabled(true)
            ab.title = title
            if (subtitle != null) {
                ab.subtitle = subtitle
            }
        }
        return ab
    }

    protected fun initActionBar(ab: ActionBar?, titleId: Int, subtitleId: Int = 0): ActionBar? {
        if (ab != null) {
            ab.elevation = 100f
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setHomeButtonEnabled(true)
            ab.setTitle(titleId)
            if (subtitleId != 0) {
                ab.setSubtitle(subtitleId)
            }
        }
        return ab
    }

    fun setActionBarTitle(titleId: Int) {
        val ab = supportActionBar
        ab?.setTitle(titleId)
    }

    fun setActionBarTitle(title: String?) {
        val ab = supportActionBar
        if (ab != null) {
            ab.title = title
        }
    }

    companion object {
        const val DELAY_AD_LAYOUT = 100
    }

    fun initFragment(fragment: Fragment, title: String) {
        setContentView(R.layout.activity_container_adview)
        setSupportActionBar(toolbar)
        initActionBar(supportActionBar, title)

        Handler().postDelayed({
            adView = UtilHelper.initAdView(this, adLayout, isAdViewPreserveSpace)
        }, DELAY_AD_LAYOUT.toLong())

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
    }
}