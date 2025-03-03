package com.navobytes.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.max
import kotlin.math.min

class btaview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        // 图片缩放类型
        const val SCALE_TYPE_CENTER_CROP = 0
        const val SCALE_TYPE_FIT_CENTER = 1

        // 动画插值器类型
        const val INTERPOLATOR_LINEAR = 0
        const val INTERPOLATOR_ACCELERATE = 1
        const val INTERPOLATOR_DECELERATE = 2
        const val INTERPOLATOR_ACCELERATE_DECELERATE = 3

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(SCALE_TYPE_CENTER_CROP, SCALE_TYPE_FIT_CENTER)
        annotation class ScaleType

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(
            INTERPOLATOR_LINEAR,
            INTERPOLATOR_ACCELERATE,
            INTERPOLATOR_DECELERATE,
            INTERPOLATOR_ACCELERATE_DECELERATE
        )
        annotation class InterpolatorType
    }

    // 图片资源
    private var beforeBitmap: Bitmap? = null
    private var afterBitmap: Bitmap? = null

    // 显示参数
    private var scaleType = SCALE_TYPE_CENTER_CROP
    private var roundRadius = 0f
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // 动画控制
    private var animator: ValueAnimator? = null
    private var progress = 0f
    private var animationDuration = 1500
    private var autoPlay = true
    private var loopAnimation = true
    private var loopInterval = 1000
    private var interpolatorType = INTERPOLATOR_ACCELERATE_DECELERATE

    // 绘制辅助对象
    private val clipPath = Path()
    private val rectF = RectF()
    private val afterMatrix = Matrix()
    private val tempRect = RectF()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        initAttributes(attrs)
        if (autoPlay) startAnimation()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        attrs ?: return

        context.obtainStyledAttributes(attrs, R.styleable.BTAView).apply {
            try {
                // 图片资源
                getResourceId(R.styleable.BTAView_beforeImage, -1).takeIf { it != -1 }?.let {
                    setBeforeImageResource(it)
                }
                getResourceId(R.styleable.BTAView_afterImage, -1).takeIf { it != -1 }?.let {
                    setAfterImageResource(it)
                }

                // 显示参数
                scaleType = getInt(R.styleable.BTAView_scaleType, SCALE_TYPE_CENTER_CROP)
                roundRadius = getDimension(R.styleable.BTAView_roundRadius, 0f)

                // 线条样式
                linePaint.color = getColor(R.styleable.BTAView_lineColor, Color.WHITE)
                linePaint.strokeWidth = getDimension(R.styleable.BTAView_lineWidth, 2f)

                // 动画参数
                autoPlay = getBoolean(R.styleable.BTAView_autoPlay, true)
                animationDuration = getInteger(R.styleable.BTAView_duration, 2000)
                loopAnimation = getBoolean(R.styleable.BTAView_loop, true)
                loopInterval = getInteger(R.styleable.BTAView_loopInterval, 0)
                interpolatorType = getInt(R.styleable.BTAView_interpolatorType, INTERPOLATOR_LINEAR)

            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        applyRoundCornerClip(canvas)
        drawStaticBeforeImage(canvas)
        drawClippedAfterImage(canvas)
        drawMovingLine(canvas)
    }

    private fun applyRoundCornerClip(canvas: Canvas) {
        if (roundRadius > 0) {
            clipPath.reset()
            rectF.set(0f, 0f, width.toFloat(), height.toFloat())
            clipPath.addRoundRect(rectF, roundRadius, roundRadius, Path.Direction.CW)
            canvas.clipPath(clipPath)
        }
    }

    private fun drawStaticBeforeImage(canvas: Canvas) {
        beforeBitmap?.let { bitmap ->
            calculateMatrix(bitmap, afterMatrix)
            canvas.drawBitmap(bitmap, afterMatrix, null)
        }
    }

    private fun drawClippedAfterImage(canvas: Canvas) {
        afterBitmap?.let { bitmap ->
            canvas.save()
            val clipWidth = width * progress
            canvas.clipRect(0f, 0f, clipWidth, height.toFloat())
            calculateMatrix(bitmap, afterMatrix)
            canvas.drawBitmap(bitmap, afterMatrix, null)
            canvas.restore()
        }
    }

    private fun drawMovingLine(canvas: Canvas) {
        val lineX = width * progress
        canvas.drawLine(lineX, 0f, lineX, height.toFloat(), linePaint)
    }

    private fun calculateMatrix(bitmap: Bitmap, matrix: Matrix) {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()

        matrix.reset()

        when (scaleType) {
            SCALE_TYPE_CENTER_CROP -> {
                val scale = max(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                matrix.postScale(scale, scale)
                tempRect.set(0f, 0f, bitmapWidth, bitmapHeight)
                matrix.mapRect(tempRect)
                matrix.postTranslate(
                    (viewWidth - tempRect.width()) / 2 - tempRect.left,
                    (viewHeight - tempRect.height()) / 2 - tempRect.top
                )
            }
            SCALE_TYPE_FIT_CENTER -> {
                val scale = min(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                matrix.postScale(scale, scale)
                matrix.postTranslate(
                    (viewWidth - bitmapWidth * scale) / 2,
                    (viewHeight - bitmapHeight * scale) / 2
                )
            }
        }
    }

    // 公共API
    fun setBeforeImageResource(@DrawableRes resId: Int) {
        beforeBitmap?.recycle()
        beforeBitmap = context.getDrawable(resId)?.toBitmap()
        invalidate()
    }

    fun setAfterImageResource(@DrawableRes resId: Int) {
        afterBitmap?.recycle()
        afterBitmap = context.getDrawable(resId)?.toBitmap()
        invalidate()
    }

    fun setScaleType(@ScaleType type: Int) {
        scaleType = type
        invalidate()
    }

    fun setRoundRadius(radius: Float) {
        roundRadius = radius
        invalidate()
    }

    // 动画控制
    fun startAnimation() {
        stopAnimation()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animationDuration.toLong()
            interpolator = getSelectedInterpolator()

            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (loopAnimation) {
                        postDelayed({ startAnimation() }, loopInterval.toLong())
                    }
                }
            })

            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    fun pauseAnimation() {
        animator?.pause()
    }

    fun resumeAnimation() {
        animator?.resume()
    }

    fun stopAnimation() {
        animator?.let {
            if (it.isRunning) it.cancel()
            animator = null
        }
    }

    fun setInterpolatorType(@InterpolatorType type: Int) {
        interpolatorType = type
        startAnimation()
    }

    private fun getSelectedInterpolator(): TimeInterpolator {
        return when (interpolatorType) {
            INTERPOLATOR_ACCELERATE -> AccelerateInterpolator()
            INTERPOLATOR_DECELERATE -> DecelerateInterpolator()
            INTERPOLATOR_ACCELERATE_DECELERATE -> AccelerateDecelerateInterpolator()
            else -> LinearInterpolator()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
        beforeBitmap?.recycle()
        afterBitmap?.recycle()
    }
}