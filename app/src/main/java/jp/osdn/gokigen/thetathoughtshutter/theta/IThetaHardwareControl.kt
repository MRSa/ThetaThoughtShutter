import android.graphics.Bitmap
import com.theta360.pluginlibrary.values.LedColor
import com.theta360.pluginlibrary.values.LedTarget
import com.theta360.pluginlibrary.values.TextArea

interface IThetaHardwareControl
{
    // LEDを制御する
    // device : 操作対象LED (LED3 - LED8)
    // period : -1 : OFF, 0 : ON, それ以外 :点滅 (250 - 2000)
    // color  : 点滅時のみ有効
    fun controlLED(device: LedTarget, period: Int, color: LedColor = LedColor.BLUE)

    // LEDの明るさを設定する (LED1, LED2, OLED)
    // device : 操作対象LED
    // brightness : 明るさ (0 - 100, default: 25)
    fun brightnessLED(device: LedTarget, brightness : Int = 25)

    // OLEDにビットマップを表示する
    fun controlOLED(period: Int, bitmap: Bitmap? = null)

    // OLEDに文字を表示する
    fun controlOLED(textMap: Map<TextArea?, String?>)

    // pluginで OLEDを使用するかどうか設定する
    fun useOLED(enable : Boolean = true)

}
