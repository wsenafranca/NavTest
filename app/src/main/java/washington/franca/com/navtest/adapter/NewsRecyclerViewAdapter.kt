package washington.franca.com.navtest.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import washington.franca.com.navtest.R
import washington.franca.com.navtest.model.News

class NewsRecyclerViewAdapter : RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder>() {
    private val news = ArrayList<News>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_news_item, parent, false))
    }

    override fun getItemCount(): Int = news.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val news = this.news[position]
        holder.container.setOnClickListener {

        }

        holder.title.text = news.title
        holder.description.text = news.description
        holder.source.text = news.source?.name ?: news.source?.id
        holder.date.text = DateUtils.getRelativeTimeSpanString(news.publishedAt.time)

        holder.bigImage.setImageDrawable(null)
        news.urlToImage?.let { url->
            Glide.with(holder.itemView.context)
                .load(url)
                .override(SIZE_ORIGINAL)
                .into(holder.bigImage)
        }
    }

    @Synchronized
    fun addAll(news:List<News>) {
        synchronized(this.news) {
            val init = this.news.size
            this.news.addAll(news)
            notifyItemRangeInserted(init, news.size)
        }
    }

    @Synchronized
    fun clear() {
        synchronized(this.news) {
            this.news.clear()
            notifyDataSetChanged()
        }
    }

    @Synchronized
    fun isEmpty() :Boolean{
        synchronized(this.news) {
            return this.news.isEmpty()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container:ViewGroup = itemView.findViewById(R.id.container)
        val bigImage:ImageView = itemView.findViewById(R.id.big_image)
        val title:TextView = itemView.findViewById(R.id.title)
        val description:TextView = itemView.findViewById(R.id.description)
        val source:TextView = itemView.findViewById(R.id.source)
        val date:TextView = itemView.findViewById(R.id.date)
    }

}