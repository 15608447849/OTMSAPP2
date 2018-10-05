package otmsapp.ping.mvp.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.act_dispatch.*
import kotlinx.android.synthetic.main.inc_dispatch_button.*
import kotlinx.android.synthetic.main.inc_dispatch_tab.*
import kotlinx.android.synthetic.main.inc_input_code.*
import otmsapp.ping.R
import otmsapp.ping.adapter.DispatchListAdapter
import otmsapp.ping.entitys.IO
import otmsapp.ping.entitys.action.ClickManager
import otmsapp.ping.entitys.dispatch.Dispatch
import otmsapp.ping.entitys.scanner.ScannerApiThread
import otmsapp.ping.entitys.scanner.ScannerCallback
import otmsapp.ping.entitys.scanner.ScannerApi_SEUIC
import otmsapp.ping.log.LLog
import otmsapp.ping.mvp.presenter.DispatchPresenter
import otmsapp.ping.mvp.contract.DispatchContract
import otmsapp.ping.tools.AppUtil
import otmsapp.ping.tools.DialogUtil
import otmsapp.ping.tools.JsonUti
import otmsapp.ping.tools.StrUtil

/**
 * 调度页面
 */
class DispatchActivity: Activity(), RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener, ScannerCallback, DispatchContract.View {



    private var adapter:DispatchListAdapter? = null

    private val click = ClickManager()

    private val presenter  = DispatchPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_dispatch)
        //点击监听
        click.addNode(rl_drop_down_check){
            //展开操作区
            rl_drop_down_check.visibility = View.GONE
            rl_dispatch_area.visibility = View.VISIBLE
            rl_pull_up_check.visibility = View.VISIBLE
        }.addNode(rl_pull_up_check){
            //收起操作区
            AppUtil.hideSoftInputFromWindow(this@DispatchActivity)
            rl_drop_down_check.visibility = View.VISIBLE
            rl_dispatch_area.visibility = View.GONE
            rl_pull_up_check.visibility = View.GONE
        }.addNode(btn_code_sure){
            //二维码手动输入
            AppUtil.hideSoftInputFromWindow(this@DispatchActivity)
            val str = et_code_input.text.toString()
            if (StrUtil.validate(str)){
                onScanner(str)
            }
            et_code_input.setText("");
        }.addNode(btn_take_out){
            //开始行程
            IO.run{ presenter.take(); }
        }.addNode(btn_ake_back){
            //已返回仓库
            IO.run{ presenter.back(); }
        }.addNode(btn_load_all){
            //装载全部
            IO.run{ presenter.loadALL(); }
        }.addNode(btn_abnormal){
            //提交货差
            IO.run{ presenter.unloadAbnormal(adapter!!.index); }
        }.addNode(btn_add_recycle){
            if (adapter?.dispatch!=null && adapter?.dispatch?.state!! >= Dispatch.STATE.UNLOAD){
                //打开回收列表
                val intent = Intent(this@DispatchActivity,RecycleActivity::class.java)
                intent.putExtra("index",adapter?.index)
                startActivity(intent)
            }else{
                DialogUtil.dialogSimple(this@DispatchActivity,"不可进行回收操作,请确认正在运输途中","好的,知道了",null)
            }
        }

        tv_state.isSelected = true
        //tab选中监听
        rg_tab.setOnCheckedChangeListener(this)
        //列表适配器
        adapter= DispatchListAdapter( this)
        lv_content.adapter = adapter
        lv_content.setOnItemClickListener(this)
        //默认选中
        rbtn_load.toggle()
    }

    override fun onResume() {
        super.onResume()
        presenter.bindView(this)
        IO.run{presenter.validateDispatch()}
    }

    override fun onPause() {
        presenter.unbindView()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra("message") == "dispatch"){
            updateDispatch()
        }
    }



    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        LLog.print("选中tab view,id: "+ checkedId)
        adapter?.tabType = when(checkedId){
            rbtn_load.id -> 1
            rbtn_unload.id  -> 2
            else -> 0
        }
        updateDispatch()
    }
    //更新调度单信息
    override fun updateDispatch() {
        runOnUiThread{

            adapter?.dispatch = Dispatch().fetch()
            adapter?.notifyDataSetChanged()
            LLog.print("更新调度单\n"+ JsonUti.javaBeanToJson(adapter?.dispatch))
            tv_state.text = when(adapter?.dispatch?.state){
                Dispatch.STATE.LOAD -> "装载总进度:[${adapter?.dispatch?.loadScanBoxIndex}/${adapter?.dispatch?.storeBoxSum}]"
                Dispatch.STATE.TAKEOUT -> "等待启程出发"
                Dispatch.STATE.UNLOAD -> "卸货总进度:[${adapter?.dispatch?.unloadScanBoxIndex}/${adapter?.dispatch?.storeBoxSum}]"
                Dispatch.STATE.BACK -> "等待返回仓库"
                Dispatch.STATE.COMPLETE -> "后台同步数据"
                else -> "暂无调度作业"
            }
        }

    }

    override fun onItemClick(viewGroup: AdapterView<*>?, view: View?, position:Int, positionLong: Long) {
        LLog.print("列表子项view,id: "+ view)
        if(adapter?.index == position){
            adapter?.index = -1
        } else{
            adapter?.index = position
        }
        adapter?.notifyDataSetChanged()
    }

    override fun onScanner(codeBar: String?) {
        IO.run{
            presenter.codeBarHandle(codeBar,adapter?.tabType!!, adapter?.index!!)
        }
    }

    override fun showProgressBar() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hindProgressBar() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toast(message: String?) {
        runOnUiThread { AppUtil.toast(this@DispatchActivity, message!!) }
    }

    override fun dialog(btnName:String?,message: String?, callback: DispatchContract.Presenter.Callback?) {
        runOnUiThread {

            DialogUtil.dialogSimple2(this, message,btnName,object : DialogUtil.Action0{
                override fun onAction0() {
                    IO.run {callback?.onCallback()}
                }
            },
                    "取消",null)
        }
    }



}