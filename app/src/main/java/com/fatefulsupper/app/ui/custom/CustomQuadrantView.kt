package com.fatefulsupper.app.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
        color = Color.DKGRAY // Changed to DKGRAY for softer axes
        strokeWidth = 4f    // Slightly thicker axes
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f // Slightly thicker grid lines
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f // Slightly larger text for labels
        textAlign = Paint.Align.CENTER
    }

    private val selectedPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6200EE") // Theme accent color (example)
        style = Paint.Style.FILL
    }

    private val centerPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY // Softer color for the default (0,0) point
        style = Paint.Style.FILL
    }

    private var viewWidth = 0f
    private var viewHeight = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var selectedLogicX: Float? = null
    private var selectedLogicY: Float? = null

    private val defaultLogicX: Float = 0f
    private val defaultLogicY: Float = 0f

    private val L_MIN = -5.0f
    private val L_MAX = 5.0f
    private val POINT_RADIUS = 15f // Slightly larger point
    private val AXIS_LABEL_PADDING = 50f // Increased padding for axis labels
    private val GRID_INTERVAL = 1.0f // Draw a grid line for every 1.0 unit

    init {
        // Consider adding custom attributes parsing here if needed
    }

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        // Ensure it's a square, using the smaller dimension if they differ
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
        // Calculate center considering padding
        centerX = paddingLeft + (w - paddingLeft - paddingRight) / 2f
        centerY = paddingTop + (h - paddingTop - paddingBottom) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val drawAreaWidth = viewWidth - paddingLeft - paddingRight
        val drawAreaHeight = viewHeight - paddingTop - paddingBottom

        if (drawAreaWidth <= 0 || drawAreaHeight <= 0) return // Nothing to draw

        // Draw Grid Lines
        val stepX = drawAreaWidth / (L_MAX - L_MIN) * GRID_INTERVAL
        val stepY = drawAreaHeight / (L_MAX - L_MIN) * GRID_INTERVAL

        var currentGridX = centerX - ( (0-L_MIN) / GRID_INTERVAL) * stepX // Start from -5 line
        while(currentGridX <= centerX + ( (L_MAX-0) / GRID_INTERVAL) * stepX + stepX/2) { // ensure 5 line is drawn
            if(currentGridX >= paddingLeft && currentGridX <= viewWidth - paddingRight) {
                canvas.drawLine(currentGridX, paddingTop.toFloat(), currentGridX, viewHeight - paddingBottom.toFloat(), gridPaint)
            }
            currentGridX += stepX
        }
        var currentGridY = centerY - ( (0-L_MIN) / GRID_INTERVAL) * stepY
        while(currentGridY <= centerY + ( (L_MAX-0) / GRID_INTERVAL) * stepY + stepY/2) {
            if(currentGridY >= paddingTop && currentGridY <= viewHeight - paddingBottom) {
                canvas.drawLine(paddingLeft.toFloat(), currentGridY, viewWidth - paddingRight.toFloat(), currentGridY, gridPaint)
            }
            currentGridY += stepY
        }


        // Draw X and Y Axes (on top of grid)
        canvas.drawLine(paddingLeft.toFloat(), centerY, viewWidth - paddingRight.toFloat(), centerY, axisPaint) // X-axis
        canvas.drawLine(centerX, paddingTop.toFloat(), centerX, viewHeight - paddingBottom.toFloat(), axisPaint) // Y-axis

        // Draw Labels for Axes
        textPaint.textSize = 32f
        canvas.drawText("開心 (Happy)", centerX, paddingTop + AXIS_LABEL_PADDING, textPaint) // Top
        canvas.drawText("不開心 (Unhappy)", centerX, viewHeight - paddingBottom - AXIS_LABEL_PADDING / 2, textPaint) // Bottom

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("不餓 (Not Hungry)", paddingLeft + AXIS_LABEL_PADDING / 3, centerY + textPaint.textSize / 3, textPaint) // Left
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("飢餓 (Hungry)", viewWidth - paddingRight - AXIS_LABEL_PADDING / 3, centerY + textPaint.textSize / 3, textPaint) // Right
        textPaint.textAlign = Paint.Align.CENTER // Reset

        // Determine point to draw
        val currentX = selectedLogicX ?: defaultLogicX
        val currentY = selectedLogicY ?: defaultLogicY
        val paintToUse = if (selectedLogicX != null || selectedLogicY != null) selectedPointPaint else centerPointPaint


        // Convert logic to pixel for drawing the point
        val pixelSelectedX = centerX + (currentX / L_MAX) * (drawAreaWidth / 2f)
        val pixelSelectedY = centerY - (currentY / L_MAX) * (drawAreaHeight / 2f) // Y is inverted

        canvas.drawCircle(pixelSelectedX, pixelSelectedY, POINT_RADIUS, paintToUse)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val touchX = event.x.coerceIn(paddingLeft.toFloat(), viewWidth - paddingRight.toFloat())
        val touchY = event.y.coerceIn(paddingTop.toFloat(), viewHeight - paddingBottom.toFloat())

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                updateSelectedPointFromTouch(touchX, touchY)
                return true
            }
            MotionEvent.ACTION_UP -> {
                updateSelectedPointFromTouch(touchX, touchY) // Final update
                if (selectedLogicX != null && selectedLogicY != null) { // Ensure a point is selected
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
        // Can be used by accessibility services
        return true
    }

    private fun updateSelectedPointFromTouch(touchX: Float, touchY: Float) {
        val drawAreaWidth = viewWidth - paddingLeft - paddingRight
        val drawAreaHeight = viewHeight - paddingTop - paddingBottom

        if (drawAreaWidth <= 0 || drawAreaHeight <= 0) return

        var logicX = (touchX - centerX) / (drawAreaWidth / 2f) * L_MAX
        var logicY = -(touchY - centerY) / (drawAreaHeight / 2f) * L_MAX

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