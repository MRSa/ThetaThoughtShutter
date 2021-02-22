package jp.osdn.gokigen.thetathoughtshutter.theta

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.IOperationCallback
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionGetControl
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionSetControl
import org.json.JSONObject

class ThetaSetupBluetoothSPP(executeUrl : String = "http://192.168.1.1") : IOperationCallback
{
    private val getOption = ThetaOptionGetControl(executeUrl)
    private val setOption = ThetaOptionSetControl(executeUrl)
    private var readyCallback : IOperationCallback? = null

    companion object
    {
        private val TAG = ThetaSetupBluetoothSPP::class.java.simpleName
    }

    fun setupBluetoothSPP(callback : IOperationCallback?)
    {
        this.readyCallback = callback
        getOption.getOptions("[ \"_bluetoothRole\", \"_bluetoothPower\", \"_bluetoothClassicEnable\" ]", this)
    }

    private fun setBluetoothPowerOn(bluetoothPower : String?)
    {
        if (bluetoothPower == null)
        {
            readyCallback?.operationExecuted(-1, "")
            return
        }
        try
        {
            if (!bluetoothPower.contains("ON"))
            {
                //
                Log.v(TAG, " --- Bluetooth Power ON ---")
                setOption.setOptions("\"_bluetoothPower\" : \"ON\"", object : IOperationCallback {
                    override fun operationExecuted(result: Int, resultStr: String?)
                    {
                        readyCallback?.operationExecuted(result, resultStr)
                    }
                })
            }
            readyCallback?.operationExecuted(0, "OK")
            return
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        readyCallback?.operationExecuted(-1, "")
    }

    private fun setBluetoothClassicEnable(bluetoothClassicEnable: Boolean, bluetoothPower: String?)
    {
        try
        {
            if (!bluetoothClassicEnable)
            {
                //
                Log.v(TAG, " --- Bluetooth Classic Enable ---")
                setOption.setOptions("\"_bluetoothClassicEnable\" : \"true\"", object : IOperationCallback {
                    override fun operationExecuted(result: Int, resultStr: String?)
                    {
                        if (resultStr != null)
                        {
                            Log.v(TAG, "_bluetoothClassicEnable : true  $resultStr ")
                            setBluetoothPowerOn(bluetoothPower)
                        }
                        else
                        {
                            readyCallback?.operationExecuted(-1, resultStr)
                        }
                    }
                })
            }
            else
            {
                setBluetoothPowerOn(bluetoothPower)
            }
            return
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        readyCallback?.operationExecuted(-1, "")
    }

    override fun operationExecuted(result: Int, resultStr: String?)
    {
        Log.v(TAG, " optionSet.getOptions(Bluetooth) : $resultStr? ")
        if (resultStr == null)
        {
            readyCallback?.operationExecuted(-1, resultStr)
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

                Log.v(TAG, " BLUETOOTH CLASSIC : $bluetoothClassicEnable  POWER: $bluetoothPower  ROLE: $bluetoothRole")

                if (bluetoothRole.contains("Central"))
                {
                    // Central: ON ⇒ OFF にする
                    Log.v(TAG, " --- CHANGE TO 'Peripheral' ---")
                    setOption.setOptions("\"_bluetoothRole\" : \"Peripheral\"", object : IOperationCallback {
                        override fun operationExecuted(result: Int, resultStr: String?)
                        {
                            if (resultStr != null)
                            {
                                Log.v(TAG, "_bluetoothRole : Peripheral  $resultStr ")
                                setBluetoothClassicEnable(bluetoothClassicEnable, bluetoothPower)
                            }
                            else
                            {
                                readyCallback?.operationExecuted(-1, resultStr)
                            }
                        }
                    })
                }
                else
                {
                    setBluetoothClassicEnable(bluetoothClassicEnable, bluetoothPower)
                }
                return
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
        readyCallback?.operationExecuted(-1, resultStr)
    }
}
