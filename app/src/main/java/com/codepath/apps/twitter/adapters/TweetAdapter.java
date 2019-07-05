package com.codepath.apps.twitter.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.ComposeDialogBuilder;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.StringUtils;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;

import java.util.List;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage, ivReply, ivRetweet, ivFavorite;
        public TextView tvName, tvScreenName, tvBody, tvDate, tvRetweets, tvFavorites;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivReply = itemView.findViewById(R.id.ivReply);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);

            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRetweets = itemView.findViewById(R.id.tvRetweets);
            tvFavorites = itemView.findViewById(R.id.tvFavorites);
        }
    }

    private List<Tweet> tweets;
    private User loggedInUser;
    private Activity activity;
    ComposeDialogBuilder.OnFinishHandler replyHandler;

    public TweetAdapter(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void setReplyHandler(ComposeDialogBuilder.OnFinishHandler replyHandler) {
        this.replyHandler = replyHandler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        activity = (Activity) viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(activity);

        // LEARN: I still don't know what attachToRoot signifies.
        View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Tweet t = tweets.get(i);

        viewHolder.tvName.setText(StringUtils.ellipsize(t.user.name, 24));
        viewHolder.tvScreenName.setText("@" + t.user.screenName);
        viewHolder.tvBody.setText(t.body);
        viewHolder.tvDate.setText(StringUtils.getRelativeTimeAgo(t.createdAt));
        viewHolder.tvRetweets.setText(Integer.toString(t.numRetweets));
        viewHolder.tvFavorites.setText(Integer.toString(t.numFavorites));
        Glide.with(activity)
             .load(t.user.profileImageUrl)
             .into(viewHolder.ivProfileImage);

        viewHolder.ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComposeDialogBuilder dialog = new ComposeDialogBuilder(activity);
                dialog.fire(loggedInUser, t, new ComposeDialogBuilder.OnFinishHandler() {
                    @Override
                    public void onPost(String body) {
                        replyHandler.onPost(body);
                    }

                    @Override
                    public void onPost(String body, Tweet toReplyTo) {
                        replyHandler.onPost(body, toReplyTo);
                    }

                    @Override
                    public void onCancel() {
                        replyHandler.onCancel();
                    }
                });
            }
        });

        viewHolder.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        viewHolder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }
}
