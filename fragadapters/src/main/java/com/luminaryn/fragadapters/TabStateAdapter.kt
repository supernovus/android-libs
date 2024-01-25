package com.luminaryn.fragadapters

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

abstract class TabStateAdapter (activity: AppCompatActivity, val tabs: TabLayout, val pager: ViewPager2) : com.luminaryn.fragadapters.PaneStateAdapter(activity) {

    override val panes: ArrayList<TabPane> = ArrayList()

    open val notifyOnAdd: Boolean = false
    open val notifyOnRemove: Boolean = false
    open val notifyOnClear: Boolean = false

    open val defaultManager: TabPane.Manager = TabPane.DefaultManager()

    val mediator: TabLayoutMediator

    init {
        setupAdapter(pager)
        mediator = TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = panes[position].title
        }
        mediator.attach()
    }

    fun addTab(title: String,
               notify: Boolean,
               manager: TabPane.Manager,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {

        val tabPane = TabPane(this, title, manager, initializer)

        if (at != null) {
            panes.add(at, tabPane)
        } else {
            panes.add(tabPane)
        }

        if (notify) {
            notifyItemInserted(tabPane.position)
        }

        return tabPane
    }

    fun addTab(strId: Int,
               notify: Boolean,
               manager: TabPane.Manager,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(getString(strId), notify, manager, at, initializer)
    }

    fun addTab(title: String,
               notify: Boolean,
               manager: TabPane.Manager,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(title, notify, manager, null, initializer)
    }

    fun addTab(strId: Int,
               notify: Boolean,
               manager: TabPane.Manager,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(getString(strId), notify, manager, initializer)
    }

    fun addTab(title: String,
               manager: TabPane.Manager,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(title, notifyOnAdd, manager, at, initializer)
    }

    fun addTab(strId: Int,
               manager: TabPane.Manager,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(strId, notifyOnAdd, manager, at, initializer)
    }

    fun addTab(title: String,
               manager: TabPane.Manager,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(title, notifyOnAdd, manager, initializer)
    }

    fun addTab(strId: Int,
               manager: TabPane.Manager,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(strId, notifyOnAdd, manager, initializer)
    }

    fun addTab(title: String,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(title, notifyOnAdd, defaultManager, at, initializer)
    }

    fun addTab(strId: Int,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(strId, notifyOnAdd, defaultManager, at, initializer)
    }

    fun addTab(title: String,
               notify: Boolean,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(title, notify, defaultManager, at, initializer)
    }

    fun addTab(strId: Int,
               notify: Boolean,
               at: Int?,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(strId, notify, defaultManager, at, initializer)
    }

    fun addTab(title: String,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(title, notifyOnAdd, defaultManager, initializer)
    }

    fun addTab(strId: Int,
               initializer: (TabPane) -> Fragment): TabPane {
        return addTab(strId, notifyOnAdd, defaultManager, initializer)
    }

    @JvmOverloads
    fun removeTab(index: Int, notify: Boolean = notifyOnRemove) {
        panes.removeAt(index)

        if (notify) {
            notifyItemRemoved(index)
        }
    }

    @JvmOverloads
    fun removeTab(tabPane: TabPane, notify: Boolean = notifyOnRemove) {
        removeTab(tabPane.position, notify)
    }

    @SuppressLint("NotifyDataSetChanged")
    @JvmOverloads
    fun clear(notify: Boolean = notifyOnClear) {
        panes.clear()
        if (notify) {
            notifyDataSetChanged()
        }
    }

    open class TabPane(val adapter: TabStateAdapter,
                       val title: String,
                       val manager: Manager,
                       val initializer: (TabPane) -> Fragment
    ) : Pane {

        open val position: Int
            get() = adapter.panes.indexOf(this)

        override val id = manager.getId(this)

        override fun getFragment(): Fragment {
            return initializer(this)
        }

        open fun remove(): Boolean {
            return manager.removePane(this)
        }

        interface Manager {
            fun getId(tabPane: TabPane): Long
            fun removePane(tabPane: TabPane): Boolean
        }

        /**
         * A default class that implements a minimal Manager interface.
         */
        open class DefaultManager : Manager {
            override fun getId(tabPane: TabPane): Long {
                return randomId()
            }

            override fun removePane(tabPane: TabPane): Boolean {
                tabPane.adapter.removeTab(tabPane)
                return true
            }
        }

        companion object {
            fun randomId(): Long {
                return Calendar.getInstance().timeInMillis + Random.nextLong()
            }
        }

    }

}