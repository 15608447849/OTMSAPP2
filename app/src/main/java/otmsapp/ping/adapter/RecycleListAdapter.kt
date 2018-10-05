package otmsapp.ping.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import otmsapp.ping.R
import otmsapp.ping.entitys.dispatch.Dispatch
import otmsapp.ping.entitys.dispatch.Store
import otmsapp.ping.entitys.recycler.RecyclerBox
import otmsapp.ping.tools.StrUtil
import otmsapp.ping.tools.TimeUtil

class RecycleListAdapter(var context: Context) : BaseAdapter(){



    /**
     * 回收列表
     */
    var data:MutableList<RecyclerBox>? = null

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    //获取列表数据
    override fun getItem(position: Int): RecyclerBox? {
        return data?.get(position)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh = if (convertView == null) ViewHolder(context = context) else convertView.tag as ViewHolder

        val recyclerBox = getItem(position)

        vh.codeBar.text = StrUtil.format("[ %s ]",recyclerBox?.boxNo)
        vh.type.text = StrUtil.format("[%s]",when(recyclerBox?.type){
            1 -> "回收箱"
            2 -> "退货箱"
            3 -> "调剂箱"
            else -> ""
        })
        vh.time.text = TimeUtil.formatUTC(recyclerBox?.time!!,"MM/dd HH:mm:ss")

        return vh.itemView
    }


    private class ViewHolder(context: Context){
        val itemView: View = LayoutInflater.from(context).inflate(R.layout.list_recyclebox,null)

        val codeBar :TextView = itemView.findViewById(R.id.tv_code) as TextView
        val type :TextView = itemView.findViewById(R.id.tv_type) as TextView
        val time :TextView = itemView.findViewById(R.id.tv_time) as TextView

        init {
            itemView.tag = this
        }

    }
}


