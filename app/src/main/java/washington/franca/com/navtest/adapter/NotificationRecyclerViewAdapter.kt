package washington.franca.com.navtest.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import washington.franca.com.navtest.R
import washington.franca.com.navtest.model.Notification

class NotificationRecyclerViewAdapter : RecyclerView.Adapter<NotificationRecyclerViewAdapter.ViewHolder>() {
    var items:List<Notification> = emptyList()
    var listener:OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_notification_item, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items.getOrNull(position)?.let { item->
            val context = holder.typeTextView.context
            holder.typeTextView.text = context.resources.getStringArray(R.array.notifications_type)[item.type]
            holder.titleTextView.text = item.title
            holder.bodyTextView.text = item.body
            holder.dateTextView.text = DateUtils.getRelativeTimeSpanString(item.date, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
            holder.statusTextView.text = if(item.status) "Read" else "Unread"
            holder.closeButton.setOnClickListener {
                listener?.onItemClick(this, holder.itemView, holder.adapterPosition, holder.itemId)
            }
        }
    }

    var data:List<Notification>
    get() = items
    set(items) {
        if(this.items.isEmpty()) {
            this.items = items
            notifyDataSetChanged()
            return
        }

        val diff = DiffUtil.calculateDiff(object:DiffUtil.Callback(){
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = this@NotificationRecyclerViewAdapter.items[oldItemPosition]
                val newItem = items[newItemPosition]
                return oldItem.id == newItem.id
            }

            override fun getOldListSize(): Int = this@NotificationRecyclerViewAdapter.items.size

            override fun getNewListSize(): Int = items.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = this@NotificationRecyclerViewAdapter.items[oldItemPosition]
                val newItem = items[newItemPosition]
                return oldItem.id == newItem.id &&
                        oldItem.title == newItem.title &&
                        oldItem.body == newItem.body &&
                        oldItem.date == newItem.date &&
                        oldItem.type == newItem.type
            }
        })
        diff.dispatchUpdatesTo(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView:TextView = itemView.findViewById(R.id.type_text)
        val titleTextView:TextView = itemView.findViewById(R.id.title_text)
        val bodyTextView:TextView = itemView.findViewById(R.id.body_text)
        val dateTextView:TextView = itemView.findViewById(R.id.date_text)
        val statusTextView:TextView = itemView.findViewById(R.id.status_text)
        val closeButton:ImageButton = itemView.findViewById(R.id.close_button)
    }

    interface OnItemClickListener {
        fun onItemClick(parent: NotificationRecyclerViewAdapter, view: View, position: Int, id: Long)
    }
}