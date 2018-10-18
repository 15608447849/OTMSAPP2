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
import otmsapp.ping.entitys.warn.WarnItem
import otmsapp.ping.tools.TimeUtil

class WarnListAdapter(val context: Context) : IAdapter(){

    /**
     * 预警列表
     */
    var data:MutableList<WarnItem>? = null

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    //获取列表数据
    override fun getItem(position: Int): WarnItem? {
        return data?.get(position)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh = if (convertView == null) ViewHolder(context = context) else convertView.tag as ViewHolder

        val warn = getItem(position)
        vh.time.text = TimeUtil.formatUTC(warn?.time!!,"MM/dd HH:mm:ss")
        vh.code.text = warn.code
        vh.type.text = warn.type
        vh.value.text = warn.value
        vh.range.text = warn.range
        vh.delete.setOnClickListener{
            callback?.onItemViewClicked(it,position)
        }
        return vh.itemView
    }


    private class ViewHolder(context: Context){
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_warn,null)!!

        val time = itemView.findViewById(R.id.tv_time) as TextView
        val code = itemView.findViewById(R.id.tv_code) as TextView
        val type = itemView.findViewById(R.id.tv_type) as TextView
        val value = itemView.findViewById(R.id.tv_value) as TextView
        val range = itemView.findViewById(R.id.tv_range) as TextView
        val delete = itemView.findViewById(R.id.iv_delete) as ImageView

        init {
            itemView.tag = this
        }
    }


}


