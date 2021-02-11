package jp.osdn.gokigen.thetathoughtshutter.theta.operation

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.utils.SimpleHttpClient

class ThetaOptionGetControl(private val executeUrl : String = "http://192.168.1.1")
{
    private val httpClient = SimpleHttpClient()

    /**
     *
     *
     */
    fun getOptions(options: String, callBack: IOperationCallback? = null)
    {
        //Log.v(TAG, "getOptions()  MSG : $options")
        try
        {
            val thread = Thread {
                try
                {
                    val setOptionsUrl = "${executeUrl}/osc/commands/execute"
                    val postData = "{\"name\":\"camera.getOptions\",\"parameters\":{\"timeout\":0, \"optionNames\": {$options}}}"
                    val result: String? = httpClient.httpPostWithHeader(setOptionsUrl, postData, null, "application/json;charset=utf-8", timeoutMs)
                    if ((result != null) && (result.isNotEmpty()))
                    {
                        Log.v(TAG, " getOptions() : $result (${setOptionsUrl})")
                        callBack?.operationExecuted(0, result)
                    }
                    else
                    {
                        Log.v(TAG, "getOptions() reply is null or empty.  $postData (${setOptionsUrl})")
                        callBack?.operationExecuted(-1, "")
                    }
                }
                catch (e: Exception)
                {
                    Log.v(TAG, "getOptions() Exception : $options")
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
        private val TAG = ThetaOptionGetControl::class.java.simpleName
        private const val timeoutMs = 1500
    }

}