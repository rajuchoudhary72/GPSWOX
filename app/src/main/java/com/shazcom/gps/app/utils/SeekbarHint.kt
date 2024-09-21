package com.shazcom.gps.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import com.shazcom.gps.app.R


class SeekbarHint : androidx.appcompat.widget.AppCompatSeekBar {

    private val MIN_PROGRESS_VALUE = 0
    private val MAX_PROGRESS_VALUE = 255

    private var mSeekBarHintPaint: Paint? = null
    private var mHintTextColor: Int = 0
    private var mHintTextSize: Float = 0.toFloat()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SeekbarHint,
            0, 0
        )

        try {
            mHintTextColor = a.getColor(R.styleable.SeekbarHint_hint_text_color, 0)
            mHintTextSize = a.getDimension(R.styleable.SeekbarHint_hint_text_size, 0f)
        } finally {
            a.recycle()
        }

        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        max = MAX_PROGRESS_VALUE
        mSeekBarHintPaint = TextPaint()
        mSeekBarHintPaint?.color = mHintTextColor
        mSeekBarHintPaint?.textSize = mHintTextSize
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val label_x = thumb.bounds.centerX() - (mHintTextSize / 1)
        val label_y = thumb.bounds.centerY() + (mHintTextSize / 4)

        mSeekBarHintPaint?.let {
            canvas.drawText(
                "$progress",
                if (progress > 90) label_x - (mHintTextSize / 2) else label_x.toFloat(),
                label_y.toFloat(),
                it
            )
        }

    }
}