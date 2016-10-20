package com.aggarwalankur.capstone.quickreddit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.Utils;
import com.aggarwalankur.capstone.quickreddit.data.responses.RedditResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Ankur on 20-Oct-2016.
 */
public class RedditPostsListAdapter extends RecyclerView.Adapter<RedditPostsListAdapter.RedditPostsViewHolder> {
    public interface RedditPostItemClicked {
        void onRedditPostItemClicked(String tag);
    }

    private static final String SUBEDDIT_PREFIX = "/r/";
    private static final String SEPARATOR_TEXT = "  \u25AA  ";

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
        final RedditResponse.RedditContent redditContent = mItemList.get(position).getRedditContent();

        String topBarText = SUBEDDIT_PREFIX + redditContent.getSubreddit()
                + SEPARATOR_TEXT + Utils.getTimeString(redditContent.getCreatedUtc())
                + SEPARATOR_TEXT + redditContent.getDomain();

        String bottomBarText = mContext.getResources().getString(R.string.comments_text) + redditContent.getNumComments()
                + SEPARATOR_TEXT
                + mContext.getResources().getString(R.string.score_text) + redditContent.getScore();

        if (redditContent.getThumbnail() != null && !redditContent.getThumbnail().isEmpty()) {
            Picasso.with(mContext).load(redditContent.getThumbnail())
                    .error(R.drawable.ic_placeholder_img)
                    .into(holder.previewImg);
        } else {
            holder.previewImg.setImageResource(R.drawable.ic_placeholder_img);
        }

        holder.topBar.setText(topBarText);
        holder.title.setText(redditContent.getTitle());
        holder.bottomBar.setText(bottomBarText);

        holder.layout.setTag(redditContent.getIdentifier());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRedditPostItemClickCallback.onRedditPostItemClicked((String) view.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mItemList == null) ? 0 : mItemList.size();
    }

    static class RedditPostsViewHolder extends RecyclerView.ViewHolder {
        ImageView previewImg;
        TextView topBar, title, bottomBar;
        LinearLayout layout;

        public RedditPostsViewHolder(View itemView) {
            super(itemView);

            layout = (LinearLayout) itemView.findViewById(R.id.reddit_post_item_layout);
            previewImg = (ImageView) itemView.findViewById(R.id.preview_img);
            topBar = (TextView) itemView.findViewById(R.id.reddit_top_bar);
            title = (TextView) itemView.findViewById(R.id.reddit_title);
            bottomBar = (TextView) itemView.findViewById(R.id.reddit_bottom_bar);
        }
    }
}
