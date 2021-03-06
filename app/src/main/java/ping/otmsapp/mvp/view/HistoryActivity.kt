package ping.otmsapp.mvp.view

import android.os.Bundle
import ping.otmsapp.R
import ping.otmsapp.tools.StrUtil
import java.util.*
import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.act_history.*
import kotlinx.android.synthetic.main.inc_back_title.*
import ping.otmsapp.adapter.HistoryListAdapter
import ping.otmsapp.entitys.IO
import ping.otmsapp.entitys.history.DispatchDetail
import ping.otmsapp.mvp.basics.ViewBaseImp
import ping.otmsapp.mvp.contract.HistoryContract
import ping.otmsapp.mvp.presenter.HistoryPresenter
import ping.otmsapp.tools.DialogUtil


class HistoryActivity: ViewBaseImp<HistoryPresenter>() ,HistoryContract.View{

    private var mYear = 0;
    private var mMonth = 0;
    private var mDay = 0;

    private var adapter:HistoryListAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_history)
        iv_back.setOnClickListener{
            startActivity(Intent(this,DispatchActivity::class.java))
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
            //日期选择
            DialogUtil.createSimpleDateDialog(this, mYear, mMonth, mDay) { view, year, monthOfYear, dayOfMonth ->
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                refreshList()
            }
        }

        //列表适配器
        adapter = HistoryListAdapter(this)

        lv_content.adapter = adapter

        iv_refresh.visibility = View.VISIBLE
        iv_refresh.setOnClickListener {
            refreshList()
        }

        adapter?.setCallback{ view,position ->

            //列表子项点击
            val dispatchDetail = adapter?.getItem(position)

            var items = arrayOfNulls<String>(dispatchDetail?.storeDetails?.size!!)

            for (i in 0 until dispatchDetail.storeDetails.size) {
                val it = dispatchDetail.storeDetails[i]
                items[i] = StrUtil.format("[ %s ]", it.simName)
            }

            DialogUtil.createSimpleListDialog(this,"门店列表",items,true){   dialog, which ->
                val storeDetail = dispatchDetail.storeDetails[which]
                DialogUtil.createSimpleListDialog(this,storeDetail.address,storeDetail.boxNoList,false,null)
            }


        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun refreshList() {
        IO.pool {
            presenter.query(mYear,mMonth,mDay)
        }
    }

    override fun updateDataText(text: String) {
        runOnUiThread{
            tv_name_sub.text = text
        }
    }

    override fun updateList(data: MutableList<DispatchDetail>?) {
        adapter?.data = data
        runOnUiThread{
            adapter?.notifyDataSetChanged()
        }
    }

}