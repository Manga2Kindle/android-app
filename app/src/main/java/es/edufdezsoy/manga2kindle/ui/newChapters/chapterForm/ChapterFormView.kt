package es.edufdezsoy.manga2kindle.ui.newChapters.chapterForm

import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import es.edufdezsoy.manga2kindle.R
import es.edufdezsoy.manga2kindle.data.model.Author
import es.edufdezsoy.manga2kindle.data.model.Chapter
import es.edufdezsoy.manga2kindle.data.model.Manga
import kotlinx.android.synthetic.main.view_chapter_form.view.*


class ChapterFormView(val view: View, val controller: ChapterFormContract.Controller) :
    ChapterFormContract.View {
    private lateinit var chapter: Chapter
    private lateinit var manga: Manga
    private lateinit var author: Author
    private lateinit var authors: List<Author>
    private var authorArray = arrayOf("")

    init {
        //#region clickListeners

        view.btnAddAuthor.setOnClickListener { controller.openAuthorForm() }
        view.btnUpload.setOnClickListener {
            saveData()
            // if the no author is set and we are not adding a new one here... toast.
            if (manga.author_id == null) {
                if (!::author.isInitialized)
                    Toast.makeText(
                        view.context,
                        R.string.chapter_form_no_author_toast,
                        Toast.LENGTH_LONG
                    ).show()
            } else {
                disableAllButtons()
                controller.sendChapter(chapter)
            }
        }
        view.rgSplitMode.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.rbSplitModeSplit -> chapter.split_mode = 0
                R.id.rbSplitModeNoSplit -> chapter.split_mode = 1
                R.id.rbSplitModeBoth -> chapter.split_mode = 2
            }
        }
        view.rgReadMode.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.rbReadModeManga -> chapter.style = "manga"
                R.id.rbReadModeComic -> chapter.style = "comic"
            }
        }

        //#endregion
        //#region editActions

        // TODO: if something is edited disable the upload button until changes are saved

        //#endregion

        view.actvAuthor.doOnTextChanged { text, start, count, after ->
            controller.searchAuthors(text.toString())
        }

        // TODO: mangas cant be edited by now
        // this disables the manga text field
        view.tietManga.inputType = InputType.TYPE_NULL

        // we are supposing chapters are always correct, we are not allowing edits
        view.etChapter.inputType = InputType.TYPE_NULL
        view.etChapter.background = null
    }

    //#region private functions

    private fun disableAllButtons() {
        onEditDisableButtons()
    }

    private fun onEditDisableButtons() {
        view.btnAddAuthor.isEnabled = false
        view.btnUpload.isEnabled = false
    }

    private fun trimTrailingZero(value: String?): String? {
        return if (!value.isNullOrEmpty()) {
            if (value.indexOf(".") < 0) {
                value

            } else {
                value.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
            }

        } else {
            value
        }
    }

    private fun setAuthorTextList(authors: List<Author>): Array<String> {
        val authorsStr = ArrayList<String>()

        authors.forEach {
            authorsStr.add(it.toString())
        }

        return authorsStr.toTypedArray()
    }

    private fun notifyAuthorAdapter() {
        val adapter = ArrayAdapter<String>(
            view.context,
            android.R.layout.simple_dropdown_item_1line,
            authorArray
        )
        view.actvAuthor.setAdapter(adapter)
        view.actvAuthor.threshold = 1
        view.actvAuthor.setOnItemClickListener { adapterView, view, i, l ->
            val authorStr = adapterView.getItemAtPosition(i)

            authors.forEach {
                if (it.toString() == authorStr) {
                    author = it
                    return@setOnItemClickListener
                }
            }
        }
    }

    //#endregion
    //#region override functions

    override fun saveData() {
        try {
            chapter.volume = view.etVolume.text.toString().toInt()
        } catch (e: Exception) {
            chapter.volume = null
        }
        // chapter.chapter = view.etChapter.text.toString().toFloat() // chapter can not be reassigned, we expect the chapter to be correct
        chapter.title = view.tietTitle.text.toString()

        // TODO: manga title cant actually be edited because to the server it will be a new manga
        // manga.title = view.tietManga.text.toString()
        if (manga.author_id == null) {
            val newManga: Manga
            if (::author.isInitialized)
                newManga = Manga(manga.id, manga.title, author.id)
            else
                newManga = Manga(manga.id, manga.title, null)
            newManga.synchronized = manga.synchronized
            newManga.identifier = manga.identifier

            controller.saveData(chapter, newManga)
        } else {
            controller.saveData(chapter, manga)
        }
    }

    override fun setChapter(chapter: Chapter) {
        this.chapter = chapter

        if (chapter.volume != null)
            view.etVolume.setText(chapter.volume.toString())

        view.etChapter.setText(trimTrailingZero(chapter.chapter.toString()))
        view.tietTitle.setText(chapter.title)

        when (chapter.split_mode) {
            0 -> view.rbSplitModeSplit.isChecked = true
            1 -> view.rbSplitModeNoSplit.isChecked = true
            2 -> view.rbSplitModeBoth.isChecked = true
        }

        when (chapter.style) {
            "manga" -> view.rbReadModeManga.isChecked = true
            "comic" -> view.rbReadModeComic.isChecked = true
        }
    }

    override fun setManga(manga: Manga) {
        this.manga = manga

        view.tietManga.setText(manga.title)
        view.tietManga.background = null
    }

    override fun setAuthor(author: Author) {
        view.actvAuthor.inputType = InputType.TYPE_NULL
        view.actvAuthor.setText(author.toString())

        // remove editText line
        view.actvAuthor.background = null
        // disable the button
        view.btnAddAuthor.isEnabled = false
        // change the color
        view.btnAddAuthor.background.setTint(
            ContextCompat.getColor(
                view.context,
                R.color.btnNormalDisabled
            )
        )
        // view.btnAddAuthor.setTextColor(ContextCompat.getColor(view.context, R.color.btnDisabled))
    }

    override fun setAuthors(authors: List<Author>) {
        this.authors = authors
        authorArray = setAuthorTextList(authors)
        notifyAuthorAdapter()
    }

    //#endregion
}