package otmsapp.ping.mvp.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import otmsapp.ping.R
import otmsapp.ping.mvp.contract.LoginContract
import kotlinx.android.synthetic.main.act_login.*
import otmsapp.ping.tools.AppUtil
import otmsapp.ping.tools.StrUtil
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.widget.Button
import otmsapp.ping.entitys.IO
import otmsapp.ping.mvp.basics.ViewBaseImp
import otmsapp.ping.mvp.presenter.LoginPresenter
import android.widget.EditText
import otmsapp.ping.entitys.DefaultVersionUpImp
import otmsapp.ping.tools.LeeApplicationAbs
import otmsapp.ping.zerocice.IceIo


class LoginActivity: ViewBaseImp<LoginPresenter>(), LoginContract.View , View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        tv_version.text = StrUtil.format("${AppUtil.getVersionName(this)}-${AppUtil.getVersionCode(this)}")
        et_phone.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))
        et_phone.keyListener = DigitsKeyListener.getInstance("0123456789")
        et_password.filters = arrayOf<InputFilter>(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                // 只允许输入字母/数字
                if (!Character.isLetterOrDigit(source!![i])) {
                    return@InputFilter ""
                }
            }
            null
        }) ;
        btn_login.setOnClickListener(this)

        iv_connect.setOnClickListener {
           openServerSetting()
        }
    }

    private fun openServerSetting() {
        val inflater = LayoutInflater.from(this)
        val v = inflater.inflate(R.layout.dialog_setting_server, null)

        val tag = v.findViewById(R.id.et_tag) as EditText
        val address = v.findViewById(R.id.et_address) as EditText
        val port = v.findViewById(R.id.et_port) as EditText

        val save = v.findViewById(R.id.btn_save) as Button
        val cancel = v.findViewById(R.id.btn_cancel) as Button

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        val dialog = builder.create();
        dialog.setView(v) //设置弹窗布局
        dialog.show()

        val array = IceIo.get().obtainParamToSharedPreference(this)

        tag.setText(array[0])
        address.setText(array[1])
        port.setText(array[2])

        cancel.setOnClickListener { dialog.dismiss()  }

        save.setOnClickListener{
            dialog.dismiss()
            val tagStr = tag.text.toString()
            val addressStr = address.text.toString()
            val portStr = port.text.toString()
            IceIo.get().saveParamToSharedPreference(this,tagStr,addressStr,portStr.toInt())

//            //重启应用
            val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            (application as? LeeApplicationAbs)?.killAllProcess(true)
        }

    }



    override fun onResume() {
        super.onResume()
        presenter.tryLogin()
    }


    override fun onClick(view: View) {
        AppUtil.hideSoftInputFromWindow(this)
        val phone = et_phone.text.toString()
        val password = et_password.text.toString()
        IO.run { presenter.login(phone, password) }
    }

    override fun onLogin() {
            startActivity(Intent(this,DispatchActivity::class.java))
            IO.run(DefaultVersionUpImp(this))
            finish()
    }
}