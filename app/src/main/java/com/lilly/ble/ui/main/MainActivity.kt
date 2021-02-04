package com.lilly.ble.ui.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lilly.ble.PERMISSIONS
import com.lilly.ble.R
import com.lilly.ble.REQUEST_ALL_PERMISSION
import com.lilly.ble.adapter.BleListAdapter
import com.lilly.ble.databinding.ActivityMainBinding
import com.lilly.ble.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private var adapter: BleListAdapter? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        binding.viewModel = viewModel

        binding.rvBleList.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.rvBleList.layoutManager = layoutManager


        adapter = BleListAdapter()
        binding.rvBleList.adapter = adapter
        adapter?.setItemClickListener(object : BleListAdapter.ItemClickListener {
            override fun onClick(view: View, device: BluetoothDevice?) {
                if (device != null) {
                    viewModel.connectDevice(device)
                }
            }
        })

        // check if location permission
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }


        initObserver(binding)




    }
    private fun initObserver(binding: ActivityMainBinding){
        viewModel.requestEnableBLE.observe(this, {
            it.getContentIfNotHandled()?.let {
                requestEnableBLE()
            }
        })
        viewModel.listUpdate.observe(this, {
            it.getContentIfNotHandled()?.let { scanResults ->
                adapter?.setItem(scanResults)
            }
        })
        viewModel.scrollDown.observe(this, {
            it.getContentIfNotHandled()?.let {
                binding.svReadData.post{
                    binding.svReadData.fullScroll(View.FOCUS_DOWN)
                }
            }
        })

        viewModel._isScanning.observe(this,{
            it.getContentIfNotHandled()?.let{ scanning->
                viewModel.isScanning.set(scanning)
            }
        })
        viewModel._isConnect.observe(this,{
            it.getContentIfNotHandled()?.let{ connect->
                viewModel.isConnect.set(connect)
            }
        })
        viewModel.statusTxt.observe(this,{
            it.getContentIfNotHandled()?.let{ status ->
                binding.statusText.text = status
            }
        })

        viewModel.readTxt.observe(this,{
            it.getContentIfNotHandled()?.let{ read ->
                binding.txtRead.text = read
            }
        })
    }
    override fun onResume() {
        super.onResume()
        // finish app if the BLE is not supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }
    }


    private val requestEnableBleResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // do somthing after enableBleRequest
        }
    }

    /**
     * Request BLE enable
     */
    private fun requestEnableBLE() {
        val bleEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestEnableBleResult.launch(bleEnableIntent)
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }
    // Permission check
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}