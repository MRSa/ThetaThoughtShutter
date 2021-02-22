package jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.eeg

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.IBluetoothScanResult
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.BluetoothDeviceFinder
import jp.osdn.gokigen.thetathoughtshutter.brainwave.BrainwaveFileLogger
import jp.osdn.gokigen.thetathoughtshutter.brainwave.IBrainwaveDataReceiver
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import kotlin.experimental.and

class MindWaveConnection(private val activity : Activity, private val dataReceiver: IBrainwaveDataReceiver, private val scanResult: IBluetoothScanResult? = null) : IBluetoothScanResult
{
    companion object
    {
        private val TAG = MindWaveConnection::class.java.simpleName
    }

    private val deviceFinder = BluetoothDeviceFinder(activity, this)
    private var fileLogger: BrainwaveFileLogger? = null
    private var foundDevice = false
    private var isPairing = false
    private var loggingFlag = false
    private var targetDevice: BluetoothDevice? = null

    private var connectionReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            onReceiveBroadcastOfConnection(this, intent)
        }
    }

    fun connect(deviceName: String, loggingFlag: Boolean = false)
    {
        Log.v(TAG, " BrainWaveMobileCommunicator::connect() : $deviceName Logging : $loggingFlag")
        try
        {
            registerReceiver()

            this.loggingFlag = loggingFlag

            // Bluetooth のサービスを取得、BLEデバイスをスキャンする
            foundDevice = false
            deviceFinder.reset()
            deviceFinder.startScan(deviceName)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun disconnect()
    {
        foundDevice = false
    }

    private fun registerReceiver()
    {
        try
        {
            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            activity.registerReceiver(connectionReceiver, filter)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    private fun unregisterReceiver()
    {
        try
        {
            activity.unregisterReceiver(connectionReceiver)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseReceivedData(data: ByteArray)
    {
        // 受信データブロック１つ分
        try
        {
            if (data.size <= 3)
            {
                // ヘッダ部しか入っていない...無視する
                return
            }
            val length = data[2]
            if (data.size < length + 2)
            {
                // データが最小サイズに満たない...無視する
                return
            }
            if (data.size == 8 || data.size == 9)
            {
                var value: Int = (data[5] and 0xff.toByte()) * 256 + (data[6] and 0xff.toByte())
                if (value > 32768)
                {
                    value -= 65536
                }
                dataReceiver.receivedRawData(value)
                return
            }
            dataReceiver.receivedSummaryData(data)

            // ファイルにサマリーデータを出力する
            fileLogger?.outputSummaryData(data)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun serialCommunicationMain(btSocket: BluetoothSocket)
    {
        var inputStream: InputStream? = null
        try
        {
            Log.v(TAG, "serialCommunicationMain connect")
            btSocket.connect()
            inputStream = btSocket.inputStream
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Fail to accept.", e)
        }
        if (inputStream == null)
        {
            Log.v(TAG, "serialCommunicationMain INPUT STREAM IS NULL...")
            return
        }
        if (loggingFlag)
        {
            try
            {
                // ログ出力を指示されていた場合...ファイル出力クラスを作成しておく
                fileLogger = BrainwaveFileLogger()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        Log.v(TAG, " serialCommunicationMain : SERIAL COMMUNICATION STARTED.")


        // シリアルデータの受信メイン部分
        var previousData = 0xff.toByte()
        val outputStream = ByteArrayOutputStream()
        while (foundDevice)
        {
            try
            {
                val data: Int = inputStream.read()
                //Log.v(TAG, " RECEIVED ")
                val byteData = (data and 0xff).toByte()
                if (previousData == byteData && byteData == 0xaa.toByte())
                {
                    // 先頭データを見つけた。 （0xaa 0xaa がヘッダ）
                    parseReceivedData(outputStream.toByteArray())
                    outputStream.reset()
                    outputStream.write(0xaa)
                    outputStream.write(0xaa)
                }
                else
                {
                    outputStream.write(byteData.toInt())
                }
                previousData = byteData
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        try
        {
            btSocket.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun foundBluetoothDevice(device: BluetoothDevice)
    {
        try
        {
            Log.v(TAG, " foundBluetoothDevice : ${device.name} : ${device.bondState}")

            deviceFinder.stopScan()

            if (device.bondState == BluetoothDevice.BOND_BONDED)
            {
                // すでにペアリング済み
                Log.v(TAG, " ALREADY PAIRED ")
                isPairing = true
                targetDevice = device
                connectBluetoothDevice(device)
                return
            }
            else if (device.bondState == BluetoothDevice.BOND_NONE)
            {
                // まだペアリングしていない...
                Log.v(TAG, " NOT PAIRED, START PAIRING : ${device.name}")
                isPairing = true
                targetDevice = device
                device.setPin(byteArrayOf(0x30, 0x30, 0x30, 0x30))
                device.createBond()
                return
            }

            if ((!foundDevice)&&(!isPairing))
            {
                Log.v(TAG, "START PAIRING ${device.name}")
                device.setPin(byteArrayOf(0x30, 0x30, 0x30, 0x30))
                targetDevice = device
                device.createBond()
                isPairing = true
            }
            deviceFinder.stopScan()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    private fun connectBluetoothDevice(device : BluetoothDevice?)
    {
        try
        {
            Log.v(TAG, "connectBluetoothDevice() : ${device?.name}")
            if (device == null)
            {
                Log.v(TAG, " DEVICE IS NULL...")
                return
            }

            val btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            val thread = Thread {
                try
                {
                    if (btSocket != null)
                    {
                        serialCommunicationMain(btSocket)
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            if (btSocket != null)
            {
                scanResult?.foundBluetoothDevice(device)
                thread.start()
                unregisterReceiver()
            }
            else
            {
                Log.v(TAG, " btSocket is NULL.")
            }
            //scanResult?.foundBluetoothDevice(device)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun notFindBluetoothDevice()
    {
        Log.v(TAG, " notFindBluetoothDevice()")
        scanResult?.notFindBluetoothDevice()
        deviceFinder.stopScan()
    }

    /**
     *
     *
     */
    private fun onReceiveBroadcastOfConnection(receiver: BroadcastReceiver, intent: Intent)
    {
        val action = intent.action
        if (action == null)
        {
            Log.v(TAG, "intent.getAction() : null")
            return
        }
        try
        {
            if (action == BluetoothDevice.ACTION_PAIRING_REQUEST)
            {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                Log.v(TAG, " onReceiveBroadcastOfConnection : BluetoothDevice.ACTION_PAIRING_REQUEST  device: ${device.name}")
                 try
                {
                    targetDevice?.setPin(byteArrayOf(0x30,0x30, 0x30, 0x30))
                    device.setPin(byteArrayOf(0x30,0x30, 0x30, 0x30))
                    receiver.abortBroadcast()
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
            else if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                Log.v(TAG, " onReceiveBroadcastOfConnection : BluetoothDevice.ACTION_BOND_STATE_CHANGED ($bondState) device: ${device.name}")
                if (bondState == BluetoothDevice.BOND_BONDED)
                {
                    // ペアリング完了！
                    Log.v(TAG, " ----- Device is Paired! ${device.name}")
                    foundDevice = true
                    connectBluetoothDevice(device)
                }
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "onReceiveBroadcastOfConnection() EXCEPTION" + e.message)
            e.printStackTrace()
        }
    }
}
