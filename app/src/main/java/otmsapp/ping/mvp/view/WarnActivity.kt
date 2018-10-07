package otmsapp.ping.mvp.view

import android.app.Activity
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
import otmsapp.ping.tools.StrUtil

/**
 * Created by Leeping on 2018/10/7.
 * email: 793065165@qq.com
 */
class WarnActivity : Activity(), WarnContract.View {

    private var adapter: WarnListAdapter? = null
    private val presenter = WarnPresenter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_warn)
        tv_name.text = "预警信息"
        adapter = WarnListAdapter(this)
        lv_content.adapter = adapter
        lv_content.setOnItemClickListener { parent, view, position, id ->
            //弹出处理预警信息提示框
            val warn = adapter?.getItem(position)
            IO.run { presenter.removeData(warn) }
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

    /**
     * 打开进度条
     */
    override fun showProgressBar() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * 关闭进度条
     */
    override fun hindProgressBar() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * 打印消息
     */
    override fun toast(message: String?) {
        runOnUiThread(Toast.makeText(this@WarnActivity, message, Toast.LENGTH_SHORT)::show)
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