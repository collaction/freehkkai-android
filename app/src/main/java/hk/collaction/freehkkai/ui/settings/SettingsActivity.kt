package hk.collaction.freehkkai.ui.settings

import androidx.fragment.app.Fragment
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.ui.base.BaseFragmentActivity

class SettingsActivity : BaseFragmentActivity() {
    override var fragment: Fragment? = SettingsFragment.newInstance()
    override var titleId: Int? = R.string.title_activity_settings
}