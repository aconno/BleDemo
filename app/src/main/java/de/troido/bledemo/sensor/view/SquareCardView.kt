package de.troido.bledemo.sensor.view

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

open class SquareCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        return super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
