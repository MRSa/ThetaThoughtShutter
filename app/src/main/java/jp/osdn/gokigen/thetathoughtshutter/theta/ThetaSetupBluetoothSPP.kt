package jp.osdn.gokigen.thetathoughtshutter.theta

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.IOperationCallback
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionGetControl
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionSetControl

class ThetaSetupBluetoothSPP(executeUrl : String = "http://192.168.1.1")
{
    private val getOption = ThetaOptionGetControl(executeUrl)
    private val setOption = ThetaOptionSetControl(executeUrl)

    fun setupBluetoothSPP()
    {
        getOption.getOptions("[ \"_bluetoothRole\", \"_bluetoothPower\", \"_bluetoothClassicEnable\" ]", object : IOperationCallback { override fun operationExecuted(result: Int, resultStr: String?)
        {
            Log.v(TAG, " optionSet.getOptions(Bluetooth) : $resultStr? ")
        }})
    }
    companion object
    {
        private val TAG = ThetaSetupBluetoothSPP::class.java.simpleName
    }
}
