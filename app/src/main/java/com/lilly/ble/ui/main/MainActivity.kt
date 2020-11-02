package com.lilly.ble.ui.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lilly.ble.Constants.Companion.PERMISSIONS
import com.lilly.ble.Constants.Companion.REQUEST_ALL_PERMISSION
import com.lilly.ble.Constants.Companion.REQUEST_ENABLE_BT
import com.lilly.ble.R
import com.lilly.ble.adapter.BleListAdapter
import com.lilly.ble.databinding.ActivityMainBinding
import com.lilly.ble.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var adapter: BleListAdapter? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        binding.viewModel = viewModel

        rv_ble_list.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        rv_ble_list.layoutManager = layoutManager


        adapter = BleListAdapter()
        rv_ble_list.adapter = adapter
        adapter?.setItemClickListener(object : BleListAdapter.ItemClickListener {
            override fun onClick(view: View, position:Int, device: BluetoothDevice?) {
                viewModel.connectDevice(device,position)
                //adapter?.conButtonToggle(position)
            }
        })

        // check if location permission
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }



        viewModel.requestEnableBLE.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                requestEnableBLE()
            }
        })
        viewModel.listUpdate.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                updateBLEList()
            }
        })
        viewModel.scrollDown.observe(this, Observer {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                sv_read_data.post{
                    sv_read_data.fullScroll(View.FOCUS_DOWN)
                }
            }
        })

        viewModel.setBLEAdapter()
    }
    override fun onResume() {
        super.onResume()
        // finish app if the BLE is not supported
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish()
        }
    }

    private fun updateBLEList(){
        adapter?.setItem(viewModel.scanResults)
    }

    /**
     * Request BLE enable
     */
    private fun requestEnableBLE() {
        val bleEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(bleEnableIntent, REQUEST_ENABLE_BT)
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