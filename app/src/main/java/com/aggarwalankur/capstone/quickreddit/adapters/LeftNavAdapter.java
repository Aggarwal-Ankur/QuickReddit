package com.aggarwalankur.capstone.quickreddit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aggarwalankur.capstone.quickreddit.IConstants;
import com.aggarwalankur.capstone.quickreddit.R;
import com.aggarwalankur.capstone.quickreddit.data.dto.SubredditDTO;

import java.util.List;

/**
 * Created by Ankur on 18-Oct-16.
 */

public class LeftNavAdapter extends RecyclerView.Adapter<LeftNavAdapter.LeftNavViewHolder>{
    public interface LeftNavItemClickCallback{
        void onLeftNavItemClicked(String tag);
    }

    private Context mContext;
    private List<SubredditDTO> mItemList;
    private LeftNavItemClickCallback mLeftNavItemClickCallback;

    public LeftNavAdapter(Context context, List<SubredditDTO> itemList, LeftNavItemClickCallback leftNavItemClickCallback) {
        this.mContext = context;
        this.mItemList = itemList;
        this.mLeftNavItemClickCallback = leftNavItemClickCallback;
    }


    @Override
    public LeftNavViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_nav_list_item, null);
        LeftNavViewHolder viewHolder = new LeftNavViewHolder(layoutView);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        /**
         * 4 Items are statically added to left Nav. These are :
         * 1. Reddit Main Page
         * 2. Subreddit feed
         * 3. Add Subreddit
         * 4. Settings (last)
         */


        if(mItemList == null || mItemList.isEmpty()){
            return 4;
        }
        return mItemList.size() + 4;
    }

    @Override
    public void onBindViewHolder(LeftNavViewHolder holder, int position) {
        if(position == 0){
            //1. Reddit Main Page
            holder.leftPadding.setVisibility(View.GONE);
            holder.plusIcon.setVisibility(View.GONE);
            holder.itemName.setText(mContext.getResources().getString(R.string.action_main_page));
            holder.layout.setTag(IConstants.LEFT_NAV_TAGS.MAIN_PAGE);
        }else if(position == 1){
            //2. Subreddit feed
            holder.leftPadding.setVisibility(View.GONE);
            holder.plusIcon.setVisibility(View.GONE);
            holder.itemName.setText(mContext.getResources().getString(R.string.action_feed));
            holder.layout.setTag(IConstants.LEFT_NAV_TAGS.SUBREDDIT_FEED);
        }else if(position == 2){
            //3. Add Subreddit
            holder.leftPadding.setVisibility(View.GONE);
            holder.plusIcon.setVisibility(View.VISIBLE);
            holder.itemName.setText(mContext.getResources().getString(R.string.action_add_subreddit));
            holder.layout.setTag(IConstants.LEFT_NAV_TAGS.ADD_SUBREDDIT);
        }else if(position == mItemList.size()){
            //4. Settings
            holder.leftPadding.setVisibility(View.GONE);
            holder.plusIcon.setVisibility(View.VISIBLE);
            holder.itemName.setText(mContext.getResources().getString(R.string.action_add_subreddit));
            holder.layout.setTag(IConstants.LEFT_NAV_TAGS.SETTINGS);
        }else{
            int listPosition = position - 3;

            final SubredditDTO currentSubreddit = mItemList.get(listPosition);

            holder.leftPadding.setVisibility(View.VISIBLE);
            holder.plusIcon.setVisibility(View.GONE);
            holder.itemName.setText(currentSubreddit.getName());
            holder.layout.setTag(currentSubreddit.getPath());
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLeftNavItemClickCallback != null){
                    mLeftNavItemClickCallback.onLeftNavItemClicked((String) view.getTag());
                }
            }
        });

    }

    static class LeftNavViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout layout;
        public View leftPadding;
        public TextView itemName;
        public ImageView plusIcon;

        public LeftNavViewHolder(View itemView) {
            super(itemView);
            layout = (LinearLayout)itemView.findViewById(R.id.left_nav_item_layout);
            leftPadding = itemView.findViewById(R.id.left_pad);
            itemName = (TextView)itemView.findViewById(R.id.item_name);
            plusIcon = (ImageView)itemView.findViewById(R.id.plus_icon);
        }
    }
}
