package com.github.neoturak

import android.content.Context
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar

internal inline fun <reified T : Any> Context.dp2px(v: T): T {
    when (v) {
        is Int -> {
            return (resources.displayMetrics.density.times(v).plus(0.5)).toInt() as T
        }

        is Float -> {
            return (resources.displayMetrics.density.times(v).plus(0.5)).toFloat() as T
        }

        is Double -> {
            return (resources.displayMetrics.density.times(v).plus(0.5)) as T
        }

        is Long -> {
            return (resources.displayMetrics.density.times(v).plus(0.5)).toLong() as T
        }

        else -> {
            return 0 as T
        }
    }
}

internal inline fun AppCompatSeekBar.valueChangeListener(crossinline onChange: (v: Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onChange.invoke(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }

    })
}