package com.shazcom.gps.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.shazcom.gps.app.R
import com.shazcom.gps.app.data.response.Groups
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.Dashboard
import com.shazcom.gps.app.ui.adapter.GroupAdapter
import kotlinx.android.synthetic.main.fragment_group_page.*

class GroupPage : BaseFragment() {

    var activity: Dashboard? = null
    private var groups: List<Groups>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.GONE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.VISIBLE



        activity = getActivity() as Dashboard
        groups = activity?.getGroups()
        loadGroups()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.nav_icon).visibility =
            View.VISIBLE
        (requireActivity() as Dashboard).findViewById<ImageView>(R.id.back_icon).visibility =
            View.GONE

    }


    private fun loadGroups() {
        groupList.apply {
            layoutManager = LinearLayoutManager(this@GroupPage.context)
            adapter = GroupAdapter(groups!!)
        }
    }
}