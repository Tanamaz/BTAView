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
    // 新增属性
    private var scalePivotX = 0.5f // 默认水平锚点
    private var scalePivotY = 0.5f // 默认垂直锚点

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
    private var currentProgress = 0f    // 统一进度 0~1
    private var isForwardDirection = true
    private var forwardDuration = 1500
    private var backwardDuration = 1000
    private var loopInterval = 1500
    private var maxScaleFactor = 1.0f
    private var currentScale = 1.0f
    private var autoPlay = true
    private var loopAnimation = true
    private var interpolatorType = INTERPOLATOR_ACCELERATE_DECELERATE

    // 绘制辅助对象
    private val clipPath = Path()
    private val rectF = RectF()
    private val tempRect = RectF()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        initAttributes(attrs)
        if (autoPlay) startAnimation()
    }

    private fun initAttributes(attrs: AttributeSet?) {
        attrs ?: return

        context.obtainStyledAttributes(attrs, R.styleable.btaview).apply {
            try {
                // 图片资源
                getResourceId(R.styleable.btaview_beforeImage, -1).takeIf { it != -1 }?.let {
                    setBeforeImageResource(it)
                }
                getResourceId(R.styleable.btaview_afterImage, -1).takeIf { it != -1 }?.let {
                    setAfterImageResource(it)
                }

                // 显示参数
                scaleType = getInt(R.styleable.btaview_scaleType, SCALE_TYPE_CENTER_CROP)
                roundRadius = getDimension(R.styleable.btaview_roundRadius, 0f)

                // 线条样式
                linePaint.color = getColor(R.styleable.btaview_lineColor, Color.WHITE)
                linePaint.strokeWidth = getDimension(R.styleable.btaview_lineWidth, 1f)

                // 动画参数
                autoPlay = getBoolean(R.styleable.btaview_autoPlay, true)
                forwardDuration = getInteger(R.styleable.btaview_forwardDuration, 1500)
                backwardDuration = getInteger(R.styleable.btaview_backwardDuration, 1000)
                maxScaleFactor = getFloat(R.styleable.btaview_maxScale, 1.0f)
                loopAnimation = getBoolean(R.styleable.btaview_loop, true)
                loopInterval = getInteger(R.styleable.btaview_loopInterval, 1500)
                interpolatorType = getInt(R.styleable.btaview_interpolatorType, INTERPOLATOR_ACCELERATE_DECELERATE)

            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        applyRoundCornerClip(canvas)
        drawScaledImages(canvas)
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

    private fun drawScaledImages(canvas: Canvas) {
        // 根据方向计算缩放比例
        currentScale = if (isForwardDirection) {
            1.0f + (maxScaleFactor - 1.0f) * currentProgress // 放大：1.0 → 1.2
        } else {
            maxScaleFactor - (maxScaleFactor - 1.0f) * currentProgress // 缩小：1.2 → 1.0
        }

        // 绘制底层图片（Before）
        beforeBitmap?.let { bitmap ->
            val matrix = createScaledMatrix(bitmap, currentScale)
            canvas.drawBitmap(bitmap, matrix, null)
        }

        // 绘制上层裁剪区域（After）
        afterBitmap?.let { bitmap ->
            canvas.save()
            val clipWidth = if (isForwardDirection) {
                width * currentProgress // 从左到右裁剪
            } else {
                width * (1 - currentProgress) // 从右到左裁剪
            }
            canvas.clipRect(0f, 0f, clipWidth, height.toFloat())
            val matrix = createScaledMatrix(bitmap, currentScale)
            canvas.drawBitmap(bitmap, matrix, null)
            canvas.restore()
        }
    }

    private fun drawMovingLine(canvas: Canvas) {
        val epsilon = 0.001f // 边界判定阈值
        val lineX = if (isForwardDirection) {
            width * currentProgress
        } else {
            width * (1 - currentProgress)
        }

        // 边界检测（左右各1像素范围内不绘制）
        if (lineX < epsilon || lineX > width - epsilon) return

        canvas.drawLine(lineX, 0f, lineX, height.toFloat(), linePaint)
    }

    private fun createScaledMatrix(bitmap: Bitmap, scale: Float): Matrix {
        val matrix = Matrix()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()

        // 计算锚点像素位置
        val pivotX = bitmapWidth * scalePivotX
        val pivotY = bitmapHeight * scalePivotY

        when (scaleType) {
            SCALE_TYPE_CENTER_CROP -> {
                val baseScale = max(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                matrix.postScale(baseScale * scale, baseScale * scale, pivotX, pivotY)

                // 计算锚点偏移
                tempRect.set(0f, 0f, bitmapWidth, bitmapHeight)
                matrix.mapRect(tempRect)
                val offsetX = (viewWidth * scalePivotX) - (tempRect.left + pivotX * baseScale * scale)
                val offsetY = (viewHeight * scalePivotY) - (tempRect.top + pivotY * baseScale * scale)
                matrix.postTranslate(offsetX, offsetY)
            }
            SCALE_TYPE_FIT_CENTER -> {
                val baseScale = min(viewWidth / bitmapWidth, viewHeight / bitmapHeight)
                matrix.postScale(baseScale * scale, baseScale * scale, pivotX, pivotY)

                // 保持锚点位置
                tempRect.set(0f, 0f, bitmapWidth, bitmapHeight)
                matrix.mapRect(tempRect)
                val targetX = viewWidth * scalePivotX
                val targetY = viewHeight * scalePivotY
                matrix.postTranslate(
                    targetX - (tempRect.left + pivotX * baseScale * scale),
                    targetY - (tempRect.top + pivotY * baseScale * scale)
                )
            }
        }
        return matrix
    }

    // 新增公共方法
    fun setScalePivot(pivotX: Float, pivotY: Float) {
        scalePivotX = pivotX.coerceIn(0f, 1f)
        scalePivotY = pivotY.coerceIn(0f, 1f)
        invalidate()
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
        isForwardDirection = true
        startPhase()
    }

    private fun startPhase() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = if (isForwardDirection) forwardDuration.toLong() else backwardDuration.toLong()
            interpolator = getSelectedInterpolator()
            addUpdateListener {
                currentProgress = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (loopAnimation) {
                        if (isForwardDirection) {
                            // 左到右完成，开始右到左
                            isForwardDirection = false
                            startPhase()
                        } else {
                            // 右到左完成，等待间隔后开始新循环
                            postDelayed({
                                isForwardDirection = true
                                startPhase()
                            }, loopInterval.toLong())
                        }
                    }
                }
            })
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