package washington.franca.com.navtest.fragment


import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_news_result.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.adapter.NewsRecyclerViewAdapter
import washington.franca.com.navtest.view.EndlessRecyclerViewScrollListener
import washington.franca.com.navtest.viewmodel.NewsViewModel

class NewsResultFragment : BaseFragment() {
    private lateinit var newsViewModel: NewsViewModel
    private val adapter = NewsRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = adapter
        recycler_view.addOnScrollListener(object:EndlessRecyclerViewScrollListener(recycler_view.layoutManager!!){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                bottom_loading_view.visibility = View.VISIBLE
                newsViewModel.search(page+1)
            }
        })

        newsViewModel = NewsViewModel.create(this)
        newsViewModel.query = arguments?.getString("query")
        newsViewModel.news.observe(viewLifecycleOwner, Observer {
            adapter.addAll(it)

            if(!adapter.isEmpty()) {
                empty_list.visibility = View.GONE
            } else {
                empty_list.visibility = View.VISIBLE
            }

            bottom_loading_view.visibility = View.GONE
            loading_view.visibility = View.GONE
        })
        newsViewModel.error.observe(viewLifecycleOwner, Observer {
            if(adapter.isEmpty()) {
                loading_view.visibility = View.GONE
                recycler_view.visibility = View.GONE
                empty_list.visibility = View.VISIBLE
                bottom_loading_view.visibility = View.GONE
            }
        })

        empty_list.visibility = View.GONE
        bottom_loading_view.visibility = View.GONE
        loading_view.visibility = View.VISIBLE
        newsViewModel.search(1)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun getToolBar(): Toolbar? {
        return toolbar
    }

    override fun isBottomNavigationVisible(): Boolean {
        return false
    }
}
