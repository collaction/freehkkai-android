package hk.collaction.freehkkai.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import hk.collaction.freehkkai.helper.UtilHelper.Companion.detectLanguage

/**
 * Created by himphen on 21/5/16.
 */
@SuppressLint("Registered")
open class BaseFragmentActivity : BaseActivity() {

    open var fragment: Fragment? = null
    open var titleId: Int? = null
    open var titleString: String? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        detectLanguage(this)

        titleString?.let {
            initActionBar(supportActionBar, it)
        } ?: run {
            titleId?.let {
                initActionBar(supportActionBar, it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragment?.let { fragment ->
            titleString?.let {
                initFragment(fragment, it)
            } ?: run {
                titleId?.let {
                    initFragment(fragment, getString(it))
                }
            }
        }
    }
}