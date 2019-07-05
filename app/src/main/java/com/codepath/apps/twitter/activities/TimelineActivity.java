package com.codepath.apps.twitter.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.ComposeDialogBuilder;
import com.codepath.apps.twitter.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.StringUtils;
import com.codepath.apps.twitter.TwitterApp;
import com.codepath.apps.twitter.TwitterClient;
import com.codepath.apps.twitter.adapters.TweetAdapter;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private TweetAdapter adapter;
    private User user;
    private ArrayList<Tweet> tweets;
    private RecyclerView rvTweets;

    private ImageView ivProfileImage;
    private TextView tvScreenName, tvName;

    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    private ImageView ivFade;
    private int yScrollPos;
    private boolean fadeShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);

        ivFade = findViewById(R.id.ivFade);
        ivFade.setVisibility(View.GONE);
        yScrollPos = 0;
        fadeShown = false;

        // Set up adapter and RecyclerView
        rvTweets = findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetAdapter(client, tweets);
        adapter.setReplyHandler(makeComposeHandler());

        // LEARN: I still have yet to understand why/how LayoutManagers work.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                long maxId = getOldestId();
                populateTimeline(TwitterClient.NUM_POSTS_TO_LOAD, maxId);
            }

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                super.onScrolled(view, dx, dy);
                yScrollPos += dy;
                if (yScrollPos > 5 && !fadeShown) {
                    ivFade.setVisibility(View.VISIBLE);
                    fadeShown = true;
                } else if (yScrollPos <= 5 && fadeShown) {
                    ivFade.setVisibility(View.GONE);
                    fadeShown = false;
                }
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        // Set up swipe container
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                populateTimeline();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fetchLoggedInUser();
        populateTimeline();
    }

    private void fetchLoggedInUser() {
        client.showLoggedInUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TimelineActivity", response.toString());
                try {
                    user = User.fromJSON(response);
                    adapter.setLoggedInUser(user);
                    tvName.setText(StringUtils.ellipsize(user.name, 24));
                    tvScreenName.setText("@" + user.screenName);
                    Glide.with(TimelineActivity.this).load(user.profileImageUrl).into(ivProfileImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void populateTimeline() {
        populateTimeline(TwitterClient.NUM_POSTS_TO_LOAD, -1);
    }

    private void populateTimeline(int count, long maxId) {
        client.getHomeTimeline(count, maxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TimelineActivity", response.toString());
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("TimelineActivity", response.toString());

                for (int i = 0; i < response.length(); i++) {
                    try {
                        Tweet t = Tweet.fromJSON(response.getJSONObject(i));
                        tweets.add(t);
                        adapter.notifyItemInserted(tweets.size() - 1);
                        swipeContainer.setRefreshing(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TimelineActivity", responseString);
                throwable.printStackTrace();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TimelineActivity", errorResponse.toString());
                throwable.printStackTrace();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TimelineActivity", errorResponse.toString());
                throwable.printStackTrace();
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void onCompose(View view) {
        ComposeDialogBuilder dialog = new ComposeDialogBuilder(this);
        dialog.fire(user, makeComposeHandler());
    }

    private ComposeDialogBuilder.OnFinishHandler makeComposeHandler() {
        final JsonHttpResponseHandler standardJsonHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(TimelineActivity.this, "Posted!", Toast.LENGTH_LONG).show();
                try {
                    Tweet posted = Tweet.fromJSON(response);
                    tweets.add(0, posted);
                    adapter.notifyItemInserted(0);
                    rvTweets.scrollToPosition(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(TimelineActivity.this, "Posting failed", Toast.LENGTH_LONG).show();
                Log.d("TimelineActivity", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(TimelineActivity.this, "Posting failed", Toast.LENGTH_LONG).show();
                Log.d("TimelineActivity", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(TimelineActivity.this, "Posting failed", Toast.LENGTH_LONG).show();
                Log.d("TimelineActivity", responseString);
                throwable.printStackTrace();
            }
        };

        return new ComposeDialogBuilder.OnFinishHandler() {
            @Override
            public void onPost(String body) {
                Toast.makeText(TimelineActivity.this, "Posting...", Toast.LENGTH_SHORT).show();
                client.sendTweet(body, standardJsonHandler);
            }

            @Override
            public void onPost(String body, Tweet toReplyTo) {
                Toast.makeText(TimelineActivity.this, "Posting...", Toast.LENGTH_SHORT).show();
                client.sendTweet(body, toReplyTo, standardJsonHandler);
            }

            @Override
            public void onCancel() {
                Toast.makeText(TimelineActivity.this, "Post canceled", Toast.LENGTH_LONG).show();
            }
        };
    }

    private long getOldestId() {
        return this.tweets.get(this.tweets.size() - 1).uid;
    }
}
