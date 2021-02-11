package jp.osdn.gokigen.thetathoughtshutter.theta.operation

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.utils.SimpleHttpClient

class ThetaMovieRecordingControl(private val executeUrl : String = "http://192.168.1.1")
{
    private val httpClient = SimpleHttpClient()
    private var isCapturing = false

    fun movieControl()
    {
        try
        {
            if (!(isCapturing))
            {
                startCapture()
            }
            else
            {
                stopCapture()
            }
            isCapturing = !isCapturing
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun startCapture()
    {
        Log.v(TAG, "startCapture()")
        try
        {
            val thread = Thread {
                try
                {
                    val shootUrl = "${executeUrl}/osc/commands/execute"
                    val postData = "{\"name\":\"camera.startCapture\",\"parameters\":{\"timeout\":0}}"

                    Log.v(TAG, " start Capture : $postData")
                    val result: String? = httpClient.httpPostWithHeader(shootUrl, postData, null, "application/json;charset=utf-8", timeoutMs)
                    if ((result != null)&&(result.isNotEmpty()))
                    {
                        Log.v(TAG, " startCapture() : $result")
                    }
                    else
                    {
                        Log.v(TAG, "startCapture() reply is null.  $postData  (${shootUrl})")
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun stopCapture()
    {
        Log.v(TAG, "stopCapture()")
        try
        {
            val thread = Thread {
                try
                {
                    val shootUrl = "${executeUrl}/osc/commands/execute"
                    val postData = "{\"name\":\"camera.stopCapture\",\"parameters\":{\"timeout\":0}}"

                    Log.v(TAG, " stop Capture : $postData")

                    val result: String? = httpClient.httpPostWithHeader(shootUrl, postData, null, "application/json;charset=utf-8", timeoutMs)
                    if ((result != null)&&(result.isNotEmpty()))
                    {
                        Log.v(TAG, " stopCapture() : $result")
                    }
                    else
                    {
                        Log.v(TAG, "stopCapture() reply is null. $postData  (${shootUrl})")
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = ThetaMovieRecordingControl::class.java.simpleName
        private const val timeoutMs = 6000
    }
}