package jp.osdn.gokigen.thetathoughtshutter.theta

import IThetaHardwareControl
import android.graphics.Bitmap
import com.theta360.pluginlibrary.activity.PluginActivity
import com.theta360.pluginlibrary.values.LedColor
import com.theta360.pluginlibrary.values.LedTarget
import com.theta360.pluginlibrary.values.OledDisplay
import com.theta360.pluginlibrary.values.TextArea
import java.util.*
import kotlin.collections.HashMap

class ThetaHardwareControl(private val activity: PluginActivity) : IThetaHardwareControl
{
    override fun controlLED(device: LedTarget, period: Int, color: LedColor)
    {
        try {
            activity.runOnUiThread {
                try
                {
                    when (period) {
                        0 -> {
                            if (device == LedTarget.LED3)
                            {
                                activity.notificationLed3Show(color)
                            }
                            else
                            {
                                activity.notificationLedShow(device)
                            }
                        }
                        -1 -> activity.notificationLedHide(device)
                        else -> activity.notificationLedBlink(device, color, period)
                    }
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun brightnessLED(device: LedTarget, brightness : Int)
    {
        try {
            activity.runOnUiThread {
                try
                {
                    activity.notificationLedBrightnessSet(device, brightness)
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun controlOLED(period: Int, bitmap: Bitmap?)
    {
        try {
            activity.runOnUiThread {
                try
                {
                    if (bitmap != null)
                    {
                        when (period)
                        {
                            0 -> activity.notificationOledImageShow(bitmap)
                            -1 -> activity.notificationOledHide()
                            else -> activity.notificationOledImageBlink(bitmap, period)
                        }
                    }
                    else
                    {
                        activity.notificationOledHide()
                    }
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun controlOLED(textMap: Map<TextArea?, String?>)
    {
        try {
            activity.runOnUiThread {
                try
                {
                    if (textMap.isEmpty())
                    {
                        activity.notificationOledHide()
                    }
                    else
                    {
                        activity.notificationOledTextShow(textMap)
                    }
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun useOLED(enable : Boolean)
    {
        try {
            activity.runOnUiThread {
                try
                {
                    activity.notificationOledDisplaySet(if (enable) { OledDisplay.DISPLAY_PLUGIN } else { OledDisplay.DISPLAY_BASIC })
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
