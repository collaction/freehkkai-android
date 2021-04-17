package hk.collaction.freehkkai.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Created by himphen on 21/5/16.
 */
abstract class BaseFragmentActivity<T : ViewBinding> : BaseActivity<T>() {
    open var fragment: Fragment? = null
    open var titleId: Int? = null
    open var titleString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFragment(fragment, titleString = titleString, titleId = titleId)
    }
}