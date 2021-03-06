package ping.otmsapp.mvp.basics

import android.app.ProgressDialog
import ping.otmsapp.tools.AppUtil
import ping.otmsapp.tools.DialogUtil

open class ViewBaseImp<P : IPresenter<*>?> : ViewBindPresenter<P>() {

    private var progressDialog: ProgressDialog? = null

    override fun showProgressBar() {
        runOnUiThread {
            if (progressDialog == null) progressDialog = DialogUtil.createSimpleProgressDialog(this, "正在执行中...");
            progressDialog?.show()
        }
    }

    override fun hindProgressBar() {
        runOnUiThread { progressDialog?.hide() }
    }

    override fun toast(message: String?) {
        runOnUiThread { AppUtil.toast(this, message!!) }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
        progressDialog  = null
    }

}