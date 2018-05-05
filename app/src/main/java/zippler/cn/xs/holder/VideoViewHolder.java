package zippler.cn.xs.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import zippler.cn.xs.R;

/**
 * Created by Zipple on 2018/5/5.
 * Instantiate view
 */
public class VideoViewHolder extends RecyclerView.ViewHolder {
    private TextView textView;
    public VideoViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.text);
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
}
