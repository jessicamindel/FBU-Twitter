package com.codepath.apps.twitter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ComposeDialog {
    public static abstract class OnFinishHandler {
        // TODO: Implement "save draft" feature
        public abstract void onPost(String body);
        public abstract void onCancel();
    }

    Activity activity;
    View vCompose;
    EditText etBody;
    TextView tvCharsLeft;

    public ComposeDialog(Activity activity) {
        this.activity = activity;
    }

    public void fire(final OnFinishHandler handler) {
        LayoutInflater inflater = activity.getLayoutInflater();
        vCompose = inflater.inflate(R.layout.dialog_compose, null);
        etBody = vCompose.findViewById(R.id.etBody);
        tvCharsLeft = vCompose.findViewById(R.id.tvCharsLeft);

        setCharsLeft(0);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Compose")
                .setView(vCompose)
                .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String body = etBody.getText().toString();
                        handler.onPost(body);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.onCancel();
                    }
                });
        builder.show();
    }

    private void setCharsLeft(int count) {
        int remaining = TwitterClient.MAX_POST_CHARS - count;
        String text = remaining + " characters left";
        tvCharsLeft.setText(text);
        int color = R.color.textRegular;
        if (remaining <= 0) {
            color = R.color.textError;
        } else if (remaining <= 20) {
            color = R.color.textWarning;
        }
        tvCharsLeft.setTextColor(ResourcesCompat.getColor(activity.getResources(), color, null));
        Log.d("ComposeDialog", "Count: " + count);
    }
}
