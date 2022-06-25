package com.udacity.objects

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonBackgroundColor = 0
    private var textColor = 0
    private var buttonLoadingColor = 0
    private var circleColor = 0

    private var widthSize = 0
    private var heightSize = 0

    @Volatile
    private var progress = 0f

    private var buttonAnimator = ValueAnimator()
    private var circleAnimator = ValueAnimator()

    private var validSelection = false

    private val animatorSet = AnimatorSet().apply {

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                this@LoadingButton.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                this@LoadingButton.isEnabled = true

            }
        })

    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { property, old, new ->
        when (new) {
            ButtonState.Loading -> {
                animatorSet.playTogether(buttonAnimator, circleAnimator)
                animatorSet.start()
            }
            ButtonState.Clicked -> {
                buttonState = if (!validSelection) {
                    ButtonState.Completed
                } else ButtonState.Loading
            }
            else -> {
                animatorSet.cancel()
                progress = 0F

            }
        }

    }

    init {
        isClickable = true
        buttonState = ButtonState.Completed

        buttonAnimator = AnimatorInflater.loadAnimator(
            context,
            R.animator.download_button_animation
        ) as ValueAnimator

        circleAnimator = AnimatorInflater.loadAnimator(
            context,
            R.animator.download_circle_animation
        ) as ValueAnimator

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, 0)
            buttonLoadingColor = getColor(R.styleable.LoadingButton_buttonLoadingColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)

        }

    }

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.buttonStrokWidth)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.buttonTextSize)
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawButton(canvas)
        drawText(canvas)
        drawCircle(canvas)
    }

    private fun drawButton(canvas: Canvas?) {
        buttonAnimator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }

        paint.color = buttonBackgroundColor

        canvas?.drawRoundRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(),20.0f, 20.0f, paint)


        if (buttonState == ButtonState.Loading) {
            paint.color = buttonLoadingColor
            canvas?.drawRoundRect(
                0f,
                0f,
                ( widthSize * (progress / 360)),
                heightSize.toFloat(),20.0f, 20.0f,
                paint
            )
        }
    }

    private fun drawText(canvas: Canvas?) {
        // check the button state
        val buttonText = if (buttonState == ButtonState.Loading){
            resources.getString(R.string.button_loading)
        }
        else {
            resources.getString(R.string.button_name)
        }
        paint.color = textColor
        canvas?.drawText(
            buttonText,
            (widthSize / 2).toFloat(),
            ((heightSize + 30) / 2).toFloat(),
            paint
        )

    }

    private fun drawCircle(canvas: Canvas?) {
        circleAnimator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()

        }

        paint.color = circleColor
        canvas?.drawArc(
            widthSize - 250f,
            (heightSize / 2) - 50f,
            widthSize - 150f,
            (heightSize / 2) + 50f,
            180f, progress, true, paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun changeButtonState(state: ButtonState) {
        if (buttonState != state) {
            buttonState = state
            invalidate()
        }
    }

    fun isSelectionValid(state: Boolean) {
        validSelection = state
    }



}

