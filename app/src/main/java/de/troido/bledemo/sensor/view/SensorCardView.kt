package de.troido.bledemo.sensor.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import de.troido.bledemo.R
import de.troido.bledemo.sensor.Sensor
import de.troido.bledemo.sensor.Sensor.*
import de.troido.bledemo.sensor.Vec3
import kotlinx.android.synthetic.main.view_sensor_card.view.*

@SuppressLint("InflateParams")
class SensorCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SquareCardView(context, attrs, defStyleAttr) {

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_sensor_card, null, false))
    }

    fun update(sensor: Sensor<*>, axis: Int = 0) {
        iv_icon.setImageResource(sensor.img)
        tv_sensor.text = sensor.name
        tv_value.text = when {
            sensor.value == null -> Companion.noValue
            else -> when (sensor) {
                is Temperature -> "%.2f°C".format(sensor.value)
                is Pressure -> "%.2fmb".format(sensor.value)
                is Light -> "%.2f%%".format(sensor.value)
                is Humidity -> "%.2f%%".format(sensor.value)
                is Compass -> getAxisString("Gauss", axis, sensor.value)
                is Gyroscope -> getAxisString("dps", axis, sensor.value)
                is Accelerometer -> getAxisString("g", axis, sensor.value)
                is Potentiometer -> ""
                is Controller -> ""
            }
        }
    }

    private fun getAxisString(units: String, axis: Int, value: Vec3<Float>?): String = when (axis) {
        0 -> {
            tv_sensor.text = "${tv_sensor.text} X"
            "%.2f$units".format(value?.x)
        }
        1 -> {
            tv_sensor.text = "${tv_sensor.text} Y"
            "%.2f$units".format(value?.y)
        }
        2 -> {
            tv_sensor.text = "${tv_sensor.text} Z"
            "%.2f$units".format(value?.z)
        }
        else -> "NA"
    }
}
