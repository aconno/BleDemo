package de.troido.bledemo.sensor

import android.bluetooth.le.ScanSettings
import android.content.Intent
import de.troido.bleacon.ble.BleActor
import de.troido.bleacon.config.BleFilter
import de.troido.bleacon.config.BleScanSettings
import de.troido.bleacon.scanner.BeaconScanner
import de.troido.bleacon.service.BleService
import de.troido.bleacon.util.Uuid16
import de.troido.bledemo.sensor.Sensor.*
import de.troido.bledemo.sharedPreferences
import de.troido.bledemo.util.localBroadcastManager

private val BEACON_UUID = Uuid16.fromString("17CF")

class SensorBleService : BleService<List<Sensor<*>>>(restartOnRemove = false) {
    companion object {
        val TEMPERATURE: String = Temperature::class.java.simpleName
        val HUMIDITY: String = Humidity::class.java.simpleName
        val PRESSURE: String = Pressure::class.java.simpleName
        val COMPASS: String = Compass::class.java.simpleName
        val ACCELEROMETER: String = Accelerometer::class.java.simpleName
        val GYROSCOPE: String = Gyroscope::class.java.simpleName
        val LIGHT: String = Light::class.java.simpleName
        val CONTROLLER: String = Controller::class.java.simpleName

        const val X_SUFFIX = "_x"
        const val Y_SUFFIX = "_y"
        const val Z_SUFFIX = "_z"

        const val ACTION = "sensors"
    }

    private fun persistIx(ix: Int, obj: Controller) {
        sharedPreferences.edit().putInt(CONTROLLER, ix).apply()
        localBroadcastManager.sendBroadcast(Intent(ACTION).apply { putExtra(CONTROLLER, obj) })
    }

    private fun persistFloat(v: Float?, obj: Sensor<*>) = v?.let {
        val k = obj::class.java.simpleName

        sharedPreferences.edit().putString(k, it.toString()).apply()

        localBroadcastManager.sendBroadcast(Intent(ACTION).apply { putExtra(k, obj) })
    }

    private fun persistVec(v: Vec3<Float>?, obj: Sensor<*>) = v?.let {
        val k = obj::class.java.simpleName

        val prefs = sharedPreferences.edit()

        arrayOf("$k$X_SUFFIX" to it.x, "$k$Y_SUFFIX" to it.y, "$k$Z_SUFFIX" to it.z)
                .forEach { (k, v) -> prefs.putString(k, v.toString()) }

        prefs.apply()

        localBroadcastManager.sendBroadcast(Intent(ACTION).apply { putExtra(k, obj) })
    }

    private val scanner = BeaconScanner(
            Deserializer,
            BleFilter { uuid16 = BEACON_UUID },
            BleScanSettings { scanMode = ScanSettings.SCAN_MODE_LOW_LATENCY }
    ) { _, _, data ->
        data.forEach {
            if (it.value != null) {
                when {
                    it.value is Float   -> persistFloat(it.value, it)
                    it is Compass       -> persistVec(it.value, it)
                    it is Accelerometer -> persistVec(it.value, it)
                    it is Gyroscope     -> persistVec(it.value, it)
                    it is Controller    -> persistIx(it.ix, it)
                }
            }
        }
    }

    override val actors: List<BleActor> = listOf(scanner)
}
