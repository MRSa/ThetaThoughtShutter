package jp.osdn.gokigen.thetathoughtshutter

import android.os.Bundle
import android.view.KeyEvent
import com.theta360.pluginlibrary.activity.PluginActivity
import com.theta360.pluginlibrary.callback.KeyCallback
import com.theta360.pluginlibrary.receiver.KeyReceiver
import com.theta360.pluginlibrary.values.LedColor
import com.theta360.pluginlibrary.values.LedTarget
import jp.osdn.gokigen.thetathoughtshutter.R.layout
import jp.osdn.gokigen.thetathoughtshutter.theta.ThetaHardwareControl
import java.lang.Exception

class MainActivity : PluginActivity()
{
    private val thetaHardwareControl = ThetaHardwareControl(this)
    private val applicationStatus : MyApplicationStatus = MyApplicationStatus()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        setAutoClose(true)

        setKeyCallback(object : KeyCallback {
            override fun onKeyDown(keyCode: Int, event: KeyEvent?)
            {
/*
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) // Modeボタン
                {

                }
                if (keyCode == KeyReceiver.KEYCODE_CAMERA)   // Shutterボタン
                {

                }
                if (keyCode == KeyReceiver.KEYCODE_FUNCTION)   // Fnボタン (Z1のみ)
                {

                }
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) // Wirelessボタン
                {

                }
*/
            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent?)
            {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA)   // Shutterボタン
                {
                    if (applicationStatus.status == MyApplicationStatus.Status.Connected)
                    {
                        // EEGからの情報を取得して撮影する
                        applicationStatus.status = MyApplicationStatus.Status.Scanning
                    }
                    else if (applicationStatus.status == MyApplicationStatus.Status.Scanning)
                    {
                        // スタンバイ状態に戻す
                        applicationStatus.status = MyApplicationStatus.Status.Connected
                    }
                }
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) // Wirelessボタン
                {
                    if (applicationStatus.status == MyApplicationStatus.Status.Initialized)
                    {
                        // Bluetooth SPPで EEGに接続する
                        applicationStatus.status = MyApplicationStatus.Status.Searching
                    }
                }
/*
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) // Modeボタン
                {

                }
                if (keyCode == KeyReceiver.KEYCODE_FUNCTION)   // Fnボタン (Z1のみ)
                {

                }
*/
                updateStatus(applicationStatus.status)
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?)
            {
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) // Modeボタン
                {
                    if (applicationStatus.status == MyApplicationStatus.Status.Searching)
                    {
                        // ダミー処理 (EEG接続完了)
                        applicationStatus.status = MyApplicationStatus.Status.Connected
                    }
                    else
                    {
                        // ダミー処理 (初期化完了)
                        applicationStatus.status = MyApplicationStatus.Status.Initialized
                    }
                }
/*
                if (keyCode == KeyReceiver.KEYCODE_CAMERA)   // Shutterボタン
                {

                }
                if (keyCode == KeyReceiver.KEYCODE_FUNCTION)   // Fnボタン (Z1のみ)
                {

                }
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) // Wirelessボタン
                {

                }
*/
                updateStatus(applicationStatus.status)
            }
        })
    }

    private fun updateStatus(currentStatus : MyApplicationStatus.Status)
    {
        try
        {
            when (currentStatus) {
                MyApplicationStatus.Status.Initialized -> {
                    thetaHardwareControl.controlLED(LedTarget.LED3, 1500, LedColor.BLUE)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.BLUE)
                }
                MyApplicationStatus.Status.Searching -> {
                    thetaHardwareControl.controlLED(LedTarget.LED3, 250, LedColor.BLUE)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.BLUE)
                }
                MyApplicationStatus.Status.Connected -> {
                    thetaHardwareControl.controlLED(LedTarget.LED3, 1, LedColor.BLUE)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.BLUE)
                }
                MyApplicationStatus.Status.Scanning -> {
                    thetaHardwareControl.controlLED(LedTarget.LED3, 1, LedColor.BLUE)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 1, LedColor.BLUE)
                }
                MyApplicationStatus.Status.FailedInitialize -> {
                    thetaHardwareControl.controlLED(LedTarget.LED3, 250, LedColor.RED)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.BLUE)
                }
                else -> {
                    thetaHardwareControl.controlLED(LedTarget.LED3, 0, LedColor.BLUE)
                    thetaHardwareControl.controlLED(LedTarget.LED7, 0, LedColor.BLUE)
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
    }

    override fun onPause()
    {
        super.onPause()
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
