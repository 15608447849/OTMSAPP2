package otmsapp.ping.mvp.view

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.act_warn.*
import kotlinx.android.synthetic.main.inc_back_title.*
import otmsapp.ping.R
import otmsapp.ping.adapter.WarnListAdapter
import otmsapp.ping.entitys.IO
import otmsapp.ping.entitys.warn.WarnItem
import otmsapp.ping.mvp.contract.WarnContract
import otmsapp.ping.mvp.presenter.WarnPresenter
import otmsapp.ping.tools.DialogUtil
import otmsapp.ping.tools.ProgressFactory
import otmsapp.ping.tools.StrUtil

/**
 * Created by Leeping on 2018/10/7.
 * email: 793065165@qq.com
 */
class WarnActivity : Activity(), WarnContract.View {
    private var progressDialog: ProgressDialog? = null
    private var adapter: WarnListAdapter? = null
    private val presenter = WarnPresenter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_warn)
        iv_back.setOnClickListener{
            finish()
        }
        tv_name.text = "预警信息"
        adapter = WarnListAdapter(this)
        lv_content.adapter = adapter
        lv_content.setOnItemClickListener { parent, view, position, id ->
            //弹出处理预警信息提示框
            val warn = adapter?.getItem(position)
            DialogUtil.dialogSimple2(this@WarnActivity,"箱号:${warn?.code}\t${warn?.value}\n确定处理将删除本条记录","处理完成"){
                IO.run { presenter.removeData(warn) }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        presenter.bindView(this)
        IO.run(presenter::updateData)
    }

    override fun onPause() {
        super.onPause()
        presenter.unbindView()
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        super.onDestroy()
    }
    /**
     * 打开进度条
     */
    override fun showProgressBar() {
        runOnUiThread {
            if (progressDialog == null) progressDialog = ProgressFactory.createSimpleDialog(this, "正在处理预警信息...");
            progressDialog?.show()
        }
    }

    /**
     * 关闭进度条
     */
    override fun hindProgressBar() {
        runOnUiThread { progressDialog?.hide() }
    }

    /**
     * 打印消息
     */
    override fun toast(message: String?) {
        runOnUiThread{
            Toast.makeText(this@WarnActivity, message, Toast.LENGTH_SHORT).show()
        }
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