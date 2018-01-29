package de.troido.bledemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import de.troido.bledemo.sensor.Sensor
import de.troido.bledemo.sensor.SensorBleService
import de.troido.bledemo.sensor.Vec3
import de.troido.bledemo.util.localBroadcastManager
import de.troido.bledemo.util.startService
import de.troido.bledemo.util.stopService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

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

    private interface BleGraph<T> {
        val description: Description
        val dataPoints: Flowable<T>
        val lineData: LineData
        fun addEntry(timestamp: Float, value: T)
    }

    private class TemperatureGraph(
        title: String,
        description: Description,
        values: Flowable<Intent>
    ) : ValueGraph(title, description, values) {

        override fun extractValue(intent: Intent) =
            (intent.getSerializableExtra(SensorBleService.TEMPERATURE)
                    as? Sensor.Temperature)?.value
    }

    private class LightGraph(
        title: String,
        description: Description,
        values: Flowable<Intent>
    ) : ValueGraph(title, description, values) {

        override fun extractValue(intent: Intent) =
            (intent.getSerializableExtra(SensorBleService.LIGHT)
                    as? Sensor.Light)?.value
    }

    private class CompassGraph(
        title: String,
        description: Description,
        values: Flowable<Intent>
    ) : TripleGraph(title, description, values) {

        override fun extractValue(intent: Intent) =
            (intent.getSerializableExtra(SensorBleService.COMPASS)
                    as? Sensor.Compass)?.value
    }

    private class GyroscopeGraph(
        title: String,
        description: Description,
        values: Flowable<Intent>
    ) : TripleGraph(title, description, values) {

        override fun extractValue(intent: Intent) =
            (intent.getSerializableExtra(SensorBleService.GYROSCOPE)
                    as? Sensor.Gyroscope)?.value
    }

    private class AccelerometerGraph(
        title: String,
        description: Description,
        values: Flowable<Intent>
    ) : TripleGraph(title, description, values) {
        override fun extractValue(intent: Intent) =
            (intent.getSerializableExtra(SensorBleService.ACCELEROMETER)
                    as? Sensor.Accelerometer)?.value
    }

    private abstract class ValueGraph(
        title: String,
        override val description: Description,
        values: Flowable<Intent>
    ) : BleGraph<Float> {

        abstract fun extractValue(intent: Intent): Float?

        override val dataPoints: Flowable<Float> = values
            .map {
                extractValue(it) ?: Float.MAX_VALUE
            }
            .filter { it != Float.MAX_VALUE }

        private val entries: MutableList<Entry> = mutableListOf(Entry(0f, 0f))
        private val dataSet = LineDataSet(entries, title)
        override val lineData = LineData(dataSet)

        override fun addEntry(timestamp: Float, value: Float) {
            val entry = Entry(timestamp, value)
            dataSet.addEntry(entry)
            dataSet.notifyDataSetChanged()
            lineData.notifyDataChanged()
        }
    }

    private abstract class TripleGraph(
        title: String,
        override val description: Description,
        values: Flowable<Intent>
    ) : BleGraph<Triple<Float, Float, Float>> {

        abstract fun extractValue(intent: Intent): Vec3<Float>?

        override val dataPoints: Flowable<Triple<Float, Float, Float>> = values
            .map {
                val value: Vec3<Float>? = extractValue(it)

                if (value != null) {
                    Triple(value.x, value.y, value.x)

                } else {
                    Triple(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
                }
            }
            .filter {
                it.first != Float.MAX_VALUE
                        && it.second != Float.MAX_VALUE
                        && it.third != Float.MAX_VALUE
            }

        private val xEntries: MutableList<Entry> = mutableListOf(Entry(0f, 0f))
        private val yEntries: MutableList<Entry> = mutableListOf(Entry(0f, 0f))
        private val zEntries: MutableList<Entry> = mutableListOf(Entry(0f, 0f))

        private val xDataSet = LineDataSet(xEntries, "$title x")
        private val yDataSet = LineDataSet(yEntries, "$title y")
        private val zDataSet = LineDataSet(zEntries, "$title z")

        override val lineData = LineData(xDataSet, yDataSet, zDataSet)

        override fun addEntry(timestamp: Float, value: Triple<Float, Float, Float>) {
            val xEntry = Entry(timestamp, value.first)
            val yEntry = Entry(timestamp, value.second)
            val zEntry = Entry(timestamp, value.third)

            xDataSet.addEntry(xEntry)
            yDataSet.addEntry(yEntry)
            zDataSet.addEntry(zEntry)

            xDataSet.notifyDataSetChanged()
            yDataSet.notifyDataSetChanged()
            zDataSet.notifyDataSetChanged()

            lineData.notifyDataChanged()
        }
    }

    private val initialTime = System.currentTimeMillis()

    private lateinit var toolbar: Toolbar
    private lateinit var chart: LineChart
    private lateinit var graphTabs: TabLayout

    private lateinit var temperatureGraph: BleGraph<Float>
    private lateinit var lightGraph: BleGraph<Float>
    private lateinit var compassGraph: BleGraph<Triple<Float, Float, Float>>
    private lateinit var gyroscopeGraph: BleGraph<Triple<Float, Float, Float>>
    private lateinit var accelerometerGraph: BleGraph<Triple<Float, Float, Float>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        toolbar = findViewById(R.id.graphs_toolbar)
        chart = findViewById(R.id.chart)
        graphTabs = findViewById(R.id.graph_tabs)

        graphTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> displayGraph(temperatureGraph, getString(R.string.temperature_chart_title))
                    1 -> displayGraph(lightGraph, getString(R.string.light_chart_title))
                    2 -> displayGraph(compassGraph, getString(R.string.compass_chart_title))
                    3 -> displayGraph(gyroscopeGraph, getString(R.string.gyro_chart_title))
                    4 -> displayGraph(
                            accelerometerGraph,
                            getString(R.string.accelerometer_chart_title)
                    )
                }
            }
        })

        val temperatureGraphDescription = Description()
        temperatureGraphDescription.text = getString(R.string.temperature_chart_description)
        temperatureGraph = TemperatureGraph(
                getString(R.string.temperature_chart_title),
                temperatureGraphDescription,
                values
        )

        val lightGraphDescription = Description()
        lightGraphDescription.text = getString(R.string.light_chart_description)
        lightGraph = LightGraph(
                getString(R.string.light_chart_title),
                lightGraphDescription,
                values
        )

        val compassGraphDescription = Description()
        compassGraphDescription.text = getString(R.string.compass_chart_description)
        compassGraph = CompassGraph(
                getString(R.string.compass_chart_title),
                compassGraphDescription,
                values
        )

        val gyroscopeGraphDescription = Description()
        gyroscopeGraphDescription.text = getString(R.string.gyro_chart_description)
        gyroscopeGraph = GyroscopeGraph(
                getString(R.string.gyro_chart_title),
                gyroscopeGraphDescription,
                values
        )

        val accelerometerGraphDescription = Description()
        accelerometerGraphDescription.text = getString(R.string.accelerometer_chart_description)
        accelerometerGraph = AccelerometerGraph(
                getString(R.string.accelerometer_chart_title),
                accelerometerGraphDescription,
                values
        )


        startService<SensorBleService>()

        displayGraph(temperatureGraph, getString(R.string.temperature_chart_title))
    }

    private fun <T> displayGraph(graph: BleGraph<T>, title: String) {
        toolbar.title = title

        chart.description = graph.description
        chart.data = graph.lineData
        chart.invalidate()

        graph.dataPoints.subscribeOn(Schedulers.io()).subscribe {
            graph.addEntry((System.currentTimeMillis() - initialTime).toFloat(), it)
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
