package jp.osdn.gokigen.thetathoughtshutter.brainwave

interface IBrainwaveDataReceiver
{
    fun receivedRawData(value: Int)
    fun receivedSummaryData(data: ByteArray?)
}
