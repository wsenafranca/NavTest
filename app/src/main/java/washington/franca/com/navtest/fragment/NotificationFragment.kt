package washington.franca.com.navtest.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import com.pd.chocobar.ChocoBar
import kotlinx.android.synthetic.main.fragment_notification.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.adapter.NotificationRecyclerViewAdapter
import washington.franca.com.navtest.model.Notification
import washington.franca.com.navtest.view.EndlessRecyclerViewScrollListener
import washington.franca.com.navtest.viewmodel.NotificationViewModel
import washington.franca.com.navtest.viewmodel.UserViewModel

class NotificationFragment : BaseFragment() {
    lateinit var notificationViewModel:NotificationViewModel
    lateinit var userViewModel: UserViewModel
    private val adapter = NotificationRecyclerViewAdapter()
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = UserViewModel.create(requireActivity())
        userViewModel.user.observe(viewLifecycleOwner, Observer {
            notificationViewModel.setUser(it)
            adapter.clear()
            scrollListener.resetState()
            notificationViewModel.load(20, null)
        })
        notificationViewModel = NotificationViewModel.create(requireActivity())
        notificationViewModel.loaded.observe(viewLifecycleOwner, Observer {
            adapter.addAll(it)
            refresh_layout.isRefreshing = false
            fab.isEnabled = true
        })
        notificationViewModel.added.observe(viewLifecycleOwner, Observer {
            adapter.add(it)
            recycler_view.smoothScrollToPosition(0)
            refresh_layout.isRefreshing = false
            fab.isEnabled = true
        })
        notificationViewModel.removed.observe(viewLifecycleOwner, Observer {
            //adapter.remove(it)
        })
        notificationViewModel.updated.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })
        notificationViewModel.error.observe(viewLifecycleOwner, Observer {
            fab.isEnabled = true
            refresh_layout.isRefreshing = false
            it?.printStackTrace()
            ChocoBar.builder().setView(view)
                .setText(it?.message)
                .setDuration(ChocoBar.LENGTH_SHORT)
                .red()
                .show()
        })

        adapter.listener = object:NotificationRecyclerViewAdapter.OnItemClickListener{
            override fun onItemClick(
                index: Int,
                holder: NotificationRecyclerViewAdapter.ViewHolder,
                notification: Notification
            ) {
                notificationViewModel.read(notification)
            }

            override fun onItemRemove(
                index: Int,
                holder: NotificationRecyclerViewAdapter.ViewHolder,
                notification: Notification
            ) {
                Snackbar.make(view, R.string.notifications_undo_message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.notifications_undo_action) {
                        adapter.add(index, notification)
                        recycler_view.smoothScrollToPosition(index)
                    }.addCallback(object: Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if(event != DISMISS_EVENT_ACTION) {
                                notificationViewModel.remove(notification)
                            }
                        }
                    }).show()
            }
        }
        recycler_view.adapter = adapter
        scrollListener = object:EndlessRecyclerViewScrollListener(recycler_view.layoutManager as LinearLayoutManager){
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                load()
            }
        }
        recycler_view.addOnScrollListener(scrollListener)

        refresh_layout.setOnRefreshListener {
            adapter.clear()
            scrollListener.resetState()
            load()
        }

        fab.setOnClickListener {
            refresh_layout.isRefreshing = false
            fab.isEnabled = false
            val notification = Notification.create("Test", "Some text "+adapter.items.size)
            notificationViewModel.add(notification)
        }

        fab.isEnabled = false
        refresh_layout.isRefreshing = true
    }

    private fun load() {
        fab.isEnabled = false
        refresh_layout.isRefreshing = true
        notificationViewModel.load(20, adapter.items.lastOrNull())
    }

    override fun getToolBar(): Toolbar? {
        return toolbar
    }

    override fun getCollapsingToolBar(): CollapsingToolbarLayout? {
        return collapsing_toolbar
    }
}
