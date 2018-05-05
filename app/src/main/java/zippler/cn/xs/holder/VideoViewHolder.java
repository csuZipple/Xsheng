package zippler.cn.xs.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import zippler.cn.xs.R;
import zippler.cn.xs.component.CircleImageView;

/**
 * Created by Zipple on 2018/5/5.
 * Instantiate view
 */
public class VideoViewHolder extends RecyclerView.ViewHolder {
    private ImageView poster;
    private TextView name;
    private TextView time;
    private CircleImageView avatar;
    private ImageView play;
    private TextView length;

    public VideoViewHolder(View itemView) {
        super(itemView);
        poster = itemView.findViewById(R.id.poster);
        name = itemView.findViewById(R.id.video_name);
        time = itemView.findViewById(R.id.video_time);
        avatar = itemView.findViewById(R.id.avatar);
        length = itemView.findViewById(R.id.length);
        play = itemView.findViewById(R.id.play);
    }

    public ImageView getPoster() {
        return poster;
    }

    public void setPoster(ImageView poster) {
        this.poster = poster;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public TextView getTime() {
        return time;
    }

    public void setTime(TextView time) {
        this.time = time;
    }

    public CircleImageView getAvatar() {
        return avatar;
    }

    public void setAvatar(CircleImageView avatar) {
        this.avatar = avatar;
    }

    public TextView getLength() {
        return length;
    }

    public void setLength(TextView length) {
        this.length = length;
    }

    public ImageView getPlay() {
        return play;
    }

    public void setPlay(ImageView play) {
        this.play = play;
    }
}
