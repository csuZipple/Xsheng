package zippler.cn.xs.component;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.CommentAdapter;
import zippler.cn.xs.entity.Comment;

/**
 * Created by ice on 2018/5/17.
 */

public class CommentPopView extends PopupWindow {

    private final View view;
    private final Context mContext;
    private List<Comment> comments;

    public CommentPopView(final Context mContext, List<Comment> comments) {
        this.view = LayoutInflater.from(mContext).inflate(R.layout.pop_view_comment, null);
        this.mContext = mContext;
        this.comments = comments;

        TextView tvCommentCount = view.findViewById(R.id.comment_number);
        tvCommentCount.setText(comments.size()+"条评论");

        RecyclerView recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setAdapter(new CommentAdapter(mContext,comments));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));


        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 设置外部可点击
        this.setOutsideTouchable(true);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.pop_layout).getTop();

                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        CommentPopView.this.dismiss();
                    }
                }
                return true;
            }
        });


    /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(1500);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xffffff);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.record_pop_anim);

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });

    }

}
