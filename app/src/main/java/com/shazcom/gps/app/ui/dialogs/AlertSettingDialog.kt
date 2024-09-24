package com.shazcom.gps.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.shazcom.gps.app.R
import com.shazcom.gps.app.databinding.DailogAlertSettingBinding

class AlertSettingDialog(private val overSpeed: Boolean = false) : DialogFragment() {

    private var mBinding: DailogAlertSettingBinding? = null


    private var onClickOkListener: ((String) -> Unit)? = null
    private var onClickCancelListener: (() -> Unit)? = null


    fun setListener(onClickOk: (String) -> Unit, onClickCancel: () -> Unit): AlertSettingDialog {
        onClickOkListener = onClickOk
        onClickCancelListener = onClickCancel
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bg_dailog
            )
        )
        mBinding = DailogAlertSettingBinding.inflate(inflater, null, false)
        return mBinding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (overSpeed.not()) {
            mBinding?.title?.text = "Idle Duration Alarm Settings"
            mBinding?.subTitle?.text = "Idle duration (min)"

        }

        mBinding?.btnCancel?.setOnClickListener {
            onClickCancelListener?.invoke()
            dismiss()
        }
        mBinding?.btnOk?.setOnClickListener {
            val value = mBinding?.editText?.text.toString().trim()
            if (value.isEmpty() || value.toInt() <= 0) {
                Toast.makeText(requireContext(), "Please valid value.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onClickOkListener?.invoke(value)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

}