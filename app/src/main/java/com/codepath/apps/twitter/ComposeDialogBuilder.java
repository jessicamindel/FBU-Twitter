package com.codepath.apps.twitter;

import android.app.Activity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.twitter.models.Tweet;
import com.codepath.apps.twitter.models.User;

public class ComposeDialogBuilder {
    public static abstract class OnFinishHandler {
        // TODO: Implement "save draft" feature
        public abstract void onPost(String body);
        public abstract void onPost(String body, Tweet toReplyTo);
        public abstract void onCancel();
    }

    private Activity activity;
    private View vCompose;
    private EditText etBody;
    private TextView tvCharsLeft, tvName, tvScreenName;
    private TextView tvToName, tvToScreenName, tvToDate, tvToBody;
    private ImageView ivProfileImage, ivToProfileImage, ivCloseButton;
    private CardView cvTo, cvPostButton;

    public ComposeDialogBuilder(Activity activity) {
        this.activity = activity;
    }

    public void fire(User fromUser, final OnFinishHandler handler) {
        inflate();

        tvName.setText(Utils.ellipsize(fromUser.name, 24));
        tvScreenName.setText("@" + fromUser.screenName);
        Glide.with(activity).load(fromUser.profileImageUrl).into(ivProfileImage);

        // Hide reply section
        cvTo.setVisibility(View.GONE);

        startCountingChars(0);
        showDialog(handler);
    }

    public void fire(User fromUser, Tweet toReplyTo, final OnFinishHandler handler) {
        inflate();

        tvName.setText(Utils.ellipsize(fromUser.name, 24));
        tvScreenName.setText("@" + fromUser.screenName);
        Glide.with(activity).load(fromUser.profileImageUrl).into(ivProfileImage);

        tvToName.setText(Utils.ellipsize(toReplyTo.user.name, 18));
        tvToScreenName.setText("@" + toReplyTo.user.screenName);
        tvToDate.setText(Utils.getRelativeTimeAgo(toReplyTo.createdAt));
        tvToBody.setText(toReplyTo.body);
        Glide.with(activity).load(toReplyTo.user.profileImageUrl).into(ivToProfileImage);

        etBody.setText("@" + toReplyTo.user.screenName + " ");
        startCountingChars(0);

        showDialog(toReplyTo, handler);
    }

    private void inflate() {
        LayoutInflater inflater = activity.getLayoutInflater();
        vCompose = inflater.inflate(R.layout.dialog_compose, null);

        etBody = vCompose.findViewById(R.id.etBody);
        tvCharsLeft = vCompose.findViewById(R.id.tvCharsLeft);

        tvName = vCompose.findViewById(R.id.tvName);
        tvScreenName = vCompose.findViewById(R.id.tvScreenName);
        ivProfileImage = vCompose.findViewById(R.id.ivProfileImage);

        cvTo = vCompose.findViewById(R.id.cvTo);
        tvToName = vCompose.findViewById(R.id.tvToName);
        tvToScreenName = vCompose.findViewById(R.id.tvToScreenName);
        tvToDate = vCompose.findViewById(R.id.tvToDate);
        tvToBody = vCompose.findViewById(R.id.tvToBody);
        ivToProfileImage = vCompose.findViewById(R.id.ivToProfileImage);

        ivCloseButton = vCompose.findViewById(R.id.ivCloseButton);
        cvPostButton = vCompose.findViewById(R.id.cvPostButton);
    }

    private void startCountingChars(int initNumChars) {
        setCharsLeft(initNumChars);
        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setCharsLeft(s.length());
            }
        });
    }

    private void setCharsLeft(int count) {
        int remaining = TwitterClient.MAX_POST_CHARS - count;
        String text = Integer.toString(remaining);
        tvCharsLeft.setText(text);
        int color = R.color.colorPrimary;
        if (remaining <= 0) {
            color = R.color.textError;
        } else if (remaining <= 20) {
            color = R.color.textWarning;
        }
        tvCharsLeft.setTextColor(ResourcesCompat.getColor(activity.getResources(), color, null));
        Log.d("ComposeDialogBuilder", "Count: " + count);
    }

    private void showDialog(final OnFinishHandler handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(vCompose);
        final AlertDialog dialog = builder.create();

        // Cancel click
        ivCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                handler.onCancel();
            }
        });

        // Post click
        cvPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String body = etBody.getText().toString();
                handler.onPost(body);
            }
        });

        dialog.show();
    }

    private void showDialog(final Tweet toReplyTo, final OnFinishHandler handler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(vCompose);
        final AlertDialog dialog = builder.create();

        // Cancel click
        ivCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                handler.onCancel();
            }
        });

        // Post reply click
        cvPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String body = etBody.getText().toString();
                handler.onPost(body, toReplyTo);
            }
        });

        dialog.show();
    }
}
