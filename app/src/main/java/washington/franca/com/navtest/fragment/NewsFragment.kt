package washington.franca.com.navtest.fragment


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_news.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.adapter.NewsRecyclerViewAdapter
import washington.franca.com.navtest.view.EndlessRecyclerViewScrollListener
import washington.franca.com.navtest.viewmodel.NewsViewModel

class NewsFragment : Fragment() {
    private lateinit var newsViewModel: NewsViewModel
    private val adapter = NewsRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = adapter
        recycler_view.addOnScrollListener(object: EndlessRecyclerViewScrollListener(recycler_view.layoutManager!!) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                refresh_layout.isRefreshing = true
                newsViewModel.headLines(page+1)
            }
        })

        newsViewModel = NewsViewModel.create(this)
        newsViewModel.query = arguments?.getString("category")
        newsViewModel.news.observe(viewLifecycleOwner, Observer {
            refresh_layout.isRefreshing = false
            adapter.addAll(it)
        })
        newsViewModel.error.observe(viewLifecycleOwner, Observer {

        })

        refresh_layout.setOnRefreshListener {
            adapter.clear()
            newsViewModel.headLines(1)
        }

        refresh()
    }

    fun refresh() {
        refresh_layout.isRefreshing = true
        adapter.clear()
        newsViewModel.headLines(1)
    }

    companion object {
        fun newInstance(category:String?) = NewsFragment().apply {
            arguments = Bundle().apply {
                putString("category", category)
            }
        }
    }
}
