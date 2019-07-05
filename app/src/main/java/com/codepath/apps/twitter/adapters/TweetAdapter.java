package com.codepath.apps.twitter.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.ComposeDialogBuilder;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.TwitterClient;
import com.codepath.apps.twitter.Utils;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

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
    private TwitterClient client;
    private ComposeDialogBuilder.OnFinishHandler replyHandler;

    private static final int VIEWTYPE_PLAIN = 0;
    private static final int VIEWTYPE_IMG = 1;

    public TweetAdapter(TwitterClient client, List<Tweet> tweets) {
        this.tweets = tweets;
        this.client = client;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void setReplyHandler(ComposeDialogBuilder.OnFinishHandler replyHandler) {
        this.replyHandler = replyHandler;
    }

    @Override
    public int getItemViewType(int position) {
        Tweet t = tweets.get(position);
        int viewType = VIEWTYPE_PLAIN;
        if (t.hasImages()) {
            viewType = VIEWTYPE_IMG;
        }
        return viewType;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        activity = (Activity) viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(activity);

        // Get correct layout for media type
        int layoutId = (i == VIEWTYPE_IMG) ? R.layout.item_tweet_img : R.layout.item_tweet;

        // LEARN: I still don't know what attachToRoot signifies.
        View tweetView = inflater.inflate(layoutId, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Tweet t = tweets.get(i);

        viewHolder.tvName.setText(Utils.ellipsize(t.user.name, 24));
        viewHolder.tvScreenName.setText("@" + t.user.screenName);
        viewHolder.tvBody.setText(t.body);
        viewHolder.tvDate.setText(Utils.getRelativeTimeAgo(t.createdAt));
        viewHolder.tvRetweets.setText(Integer.toString(t.numRetweets));
        setRetweetColor(viewHolder, t);
        viewHolder.tvFavorites.setText(Integer.toString(t.numFavorites));
        setFavoriteColor(viewHolder, t);
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
                client.retweet(t, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Update retweet count
                        int prevCount = Integer.parseInt(viewHolder.tvRetweets.getText().toString());
                        int toAdd = (t.retweeted) ? 1 : -1;
                        viewHolder.tvRetweets.setText(Integer.toString(prevCount + toAdd));
                        // Change button color
                        setRetweetColor(viewHolder, t);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(activity, "Failed to retweet", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        throwable.printStackTrace();
                        Toast.makeText(activity, "Failed to retweet", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        throwable.printStackTrace();
                        Toast.makeText(activity, "Failed to retweet", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        viewHolder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.favorite(t, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Update favorite count
                        int prevCount = Integer.parseInt(viewHolder.tvFavorites.getText().toString());
                        int toAdd = (t.favorited) ? 1 : -1;
                        viewHolder.tvFavorites.setText(Integer.toString(prevCount + toAdd));
                        // Change button color
                        setFavoriteColor(viewHolder, t);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        throwable.printStackTrace();
                        Toast.makeText(activity, "Failed to favorite", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        throwable.printStackTrace();
                        Toast.makeText(activity, "Failed to favorite", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(activity, "Failed to favorite", Toast.LENGTH_LONG).show();
                    }
                });
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

    private void setRetweetColor(ViewHolder viewHolder, Tweet t) {
        int colorId = (t.retweeted) ? R.color.colorPrimary : R.color.colorAccent;
        Utils.changeColor(activity, viewHolder.ivRetweet, viewHolder.tvRetweets, colorId, R.drawable.ic_retweet);
    }

    private void setFavoriteColor(ViewHolder viewHolder, Tweet t) {
        int colorId = (t.favorited) ? R.color.colorPrimary : R.color.colorAccent;
        Utils.changeColor(activity, viewHolder.ivFavorite, viewHolder.tvFavorites, colorId, R.drawable.ic_heart);
    }
}
