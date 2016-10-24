package com.aggarwalankur.capstone.quickreddit.widget;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import com.aggarwalankur.capstone.quickreddit.R;

/**
 * Created by Ankur on 24-Oct-16.
 */

public class AddSubredditDialog extends Dialog {

    private Activity mActivity;

    public AddSubredditDialog(Activity activity) {
        super(activity);
        this.mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_subreddit_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }
}
