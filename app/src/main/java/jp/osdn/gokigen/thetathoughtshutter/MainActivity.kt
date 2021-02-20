package jp.osdn.gokigen.thetathoughtshutter

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.theta360.pluginlibrary.activity.PluginActivity
import com.theta360.pluginlibrary.callback.KeyCallback
import com.theta360.pluginlibrary.receiver.KeyReceiver
import com.theta360.pluginlibrary.values.LedColor
import com.theta360.pluginlibrary.values.LedTarget
import jp.osdn.gokigen.thetathoughtshutter.R.layout
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.BluetoothDeviceFinder
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.IBluetoothScanResult
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.eeg.MindWaveConnection
import jp.osdn.gokigen.thetathoughtshutter.brainwave.BrainwaveDataHolder
import jp.osdn.gokigen.thetathoughtshutter.theta.ThetaHardwareControl
import jp.osdn.gokigen.thetathoughtshutter.theta.ThetaSetupBluetoothSPP
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.IOperationCallback
import java.lang.Exception

class MainActivity : PluginActivity(), IBluetoothScanResult
{
    private val thetaHardwareControl = ThetaHardwareControl(this)
    private val applicationStatus : MyApplicationStatus = MyApplicationStatus()
    //private val bluetoothFinder = BluetoothDeviceFinder(this, this)
    private val bluetoothConnection = MindWaveConnection(this, BrainwaveDataHolder())

    companion object
    {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        setAutoClose(true)

        setKeyCallback(object : KeyCallback {
            override fun onKeyDown(keyCode: Int, event: KeyEvent?)
            {

            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent?)
            {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA)   // Shutterボタン
                {
                    when (applicationStatus.status)
                    {
                        MyApplicationStatus.Status.Connected -> {
                            // EEGからの情報を取得して撮影する
                            applicationStatus.status = MyApplicationStatus.Status.Scanning
                        }
                        MyApplicationStatus.Status.Scanning -> {
                            // スタンバイ状態に戻す
                            applicationStatus.status = MyApplicationStatus.Status.Connected
                        }
                        else -> {
                            // ダミー処理 (仮にEEG接続失敗のステータスにする)
                            applicationStatus.status = MyApplicationStatus.Status.FailedInitialize
                        }
                    }
                }
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) // Wirelessボタン
                {
                    if (applicationStatus.status == MyApplicationStatus.Status.Initialized)
                    {
                        // Bluetooth SPPで EEGに接続する
                        connectToEEG()
                    }
                }
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) // Modeボタン
                {
                    if (applicationStatus.status == MyApplicationStatus.Status.Searching)
                    {
                        // ダミー処理 (EEG接続完了)
                        applicationStatus.status = MyApplicationStatus.Status.Connected
                    }
                    else if (applicationStatus.status == MyApplicationStatus.Status.Scanning)
                    {
                        // ダミー処理 (高まっている状態)
                        applicationStatus.status = MyApplicationStatus.Status.Syncing
                    }
                    else if (applicationStatus.status == MyApplicationStatus.Status.Syncing)
                    {
                        // ダミー処理 (高まるのを待っている状態)
                        applicationStatus.status = MyApplicationStatus.Status.Scanning
                    }
                    else
                    {
                        // ダミー処理 (初期化完了)
                        applicationStatus.status = MyApplicationStatus.Status.Initialized
                    }
                }
/*
                if (keyCode == KeyReceiver.KEYCODE_FUNCTION)   // Fnボタン (Z1のみ)
                {

                }
*/
                updateStatus(applicationStatus.status)
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?)
            {

            }
        })
        updateStatus(applicationStatus.status)
    }

    // Bluetooth SPPで EEGに接続する
    private fun connectToEEG()
    {
        try
        {
            val thread = Thread {
                try
                {
                    bluetoothConnection.connect("MindWave Mobile")
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        applicationStatus.status = MyApplicationStatus.Status.Searching
    }

    private fun updateStatus(currentStatus : MyApplicationStatus.Status)
    {
        try
        {
            when (currentStatus) {
                MyApplicationStatus.Status.Initialized -> {
                    Log.v(TAG, " INITIALIZED")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 1500, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)     // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)      // 赤ランプ
                }
                MyApplicationStatus.Status.Searching -> {
                    Log.v(TAG, " SEARCHING")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 250, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)     // 赤ランプ
                }
                MyApplicationStatus.Status.Connected -> {
                    Log.v(TAG, " CONNECTED")
                    thetaHardwareControl.controlLED(LedTarget.LED3,  0, LedColor.GREEN)   // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)     // 赤ランプ
                }
                MyApplicationStatus.Status.Scanning -> {
                    Log.v(TAG, " SCANNING")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 0, LedColor.GREEN)    // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.RED)      // 赤ランプ
                }
                MyApplicationStatus.Status.Syncing -> {
                    Log.v(TAG, " SYNCING")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 0, LedColor.GREEN)    // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, 0, LedColor.BLUE)     // Liveランプ (ON)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.RED)      // 赤ランプ
                }
                MyApplicationStatus.Status.FailedInitialize -> {
                    Log.v(TAG, " FAILED INITIALIZE")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 250, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 250, LedColor.RED)    // 赤ランプ
                }
                else -> {
                    Log.v(TAG, " UNDEFINED")
                    thetaHardwareControl.controlLED(LedTarget.LED3, -1, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED4, -1, LedColor.BLUE)   // カメラランプ
                    thetaHardwareControl.controlLED(LedTarget.LED5, -1, LedColor.BLUE)   // ムービーランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6,  0, LedColor.BLUE)   // Liveランプ (ON)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)    // 赤ランプ
                }
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onResume()
    {
        super.onResume()
        if (isApConnected)
        {

        }
        initializeBluetooth()
    }

    override fun onPause()
    {
        super.onPause()
    }

    private fun initializeBluetooth()
    {
        try
        {
            val thread = Thread {
                try
                {
                    val setupBluetooth = ThetaSetupBluetoothSPP("http://localhost:8080")
                    setupBluetooth.setupBluetoothSPP(object : IOperationCallback { override fun operationExecuted(result: Int, resultStr: String?)
                    {
                        Log.v(TAG, " optionSet.getOptions(Bluetooth) : $resultStr ($result)")

                        if (result == 0)
                        {
                            // Bluetoothの初期化終了
                            applicationStatus.status = MyApplicationStatus.Status.Initialized
                            updateStatus(applicationStatus.status)
                        }
                    }})
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
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
            // Bluetoothデバイスが見つかった！
            applicationStatus.status = MyApplicationStatus.Status.Connected
            updateStatus(applicationStatus.status)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    override fun notFindBluetoothDevice()
    {
        try
        {
            // Bluetoothデバイスが見つからなかった...
            applicationStatus.status = MyApplicationStatus.Status.Initialized
            updateStatus(applicationStatus.status)
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }
}

//
// -----------------------------------------------------
//  LED1 : 電源ランプ
//  LED2 : カメラステータス ランプ(レンズとマイクの間)
//  LED3 : ワイヤレスマーク ランプ
//  LED4 : キャプチャーモード (カメラ)
//  LED5 : キャプチャーモード (ムービー)
//  LED6 : キャプチャーモード (LIVEストリーミング)
//  LED7 : ビデオ録画 ランプ
//  LED8 : メモリ警告ランプ
//
//  BTN1 : 電源ボタン
//  BTN2 : ワイヤレスボタン
//  BTN3 : モードボタン
//  SHUT : シャッターボタン
// -----------------------------------------------------
//
//  [制御可能なLED]
//    - LED3～LED8 : カラー : "blue", "green", "red", "cyan", "magenta", "yellow", "white"
//    - ブリンク間隔 : 1～2000 msec
//
//  [KeyCode]
//    - 27  : Shutter Button
//    - 130 : Mode Button
//    - 284 : Wireless Button
//    - 119 : Fn Button (Z1 Only)
//
//
//   http://localhost:8080/
//
