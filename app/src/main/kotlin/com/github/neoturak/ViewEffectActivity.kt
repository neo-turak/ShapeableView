package com.github.neoturak

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.github.neoturak.viewer.databinding.ActivityViewEffectBinding


class ViewEffectActivity : AppCompatActivity() {
    private var _binding: ActivityViewEffectBinding? = null
    private val binding: ActivityViewEffectBinding by lazy { _binding!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _binding = ActivityViewEffectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //corner radius
        binding.sbCornerRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.sv.cornersRadius = dp2px(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        //top-left corner

        binding.sbCornerRadiusLeftTop.valueChangeListener {
            binding.sv.cornerTopLeft = dp2px(it.toFloat())
        }
        //top-right corner
        binding.sbCornerRadiusRightTop.valueChangeListener {
            binding.sv.cornerTopRight = dp2px(it.toFloat())
        }

        //bottom-left corner
        binding.sbCornerRadiusLeftBottom.valueChangeListener {
            binding.sv.cornerBottomLeft = dp2px(it.toFloat())
        }

        //bottom-right corner
        binding.sbCornerRadiusRightBottom.valueChangeListener {
            binding.sv.cornerBottomRight = dp2px(it.toFloat())
        }

        //bg color
        binding.cvvSoldColor.setOnClickListener {
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose sold color")
                .initialColor(Color.BLACK)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->

                }
                .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                    binding.cvvSoldColor.setBackgroundColor(selectedColor)
                    binding.sv.soldColor = selectedColor
                }
                .build()
                .show()
        }


        binding.sbStrokeWidth.valueChangeListener {
            binding.sv.strokeWidth = dp2px(it.toFloat())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}