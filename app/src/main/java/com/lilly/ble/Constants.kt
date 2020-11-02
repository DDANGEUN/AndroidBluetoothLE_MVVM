package com.lilly.ble

import android.Manifest

class Constants{
    companion object{
        // used to identify adding bluetooth names
        const val REQUEST_ENABLE_BT = 1
        // used to request fine location permission
        const val REQUEST_ALL_PERMISSION = 2
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        //사용자 BLE UUID Service/Rx/Tx
        const val SERVICE_STRING = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_COMMAND_STRING = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
        const val CHARACTERISTIC_RESPONSE_STRING = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

        //BluetoothGattDescriptor 고정
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }
}