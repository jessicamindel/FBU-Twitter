package com.codepath.apps.twitter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage, ivReply;
        public TextView tvUserName, tvBody, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivReply = itemView.findViewById(R.id.ivReply);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    private List<Tweet> tweets;
    private Activity activity;
    ComposeDialogBuilder.OnFinishHandler replyHandler;

    public TweetAdapter(List<Tweet> tweets) {
        this.tweets = tweets;
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

        viewHolder.tvUserName.setText(t.user.name);
        viewHolder.tvBody.setText(t.body);
        viewHolder.tvDate.setText(getRelativeTimeAgo(t.createdAt));
        Glide.with(activity)
             .load(t.user.profileImageUrl)
             .into(viewHolder.ivProfileImage);

        viewHolder.ivReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComposeDialogBuilder dialog = new ComposeDialogBuilder(activity);
                dialog.fire(t, new ComposeDialogBuilder.OnFinishHandler() {
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


    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();

            long now = (new Date()).getTime();
            long hoursBetween = now - dateMillis;
            hoursBetween = (int) ((hoursBetween / (1000 * 60 * 60)));

            if (hoursBetween >= 24) {
                SimpleDateFormat moreThanADay = new SimpleDateFormat("MMM dd", Locale.US);
                relativeDate = moreThanADay.format(dateMillis);
            } else {
                String rawRelativeDate = DateUtils.getRelativeTimeSpanString(dateMillis, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
                String[] parts = rawRelativeDate.split(" ");
                relativeDate = parts[0];
                switch (parts[1]) {
                    case "second":
                    case "seconds":
                        relativeDate += "s";
                        break;
                    case "minute":
                    case "minutes":
                        relativeDate += "m";
                        break;
                    case "hour":
                    case "hours":
                        relativeDate += "h";
                        break;
                    case "day":
                    case "days":
                        relativeDate += "d";
                        break;
                    case "month":
                    case "months":
                        relativeDate += "mo";
                        break;
                    case "year":
                    case "years":
                        relativeDate += "y";
                        break;
                    default:
                        relativeDate = rawRelativeDate;
                        break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}
