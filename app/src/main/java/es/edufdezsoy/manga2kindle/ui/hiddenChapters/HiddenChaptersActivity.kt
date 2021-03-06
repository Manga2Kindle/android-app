package es.edufdezsoy.manga2kindle.ui.hiddenChapters

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import es.edufdezsoy.manga2kindle.R
import es.edufdezsoy.manga2kindle.ui.base.BaseInteractor
import kotlinx.android.synthetic.main.activity_base.*

class HiddenChaptersActivity : AppCompatActivity(), BaseInteractor.Controller {
    //#region vars and vals

    private lateinit var router: Router
    private lateinit var controller: HiddenChaptersController

    //#endregion
    //#region lifecycle functions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        router = Conductor.attachRouter(this, controller_container, savedInstanceState)
        controller = HiddenChaptersController()

        if (!router.hasRootController())
            router.setRoot(RouterTransaction.with(controller))

        setToolbar()
    }

    override fun onBackPressed() {
        if (!router.handleBack())
            super.onBackPressed()
    }

    //#endregion
    //#region toolbar functions

    private fun setToolbar() {
        baseToolbar.setTitle(R.string.hidden_chapters_title)
        setSupportActionBar(baseToolbar)
        baseToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        baseToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    //#endregion
}
