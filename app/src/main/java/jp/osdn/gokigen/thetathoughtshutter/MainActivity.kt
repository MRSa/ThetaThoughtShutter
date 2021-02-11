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

class MainActivity : PluginActivity()
{
    private val thetaHardwareControl = ThetaHardwareControl(this)
    private var applicationStatus : MyApplicationStatus = MyApplicationStatus.Undefined

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        setAutoClose(true)

        setKeyCallback(object : KeyCallback {
            override fun onKeyDown(keyCode: Int, event: KeyEvent?)
            {
                if (keyCode == KeyReceiver.KEYCODE_CAMERA)
                {

                }
            }

            override fun onKeyUp(keyCode: Int, event: KeyEvent?)
            {
                notificationLedBlink(LedTarget.LED3, LedColor.BLUE, 1000)
                //notificationLedBlink(LedTarget.LED4, LedColor.CYAN, 1000)
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?)
            {
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
            }
        })
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
