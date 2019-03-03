package de.troido.bledemo.epd.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import de.troido.bleacon.util.Uuid16
import java.util.*

class BluetoothImpl(private val context: Context, private val bluetoothImplListener: BluetoothImplListener): ScanResultFilteredDevice, BleConnStatusChangeListener, BleCharacteristicListener {


    private val bleScanCallback = ScanCallback(this)
    private val bleGattCallback = GattCallback(null,this,this)
    private var gatt: BluetoothGatt? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    override fun onDeviceFound(device: BluetoothDevice) {
        Log.d("BluetoothImplementation", "Device found")
        stopScan()
        gatt = device.connectGatt(context,false,bleGattCallback)
    }

    fun writeLongMessage(message: ByteArray){
        gatt?.let { tempGatt ->
            txCharacteristic?.let { txCharacteristic ->
                val totalPacketsCount = Math.ceil(message.size.toDouble() / MAX_PACKET_SIZE).toInt()
                var packetsSentCount = 0
                val packetsToSend = getPacketsArray(totalPacketsCount,message)
                val startTime = System.currentTimeMillis()

                Log.d("Writing", "STARTED WRITING")
                bleGattCallback.packetWriterListener = object : PacketWriterListener{
                    override fun onPacketWriteFinished() {
                        packetsSentCount++
                        if(totalPacketsCount <= packetsSentCount){
                            val endTime = System.currentTimeMillis()
                            Log.d("Total Time", "${(endTime - startTime).toDouble()/1000}s")
                            return
                        }
                        writePacket(tempGatt,txCharacteristic,packetsToSend[packetsSentCount])
                    }
                }

                if(totalPacketsCount < packetsSentCount)return
                writePacket(tempGatt,txCharacteristic,packetsToSend[0])
            } ?: kotlin.run { bluetoothImplListener.onMessageWriteFailed() }
        } ?: kotlin.run { bluetoothImplListener.onMessageWriteFailed() }
    }

    private fun writePacket(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, message: ByteArray){
        txCharacteristic?.value = message
        gatt.writeCharacteristic(characteristic)
    }

    private fun getPacketsArray(totalPackets: Int, message: ByteArray): List<ByteArray> {
        val tempPackets = arrayOfNulls<ByteArray>(totalPackets)
        for (i in 0 until totalPackets){
            tempPackets[i] = Arrays.copyOfRange(
                    message,
                    i * MAX_PACKET_SIZE,
                    (i + 1) * MAX_PACKET_SIZE
            )
        }
        return tempPackets.map { it!! }
    }

    fun startScan(){
        if(!bluetoothAdapter.isEnabled){
            bluetoothImplListener.onAdapterOff()
            return
        }
        bluetoothAdapter.bluetoothLeScanner.startScan(defaultFilter(),defaultSettings(),bleScanCallback)
    }

    fun stopScan(){
        if(!bluetoothAdapter.isEnabled){
            bluetoothImplListener.onAdapterOff()
            return
        }
        bluetoothAdapter.bluetoothLeScanner.stopScan(bleScanCallback)
    }

    fun turnOnBluetooth(){
        bluetoothAdapter.enable()
    }

    fun closeGatt(){
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        txCharacteristic = null
    }

    override fun onBleConnectionStatusChange() {

    }

    override fun onBleConnected() {
        bluetoothImplListener.onConnected()
    }

    override fun onBleDisconnected() {
        closeGatt()
        bluetoothImplListener.onConnectionLost()
    }

    override fun onTxCharacteristicFound(gattCharacteristic: BluetoothGattCharacteristic) {
        txCharacteristic = gattCharacteristic
    }

    private fun defaultFilter() = mutableListOf<ScanFilter>(
            ScanFilter.Builder().setDeviceName(DEVICE_NAME).build()
    )

    private fun defaultSettings() = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

    companion object {
        const val DEVICE_NAME = "EPD"
        private const val MAX_PACKET_SIZE = 20

        val UART_RX_CHR_UUID = Uuid16.fromString("A001").toUuid()
        val UART_SVC_UUID = Uuid16.fromString("A000").toUuid()
    }

}