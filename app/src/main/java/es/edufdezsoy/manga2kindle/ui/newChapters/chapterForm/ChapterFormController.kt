package es.edufdezsoy.manga2kindle.ui.newChapters.chapterForm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import es.edufdezsoy.manga2kindle.R
import es.edufdezsoy.manga2kindle.data.M2kDatabase
import es.edufdezsoy.manga2kindle.data.model.Author
import es.edufdezsoy.manga2kindle.data.model.Chapter
import es.edufdezsoy.manga2kindle.data.model.Manga
import es.edufdezsoy.manga2kindle.data.model.viewObject.NewChapter
import es.edufdezsoy.manga2kindle.ui.newChapters.chapterForm.authorForm.AuthorFormController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ChapterFormController : Controller, CoroutineScope,
    ChapterFormContract.Controller, ChapterFormInteractor.Controller {
    //#region vals and vars

    private lateinit var interactor: ChapterFormInteractor
    private lateinit var view: ChapterFormView
    private lateinit var newChapter: NewChapter
    private lateinit var chapter: Chapter
    private lateinit var context: Context
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    //#endregion
    //#region Constructors

    constructor() : super()

    constructor(chapter: NewChapter) : super() {
        this.newChapter = chapter
    }

    //#endregion
    //#region lifecycle methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val v = inflater.inflate(R.layout.view_chapter_form, container, false)
        interactor = ChapterFormInteractor(this, M2kDatabase.invoke(v.context))
        context = v.context

        job = Job()
        view = ChapterFormView(view = v, controller = this)

        launch {
            interactor.getChapter(newChapter)
            interactor.getManga(newChapter.manga_local_id)
            interactor.getMail(activity!!)
        }

        return v
    }

    override fun onDestroyView(view: View) {
        job.cancel()
        super.onDestroyView(view)
    }

    //#endregion
    //#region public methods

    /**
     * Called from the view
     */
    override fun saveData(chapter: Chapter, manga: Manga, mail: String?) {
        launch { interactor.saveChapter(chapter) }
        launch { interactor.saveManga(manga) }
        if (mail != null)
            launch { interactor.saveMail(activity!!, mail) }
    }

    /**
     * Called from the view
     */
    override fun sendChapter(chapter: Chapter, mail: String) {
        launch {
            interactor.sendChapter(chapter, mail, context)
        }
    }

    /**
     * Called from the view
     */
    override fun openAuthorForm() {
        router.pushController(
            RouterTransaction.with(AuthorFormController(chapter))
                .pushChangeHandler(overriddenPushHandler)
                .popChangeHandler(overriddenPopHandler)
        )
    }

    /**
     * Called from the view
     */
    override fun cancelEdit() {
        done()
    }

    /**
     * Called from the interactor
     */
    override fun setChapter(chapter: Chapter) {
        this.chapter = chapter
        view.setChapter(chapter)
    }

    /**
     * Called from the interactor
     */
    override fun setManga(manga: Manga) {
        view.setManga(manga)
        launch {
            if (manga.author_id != null)
                interactor.getAuthor(manga.author_id)
            else
                interactor.getAuthors()
        }
    }

    /**
     * Called from the interactor
     */
    override fun setAuthor(author: Author) {
        view.setAuthor(author)
    }

    /**
     * Called from the interactor
     */
    override fun setAuthors(authors: List<Author>) {
        view.setAuthors(authors)
    }

    /**
     * Called from the interactor
     */
    override fun setMail(mail: String?) {
        if (mail != null)
            view.setMail(mail)
    }

    /**
     * Called from the interactor
     */
    override fun done() {
        activity!!.onBackPressed()
    }

    //#endregion

}