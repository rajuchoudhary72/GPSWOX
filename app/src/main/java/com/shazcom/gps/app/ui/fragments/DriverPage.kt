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
import com.shazcom.gps.app.data.response.UserDriverResponse
import com.shazcom.gps.app.databinding.FragmentDriverPageBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.adapter.DriverAdapter
import com.shazcom.gps.app.ui.dialogs.AddDriverDialog
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel

import org.kodein.di.KodeinAware
import org.kodein.di.android.support.closestKodein
import org.kodein.di.generic.instance

class DriverPage : BaseFragment(), KodeinAware {

    private lateinit var binding: FragmentDriverPageBinding
    override val kodein by closestKodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding=  FragmentDriverPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.GONE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.VISIBLE

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository

        loadDrivers()

        binding.addDriver.setOnClickListener {
            val addDriverDialog = AddDriverDialog(this)
            addDriverDialog.show(childFragmentManager, AddDriverDialog::class.java.name)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.VISIBLE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.GONE

    }

    fun loadDrivers() {
        toolsViewModel?.loadDrivers("en", localDB.getToken()!!)
            ?.observe(requireActivity(), Observer { resources ->
                if(isVisible) {
                    when (resources.status) {
                        Status.SUCCESS -> {
                            binding.      progressBar.visibility = View.INVISIBLE
                            processData(resources.data)
                        }
                        Status.ERROR -> {
                            binding.   progressBar.visibility = View.INVISIBLE
                            binding.inc.  emptyText.visibility = View.VISIBLE
                            binding.inc. emptyText.text = "No Driver Found"
                        }
                        Status.LOADING -> {
                            binding.  progressBar.visibility = View.VISIBLE
                        }
                    }
                }

            })
    }

    private fun processData(data: UserDriverResponse?) {
        binding.  driverList.apply {
            layoutManager = LinearLayoutManager(this@DriverPage.context)
            adapter = DriverAdapter(data?.items?.drivers?.data!!)
        }
    }
}