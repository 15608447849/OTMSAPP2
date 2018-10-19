package ping.otmsapp.entitys.action;

import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashSet;

import ping.otmsapp.log.LLog;

public class ClickManager implements View.OnClickListener{
   private LinkedHashSet<IClick> iClicks = new LinkedHashSet<>();

    public ClickManager addNode(@NotNull View view , @NotNull Callback callback){
        IClick iClick = new IClick(view,callback);
        if (iClicks.add(iClick))  view.setOnClickListener(this);
        return this;
    }
    @Override
    public void onClick(View view) {
        Iterator<IClick> it = iClicks.iterator();
        IClick cur;
        while (it.hasNext()){
            cur = it.next();
            if (cur.check(view)) {
                cur.execute();
                break;
            }
        }
    }

    public interface Callback{
        void onAction();
    }

    private class IClick {
        final int vid;
        Callback action;
        IClick(View view, Callback callback) {
            vid = view.getId();
            action = callback;
        }

        boolean check(View view){
            return view.getId() == vid; }
        void execute(){ if (action!=null) action.onAction();; }

        @Override
        public boolean equals(Object o) {
            if (o instanceof IClick){
                IClick iClick = (IClick) o;
                return iClick.vid == this.vid;
            }
            return super.equals(o);
        }
    }



}
