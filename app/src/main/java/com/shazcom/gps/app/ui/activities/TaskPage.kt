package com.shazcom.gps.app.ui.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.LocalDB
import com.shazcom.gps.app.data.repository.ToolsRepository
import com.shazcom.gps.app.data.response.TaskListResponse
import com.shazcom.gps.app.databinding.TaskListBinding
import com.shazcom.gps.app.network.internal.Status
import com.shazcom.gps.app.ui.BaseActivity
import com.shazcom.gps.app.ui.adapter.TaskAdapter
import com.shazcom.gps.app.ui.dialogs.AddTaskDialog
import com.shazcom.gps.app.ui.dialogs.LocationPickerDialog
import com.shazcom.gps.app.ui.viewmodal.ToolsViewModel

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class TaskPage : BaseActivity(), KodeinAware {

    private lateinit var binding: TaskListBinding
    override val kodein by kodein()
    private val localDB: LocalDB by instance<LocalDB>()
    private val repository: ToolsRepository by instance<ToolsRepository>()
    private var toolsViewModel: ToolsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=TaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setNavigationOnClickListener { finish() }

        toolsViewModel = ViewModelProvider(this).get(ToolsViewModel::class.java)
        toolsViewModel?.toolsRepository = repository
        loadTaskList()

        binding.addTask.setOnClickListener {
            val addTaskDialog = AddTaskDialog(this)
            addTaskDialog.show(supportFragmentManager, AddTaskDialog::class.java.name)
        }
    }

    fun loadTaskList() {
        toolsViewModel?.loadTask("en", localDB.getToken()!!)?.observe(this, Observer { resources ->

            when (resources.status) {
                Status.SUCCESS -> {
                    processData(resources.data!!)
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.inc.emptyText.visibility = View.INVISIBLE
                }
                Status.ERROR -> {
                    binding. progressBar.visibility = View.INVISIBLE
                    binding.inc. emptyText.visibility = View.VISIBLE
                }
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.inc. emptyText.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun processData(data: TaskListResponse) {
        data?.items?.let {
            binding. taskList.apply {
                layoutManager = LinearLayoutManager(this@TaskPage)
                adapter = TaskAdapter(it.data)
            }
        }
    }

    override fun popUpAddress(addressOutput: String) {
        for (fragment in supportFragmentManager.fragments) {
            for (frag in fragment.childFragmentManager.fragments) {
                if (frag.isVisible && frag is LocationPickerDialog) {
                    frag.showAddress(addressOutput)
                    break
                }
            }
        }

    }
}