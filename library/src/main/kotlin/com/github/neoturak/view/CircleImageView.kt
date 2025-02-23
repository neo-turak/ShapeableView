/*
 * Copyright 2014 - 2020 Henning Dodenhof
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.neoturak.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.github.neoturak.R
import kotlin.math.pow

@Suppress("UnusedDeclaration")
class CircleImageView : AppCompatImageView {

    companion object {
        private val SCALE_TYPE = ScaleType.CENTER_CROP
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLORDRAWABLE_DIMENSION = 2
        private const val DEFAULT_BORDER_WIDTH = 0
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        private const val DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT
        private const val DEFAULT_IMAGE_ALPHA = 255
        private const val DEFAULT_BORDER_OVERLAY = false
    }

    private val drawableRect = RectF()
    private val borderRect = RectF()
    private val shaderMatrix = Matrix()
    private val bitmapPaint = Paint()
    private val borderPaint = Paint()
    private val circleBackgroundPaint = Paint()

    private var borderColor = DEFAULT_BORDER_COLOR
    private var borderWidth = DEFAULT_BORDER_WIDTH
    private var circleBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR
    private var imageAlpha = DEFAULT_IMAGE_ALPHA

    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private var drawableRadius: Float = 0f
    private var borderRadius: Float = 0f

    private var colorFilter: ColorFilter? = null

    private var initialized = false
    private var rebuildShader = false
    private var drawableDirty = false
    private var borderOverlay = DEFAULT_BORDER_OVERLAY
    private var disableCircularTransformation = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)
        borderWidth = a.getDimensionPixelSize(
            R.styleable.CircleImageView_civ_border_width,
            DEFAULT_BORDER_WIDTH
        )
        borderColor = a.getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR)
        borderOverlay =
            a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY)
        circleBackgroundColor = a.getColor(
            R.styleable.CircleImageView_civ_circle_background_color,
            DEFAULT_CIRCLE_BACKGROUND_COLOR
        )
        a.recycle()
        init()
    }

    private fun init() {
        initialized = true
        super.setScaleType(SCALE_TYPE)

        bitmapPaint.isAntiAlias = true
        bitmapPaint.isDither = true
        bitmapPaint.isFilterBitmap = true
        bitmapPaint.alpha = imageAlpha
        bitmapPaint.colorFilter = colorFilter

        borderPaint.style = Paint.Style.STROKE
        borderPaint.isAntiAlias = true
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth.toFloat()

        circleBackgroundPaint.style = Paint.Style.FILL
        circleBackgroundPaint.isAntiAlias = true
        circleBackgroundPaint.color = circleBackgroundColor

        outlineProvider = OutlineProvider()
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType != SCALE_TYPE) {
            throw IllegalArgumentException("ScaleType $scaleType not supported.")
        }
    }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        if (adjustViewBounds) {
            throw IllegalArgumentException("adjustViewBounds not supported.")
        }
    }

    @SuppressLint("CanvasSize")
    override fun onDraw(canvas: Canvas) {
        if (disableCircularTransformation) {
            super.onDraw(canvas)
            return
        }

        if (circleBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(
                drawableRect.centerX(),
                drawableRect.centerY(),
                drawableRadius,
                circleBackgroundPaint
            )
        }

        bitmap?.let { bitmap ->
            if (drawableDirty && bitmapCanvas != null) {
                drawableDirty = false
                val drawable = drawable
                drawable.setBounds(0, 0, bitmapCanvas!!.width, bitmapCanvas!!.height)
                drawable.draw(bitmapCanvas!!)
            }

            if (rebuildShader) {
                rebuildShader = false
                val bitmapShader =
                    BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                bitmapShader.setLocalMatrix(shaderMatrix)
                bitmapPaint.shader = bitmapShader
            }

            canvas.drawCircle(
                drawableRect.centerX(),
                drawableRect.centerY(),
                drawableRadius,
                bitmapPaint
            )
        }

        if (borderWidth > 0) {
            canvas.drawCircle(borderRect.centerX(), borderRect.centerY(), borderRadius, borderPaint)
        }
    }

    override fun invalidateDrawable(dr: Drawable) {
        drawableDirty = true
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateDimensions()
        invalidate()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        updateDimensions()
        invalidate()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        updateDimensions()
        invalidate()
    }

    fun getBorderColor(): Int = borderColor

    fun setBorderColor(@ColorInt borderColor: Int) {
        if (borderColor == this.borderColor) return
        this.borderColor = borderColor
        borderPaint.color = borderColor
        invalidate()
    }

    fun getCircleBackgroundColor(): Int = circleBackgroundColor

    fun setCircleBackgroundColor(@ColorInt circleBackgroundColor: Int) {
        if (circleBackgroundColor == this.circleBackgroundColor) return
        this.circleBackgroundColor = circleBackgroundColor
        circleBackgroundPaint.color = circleBackgroundColor
        invalidate()
    }

    @Deprecated(
        "Use setCircleBackgroundColor instead",
        ReplaceWith("setCircleBackgroundColor(context.resources.getColor(circleBackgroundRes))")
    )
    fun setCircleBackgroundColorResource(@ColorRes circleBackgroundRes: Int) {
        setCircleBackgroundColor(context.resources.getColor(circleBackgroundRes))
    }

    fun getBorderWidth(): Int = borderWidth

    fun setBorderWidth(borderWidth: Int) {
        if (borderWidth == this.borderWidth) return
        this.borderWidth = borderWidth
        borderPaint.strokeWidth = borderWidth.toFloat()
        updateDimensions()
        invalidate()
    }

    fun isBorderOverlay(): Boolean = borderOverlay

    fun setBorderOverlay(borderOverlay: Boolean) {
        if (borderOverlay == this.borderOverlay) return
        this.borderOverlay = borderOverlay
        updateDimensions()
        invalidate()
    }

    fun isDisableCircularTransformation(): Boolean = disableCircularTransformation

    fun setDisableCircularTransformation(disableCircularTransformation: Boolean) {
        if (disableCircularTransformation == this.disableCircularTransformation) return
        this.disableCircularTransformation = disableCircularTransformation
        if (disableCircularTransformation) {
            bitmap = null
            bitmapCanvas = null
            bitmapPaint.shader = null
        } else {
            initializeBitmap()
        }
        invalidate()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        initializeBitmap()
        invalidate()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
        invalidate()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
        invalidate()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
        invalidate()
    }

    override fun setImageAlpha(alpha: Int) {
        val clampedAlpha = alpha and 0xFF
        if (clampedAlpha == imageAlpha) return
        imageAlpha = clampedAlpha
        if (initialized) {
            bitmapPaint.alpha = clampedAlpha
            invalidate()
        }
    }

    override fun getImageAlpha(): Int = imageAlpha

    override fun setColorFilter(cf: ColorFilter?) {
        if (cf == colorFilter) return
        colorFilter = cf
        if (initialized) {
            bitmapPaint.colorFilter = cf
            invalidate()
        }
    }

    override fun getColorFilter(): ColorFilter? = colorFilter

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        return try {
            val bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    BITMAP_CONFIG
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initializeBitmap() {
        bitmap = getBitmapFromDrawable(drawable)
        bitmapCanvas = bitmap?.takeIf { it.isMutable }?.let { Canvas(it) }
        if (!initialized) return
        if (bitmap != null) {
            updateShaderMatrix()
        } else {
            bitmapPaint.shader = null
        }
    }

    private fun updateDimensions() {
        borderRect.set(calculateBounds())
        borderRadius = minOf(
            (borderRect.height() - borderWidth) / 2.0f,
            (borderRect.width() - borderWidth) / 2.0f
        )
        drawableRect.set(borderRect)
        if (!borderOverlay && borderWidth > 0) {
            drawableRect.inset(borderWidth - 1.0f, borderWidth - 1.0f)
        }
        drawableRadius = minOf(drawableRect.height() / 2.0f, drawableRect.width() / 2.0f)
        updateShaderMatrix()
    }

    private fun calculateBounds(): RectF {
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        val sideLength = minOf(availableWidth, availableHeight)
        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f
        return RectF(left, top, left + sideLength, top + sideLength)
    }

    private fun updateShaderMatrix() {
        bitmap ?: return
        val scale: Float
        var dx = 0f
        var dy = 0f
        shaderMatrix.set(null)
        val bitmapHeight = bitmap!!.height
        val bitmapWidth = bitmap!!.width

        if (bitmapWidth * drawableRect.height() > drawableRect.width() * bitmapHeight) {
            scale = drawableRect.height() / bitmapHeight.toFloat()
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5f
        } else {
            scale = drawableRect.width() / bitmapWidth.toFloat()
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5f
        }

        shaderMatrix.setScale(scale, scale)
        shaderMatrix.postTranslate(
            (dx + 0.5f).toInt() + drawableRect.left,
            (dy + 0.5f).toInt() + drawableRect.top
        )
        rebuildShader = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (disableCircularTransformation) {
            super.onTouchEvent(event)
        } else {
            inTouchableArea(event.x, event.y) && super.onTouchEvent(event)
        }
    }

    private fun inTouchableArea(x: Float, y: Float): Boolean {
        if (borderRect.isEmpty) return true
        return (x - borderRect.centerX()).pow(2) + (y - borderRect.centerY()).pow(2) <= borderRadius.pow(
            2
        )
    }

    private inner class OutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            if (disableCircularTransformation) {
                BACKGROUND.getOutline(view, outline)
            } else {
                val bounds = Rect()
                borderRect.roundOut(bounds)
                outline.setRoundRect(bounds, bounds.width() / 2.0f)
            }
        }
    }
}