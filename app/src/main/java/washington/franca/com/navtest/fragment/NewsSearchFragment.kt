package washington.franca.com.navtest.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_news_search.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.adapter.NewsSearchSuggestionRecyclerViewAdapter
import washington.franca.com.navtest.util.SoftKeyboard
import washington.franca.com.navtest.viewmodel.NewsViewModel

class NewsSearchFragment : BaseFragment(), SearchView.OnQueryTextListener {
    private lateinit var newsViewModel: NewsViewModel
    private var adapter = NewsSearchSuggestionRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel = NewsViewModel.create(this)
        newsViewModel.suggestions.observe(viewLifecycleOwner, Observer {
            adapter.set(ArrayList(it))
        })
        newsViewModel.getSuggestions()
        newsViewModel.query?.let {search_view.setQuery(it, false)}

        recycler_view.adapter = adapter
        adapter.listener = object:NewsSearchSuggestionRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int, item: String) {
                search_view.setQuery(item, true)
            }

            override fun onItemRemoved(position: Int, item: String) {
                newsViewModel.removeSuggestion(item)
            }
        }

        search_view.onActionViewExpanded()
        search_view.setOnQueryTextListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        SoftKeyboard.hide(requireActivity())
        if(!TextUtils.isEmpty(query)) {
            newsViewModel.addSuggestion(query!!)
            findNavController().navigate(NewsSearchFragmentDirections.actionDestNewsSearchToDestNewsResult(query))
            return true
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newsViewModel.query = newText
        newsViewModel.getSuggestions()
        return true
    }

    override fun getToolBar(): Toolbar? {
        return toolbar
    }

    override fun isBottomNavigationVisible(): Boolean {
        return false
    }
}
