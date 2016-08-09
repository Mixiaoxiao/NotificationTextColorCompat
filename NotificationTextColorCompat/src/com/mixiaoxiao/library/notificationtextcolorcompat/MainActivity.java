package com.mixiaoxiao.library.notificationtextcolorcompat;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	
	private TextView mTestInfoTextView;
	private final int mSmallIconId = R.drawable.ic_stat_action_thumb_up;
	private final int mLargeIconId = R.drawable.large_icon_40dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestInfoTextView = (TextView) findViewById(R.id.testinfo_text);
    }
    
	public void onClickSystemNotification(View v){
    	Notification.Builder builder = new Notification.Builder(this);
		builder.setTicker("System Ticker")
		.setContentTitle("System ContentTitle")
		.setContentText("System ContentText")
		.setSmallIcon(mSmallIconId)
		.setLargeIcon(getLargeIcon());
		getNotificationManager().notify(0, builder.getNotification());
    }
    
    public void onClickCustomNotification(View v){
    	Notification.Builder builder = new Notification.Builder(this);
		builder.setTicker("Custom Ticker")
		//.setContentTitle("Custom ContentTitle")
		//.setContentText("Custom ContentText")
		.setSmallIcon(mSmallIconId)
		.setLargeIcon(getLargeIcon());
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout_withbutton);
		builder.setContent(remoteViews);
		NotificationTextColorCompat.byAuto(this).setContentTitleColor(remoteViews, R.id.notification_content_title)
			.setContentTextColor(remoteViews, R.id.notification_content_text);
		remoteViews.setTextViewText(R.id.notification_content_title, "Custom ContentTitle");
		remoteViews.setTextViewText(R.id.notification_content_text, "Custom ContentText");
		remoteViews.setImageViewResource(R.id.notification_large_icon, mLargeIconId);
		getNotificationManager().notify(1, builder.getNotification());
    }
    
    private NotificationManager getNotificationManager(){
    	return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
    }
    private Bitmap getLargeIcon(){
    	return BitmapFactory.decodeResource(getResources(), mLargeIconId);
    }
    
    
    
    public void onClickByAuto(View v) {
		NotificationTextColorCompat fetcher = new NotificationTextColorCompat(this).byAuto();
		showTestInfo(fetcher.toString()); 
	}

	public void onClickByText(View v) {
		NotificationTextColorCompat fetcher = new NotificationTextColorCompat(this).byText();
		showTestInfo(fetcher.toString());
	}

	public void onClickById(View v) {
		NotificationTextColorCompat fetcher = new NotificationTextColorCompat(this).byId();
		showTestInfo(fetcher.toString());
	}

	public void onClickByAnyText(View v) {
		NotificationTextColorCompat fetcher = new NotificationTextColorCompat(this).byAnyTextView();
		showTestInfo(fetcher.toString());
	}

	public void onClickBySdkVersion(View v) {
		NotificationTextColorCompat fetcher = new NotificationTextColorCompat(this).bySdkVersion();
		showTestInfo(fetcher.toString()); 
	}
	
	private void showTestInfo(String text) {
		mTestInfoTextView.setText(text);
	}

}
