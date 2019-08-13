package washington.franca.com.navtest.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_home.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentHomeBinding
import washington.franca.com.navtest.viewmodel.UserViewModel

class HomeFragment : BaseFragment() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var binding:FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = UserViewModel.create(requireActivity())
        userViewModel.profilePhoto.observe(viewLifecycleOwner, Observer {
            //profile_photo.setImageDrawable(RoundedBitmapDrawableFactory.create(resources, it))
        })

        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        view_pager.adapter = PagerAdapter(childFragmentManager)
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun getToolBar(): Toolbar? {
        return toolbar
    }

    override fun isBottomNavigationVisible(): Boolean {
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_news, menu)
        /*
        menu.findItem(R.id.action_search)?.let {
            search_view.setMenuItem(it)
        }
        */
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_search -> {
                findNavController().navigate(HomeFragmentDirections.actionDestHomeToDestNewsSearch())
            }
            R.id.action_refresh -> {
                (view_pager.adapter as? PagerAdapter)?.fragments?.get(view_pager.currentItem)?.refresh()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class PagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        @SuppressLint("UseSparseArrays")
        private var map = HashMap<Int, NewsFragment>()
        val fragments = map

        override fun getItem(position: Int): Fragment {
            val category = if(position > 0) {
                resources.getStringArray(R.array.news_category)[position]
            }else {
                null
            }
            val fragment = NewsFragment.newInstance(category)
            this.map[position] = fragment
            return fragment
        }

        override fun getCount(): Int {
            return resources.getStringArray(R.array.news_category).size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return resources.getStringArray(R.array.news_category)[position]
        }
    }
}
