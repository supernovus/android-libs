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
    class TagMatcher (private val tag: Any, private val id: Int?) : ViewMatcher {
        override fun matches(view: View): Boolean {
            if (id != null) { // Compare tag with a specific id.
                return view.getTag(id) == tag
            } else { // Compare default tag.
                return view.tag == tag
            }
        }
    }

    /**
     * A ViewMatcher that checks multiple tags at once and only returns if all match.
     *
     * @param tags A HashMap where the key is the Tag ID, and the value is the Tag value.
     * @param defTag If specified as a non-null value, the "default" tag must match this.
     */
    class MultipleTagMatcher (private val tags: HashMap<Int,Any>, private val defTag: Any?) : ViewMatcher {
        override fun matches(view: View): Boolean {
            if (defTag != null) { // First off check the optional defTag if it was set.
                if (view.tag != defTag) return false
            }
            for ((tagID, tagVal) in tags) {
                if (view.getTag(tagID) != tagVal) return false
            }
            return true // All tags matched.
        }
    }

    /**
     * Get all of the views with the specified tag.
     *
     * @param root  The ViewGroup we're searching in.
     * @param tag  The tag we're looking for.
     * @param id Optional tag id, if null we look for the "default" tag.
     * @param recurseDepth  Maximum depth of recursion, 0 means none, -1 means no limit.
     *
     * @return An ArrayList of all matching views.
     */
    @JvmOverloads
    fun getViewsByTag (root: ViewGroup, tag: Any, id: Int? = null, recurseDepth: Int = 0) : ArrayList<View> {
        return getMatchingViews(root, TagMatcher(tag, id), recurseDepth)
    }

    /**
     * Get all of the views with all of the specified tags.
     *
     * @param root  The ViewGroup we're searching in.
     * @param tags  The tags we're looking for, see MultipleTagMatcher for details.
     * @param defTag Optional default tag value, see MultipleTagMatcher for details.
     * @param recurseDepth  Maximum depth of recursion, 0 means none, -1 means no limit.
     *
     * @return An ArrayList of all matching views.
     */
    @JvmOverloads
    fun getViewsByTags (root: ViewGroup, tags: HashMap<Int,Any>, defTag: Any? = null, recurseDepth: Int = 0) : ArrayList<View> {
        return getMatchingViews(root, MultipleTagMatcher(tags, defTag), recurseDepth)
    }

}