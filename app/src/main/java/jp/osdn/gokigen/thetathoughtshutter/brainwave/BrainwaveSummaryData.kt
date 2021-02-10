package jp.osdn.gokigen.thetathoughtshutter.brainwave

import kotlin.experimental.and

class BrainwaveSummaryData
{
    //  3-byte value : delta (0.5 - 2.75Hz), theta (3.5 - 6.75Hz), low-alpha (7.5 - 9.25Hz), high-alpha (10 - 11.75Hz), low-beta (13 - 16.75Hz), high-beta (18 - 29.75Hz), low-gamma (31 - 39.75Hz), and mid-gamma (41 - 49.75Hz).
    private var delta = 0
    private var theta = 0
    private var lowAlpha = 0
    private var highAlpha = 0
    private var lowBeta = 0
    private var highBeta = 0
    private var lowGamma = 0
    private var midGamma = 0
    private var poorSignal = 0
    private var attention = 0
    private var mediation = 0

    fun update(packet: ByteArray): Boolean
    {
        var ret = false
        try
        {
            val length = packet.size
            if (length < 36)
            {
                return ret
            }
            poorSignal = packet[4].toInt()
            delta = (packet[7] and 0xff.toByte()) * 65536 + (packet[8] and 0xff.toByte()) * 256 + (packet[9] and 0xff.toByte())
            theta = (packet[10] and 0xff.toByte()) * 65536 + (packet[11] and 0xff.toByte()) * 256 + (packet[12] and 0xff.toByte())
            lowAlpha = (packet[13] and 0xff.toByte()) * 65536 + (packet[14] and 0xff.toByte()) * 256 + (packet[15] and 0xff.toByte())
            highAlpha = (packet[16] and 0xff.toByte()) * 65536 + (packet[17] and 0xff.toByte()) * 256 + (packet[18] and 0xff.toByte())
            lowBeta = (packet[19] and 0xff.toByte()) * 65536 + (packet[20] and 0xff.toByte()) * 256 + (packet[21] and 0xff.toByte())
            highBeta = (packet[22] and 0xff.toByte()) * 65536 + (packet[23] and 0xff.toByte()) * 256 + (packet[24] and 0xff.toByte())
            lowGamma = (packet[25] and 0xff.toByte()) * 65536 + (packet[26] and 0xff.toByte()) * 256 + (packet[27] and 0xff.toByte())
            midGamma = (packet[28] and 0xff.toByte()) * 65536 + (packet[29] and 0xff.toByte()) * 256 + (packet[30] and 0xff.toByte())
            attention = ((packet[32] and 0xff.toByte()).toInt())
            mediation = ((packet[34] and 0xff.toByte()).toInt())
            ret = true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ret
    }

    fun isSkinConnected(): Boolean
    {
        return poorSignal != 200
    }

    fun getPoorSignal(): Int
    {
        return poorSignal
    }

    fun getDelta(): Int
    {
        return delta
    }

    fun getTheta(): Int
    {
        return theta
    }

    fun getLowAlpha(): Int
    {
        return lowAlpha
    }

    fun getHighAlpha(): Int
    {
        return highAlpha
    }

    fun getLowBeta(): Int
    {
        return lowBeta
    }

    fun getHighBeta(): Int
    {
        return highBeta
    }

    fun getLowGamma(): Int
    {
        return lowGamma
    }

    fun getMidGamma(): Int
    {
        return midGamma
    }

    fun getAttention(): Int
    {
        return attention
    }

    fun getMediation(): Int
    {
        return mediation
    }
}