package m.example.fibrorecoverytracker

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView


class LabelledSeekBar<E : Metric<E>> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private lateinit var type: Class<E>

    private var seekbar: SeekBar
    private var linearLayout: LinearLayout

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.labelled_seek_bar, this)

        orientation = VERTICAL

        seekbar = findViewById(R.id.seekBar)
        linearLayout = findViewById(R.id.labels)
    }

    fun setMetric(type: Class<E>) {
        this.type = type

        val options = type.enumConstants
        val count = options.count()

        seekbar.min = 0
        seekbar.max = count - 1

        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                    val availableWidth = linearLayout.width
                    val textViewWidth = availableWidth / (count - 1)
                    for ((index, option) in options.withIndex()) {
                        val textView = TextView(context)
                        textView.textSize = 12F
                        textView.text = option.toString()
                        val width =
                            if (index == 0 || index == count - 1) textViewWidth / 2 else textViewWidth
                        textView.layoutParams = LayoutParams(width, LayoutParams.WRAP_CONTENT)
                        textView.textAlignment =
                            when (index) {
                                0 -> TEXT_ALIGNMENT_VIEW_START
                                count - 1 -> TEXT_ALIGNMENT_VIEW_END
                                else -> TEXT_ALIGNMENT_CENTER
                            }
                        linearLayout.addView(textView)
                    }
                }
            })
        }
    }

    fun setProgress(score: Int) {
        var idx = 0
        for (value in type.enumConstants) {
            if (value.score() == score) {
                break
            }
            idx++
        }
        seekbar.progress = idx
    }

    fun score() = type.enumConstants[seekbar.progress].score()
    fun getAngleDrawable(
        solidColor: Int,
        _radius: FloatArray?,
        strokeWidth: Int,
        strokeColor: Int
    ): GradientDrawable? {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(solidColor)
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadii = _radius
        gradientDrawable.setStroke(strokeWidth, strokeColor)
        return gradientDrawable
    }
}