package de.troido.bledemo.sensor

import android.support.annotation.DrawableRes
import android.util.Log
import de.troido.bleacon.data.*
import de.troido.bledemo.R
import java.io.Serializable
import java.util.*

sealed class Sensor<out V>(val value: V?, @DrawableRes val img: Int, val name: String)
    : Serializable {

    companion object {
        const val noValue: String = "N/A"
    }

    object Deserializer : BleDeserializer<List<Sensor<*>>> {
        override val length = -1

        override fun deserialize(data: ByteArray): List<Sensor<*>>? {
            Log.e("Test", data.contentToString())
//            return null
            return deserializers[data[3]]?.let {
                Log.e("Test", "test")
                it.deserialize(data.copyOfRange(4, data.size))
            }
        }
    }

    class Accelerometer(v: Vec3<Float>?) : Sensor<Vec3<Float>>(
            v?.map { if (it > -DEAD_ZONE && it < DEAD_ZONE) 0.0f else it },
            R.drawable.ic_acc,
            "Accelerometer"
    ) {
        companion object {
            private const val DEAD_ZONE = 0.1f

            private fun convert(x: Short): Float = x * 2.0f / 32768.0f

            fun fromShorts(x: Short, y: Short, z: Short): Accelerometer =
                    Accelerometer(Vec3(convert(x), convert(y), convert(z)))
        }
    }

    class Gyroscope(v: Vec3<Float>?) : Sensor<Vec3<Float>>(
            v?.map { if (it > -DEAD_ZONE && it < DEAD_ZONE) 0.0f else it },
            R.drawable.ic_gyro,
            "Gyroscope"
    ) {
        companion object {
            private const val DEAD_ZONE = 2.0f

            private fun convert(x: Short): Float = x * 245.0f / 32768.0f

            fun fromShorts(x: Short, y: Short, z: Short): Gyroscope =
                    Gyroscope(Vec3(convert(x), convert(y), convert(z)))
        }
    }

    class Compass(v: Vec3<Float>?) : Sensor<Vec3<Float>>(v, R.drawable.ic_compass, "Compass") {
        companion object {
            private fun convert(x: Short): Float = x * 0.00014f

            fun fromShorts(x: Short, y: Short, z: Short): Compass =
                    Compass(Vec3(convert(x), convert(y), convert(z)))
        }
    }

    class Temperature(v: Float?) : Sensor<Float>(v, R.drawable.ic_temperature, "Temperature")

    class Humidity(v: Float?) : Sensor<Float>(v, R.drawable.ic_humidity, "Humidity") {
        companion object {
            private const val HUMIDITY_MOCK_MIN = 55.0f
            private const val HUMIDITY_MOCK_MAX = 56.0f
            private val rng = Random()

            fun mockHumidity(): Humidity = Humidity(
                    HUMIDITY_MOCK_MIN + (HUMIDITY_MOCK_MAX - HUMIDITY_MOCK_MIN) * rng.nextFloat()
            )
        }
    }

    class Pressure(v: Float?) : Sensor<Float>(v, R.drawable.ic_pressure, "Pressure")

    class Light(v: Float?) : Sensor<Float>(v, R.drawable.ic_light, "Light")

    class Potentiometer(v: Float?) : Sensor<Float>(v, -1, "Potentiometer") {
        object Deserializer : BleDeserializer<Potentiometer> {
            override val length: Int = 1

            override fun deserialize(data: ByteArray): Potentiometer? {
                Log.d("POTENTIOMETER", "${data.last().toFloat()}")
                return Potentiometer(data.last().toFloat())
            }
        }
    }

    sealed class Controller(val ix: Int, @DrawableRes img: Int) :
            Sensor<Unit>(Unit, img, "Controller") {

        companion object {
            private val controllers = arrayOf(JoystickUp, JoystickDown, JoystickLeft,
                    JoystickRight, LeftButton, RightButton)

            fun fromIx(ix: Int): Controller? = controllers.firstOrNull { it.ix == ix }
        }

        object JoystickUp : Controller(0, R.drawable.board_joystick_up)
        object JoystickDown : Controller(1, R.drawable.board_joystick_down)
        object JoystickLeft : Controller(2, R.drawable.board_joystick_left)
        object JoystickRight : Controller(3, R.drawable.board_joystick_right)
        object None : Controller(4, R.drawable.board)
        object RightButton : Controller(6, R.drawable.board_right_button)
        object LeftButton : Controller(7, R.drawable.board_left_button)

        object Deserializer : BleDeserializer<Controller> {

            override val length = 1
            override fun deserialize(data: ByteArray): Controller? {
                Log.d("BluetoothBits", Integer.toBinaryString(data[0].toInt() and 0xFF))
                return controllers.firstOrNull { data[0].toInt() and (1 shl 7 shr it.ix) != 0 }?.also {
                    Log.e("Active", it.javaClass.name)
                } ?: None
            }
        }
    }
}

private val deserializer1 =
        VecDeserializer(9, Int16Deserializer).mapping {
                listOf(
                        Sensor.Gyroscope.fromShorts(it[0], it[1], it[2]),
                        Sensor.Accelerometer.fromShorts(it[3], it[4], it[5]),
                        Sensor.Compass.fromShorts(it[6], it[7], it[8])
                )
        }

private val deserializer2 = getDeserializer2()
//        VecDeserializer(2, Primitive.Float32.Deserializer)
//                .mapping { list ->
//                    val (t, l) = list.map(Primitive.Float32::data)
//                    listOf(Sensor.Temperature(t), Sensor.Light(l * 100))
//                }
//                .then(Sensor.Controller.Deserializer) { vec, controller -> vec + controller }

private fun getDeserializer2(): BleDeserializer<List<Sensor<Any>>> {
    val listSize = 2
    val vecDeserializer: VecDeserializer<Float> =
            VecDeserializer(listSize, Float32Deserializer)

    val sensorDeserializer: BleDeserializer<List<Sensor<Float>>> = vecDeserializer.mapping { list ->
       listOf(Sensor.Temperature(list[0]), Sensor.Light(list[1] * 100))
    }

    val combinedDeserializer: BleDeserializer<List<Sensor<Any>>> =
            sensorDeserializer.then(Sensor.Controller.Deserializer) { vec, controller -> vec + controller }

    return combinedDeserializer.then(Sensor.Potentiometer.Deserializer) { vec, potentiometer -> vec + potentiometer }
}

private val deserializers = mapOf(
        0x00.toByte() to deserializer1,
        0x01.toByte() to deserializer2
)
