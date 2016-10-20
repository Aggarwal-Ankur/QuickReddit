package com.aggarwalankur.capstone.quickreddit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;

import java.util.List;

/**
 * Created by Ankur on 20-Oct-2016.
 */
public class RedditPostsListAdapter extends RecyclerView.Adapter<RedditPostsListAdapter.RedditPostsViewHolder> {
    public interface RedditPostItemClicked {
        void onRedditPostItemClicked(String tag);
    }

    private Context mContext;
    private List<RedditResponse.RedditPost> mItemList;
    private RedditPostItemClicked mRedditPostItemClickCallback;

    public RedditPostsListAdapter(Context context, List<RedditResponse.RedditPost> itemList, RedditPostItemClicked redditPostItemClickCallback) {
        this.mContext = context;
        this.mItemList = itemList;
        this.mRedditPostItemClickCallback = redditPostItemClickCallback;
    }

    @Override
    public RedditPostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reddit_post_list_item, null);
        RedditPostsViewHolder viewHolder = new RedditPostsViewHolder(layoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RedditPostsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return (mItemList == null) ? 0 : mItemList.size();
    }

    static class RedditPostsViewHolder extends RecyclerView.ViewHolder {
        public RedditPostsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
