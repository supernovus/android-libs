package com.luminaryn.common

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

/**
 * A minimalist table builder where all children should have the same layout parameters.
 *
 * @param context The context to pass to created View objects.
 * @param table A TableLayout instance to add the rows to (optional).
 * @param rowLayout The layout to be applied to all rows (auto-generated).
 * @param childLayout The layout to be applied to all row children (auto-generated).
 *
 * @property context The context to pass to created View objects.
 * @property table The TableLayout instance to add the rows to.
 * @property rowLayout The default layout to be applied to rows.
 * @property childLayout default The layout to be applied to all row children.
 */
class TableBuilder(
    val context: Context,
    val table: TableLayout? = null,
    val rowLayout: TableLayout.LayoutParams = TableLayout.LayoutParams(),
    val childLayout: TableRow.LayoutParams = TableRow.LayoutParams(),
) {

    /**
     * An internal class used to build the Rows.
     *
     * You probably don't need to construct this manually, instead use the row() method.
     *
     * @property context The context to pass to the TableRow() constructor.
     * @property rowLayout The layout parameters to apply to this row.
     * @property childLayout The layout parameters to apply to children of this row.
     * @constructor Build a new Row instance.
     */
    class Row(
        val context: Context,
        val rowLayout: TableLayout.LayoutParams,
        val childLayout: TableRow.LayoutParams
    ) {
        /**
         * @property element The underlying TableRow element.
         */
        val element = TableRow(context)

        init {
            element.layoutParams = rowLayout // Apply the rowLayout.
        }

        /**
         * Add a child View to this row, and set it's layout parameters.
         *
         * @param childView The view we are adding.
         * @param newLayoutParams If true, use new layout params instead of the default.
         * @return The childView is returned after adding it to the row and assigning the layout.
         */
        @JvmOverloads
        fun add (childView: View, newLayoutParams: Boolean = false) : View {
            val childLayout = if (newLayoutParams) TableRow.LayoutParams() else this.childLayout
            childView.layoutParams = childLayout
            element.addView(childView)
            return childView
        }

        /**
         * Add a TextView child to this row, and set it's text.
         *
         * @param text The text to display in the TextView.
         * @param newLayoutParams If true, use new layout params instead of the default.
         * @return The TextView
         */
        @JvmOverloads
        fun addText (text: CharSequence? = null, newLayoutParams: Boolean = false) : TextView {
            val view = TextView(context)
            if (text != null)
                view.text = text
            add(view, newLayoutParams)
            return view
        }

        /**
         * A shortcut for making a "header" column.
         */
        fun header (text: CharSequence? = null, newLayoutParams: Boolean = false) : TextView {
            val elem = addText(text, newLayoutParams)
            elem.setTypeface(null, Typeface.BOLD)
            return elem
        }

        /**
         * Set a tag on the row element.
         */
        @JvmOverloads
        fun tag (tag: Any, id: Int? = null) {
            if (id != null) {
                element.setTag(id, tag)
            } else {
                element.tag = tag
            }
        }
    }

    /**
     * Create a new Row object.
     *
     * If a [table] property is set, the new row will be added to it automatically.
     *
     * @param newRowLayout If true, use new layout params for the row instead of the default.
     * @param newChildLayout If true, use new layout params for the children instead of the default.
     * @param index The index at which to insert the row (or -1 for the end of the table.)
     * @return The newly created Row object will be returned.
     */
    @JvmOverloads
    fun row(newRowLayout: Boolean = false, newChildLayout: Boolean = false, index: Int = -1) : Row {
        val rowLayout = if (newRowLayout) TableLayout.LayoutParams() else  this.rowLayout
        val childLayout = if (newChildLayout) TableRow.LayoutParams() else this.childLayout
        val row = Row(context, rowLayout, childLayout)
        table?.addView(row.element, index)
        return row
    }

    /**
     * Create a new Row object, and insert it before or after the specified existing View object.
     *
     * If the [table] property is not set, this simply returns the new Row like row() does.
     * If the specified View is not found in the [table] children, the new Row will be added
     * at the end of the table.
     *
     * @param existingChild  The View instance we want to create a row after in the [table].
     * @param insertAfter Insert after the existing element instead of before.
     * @param newRowLayout If true, use new layout params for the row instead of the default.
     * @param newChildLayout If true, use new layout params for the children instead of the default.
     * @return The newly created Row object.
     */
    @JvmOverloads
    fun rowAt(existingChild: View, insertAfter: Boolean = false, newRowLayout: Boolean = false, newChildLayout: Boolean = false) : Row {
        val existingIndex = table?.indexOfChild(existingChild) ?: -1
        val insertIndex = if (insertAfter && existingIndex != -1) existingIndex + 1 else existingIndex
        return row(newRowLayout, newChildLayout, insertIndex)
    }

    /**
     * Create a new Row object, and insert it before or after the specified existing Row object.
     *
     * If the [table] property is not set, this simply returns the new Row like row() does.
     * If the specified View is not found in the [table] children, the new Row will be added
     * at the end of the table.
     *
     * @param existingRow  The Row instance we want to create a row after in the [table].
     * @param insertAfter Insert after the existing element instead of before.
     * @param newRowLayout If true, use new layout params for the row instead of the default.
     * @param newChildLayout If true, use new layout params for the children instead of the default.
     * @return The newly created Row object.
     */
    @JvmOverloads
    fun rowAt(existingRow: Row, insertAfter: Boolean = false, newRowLayout: Boolean = false, newChildLayout: Boolean = false) : Row {
        return rowAt(existingRow.element, insertAfter, newRowLayout, newChildLayout)
    }

    /**
     * Find existing views with a specific tag.
     *
     * Only works if the [table] property is set.
     *
     * @param tag See Views.getViewsByTag
     * @param id See Views.getViewsByTag
     * @param recurseDepth Maximum depth of recursion. See Views.getViewsByTag for details.
     */
    @JvmOverloads
    fun findWithTag (tag: Any, id: Int? = null, recurseDepth: Int = ROWS) : ArrayList<View>? {
        if (table != null) {
            return Views.getViewsByTag(table, tag, id, recurseDepth)
        } else {
            return null
        }
    }

    /**
     * Find existing views with a specific tag.
     *
     * Only works if the [table] property is set.
     *
     * @param tags See Views.getViewsByTags
     * @param defTag See Views.getViewsByTags
     * @param recurseDepth See Views.getViewsByTags
     */
    @JvmOverloads
    fun findWithTags (tags: HashMap<Int,Any>, defTag: Any? = null, recurseDepth: Int = ROWS) : ArrayList<View>? {
        if (table != null) {
            return Views.getViewsByTags(table, tags, defTag, recurseDepth)
        } else {
            return null
        }
    }

    companion object {
        const val ROWS = 0
        const val COLS = 1
        const val ANY = -1
    }

}