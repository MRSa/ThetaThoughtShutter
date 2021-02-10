package jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.eeg

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.IBluetoothScanResult
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.BluetoothDeviceFinder
import jp.osdn.gokigen.thetathoughtshutter.brainwave.BrainwaveFileLogger
import jp.osdn.gokigen.thetathoughtshutter.brainwave.IBrainwaveDataReceiver
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import kotlin.experimental.and


class MindWaveConnection(private val context : Activity, private val dataReceiver: IBrainwaveDataReceiver) : IBluetoothScanResult
{
    companion object
    {
        private val TAG = toString()
    }

    private val deviceFinder: BluetoothDeviceFinder = BluetoothDeviceFinder(context, this)
    private var fileLogger: BrainwaveFileLogger? = null
    private var foundDevice = false
    private var loggingFlag = false

    fun connect(deviceName: String, loggingFlag: Boolean = false)
    {
        Log.v(TAG, " BrainWaveMobileCommunicator::connect() : $deviceName Logging : $loggingFlag")
        try
        {
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
            btSocket.connect()
            inputStream = btSocket.inputStream
        }
        catch (e: Exception)
        {
            Log.e(TAG, "Fail to accept.", e)
        }
        if (inputStream == null)
        {
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

        // シリアルデータの受信メイン部分
        var previousData = 0xff.toByte()
        val outputStream = ByteArrayOutputStream()
        while (foundDevice)
        {
            try
            {
                val data: Int = inputStream.read()
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
            if (foundDevice)
            {
                // デバイスがすでに見つかっている
                Log.v(TAG, " ALREADY FIND BLUETOOTH DEVICE. : $device.name")
                return
            }
            foundDevice = true
            val btSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            val thread = Thread {
                try
                {
                    serialCommunicationMain(btSocket)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            if (btSocket != null)
            {
                thread.start()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}