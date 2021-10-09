package com.samagra.parent.authentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.assets.uielements.CustomMessageDialog
import com.google.gson.Gson
import com.samagra.ancillaryscreens.custom.otptextview.OTPListener
import com.samagra.commons.models.Result
import com.samagra.ancillaryscreens.data.pinverification.PinModel
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.ancillaryscreens.fcm.NotificationViewModel
import com.samagra.ancillaryscreens.utils.KeyConstants
import com.samagra.commons.CommonUtilities
import com.samagra.commons.basemvvm.BaseFragment
import com.samagra.parent.R
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.databinding.FragmentPinBinding


open class PinFragment : BaseFragment<FragmentPinBinding, PinViewModel>(), OTPListener {
    private lateinit var prefs: CommonsPrefsHelperImpl
    private lateinit var pinModel: PinModel

    override fun layoutId(): Int {
        return R.layout.fragment_pin
    }

    private var listener: PinActionListener? = null

    fun setListener(listener: PinActionListener) {
        this.listener = listener
    }

    override fun getBaseViewModel(): PinViewModel? {
        return PinViewModel(requireActivity().application)
    }

    override fun getBindingVariable(): Int {
        return 0;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromArgs()
        setupUi()
        setupListeners()
        CommonUtilities.showKeyboard(requireContext(), binding.otpView)
    }

    private fun setupListeners() {
        binding.otpView.setOnKeyboardDefaultButtonClickListener(this)
        binding.tvOtpVBack.setOnClickListener {
            listener?.onCloseClicked()
        }
        binding.validateButton.setOnClickListener {
            setButtonVerify(binding.otpView.otp)
        }
        binding.btnForgot.setOnClickListener {
            //clear saved pin and send to login mobile verification screen.
            prefs.saveCreatedPin("")
            val intent = Intent(activity, AuthenticationActivity::class.java)
            CommonUtilities.startActivityAsNewTask(
                intent,
                activity as Context
            )
        }
    }

    private fun setButtonVerify(otp: String?) {
        if (otp?.length == 4) {
            hideKeyboard()
            if (pinModel.createPin) {
                listener?.onCreateNewPinClicked(binding.otpView.otp.toString())
            } else {
                listener?.onLoginVerifyPinClicked(binding.otpView.otp.toString())
            }
        } else {
            activity?.let {
                val customDialog = CustomMessageDialog(
                    it,
                    null,
                    if(otp?.length == 0) getString(R.string.please_enter_pin)
                    else getString(R.string.entered_incorrect_pin),
                    null
                )
                customDialog.setOnFinishListener {
                    //handle finish
                }
                customDialog.show()
            }
        }
    }

    private fun setupUi() {
        binding.tvVersionName.text = UtilityFunctions.getVersionName(context)
        if (pinModel.createPin) {
            binding.btnForgot.visibility = View.GONE
        } else {
            binding.tvOtpVBack.visibility = View.GONE
            binding.btnForgot.visibility = View.VISIBLE
            val mentorDetails =
                Gson().fromJson(prefs.mentorDetails, Result::class.java)
            NotificationViewModel().registerFCMToken(mentorDetails.id)
        }
        binding.ivHeader.setImageResource(pinModel.resourceImageValue)
        binding.pinTitle.text = pinModel.textTitle
        binding.validateButton.text = pinModel.textButton
    }

    private fun getDataFromArgs() {
        pinModel =
            arguments?.getSerializable(KeyConstants.PIN_BOTTOM_SHEET_DATA) as PinModel
        prefs = CommonsPrefsHelperImpl(activity, "prefs")
    }

    companion object {
        @JvmStatic
        fun newInstance(pinModel: PinModel): PinFragment {

            return PinFragment().apply {
                this.arguments = Bundle().apply {
                    this.putSerializable(KeyConstants.PIN_BOTTOM_SHEET_DATA, pinModel)
                }
            }
        }
    }

    override fun onKeyboardDefaultButtonClick(pinText: String) {
        setButtonVerify(pinText)
    }

    interface PinActionListener {
        fun onCreateNewPinClicked(pin: String) {}
        fun onLoginVerifyPinClicked(verificationPin: String) {}
        fun onCloseClicked()
    }
}