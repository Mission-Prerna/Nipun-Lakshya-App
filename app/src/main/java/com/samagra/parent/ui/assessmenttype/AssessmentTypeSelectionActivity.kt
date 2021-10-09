package com.samagra.parent.ui.assessmenttype

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.chatbot.ChatBotActivity
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.ancillaryscreens.utils.Constant
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.constants.Constants
import com.samagra.commons.helper.GatekeeperHelper
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.grove.logging.Grove
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityAssessmentTypeSelectionBinding
import com.samagra.parent.ui.*
import com.samagra.parent.ui.assessmenthome.DrawerItem
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import com.samagra.parent.ui.logout.LogoutUI
import com.samagra.parent.ui.privacypolicy.PrivacyPolicyActivity
import com.samagra.parent.ui.userselection.UserSelectionActivity
import org.odk.collect.android.utilities.ToastUtils
import timber.log.Timber

@Suppress("UNUSED_PARAMETER")
class AssessmentTypeSelectionActivity :
    BaseActivity<ActivityAssessmentTypeSelectionBinding, AssessmentTypeVM>() {

    private val prefs by lazy { initPreferences() }

    private var schoolsData: SchoolsData? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_assessment_type_selection

    override fun getBaseViewModel(): AssessmentTypeVM {
        val repository = DataSyncRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(this.application, repository)
        return ViewModelProvider(
            this,
            viewModelProviderFactory
        )[AssessmentTypeVM::class.java]
    }

    override fun getBindingVariable() = BR.vm

    private val viewModels: AssessmentTypeVM by viewModels()

    override fun onLoadData() {
        setupToolbar()
        getDataFromIntent()
        setupUI()
        setupListener()
        setupObserver()
        viewModel.checkForFallback(prefs)
    }

    private fun setupObserver() {
        with(viewModels) {
            observe(remoteConfigString, ::handleInfoNoteText)
            observe(showSyncBeforeLogout, ::handleSyncBeforeLogout)
            observe(logoutUserLiveData, ::handleLogoutUser)
            observe(showDialogLogoutWithNoInternet, ::handleLogoutWhenNoInternet)
            observe(gotoLogin, ::handleLogoutRedirection)
            observe(progressBarVisibility, ::handleProgressBarVisibility)
        }
    }

    private fun handleLogoutRedirection(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        setRedirectionsOnIntent()
    }

    private fun handleLogoutWhenNoInternet(@Suppress("UNUSED_PARAMETER") testResId: Int?) {
        testResId?.let { ToastUtils.showShortToast(it) }
    }

    private fun handleLogoutUser(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        LogoutUI.confirmLogout(this) {
            viewModel.onLogoutUserData(prefs)
        }
    }

    private fun setRedirectionsOnIntent() {
        val intentToUserSelection = Intent(this, AuthenticationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intentToUserSelection)
        finish()
    }

    private fun initPreferences() = CommonsPrefsHelperImpl(this, "prefs")

    private fun setupUI() {
        when (prefs.selectedUser) {
            AppConstants.USER_PARENT -> {
                binding.schoolInfo.root.visibility = View.GONE
                binding.btnNipunSuchi.visibility = View.GONE
            }
            AppConstants.USER_TEACHER -> {
                setHeaderUi()
                setTeacherContainerUI()
                val isChatbotEnabled = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                    .getBoolean(RemoteConfigUtils.CHATBOT_ENABLED)
                Timber.d("setupUI isChatbotEnabled: $isChatbotEnabled")
                if (isChatbotEnabled) {
                    binding.botFab.visibility = View.VISIBLE
                }
                //Syncing data is only required for the teacher flow as it is home landing page.
                viewModel.syncDataFromServer(prefs)
            }
            AppConstants.USER_MENTOR, Constants.USER_DIET_MENTOR -> {
                setHeaderUi()
                setMentorContainerUI()
            }
            else -> {
                // handle other types
            }
        }

        binding.botFab.setOnClickListener {
            openBot()
            logChatbotInitiate()
        }
    }

    private fun logChatbotInitiate() {
        val properties = PostHogManager.createProperties(
            page = ASSESSMENT_TYPE_SELECTION_SCREEN,
            eventType = EVENT_TYPE_USER_ACTION,
            eid = EID_INTERACT,
            context = PostHogManager.createContext(
                id = APP_ID,
                pid = NL_APP_ASSESSMENT_TYPE_SELECTION,
                dataList = ArrayList()
            ),
            eData = Edata(NL_ASSESSMENT_TYPE_SELECTION, TYPE_CLICK),
            objectData = Object.Builder().id(BOT_INITIATION_BUTTON).type(OBJ_TYPE_UI_ELEMENT)
                .build(),
            prefs = PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(
            context = this,
            eventName = EVENT_CHATBOT_INITIATE,
            properties = properties
        )
    }

    private fun openBot() {
        startActivity(Intent(this, ChatBotActivity::class.java))
    }

    private fun setTeacherContainerUI() {
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_lakshya_app)
        binding.tvTitle.setTextOnUI(getString(R.string.select_assessment_type_title))
        binding.tvBtn1.setTextOnUI(getString(R.string.nipun_abhyas_text))
        getDrawable(R.drawable.ic_type_nipun_lakshya)?.let { binding.ivBtn1.setImageOnUI(it) }
        binding.tvBtn2.setTextOnUI(getString(R.string.suchi_abhyas_text))
        getDrawable(R.drawable.ic_nipun_soochi)?.let { binding.ivBtn2.setImageOnUI(it) }
        binding.profileInfoNote.tvInfoNote.text =
            getString(R.string.info_note_text_assessment_type_for_teacher).getHtmlSpanString()
        getMentorDetailsFromPrefs(prefs)
    }

    private fun getMentorDetailsFromPrefs(prefs: CommonsPrefsHelperImpl) {
        val mentorDetailsFromDb = prefs.mentorDetailsData
        mentorDetailsFromDb?.let {
            binding.navigationDrawer.usernameTv.text = it.officer_name
            binding.navigationDrawer.phoneTv.text = it.phone_no
        }
    }

    private fun setMentorContainerUI() {
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_lakshya_app)
        binding.tvTitle.setTextOnUI(getString(R.string.select_assessment_type_title))
        binding.tvBtn1.setTextOnUI(getString(R.string.nipun_lakshya_text))
        getDrawable(R.drawable.ic_type_nipun_lakshya)?.let { binding.ivBtn1.setImageOnUI(it) }
        binding.tvBtn2.setTextOnUI(getString(R.string.nipun_suchi_text))
        getDrawable(R.drawable.ic_nipun_soochi)?.let { binding.ivBtn2.setImageOnUI(it) }
        viewModels.getInfoNoteFromRemoteConfig(Constant.INFO_NOTES_TYPE_SELECTION)
    }

    private fun setHeaderUi() {
        with(binding.schoolInfo) {
            root.visibility = View.VISIBLE
            name.visibility = View.VISIBLE
            udise.visibility = View.VISIBLE
            tvTime.visibility = View.GONE
            address.visibility = View.GONE
            when (prefs.selectedUser) {
                AppConstants.USER_TEACHER -> {
                    name.setTextOnUI(
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            prefs.mentorDetailsData?.schoolName ?: ""
                        )
                    )
                    udise.setTextOnUI(
                        setHeaderUiText(
                            R.string.udise_top_banner,
                            prefs.mentorDetailsData?.udise.toString()
                        )
                    )
                }
                else -> {
                    name.text =
                        setHeaderUiText(
                            R.string.school_name_top_banner,
                            schoolsData?.schoolName ?: ""
                        )
                    udise.text =
                        setHeaderUiText(R.string.udise_top_banner, schoolsData?.udise.toString())
                }
            }
        }
    }

    private fun setHeaderUiText(resValue: Int, text: String): String {
        return String.format(getString(resValue), text)
    }

    private fun getDataFromIntent() {
        if (intent.hasExtra(AppConstants.INTENT_SCHOOL_DATA)) {
            schoolsData =
                intent.getSerializableExtra(AppConstants.INTENT_SCHOOL_DATA) as SchoolsData
        }
        if (schoolsData == null && prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
            with(prefs.mentorDetailsData) {
                schoolsData = SchoolsData(
                    this?.udise,
                    this?.schoolName,
                    this?.schoolId,
                    true,
                    this?.schoolDistrict,
                    this?.schoolDistrictId,
                    this?.schoolBlock,
                    this?.schoolBlockId,
                    this?.schoolNyayPanchayat,
                    this?.schoolNyayPanchayatId,
                    this?.schoolLat,
                    this.schoolLong,
                    this?.schoolGeoFenceEnabled
                    )
            }
        }
        Timber.e("Schools data for udise assessmentTypeSelection : ${schoolsData?.udise.toString()}")
    }

    private fun setupListener() {
        binding.btnNipunLakshya.setOnClickListener {
            when (prefs.selectedUser) {
                AppConstants.USER_TEACHER -> {
                    prefs.saveAssessmentType(AppConstants.NIPUN_ABHYAS)
                    redirectToGradeSelectionScreen()
                    Timber.e("assessmentTypeActivity : redirectToGradeSelectionScreen. ")
                }
                else -> {
                    prefs.saveAssessmentType(AppConstants.NIPUN_LAKSHYA)
                    redirectToGradeSelectionScreen()
                    Timber.e("assessmentTypeActivity : redirectToGradeSelectionScreen. ")
                }
            }
        }

        binding.btnNipunSuchi.setOnClickListener {
            when (prefs.selectedUser) {
                AppConstants.USER_TEACHER -> {
                    prefs.saveAssessmentType(AppConstants.SUCHI_ABHYAS)
                    redirectToGradeSelectionScreen()
                }
                else -> {
                    prefs.saveAssessmentType(AppConstants.NIPUN_SUCHI)
                    redirectToGradeSelectionScreen()
                }
            }
        }
    }

    private fun redirectToGradeSelectionScreen() {
        val intent = Intent(this, DetailsSelectionActivity::class.java)
        if (schoolsData != null) {
            intent.putExtra(AppConstants.INTENT_SCHOOL_DATA, schoolsData)
        } else {
            Grove.e("Schools data is null and selected user is: ${prefs.selectedUser}")
        }
        startActivity(intent)
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.includeToolbar.toolbar.setTitle(R.string.nipun_lakshya_app)
        binding.includeToolbar.tvVersion.text = UtilityFunctions.getVersionName(this)
        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
                binding.dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                if (binding.dl.isDrawerVisible(GravityCompat.START)) {
                    binding.dl.closeDrawer(GravityCompat.START)
                } else {
                    binding.dl.openDrawer(GravityCompat.START)
                }
            } else {
                binding.dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                finish()
            }
        }
        if (prefs.selectedUser.equals(AppConstants.USER_TEACHER, true)) {
            binding.includeToolbar.toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        } else {
            binding.includeToolbar.toolbar.setNavigationIcon(R.drawable.ic_backspace_24)
        }

        binding.dl.setBodyForTeacher(this) {
            onDrawerClickHandler(it)
        }
    }

    private fun onDrawerClickHandler(drawerItem: DrawerItem) {
        when (drawerItem.drawerOptionType) {
            DrawerOptions.LOGOUT -> {
                binding.dl.closeDrawer(GravityCompat.START)
                viewModel.onLogoutClicked()
            }
            DrawerOptions.PRIVACY_POLICY -> {
                binding.dl.closeDrawer(GravityCompat.START)
                openPrivacyPolicy()
            }
            else -> {
                //implement other options when required
            }
        }
    }

    private fun openPrivacyPolicy() {
        startActivity(Intent(this, PrivacyPolicyActivity::class.java))
    }

    private fun handleSyncBeforeLogout(@Suppress("UNUSED_PARAMETER") unit: Unit?) {
        confirmUserLogoutWithSync()
    }

    private fun confirmUserLogoutWithSync() {
        LogoutUI.confirmLogoutWithSync(context = this) {
            viewModel.syncDataToServer(prefs, {
                viewModel.onLogoutUserData(prefs)
            }) {
                ToastUtils.showShortToast(R.string.error_generic_message)
            }
        }
    }


    private fun handleInfoNoteText(infoNote: String?) {
        infoNote?.let {
            binding.profileInfoNote.tvInfoNote.setTextOnUI(it)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun handleProgressBarVisibility(visible: Boolean?) {
        if (visible == true) {
            showProgressBar()
        } else {
            hideProgressBar()
        }
    }
}
