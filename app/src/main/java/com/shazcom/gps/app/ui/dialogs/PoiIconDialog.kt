package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.IconItems
import com.shazcom.gps.app.data.response.PoiIconsResponse
import com.shazcom.gps.app.databinding.DialogPoiIconsBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.adapter.PoiIconAdapter
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel

import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class PoiIconDialog(private val poiDialog: POIDialog) : DialogFragment(), KodeinAware {

    private lateinit var binding: DialogPoiIconsBinding
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        toolsViewModel?.loadPoiIcons("en", localDB.getToken()!!)?.observe(
            requireActivity(),
            Observer { resources ->
                if(isVisible) {
                    when (resources.status) {
                        Status.SUCCESS -> {
                            binding. progressBar.visibility = View.INVISIBLE
                            processData(resources.data!!)
                        }

                        Status.LOADING -> {
                            binding. progressBar.visibility = View.VISIBLE
                        }

                        Status.ERROR -> {
                            binding. progressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        )

        binding.   closeBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun processData(data: PoiIconsResponse) {
        binding. poiIconList.apply {
            layoutManager = GridLayoutManager(this@PoiIconDialog.context, 6)
            adapter = PoiIconAdapter(data.items) { item -> onPoiIconClick(item) }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.customDialogTheme)
        dialog.setContentView(R.layout.dialog_poi_icons)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                dialog?.window?.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= DialogPoiIconsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onPoiIconClick(iconItems: IconItems) {
        poiDialog.setIconItems(iconItems)
        dialog?.dismiss()
    }

}