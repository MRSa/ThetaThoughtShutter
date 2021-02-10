package jp.osdn.gokigen.thetathoughtshutter.bluetooth

interface ITextDataUpdater
{
    fun setText(data: String?)
    fun addText(data: String?)
    fun showSnackBar(message: String?)
    fun showSnackBar(rscId: Int)
    fun enableOperation(isEnable: Boolean)
}
