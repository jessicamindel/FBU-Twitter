package com.codepath.apps.twitter;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private TweetAdapter adapter;
    private ArrayList<Tweet> tweets;
    private RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);

        TweetAdapter.getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");

        rvTweets = findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetAdapter(tweets);
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

        populateTimeline();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timeline, menu);
        MenuItem item = menu.findItem(R.id.action_compose);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_compose) {
            ComposeDialogBuilder dialog = new ComposeDialogBuilder(this);
            dialog.fire(makeComposeHandler());
        }

        return super.onOptionsItemSelected(item);
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
