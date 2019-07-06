package com.codepath.apps.twitter.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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

    public static class ImgViewHolder extends ViewHolder {
        public ImageView ivFirstImage, ivFade;
        public FrameLayout flBG;

        public ImgViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFirstImage = itemView.findViewById(R.id.ivFirstImage);
            ivFade = itemView.findViewById(R.id.ivFade);
            flBG = itemView.findViewById(R.id.flBG);
        }
    }

    private static class UnconditionalAllow {
        public boolean val;

        public UnconditionalAllow() {
            val = false;
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
        ViewHolder viewHolder = (i == VIEWTYPE_IMG) ? new ImgViewHolder(tweetView) : new ViewHolder(tweetView);
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
        setReplyColor(viewHolder, t);
        Glide.with(activity)
             .load(t.user.profileImageUrl)
             .into(viewHolder.ivProfileImage);

        t.onColor = Utils.colorFromId(activity, R.color.colorPrimary);
        t.offColor = Utils.colorFromId(activity, R.color.colorAccent);

        if (t.hasImages()) {
            SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    ((ImgViewHolder) viewHolder).ivFirstImage.setImageBitmap(resource);
                    Palette.Builder paletteBuilder = new Palette.Builder(resource);
                    final UnconditionalAllow unconditionalAllow = new UnconditionalAllow();
                    paletteBuilder.addFilter(new Palette.Filter() {
                        @Override
                        public boolean isAllowed(int color, @NonNull float[] floats) {
                            float[] hsl = new float[3];
                            ColorUtils.colorToHSL(color, hsl);
                            boolean lightnessCondition = hsl[2] >= 0.2 && hsl[2] <= 0.8;
                            boolean bgCondition = color != Utils.colorFromId(activity, R.color.colorPrimary);
                            return unconditionalAllow.val || (lightnessCondition && bgCondition);
                        }
                    });
                    Palette palette = paletteBuilder.generate();

                    // Get dominant color without any filter restrictions
                    unconditionalAllow.val = true;
                    int domColor = palette.getDominantColor(0x000000);
                    unconditionalAllow.val = false;
                    float[] domHSL = new float[3];
                    ColorUtils.colorToHSL(domColor, domHSL);
                    boolean imgIsDark = domHSL[2] <= 0.5;

                    int bgColor = palette.getDarkVibrantColor(palette.getDominantColor(0x285481));
                    float[] bgHSL = new float[3];
                    ColorUtils.colorToHSL(bgColor, bgHSL);
                    boolean bgIsDark = bgHSL[2] <= 0.5;

                    int transparentBgColor = ColorUtils.setAlphaComponent(bgColor, 0);
                    int solidFgColor = ColorUtils.HSLToColor(new float[]{ bgHSL[0], bgHSL[1],
                                                bgHSL[2] + (float) ((bgIsDark) ? 0.25 : -0.25) });
                    int activeSolidFgColor = ColorUtils.HSLToColor(new float[]{ bgHSL[0], bgHSL[1],
                                                bgHSL[2] + (float) ((bgIsDark) ? 0.45 : -0.45) });
                    int imgFgColor;
                    try {
                        // FIXME: Is there any way I can make this load consistently?
                        imgFgColor = palette.getDarkVibrantSwatch().getBodyTextColor();
                    } catch (Exception e) {
                        imgFgColor = ColorUtils.HSLToColor(new float[]{ bgHSL[0], bgHSL[1],
                                                domHSL[2] + (float) ((imgIsDark) ? 0.25 : -0.25) });
                    }
                    int translucentImgFgColor = ColorUtils.setAlphaComponent(imgFgColor, (int) Math.round(255 * 0.75)); // Is this really the right alpha value? Or is it x/255?
                    int mainTextColor = (bgIsDark) ? Utils.colorFromId(activity, R.color.colorNeutral) : Utils.colorFromId(activity, R.color.textRegular);

                    ((ImgViewHolder) viewHolder).flBG.setBackgroundColor(bgColor);
                    viewHolder.tvName.setTextColor(imgFgColor);
                    viewHolder.tvScreenName.setTextColor(translucentImgFgColor);
                    viewHolder.tvDate.setTextColor(imgFgColor);
                    viewHolder.tvBody.setTextColor(mainTextColor);

                    t.onColor = activeSolidFgColor;
                    t.offColor = solidFgColor;
                    setRetweetColor(viewHolder, t);
                    setFavoriteColor(viewHolder, t);
                    setReplyColor(viewHolder, t);

                    Bitmap b;
                    try {
                        b = Bitmap.createBitmap(((ImgViewHolder) viewHolder).ivFade.getWidth(), ((ImgViewHolder) viewHolder).ivFade.getHeight(), Bitmap.Config.ARGB_8888);
                    } catch (IllegalArgumentException e) {
                        b = Bitmap.createBitmap(10000, 220, Bitmap.Config.ARGB_8888);
                    }
                    Shader mShader = new LinearGradient(0, 0, 0, b.getHeight(), new int[] { transparentBgColor, bgColor },
                            null, Shader.TileMode.CLAMP);
                    Paint paint = new Paint();
                    Canvas c = new Canvas(b);
                    paint.setShader(mShader);
                    c.drawRect(0, 0, b.getWidth(), b.getHeight(), paint);
                    ((ImgViewHolder) viewHolder).ivFade.setImageBitmap(b);
                }
            };
            Glide.with(activity).load(t.imageUrls.get(0)).asBitmap().into(target);
        }

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
        int color = (t.retweeted) ? t.onColor : t.offColor;
        Utils.changeColor(activity, viewHolder.ivRetweet, viewHolder.tvRetweets, color, R.drawable.ic_retweet, false);
    }

    private void setFavoriteColor(ViewHolder viewHolder, Tweet t) {
        int color = (t.favorited) ? t.onColor : t.offColor;
        Utils.changeColor(activity, viewHolder.ivFavorite, viewHolder.tvFavorites, color, R.drawable.ic_heart, false);
    }

    private void setReplyColor(ViewHolder viewHolder, Tweet t) {
        Utils.changeColor(activity, viewHolder.ivReply, null, t.offColor, R.drawable.ic_reply, false);
    }
}
