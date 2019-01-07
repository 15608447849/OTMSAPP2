package ping.otmsapp.mvp.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.act_dispatch.*
import kotlinx.android.synthetic.main.inc_dispatch_button.*
import kotlinx.android.synthetic.main.inc_dispatch_tab.*
import kotlinx.android.synthetic.main.inc_input_code.*
import ping.otmsapp.R
import ping.otmsapp.adapter.DispatchListAdapter
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.UserInfo
import ping.otmsapp.entitys.dispatch.Box
import ping.otmsapp.entitys.dispatch.Dispatch
import ping.otmsapp.entitys.scanner.ScannerCallback
import ping.otmsapp.entitys.upload.BillImage
import ping.otmsapp.log.LLog
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.DispatchContract
import ping.otmsapp.mvp.contract.MenuContract
import ping.otmsapp.mvp.presenter.DispatchPresenter
import ping.otmsapp.mvp.presenter.MenuPresenter
import ping.otmsapp.tools.AppUtil
import ping.otmsapp.tools.DialogUtil
import ping.otmsapp.tools.MediaUse
import ping.otmsapp.tools.StrUtil
import java.io.File

/**
 * 调度页面
 */
class DispatchActivity: ViewBaseImp<DispatchPresenter>(), RadioGroup.OnCheckedChangeListener,  ScannerCallback, DispatchContract.View {

    private var adapter:DispatchListAdapter? = null

    private var menuPopWindow: MenuContract.View? = null

    private val menuPresenter = MenuPresenter(this)

    private var mediaUse:MediaUse? = null

    private var tempFile : File? = null
    private var billImage : BillImage? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_dispatch)
        //点击监听
        rl_drop_down_check.setOnClickListener {
            //展开操作区
            rl_drop_down_check.visibility = View.GONE
            rl_dispatch_area.visibility = View.VISIBLE
            rl_pull_up_check.visibility = View.VISIBLE
        }
        rl_pull_up_check.setOnClickListener {
            //收起操作区
            AppUtil.hideSoftInputFromWindow(this@DispatchActivity)
            rl_drop_down_check.visibility = View.VISIBLE
            rl_dispatch_area.visibility = View.GONE
            rl_pull_up_check.visibility = View.GONE
        }
        btn_code_sure.setOnClickListener {
            //二维码手动输入
            AppUtil.hideSoftInputFromWindow(this@DispatchActivity)
            val str = et_code_input.text.toString()
            if (StrUtil.validate(str)){
                onScanner(str)
            }
            et_code_input.setText("");
        }
        btn_take_out.setOnClickListener {
            //开始行程
            IO.pool{ presenter.take(); }
        }
        btn_ake_back.setOnClickListener {
            //已返回仓库
            IO.pool{ presenter.back(); }
        }
        btn_load_all.setOnClickListener {
            //装载全部
            IO.pool{ presenter.loadALL(); }
        }
        btn_abnormal.setOnClickListener {
            //提交货差
            IO.pool{ presenter.unloadAbnormal(adapter!!.index); }
        }
        btn_add_recycle.setOnClickListener {
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

        adapter?.setCallback{ view,position ->
            when ( view.id ){
                R.id.iv_checkbox -> checkBoxClick(position)
                R.id.btn_upload_receipt -> uploadReceipt(position);
            }

        }

        lv_content.adapter = adapter

        lv_content.setOnItemClickListener { parent, view, position, id ->
            showDetailDialog(position)
        }
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



    //上传回单
    private fun uploadReceipt(position: Int) {
        try {
            //点击上传图片
            tempFile = File(Environment.getExternalStorageDirectory(), System.currentTimeMillis().toString() + ".png")
            if (!tempFile!!.exists()) {
                if (!tempFile!!.createNewFile()) {
                    return
                }
            }
            billImage = BillImage()
            billImage?.path = tempFile?.canonicalPath
            billImage?.storeId = adapter?.getItem(position)?.customerAgency
            openSelectPictureDialog()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //打开图片选择窗口
    private fun openSelectPictureDialog() {
        val items = arrayOf<CharSequence>("相册", "拍照")
        DialogUtil.createSimpleListDialog(this, "请选择发票图片", items, true) { dialog, which ->

            val intent = Intent()

            when (which) {
                0 -> { //打开相册
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    startActivityForResult(intent, 100)
                }
                1 -> { //拍照
                    intent.action = MediaStore.ACTION_IMAGE_CAPTURE
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    val uri = Uri.fromFile(tempFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    startActivityForResult(intent, 200)
                }
            }
        }
    }

    //图片选择回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != RESULT_OK) {
            toast("图片不可用")
            return
        }
        IO.pool {

            try {
                if (requestCode == 100) { //相册选择
                    val uri = data?.data

                    val cr = this.contentResolver
                    val input = cr.openInputStream(uri);
                    val bitmap = BitmapFactory.decodeStream(input)
                    if (AppUtil.bitmap2File(bitmap,tempFile)){
                        previewPictures(bitmap)
                    }
                } else if (requestCode == 200) { //拍照
                    val bitmap = BitmapFactory.decodeStream(tempFile?.inputStream())
                    previewPictures(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //预览图片
    private fun previewPictures(bitmap: Bitmap) {
        runOnUiThread {
            val inflater = LayoutInflater.from(this)
            val v = inflater.inflate(R.layout.dialog_cost_upload_image, null)

            val iv = v.findViewById(R.id.iv_preview) as ImageView
            val upload = v.findViewById(R.id.btn_upload) as Button
            val cancel = v.findViewById(R.id.btn_cancel) as Button

            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            val dialog = builder.create();
            dialog.setView(v) //设置弹窗布局
            dialog.show()

//            dialog.window.setContentView(v);
            iv.setImageBitmap(bitmap) //图片显示

                val onClickListener = View.OnClickListener {
                    dialog.dismiss()
                    iv.setImageBitmap(null)
                    bitmap.recycle()
                    if (it.id == upload.id){
                        //上传图片
                        IO.pool {
                            //图片压缩
                            AppUtil.imageCompression(this@DispatchActivity,tempFile,1024)
                            presenter.uploadBillImage(billImage)
                            tempFile = null
                            billImage = null
                        }

                }

            }
            cancel.setOnClickListener(onClickListener)
            upload.setOnClickListener(onClickListener)

        }
    }

    //显示箱号详情并选中
    private fun showDetailDialog(position: Int) {
        val dispatch = adapter?.dispatch

        val store = dispatch!!.storeList!![position]

        val boxList = store?.boxList
        val list = arrayListOf<String>()
        //等待装货箱号
        for (i in boxList!!.indices) {
            val box = boxList[i]
            if (dispatch.state == Dispatch.STATE.LOAD && box.state == Box.STATE.LOAD){
                list.add(box.barCode)
            }else if (dispatch.state == Dispatch.STATE.UNLOAD && box.state == Box.STATE.UNLOAD){
                list.add(box.barCode)
            }
        }

        val boxListStrArray = arrayOfNulls<CharSequence>(list.size)

        DialogUtil.createSimpleListDialog(
                this@DispatchActivity,
                store.detailedAddress,
                list.toArray(boxListStrArray),
                true
                ){  dialog, which ->
            DialogUtil.dialogSimple2(this@DispatchActivity,
                    "手动操作扫描箱号: "+ list[which],
                    "确定"
            ) { IO.pool { onScanner(list[which]) } }
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
            //LLog.print("更新调度单\n"+ JsonUtil.javaBeanToJson(adapter?.dispatch))
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
    fun checkBoxClick( position:Int) {
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