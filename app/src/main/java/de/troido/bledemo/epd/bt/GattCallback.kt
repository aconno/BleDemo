package de.troido.bledemo.epd.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log

class GattCallback(
        var packetWriterListener: PacketWriterListener?,
        private val bleConnStatusChangeListener: BleConnStatusChangeListener,
        private val bleCharacteristicListener: BleCharacteristicListener
): BluetoothGattCallback() {

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS && characteristic?.uuid.toString() == BluetoothImpl.UART_RX_CHR_UUID.toString()){
            packetWriterListener?.onPacketWriteFinished()
        }
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        Log.d("Bluetooth_status", "Old $status - New $newState")
        if(newState==BluetoothGatt.STATE_CONNECTED){
            gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            gatt?.discoverServices()
            return
        }

        if(newState==BluetoothGatt.STATE_DISCONNECTED){
            bleConnStatusChangeListener.onBleDisconnected()
            return
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        gatt?.getService(BluetoothImpl.UART_SVC_UUID)?.let { bluetoothGattService ->
            bluetoothGattService.getCharacteristic(BluetoothImpl.UART_RX_CHR_UUID)?.let {
                it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                bleCharacteristicListener.onTxCharacteristicFound(it)
                bleConnStatusChangeListener.onBleConnected()
            }
        }
    }



}