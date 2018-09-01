package com.mopat.patrick.voicerecorderhd;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Patrick on 08.11.2016.
 */

public class GoForPro {
    private final static String APP_TITLE = "HD Voice Recorder";// App Name
    private final static int DAYS_UNTIL_PROMPT = 1;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("goforpro", 0);
        if (prefs.getBoolean("dontshowagaingoforpro", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_countgoforpro", 0) + 1;
        editor.putLong("launch_countgoforpro", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunchgoforpro", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunchgoforpro", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showProDialog(mContext, editor);
            }
        }
        editor.apply();
    }

    public static void showProDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using HD Voice Recorder try the Ad-Free Pro-Version with advanced Features!");
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        tv.setHeight(350);
        tv.setPadding(10, 10, 10, 10);
        ll.addView(tv);

        Button b1 = new Button(mContext);
        b1.setBackgroundColor(Color.GREEN);
        b1.setText("I want to get the Pro-Version!");
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Absolutes.PRO_PACKAGE_NAME)));
                } catch (ActivityNotFoundException anfe) {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Absolutes.PRO_PACKAGE_NAME)));
                }
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setText("Stick to this version");
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagaingoforpro", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
    }
}
