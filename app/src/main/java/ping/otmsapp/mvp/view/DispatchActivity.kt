package ping.otmsapp.mvp.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.act_dispatch.*
import kotlinx.android.synthetic.main.inc_dispatch_button.*
import kotlinx.android.synthetic.main.inc_dispatch_tab.*
import kotlinx.android.synthetic.main.inc_input_code.*
import ping.otmsapp.R
import ping.otmsapp.adapter.DispatchListAdapter
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.UserInfo
import ping.otmsapp.entitys.action.ClickManager
import ping.otmsapp.entitys.dispatch.Dispatch
import ping.otmsapp.entitys.scanner.ScannerCallback
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.DispatchContract
import ping.otmsapp.mvp.contract.MenuContract
import ping.otmsapp.mvp.presenter.DispatchPresenter
import ping.otmsapp.mvp.presenter.MenuPresenter
import ping.otmsapp.tools.*

/**
 * 调度页面
 */
class DispatchActivity: ViewBaseImp<DispatchPresenter>(), RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener, ScannerCallback, DispatchContract.View {

    private var adapter:DispatchListAdapter? = null

    private val click = ClickManager()

    private var menuPopWindow: MenuContract.View? = null

    private val menuPresenter = MenuPresenter(this)

    private var mediaUse:MediaUse? = null

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
            IO.pool{ presenter.take(); }
        }.addNode(btn_ake_back){
            //已返回仓库
            IO.pool{ presenter.back(); }
        }.addNode(btn_load_all){
            //装载全部
            IO.pool{ presenter.loadALL(); }
        }.addNode(btn_abnormal){
            //提交货差
            IO.pool{ presenter.unloadAbnormal(adapter!!.index); }
        }.addNode(btn_add_recycle){
            if (adapter?.dispatch!=null && adapter?.dispatch?.state!! >= Dispatch.STATE.UNLOAD){
                //打开回收列表
                val intent = Intent(this@DispatchActivity,RecycleActivity::class.java)
                intent.putExtra("index",adapter?.index)
                startActivity(intent)
            }else{
                toast("不可进行回收操作,请确认正在运输途中");
            }
        }

        tv_state.isSelected = true
        //tab选中监听
        rg_tab.setOnCheckedChangeListener(this)
        //列表适配器
        adapter= DispatchListAdapter( this)
        lv_content.adapter = adapter
        lv_content.onItemClickListener = this
        //默认选中
        rbtn_load.toggle()

        menuPopWindow = MenuPopWindow(this,rl_title)

        iv_menu.setOnClickListener {
            //弹出菜单pop
            menuPopWindow?.showWindows()
        }

        mediaUse = MediaUse(this)


        iv_logo.setOnClickListener {
            //弹出个人信息
            val inflater = LayoutInflater.from(this)
            val v = inflater.inflate(R.layout.dialog_userinfo, null)

            val name = v.findViewById(R.id.tv_user_name) as TextView
            val id = v.findViewById(R.id.tv_user_id) as TextView
            val phone = v.findViewById(R.id.tv_user_phone) as TextView
            val comp = v.findViewById(R.id.tv_user_comp) as TextView

            val builder = AlertDialog.Builder(this)
            val dialog = builder.create();
            dialog.setView(v) //设置弹窗布局
            dialog.show()

            val user = UserInfo().fetch<UserInfo>()

            name.text = user.name
            id.text = user.id.toString()
            phone.text = user.phone
            comp.text = user.compName

        }

    }

    override fun onResume() {
        super.onResume()
        menuPopWindow?.bindPresenter(menuPresenter)
        IO.pool{presenter.validateDispatch()}
    }

    override fun onPause() {
        super.onPause()
        menuPopWindow?.unbindPresenter()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getStringExtra("message") == "dispatch"){
            updateDispatch()
        }
    }

    override fun onDestroy() {
        mediaUse?.destroy()
        super.onDestroy()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        adapter?.tabType = when(checkedId){
            rbtn_load.id -> 1
            rbtn_unload.id  -> 2
            else -> 0
        }
        updateDispatch()
    }
    override fun resetListIndex() {
        adapter?.index = -1
    }

    //更新调度单信息
    override fun updateDispatch() {
        runOnUiThread{
            adapter?.dispatch = Dispatch().fetch()
            adapter?.notifyDataSetChanged()
            //LLog.print("更新调度单\n"+ JsonUti.javaBeanToJson(adapter?.dispatch))
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

    //列表子项点击
    override fun onItemClick(viewGroup: AdapterView<*>?, view: View?, position:Int, positionLong: Long) {
        if(adapter?.index == position){
            adapter?.index = -1
        } else{
            adapter?.index = position
        }
        adapter?.notifyDataSetChanged()
    }

    //接受扫码消息
    override fun onScanner(codeBar: String?) {
        IO.pool{
            presenter.codeBarHandle(codeBar,adapter?.tabType!!, adapter?.index!!)
        }
    }

    override fun toast(message: String?) {
        runOnUiThread {
            DialogUtil.dialogSimple(this, message,"知道了",null)
        }
    }

    override fun dialog(btnName:String?,message: String?, callback: DispatchContract.Presenter.Callback?) {
        runOnUiThread {
            DialogUtil.dialogSimple2(this, message,btnName) { IO.pool {callback?.onCallback()} }
        }
    }

    override fun playScanFailMusic() {
        runOnUiThread{
            mediaUse?.play(R.raw.fait)
        }
    }

    override fun playScanSuccessMusic() {
        runOnUiThread{
            mediaUse?.play(R.raw.success)
        }
    }
}