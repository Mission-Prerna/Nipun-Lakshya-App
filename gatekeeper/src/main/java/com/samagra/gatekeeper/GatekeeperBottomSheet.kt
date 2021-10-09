package com.samagra.gatekeeper

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.samagra.gatekeeper.databinding.GatekeeperDialogBinding

internal class GatekeeperBottomSheet private constructor() : BottomSheetDialogFragment() {

    private var _binding: GatekeeperDialogBinding? = null
    private val binding get() = _binding!!

    private var errorModel: Error? = null

    var interactions: GatekeeperBottomSheetInteractions? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GatekeeperDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        errorModel = arguments?.getSerializable(
            ERROR_MODEL
        ) as Error?
        errorModel?.let {
            renderViewContent(it)
        } ?: kotlin.run {
            dismiss()
        }
    }

    private fun renderViewContent(errorModel: Error) {
        binding.tvErrorTitle.text = errorModel.title
        binding.tvErrorDescription.text = errorModel.description
        binding.bError.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        interactions?.onDialogDismiss(
            context = context,
            action = errorModel?.action
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        internal const val TAG = "GatekeeperBottomSheet"
        private const val ERROR_MODEL = "error_model"

        internal fun newInstance(errorModel: Error): GatekeeperBottomSheet {
            val gatekeeperBottomSheet = GatekeeperBottomSheet()
            val bundle = Bundle()
            bundle.putSerializable(ERROR_MODEL, errorModel)
            gatekeeperBottomSheet.arguments = bundle
            return gatekeeperBottomSheet
        }
    }
}

interface GatekeeperBottomSheetInteractions {
    fun onDialogDismiss(context: Context?, action: String?)
}