package jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection

import android.bluetooth.BluetoothDevice

interface IBluetoothScanResult
{
    fun foundBluetoothDevice(device: BluetoothDevice)
}
