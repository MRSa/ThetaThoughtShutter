package jp.osdn.gokigen.thetathoughtshutter

data class MyApplicationStatus(val defaultStatus : Status = Status.Undefined)
{
    var status: Status = Status.Undefined

    enum class Status
    {
        Undefined,
        FailedInitialize,
        Initialized,
        Searching,
        Connected,
        Scanning,
    }
}
