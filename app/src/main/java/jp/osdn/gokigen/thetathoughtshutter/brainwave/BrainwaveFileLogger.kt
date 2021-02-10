package jp.osdn.gokigen.thetathoughtshutter.brainwave

import android.os.Environment
import jp.osdn.gokigen.thetathoughtshutter.utils.SimpleLogDumper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class BrainwaveFileLogger(prefix : String = "TTShut", private val loggingSize : Int = 36)
{
    private var outputStream: FileOutputStream? = null
    init
    {
        try
        {
            val fileNamePrefix = "${prefix}_EEG"
            val calendar: Calendar = Calendar.getInstance()
            val extendName: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(calendar.time)
            @Suppress("DEPRECATION") val directoryPath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path.toString() + "/"
            val outputFileName = fileNamePrefix + "_" + extendName + ".bin"
            val filepath: String = File(directoryPath.toLowerCase(Locale.getDefault()), outputFileName.toLowerCase(Locale.getDefault())).path
            outputStream = FileOutputStream(filepath)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun outputSummaryData(data: ByteArray)
    {
        try
        {
            SimpleLogDumper.dumpBytes("RX [" + data.size + "] ", data)
            if (outputStream != null)
            {
                if (data.size >= loggingSize)
                {
                    outputStream?.write(data, 0, loggingSize)
                    outputStream?.flush()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}