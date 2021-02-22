package com.lilly.ble.viewmodel

import android.bluetooth.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.lilly.ble.*
import com.lilly.ble.util.Event
import java.util.*


class MainViewModel(private val bleRepository: BleRepository) : ViewModel() {
    val statusTxt: LiveData<String>
        get() = bleRepository.fetchStatusText().asLiveData(viewModelScope.coroutineContext)
    val readTxt: LiveData<String>
        get() = bleRepository.fetchReadText().asLiveData(viewModelScope.coroutineContext)




    //ble adapter
    private val bleAdapter: BluetoothAdapter?
        get() = bleRepository.bleAdapter


    val requestEnableBLE : LiveData<Event<Boolean>>
        get() = bleRepository.requestEnableBLE
    val listUpdate : LiveData<Event<ArrayList<BluetoothDevice>?>>
        get() = bleRepository.listUpdate

    val _isScanning: LiveData<Event<Boolean>>
        get() = bleRepository.isScanning
    var isScanning = ObservableBoolean(false)
    val _isConnect: LiveData<Event<Boolean>>
        get() = bleRepository.isConnect
    var isConnect = ObservableBoolean(false)



    /**
     *  Start BLE Scan
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickScan(){
        bleRepository.startScan()
    }
    fun onClickDisconnect(){
        bleRepository.disconnectGattServer()
    }
    fun connectDevice(bluetoothDevice: BluetoothDevice){
        bleRepository.connectDevice(bluetoothDevice)
    }




    fun onClickWrite(){

        val cmdBytes = ByteArray(2)
        cmdBytes[0] = 1
        cmdBytes[1] = 2

        bleRepository.writeData(cmdBytes)

    }


}