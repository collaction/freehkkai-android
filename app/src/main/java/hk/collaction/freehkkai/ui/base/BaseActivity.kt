package hk.collaction.freehkkai.ui.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.gms.ads.AdView
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.databinding.ActivityContainerAdviewBinding
import hk.collaction.freehkkai.util.Utils

/**
 * Created by himphen on 21/5/16.
 */
abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    lateinit var viewBinding: T

    abstract fun getActivityViewBinding(): T

    open var isAdViewPreserveSpace = false

    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = getActivityViewBinding()
        setContentView(viewBinding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    protected fun initActionBar(
        toolbar: Toolbar?,
        titleString: String? = null, subtitleString: String? = null,
        @StringRes titleId: Int? = null, @StringRes subtitleId: Int? = null
    ) {
        toolbar?.let {
            setSupportActionBar(toolbar)
            supportActionBar?.let { ab ->
                ab.setDisplayHomeAsUpEnabled(true)
                ab.setHomeButtonEnabled(true)
                titleString?.let {
                    ab.title = titleString
                }
                titleId?.let {
                    ab.setTitle(titleId)
                }
                subtitleString?.let {
                    ab.subtitle = subtitleString
                }
                subtitleId?.let {
                    ab.setSubtitle(subtitleId)
                }
            }
        }
    }

    public override fun onDestroy() {
        adView?.removeAllViews()
        adView?.destroy()
        super.onDestroy()
    }

    fun initFragment(fragment: Fragment?, titleString: String?, titleId: Int?) {
        fragment?.let {
            initActionBar(
                (viewBinding as? ActivityContainerAdviewBinding?)?.toolbar?.root,
                titleString = titleString,
                titleId = titleId
            )

            Handler(Looper.getMainLooper()).postDelayed({
                adView = Utils.initAdView(
                    this,
                    (viewBinding as? ActivityContainerAdviewBinding)?.adLayout,
                    isAdViewPreserveSpace
                )
            }, Utils.DELAY_AD_LAYOUT)

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }
}