package ping.otmsapp.mvp.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.act_warn.*
import kotlinx.android.synthetic.main.inc_back_title.*
import ping.otmsapp.R
import ping.otmsapp.adapter.WarnListAdapter
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.warn.WarnItem
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.WarnContract
import ping.otmsapp.mvp.presenter.WarnPresenter
import ping.otmsapp.tools.DialogUtil
import ping.otmsapp.tools.StrUtil

/**
 * Created by Leeping on 2018/10/7.
 * email: 793065165@qq.com
 */
class WarnActivity : ViewBaseImp<WarnPresenter>(), WarnContract.View {
    private var adapter: WarnListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_warn)
        iv_back.setOnClickListener{
            startActivity(Intent(this,DispatchActivity::class.java))
            finish()
        }
        tv_name.text = "预警信息"
        adapter = WarnListAdapter(this)
        adapter?.setCallback { view, position ->
            //弹出处理预警信息提示框
            val warn = adapter?.getItem(position)
            DialogUtil.dialogSimple2(this@WarnActivity,"箱号:${warn?.code}\t${warn?.value}\n确定处理将删除本条记录","处理完成"){
                IO.run { presenter.removeData(warn) }
            }
        }
        lv_content.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        IO.run(presenter::updateData)
    }

    override fun refreshList(warnItems: MutableList<WarnItem>?) {
        adapter?.data = warnItems
        runOnUiThread {
            adapter?.notifyDataSetChanged()
            tv_name_sub.text = StrUtil.format("总预警数:%d",adapter?.data?.size)
        }
    }

    override fun dialogHandler(msg: String?, callback: WarnContract.Presenter.Callback?) {
        DialogUtil.dialogSimple2(this@WarnActivity,msg,"确定"){
            callback?.action()
        }
    }
}