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
        void onRedditPostItemClicked(int clickedPosition);
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

        String bottomBarText = redditContent.getNumComments() + " " + mContext.getResources().getString(R.string.comments_text)
                + SEPARATOR_TEXT
                + mContext.getResources().getString(R.string.score_text) + " " + redditContent.getScore();

        String imgUrl = getImageUrl(redditContent);

        if (imgUrl != null && !imgUrl.isEmpty()) {
            Picasso.with(mContext).load(imgUrl)
                    .error(R.drawable.ic_placeholder_img)
                    .into(holder.previewImg);
        } else {
            holder.previewImg.setImageResource(R.drawable.ic_placeholder_img);
        }

        holder.topBar.setText(topBarText);
        holder.title.setText(redditContent.getTitle());
        holder.bottomBar.setText(bottomBarText);

        holder.layout.setTag(position);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRedditPostItemClickCallback.onRedditPostItemClicked((int) view.getTag());
            }
        });
    }

    /**
     * Tries to get the best quality image preview URL available
     */
    private String getImageUrl(RedditResponse.RedditContent redditContent){
        String previewImgUrlFromDb = redditContent.getPreviewImgUrl();
        if(previewImgUrlFromDb != null && !previewImgUrlFromDb.isEmpty()){
            if(previewImgUrlFromDb.contains("&amp;")){
                previewImgUrlFromDb = previewImgUrlFromDb.replaceAll("&amp;", "&");
            }

            return previewImgUrlFromDb;
        }

        String thumbnail = redditContent.getThumbnail();

        if(thumbnail != null && !thumbnail.isEmpty()){
            thumbnail = thumbnail.replaceAll("&amp;", "&");
            return thumbnail;
        }

        String previewImgUrl = redditContent.getPreviewImgUrl();

        if(previewImgUrl != null && !previewImgUrl.isEmpty()){
            previewImgUrl = previewImgUrl.replaceAll("&amp;", "&");
            return previewImgUrl;
        }

        return null;

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
