package otmsapp.ping.mvp.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import kotlinx.android.synthetic.main.inc_back_title.*
import otmsapp.ping.R
import otmsapp.ping.log.LLog
import otmsapp.ping.tools.StrUtil
import java.util.*
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.widget.DatePicker
import android.widget.Toast
import otmsapp.ping.entitys.IO
import otmsapp.ping.entitys.history.DispatchDetail
import otmsapp.ping.mvp.contract.HistoryContract
import otmsapp.ping.mvp.presenter.HistoryPresenter
import otmsapp.ping.tools.ProgressFactory


class HistoryActivity: Activity() ,HistoryContract.View{


    private var progressDialog: ProgressDialog? = null
    private val presenter = HistoryPresenter()

    var mYear = 0;
    var mMonth = 0;
    var mDay = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_history)
        iv_back.setOnClickListener{
            finish()
        }

        tv_name.text = "历史记录"

        //初始化时间
        val ca = Calendar.getInstance();
        ca.time = Date()
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);

        tv_name_sub.setOnClickListener {
            DatePickerDialog(this@HistoryActivity, object :DatePickerDialog.OnDateSetListener{
                override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    IO.run {
                        presenter.query(year,monthOfYear,dayOfMonth) //查询
                    }
                }
            }, mYear, mMonth, mDay).show()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.bindView(this)
        IO.run {
            presenter.query(mYear,mMonth,mDay)
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.unbindView()
    }

    override fun onDestroy() {
        progressDialog?.dismiss()
        super.onDestroy()
    }

    override fun showProgressBar() {
        runOnUiThread {
            if (progressDialog == null) progressDialog = ProgressFactory.createSimpleDialog(this, "正在查询...");
            progressDialog?.show()
        }
    }

    override fun hindProgressBar() {
        runOnUiThread { progressDialog?.hide() }
    }

    override fun toast(message: String?) {
        runOnUiThread{
            Toast.makeText(this@HistoryActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
    override fun updateDataText(text: String) {
        runOnUiThread{
            tv_name_sub.text = text
        }
    }
    override fun updateList(data: MutableList<DispatchDetail>?) {
    }

}