package hk.collaction.freehkkai.ui.activity

import androidx.fragment.app.Fragment
import hk.collaction.freehkkai.R
import hk.collaction.freehkkai.ui.fragment.SettingsFragment

class SettingsActivity : BaseFragmentActivity() {
    override var fragment: Fragment? = SettingsFragment.newInstance()
    override var titleId: Int? = R.string.title_activity_settings
}