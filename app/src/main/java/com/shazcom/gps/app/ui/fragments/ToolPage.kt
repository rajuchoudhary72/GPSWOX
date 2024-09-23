package com.shazcom.gps.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.shazcom.gps.app.R
import com.shazcom.gps.app.databinding.ActivityAlertsBinding
import com.shazcom.gps.app.databinding.FragmentToolPageBinding
import com.shazcom.gps.app.ui.BaseFragment
import com.shazcom.gps.app.ui.activities.*


class ToolPage : BaseFragment() {

    private lateinit var binding: FragmentToolPageBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alerts.setOnClickListener {
            findNavController().navigate(R.id.alerts)
        }

        binding.points.setOnClickListener {
            Intent(requireActivity(), ShowPoint::class.java).apply {
                startActivity(this)
            }
        }

        binding.ruler.setOnClickListener {
            Intent(requireActivity(), Rulers::class.java).apply {
                startActivity(this)
            }
        }

        binding.poi.setOnClickListener {
            Intent(requireActivity(), POI::class.java).apply {
                startActivity(this)
            }
        }

        binding.geoFencing.setOnClickListener {
            Intent(requireActivity(), GeoFencing::class.java).apply {
                startActivity(this)
            }
        }

        binding.tasks.setOnClickListener {
            Intent(requireActivity(), TaskPage::class.java).apply {
                startActivity(this)
            }
        }

        binding.gprs.setOnClickListener {
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
       binding= FragmentToolPageBinding.inflate(inflater, container, false)
        return binding.root
    }
}