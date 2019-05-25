package ru.deledzis.vk.base

import android.support.v4.app.Fragment
import ru.deledzis.vk.MainActivity

open class BaseFragment : Fragment() {
    protected lateinit var mActivity: MainActivity
}
