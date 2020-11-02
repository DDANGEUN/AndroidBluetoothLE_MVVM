package com.lilly.ble.viewmodel

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lilly.ble.Constants
import com.lilly.ble.Constants.Companion.CLIENT_CHARACTERISTIC_CONFIG
import com.lilly.ble.MyApplication
import com.lilly.ble.Repository
import com.lilly.ble.util.BluetoothUtils
import com.lilly.ble.util.BluetoothUtils.Companion.findResponseCharacteristic
import com.lilly.ble.util.Event
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule


class MainViewModel(application: Application) : AndroidViewModel(application) {
    var btnScanTxt: ObservableField<String> = ObservableField("Start Scan")
    var statusTxt: ObservableField<String> = ObservableField("BLE SCAN 버튼을 눌러 스캔을 시작합니다.")
    var txtRead: ObservableField<String> = ObservableField("")
    var _txtRead: String = ""
    private val repository: Repository = MyApplication.getRepository()
    private val _application = application
    // Tag name for Log message
    private val TAG = "Central"

    // scan results
    var scanResults: ArrayList<BluetoothDevice>? = ArrayList()

    //ble adapter
    private var bleAdapter: BluetoothAdapter? = repository.bleAdapter

    private val _requestEnableBLE = MutableLiveData<Event<Boolean>>()
    val requestEnableBLE : LiveData<Event<Boolean>>
        get() = _requestEnableBLE
    private val _listUpdate = MutableLiveData<Event<Boolean>>()
    val listUpdate : LiveData<Event<Boolean>>
        get() = _listUpdate
    private val _scrollDown = MutableLiveData<Event<Boolean>>()
    val scrollDown : LiveData<Event<Boolean>>
        get() = _scrollDown

    var isScanning:ObservableBoolean = ObservableBoolean(false)
    var isConnect:ObservableBoolean = ObservableBoolean(false)
    // BLE Gatt
    private var bleGatt: BluetoothGatt? = null

    fun setBLEAdapter() {
        repository.setBLEAdapter()
        bleAdapter = repository.bleAdapter
    }

    /**
     *  Start BLE Scan
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickScan(){
        // check ble adapter and ble enabled
        if(!isScanning.get()) {
            if (bleAdapter == null || !bleAdapter?.isEnabled!!) {
                _requestEnableBLE.value = Event(true)
                statusTxt.set("Scanning Failed: ble not enabled")
                return
            }
            //scan filter
            val filters: MutableList<ScanFilter> = ArrayList()
            val scanFilter: ScanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(Constants.SERVICE_STRING)))
                .build()
            filters.add(scanFilter)
            // scan settings
            // set low power scan mode
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
            // start scan
            //bleAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)
            bleAdapter?.bluetoothLeScanner?.startScan(BLEScanCallback)
            btnScanTxt.set("Scanning..")

            isScanning.set(true)
            Timer("SettingUp", false).schedule(3000) { stopScan() }
        }
    }
    fun onClickDisconnect(){
        disconnectGattServer()
        isConnect.set(false)
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopScan(){
        bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        isScanning.set(false)
        btnScanTxt.set("Start Scan")
        scanResults = ArrayList() //list 초기화
        Log.d(TAG, "BLE Stop!")
    }

    /**
     * Connect to the ble device
     */
    var connectingDevice:Int? = null
    fun connectDevice(device: BluetoothDevice?, position: Int) {
        // update the status
        statusTxt.set("Connecting to ${device?.address}")
        connectingDevice = position
        bleGatt = device?.connectGatt(_application, false, gattClientCallback)
    }


    /**
     * BLE Scan Callback
     */
    private val BLEScanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i(TAG, "Remote device name: " + result.device.name)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(_error: Int) {
            Log.e(TAG, "BLE scan failed with code $_error")
        }

        /**
         * Add scan result
         */
        private fun addScanResult(result: ScanResult) {
            // get scanned device
            val device = result.device
            // get scanned device MAC address
            val deviceAddress = device.address
            val deviceName = device.name
            // add the device to the result list
            for (dev in scanResults!!) {
                if (dev.address == deviceAddress) return
            }
            scanResults?.add(result.device)
            // log
            statusTxt.set("add scanned device: $deviceAddress")
            _listUpdate.value = Event(true)
        }
    }

    /**
     * BLE gattClientCallback
     */
    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if( status == BluetoothGatt.GATT_FAILURE ) {
                disconnectGattServer()
                return
            } else if( status != BluetoothGatt.GATT_SUCCESS ) {
                disconnectGattServer()
                return
            }
            if( newState == BluetoothProfile.STATE_CONNECTED ) {
                // update the connection status message

                statusTxt.set("Connected")
                Log.d(TAG, "Connected to the GATT server")
                gatt.discoverServices()
            } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
                disconnectGattServer()
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Device service discovery failed, status: $status")
                return
            }
            // log for successful discovery
            Log.d(TAG, "Services discovery is successful")
            isConnect.set(true)
            // find command characteristics from the GATT server
            val respCharacteristic = gatt?.let { findResponseCharacteristic(it) }
            // disconnect if the characteristic is not found
            if( respCharacteristic == null ) {
                Log.e(TAG, "Unable to find cmd characteristic")
                disconnectGattServer()
                return
            }
            gatt.setCharacteristicNotification(respCharacteristic, true)
            // UUID for notification
            val descriptor:BluetoothGattDescriptor = respCharacteristic.getDescriptor(
                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG)
            )
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
           //Log.d(TAG, "characteristic changed: " + characteristic.uuid.toString())
            readCharacteristic(characteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully")
            } else {
                Log.e(TAG, "Characteristic write unsuccessful, status: $status")
                disconnectGattServer()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e(TAG, "Characteristic read unsuccessful, status: $status")
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer()
            }
        }

        /**
         * Log the value of the characteristic
         * @param characteristic
         */
        private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {

            val msg = characteristic.getStringValue(0)
            _txtRead += msg
            txtRead.set(_txtRead)
            viewModelScope.launch{
                _scrollDown.value = Event(true)
            }

            Log.d(TAG, "read: $msg")
        }


    }

    /**
     * Disconnect Gatt Server
     */
    fun disconnectGattServer() {
        Log.d(TAG, "Closing Gatt connection")
        // disconnect and close the gatt
        if (bleGatt != null) {
            bleGatt!!.disconnect()
            bleGatt!!.close()
            statusTxt.set("Disconnected")
        }
    }

    fun onClickWrite(){
        val cmdCharacteristic = BluetoothUtils.findCommandCharacteristic(bleGatt!!)
        // disconnect if the characteristic is not found
        if (cmdCharacteristic == null) {
            Log.e(TAG, "Unable to find cmd characteristic")
            disconnectGattServer()
            return
        }
        val cmdBytes = ByteArray(2)
        cmdBytes[0] = 1
        cmdBytes[1] = 2
        cmdCharacteristic.value = cmdBytes
        val success: Boolean = bleGatt!!.writeCharacteristic(cmdCharacteristic)
        // check the result
        if( !success ) {
            Log.e(TAG, "Failed to write command")
        }
    }


}