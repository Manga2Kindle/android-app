package es.edufdezsoy.manga2kindle.ui.uploadedChapters

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import es.edufdezsoy.manga2kindle.R
import es.edufdezsoy.manga2kindle.data.model.Chapter
import es.edufdezsoy.manga2kindle.data.model.viewObject.UploadedChapter
import es.edufdezsoy.manga2kindle.data.repository.AuthorRepository
import es.edufdezsoy.manga2kindle.data.repository.ChapterRepository
import es.edufdezsoy.manga2kindle.data.repository.MangaRepository
import es.edufdezsoy.manga2kindle.service.intentService.UpdateChapterStatusIntentService
import es.edufdezsoy.manga2kindle.service.util.BroadcastReceiver

class UploadedChaptersInteractor(val controller: Controller, val context: Context) {
    interface Controller {
        fun setNewChapters(chapters: List<UploadedChapter>)
        fun updateList()
    }

    private lateinit var receiver: BroadcastReceiver
    private val chapterRepository = ChapterRepository.invoke(context)
    private val mangaRepository = MangaRepository.invoke(context)
    private val authorRepository = AuthorRepository.invoke(context)

    suspend fun loadChapters() {
        getChaptersList().also {
            controller.setNewChapters(it)
        }
    }

    suspend fun hideChapter(chapter: UploadedChapter) {
        chapterRepository.hide(chapter.local_id).also {
            controller.updateList()
        }
    }

    suspend fun showChapter(chapter: UploadedChapter) {
        val chap = chapterRepository.getChapter(chapter.local_id)
        chap.visible = true
        chapterRepository.update(chap).also {
            controller.updateList()
        }
    }

    suspend fun updateStatus(context: Context) {
        UpdateChapterStatusIntentService.enqueueWork(context, Intent())

        // register reciver
        if (!::receiver.isInitialized) {
            val filter = IntentFilter(BroadcastReceiver.ACTION_UPDATED_CHAPTER_STATUS)
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            receiver = BroadcastReceiver(BroadcastReceiver.ACTION_UPDATED_CHAPTER_STATUS) {
                controller.updateList()
            }
            context.registerReceiver(receiver, filter)
        }
    }

    fun close(context: Context) {
        if (::receiver.isInitialized)
            context.unregisterReceiver(receiver)
    }

    private suspend fun getChaptersList(): ArrayList<UploadedChapter> {
        chapterRepository.getUploadedChapters().also {
            val uploadedChapters = ArrayList<UploadedChapter>()
            it.forEach {
                if (it.visible) {
                    val manga = mangaRepository.getMangaById(it.manga_id)

                    var author = ""
                    if (manga.author_id != null) {
                        val au = authorRepository.getAuthor(manga.author_id)
                        if (au != null)
                            author = au.toString()
                    }

                    var status: String = ""
                    var status_color: Int = 0
                    var reason = ""

                    when (it.status) {
                        Chapter.STATUS_ENQUEUE -> {
                            status = context.getString(R.string.chapter_status_enqueue)
                            status_color = R.color.colorEnqueue
                        }
                        Chapter.STATUS_PROCESSING -> {
                            status = context.getString(R.string.chapter_status_compressing)
                            status_color = R.color.colorCompressing
                        }
                        Chapter.STATUS_UPLOADING -> {
                            status = context.getString(R.string.chapter_status_uploading)
                            status_color = R.color.colorUploading
                        }
                        Chapter.STATUS_UPLOADED -> {
                            status = context.getString(R.string.chapter_status_processing)
                            status_color = R.color.colorProcessing
                        }
                        Chapter.STATUS_LOCAL_ERROR -> {
                            status = context.getString(R.string.chapter_status_failed_local)
                            status_color = R.color.colorFailed
                        }
                    }

                    if (it.error) {
                        status = context.getString(R.string.chapter_status_failed)
                        status_color = R.color.colorFailed
                        reason = it.reason.toString()
                    } else if (it.delivered) {
                        status = context.getString(R.string.chapter_status_success)
                        status_color = R.color.colorSuccess
                    }

                    if (it.id == null)
                        it.id = 0

                    uploadedChapters.add(
                        UploadedChapter(
                            it.id!!,
                            it.identifier,
                            it.toString(),
                            it.manga_id,
                            manga.title,
                            manga.author_id,
                            author,
                            it.status,
                            status,
                            status_color,
                            reason,
                            it.enqueue_date,
                            it.upload_date
                        )
                    )
                }
            }

            return uploadedChapters
        }
    }
}