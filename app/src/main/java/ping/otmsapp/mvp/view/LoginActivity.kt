package ping.otmsapp.mvp.view

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.act_login.*
import ping.otmsapp.R
import ping.otmsapp.entitys.DefaultVersionUpImp
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.LogsUploader
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.LoginContract
import ping.otmsapp.mvp.presenter.LoginPresenter
import ping.otmsapp.tools.AppUtil
import ping.otmsapp.tools.LeeApplicationAbs
import ping.otmsapp.tools.PermissionApply
import ping.otmsapp.tools.StrUtil
import ping.otmsapp.zerocice.IceHelper


class LoginActivity: ViewBaseImp<LoginPresenter>(), LoginContract.View , View.OnClickListener,PermissionApply.Callback {
    //角色码
    var role:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_login)
        tv_version.text = StrUtil.format("${AppUtil.getVersionName(this)} v${AppUtil.getVersionCode(this)}") //版本信息
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

        val array = IceHelper.get().obtainParamToSharedPreference(this)

        tag.setText(array[0])
        address.setText(array[1])
        port.setText(array[2])

        cancel.setOnClickListener { dialog.dismiss()  }

        save.setOnClickListener{
            dialog.dismiss()
            val tagStr = tag.text.toString()
            val addressStr = address.text.toString()
            val portStr = port.text.toString()
            IceHelper.get().saveParamToSharedPreference(this,tagStr,addressStr,portStr.toInt())
            //重启应用
            val i = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            (application as? LeeApplicationAbs)?.killAllProcess(true)
        }

    }



    override fun onResume() {
        super.onResume()
        IO.pool(DefaultVersionUpImp(this)) //检测升级
        IO.pool(LogsUploader()) //日志上传
        presenter.tryLogin()
    }


    override fun onClick(view: View) {
        AppUtil.hideSoftInputFromWindow(this)
        val phone = et_phone.text.toString()
        val password = et_password.text.toString()
        IO.pool { presenter.login(phone, password) }
    }


    private val permissionArray = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 写sd卡
            Manifest.permission.CAMERA, // 相机和闪光灯
            Manifest.permission.ACCESS_FINE_LOCATION, //GPS
            Manifest.permission.ACCESS_COARSE_LOCATION, //NET LOCATION
            Manifest.permission.READ_PHONE_STATE //获取手机信息
    )

    private val permissionApply = PermissionApply(this, permissionArray, this);

    override fun onLogin(role: Long) {
            this.role = role;
            if (android.os.Build.VERSION.SDK_INT >= 23){
                permissionApply.permissionCheck()
            }else{
                loginSuccess()
            }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionApply.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        permissionApply.onActivityResult(requestCode, resultCode, data)
    }
    override fun onPermissionsGranted() {
        if (permissionApply.isIgnoreBatteryOption){
            loginSuccess()
        }
    }
    override fun onPowerIgnoreGranted() {
        loginSuccess()
    }
    fun loginSuccess(){
        if (role == 2L){
            startActivity(Intent(this,DispatchActivity::class.java))
        }else{
            startActivity(Intent(this,SingeRecycleActivity::class.java))
        }
        finish()
    }
}