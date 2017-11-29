package de.troido.bledemo.sensor

import android.content.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import de.troido.bledemo.R
import de.troido.bledemo.sensor.Sensor
import de.troido.bledemo.sharedPreferences
import de.troido.bledemo.util.localBroadcastManager
import de.troido.bledemo.util.sequence
import de.troido.bledemo.util.startService
import de.troido.bledemo.util.stopService
import de.troido.measurementloader.Measurement
import de.troido.measurementloader.MeasurementUploaderService
import kotlinx.android.synthetic.main.activity_sensor.*

class SensorActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updateFromIntent(it)
                //TODO: Enable to configure the app to use rest or not. For now, just disable rest.
                //syncFromIntent(it)
            }
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

    private var previousMeasurement = Measurement()
    private var lastSync = System.currentTimeMillis()

    private fun syncFromIntent(intent: Intent) {
        val temperature: Sensor.Temperature? =
                intent.getSerializableExtra(SensorBleService.TEMPERATURE) as? Sensor.Temperature

        val controller: Sensor.Controller? =
                intent.getSerializableExtra(SensorBleService.CONTROLLER) as? Sensor.Controller

        var button1: Boolean? = null
        var button2: Boolean? = null

        controller?.let {
            button1 = controller.ix == 7
            button2 = controller.ix == 6
        }

        val illumination: Sensor.Light? =
                intent.getSerializableExtra(SensorBleService.LIGHT) as? Sensor.Light

        temperature?.value?.toDouble()?.let {
            previousMeasurement.temperature = it
        }
        button1?.let { previousMeasurement.isButton1 = it }
        button2?.let { previousMeasurement.isButton2 = it }

        illumination?.value?.toInt()?.let { previousMeasurement.illumination = it }

        val potentiometer: Sensor.Potentiometer? =
                intent.getSerializableExtra(SensorBleService.POTENTIOMETER) as? Sensor.Potentiometer

        potentiometer?.value?.toInt()?.let { previousMeasurement.potentiometer = it }

        val syncMeasure = Intent(this, MeasurementUploaderService::class.java)
        syncMeasure.putExtra(MeasurementUploaderService.EXTRA_TEMPERATURE, previousMeasurement.temperature)
        syncMeasure.putExtra(MeasurementUploaderService.EXTRA_BUTTON_1, previousMeasurement.isButton1)
        syncMeasure.putExtra(MeasurementUploaderService.EXTRA_BUTTON_2, previousMeasurement.isButton2)
        syncMeasure.putExtra(MeasurementUploaderService.EXTRA_ILLUMINATION, previousMeasurement.illumination)
        syncMeasure.putExtra(MeasurementUploaderService.EXTRA_POTENTIOMETER, previousMeasurement.potentiometer)

        if (System.currentTimeMillis() - lastSync > 3000) {
            Log.d("MEASUREMENT", previousMeasurement.toString())
            startService(syncMeasure)
            lastSync = System.currentTimeMillis()
        }
    }

    private fun updateFromIntent(intent: Intent) {
        arrayOf(SensorBleService.TEMPERATURE,
                SensorBleService.LIGHT,
                SensorBleService.COMPASS,
                SensorBleService.ACCELEROMETER,
                SensorBleService.GYROSCOPE)
                .map { key -> intent.getSerializableExtra(key) as? Sensor<*> }
                .forEach { updateSensorUi(it) }

        intent.getSerializableExtra(SensorBleService.CONTROLLER)
                ?.let { iv_controller.setImageResource((it as Sensor.Controller).img) }
    }

    private fun updateSensorUi(sensor: Sensor<*>?) {
        when (sensor) {
            is Sensor.Temperature -> sensor_temperature.update(sensor)
            is Sensor.Light -> sensor_light.update(sensor)
            is Sensor.Compass -> {
                sensor_magnetometer_x.update(sensor, 0)
                sensor_magnetometer_y.update(sensor, 1)
                sensor_magnetometer_z.update(sensor, 2)
            }
            is Sensor.Accelerometer -> {
                sensor_accelerometer_x.update(sensor, 0)
                sensor_accelerometer_y.update(sensor, 1)
                sensor_accelerometer_z.update(sensor, 2)
            }
            is Sensor.Gyroscope -> {
                sensor_gyroscope_x.update(sensor, 0)
                sensor_gyroscope_y.update(sensor, 1)
                sensor_gyroscope_z.update(sensor, 2)
            }
        }
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
        sensor_gyroscope_x.update(Sensor.Gyroscope(getVec(SensorBleService.GYROSCOPE)))
        sensor_gyroscope_y.update(Sensor.Gyroscope(getVec(SensorBleService.GYROSCOPE)))
        sensor_gyroscope_z.update(Sensor.Gyroscope(getVec(SensorBleService.GYROSCOPE)))
        sensor_magnetometer_x.update(Sensor.Compass(getVec(SensorBleService.COMPASS)))
        sensor_magnetometer_y.update(Sensor.Compass(getVec(SensorBleService.COMPASS)))
        sensor_magnetometer_z.update(Sensor.Compass(getVec(SensorBleService.COMPASS)))
        sensor_accelerometer_x.update(Sensor.Accelerometer(getVec(SensorBleService.ACCELEROMETER)))
        sensor_accelerometer_y.update(Sensor.Accelerometer(getVec(SensorBleService.ACCELEROMETER)))
        sensor_accelerometer_z.update(Sensor.Accelerometer(getVec(SensorBleService.ACCELEROMETER)))
    }
}
