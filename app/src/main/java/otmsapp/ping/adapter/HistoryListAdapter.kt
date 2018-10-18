package otmsapp.ping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import otmsapp.ping.R
import otmsapp.ping.adapter.infs.IAdapter
import otmsapp.ping.entitys.history.DispatchDetail
import otmsapp.ping.tools.StrUtil

class HistoryListAdapter(val context: Context) : IAdapter(){

    /**
     * 历史详情列表
     */
    var data:MutableList<DispatchDetail>? = null

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    //获取列表数据
    override fun getItem(position: Int): DispatchDetail? {
        return data?.get(position)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh = if (convertView == null) ViewHolder(context = context) else convertView.tag as ViewHolder

        val dispatchDetail = getItem(position)

        vh.train.text = StrUtil.format("车次号:%s",dispatchDetail?.trainNo)
        vh.store_number.text = StrUtil.format("门店数:%d",dispatchDetail?.storeDetails?.size)
        vh.total.text = StrUtil.format("总费用:%.2f",dispatchDetail?.totalFee)
        vh.initial.text = StrUtil.format("初始费:%.2f",dispatchDetail?.initialFee)
        vh.abnormal.text = StrUtil.format("异动费:%.2f",dispatchDetail?.abnormalFee)
        vh.detail.setOnClickListener{
            callback?.onItemViewClicked(it,position)
        }
        return vh.itemView
    }


    private class ViewHolder(context: Context){
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_history,null)!!

        val train = itemView.findViewById(R.id.tv_train) as TextView
        val store_number = itemView.findViewById(R.id.tv_store_number) as TextView
        val total = itemView.findViewById(R.id.tv_total) as TextView
        val initial = itemView.findViewById(R.id.tv_initial) as TextView
        val abnormal = itemView.findViewById(R.id.tv_abnormal) as TextView
        val detail = itemView.findViewById(R.id.iv_detail) as ImageView
        init {
            itemView.tag = this
            total.isSelected = true
            initial.isSelected = true
            abnormal.isSelected = true
        }
    }


}


