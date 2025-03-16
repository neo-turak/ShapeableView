package com.github.neoturak.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.github.neoturak.R
import com.github.neoturak.utils.ViewUtils

class ShapeableImageView : AppCompatImageView {

    // Corners
    var cornersRadius = 0f
        set(value) {
            field = value
            setAttrs()
            updateClipPath()
            invalidate()
        }
    var cornerTopLeft = 0f
        set(value) {
            field = value
            setAttrs()
            updateClipPath()
            invalidate()
        }
    var cornerTopRight = 0f
        set(value) {
            field = value
            setAttrs()
            updateClipPath()
            invalidate()
        }
    var cornerBottomLeft = 0f
        set(value) {
            field = value
            setAttrs()
            updateClipPath()
            invalidate()
        }
    var cornerBottomRight = 0f
        set(value) {
            field = value
            setAttrs()
            updateClipPath()
            invalidate()
        }

    // Strokes
    var strokeColor = 0
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }
    var strokeWidth = 0f
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }

    // Solid color
    var soldColor: Int = 0
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }

    // Gradient colors
    var startColor = 0
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }
    var endColor = 0
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }
    var centerColor = 0
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }

    // Angle
    var angle = 0
        set(value) {
            field = value
            setAttrs()
            invalidate()
        }

    private val path = Path() // Non-null, initialized Path for clipping
    private val strokePaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    private fun provideWithCare(f: Float): Float {
        return if (f == 0f) cornersRadius else f
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        val ta = context!!.obtainStyledAttributes(attrs, R.styleable.ShapeableImageView)
        cornersRadius = ta.getDimension(R.styleable.ShapeableImageView_shape_cornersRadius, 0f)
        cornerTopLeft = ta.getDimension(R.styleable.ShapeableImageView_shape_cornerTopLeft, 0f)
        cornerTopRight = ta.getDimension(R.styleable.ShapeableImageView_shape_cornerTopRight, 0f)
        cornerBottomLeft =
            ta.getDimension(R.styleable.ShapeableImageView_shape_cornerBottomLeft, 0f)
        cornerBottomRight =
            ta.getDimension(R.styleable.ShapeableImageView_shape_cornerBottomRight, 0f)
        strokeColor = ta.getColor(R.styleable.ShapeableImageView_shape_strokeColor, Color.WHITE)
        strokeWidth = ta.getDimension(R.styleable.ShapeableImageView_shape_strokeWidth, 0f)
        soldColor = ta.getColor(R.styleable.ShapeableImageView_shape_soldColor, Color.WHITE)
        startColor = ta.getColor(R.styleable.ShapeableImageView_gradient_startColor, 0)
        centerColor = ta.getColor(R.styleable.ShapeableImageView_gradient_centerColor, 0)
        endColor = ta.getColor(R.styleable.ShapeableImageView_gradient_endColor, 0)
        angle = ta.getInteger(R.styleable.ShapeableImageView_gradient_angle, 6)
        ta.recycle()

        //  scaleType = ScaleType.CENTER_CROP
        setAttrs()
        if (width == 0 || height == 0) {
            post {
                updateClipPath()
                invalidate()
                //"initView: Deferred clip path update")
            }
        } else {
            updateClipPath()
        }
    }

    private fun setAttrs() {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        val corners = floatArrayOf(
            provideWithCare(cornerTopLeft), provideWithCare(cornerTopLeft),
            provideWithCare(cornerTopRight), provideWithCare(cornerTopRight),
            provideWithCare(cornerBottomRight), provideWithCare(cornerBottomRight),
            provideWithCare(cornerBottomLeft), provideWithCare(cornerBottomLeft)
        )
        shape.cornerRadii = corners
        val realAngle = ViewUtils().realAngle(angle)
        if (startColor == endColor && startColor == centerColor) {
            shape.color = ColorStateList.valueOf(soldColor)
        } else {
            if (endColor == 0) endColor = Color.WHITE
            if (centerColor == 0) centerColor = ViewUtils().middleColor(startColor, endColor)
            shape.colors = intArrayOf(startColor, centerColor, endColor)
            shape.orientation = realAngle
        }
        this.background = shape
        strokePaint.strokeWidth = strokeWidth
        strokePaint.color = strokeColor
    }

    private fun updateClipPath() {
        if (path == null) return //this is swill needed,
        path.reset() // No null check needed since path is always initialized
        if (width == 0 || height == 0) {
            //updateClipPath: Width or height is 0, skipping
            return
        }
        val corners = floatArrayOf(
            provideWithCare(cornerTopLeft), provideWithCare(cornerTopLeft),
            provideWithCare(cornerTopRight), provideWithCare(cornerTopRight),
            provideWithCare(cornerBottomRight), provideWithCare(cornerBottomRight),
            provideWithCare(cornerBottomLeft), provideWithCare(cornerBottomLeft)
        )
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(rect, corners, Path.Direction.CW)
        // "updateClipPath: Path updated - Width=$width, Height=$height, Corners=${corners.joinToString()}"
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateClipPath()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (path.isEmpty) {
            //  "onDraw: Path is empty, no clipping applied")
            super.onDraw(canvas)
        } else {
            canvas.save()
            canvas.clipPath(path)
            super.onDraw(canvas)
            // Draw stroke on top of the clipped image
            if (strokeWidth > 0f && strokeColor != Color.TRANSPARENT) {
                canvas.drawPath(path, strokePaint)
            }
            canvas.restore()
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        updateClipPath()
        invalidate()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        updateClipPath()
        invalidate()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        updateClipPath()
        invalidate()
    }
}
