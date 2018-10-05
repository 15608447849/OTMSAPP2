package otmsapp.ping.mvp.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import otmsapp.ping.R
import otmsapp.ping.mvp.presenter.LoginPresenter
import otmsapp.ping.mvp.contract.LoginContract
import kotlinx.android.synthetic.main.act_login.*
import otmsapp.ping.entitys.IO
import otmsapp.ping.tools.AppUtil
import otmsapp.ping.tools.StrUtil
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.text.Spanned
import otmsapp.ping.tools.ProgressFactory


class LoginActivity: Activity(), LoginContract.View , View.OnClickListener {

    private val presenter = LoginPresenter()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        tv_version.text = StrUtil.format("${AppUtil.getVersionName(this)}-${AppUtil.getVersionCode(this)}-${AppUtil.getCpuType(this)}")
        et_phone.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))
        et_phone.keyListener = DigitsKeyListener.getInstance("0123456789")
        et_password.filters = arrayOf<InputFilter>(object : InputFilter{
            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                for (i in start until end) {
                    // 只允许输入字母/数字
                    if (!Character.isLetterOrDigit(source!![i])) {
                        return ""
                    }
                }
                return null
            }
        }) ;
        btn_login.setOnClickListener(this)
        presenter.bindView(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.tryLogin()
    }

    override fun onDestroy() {
        presenter.unbindView()
        progressDialog?.dismiss()
        super.onDestroy()
    }

    override fun showProgressBar() {
        runOnUiThread {
            if (progressDialog==null) progressDialog = ProgressFactory.createSimpleDialog(this,"正在登陆中,请稍等片刻...");
            progressDialog?.show()
        }
    }

    override fun hindProgressBar() {
        runOnUiThread { progressDialog?.hide() }
    }

    override fun toast(message: String?) {
        runOnUiThread { AppUtil.toast(this@LoginActivity, message!!) }
    }

    override fun onClick(view: View) {
        AppUtil.hideSoftInputFromWindow(this)
        val phone = et_phone.text.toString()
        val password = et_password.text.toString()
        IO.run { presenter.login(phone, password) }
    }

    override fun onLogin() {
           startActivity(Intent(this@LoginActivity,DispatchActivity::class.java))
           finish()
    }
}