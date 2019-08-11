package washington.franca.com.navtest.adapter

import android.graphics.Canvas
import android.graphics.Color
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.absoluteValue

abstract class SwipeableRecyclerViewAdapter<VH : RecyclerView.ViewHolder?>(swipeDirs:Int) : RecyclerView.Adapter<VH>() {
    private val swipeToDeleteCallback = Callback(swipeDirs)
    var swipeIcon: Drawable? = null
    var swipeBackground: Drawable? = null
    @ColorRes var swipeColorAction:Int = 0
    var swipeColorDefault:Int = Color.parseColor("#80808080")
    var itemPaddingTop: Int = 0
    var itemPaddingLeft: Int = 0
    var itemPaddingRight: Int = 0
    var itemPaddingBottom: Int = 0
    var itemPadding:Int
        set(value) { itemPaddingTop = value; itemPaddingBottom = value; itemPaddingLeft = value; itemPaddingRight = value }
        get() {return itemPaddingTop}

    abstract fun onSwipe(viewHolder:VH, direction:Int)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    inner class Callback(swipeDirs: Int) : ItemTouchHelper.SimpleCallback(0, swipeDirs) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean { return false }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            @Suppress("UNCHECKED_CAST")
            this@SwipeableRecyclerViewAdapter.onSwipe(viewHolder as VH, direction)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if(!isCurrentlyActive) return

            val itemView = viewHolder.itemView

            val backgroundCornerOffset = itemPaddingRight + 50 //so background is behind the rounded corners of itemView

            val iconWidth = swipeIcon?.intrinsicWidth?:0
            val iconHeight = swipeIcon?.intrinsicHeight?:0
            val iconMargin = (itemView.measuredHeight - iconHeight) / 2
            val iconTop = itemView.top + (itemView.height - iconHeight) / 2
            val iconBottom = iconTop + iconHeight

            when {
                dX > 0 -> { // Swiping to the right
                    val backgroundColor:Int
                    val iconColor:Int
                    if((dX / itemView.width) > 0.5) {
                        backgroundColor = swipeColorAction
                        iconColor = Color.WHITE
                    } else {
                        backgroundColor = swipeColorDefault
                        iconColor = swipeColorAction
                    }

                    swipeBackground?.let {
                        DrawableCompat.setTint(it, backgroundColor)
                        it.setBounds(
                            itemView.left + itemPaddingLeft,
                            itemView.top + itemPaddingTop,
                            itemView.left + dX.toInt() + backgroundCornerOffset - itemPaddingRight,
                            itemView.bottom - itemPaddingBottom
                        )
                        it.draw(c)
                    }


                    swipeIcon?.let {
                        DrawableCompat.setTint(it, iconColor)
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = itemView.left + iconMargin + iconWidth
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        if(dX > iconRight) it.draw(c)
                    }
                }
                dX < 0 -> { // Swiping to the left
                    val backgroundColor:Int
                    val iconColor:Int
                    if((dX.absoluteValue / itemView.width) > 0.5) {
                        backgroundColor = swipeColorAction
                        iconColor = Color.WHITE
                    } else {
                        backgroundColor = swipeColorDefault
                        iconColor = swipeColorAction
                    }

                    swipeBackground?.let {
                        DrawableCompat.setTint(it, backgroundColor)
                        it.setBounds(
                            itemView.right + dX.toInt() - backgroundCornerOffset + itemPaddingLeft,
                            itemView.top + itemPaddingTop,
                            itemView.right - itemPaddingRight,
                            itemView.bottom - itemPaddingBottom
                        )
                        it.draw(c)
                    }

                    swipeIcon?.let {
                        DrawableCompat.setTint(it, iconColor)
                        val iconLeft = itemView.right - iconMargin - iconWidth
                        val iconRight = itemView.right - iconMargin
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        if(itemView.width + dX < iconLeft) it.draw(c)
                    }
                }
                else -> { // view is unSwiped
                    swipeBackground?.setBounds(0, 0, 0, 0)
                    swipeIcon?.setBounds(0, 0, 0, 0)
                }
            }
        }
    }
}