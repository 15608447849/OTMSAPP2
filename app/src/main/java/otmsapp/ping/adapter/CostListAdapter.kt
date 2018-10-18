package otmsapp.ping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import otmsapp.ping.R
import otmsapp.ping.adapter.infs.IAdapter
import otmsapp.ping.entitys.cost.FeeDetail
import otmsapp.ping.entitys.history.DispatchDetail
import otmsapp.ping.log.LLog
import otmsapp.ping.tools.StrUtil

class CostListAdapter(val context: Context) : IAdapter(){

    /**
     * 费用详情列表
     */
    var data:MutableList<FeeDetail>? = null

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    //获取列表数据
    override fun getItem(position: Int): FeeDetail? {
        if (position<0) return null
        return data?.get(position)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {


        val vh = if (convertView == null) ViewHolder(context = context) else convertView.tag as ViewHolder

        val feeDetail = getItem(position)

        vh.train.text = StrUtil.format("车次号:%s",feeDetail?.trainNo)
        vh.plate.text = StrUtil.format("车牌号:%s",feeDetail?.plateNo)
        vh.store_number.text = StrUtil.format("门店数:%d",feeDetail?.storeTotal)
        vh.box_number.text = StrUtil.format("总箱数:%d",feeDetail?.boxTotal)
        vh.should.text = StrUtil.format("应结费用:%.2f",feeDetail?.shouldFee)
        vh.actual.text = StrUtil.format("实结费用:%.2f",feeDetail?.actual)


        vh.upload.setOnClickListener{
            callback?.onItemViewClicked(it,position)
        }

        vh.reject.setOnClickListener{
            callback?.onItemViewClicked(it,position)
        }

        vh.sure.setOnClickListener{
            callback?.onItemViewClicked(it,position)
        }

        return vh.itemView
    }


    private class ViewHolder(context: Context){
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_cost,null)!!

        val train = itemView.findViewById(R.id.tv_train) as TextView
        val plate = itemView.findViewById(R.id.tv_plate) as TextView
        val mileage = itemView.findViewById(R.id.tv_mileage) as TextView
        val store_number = itemView.findViewById(R.id.tv_store_number) as TextView
        val box_number = itemView.findViewById(R.id.tv_box_number) as TextView

        val should = itemView.findViewById(R.id.tv_should) as TextView
        val actual = itemView.findViewById(R.id.tv_actual) as TextView

        val upload = itemView.findViewById(R.id.iv_upload) as ImageView

        val reject = itemView.findViewById(R.id.btn_reject) as Button
        val sure = itemView.findViewById(R.id.btn_sure) as Button

        init {
            itemView.tag = this
            should.isSelected = true
            actual.isSelected = true
        }
    }


}


