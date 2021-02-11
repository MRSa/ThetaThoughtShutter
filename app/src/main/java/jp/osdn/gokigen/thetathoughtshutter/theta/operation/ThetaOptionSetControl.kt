package jp.osdn.gokigen.thetathoughtshutter.theta.operation

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.utils.SimpleHttpClient

class ThetaOptionSetControl(private val executeUrl : String = "http://192.168.1.1")
{
    private val httpClient = SimpleHttpClient()

    /**
     *
     *
     */
    fun setOptions(options: String, callBack: IOperationCallback? = null)
    {
        //Log.v(TAG, "setOptions()  MSG : $options")
        try
        {
            val thread = Thread {
                try
                {
                    val setOptionsUrl = "${executeUrl}/osc/commands/execute"
                    val postData = "{\"name\":\"camera.setOptions\",\"parameters\":{\"timeout\":0, \"options\": {$options}}}"
                    val result: String? = httpClient.httpPostWithHeader(setOptionsUrl, postData, null, "application/json;charset=utf-8", timeoutMs)
                    if ((result != null) && (result.isNotEmpty()))
                    {
                        Log.v(TAG, " setOptions() : $result (${setOptionsUrl})")
                        callBack?.operationExecuted(0, result)
                    }
                    else
                    {
                        Log.v(TAG, "setOptions() reply is null or empty.  $postData (${setOptionsUrl})")
                        callBack?.operationExecuted(-1, "")
                    }
                }
                catch (e: Exception)
                {
                    Log.v(TAG, "setOptions() Exception : $options")
                    e.printStackTrace()
                    callBack?.operationExecuted(-1, e.localizedMessage)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callBack?.operationExecuted(-1, e.localizedMessage)
        }
    }

    companion object
    {
        private val TAG = ThetaOptionSetControl::class.java.simpleName
        private const val timeoutMs = 1500
    }
}
