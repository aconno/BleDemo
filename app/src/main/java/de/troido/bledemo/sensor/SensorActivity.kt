package de.troido.bledemo.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.troido.bledemo.R
import de.troido.bledemo.sensor.Sensor
import de.troido.bledemo.sharedPreferences
import de.troido.bledemo.util.localBroadcastManager
import de.troido.bledemo.util.sequence
import de.troido.bledemo.util.startService
import de.troido.bledemo.util.stopService
import kotlinx.android.synthetic.main.activity_sensor.*

class SensorActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let(this@SensorActivity::updateFromIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)
        startService<SensorBleService>()
    }

    override fun onResume() {
        super.onResume()
        updateFromPrefs()
        localBroadcastManager.registerReceiver(receiver, IntentFilter(SensorBleService.ACTION))
        startService<SensorBleService>()
    }

    override fun onPause() {
        super.onPause()
        localBroadcastManager.unregisterReceiver(receiver)
    }

    override fun onStop() {
        stopService<SensorBleService>()
        super.onStop()
    }

    private fun updateFromIntent(intent: Intent) {
        arrayOf(
                SensorBleService.TEMPERATURE to sensor_temperature,
                SensorBleService.LIGHT to sensor_light,
                SensorBleService.COMPASS to sensor_compass,
                SensorBleService.ACCELEROMETER to sensor_accelerometer,
                SensorBleService.GYROSCOPE to sensor_gyroscope
        ).forEach { (k, view) ->
            intent.getSerializableExtra(k)?.let { view.update(it as Sensor<*>) }
        }
        intent.getSerializableExtra(SensorBleService.CONTROLLER)
                ?.let { iv_controller.setImageResource((it as Sensor.Controller).img) }
    }

    private fun SharedPreferences.getStringFloat(k: String): Float? =
            getString(k, null)?.toFloatOrNull()

    private fun SharedPreferences.getVec(k: String): Vec3<Float>? =
            arrayOf("$k${SensorBleService.X_SUFFIX}", "$k${SensorBleService.Y_SUFFIX}",
                    "$k${SensorBleService.Z_SUFFIX}")
                    .map { getString(it, null) }
                    .map { it?.toFloatOrNull() }
                    .sequence()
                    ?.let { (x, y, z) -> Vec3(x, y, z) }

    private fun updateFromPrefs() = sharedPreferences.apply {
        Sensor.Controller.fromIx(getInt(SensorBleService.CONTROLLER, Sensor.Controller.None.ix))
                ?.let { iv_controller.setImageResource(it.img) }
        sensor_temperature.update(Sensor.Temperature(getStringFloat(SensorBleService.TEMPERATURE)))
        sensor_light.update(Sensor.Light(getStringFloat(SensorBleService.LIGHT)))
        sensor_gyroscope.update(Sensor.Gyroscope(getVec(SensorBleService.GYROSCOPE)))
        sensor_compass.update(Sensor.Compass(getVec(SensorBleService.COMPASS)))
        sensor_accelerometer.update(Sensor.Accelerometer(getVec(SensorBleService.ACCELEROMETER)))
    }
}
