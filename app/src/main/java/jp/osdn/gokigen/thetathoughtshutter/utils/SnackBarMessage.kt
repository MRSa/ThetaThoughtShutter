package jp.osdn.gokigen.thetathoughtshutter.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import jp.osdn.gokigen.thetathoughtshutter.R

class SnackBarMessage(private val context: Activity, private val isToast: Boolean)
{
    private val TAG = toString()
    fun showMessage(message: String?)
    {
        try
        {
            Log.v(TAG, message!!)
            context.runOnUiThread {
                try
                {
                    if (!isToast)
                    {
                        // Snackbarでメッセージを通知する
                        Snackbar.make(context.findViewById(R.id.main_layout), message, Snackbar.LENGTH_LONG).show()
                    }
                    else
                    {
                        // Toastでメッセージを通知する
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun showMessage(stringId: Int)
    {
        try
        {
            showMessage(context.getString(stringId))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}
