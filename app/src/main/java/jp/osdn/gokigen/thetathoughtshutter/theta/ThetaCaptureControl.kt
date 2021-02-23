package jp.osdn.gokigen.thetathoughtshutter.theta

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.IOperationCallback
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaOptionSetControl
import jp.osdn.gokigen.thetathoughtshutter.theta.operation.ThetaSingleShotControl
import java.lang.Exception

class ThetaCaptureControl(executeUrl : String = "http://192.168.1.1")
{
    private val setOption = ThetaOptionSetControl(executeUrl)
    private val singleShot = ThetaSingleShotControl(executeUrl)
    private var isCapturing = false

    companion object
    {
        private val TAG = ThetaCaptureControl::class.java.simpleName
    }

    fun setupCaptureMode()
    {
        setOption.setOptions("\"captureMode\" : \"image\"", object : IOperationCallback {
            override fun operationExecuted(result: Int, resultStr: String?)
            {
                if (resultStr != null)
                {
                    Log.v(TAG, "set to Image capture mode.:  $resultStr ")
                }
            }
        })
    }

    fun doCapture()
    {
        try
        {
            if (isCapturing)
            {
                Log.v(TAG, "ALREADY START CAPTURE SEQUENCE")
                return
            }
            singleShot.singleShot()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }
}