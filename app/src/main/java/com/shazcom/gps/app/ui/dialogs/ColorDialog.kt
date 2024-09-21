package com.shazcom.gps.app.ui.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.shazcom.gps.app.R
import kotlinx.android.synthetic.main.layout_color_dialog.*
import okhttp3.internal.toHexString

class ColorDialog(private val geoFenceDialog: GeoFenceDialog) : DialogFragment(),
    SeekBar.OnSeekBarChangeListener {

    private var colorRedValue = 0
    private var colorGreenValue = 0
    private var colorBlueValue = 0
    private var hexColor = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_color_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorRed.setOnSeekBarChangeListener(this)
        colorGreen.setOnSeekBarChangeListener(this)
        colorBlue.setOnSeekBarChangeListener(this)

        colorRedValue = colorRed.progress
        colorBlueValue = colorBlue.progress
        colorGreenValue = colorGreen.progress

        val rgbColor = Color.rgb(colorRedValue, colorGreenValue, colorBlueValue)
        colorPane.setBackgroundColor(rgbColor)
        if (rgbColor.toString().length > 6) {
            colorCode.setText(rgbColor.toHexString().substring(2, 8))
        } else {
            colorCode.setText(rgbColor.toHexString())
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

        selectBtn.setOnClickListener {
            geoFenceDialog.addPolygonColor(hexColor)
            dismiss()
        }
    }

    private fun updateColorPane() {
        val rgbColor = Color.rgb(colorRedValue, colorGreenValue, colorBlueValue)
        colorPane.setBackgroundColor(rgbColor)
        hexColor = rgbColor.toHexString()
        if (hexColor.length > 6) {
            colorCode.setText(hexColor.substring(2, 8))
        } else {
            colorCode.setText(hexColor)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

}

