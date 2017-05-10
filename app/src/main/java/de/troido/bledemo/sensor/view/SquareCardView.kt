package de.troido.bledemo.sensor.view

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet

open class SquareCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
}
