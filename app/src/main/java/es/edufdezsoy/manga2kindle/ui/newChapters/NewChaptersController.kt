package es.edufdezsoy.manga2kindle.ui.newChapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import es.edufdezsoy.manga2kindle.R
import es.edufdezsoy.manga2kindle.data.M2kDatabase
import es.edufdezsoy.manga2kindle.data.model.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class NewChaptersController : Controller(), CoroutineScope, NewChaptersContract.Controller,
    NewChaptersInteractor.Controller {
    //#region vars and vals

    private lateinit var interactor: NewChaptersInteractor
    lateinit var view: NewChaptersView
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    //#endregion
    //#region lifecycle methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val v = inflater.inflate(R.layout.view_new_chapters, container, false)

        interactor = NewChaptersInteractor(this, M2kDatabase.invoke(v.context))

        job = Job()
        view = NewChaptersView(view = v, controller = this)

        return v
    }

    override fun onDestroyView(view: View) {
        job.cancel()
        super.onDestroyView(view)
    }

    //#endregion
    //#region override methods

    override fun loadChapters() {
        launch {
            interactor.loadChapters()
        }
    }

    override fun openChapterDetails(chapter: Chapter) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideChapter(chapter: Chapter) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setNewChapters(chapters: List<Chapter>) {
        view.setChapters(chapters)
    }

    //#endregion
}