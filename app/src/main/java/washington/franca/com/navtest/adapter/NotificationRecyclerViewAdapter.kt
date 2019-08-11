package washington.franca.com.navtest.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import washington.franca.com.navtest.R
import washington.franca.com.navtest.model.Notification
import java.util.*
import kotlin.collections.ArrayList

class NotificationRecyclerViewAdapter :
    SwipeableRecyclerViewAdapter<NotificationRecyclerViewAdapter.ViewHolder>(ItemTouchHelper.RIGHT.or(ItemTouchHelper.LEFT))
{
    var items = LinkedList<Notification>()
    var listener:OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorError, typedValue, true)
        swipeBackground = ContextCompat.getDrawable(context, R.drawable.swipe_action_delete)
        swipeIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
        swipeColorAction = typedValue.data
        itemPadding = 16
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.fragment_notification_item, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items.getOrNull(position)?.let { item->
            val context = holder.typeTextView.context
            holder.typeTextView.text = context.resources.getStringArray(R.array.notifications_type)[item.type]
            holder.titleTextView.text = item.title
            holder.bodyTextView.text = item.body
            holder.dateTextView.text = item.relativeTime()
            if(item.status) {
                holder.statusTextView.setText(R.string.notifications_status_read)
                holder.statusTextView.setTextColor(Color.parseColor("#40ff40"))
                ImageViewCompat.setImageTintList(holder.statusImageView, ColorStateList.valueOf(Color.parseColor("#40ff40")))
            } else {
                holder.statusTextView.setText(R.string.notifications_status_unread)
                holder.statusTextView.setTextColor(Color.parseColor("#40404040"))
                ImageViewCompat.setImageTintList(holder.statusImageView, ColorStateList.valueOf(Color.parseColor("#40404040")))
            }
            holder.container.setOnClickListener {
                listener?.onItemClick(holder.adapterPosition, holder, items[holder.adapterPosition])
            }
        }
    }

    @Synchronized
    override fun onSwipe(viewHolder: ViewHolder, direction: Int) {
        synchronized(this.items) {
            val position = viewHolder.adapterPosition
            val notification = items[position]
            this.items.removeAt(position)
            notifyItemRemoved(position)
            listener?.onItemRemove(position, viewHolder, notification)
        }
    }

    @Synchronized
    fun addAll(notifications:ArrayList<Notification>) {
        synchronized(this.items) {
            val init = items.size
            items.addAll(notifications)
            notifyItemRangeInserted(init, notifications.size)
        }
    }

    @Synchronized
    fun add(item:Notification) {
        synchronized(this.items) {
            this.items.addFirst(item)
            notifyItemInserted(0)
        }
    }

    @Synchronized
    fun add(position:Int, item:Notification) {
        synchronized(this.items) {
            this.items.add(position, item)
            notifyItemInserted(position)
        }
    }

    @Synchronized
    fun update(item:Notification) {
        synchronized(this.items) {
            val index = this.items.indexOf(item)
            this.items[index] = item
            notifyItemChanged(index)
        }
    }

    @Synchronized
    fun clear() {
        synchronized(this.items) {
            this.items.clear()
            notifyDataSetChanged()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView:TextView = itemView.findViewById(R.id.type_text)
        val titleTextView:TextView = itemView.findViewById(R.id.title_text)
        val bodyTextView:TextView = itemView.findViewById(R.id.body_text)
        val dateTextView:TextView = itemView.findViewById(R.id.date_text)
        val statusTextView:TextView = itemView.findViewById(R.id.status_text)
        val statusImageView:ImageView = itemView.findViewById(R.id.status_image)
        val container:ViewGroup = itemView.findViewById(R.id.container)
    }

    interface OnItemClickListener {
        fun onItemClick(index:Int, holder:ViewHolder, notification: Notification)
        fun onItemRemove(index:Int, holder:ViewHolder, notification: Notification)
    }
}