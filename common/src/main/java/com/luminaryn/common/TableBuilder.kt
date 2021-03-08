package com.luminaryn.common

import android.content.Context
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

/**
 * A minimalist table builder where all children should have the same layout parameters.
 *
 * @property context The context to pass to created View objects.
 * @property table A TableLayout instance to add the rows to (optional).
 * @property rowLayout The layout to be applied to all rows (auto-generated).
 * @property childLayout The layout to be applied to all row children (auto-generated).
 * @constructor Build a new TableBuilder object.
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
    }

    /**
     * Create a new Row object.
     *
     * If a [table] property is set, the new row will be added to it automatically.
     *
     * @param newRowLayout If true, use new layout params for the row instead of the default.
     * @param newChildLayout If true, use new layout params for the children instead of the default.
     * @return The newly created Row object will be returned.
     */
    @JvmOverloads
    fun row(newRowLayout: Boolean = false, newChildLayout: Boolean = false) : Row {
        val rowLayout = if (newRowLayout) TableLayout.LayoutParams() else  this.rowLayout
        val childLayout = if (newChildLayout) TableRow.LayoutParams() else this.childLayout
        val row = Row(context, rowLayout, childLayout)
        table?.addView(row.element)
        return row
    }

}