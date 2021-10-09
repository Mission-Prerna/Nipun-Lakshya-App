/*
* This activity is not in use can be deleted
* */

package com.samagra.parent.ui.userselection

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.di.FormManagementCommunicator
import com.samagra.parent.authentication.AuthenticationActivity
import com.samagra.ancillaryscreens.utils.observe
import com.samagra.commons.basemvvm.BaseActivity
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.CustomEventCrashUtil
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.*
import com.samagra.parent.databinding.ActivityUserSelectionBinding
import com.samagra.parent.ui.DataSyncRepository
import com.samagra.parent.ui.detailselection.DetailsSelectionActivity
import timber.log.Timber

class UserSelectionActivity : BaseActivity<ActivityUserSelectionBinding, UserSelectionVM>() {

    @LayoutRes
    override fun layoutRes() = R.layout.activity_user_selection

    private val prefs by lazy { initPreferences() }

    override fun getBaseViewModel(): UserSelectionVM {
        val repository = UserSelectionRepository()
        val dataSyncRepo = DataSyncRepository()
        val viewModelProviderFactory =
            ViewModelProviderFactory(this.application, repository, dataSyncRepo)
        return ViewModelProvider(
            this,
            viewModelProviderFactory
        )[UserSelectionVM::class.java]
    }

    override fun getBindingVariable() = BR.vm

    override fun onLoadData() {
        FormManagementCommunicator.getContract().applyODKCollectSettings(
            this,
            com.samagra.ancillaryscreens.R.raw.settings
        )
        setObservers()
        setListeners()
        viewModel.fetchData(prefs, Pair(AuthenticationActivity.RedirectionFlow.NOTHING, ""))
        binding.tvVersionName.text = UtilityFunctions.getVersionName(this)
    }

    private fun setObservers() {
        with(viewModel) {
            getInfoNoteFromRemoteConfig(RemoteConfigUtils.PROFILE_SELECTION_INFO)
            observe(remoteConfigString, ::handleInfoNoteText)
        }
    }

    private fun handleInfoNoteText(infoNote: String?) {
        infoNote?.let {
            val ss = SpannableString(it)
            val subs = infoNote.split(" ")
            val phoneNumber = subs.first { substring ->
                substring.first().isDigit()
            }
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phoneNumber")
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }
            val first = infoNote.indexOf(phoneNumber)
            ss.setSpan(
                clickableSpan,
                first,
                first + phoneNumber.length,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
            binding.incMvInfoNote.tvInfoNote.text = ss
            binding.incMvInfoNote.tvInfoNote.movementMethod = LinkMovementMethod.getInstance()
            binding.incMvInfoNote.tvInfoNote.highlightColor = Color.RED
        }
    }

    private fun initPreferences() = CommonsPrefsHelperImpl(this, "prefs")

    private fun setListeners() {
        binding.llParent.setOnClickListener {
            prefs.saveSelectedUser(AppConstants.USER_PARENT)
            prefs.saveAssessmentType(AppConstants.NIPUN_ABHYAS)
            Timber.d("setListeners: selected user ${prefs.selectedUser}")
            sendTelemetry(EVENT_SELECT_PARENT, PARENT_BUTTON)
            CustomEventCrashUtil.setSelectedUserProperty(prefs.selectedUser)
            val intent = Intent(this, DetailsSelectionActivity::class.java)
            startActivity(intent)
        }
        binding.llTeacher.setOnClickListener {
//            prefs.saveSelectedUser(AppConstants.USER_TEACHER)
            Timber.d("setListeners: selected user ${prefs.selectedUser}")
            sendTelemetry(EVENT_SELECT_TEACHER, TEACHER_BUTTON)
            setEventAndRedirectToAuthFlow()
        }
        binding.llMentor.setOnClickListener {
//            prefs.saveSelectedUser(AppConstants.USER_MENTOR)
            Timber.d("setListeners: selected user ${prefs.selectedUser}")
            sendTelemetry(EVENT_SELECT_MENTOR, MENTOR_BUTTON)
            setEventAndRedirectToAuthFlow()
        }
        binding.llExamimer.setOnClickListener {
//            prefs.saveSelectedUser(AppConstants.USER_EXAMINER)
            Timber.d("setListeners: selected user ${prefs.selectedUser}")
            sendTelemetry(EVENT_SELECT_EXAMINER, EXAMINER_BUTTON)
            setEventAndRedirectToAuthFlow()
        }
    }

    private fun setEventAndRedirectToAuthFlow() {
        CustomEventCrashUtil.setSelectedUserProperty(prefs.selectedUser)
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
    }

    private fun sendTelemetry(event: String, button: String) {
        PostHogManager.createBaseMap(
            PRODUCT,
            prefs.selectedUser,
            prefs.selectedUser,
            prefs.selectedUser,
            prefs.selectedUser,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        val properties = PostHogManager.createProperties(
            USERSELECTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_USER_SELECTION, ArrayList()),
            Edata(NL_USERSELECTION, TYPE_CLICK),
            Object.Builder().id(button).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        PostHogManager.capture(this, event, properties)
    }
}