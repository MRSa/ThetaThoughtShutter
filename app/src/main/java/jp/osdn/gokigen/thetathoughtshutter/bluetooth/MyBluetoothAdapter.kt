package jp.osdn.gokigen.thetathoughtshutter.bluetooth

import android.bluetooth.BluetoothAdapter


class MyBluetoothAdapter
{
    fun getBondedDevices(): List<String>
    {
        val s: MutableList<String> = ArrayList()
        try
        {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val bondedDevices = btAdapter.bondedDevices
            for (bt in bondedDevices)
            {
                s.add(bt.name)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (s)
    }
}
