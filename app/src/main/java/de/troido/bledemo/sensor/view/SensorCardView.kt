package de.troido.bledemo.sensor.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import de.troido.bledemo.R
import de.troido.bledemo.sensor.Sensor
import de.troido.bledemo.sensor.Sensor.*
import kotlinx.android.synthetic.main.view_sensor_card.view.*

@SuppressLint("InflateParams")
class SensorCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SquareCardView(context, attrs, defStyleAttr) {

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_sensor_card, null, false))
    }

    fun update(sensor: Sensor<*>) {
        iv_icon.setImageResource(sensor.img)
        tv_sensor.text = sensor.name
        tv_value.text = when {
            sensor.value == null -> Companion.noValue
            else                 -> when (sensor) {
                is Temperature   -> "%.2f°C".format(sensor.value)
                is Pressure      -> "%.2fmb".format(sensor.value)
                is Light         -> "%.2f%%".format(sensor.value)
                is Humidity      -> "%.2f%%".format(sensor.value)
                is Compass       -> "%.2fGauss".format(sensor.value?.x)
                is Gyroscope     -> "%.2fdps".format(sensor.value?.x)
                is Accelerometer -> "%.2fg".format(sensor.value?.x)
                is Controller    -> ""
            }
        }
    }
}
