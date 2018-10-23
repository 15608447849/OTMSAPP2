package ping.otmsapp.mvp.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import ping.otmsapp.R
import kotlinx.android.synthetic.main.act_recycle.*
import kotlinx.android.synthetic.main.inc_back_title.*
import kotlinx.android.synthetic.main.inc_input_code.*
import ping.otmsapp.adapter.RecycleListAdapter
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.action.ClickManager
import ping.otmsapp.entitys.recycler.RecyclerBox
import ping.otmsapp.entitys.scanner.ScannerCallback
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.RecycleContract
import ping.otmsapp.mvp.presenter.RecyclePresenter
import ping.otmsapp.tools.AppUtil
import ping.otmsapp.tools.DialogUtil
import ping.otmsapp.tools.StrUtil


class RecycleActivity: ViewBaseImp<RecyclePresenter>(), RecycleContract.View, ScannerCallback {

    private var adapter: RecycleListAdapter? = null
    private val click = ClickManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_recycle)
        tv_name.text = "回收列表"

        click.addNode(iv_back) {
            //返回
            finish()
        }.addNode(btn_store_select){
            //选择门店
            presenter.selectStore();
        }.addNode(btn_carton_box) {
            //添加纸箱
            val type = getSelectType()
            if (type==2 || type==3){
                val editText = EditText(this)
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(7))
                editText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val builder = AlertDialog.Builder(this)
                builder.setTitle("请输入纸箱数量,添加成功将无法修改,请慎重操作")//提示框标题
                builder.setView(editText)
                builder.setPositiveButton("添加") { dialog, which ->
                    dialog.dismiss()
                    try {
                        val number = Integer.parseInt(editText.text.toString())
                        presenter.addCartonNumber(number,type);
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toast("添加失败")
                    }
                }
                builder.create().show()
            }else{
                toast("当前类型无法添加纸箱")
            }

        }.addNode(btn_code_sure){
            //二维码手动输入
            AppUtil.hideSoftInputFromWindow(this@RecycleActivity)
            val str = et_code_input.text.toString()
            if (StrUtil.validate(str)){
                onScanner(str)
            }
            et_code_input.setText("");
        }
        //关联列表
        adapter = RecycleListAdapter(this)
        lv_content.adapter = adapter

        if (!presenter.init()){
            startActivity(Intent(this,DispatchActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setCurrentStoreIndex(intent.getIntExtra("index",-1))
        presenter.updateData();
    }

    override fun updateStoreName(storeName: String?) {
       runOnUiThread {
           tv_store_name.text = storeName
       }
    }

    override fun updateBoxInfo(info: String?) {
        runOnUiThread {
            tv_info.text = info
        }
    }

    override fun refreshList(recyclerBoxes: MutableList<RecyclerBox>?) {
        adapter?.data = recyclerBoxes
        runOnUiThread {
            adapter?.notifyDataSetChanged()
        }
    }

    override fun openStoreList(storeNames: Array<out CharSequence>?) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("请选择进行回收的门店")
        builder.setItems(storeNames) { dialog, which ->
            dialog.dismiss()
            presenter.setCurrentStoreIndex(which)
        }
        builder.create().show()

    }

    override fun toast(message: String?) {
        runOnUiThread {
            DialogUtil.dialogSimple(this@RecycleActivity,message,"好的,知道了",null)
        }
    }

    override fun onScanner(codeBar: String?) {
        IO.pool{
            val type = getSelectType()
            presenter.scanHandle(codeBar,type);
        }
    }

    private fun getSelectType(): Int {
        return when(rg_select.checkedRadioButtonId){
            rbtn_recycle_box.id -> 1
            rbtn_back_box.id -> 2
            rbtn_adjust_box.id -> 3
            else -> -1
        }
    }

}