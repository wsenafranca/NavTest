package washington.franca.com.navtest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import washington.franca.com.navtest.R

class NewsSearchSuggestionRecyclerViewAdapter : RecyclerView.Adapter<NewsSearchSuggestionRecyclerViewAdapter.ViewHolder>() {
    private var items = ArrayList<String>()
    var listener:OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_news_search_suggestion_item, parent, false))
    }

    override fun getItemCount():Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.containerViewGroup.setOnClickListener {
            val p = holder.adapterPosition
            listener?.onItemClicked(p, item)
        }
        holder.textView.text = item
        holder.closeButton.setOnClickListener {
            val p = holder.adapterPosition
            listener?.onItemRemoved(p, item)
        }
    }

    @Synchronized
    fun set(newItems:ArrayList<String>) {
        this.items.clear()
        notifyDataSetChanged()
        this.items.addAll(newItems)
        notifyItemRangeInserted(0, newItems.size)
    }

    interface OnItemClickListener {
        fun onItemClicked(position: Int, item:String)
        fun onItemRemoved(position: Int, item:String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val containerViewGroup:ViewGroup = itemView.findViewById(R.id.container)
        val textView:TextView = itemView.findViewById(R.id.text)
        val closeButton:ImageButton = itemView.findViewById(R.id.close_button)
    }
}