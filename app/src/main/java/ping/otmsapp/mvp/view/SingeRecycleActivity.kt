package ping.otmsapp.mvp.view

import android.os.Bundle
import kotlinx.android.synthetic.main.act_singe_recycle.*
import ping.otmsapp.R
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.scanner.ScannerCallback
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.SingeRecycleContract
import ping.otmsapp.mvp.presenter.SingeRecyclePresenter
import ping.otmsapp.tools.MediaUse

/**
 * Created by Leeping on 2019/3/15.
 * email: 793065165@qq.com
 */
class SingeRecycleActivity : ViewBaseImp<SingeRecyclePresenter>(), SingeRecycleContract.View , ScannerCallback{

    //媒体
    private var mediaUse:MediaUse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_singe_recycle)

        tv_btn_close.setOnClickListener{
           presenter.logout(this)
        }
        btn_code_upload.setOnClickListener{
            IO.pool {
                presenter.upload()
            }
        }
        mediaUse = MediaUse(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onDestroy() {
        mediaUse?.destroy()
        super.onDestroy()
    }

    override fun setInfo(name: String?, code: String?) {
        tv_user_name.setText("当前用户: " + name)
        tv_user_code.setText("用户编码: " + code)
    }

    override fun uploadNumber(num: String?) {
        runOnUiThread { tv_box_num.setText(num) }
    }
    /**
     * 扫码结果回调
     */
    override fun onScanner(codeBar: String?) {
        runOnUiThread {
            mediaUse!!.play(R.raw.recycle)
        }
        IO.pool{
            presenter.scanHandle(codeBar)
        }
    }
}