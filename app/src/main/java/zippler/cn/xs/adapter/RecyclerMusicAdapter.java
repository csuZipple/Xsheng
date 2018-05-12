package zippler.cn.xs.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import zippler.cn.xs.R;

/**
 * Created by Zipple on 2018/5/12.
 * set music in recycler view which origin from background internet.
 */
public class RecyclerMusicAdapter extends RecyclerView.Adapter<RecyclerMusicAdapter.MusicHolder> {

    private ArrayList<String> data;

    public RecyclerMusicAdapter(ArrayList<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public MusicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);
        MusicHolder holder = new MusicHolder(view);
        //set some listener here.
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MusicHolder holder, int position) {
        holder.getTextView().setText(++position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MusicHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        MusicHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.music_text);
        }

        public TextView getTextView() {
            return textView;
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }
    }
}
