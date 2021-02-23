package jp.osdn.gokigen.thetathoughtshutter.utils

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object SimpleLogDumper
{
    private val TAG = SimpleLogDumper::class.java.simpleName

    /**
     * デバッグ用：ログにバイト列を出力する
     *
     */
    fun dumpBytes(header: String, data: ByteArray?)
    {
        if (data == null)
        {
            Log.v(TAG, "DATA IS NULL")
            return
        }
        if (data.size > 8192)
        {
            Log.v(TAG, " --- DUMP DATA IS TOO LONG... " + data.size + " bytes.")
            return
        }
        var index = 0
        var message: StringBuffer
        message = StringBuffer()
        for (item in data)
        {
            index++
            message.append(String.format("%02x ", item))
            if (index >= 16)
            {
                Log.v(TAG, "$header $message")
                index = 0
                message = StringBuffer()
            }
        }
        if (index != 0)
        {
            Log.v(TAG, "$header $message")
        }
        System.gc()
    }

    fun binaryOutputToFile(fileNamePrefix: String, rx_body: ByteArray)
    {
        try
        {
            val calendar = Calendar.getInstance()
            val extendName = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(calendar.time)
            @Suppress("DEPRECATION") val directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/"
            val outputFileName = fileNamePrefix + "_" + extendName + ".bin"
            val filepath = File(directoryPath.toLowerCase(Locale.ROOT), outputFileName.toLowerCase(Locale.ROOT)).path
            val outputStream = FileOutputStream(filepath)
            outputStream.write(rx_body, 0, rx_body.size)
            outputStream.flush()
            outputStream.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}
