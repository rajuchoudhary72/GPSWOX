package com.shazcom.gps.app.ui.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.shazcom.gps.app.R
import com.shazcom.gps.app.databinding.LayoutColorDialogBinding

import okhttp3.internal.toHexString

class ColorDialog(private val geoFenceDialog: GeoFenceDialog) : DialogFragment(),
    SeekBar.OnSeekBarChangeListener {

    private lateinit var binding: LayoutColorDialogBinding
    private var colorRedValue = 0
    private var colorGreenValue = 0
    private var colorBlueValue = 0
    private var hexColor = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      binding=  LayoutColorDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.colorRed.setOnSeekBarChangeListener(this)
        binding. colorGreen.setOnSeekBarChangeListener(this)
        binding. colorBlue.setOnSeekBarChangeListener(this)

        colorRedValue = binding.colorRed.progress
        colorBlueValue =binding. colorBlue.progress
        colorGreenValue = binding.colorGreen.progress

        val rgbColor = Color.rgb(colorRedValue, colorGreenValue, colorBlueValue)
        binding. colorPane.setBackgroundColor(rgbColor)
        if (rgbColor.toString().length > 6) {
            binding.colorCode.setText(rgbColor.toHexString().substring(2, 8))
        } else {
            binding. colorCode.setText(rgbColor.toHexString())
        }

    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        when (seekBar?.id) {

            R.id.colorBlue -> {
                colorBlueValue = progress
            }

            R.id.colorGreen -> {
                colorGreenValue = progress
            }

            R.id.colorRed -> {
                colorRedValue = progress
            }
        }

        updateColorPane()

        binding. selectBtn.setOnClickListener {
            geoFenceDialog.addPolygonColor(hexColor)
            dismiss()
        }
    }

    private fun updateColorPane() {
        val rgbColor = Color.rgb(colorRedValue, colorGreenValue, colorBlueValue)
        binding. colorPane.setBackgroundColor(rgbColor)
        hexColor = rgbColor.toHexString()
        if (hexColor.length > 6) {
            binding. colorCode.setText(hexColor.substring(2, 8))
        } else {
            binding.  colorCode.setText(hexColor)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

}

