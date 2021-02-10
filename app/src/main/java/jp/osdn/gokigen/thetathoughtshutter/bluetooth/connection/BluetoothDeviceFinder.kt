package jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.R
import jp.osdn.gokigen.thetathoughtshutter.utils.SnackBarMessage

class BluetoothDeviceFinder(private val context: Activity, private val scanResult: IBluetoothScanResult) : BluetoothAdapter.LeScanCallback
{

    companion object
    {
        private val TAG = toString()
        private const val BLE_SCAN_TIMEOUT_MILLIS = 15 * 1000 // 15秒間
        private const val BLE_WAIT_DURATION = 100 // 100ms間隔
    }
    private lateinit var targetDeviceName: String
    private val messageToShow = SnackBarMessage(context, false)
    private var foundBleDevice = false

    fun reset()
    {
        foundBleDevice = false
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
                messageToShow.showMessage(R.string.bluetooth_setting_is_off)
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
            // スキャン開始
            foundBleDevice = false
            val adapter = btMgr.adapter
            if (!adapter.startLeScan(this))
            {
                // Bluetooth LEのスキャンが開始できなかった場合...
                Log.v(TAG, "Bluetooth LE SCAN START fail...")
                messageToShow.showMessage(R.string.bluetooth_scan_start_failure)
                return
            }
            Log.v(TAG, " ----- BT SCAN STARTED ----- ")
            var passed = 0
            while (passed < BLE_SCAN_TIMEOUT_MILLIS)
            {
                if (foundBleDevice)
                {
                    // デバイス発見
                    Log.v(TAG, "FOUND DEVICE")
                    break
                }

                // BLEのスキャンが終わるまで待つ
                Thread.sleep(BLE_WAIT_DURATION.toLong())
                passed += BLE_WAIT_DURATION
            }
            // スキャンを止める(500ms後に)
            Thread.sleep(500)
            adapter.stopLeScan(this)
            Log.v(TAG, " ----- BT SCAN STOPPED ----- ")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Log.v(TAG, "Bluetooth LE SCAN EXCEPTION...")
            messageToShow.showMessage(R.string.scan_fail_via_bluetooth)
        }
        Log.v(TAG, "Bluetooth SCAN STOPPED.")
    }

    override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?)
    {
        try
        {
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
}
