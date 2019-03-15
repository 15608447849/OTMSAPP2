package ping.otmsapp.mvp.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import kotlinx.android.synthetic.main.act_history.*
import kotlinx.android.synthetic.main.inc_back_title.*
import ping.otmsapp.R
import ping.otmsapp.adapter.CostListAdapter
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.cost.FeeDetail
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.CostContract
import ping.otmsapp.mvp.presenter.CostPresenter
import ping.otmsapp.tools.AppUtil
import ping.otmsapp.tools.DialogUtil
import java.io.File
import java.util.*


class CostActivity: ViewBaseImp<CostPresenter>(),CostContract.View{

    private var tempFile :File ? = null

    private var mYear = 0;
    private var mMonth = 0;
    private var mDay = 0;

    private var adapter: CostListAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_cost)
        iv_back.setOnClickListener{
            startActivity(Intent(this,DispatchActivity::class.java))
            finish()
        }
        tv_name.text = "费用账单"

        //初始化时间
        val ca = Calendar.getInstance();
        ca.time = Date()
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);

        /*tv_name_sub.setOnClickListener {
            //日期选择
            DialogUtil.createSimpleDateDialog(this, mYear, mMonth, mDay) { view, year, monthOfYear, dayOfMonth ->
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                refreshList()
            }
        }*/

        adapter = CostListAdapter(this)

        lv_content.adapter = adapter

        iv_refresh.visibility = View.VISIBLE
        iv_refresh.setOnClickListener {
            refreshList()
        }

        adapter?.setCallback{ view,position ->

            //列表子项点击
            val feeDetail = adapter?.getItem(position)

            when(view.id){
                R.id.btn_reject -> {
                    IO.pool {
                        presenter.rejectCostBill(feeDetail)
                    }
                }
                R.id.btn_sure-> {
                    IO.pool {
                        presenter.sureCostBill(feeDetail)
                    }
                }
                R.id.ll_upload -> {
                    IO.pool {
                        presenter.preUploadImage(feeDetail);
                    }
                }
            }

        }

    }


    override fun onResume() {
        super.onResume()
        refreshList()
    }


    override fun updateDataText(text: String) {
        runOnUiThread{
            //tv_name_sub.text = text
        }
    }


    override fun updateList(data: MutableList<FeeDetail>?) {
        adapter?.data = data
        runOnUiThread{
            adapter?.notifyDataSetChanged()
        }
    }

    override fun refreshList() {
        IO.pool {
            presenter.query(mYear,mMonth,mDay)
        }
    }



    //弹出图片选择界面
    override fun selectPicture(imageFile: File) {
        tempFile = imageFile

        runOnUiThread {
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
    }


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
    override fun previewPictures(bitmap: Bitmap) {
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

            upload.setOnClickListener {
                dialog.dismiss()
                iv.setImageBitmap(null)
                bitmap.recycle()
                IO.pool {
                    presenter.uploadImage(tempFile);
                    tempFile?.delete()
                    tempFile = null
                }
            }
            cancel.setOnClickListener {
                dialog.dismiss()
                iv.setImageBitmap(null)
                bitmap.recycle()
                tempFile?.delete()
                tempFile = null
            }
        }
    }







}