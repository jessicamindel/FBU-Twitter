package com.codepath.apps.twitter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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

        // TODO: Logic around updating tvCharsLeft

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
}
