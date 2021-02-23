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
import com.theta360.pluginlibrary.values.TextArea
import jp.osdn.gokigen.thetathoughtshutter.R.layout
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.IBluetoothScanResult
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.eeg.MindWaveConnection
import jp.osdn.gokigen.thetathoughtshutter.brainwave.BrainwaveDataHolder
import jp.osdn.gokigen.thetathoughtshutter.brainwave.IDetectSensingReceiver
import jp.osdn.gokigen.thetathoughtshutter.theta.ThetaCaptureControl
import jp.osdn.gokigen.thetathoughtshutter.theta.ThetaHardwareControl
import jp.osdn.gokigen.thetathoughtshutter.theta.ThetaSetupBluetoothSPP
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.IOperationCallback
import jp.osdn.gokigen.thetathoughtshutter.theta.status.ThetaCameraStatusWatcher
import java.util.*
import kotlin.collections.HashMap

class MainActivity : PluginActivity(), IBluetoothScanResult, IDetectSensingReceiver
{
    private val thetaHardwareControl = ThetaHardwareControl(this)
    private val thetaCaptureControl = ThetaCaptureControl("http://localhost:8080")
    private val thetaStatusWatcher = ThetaCameraStatusWatcher("http://localhost:8080")
    private val applicationStatus : MyApplicationStatus = MyApplicationStatus()
    private val bluetoothConnection = MindWaveConnection(this, BrainwaveDataHolder(this), this)

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
            override fun onKeyDown(keyCode: Int, event: KeyEvent?) {

            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent?) {
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) // Wirelessボタン
                {
                    if ((applicationStatus.status == MyApplicationStatus.Status.Initialized) ||
                            (applicationStatus.status == MyApplicationStatus.Status.FailedInitialize)) {
                        // Bluetooth SPPで EEGに接続する
                        connectToEEG()
                    }
                }
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) // Modeボタン
                {
                    // 接続エラーにする
                    applicationStatus.status = MyApplicationStatus.Status.FailedInitialize
                }
/*
                if (keyCode == KeyReceiver.KEYCODE_FUNCTION)   // Fnボタン (Z1のみ)
                {

                }

                if (keyCode == KeyReceiver.KEYCODE_CAMERA)   // Shutterボタン
                {

                }
*/
                updateStatus(applicationStatus.status)
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?) {

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

    private fun disconnectToEEG()
    {
        try
        {
            val thread = Thread {
                try
                {
                    bluetoothConnection.disconnect()
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
        applicationStatus.status = MyApplicationStatus.Status.Initialized
    }

    private fun updateStatus(currentStatus: MyApplicationStatus.Status)
    {
        try
        {
            when (currentStatus) {
                MyApplicationStatus.Status.Initialized -> {
                    Log.v(TAG, " INITIALIZED")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 1500, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)     // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)      // 赤ランプ
                    thetaHardwareControl.controlOLED(mapOf(TextArea.MIDDLE to "READY TO SEARCH"))
                }
                MyApplicationStatus.Status.Searching -> {
                    Log.v(TAG, " SEARCHING")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 250, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)     // 赤ランプ
                    thetaHardwareControl.controlOLED(mapOf(TextArea.MIDDLE to "SEARCHING"))
                }
                MyApplicationStatus.Status.Connected -> {
                    Log.v(TAG, " CONNECTED")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 0, LedColor.GREEN)   // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)     // 赤ランプ
                    thetaHardwareControl.controlOLED(mapOf(TextArea.MIDDLE to "CONNECTED"))
                }
                MyApplicationStatus.Status.Scanning -> {
                    Log.v(TAG, " SCANNING")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 0, LedColor.GREEN)    // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.RED)      // 赤ランプ
                    thetaHardwareControl.controlOLED(mapOf(TextArea.MIDDLE to "SCANNING"))
                }
                MyApplicationStatus.Status.Syncing -> {
                    Log.v(TAG, " SYNCING")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 0, LedColor.GREEN)    // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, 0, LedColor.BLUE)     // Liveランプ (ON)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.RED)      // 赤ランプ
                    thetaHardwareControl.controlOLED(mapOf(TextArea.MIDDLE to "SYNCING"))
                }
                MyApplicationStatus.Status.FailedInitialize -> {
                    Log.v(TAG, " FAILED INITIALIZE")
                    thetaHardwareControl.controlLED(LedTarget.LED3, 250, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, -1, LedColor.BLUE)    // Liveランプ (OFF)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 250, LedColor.RED)    // 赤ランプ
                    thetaHardwareControl.controlOLED(mapOf(TextArea.MIDDLE to "INITIALIZE FAILED"))
                }
                else -> {
                    Log.v(TAG, " UNDEFINED")
                    thetaHardwareControl.controlLED(LedTarget.LED3, -1, LedColor.GREEN)  // WIFIランプ
                    thetaHardwareControl.controlLED(LedTarget.LED4, -1, LedColor.BLUE)   // カメラランプ
                    thetaHardwareControl.controlLED(LedTarget.LED5, -1, LedColor.BLUE)   // ムービーランプ
                    thetaHardwareControl.controlLED(LedTarget.LED6, 0, LedColor.BLUE)   // Liveランプ (ON)
                    thetaHardwareControl.controlLED(LedTarget.LED7, -1, LedColor.RED)    // 赤ランプ
                    thetaHardwareControl.controlOLED(EnumMap(com.theta360.pluginlibrary.values.TextArea::class.java))
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onResume()
    {
        super.onResume()
        if (isApConnected)
        {
            Log.v(TAG, " isApConnected : $isApConnected")
        }
        try
        {
            thetaCaptureControl.setupCaptureMode()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        initializeBluetooth()
    }

    override fun onPause()
    {
        super.onPause()
        try
        {
            disconnectToEEG()
            thetaStatusWatcher.stopStatusWatch()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun initializeBluetooth()
    {
        try
        {
            val thread = Thread {
                try
                {
                    val setupBluetooth = ThetaSetupBluetoothSPP("http://localhost:8080")
                    setupBluetooth.setupBluetoothSPP(object : IOperationCallback {
                        override fun operationExecuted(result: Int, resultStr: String?) {
                            Log.v(TAG, " optionSet.getOptions(Bluetooth) : $resultStr ($result)")

                            if (result == 0) {
                                // Bluetoothの初期化終了
                                applicationStatus.status = MyApplicationStatus.Status.Initialized
                                updateStatus(applicationStatus.status)
                            }
                        }
                    })
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

            // 周期実行
            thetaStatusWatcher.startStatusWatch()
        }
        catch (e: Exception)
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
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startSensing()
    {
        // データの検出を開始する
        Log.v(TAG, "  ===== START SENSING =====")
        applicationStatus.status = MyApplicationStatus.Status.Scanning
        updateStatus(applicationStatus.status)
    }

    override fun detectAttention()
    {
        //Log.v(TAG, "  ===== DETECT ATTENTION =====")
        applicationStatus.status = MyApplicationStatus.Status.Syncing
        updateStatus(applicationStatus.status)
    }

    override fun detectMediation()
    {
        //Log.v(TAG, "  ===== DETECT MEDIATION =====")
    }

    override fun lostAttention()
    {
        //Log.v(TAG, "  ===== LOST ATTENTION =====")
        applicationStatus.status = MyApplicationStatus.Status.Scanning
        updateStatus(applicationStatus.status)
    }

    override fun lostMediation()
    {
        //Log.v(TAG, "  ===== LOST MEDIATION =====")
    }

    override fun detectAttentionThreshold()
    {
        //Log.v(TAG, "  ===== DETECT ATTENTION THRESHOLD =====")
        try
        {
            // 静止画の撮影！
            thetaCaptureControl.doCapture()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun detectMediationThreshold()
    {
        //Log.v(TAG, "  ===== DETECT MEDIATION THRESHOLD =====")
    }
}
