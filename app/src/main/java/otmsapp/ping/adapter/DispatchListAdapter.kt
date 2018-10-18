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
import otmsapp.ping.entitys.dispatch.Dispatch
import otmsapp.ping.entitys.dispatch.Store
import otmsapp.ping.tools.StrUtil

class DispatchListAdapter(val context: Context) : BaseAdapter(){

    /**
     * 调度对象
     */
   var dispatch: Dispatch? = null

    /**
     * 当前选中TAB类型
     * 1 - 装载列表
     * 2 - 卸载列表
     */
    var tabType :Int = -1

    /**
     * 当前选中列表
     */
    var index:Int = -1

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    //获取列表数据
    override fun getItem(position: Int): Store? {
        return dispatch?.storeList?.get(position)
    }

    override fun getCount(): Int {
        return dispatch?.storeList?.size ?: 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val vh = if (convertView == null) ViewHolder(context = context) else convertView.tag as ViewHolder

        val store = getItem(position)

        vh.itemView.visibility = View.GONE
        vh.checkBox.setImageResource(R.drawable.ic_checkbox_off)
        vh.state.setImageResource(R.drawable.ic_wait)
        vh.storeName.text = store?.storeName
        vh.order.text = StrUtil.format("配送顺序: %d", store?.specifiedOrder)
        if (position == index) vh.checkBox.setImageResource(R.drawable.ic_checkbox_on)

        when(tabType){
            1->{
                //装货
                loadHandler(vh,store,position)
            }
            2->{
                //卸货
                unloadHandler(vh,store,position)
            }
        }

        return vh.itemView
    }



    private fun loadHandler(vh: ViewHolder, store: Store?,position: Int) {
        if(dispatch?.state!! > Dispatch.STATE.TAKEOUT) return
        vh.stateText.text = StrUtil.format("装载进度: [%d/%d]", store?.loadScanIndex, store?.boxSum)
        if (store?.loadScanIndex!! == store.boxSum) {
            vh.state.setImageResource(R.drawable.ic_complete)
        }
        vh.itemView.visibility = View.VISIBLE

    }

    private fun unloadHandler(vh: ViewHolder, store: Store?,position: Int) {
        if(dispatch?.state!! != Dispatch.STATE.UNLOAD) return
        vh.stateText.text = StrUtil.format("卸载进度: [%d/%d]", store?.unloadScanIndex, store?.boxSum)
        if (store?.unloadScanIndex!! == store.boxSum) {
            vh.state.setImageResource(R.drawable.ic_complete)
        }
        vh.itemView.visibility = View.VISIBLE
    }

    private class ViewHolder(context: Context){
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_dispatch,null)!!

        val checkBox = itemView.findViewById(R.id.iv_checkbox) as ImageView
        val storeName = itemView.findViewById(R.id.tv_store_name) as TextView
        val state = itemView.findViewById(R.id.iv_state) as ImageView
        val order = itemView.findViewById(R.id.tv_order) as TextView
        val stateText = itemView.findViewById(R.id.tv_state) as TextView

        init {
            itemView.tag = this
        }

    }
}


