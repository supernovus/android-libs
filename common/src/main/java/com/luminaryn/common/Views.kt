package com.luminaryn.common

import android.view.ViewGroup
import android.view.View

/**
 * Simple helper functions for working with Views
 */
object Views {
    /**
     * Get all of the views matching the specified ViewMatcher instance.
     *
     * @param root  The ViewGroup we're searching in.
     * @param viewMatcher  The matcher instance to check views with.
     * @param recurseDepth  Maximum depth of recursion, 0 means none, -1 means no limit.
     *
     * @return An ArrayList of all matching views.
     */
    @JvmOverloads
    fun getMatchingViews (root: ViewGroup, viewMatcher: ViewMatcher, recurseDepth: Int = 0) : ArrayList<View> {
        val views = ArrayList<View>()
        val childCount = root.childCount
        for (i in 0 until childCount) {
            val child = root.getChildAt(i)
            if (viewMatcher.matches(child)) {
                views.add(child)
            }
            if (recurseDepth != 0 && child is ViewGroup) {
                val nextDepth = if (recurseDepth > 0) recurseDepth - 1 else recurseDepth
                views.addAll(getMatchingViews(child, viewMatcher, nextDepth))
            }
        }
        return views
    }

    /**
     * The interface used by getMatchingViews() to determine matches.
     */
    interface ViewMatcher {
        /**
         * The method that determines a match.
         *
         * @param view  The view we're testing.
         */
        fun matches (view: View): Boolean
    }

    /**
     * A ViewMatcher that checks for a specific tag.
     *
     * @param tag The tag we're searching for.
     */
    class TagMatcher (private val tag: Any) : ViewMatcher {
        override fun matches(view: View): Boolean {
            return view.tag == tag
        }
    }

    /**
     * Get all of the views with the specified tag.
     *
     * @param root  The ViewGroup we're searching in.
     * @param tag  The tag we're looking for.
     * @param recurseDepth  Maximum depth of recursion, 0 means none, -1 means no limit.
     *
     * @return An ArrayList of all matching views.
     */
    @JvmOverloads
    fun getViewsByTag (root: ViewGroup, tag: Any, recurseDepth: Int = 0) : ArrayList<View> {
        return getMatchingViews(root, TagMatcher(tag), recurseDepth)
    }

}