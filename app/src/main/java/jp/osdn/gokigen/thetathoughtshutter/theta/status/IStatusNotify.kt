package jp.osdn.gokigen.thetathoughtshutter.theta.status

interface IStatusNotify
{
    fun changedCaptureMode(captureMode : String)
    fun changedExposureProgram(exposureProgram : String)
}