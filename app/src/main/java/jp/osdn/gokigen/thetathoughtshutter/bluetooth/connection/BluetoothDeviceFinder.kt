package jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

class BluetoothDeviceFinder(private val context: Activity, private val scanResult: IBluetoothScanResult) : BluetoothAdapter.LeScanCallback, ScanCallback()
{

    companion object
    {
        private val TAG = BluetoothDeviceFinder::class.java.simpleName
    }
    private lateinit var targetDeviceName: String
    private var foundBleDevice = false
    private var scanner : BluetoothLeScanner? = null

    fun reset()
    {
        foundBleDevice = false
    }

    fun stopScan()
    {
        try
        {
            Log.v(TAG, " stopScan()")
            val btMgr: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            btMgr.adapter.cancelDiscovery()
            scanner?.flushPendingScanResults(this)
            scanner?.stopScan(this)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    fun startScan(targetDeviceName: String)
    {
        try
        {
            this.targetDeviceName = targetDeviceName
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!btAdapter.isEnabled)
            {
                // Bluetoothの設定がOFFだった
                Log.v(TAG, " BLUETOOTH SETTING IS OFF")
                scanResult.notFindBluetoothDevice()
                return
            }
            // Bluetooth のサービスを取得
            val btMgr: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            scanBluetoothDevice(btMgr)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun scanBluetoothDevice(btMgr: BluetoothManager)
    {
        try
        {
            Log.v(TAG, " scanBluetoothDevice() ")

            // スキャン開始
            foundBleDevice = false
            val adapter = btMgr.adapter
            if (adapter.isDiscovering)
            {
                adapter.cancelDiscovery()
            }
            //adapter.startDiscovery()
            scanner = adapter.bluetoothLeScanner
            scanner?.startScan(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Log.v(TAG, "Bluetooth SCAN EXCEPTION...")
        }
    }

    override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?)
    {
        try
        {
            Log.v(TAG, " onLeScan() ")

            val btDeviceName = device.name
            if (btDeviceName != null && btDeviceName.matches(Regex(targetDeviceName)))
            {
                // device発見！
                foundBleDevice = true
                scanResult.foundBluetoothDevice(device)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onScanFailed(errorCode: Int)
    {
        Log.v(TAG, " onScanFailed : $errorCode")
    }

    override fun onScanResult(callbackType: Int, result: ScanResult?)
    {
        //super.onScanResult(callbackType, result)
        val device = result?.device
        Log.v(TAG, " onScanResult($callbackType, ${device?.name}) ")
        val findDevice = (device?.name)?.contains(targetDeviceName)
        if ((findDevice != null)&&(findDevice))
        {
            Log.v(TAG, " FIND DEVICE : $targetDeviceName")
            scanResult.foundBluetoothDevice(device)
            scanner?.stopScan(this)
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?)
    {
        //super.onBatchScanResults(results)
        Log.v(TAG, " onBatchScanResults ")
    }
}
