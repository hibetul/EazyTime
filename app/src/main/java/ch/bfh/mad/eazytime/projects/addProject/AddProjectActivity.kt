package ch.bfh.mad.eazytime.projects.addProject


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ch.bfh.mad.R
import ch.bfh.mad.databinding.ActivityAddProjectBinding
import ch.bfh.mad.eazytime.TAG
import ch.bfh.mad.eazytime.data.entity.Project
import ch.bfh.mad.eazytime.data.repo.ProjectRepo
import ch.bfh.mad.eazytime.di.Injector
import ch.bfh.mad.eazytime.homeScreenWidget.WidgetProvider
import ch.bfh.mad.eazytime.projects.ProjectModelFactory
import ch.bfh.mad.eazytime.util.EazyTimeColorUtil
import ch.bfh.mad.eazytime.util.ProjectProviderService
import com.thebluealliance.spectrum.SpectrumDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random


class AddProjectActivity : AppCompatActivity() {

    @Inject
    lateinit var projectProviderService: ProjectProviderService

    @Inject
    lateinit var projectRepo: ProjectRepo

    @Inject
    lateinit var colorUtil: EazyTimeColorUtil

    @Inject
    lateinit var saveOrUpdateService: ProjectSaveOrUpdateService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        title = getString(R.string.add_project_fragment_title)

        Injector.appComponent.inject(this)
        val addProjectViewModel = ViewModelProviders.of(this, ProjectModelFactory(projectProviderService))
            .get(AddProjectViewModel::class.java)

        val dataBinding = DataBindingUtil.setContentView<ActivityAddProjectBinding>(this, R.layout.activity_add_project)
            .apply {
                this.lifecycleOwner = this@AddProjectActivity
                this.addProjectViewModel = addProjectViewModel
            }

        val extras = intent.extras
        val projectId = extras?.get(INTENT_EXTRA_KEY_PROJECT_ID) as Long?
        projectId?.let { id ->
            Log.i(TAG, "initViewModelWithSelectedProjectInformation")
            initViewModelWithSelectedProjectInformation(addProjectViewModel, dataBinding, id)
        } ?: run {
            Log.i(TAG, "initViewModelWithDefaultValues")
            initViewModelWithDefaultValues(addProjectViewModel, dataBinding)
        }

        addProjectViewModel.shortCode.observe(this, Observer { shortCodeText ->
            shortCodeText?.let {
                if (shortCodeText.length > 3){
                    dataBinding.etAddProjectShortCode.error = getString(R.string.error_short_code_length)
                    dataBinding.buAddProjectSave.isEnabled = false
                }else{
                    dataBinding.etAddProjectShortCode.error = null
                    dataBinding.buAddProjectSave.isEnabled = true
                }
            }
        })

        addProjectViewModel.projectName.observe(this, Observer { projectName ->
            projectName?.let {
                if (projectName.isBlank()){
                    dataBinding.etAddProjectName.error = getString(R.string.error_project_name_length)
                    dataBinding.buAddProjectSave.isEnabled = false
                }else{
                    dataBinding.etAddProjectName.error = null
                    dataBinding.buAddProjectSave.isEnabled = true
                }
            }
        })

        addProjectViewModel.colorId.observe(this, Observer { colorId ->
            colorId?.let { dataBinding.buAddProjectColorPicker.setBackgroundColor(colorId) }
        })

        dataBinding.buAddProjectColorPicker.setOnClickListener {
            showColorPickerDialog(addProjectViewModel)
        }

        dataBinding.buAddProjectSave.setOnClickListener { createOrUpdateProject(addProjectViewModel) }

        dataBinding.buAddProjectDelete.setOnClickListener { deleteProject(addProjectViewModel) }
    }

    private fun initViewModelWithDefaultValues(addProjectViewModel: AddProjectViewModel, dataBinding: ActivityAddProjectBinding) {
        val colors = resources.getIntArray(R.array.eazyTime_project_colors)
        addProjectViewModel.selectProjectColor(colors[Random.nextInt(colors.size)])
        dataBinding.buAddProjectSave.isEnabled = false

    }

    private fun initViewModelWithSelectedProjectInformation(addProjectViewModel: AddProjectViewModel, dataBinding: ActivityAddProjectBinding, projectId: Long) = runBlocking {
        projectRepo.getProjectById(projectId)?.let { project ->
            addProjectViewModel.initializeWithProjectData(project, colorUtil)
            dataBinding.buAddProjectSave.isEnabled = true
        }
    }

    private fun deleteProject(addProjectViewModel: AddProjectViewModel) = runBlocking {
        addProjectViewModel.projectId.value?.let { id ->
            Log.i(TAG, "Delete Project started in InsertAsyncTask, close the Activity")
            withContext(Dispatchers.IO) {
                projectRepo.deleteProject(id)
                finish()
            }
        }
    }

    private fun createOrUpdateProject(addProjectViewModel: AddProjectViewModel) {
        val tmpProject = Project()
        tmpProject.id = addProjectViewModel.projectId.value ?: 0
        tmpProject.name = addProjectViewModel.projectName.value ?: getString(R.string.default_projectName)
        tmpProject.shortCode = addProjectViewModel.shortCode.value ?: ""
        tmpProject.isDefault = addProjectViewModel.defaultProject.value ?: false
        tmpProject.onWidget = addProjectViewModel.onWidget.value ?: false
        tmpProject.color = colorUtil.getColorString(addProjectViewModel.colorId.value?:0)
        Log.i(TAG, "project to save: $tmpProject")
        saveOrUpdateService.saveOrUpdateProject(tmpProject)
        sendBroadcast(WidgetProvider.getUpdateAppWidgetsIntent(this))
        Log.i(TAG, "Save Project started in InsertAsyncTask, close the Activity")
        finish()
    }

    private fun showColorPickerDialog(addProjectViewModel: AddProjectViewModel) {
        val selectedColor = addProjectViewModel.colorId.value ?: R.color.eazyTime_colorProject1
        SpectrumDialog.Builder(this)
            .setTitle(getString(R.string.color_picker_dialog_title))
            .setColors(R.array.eazyTime_project_colors)
            .setSelectedColor(selectedColor)
            .setDismissOnColorSelected(true)
            .setOnColorSelectedListener { positiveResult, color ->
                Log.i(TAG, "Choose projectColor by spectrumDialog: id=$color positiveResult=$positiveResult")
                addProjectViewModel.selectProjectColor(color)
            }
            .build()
            .show(supportFragmentManager, "spectrumDialog")
    }

    companion object {

        private const val INTENT_EXTRA_KEY_PROJECT_ID = "Constants"

        fun getAddProjectActivityIntent(ctx: Context) = Intent(ctx, AddProjectActivity::class.java)

        fun getUpdateProjectActivityIntent(ctx: Context, projectId: Long): Intent {
            return Intent(ctx, AddProjectActivity::class.java).also { intent ->
                intent.putExtra(INTENT_EXTRA_KEY_PROJECT_ID, projectId)
            }
        }
    }
}
