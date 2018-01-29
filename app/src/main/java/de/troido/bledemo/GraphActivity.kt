package de.troido.bledemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import de.troido.bledemo.sensor.Sensor
import de.troido.bledemo.sensor.SensorBleService
import de.troido.bledemo.util.localBroadcastManager
import de.troido.bledemo.util.startService
import de.troido.bledemo.util.stopService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*

class IntentBroadcastReceiver : BroadcastReceiver() {
    val intentReceiver: PublishSubject<Intent> = PublishSubject.create()

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            intentReceiver.onNext(intent)
        }
    }
}

class GraphActivity : AppCompatActivity() {

    private val receiver = IntentBroadcastReceiver()
    private val values: Flowable<Intent> =
        receiver.intentReceiver.toFlowable(BackpressureStrategy.LATEST)

    private val temperatures: Flowable<Float> = values
        .map {
            (it.getSerializableExtra(SensorBleService.TEMPERATURE) as?
                    Sensor.Temperature)?.value?.toDouble() ?: Double.MAX_VALUE
        }
        .filter { it != Double.MAX_VALUE }
        .map { it.toFloat() }

    private val initialTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        startService<SensorBleService>()

        val chart: LineChart = findViewById(R.id.chart)

        val entries: MutableList<Entry> = ArrayList()
        entries.add(Entry(0f, 0f))

        val dataSet = LineDataSet(entries, getString(R.string.temperature_chart_title))

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
        val description = Description()
        description.text = getString(R.string.temperature_chart_description)
        chart.description = description

        temperatures.subscribeOn(Schedulers.io()).subscribe {
            dataSet.addEntry(Entry((System.currentTimeMillis() - initialTime).toFloat(), it))
            dataSet.notifyDataSetChanged()
            lineData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
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
}
