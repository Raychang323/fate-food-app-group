package com.fatefulsupper.app.ui.recommendation

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint // Added import for TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

class RouletteWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Colors based on HTML example
    private val colorGold = Color.parseColor("#FFD700")
    private val colorSegmentDark = Color.parseColor("#1a1a1a")
    private val colorSegmentLight = Color.parseColor("#2c2c2c")
    private val colorText = Color.WHITE
    private val colorPointer = Color.parseColor("#FF4500")
    private val colorTextShadow = Color.argb(153, 255, 215, 0) // Gold shadow: rgba(255, 215, 0, 0.6)

    private val segmentPaintDark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorSegmentDark
        style = Paint.Style.FILL
    }
    private val segmentPaintLight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorSegmentLight
        style = Paint.Style.FILL
    }
    private val wheelBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorGold
        style = Paint.Style.STROKE
        strokeWidth = 8f // Equivalent to CSS border width
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorGold
        style = Paint.Style.STROKE
        strokeWidth = 1.5f // Thin divider lines
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { // Changed Paint to TextPaint
        color = colorText
        textAlign = Paint.Align.CENTER
        textSize = 36f
        setShadowLayer(5f, 1f, 1f, colorTextShadow) // Subtle gold shadow
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorPointer
        style = Paint.Style.FILL
    }

    private var restaurantNames: List<String> = emptyList()
    private var currentRotationAngle = 0f
    private var sectorAngle = 0f
    private val wheelBounds = RectF()
    private var isSpinning = false

    var onResultListener: ((String) -> Unit)? = null
    var onItemHoverListener: ((String?) -> Unit)? = null // New listener for hover

    fun setRestaurantList(names: List<String>) {
        this.restaurantNames = names
        if (names.isNotEmpty()) {
            sectorAngle = 360f / names.size
        } else {
            sectorAngle = 0f
        }
        currentRotationAngle = 0f
        invalidate()
    }

    fun spinToTarget(targetIndex: Int) {
        if (isSpinning || restaurantNames.isEmpty() || targetIndex < 0 || targetIndex >= restaurantNames.size) {
            return
        }
        isSpinning = true

        // Target segment's middle should align with the top pointer (270 degrees on un-rotated canvas)
        val middleOfTargetSectorAngle = targetIndex * sectorAngle + sectorAngle / 2f
        var desiredStopAngle = 270f - middleOfTargetSectorAngle

        while (desiredStopAngle < currentRotationAngle) {
            desiredStopAngle += 360f
        }

        val targetAngleForAnimation = desiredStopAngle + (360f * Random.nextInt(4, 7)) // Spin a few more times

        val animator = ValueAnimator.ofFloat(currentRotationAngle, targetAngleForAnimation).apply {
            duration = 4000 + Random.nextInt(-500, 500).toLong()
            interpolator = DecelerateInterpolator(1.8f + Random.nextFloat() * 0.4f)
            addUpdateListener {
                currentRotationAngle = it.animatedValue as Float
                invalidate()

                // Logic to invoke onItemHoverListener
                if (restaurantNames.isNotEmpty() && sectorAngle > 0f) {
                    // Angle on the unrotated wheel that is currently under the top pointer (270 deg)
                    val angleUnderFixedPointer = (270f - (currentRotationAngle % 360f) + 360f) % 360f
                    
                    // Determine which segment this angle falls into.
                    // Segment i covers angles from i * sectorAngle to (i+1) * sectorAngle.
                    val hoveredIndex = (angleUnderFixedPointer / sectorAngle).toInt() % restaurantNames.size
                    
                    onItemHoverListener?.invoke(restaurantNames[hoveredIndex])
                } else {
                    onItemHoverListener?.invoke(null)
                }
            }
            doOnEnd {
                isSpinning = false
                currentRotationAngle = (currentRotationAngle % 360f + 360f) % 360f
                onResultListener?.invoke(restaurantNames[targetIndex]) // Return original full name
                // Also call hover listener with the final result
                if (targetIndex >= 0 && targetIndex < restaurantNames.size) {
                    onItemHoverListener?.invoke(restaurantNames[targetIndex])
                } else {
                    onItemHoverListener?.invoke(null)
                }
            }
        }
        animator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        val size = width.coerceAtMost(height)
        setMeasuredDimension(size, size)
        if (size > 0) {
            textPaint.textSize = (size / 38f).coerceAtMost(28f).coerceAtLeast(14f) // Further adjusted text size
            wheelBorderPaint.strokeWidth = (size / 40f).coerceAtLeast(6f).coerceAtMost(12f)
            dividerPaint.strokeWidth = (size / 200f).coerceAtLeast(1f)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (measuredWidth / 2f) * 0.92f // Expanded wheel radius

        if (restaurantNames.isEmpty() || sectorAngle == 0f) { // Added check for sectorAngle to prevent division by zero if list becomes empty after init
            canvas.drawText("輪盤是空的", centerX, centerY, textPaint.apply{ setShadowLayer(0f,0f,0f,0) })
            return
        }
        
        wheelBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        canvas.save()
        canvas.rotate(currentRotationAngle, centerX, centerY)
        
        // Calculate available width for text (once, as sectorAngle and radius are constant per draw for all segments)
        val textPlacementRadius = radius * 0.70f // Pushed text slightly outward
        val arcLengthForText = Math.toRadians(sectorAngle.toDouble()).toFloat() * textPlacementRadius
        val maxTextWidth = arcLengthForText * 0.95f // Maintained factor for maxTextWidth

        for (i in restaurantNames.indices) {
            val startAngle = i * sectorAngle
            val currentSegmentPaint = if (i % 2 == 0) segmentPaintLight else segmentPaintDark
            canvas.drawArc(wheelBounds, startAngle, sectorAngle, true, currentSegmentPaint)
            
            // Draw text
            canvas.save()
            val textAngle = startAngle + sectorAngle / 2f
            canvas.rotate(textAngle, centerX, centerY)
            
            val originalName = restaurantNames[i]
            val displayName = TextUtils.ellipsize(originalName, textPaint, maxTextWidth, TextUtils.TruncateAt.END)
            
            val textX = centerX + textPlacementRadius // Text X is at the textPlacementRadius
            val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f // Center text vertically
            
            canvas.drawText(displayName.toString(), textX, textY, textPaint)
            canvas.restore()
        }
        canvas.restore()

        // Draw wheel border on top of segments
        canvas.drawCircle(centerX, centerY, radius, wheelBorderPaint)

        // Draw top fixed pointer (pointing downwards)
        val pointerWidth = (measuredWidth / 18f).coerceAtLeast(25f)
        val pointerHeight = (measuredWidth / 16f).coerceAtLeast(35f)

        val pointerBaseY = centerY - radius - (wheelBorderPaint.strokeWidth / 2) - pointerHeight - 2f
        val pointerTipY = centerY - radius - (wheelBorderPaint.strokeWidth / 2) - 2f

        val pointerPath = Path().apply {
            moveTo(centerX - pointerWidth / 2, pointerBaseY)
            lineTo(centerX + pointerWidth / 2, pointerBaseY)
            lineTo(centerX, pointerTipY)
            close()
        }
        canvas.drawPath(pointerPath, pointerPaint)
    }
}

fun ValueAnimator.doOnEnd(action: () -> Unit) {
    addListener(object : android.animation.Animator.AnimatorListener {
        override fun onAnimationStart(animation: android.animation.Animator) {}
        override fun onAnimationEnd(animation: android.animation.Animator) { action.invoke() }
        override fun onAnimationCancel(animation: android.animation.Animator) {}
        override fun onAnimationRepeat(animation: android.animation.Animator) {}
    })
}
