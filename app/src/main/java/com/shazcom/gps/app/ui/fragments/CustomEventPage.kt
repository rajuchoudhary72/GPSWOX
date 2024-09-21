package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.CustomEventResponse
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.adapter.CustomEventAdapter
import com.shazcom.gps.app.ui.dialogs.AddEventDialog
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel
import kotlinx.android.synthetic.main.empty_layout.*
import kotlinx.android.synthetic.main.fragment_custom_event_page.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class CustomEventPage : BaseFragment(), KodeinAware {

    override val kodein by closestKodein()
    private val localDB: LocalDB by instance()
    private val repository: ToolsRepository by instance()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_custom_event_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.GONE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.VISIBLE

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadCustomEvents()

        addEvent.setOnClickListener {
            val addEventDialog = AddEventDialog(this)
            addEventDialog.show(childFragmentManager, AddEventDialog::class.java.name)
        }
    }

    fun loadCustomEvents() {
        toolsViewModel?.loadCustomEvents("en", localDB.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->
                when (resources.status) {
                    Status.SUCCESS -> {
                        progressBar.visibility = View.INVISIBLE
                        processData(resources.data)
                    }
                    Status.ERROR -> {
                        progressBar.visibility = View.INVISIBLE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "No Driver Found"
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.VISIBLE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.GONE

    }


    private fun processData(data: CustomEventResponse?) {
        eventList.apply {
            layoutManager = LinearLayoutManager(this@CustomEventPage.context)
            adapter = CustomEventAdapter(data?.items?.events?.data!!)
        }
    }

}