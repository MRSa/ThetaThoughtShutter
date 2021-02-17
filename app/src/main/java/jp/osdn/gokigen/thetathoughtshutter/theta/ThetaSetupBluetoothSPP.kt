package jp.osdn.gokigen.thetathoughtshutter.theta

import android.graphics.Color
import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.IOperationCallback
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionGetControl
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionSetControl
import org.json.JSONObject

class ThetaSetupBluetoothSPP(executeUrl : String = "http://192.168.1.1")
{
    private val getOption = ThetaOptionGetControl(executeUrl)
    private val setOption = ThetaOptionSetControl(executeUrl)

    fun setupBluetoothSPP(callback : IOperationCallback?)
    {
        getOption.getOptions("[ \"_bluetoothRole\", \"_bluetoothPower\", \"_bluetoothClassicEnable\" ]", object : IOperationCallback { override fun operationExecuted(result: Int, resultStr: String?)
        {
            Log.v(TAG, " optionSet.getOptions(Bluetooth) : $resultStr? ")
            if (resultStr == null)
            {
                callback?.operationExecuted(-1, resultStr)
                return
            }
            try
            {
                val stateObject = JSONObject(resultStr).getJSONObject("results").getJSONObject("options")
                try
                {
                    // Bluetoothの状態を確認
                    val bluetoothClassicEnable = stateObject.getBoolean("_bluetoothClassicEnable")
                    val bluetoothPower = stateObject.getString("_bluetoothPower")
                    val bluetoothRole = stateObject.getString("_bluetoothRole")

                    if (bluetoothRole.contains("Central"))
                    {
                        // Central: ON ⇒ OFF にする
                        //setOption.setOptions("")
                        Log.v(TAG, " --- CHANGE TO 'Peripheral' ---")
                    }
                    Log.v(TAG, " BLUETOOTH CLASSIC : $bluetoothClassicEnable  POWER: $bluetoothPower  ROLE: $bluetoothRole")


                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }





                callback?.operationExecuted(result, resultStr)
            }
            catch (e : Exception)
            {
                e.printStackTrace()
            }
        }})
    }
    companion object
    {
        private val TAG = ThetaSetupBluetoothSPP::class.java.simpleName
    }
}
