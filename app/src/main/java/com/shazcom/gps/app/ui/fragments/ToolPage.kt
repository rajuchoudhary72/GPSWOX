package com.shazcom.gps.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.shazcom.gps.app.R
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.*
import kotlinx.android.synthetic.main.fragment_tool_page.*

class ToolPage : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alerts.setOnClickListener {
            findNavController().navigate(R.id.alerts)
        }

        points.setOnClickListener {
            Intent(requireActivity(), ShowPoint::class.java).apply {
                startActivity(this)
            }
        }

        ruler.setOnClickListener {
            Intent(requireActivity(), Rulers::class.java).apply {
                startActivity(this)
            }
        }

        poi.setOnClickListener {
            Intent(requireActivity(), POI::class.java).apply {
                startActivity(this)
            }
        }

        geoFencing.setOnClickListener {
            Intent(requireActivity(), GeoFencing::class.java).apply {
                startActivity(this)
            }
        }

        tasks.setOnClickListener {
            Intent(requireActivity(), TaskPage::class.java).apply {
                startActivity(this)
            }
        }

        gprs.setOnClickListener {
            Intent(requireActivity(), GprsCommand::class.java).apply {
                startActivity(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tool_page, container, false)
    }
}