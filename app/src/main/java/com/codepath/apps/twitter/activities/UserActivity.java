package com.codepath.apps.twitter.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitter.R;

public class UserActivity extends AppCompatActivity {
    RecyclerView rvContent;
    ImageView ivBannerImage, ivProfileImage;
    TextView tvName, tvScreenName, tvDate, tvTweets, tvFollowers, tvFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
    }
}
