package com.udacity.objects

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var buttonBackgroundColor = 0
    private var textColor = 0 // default color
    private var buttonLoadingColor = 0
    private var circleColor = 0

    private var widthSize = 0
    private var heightSize = 0

    // tells the compiler that the value of a variable
    // must never be cached as its value may change outside
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
                buttonState = ButtonState.Completed

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
                if (!validSelection) {
                    buttonState = ButtonState.Completed
                } else buttonState = ButtonState.Loading
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
            // properties for downloading progress is defined
            R.animator.download_button_animation
        ) as ValueAnimator

        circleAnimator = AnimatorInflater.loadAnimator(
            context,
            // properties for downloading progress is defined
            R.animator.download_circle_animation
        ) as ValueAnimator

        // initialize custom attributes of the button
        val attr = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        )
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_buttonBackgroundColor, 0)
            buttonLoadingColor = getColor(R.styleable.LoadingButton_buttonLoadingColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)

        }

    }

    // set attributes of paint
    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.buttonStrokWidth)
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER // button text alignment
        textSize = resources.getDimension(R.dimen.buttonTextSize) //button text size
        typeface = Typeface.create("", Typeface.BOLD) // button text's font style
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
        canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)


        if (buttonState == ButtonState.Loading) {
            paint.color = buttonLoadingColor
            canvas?.drawRect(
                0f,
                0f,
                (measuredWidth * (progress / 100)),
                measuredHeight.toFloat(),
                paint
            )
        }

    }

    private fun drawText(canvas: Canvas?) {
        // check the button state
        val buttonText = if (buttonState == ButtonState.Loading)
            resources.getString(R.string.button_loading)  // We are loading as button text
        else resources.getString(R.string.button_name)// download as button text
        // write the text on custom button
        paint.color = textColor
        canvas?.drawText(
            buttonText,
            (measuredWidth / 2).toFloat(),
            ((measuredHeight + 30) / 2).toFloat(),
            paint
        )

    }

    private fun drawCircle(canvas: Canvas?) {
        circleAnimator.addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()

        }

        paint.color = circleColor
        canvas?.drawArc(//left, top, right, bottom, start angle, sweep angle, use center, Paint
            measuredWidth - 250f,
            (measuredHeight / 2) - 50f,
            measuredWidth - 150f,
            (measuredHeight / 2) + 50f,
            180f, progress * 3.6.toFloat(), true, paint
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

    fun isSelectionValid(state: Boolean) {
//        Log.i("LoadingButton.changeButtonState", "Change button state to: $state from $buttonState")
        validSelection = state

    }

}

