package jp.osdn.gokigen.thetathoughtshutter

import android.os.Bundle
import android.view.KeyEvent
import com.theta360.pluginlibrary.activity.PluginActivity
import com.theta360.pluginlibrary.callback.KeyCallback
import com.theta360.pluginlibrary.receiver.KeyReceiver
import com.theta360.pluginlibrary.values.LedColor
import com.theta360.pluginlibrary.values.LedTarget
import jp.osdn.gokigen.thetathoughtshutter.R.layout

class MainActivity : PluginActivity()
{
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
            }

            override fun onKeyLongPress(keyCode: Int, event: KeyEvent?)
            {
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD)
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