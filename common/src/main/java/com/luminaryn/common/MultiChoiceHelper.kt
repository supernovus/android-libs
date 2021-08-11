package com.luminaryn.common

import android.app.Activity
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.LongSparseArray
import android.util.SparseBooleanArray
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Checkable
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper class to reproduce ListView's modal MultiChoice mode with a RecyclerView.
 *
 * Adapted from the original Java version, and reworked a bit.
 *
 * https://gist.github.com/cbeyls/32b2b7a33caee29e97bfe81a949ba247
 *
 * The original author since writing this has rewritten it significantly, see the
 * new version and decide if we want to move to it.
 *
 * https://github.com/cbeyls/fosdem-companion-android/blob/master/app/src/main/java/be/digitalia/fosdem/widgets/MultiChoiceHelper.kt
 *
 * @author Christophe Beyls
 */
class MultiChoiceHelper(
    val activity: Activity,
    val adapter: RecyclerView.Adapter<*>
) {
    /**
     * A handy ViewHolder base class which works with the MultiChoiceHelper
     * and reproduces the default behavior of a ListView.
     */
    abstract class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var clickListener: View.OnClickListener? = null
        var multiChoiceHelper: MultiChoiceHelper? = null

        fun updateCheckedState(position: Int) {
            val isChecked = multiChoiceHelper!!.isItemChecked(position)
            if (itemView is Checkable) {
                (itemView as Checkable).isChecked = isChecked
            } else {
                itemView.isActivated = isChecked
            }
        }

        fun setOnClickListener(clickListener: View.OnClickListener?) {
            this.clickListener = clickListener
        }

        fun bind(multiChoiceHelper: MultiChoiceHelper?, position: Int) {
            this.multiChoiceHelper = multiChoiceHelper
            if (multiChoiceHelper != null) {
                updateCheckedState(position)
            }
        }

        val isMultiChoiceActive: Boolean
            get() = multiChoiceHelper != null && multiChoiceHelper!!.checkedItemCount > 0

        init {
            itemView.setOnClickListener() { view ->
                if (isMultiChoiceActive) {
                    val position: Int = getAdapterPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        multiChoiceHelper!!.toggleItemChecked(position, false)
                        updateCheckedState(position)
                    }
                } else {
                    if (clickListener != null) {
                        clickListener!!.onClick(view)
                    }
                }
            }
            itemView.setOnLongClickListener() { view ->
                if (multiChoiceHelper == null || isMultiChoiceActive) {
                    false
                }
                else {
                    val position: Int = getAdapterPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        multiChoiceHelper!!.setItemChecked(position, true, false)
                        updateCheckedState(position)
                    }
                    true
                }
            }
        }
    }

    interface MultiChoiceModeListener : ActionMode.Callback {
        /**
         * Called when an item is checked or unchecked during selection mode.
         *
         * @param mode     The [ActionMode] providing the selection startSupportActionModemode
         * @param position Adapter position of the item that was checked or unchecked
         * @param id       Adapter ID of the item that was checked or unchecked
         * @param checked  `true` if the item is now checked, `false`
         * if the item is now unchecked.
         */
        fun onItemCheckedStateChanged(mode: ActionMode?, position: Int, id: Long, checked: Boolean)
    }

    var checkedItemPositions: SparseBooleanArray?
        private set
    private var checkedIdStates: LongSparseArray<Int>? = null
    var checkedItemCount = 0
        private set
    private var multiChoiceModeCallback: MultiChoiceModeWrapper? = null
    var choiceActionMode: ActionMode? = null
    val context: Context
        get() = activity

    fun setMultiChoiceModeListener(listener: MultiChoiceModeListener?) {
        if (listener == null) {
            multiChoiceModeCallback = null
            return
        }
        if (multiChoiceModeCallback == null) {
            multiChoiceModeCallback = MultiChoiceModeWrapper()
        }
        multiChoiceModeCallback!!.setWrapped(listener)
    }

    fun isItemChecked(position: Int): Boolean {
        return checkedItemPositions!![position]
    }

    val checkedItemIds: LongArray
        get() {
            val idStates: LongSparseArray<Int> = checkedIdStates ?: return LongArray(0)
            val count: Int = idStates.size()
            val ids = LongArray(count)
            for (i in 0 until count) {
                ids[i] = idStates.keyAt(i)
            }
            return ids
        }

    fun clearChoices() {
        if (checkedItemCount > 0) {
            val start = checkedItemPositions!!.keyAt(0)
            val end = checkedItemPositions!!.keyAt(checkedItemPositions!!.size() - 1)
            checkedItemPositions!!.clear()
            if (checkedIdStates != null) {
                checkedIdStates!!.clear()
            }
            checkedItemCount = 0
            adapter.notifyItemRangeChanged(start, end - start + 1)
            if (choiceActionMode != null) {
                choiceActionMode!!.finish()
            }
        }
    }

    fun setItemChecked(position: Int, value: Boolean, notifyChanged: Boolean) {
        // Start selection mode if needed. We don't need to if we're unchecking something.
        if (value) {
            startSupportActionModeIfNeeded()
        }
        val oldValue = checkedItemPositions!![position]
        checkedItemPositions!!.put(position, value)
        if (oldValue != value) {
            val id: Long = adapter.getItemId(position)
            if (checkedIdStates != null) {
                if (value) {
                    checkedIdStates!!.put(id, position)
                } else {
                    checkedIdStates!!.delete(id)
                }
            }
            if (value) {
                checkedItemCount++
            } else {
                checkedItemCount--
            }
            if (notifyChanged) {
                adapter.notifyItemChanged(position)
            }
            if (choiceActionMode != null) {
                multiChoiceModeCallback!!.onItemCheckedStateChanged(
                    choiceActionMode,
                    position,
                    id,
                    value
                )
                if (checkedItemCount == 0) {
                    choiceActionMode!!.finish()
                }
            }
        }
    }

    fun toggleItemChecked(position: Int, notifyChanged: Boolean) {
        setItemChecked(position, !isItemChecked(position), notifyChanged)
    }

    fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState()
        savedState.checkedItemCount = checkedItemCount
        savedState.checkStates = checkedItemPositions!!.clone()
        if (checkedIdStates != null) {
            savedState.checkedIdStates = checkedIdStates!!.clone()
        }
        return savedState
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null && checkedItemCount == 0) {
            val savedState = state as SavedState
            checkedItemCount = savedState.checkedItemCount
            checkedItemPositions = savedState.checkStates
            checkedIdStates = savedState.checkedIdStates
            if (checkedItemCount > 0) {
                // Empty adapter is given a chance to be populated before completeRestoreInstanceState()
                if (adapter.getItemCount() > 0) {
                    confirmCheckedPositions()
                }
                activity.getWindow().getDecorView()
                    .post(Runnable { completeRestoreInstanceState() })
            }
        }
    }

    fun completeRestoreInstanceState() {
        if (checkedItemCount > 0) {
            if (adapter.getItemCount() == 0) {
                // Adapter was not populated, clear the selection
                confirmCheckedPositions()
            } else {
                startSupportActionModeIfNeeded()
            }
        }
    }

    private fun startSupportActionModeIfNeeded() {
        if (choiceActionMode == null) {
            checkNotNull(multiChoiceModeCallback) { "No callback set" }
            choiceActionMode = activity.startActionMode(multiChoiceModeCallback)
        }
    }

    class SavedState : Parcelable {
        var checkedItemCount = 0
        var checkStates: SparseBooleanArray? = null
        var checkedIdStates: LongSparseArray<Int>? = null

        internal constructor() {}
        internal constructor(input: Parcel) {
            checkedItemCount = input.readInt()
            checkStates = input.readSparseBooleanArray()
            val n = input.readInt()
            if (n >= 0) {
                checkedIdStates = LongSparseArray(n)
                for (i in 0 until n) {
                    val key = input.readLong()
                    val value = input.readInt()
                    checkedIdStates!!.append(key, value)
                }
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeInt(checkedItemCount)
            out.writeSparseBooleanArray(checkStates)
            val n = checkedIdStates?.size() ?: -1
            out.writeInt(n)
            for (i in 0 until n) {
                out.writeLong(checkedIdStates!!.keyAt(i))
                out.writeInt(checkedIdStates!!.valueAt(i))
            }
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
            @JvmField val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    fun confirmCheckedPositions() {
        if (checkedItemCount == 0) {
            return
        }
        val itemCount: Int = adapter.getItemCount()
        var checkedCountChanged = false
        val checkedIdStates = this.checkedIdStates
        if (itemCount == 0) {
            // Optimized path for empty adapter: remove all items.
            checkedItemPositions!!.clear()
            if (checkedIdStates != null) {
                checkedIdStates.clear()
            }
            checkedItemCount = 0
            checkedCountChanged = true
        } else if (checkedIdStates != null) {
            // Clear out the positional check states, we'll rebuild it below from IDs.
            checkedItemPositions!!.clear()
            var checkedIndex = 0
            while (checkedIndex < checkedIdStates.size()) {
                val id: Long = checkedIdStates.keyAt(checkedIndex)
                val lastPos: Int = checkedIdStates.valueAt(checkedIndex)
                if (lastPos >= itemCount || id != adapter.getItemId(lastPos)) {
                    // Look around to see if the ID is nearby. If not, uncheck it.
                    val start = Math.max(0, lastPos - CHECK_POSITION_SEARCH_DISTANCE)
                    val end = Math.min(lastPos + CHECK_POSITION_SEARCH_DISTANCE, itemCount)
                    var found = false
                    for (searchPos in start until end) {
                        val searchId: Long = adapter.getItemId(searchPos)
                        if (id == searchId) {
                            found = true
                            checkedItemPositions!!.put(searchPos, true)
                            checkedIdStates.setValueAt(checkedIndex, searchPos)
                            break
                        }
                    }
                    if (!found) {
                        checkedIdStates.delete(id)
                        checkedIndex--
                        checkedItemCount--
                        checkedCountChanged = true
                        if (choiceActionMode != null && multiChoiceModeCallback != null) {
                            multiChoiceModeCallback!!.onItemCheckedStateChanged(
                                choiceActionMode,
                                lastPos,
                                id,
                                false
                            )
                        }
                    }
                } else {
                    checkedItemPositions!!.put(lastPos, true)
                }
                checkedIndex++
            }
        } else {
            // If the total number of items decreased, remove all out-of-range check indexes.
            var i = checkedItemPositions!!.size() - 1
            while (i >= 0 && checkedItemPositions!!.keyAt(i) >= itemCount) {
                if (checkedItemPositions!!.valueAt(i)) {
                    checkedItemCount--
                    checkedCountChanged = true
                }
                checkedItemPositions!!.delete(checkedItemPositions!!.keyAt(i))
                i--
            }
        }
        if (checkedCountChanged && choiceActionMode != null) {
            if (checkedItemCount == 0) {
                choiceActionMode!!.finish()
            } else {
                choiceActionMode!!.invalidate()
            }
        }
    }

    internal inner class AdapterDataSetObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            confirmCheckedPositions()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            confirmCheckedPositions()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            confirmCheckedPositions()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            confirmCheckedPositions()
        }
    }

    internal inner class MultiChoiceModeWrapper : MultiChoiceModeListener {
        private var wrapped: MultiChoiceModeListener? = null

        fun setWrapped(wrapped: MultiChoiceModeListener) {
            this.wrapped = wrapped
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return wrapped?.onCreateActionMode(mode, menu) ?: false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return wrapped?.onPrepareActionMode(mode, menu) ?: false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return wrapped?.onActionItemClicked(mode, item) ?: false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            wrapped?.onDestroyActionMode(mode)
            choiceActionMode = null
            clearChoices()
        }

        override fun onItemCheckedStateChanged(
            mode: ActionMode?,
            position: Int,
            id: Long,
            checked: Boolean
        ) {
            wrapped!!.onItemCheckedStateChanged(mode, position, id, checked)
        }
    }

    companion object {
        private const val CHECK_POSITION_SEARCH_DISTANCE = 20
    }

    /**
     * Make sure this constructor is called before setting the adapter on the RecyclerView
     * so this class will be notified before the RecyclerView in case of data set changes.
     */
    init {
        adapter.registerAdapterDataObserver(AdapterDataSetObserver())
        checkedItemPositions = SparseBooleanArray(0)
        if (adapter.hasStableIds()) {
            checkedIdStates = LongSparseArray(0)
        }
    }
}