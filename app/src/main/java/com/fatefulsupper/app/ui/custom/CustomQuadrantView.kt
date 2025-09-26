package com.fatefulsupper.app.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CustomQuadrantView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnQuadrantSelectedListener {
        fun onQuadrantSelected(x: Float, y: Float)
    }

    private var listener: OnQuadrantSelectedListener? = null

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        // For grid lines, Butt cap is often preferred to avoid overdraw at intersections
        strokeCap = Paint.Cap.BUTT
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    private val selectedPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6200EE")
        style = Paint.Style.FILL
    }

    private val centerPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private var viewWidth = 0f
    private var viewHeight = 0f

    // These represent the center of the touchable padded area, used for touch-to-logic conversion
    private var paddedCenterX = 0f
    private var paddedCenterY = 0f

    private var selectedLogicX: Float? = null
    private var selectedLogicY: Float? = null

    private val defaultLogicX: Float = 0f
    private val defaultLogicY: Float = 0f

    private val L_MIN = -5.0f
    private val L_MAX = 5.0f
    private val POINT_RADIUS = 15f
    private val GRID_INTERVAL = 1.0f
    private val LABEL_OUTER_MARGIN = 20f

    // RectF for the actual drawing area of the quadrant (grid and axes)
    // This area is inset from the padding by half the widest stroke width used for grid/axes
    private val contentAreaRect = RectF()
    private var contentCenterX = 0f
    private var contentCenterY = 0f

    init {}

    fun setOnQuadrantSelectedListener(listener: OnQuadrantSelectedListener?) {
        this.listener = listener
    }

    fun setInitialPoint(x: Float, y: Float) {
        val clampedX = clampLogicCoordinate(x)
        val clampedY = clampLogicCoordinate(y)
        selectedLogicX = String.format("%.1f", clampedX).replace(',', '.').toFloat()
        selectedLogicY = String.format("%.1f", clampedY).replace(',', '.').toFloat()
        invalidate()
    }

    fun getSelectedPoint(): Pair<Float, Float> {
        return Pair(selectedLogicX ?: defaultLogicX, selectedLogicY ?: defaultLogicY)
    }

    fun clearSelection() {
        selectedLogicX = null
        selectedLogicY = null
        invalidate()
    }

    fun setAxisColor(color: Int) {
        axisPaint.color = color
        invalidate()
    }

    fun setGridColor(color: Int) {
        gridPaint.color = color
        invalidate()
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
        invalidate()
    }

    fun setSelectedPointColor(color: Int) {
        selectedPointPaint.color = color
        invalidate()
    }

    fun setCenterPointColor(color: Int) {
        centerPointPaint.color = color
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()

        // Center of the padded area (for touch and logic mapping)
        paddedCenterX = paddingLeft + (w - paddingLeft - paddingRight) / 2f
        paddedCenterY = paddingTop + (h - paddingTop - paddingBottom) / 2f

        // Calculate the actual content drawing area, inset from padding
        // to prevent clipping of lines due to stroke width.
        // Use the maximum of axis and grid stroke widths for inset calculation.
        val maxStrokeWidth = max(axisPaint.strokeWidth, gridPaint.strokeWidth)
        val inset = maxStrokeWidth / 2f

        contentAreaRect.set(
            paddingLeft + inset,
            paddingTop + inset,
            w - paddingRight - inset,
            h - paddingBottom - inset
        )

        contentCenterX = contentAreaRect.centerX()
        contentCenterY = contentAreaRect.centerY()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (contentAreaRect.width() <= 0 || contentAreaRect.height() <= 0) return

        val numberOfIntervals = ((L_MAX - L_MIN) / GRID_INTERVAL).toInt()
        val stepX = contentAreaRect.width() / numberOfIntervals
        val stepY = contentAreaRect.height() / numberOfIntervals

        // Draw Grid Lines
        for (i in 0..numberOfIntervals) {
            val x = contentAreaRect.left + i * stepX
            canvas.drawLine(x, contentAreaRect.top, x, contentAreaRect.bottom, gridPaint)
            val y = contentAreaRect.top + i * stepY
            canvas.drawLine(contentAreaRect.left, y, contentAreaRect.right, y, gridPaint)
        }

        // Draw X and Y Axes (centered within contentAreaRect based on L_MIN, L_MAX)
        // Find the pixel position of logical zero for X and Y axes
        val zeroLogicRatioX = -L_MIN / (L_MAX - L_MIN)
        val zeroLogicRatioY = -L_MIN / (L_MAX - L_MIN) // For Y, it's often inverted in display, but logic is same

        val axisXPosition = contentAreaRect.left + zeroLogicRatioX * contentAreaRect.width()
        val axisYPosition = contentAreaRect.top + zeroLogicRatioY * contentAreaRect.height() // This is logical 0,0 y-coord from top
        // For drawing, Y axis is traditionally from top to bottom, so logical Y=0 will be at axisYPosition
        // and X axis is logical X=0 will be at axisXPosition.

        canvas.drawLine(contentAreaRect.left, axisYPosition, contentAreaRect.right, axisYPosition, axisPaint) // X-axis (at logical Y=0)
        canvas.drawLine(axisXPosition, contentAreaRect.top, axisXPosition, contentAreaRect.bottom, axisPaint) // Y-axis (at logical X=0)


        // --- Draw Labels outside the quadrant box (relative to overall view and padding) ---
        textPaint.textSize = 32f
        val textMetrics = textPaint.fontMetrics

        textPaint.textAlign = Paint.Align.CENTER
        val topLabelY = paddingTop - LABEL_OUTER_MARGIN - textMetrics.descent
        canvas.drawText("開心 (Happy)", viewWidth / 2f, topLabelY, textPaint)

        val bottomLabelY = viewHeight - paddingBottom + LABEL_OUTER_MARGIN - textMetrics.ascent
        canvas.drawText("不開心 (Unhappy)", viewWidth / 2f, bottomLabelY, textPaint)

        textPaint.textAlign = Paint.Align.CENTER
        val leftLabelText = "不餓 (Not Hungry)"
        canvas.save()
        val rotateXLeft = paddingLeft - LABEL_OUTER_MARGIN - (textMetrics.descent - textMetrics.ascent) / 2f
        canvas.rotate(-90f, rotateXLeft, viewHeight / 2f)
        canvas.drawText(leftLabelText, rotateXLeft, viewHeight / 2f - (textMetrics.descent + textMetrics.ascent) / 2f, textPaint)
        canvas.restore()

        val rightLabelText = "飢餓 (Hungry)"
        canvas.save()
        val rotateXRight = viewWidth - paddingRight + LABEL_OUTER_MARGIN + (textMetrics.descent - textMetrics.ascent) / 2f
        canvas.rotate(90f, rotateXRight, viewHeight / 2f)
        canvas.drawText(rightLabelText, rotateXRight, viewHeight / 2f - (textMetrics.descent + textMetrics.ascent) / 2f, textPaint)
        canvas.restore()

        textPaint.textAlign = Paint.Align.CENTER

        // Determine point to draw
        val currentLogicX = selectedLogicX ?: defaultLogicX
        val currentLogicY = selectedLogicY ?: defaultLogicY
        val paintToUse = if (selectedLogicX != null || selectedLogicY != null) selectedPointPaint else centerPointPaint

        // Convert logic to pixel for drawing the point (within contentAreaRect)
        // Ratio of current logic point within the L_MIN to L_MAX range
        val xRatio = (currentLogicX - L_MIN) / (L_MAX - L_MIN)
        val yRatio = (currentLogicY - L_MIN) / (L_MAX - L_MIN) // yRatio is from bottom-up logic

        // Pixel position within contentAreaRect. Y is inverted for canvas drawing (top-down)
        val pixelSelectedX = contentAreaRect.left + xRatio * contentAreaRect.width()
        val pixelSelectedY = contentAreaRect.bottom - yRatio * contentAreaRect.height()

        canvas.drawCircle(pixelSelectedX, pixelSelectedY, POINT_RADIUS, paintToUse)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        // Touch events are still relative to the padded area, not the inset contentAreaRect
        val effectivePaddingLeft = paddingLeft.toFloat()
        val effectivePaddingTop = paddingTop.toFloat()
        val effectiveDrawableWidth = viewWidth - paddingLeft - paddingRight
        val effectiveDrawableHeight = viewHeight - paddingTop - paddingBottom

        val touchX = event.x.coerceIn(effectivePaddingLeft, effectivePaddingLeft + effectiveDrawableWidth)
        val touchY = event.y.coerceIn(effectivePaddingTop, effectivePaddingTop + effectiveDrawableHeight)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                updateSelectedPointFromTouch(touchX, touchY)
                return true
            }
            MotionEvent.ACTION_UP -> {
                updateSelectedPointFromTouch(touchX, touchY)
                if (selectedLogicX != null && selectedLogicY != null) {
                    listener?.onQuadrantSelected(selectedLogicX!!, selectedLogicY!!)
                }
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateSelectedPointFromTouch(touchX: Float, touchY: Float) {
        // Logic calculation uses the padded area dimensions and center
        val paddedAreaWidth = viewWidth - paddingLeft - paddingRight
        val paddedAreaHeight = viewHeight - paddingTop - paddingBottom

        if (paddedAreaWidth <= 0 || paddedAreaHeight <= 0) return

        // Calculate logicX/Y based on the center of the padded area (paddedCenterX, paddedCenterY)
        // (touchX - paddedCenterX) gives distance from center. Divide by half-width for ratio.
        var logicX = (touchX - paddedCenterX) / (paddedAreaWidth / 2f) * L_MAX
        var logicY = -(touchY - paddedCenterY) / (paddedAreaHeight / 2f) * L_MAX // Y is inverted

        logicX = clampLogicCoordinate(logicX)
        logicY = clampLogicCoordinate(logicY)

        val formattedX = String.format("%.1f", logicX).replace(',', '.').toFloat()
        val formattedY = String.format("%.1f", logicY).replace(',', '.').toFloat()

        if (selectedLogicX != formattedX || selectedLogicY != formattedY) {
            selectedLogicX = formattedX
            selectedLogicY = formattedY
            invalidate()
        }
    }

    private fun clampLogicCoordinate(value: Float): Float {
        return max(L_MIN, min(L_MAX, value))
    }
}
