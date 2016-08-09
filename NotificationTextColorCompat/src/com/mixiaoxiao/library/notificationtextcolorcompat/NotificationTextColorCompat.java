package com.mixiaoxiao.library.notificationtextcolorcompat;

import java.lang.reflect.Field;
import java.util.Stack;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * 获取系统默认Notification的标题/内容的文字颜色 </br> 
 * 懒人使用方式 NotificationTextColorCompat.byAuto(context).setContentTitleColor(,).setContentTextColor(,);</br> 
 * 参见安卓源码Notification$Builder.java的build方法buildUnstyled的makeContentView布局notification_template_base.xml</br> 
 * 四种方式获取，准确度依次降低：ByText,ById,ByAnyTextView,BySdkVersion</br> 
 * 测试结果:</br>  
 * 原生Android4.4:全部正确</br> 
 * 原生Android5.0:全部正确 </br> 
 * MIUI5-Android4.1:全部正确</br> 
 * MIUI7-Android5.0:ByText|ById全部正确;ByAnyTextView标题正确，内容不对(MIUI改成了0x80ffffff);BySdkVersion全错 ，MIUI7强行暗色通知栏</br> 
 * MIUI8-Android5.0:ByText|ById全部正确;ByAnyTextView|BySdkVersion全错，MIUI8是白色通知栏，但是文字的黑色是非标准的</br> 
 * Flyme5-Android5.1:ByText全部正确;ById全部错误(Fuck Flyme);ByAnyTextView标题正确,内容不对(Flyme改成了0xffffffff),BySdkVersion全错 ，Flyme强行暗色通知栏</br> 
 * @author Mixiaoxiao
 * 
 */
public class NotificationTextColorCompat {
	private static final boolean DEBUG = true;

	private static final int INVALID_COLOR = 0;
	
	/** 原生4.x的颜色，属于Light的 **/
	private final int ANDROID_4_CONTENT_TITLE_COLOR = 0xffffffff;
	private final int ANDROID_4_CONTENT_TEXT_COLOR = 0xff999999; 
	
	/** 原生5.0的颜色，属于DRAK的 **/
	private final int ANDROID_5_CONTENT_TITLE_COLOR = 0xde000000;
	private final int ANDROID_5_CONTENT_TEXT_COLOR = 0x8a000000;
	 
	private final int DEFAULT_LIGHT_CONTENT_TITLE_COLOR = ANDROID_4_CONTENT_TITLE_COLOR;//0xffffffff;
	private final int DEFAULT_LIGHT_CONTENT_TEXT_COLOR = ANDROID_4_CONTENT_TEXT_COLOR;//0xff999999;// 0x99=153
																		
	private final int DEFAULT_DARK_CONTENT_TITLE_COLOR = 0xff000000;
	private final int DEFAULT_DARK_CONTENT_TEXT_COLOR = 0xff666666;// 0x66=102
	 
 
	private final String fakeContentTitle = "fakeContentTitle";
	private final String fakeContentText = "fakeContentText";

	private int contentTitleColor = INVALID_COLOR; 
	private int contentTextColor = INVALID_COLOR; 
	
	private Context context;
	
	private String fetchMode = "";

	public NotificationTextColorCompat(Context context) {
		super();
		this.context = context;
		if (DEBUG) {
			log("start ->" + toString());
		}
	}
	/**使用这个方法即可**/
	public static NotificationTextColorCompat byAuto(Context context) {
		return new NotificationTextColorCompat(context).byAuto();
	}

	public int getContentTitleColor() {
		return contentTitleColor;
	} 

	public int getContentTextColor() {
		return contentTextColor;
	}

	public NotificationTextColorCompat setContentTitleColor(RemoteViews remoteViews, int contentTitleIds) {
		remoteViews.setTextColor(contentTitleIds, contentTitleColor); 
		return this;
	}
 
	public NotificationTextColorCompat setContentTextColor(RemoteViews remoteViews, int contentTextIds) {
		remoteViews.setTextColor(contentTextIds, contentTextColor);
		return this;
	}

	public NotificationTextColorCompat setContentTitleColor(RemoteViews remoteViews, int... contentTitleIds) {
		for (int tId : contentTitleIds) { 
			remoteViews.setTextColor(tId, contentTitleColor);
		}
		return this;
	}
 
	public NotificationTextColorCompat setContentTextColor(RemoteViews remoteViews, int... contentTextIds) {
		for (int cId : contentTextIds) {
			remoteViews.setTextColor(cId, contentTextColor);
		}
		return this;
	}
	
	public NotificationTextColorCompat byAuto() {
		RemoteViews remoteViews = buildFakeRemoteViews(context);
		if (!fetchNotificationTextColorByText(remoteViews)) {
			if (!fetchNotificationTextColorById(remoteViews)) {
				if (!fetchNotificationTextColorByAnyTextView(remoteViews)) {
					fetchNotificationTextColorBySdkVersion();
				}
			}
		}
		if (DEBUG) {
			log("end ->" + toString());
		}
		return this;
	}
	/**public该方法用于机型测试**/
	public NotificationTextColorCompat byText() {
		RemoteViews remoteViews = buildFakeRemoteViews(context);
		fetchNotificationTextColorByText(remoteViews);
		if (DEBUG) {
			log("end ->" + toString());
		}
		return this;
	}
	/**public该方法用于机型测试**/
	public NotificationTextColorCompat byId() {
		RemoteViews remoteViews = buildFakeRemoteViews(context);
		fetchNotificationTextColorById(remoteViews);
		if (DEBUG) {
			log("end ->" + toString());
		}
		return this;
	}
	/**public该方法用于机型测试**/
	public NotificationTextColorCompat byAnyTextView() {
		RemoteViews remoteViews = buildFakeRemoteViews(context);
		fetchNotificationTextColorByAnyTextView(remoteViews);
		if (DEBUG) {
			log("end ->" + toString());
		}
		return this;
	}
	/**public该方法用于机型测试**/
	public NotificationTextColorCompat bySdkVersion() {
		fetchNotificationTextColorBySdkVersion();
		if (DEBUG) {
			log("end ->" + toString());
		}
		return this;
	}

	/**
	 * 通过我们设置的contentTitle和contentText的文字来获取对应的textView 
	 * 如果context是AppCompatActivity则可能会出错，这个没有实际测试
	 * 因为AppCompatActivity的LayoutInflaterFactory已经是自定义的了，部分控件会返回AppCompatXXX
	 * 如果成功则准确度100%
	 * @param remoteViews
	 * @return
	 */
	private boolean fetchNotificationTextColorByText(final RemoteViews remoteViews) {
		if (DEBUG) {
			log("fetchNotificationTextColorByText");
		}
		fetchMode = "ByText";
		try {
			if (remoteViews != null) {
				TextView contentTitleTextView = null, contentTextTextView = null;
				View notificationRootView = remoteViews.apply(context, new FrameLayout(context));
				// 这个可能不兼容AppCompatActivity,所以有了下面的fetchNotificationTextColorById的方法
				Stack<View> stack = new Stack<View>();
				stack.push(notificationRootView);
				while (!stack.isEmpty()) {
					View v = stack.pop();
					if (v instanceof TextView) {
						final TextView childTextView = ((TextView) v);
						final CharSequence charSequence = childTextView.getText();
						if (TextUtils.equals(fakeContentTitle, charSequence)) {
							contentTitleTextView = childTextView;
							if (DEBUG) {
								log("fetchNotificationTextColorByText -> contentTitleTextView -> OK");
							}
						} else if (TextUtils.equals(fakeContentText, charSequence)) {
							contentTextTextView = childTextView;
							if (DEBUG) {
								log("fetchNotificationTextColorByText -> contentTextTextView -> OK");
							}
						}
						if ((contentTitleTextView != null) && (contentTextTextView != null)) {
							break;
						}

					}
					if (v instanceof ViewGroup) {
						ViewGroup vg = (ViewGroup) v;
						final int count = vg.getChildCount();
						for (int i = 0; i < count; i++) {
							stack.push(vg.getChildAt(i));
						}
					}
				}
				stack.clear();
				return checkAndGuessColor(contentTitleTextView, contentTextTextView);

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 通过contentTitle/contentText(反射获取)的id来取得TextView 
	 * 如果成功则contentTitleColor的准确度100%
	 * 如果systemNotificationContentId > 0则contentTextColor的准确度也是100%
	 * @param remoteViews
	 * @return
	 */
	private boolean fetchNotificationTextColorById(final RemoteViews remoteViews) {
		if (DEBUG) {
			log("fetchNotificationTextColorById");
		}
		fetchMode = "ById";
		try { 
			final int systemNotificationContentTitleId = android.R.id.title;
			final int systemNotificationContentTextId = getAndroidInternalResourceId("text");//获取android.R.id.text
			if (DEBUG) {
				log("systemNotificationContentId -> #" + Integer.toHexString(systemNotificationContentTextId));
			} 
			if (remoteViews != null && remoteViews.getLayoutId() > 0) {
				TextView contentTitleTextView = null, contentTextTextView = null;
				View notificationRootView = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);
				View titleView = notificationRootView.findViewById(systemNotificationContentTitleId);
				if(titleView instanceof TextView){
					contentTitleTextView = (TextView)titleView;
				}
				if(systemNotificationContentTextId > 0){
					View contentView = notificationRootView.findViewById(systemNotificationContentTextId);
					contentTextTextView = (TextView) contentView;
				}
				return checkAndGuessColor(contentTitleTextView, contentTextTextView);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 随意取一个textView判断是light或者是dark
	 * @param remoteViews
	 * @return
	 */
	private boolean fetchNotificationTextColorByAnyTextView(final RemoteViews remoteViews) {
		fetchMode = "ByAnyTextView";
		try {
			if (remoteViews != null && remoteViews.getLayoutId() > 0) {
				TextView anyTextView = null;
				View notificationRootView = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);
				Stack<View> stack = new Stack<View>();
				stack.push(notificationRootView);
				while (!stack.isEmpty()) {
					View v = stack.pop();
					if (v instanceof TextView) {
						anyTextView = (TextView) v;
						break;
					}
					if (v instanceof ViewGroup) {
						ViewGroup vg = (ViewGroup) v;
						final int count = vg.getChildCount();
						for (int i = 0; i < count; i++) {
							stack.push(vg.getChildAt(i));
						}
					}
				}
				stack.clear();
				if (anyTextView != null) {
					if (isLightColor(anyTextView.getTextColors().getDefaultColor())) {
						contentTitleColor = DEFAULT_LIGHT_CONTENT_TITLE_COLOR;
						contentTextColor = DEFAULT_LIGHT_CONTENT_TEXT_COLOR;
					} else {// DARK textColor
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							contentTitleColor = ANDROID_5_CONTENT_TITLE_COLOR;
							contentTextColor = ANDROID_5_CONTENT_TEXT_COLOR;
						} else {
							contentTitleColor = DEFAULT_DARK_CONTENT_TITLE_COLOR;
							contentTextColor = DEFAULT_DARK_CONTENT_TEXT_COLOR;
						}
					}
					return true;
				}

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 按照安卓版本纯猜测 如果是原生安卓准确度100%
	 */
	private void fetchNotificationTextColorBySdkVersion() {
		fetchMode = "BySdkVersion"; 
		final int SDK_INT = Build.VERSION.SDK_INT;
		final boolean isLightColor = (SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				&& (SDK_INT < Build.VERSION_CODES.LOLLIPOP);// 安卓3.0到4.4之间是黑色通知栏
		if (isLightColor) {
			contentTitleColor = DEFAULT_LIGHT_CONTENT_TITLE_COLOR;
			contentTextColor = DEFAULT_LIGHT_CONTENT_TEXT_COLOR;
		} else {// DRAK
			if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				contentTitleColor = ANDROID_5_CONTENT_TITLE_COLOR;
				contentTextColor = ANDROID_5_CONTENT_TEXT_COLOR;
			} else {
				contentTitleColor = DEFAULT_DARK_CONTENT_TITLE_COLOR;
				contentTextColor = DEFAULT_DARK_CONTENT_TEXT_COLOR;
			}

		}
	}

	private void log(String msg) {
		Log.d("NotificationTextColorFetcher", msg);
	}

	private boolean checkAndGuessColor(TextView contentTitleTextView, TextView contentTextTextView) {

		if (contentTitleTextView != null) {
			contentTitleColor = contentTitleTextView.getTextColors().getDefaultColor();// .getCurrentTextColor();
//			if (DEBUG) {
//				//事实上在这里getTextColors().getDefaultColor()和.getCurrentTextColor()是一样的
//				log("titleColor->getDefaultColor->#"
//						+ Integer.toHexString(contentTitleTextView.getTextColors().getDefaultColor())
//						+ " getCurrentTextColor->#" + Integer.toHexString(contentTitleTextView.getCurrentTextColor()));
//			}
		}
		if (contentTextTextView != null) {
			contentTextColor = contentTextTextView.getTextColors().getDefaultColor();
		}
		if (DEBUG) {
			log("checkAndGuessColor-> beforeGuess->" + toString());
		}
		if (contentTitleColor != INVALID_COLOR && contentTextColor != INVALID_COLOR) {
			return true;
		}
		//只要一项获取到了就猜一下就OK
		if (contentTitleColor != INVALID_COLOR) {
			if (isLightColor(contentTitleColor)) {
				contentTextColor = DEFAULT_LIGHT_CONTENT_TEXT_COLOR;
			} else {
				contentTextColor = DEFAULT_DARK_CONTENT_TEXT_COLOR;
			}
			return true;
		}
		if (contentTextColor != INVALID_COLOR) {
			if (isLightColor(contentTextColor)) {
				contentTitleColor = DEFAULT_LIGHT_CONTENT_TITLE_COLOR;
			} else {
				contentTitleColor = DEFAULT_DARK_CONTENT_TITLE_COLOR;
			}
			return true;
		}
		return false;
	}

	private static boolean isLightColor(int color) {
		return isLightAverageColor(toAverageColor(color));
	}
	//RGB的平均值
	private static int toAverageColor(int color) {
		return (int) ((Color.red(color) + Color.green(color) + Color.blue(color)) / 3f + 0.5f);
	}

	private static boolean isLightAverageColor(int averageColor) {
		return averageColor >= 0x80;
	}

	@SuppressWarnings("deprecation")
	private RemoteViews buildFakeRemoteViews(Context context) {
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle(fakeContentTitle).setContentText(fakeContentText).setTicker("fackTicker")
				.setSmallIcon(android.R.drawable.stat_sys_warning);
		Notification notification = builder.getNotification();//.build();
		return notification.contentView;
	}

	@Override
	public String toString() {
		return "NotificationTextColorCompat." + fetchMode + "\ncontentTitleColor=#" + Integer.toHexString(contentTitleColor)
				+ "\ncontentTextColor=#" + Integer.toHexString(contentTextColor) + "";
	}
	
	public static int getAndroidInternalResourceId(String resourceName) {
		//获取"android"包名里的id
		//即com.android.internal.R.id.resourceName
		//实际上如果getIdentifier没有的话，下面反射的方式也应该是没有的
		//实际上不应该是没有的，除非厂商是傻逼
		//defType = "id"，还可以有"layout","drawable"之类的
		final int id = Resources.getSystem().getIdentifier(resourceName, "id", "android");//defType和defPackage必须指定
        if(id > 0){
        	return id;
        }
		try {
			// 反射的方法取com.android.internal.R.id.resourceName
			// 通知栏的大图标imageView的id="icon"
			// 标题是"title" 内容是"text"
			Class<?> clazz = Class.forName("com.android.internal.R$id");
			Field field = clazz.getField(resourceName);
			field.setAccessible(true);
			return field.getInt(null);
		} catch (Exception e) {
		}
		return 0;
	}

}
