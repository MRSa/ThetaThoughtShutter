package jp.osdn.gokigen.thetathoughtshutter.brainwave

import android.util.Log
import java.util.*

class BrainwaveDataHolder(private val receiver: IDetectSensingReceiver? = null, maxBufferSize: Int = 16000) : IBrainwaveDataReceiver
{
    companion object
    {
        private val TAG = BrainwaveDataHolder::class.java.simpleName
        private const val THRESHOLD_ATTENTION = 80
        private const val THRESHOLD_MEDIATION = 90
        private const val COUNT_CONTINUOUS_ATTENTION = 5
        private const val COUNT_CONTINUOUS_MEDIATION = 5
    }

    private var valueBuffer: IntArray
    private var currentSummaryData = BrainwaveSummaryData()
    private var maxBufferSize = 0
    private var currentPosition = 0
    private var bufferIsFull = false
    private var attentionCount = 0
    private var mediationCount = 0
    private var dataReceived = false

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
            // Log.v(TAG, " receivedSummaryData() : ${data.size} bytes.")
            if (!currentSummaryData.update(data))
            {
                // parse failure...
                Log.v(TAG, " FAIL : PARSE EEG SUMMARY DATA (" + data.size + ")")
                return
            }
            try
            {
                val attention = currentSummaryData.getAttention()
                val mediation = currentSummaryData.getMediation()
                Log.v(TAG, "  ATTENTION : $attention   MEDIATION : $mediation")
                if ((!dataReceived)&&(attention > 0)&&(mediation > 0))
                {
                    // データの受信を開始した
                    dataReceived = true
                    receiver?.startSensing()
                }

                if (attention > THRESHOLD_ATTENTION)
                {
                    if (attentionCount == 0)
                    {
                        receiver?.detectAttention()
                    }
                    attentionCount++
                    if (attentionCount == COUNT_CONTINUOUS_ATTENTION)
                    {
                        // 検出を通知する
                        receiver?.detectAttentionThreshold()
                    }
                }
                else
                {
                    // リミッターを下回ったので連続カウンタをクリアする
                    if( attentionCount > 0)
                    {
                        receiver?.lostAttention()
                    }
                    attentionCount = 0
                }

                if (mediation > THRESHOLD_MEDIATION)
                {
                    if (mediationCount == 0)
                    {
                        receiver?.detectMediation()
                    }
                    mediationCount++
                    if (mediationCount == COUNT_CONTINUOUS_MEDIATION)
                    {
                        // 検出を通知する
                        receiver?.detectMediationThreshold()
                    }
                }
                else
                {
                    // リミッターを下回ったので連続カウンタをクリアする
                    if( mediationCount > 0)
                    {
                        receiver?.lostMediation()
                    }
                    mediationCount = 0
                }
            }
            catch (e : Exception)
            {
                e.printStackTrace()
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