package com.shazcom.gps.app.ui.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.ProtocolResponse
import com.shazcom.gps.app.data.response.Protocols
import com.shazcom.gps.app.data.response.Types
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.fragments.CustomEventPage
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.add_custom_event.*
import kotlinx.android.synthetic.main.add_task.closeBtn
import kotlinx.android.synthetic.main.add_task.saveBtn
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class AddEventDialog(private val customEventPage: CustomEventPage) : DialogFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: ToolsRepository by instance()
    private var toolsViewModel: ToolsViewModel? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadProtocols()


        closeBtn.setOnClickListener {
            dismiss()
        }

        saveBtn.setOnClickListener {

        }
    }

    private fun loadProtocols() {
        toolsViewModel?.loadProtocols("en", localDB.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        setProtocolAndTypes(resources?.data!!)
                    }
                    else ->{}
                }
            })
    }

    private fun setProtocolAndTypes(data: ProtocolResponse) {

        val protocolAdapter = ArrayAdapter<Protocols>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            data?.protocols?: emptyList()
        )

        val typesAdapter = ArrayAdapter<Types>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            data?.types?: emptyList()
        )

        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.adapter = typesAdapter

        protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.adapter = protocolAdapter

        protocolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

        }


        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireActivity(), R.style.customDialogTheme)
        dialog.setContentView(R.layout.add_custom_event)
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
        return inflater.inflate(R.layout.add_custom_event, container, false)
    }
}