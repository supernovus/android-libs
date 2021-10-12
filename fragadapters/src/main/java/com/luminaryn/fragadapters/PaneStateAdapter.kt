package com.luminaryn.fragadapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

abstract class PaneStateAdapter(protected val activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    protected abstract val panes: List<Pane>

    override fun getItemCount(): Int {
        return panes.size
    }

    override fun createFragment(position: Int): Fragment {
        return panes[position].getFragment()
    }

    override fun getItemId(position: Int): Long {
        return panes[position].id
    }

    override fun containsItem(itemId: Long): Boolean {
        return panes.any { it.id == itemId }
    }

    open fun getPaneAt(position: Int): Pane? {
        val pos = if (position < 0) panes.size + position else position
        if (pos < panes.size) {
            return panes[pos]
        }
        return null
    }

    interface Pane {
        val id: Long
        fun getFragment(): Fragment
    }

    fun setupAdapter(pager: ViewPager2) {
        pager.adapter = this
    }

    fun getString(id: Int): String {
        return activity.getString(id)
    }

    fun getString(id: Int, vararg formatArgs: Any): String {
        return activity.getString(id, formatArgs)
    }

    fun getStringArray(id: Int): Array<String> {
        return activity.resources.getStringArray(id)
    }

    fun getStringList(id: Int): List<String> {
        return getStringArray(id).toList()
    }

    fun getFragment(index: Int): Fragment? {
        val fm = activity.supportFragmentManager
        return fm.findFragmentByTag("f"+getItemId(index))
            ?: fm.findFragmentById(getItemId(index).toInt())
    }

}