package es.edufdezsoy.manga2kindle.ui.newChapters

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import es.edufdezsoy.manga2kindle.M2kApplication
import es.edufdezsoy.manga2kindle.R
import es.edufdezsoy.manga2kindle.data.model.viewObject.NewChapter
import es.edufdezsoy.manga2kindle.ui.adapter.NewChapterAdapter
import kotlinx.android.synthetic.main.view_new_chapters.view.*

class NewChaptersView(val view: View, val controller: NewChaptersController) :
    NewChaptersContract.View, NewChapterAdapter.OnClickListener,
    NewChapterAdapter.OnLongClickListener {
    private lateinit var adapter: NewChapterAdapter

    init {
        view.rvNewChapters.layoutManager = LinearLayoutManager(controller.activity)
        view.tvViewTitle.text = view.context.getString(R.string.action_loading)

        controller.loadChapters()

        view.swipeRefresh.setOnRefreshListener {
            controller.reloadChapters()
        }
    }

    override fun setChapters(chapters: List<NewChapter>) {
        if (chapters.isNotEmpty()) {
            if (::adapter.isInitialized) {
                Log.d(M2kApplication.TAG, "adapter updated")
                adapter.setData(chapters)
            } else {
                adapter = NewChapterAdapter(chapters)
                adapter.setOnClickListener(this)
                adapter.setOnLongClickListener(this)
                view.rvNewChapters.layoutManager = LinearLayoutManager(controller.context)
                view.rvNewChapters.itemAnimator = DefaultItemAnimator()
                view.rvNewChapters.adapter = adapter
                view.flBackground.visibility = View.GONE
            }
        } else {
            view.tvViewTitle.text = view.context.getString(R.string.new_chapters_empty_list)
        }

        view.swipeRefresh.isRefreshing = false
    }

    override fun onItemClicked(chapter: NewChapter) {
        controller.openChapterDetails(chapter)
    }

    override fun onItemLongClicked(chapter: NewChapter) {
        controller.hideChapter(chapter)
    }
}