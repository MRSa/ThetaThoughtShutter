package jp.osdn.gokigen.thetathoughtshutter.brainwave

import android.util.Log
import jp.osdn.gokigen.thetathoughtshutter.bluetooth.connection.eeg.MindWaveConnection
import java.util.*

class BrainwaveDataHolder(maxBufferSize: Int = 16000) : IBrainwaveDataReceiver
{
    companion object
    {
        private val TAG = BrainwaveDataHolder::class.java.simpleName
    }

    private var valueBuffer: IntArray
    private var currentSummaryData = BrainwaveSummaryData()
    private var maxBufferSize = 0
    private var currentPosition = 0
    private var bufferIsFull = false

    init
    {
        this.maxBufferSize = maxBufferSize
        valueBuffer = IntArray(maxBufferSize)
    }

    override fun receivedRawData(value: Int)
    {
        //Log.v(TAG, " receivedRawData() : $value");
        try
        {
            valueBuffer[currentPosition] = value
            currentPosition++
            if (currentPosition == maxBufferSize)
            {
                currentPosition = 0
                bufferIsFull = true
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun receivedSummaryData(data: ByteArray?)
    {
        if (data != null)
        {
            Log.v(TAG, " receivedSummaryData() : ${data.size} bytes.")
            if (!currentSummaryData.update(data))
            {
                // parse failure...
                Log.v(TAG, " FAIL : PARSE EEG SUMMARY DATA (" + data.size + ")")
            }
        }
    }

    fun getSummaryData(): BrainwaveSummaryData
    {
        return currentSummaryData
    }

    fun getValues(size: Int): IntArray?
    {
        var replyData: IntArray? = null
        try {
            var endPosition = currentPosition - 1
            if (currentPosition > size) {
                return Arrays.copyOfRange(valueBuffer, endPosition - size, endPosition)
            }
            if (!bufferIsFull) {
                return Arrays.copyOfRange(valueBuffer, 0, endPosition)
            }
            if (currentPosition == 0) {
                endPosition = maxBufferSize - 1
                return Arrays.copyOfRange(valueBuffer, endPosition - size, endPosition)
            }
            val remainSize = size - (currentPosition - 1)
            val size0: IntArray = Arrays.copyOfRange(valueBuffer, 0, currentPosition - 1)
            val size1: IntArray = Arrays.copyOfRange(
                valueBuffer,
                maxBufferSize - 1 - remainSize, maxBufferSize - 1
            )
            replyData = IntArray(size)
            System.arraycopy(size1, 0, replyData, 0, size1.size)
            System.arraycopy(size0, 0, replyData, size1.size, size0.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return replyData
    }

}