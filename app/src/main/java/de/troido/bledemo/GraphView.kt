package de.troido.bledemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val startX = 0f
        val startY = 0f
        val stopX = 100f
        val stopY = 100f
        val paint = Paint()
        canvas?.drawLine(startX, startY, stopX, stopY, paint)


    }
}
