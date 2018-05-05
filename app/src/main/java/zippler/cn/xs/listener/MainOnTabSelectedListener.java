package zippler.cn.xs.listener;

import android.support.design.widget.TabLayout;

import zippler.cn.xs.R;
import zippler.cn.xs.component.NoPreloadViewPager;

/**
 * Created by Zipple on 2018/5/5.
 * Setting a listener for tab layout item
 */
public class MainOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
    private NoPreloadViewPager pager;

    public MainOnTabSelectedListener(NoPreloadViewPager pager) {
        this.pager = pager;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
         switch(position){
             case 0:
                 tab.setIcon(R.mipmap.refresh1);
                 break;
             case 1:
                 tab.setIcon(R.mipmap.camera1);
                 break;
             case 2:
                 tab.setIcon(R.mipmap.me1);
                 break;
         }
        pager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        switch (tab.getPosition()){
            case 0:
                tab.setIcon(R.mipmap.refresh2);
                break;
            case 1:
                tab.setIcon(R.mipmap.camera2);
                break;
            case 2:
                tab.setIcon(R.mipmap.me2);
                break;
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
           onTabSelected(tab);
    }
}
