package zippler.cn.xs.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import zippler.cn.xs.R;
import zippler.cn.xs.adapter.RecyclerChooseMusicAdapter;
import zippler.cn.xs.entity.Music;
import zippler.cn.xs.util.LinerLayoutManager;
import zippler.cn.xs.util.RemoveLastLineDividerItemDecoration;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment implements View.OnClickListener{

    private RecyclerView recyclerView;//display the Synthetic music
    private LinearLayout upload;
    private LinearLayout record;
    private RelativeLayout search;

    //data
    private List<Music> musicList;

    public CameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        initViews(view);
        registerListeners();

        LinerLayoutManager linerLayoutManager = new LinerLayoutManager(this.getContext());
        recyclerView.setLayoutManager(linerLayoutManager);
        recyclerView.addItemDecoration(new RemoveLastLineDividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));

        Log.d(TAG, "onCreateView: music init");
        initMusic();
        Log.d(TAG, "onCreateView: music init " +musicList.size());
        RecyclerChooseMusicAdapter musicAdapter = new RecyclerChooseMusicAdapter(this.getContext(),musicList);
        recyclerView.setAdapter(musicAdapter);

        return view;
    }

    @Override
    public void onClick(View v) {
         switch (v.getId()){
             default:
                 Toast.makeText(this.getContext(),"default clicked",Toast.LENGTH_SHORT).show();
                 break;
         }
    }

    /**
     * instantiate views
     * @param view the root view
     */
    private void initViews(View view){
        recyclerView = view.findViewById(R.id.synthetic_music);
        upload = view.findViewById(R.id.upload);
        record = view.findViewById(R.id.record);
        search = view.findViewById(R.id.search);
    }

    /**
     * register listeners
     */
    private void registerListeners(){
        upload.setOnClickListener(this);
        record.setOnClickListener(this);
        search.setOnClickListener(this);
    }

    /**
     * init music
     */
    private void initMusic(){
        musicList = new ArrayList<>();
        //add music data here . it also can be load by internet
        Music temp ;
        for (int i = 0; i < 10; i++) {
            temp = new Music();
            temp.setName("Music_add_"+i);
            temp.setLength("00:0"+i);
            musicList.add(temp);
        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public LinearLayout getUpload() {
        return upload;
    }

    public void setUpload(LinearLayout upload) {
        this.upload = upload;
    }

    public LinearLayout getRecord() {
        return record;
    }

    public void setRecord(LinearLayout record) {
        this.record = record;
    }

    public RelativeLayout getSearch() {
        return search;
    }

    public void setSearch(RelativeLayout search) {
        this.search = search;
    }

}
