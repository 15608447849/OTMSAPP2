package otmsapp.ping.mvp.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import otmsapp.ping.R;
import otmsapp.ping.log.LLog;
import otmsapp.ping.mvp.contract.MenuContract;
import otmsapp.ping.tools.AppUtil;
import otmsapp.ping.tools.StrUtil;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Leeping on 2018/10/8.
 * email: 793065165@qq.com
 */
public class MenuPopWindow implements MenuContract.View {

    private static class MenuItem{

        int imageRid;
        String text;
        Callback callback;

        MenuItem(int imageRid, String text,Callback callback) {
            this.imageRid = imageRid;
            this.text = text;
            this.callback = callback;
        }

        interface Callback{
            void onAction();
        }
    }

    private MenuContract.Presenter presenter;

    private PopupWindow popupWindow;
    private View anchor;
    private DisplayMetrics outMetrics = new DisplayMetrics();


    public MenuPopWindow(final Context context, View anchor) {

        this.anchor = anchor;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (wm!=null){
            wm.getDefaultDisplay().getMetrics(outMetrics);
        }

        final View contentView= LayoutInflater.from(context).inflate(R.layout.pop_menu,null);
        ListView listView = (ListView) contentView.findViewById(R.id.pop_lv_content);


        final BaseAdapter baseAdapter = new BaseAdapter() {

            class ViewHolder{
                View itemView;
                ImageView iv;
                TextView tv;
                private ViewHolder(Context context) {
                    itemView = LayoutInflater.from(context).inflate(R.layout.list_menu,null);
                    iv = (ImageView) itemView.findViewById(R.id.iv_icon);
                    tv = (TextView) itemView.findViewById(R.id.tv_text);
                    itemView.setTag(this);
                }
            }

            MenuItem[] menuItems = new MenuItem[]{
                    new MenuItem(R.drawable.ic_menu_warn, "预警信息", new MenuItem.Callback() {
                        @Override
                        public void onAction() {
                            if (presenter!=null) presenter.openWarn();
                        }
                    }),
                    new MenuItem(R.drawable.ic_menu_history, "历史记录", new MenuItem.Callback() {
                        @Override
                        public void onAction() {
                            if (presenter!=null) presenter.openHistory();
                        }
                    }),
                    new MenuItem(R.drawable.ic_menu_fee, "费用账单", new MenuItem.Callback() {
                        @Override
                        public void onAction() {
                            if (presenter!=null) presenter.openCost();
                        }
                    }),
                    new MenuItem(R.drawable.ic_short_cut, "快捷方式", new MenuItem.Callback() {
                        @Override
                        public void onAction() {
                            if (presenter!=null) presenter.createShortCut();
                        }
                    }),
                    new MenuItem(R.drawable.ic_menu_logout, "退出登录", new MenuItem.Callback() {
                        @Override
                        public void onAction() {
                            if (presenter!=null) presenter.logout();
                        }
                    }),
                    new MenuItem(R.drawable.ic_menu_exit, "结束应用", new MenuItem.Callback() {
                        @Override
                        public void onAction() {
                            if (presenter!=null) presenter.exit();
                        }
                    })
            };

            @Override
            public int getCount() {
                return menuItems.length;
            }

            @Override
            public MenuItem getItem(int position) {
                return menuItems[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder vh;
                if (convertView==null){
                    vh = new ViewHolder(context);
                }else{
                    vh = (ViewHolder) convertView.getTag();
                }

                final MenuItem item = getItem(position);

                vh.iv.setImageResource(item.imageRid);
                vh.tv.setText(item.text);

                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.callback.onAction();
                        dismissWindows();
                    }
                });

                return vh.itemView;
            }
        };


        listView.setAdapter(baseAdapter);

        popupWindow = new PopupWindow(contentView, outMetrics.widthPixels/2, WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);

    }

    @Override
    public void showProgressBar() {
    }
    @Override
    public void hindProgressBar() {
    }
    @Override
    public void toast(String message) {
        AppUtil.toast(anchor.getContext(),message);
    }

    @Override
    public void showWindows() {
        if (popupWindow!=null) popupWindow.showAsDropDown(anchor,outMetrics.widthPixels/2,0);
    }

    @Override
    public void dismissWindows() {
        if (popupWindow!=null) popupWindow.dismiss();
    }

    @Override
    public void bindPresenter(MenuContract.Presenter presenter) {
        presenter.bindView(this);
        this.presenter = presenter ;
        popupWindow.dismiss();
    }

    @Override
    public void unbindPresenter() {
        this.presenter.unbindView();
        this.presenter = null;
    }




}
