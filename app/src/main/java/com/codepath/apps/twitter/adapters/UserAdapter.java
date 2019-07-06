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
import com.codepath.apps.twitter.R;
import com.codepath.apps.twitter.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfileImage;
        TextView tvName, tvScreenName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
        }
    }

    Activity activity;
    List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        activity = (Activity) viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(activity);

        View tweetView = inflater.inflate(R.layout.item_user, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        User u = users.get(i);
        viewHolder.tvName.setText(u.name);
        viewHolder.tvScreenName.setText(u.screenName);
        Glide.with(activity).load(u.profileImageUrl).into(viewHolder.ivProfileImage);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
