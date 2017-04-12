package de.troido.bledemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import de.troido.bledemo.epd.EpdCameraActivity
import de.troido.bledemo.util.checkPermission
import de.troido.bledemo.util.longToast
import de.troido.bledemo.util.start
import kotlinx.android.synthetic.main.activity_main.*

private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val required = PERMISSIONS.filter { !checkPermission(it) }
        if (required.isEmpty()) {
            startBle()
        } else {
            ActivityCompat.requestPermissions(this, required.toTypedArray(), 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            PERMISSIONS: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, PERMISSIONS, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startBle()
        } else {
            longToast(R.string.permission_fail)
            finishAndRemoveTask()
        }
    }

    private fun startBle() {
        btn_epd.setOnClickListener { start<EpdCameraActivity>() }
    }
}
