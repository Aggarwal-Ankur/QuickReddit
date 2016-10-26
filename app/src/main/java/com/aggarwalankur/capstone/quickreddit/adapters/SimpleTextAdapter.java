package com.aggarwalankur.capstone.quickreddit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aggarwalankur.capstone.quickreddit.R;

import java.util.List;

/**
 * Created by ggne0497 on 10/25/2016.
 */
public class SimpleTextAdapter extends RecyclerView.Adapter<TextHolder> {

    public interface SubscribeSubredditClickListener{
        void OnSubredditClicked(String clickedItem);
    }

    private List<String> mList;
    private SubscribeSubredditClickListener mCallbackListener;
    public SimpleTextAdapter(SubscribeSubredditClickListener callbackListener, List<String> searchList){
        this.mCallbackListener = callbackListener;
        this.mList = searchList;
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = View.inflate(viewGroup.getContext(), R.layout.search_suggestion_item, null);
        return new TextHolder(view);
    }

    @Override
    public void onBindViewHolder(TextHolder textHolder, final int position) {
        textHolder.text.setText(mList.get(position));

        textHolder.textHolder.setTag(position);
        textHolder.textHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCallbackListener !=  null){
                    mCallbackListener.OnSubredditClicked(mList.get(position));
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

}

class TextHolder extends RecyclerView.ViewHolder {
    TextView text;
    View textHolder;
    public TextHolder(View itemView) {
        super(itemView);
        text = (TextView) itemView.findViewById(R.id.suggestion_text);
        textHolder = itemView.findViewById(R.id.suggestion_text_holder);
    }

}
