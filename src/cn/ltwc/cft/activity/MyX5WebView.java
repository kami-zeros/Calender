package cn.ltwc.cft.activity;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.ltwc.cft.AppManager;
import cn.ltwc.cft.R;
import cn.ltwc.cft.data.Constant;
import cn.ltwc.cft.utils.FileUtils;
import cn.ltwc.cft.view.TitleView;
import cn.ltwc.cft.x5web.utils.WebViewJavaScriptFunction;
import cn.ltwc.cft.x5web.utils.X5WebView;

import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

@SuppressLint("InlinedApi")
public class MyX5WebView extends Activity {
	/**
	 * 用于演示X5webview实现视频的全屏播放功能 其中注意 X5的默认全屏方式 与 android 系统的全屏方式
	 */

	private X5WebView webView;
	private TitleView title;
	private String webURL;// 加载的网址
	private String webTitle;// 加载网页的标题
	private ProgressBar bar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 沉浸式导航栏
		getWindow()
				.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		AppManager.getInstance().addActivity(this);
		setContentView(R.layout.activity_myx5_web);
		initView();
		initData();
		bindView();

		// webView.loadUrl("file:///android_asset/webpage/fullscreenVideo.html");

		getWindow().setFormat(PixelFormat.TRANSLUCENT);

	}

	private void initView() {
		// TODO Auto-generated method stub
		webView = (X5WebView) findViewById(R.id.web_filechooser);
		title = (TitleView) findViewById(R.id.xweb_title);
		bar = (ProgressBar) findViewById(R.id.bar);
	}

	private void bindView() {
		// TODO Auto-generated method stub
		webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView arg0, int process) {
				bar.setProgress(process);
				if (process >= 100) {
					bar.postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							bar.setVisibility(View.GONE);
						}
					}, 200);
				} else {
					bar.setVisibility(View.VISIBLE);
				}
			}

		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView arg0, String arg1) {
				// TODO Auto-generated method stub
				return super.shouldOverrideUrlLoading(arg0, arg1);
			}

		});
		webView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				// TODO Auto-generated method stub
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					Toast t = Toast.makeText(MyX5WebView.this, "需要SD卡。",
							Toast.LENGTH_LONG);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
					return;
				}
				DownloaderTask task = new DownloaderTask();
				task.execute(url);
			}

		});
		webView.addJavascriptInterface(new WebViewJavaScriptFunction() {

			@Override
			public void onJsFunctionCalled(String tag) {
				// TODO Auto-generated method stub

			}

			@JavascriptInterface
			public void onX5ButtonClicked() {
				MyX5WebView.this.enableX5FullscreenFunc();
			}

			@JavascriptInterface
			public void onCustomButtonClicked() {
				MyX5WebView.this.disableX5FullscreenFunc();
			}

			@JavascriptInterface
			public void onLiteWndButtonClicked() {
				MyX5WebView.this.enableLiteWndFunc();
			}

			@JavascriptInterface
			public void onPageVideoClicked() {
				MyX5WebView.this.enablePageVideoFunc();
			}
		}, "Android");

		title.getRightText().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		title.getLeftIcon().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (webView != null && webView.canGoBack()) {
					webView.goBack();
				} else {
					finish();
				}
			}
		});

	}

	private void initData() {
		// TODO Auto-generated method stub
		webURL = getIntent().getStringExtra(Constant.WEBURL);
		webTitle = getIntent().getStringExtra(Constant.WEBTITLE);
		title.setTitletext(webTitle);
		title.setRightBtnTextVisibility(View.VISIBLE);
		title.setRightText("关闭");
		webView.loadUrl(webURL);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		try {
			super.onConfigurationChanged(newConfig);
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// /////////////////////////////////////////
	// 向webview发出信息
	private void enableX5FullscreenFunc() {
		Log.i("jsToAndroid", "enableX5FullscreenFunc happend!");

		if (webView.getX5WebViewExtension() != null) {
			Toast.makeText(this, "开启X5全屏播放模式", Toast.LENGTH_LONG).show();
			Bundle data = new Bundle();

			data.putBoolean("standardFullScreen", false);// true表示标准全屏，false表示X5全屏；不设置默认false，

			data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

			data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

			webView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
					data);
		}
	}

	private void disableX5FullscreenFunc() {
		Log.i("jsToAndroid", "disableX5FullscreenFunc happend!");
		if (webView.getX5WebViewExtension() != null) {
			Toast.makeText(this, "恢复webkit初始状态", Toast.LENGTH_LONG).show();
			Bundle data = new Bundle();

			data.putBoolean("standardFullScreen", true);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

			data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

			data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

			webView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
					data);
		}
	}

	private void enableLiteWndFunc() {
		Log.i("jsToAndroid", "enableLiteWndFunc happend!");
		if (webView.getX5WebViewExtension() != null) {
			Toast.makeText(this, "开启小窗模式", Toast.LENGTH_LONG).show();
			Bundle data = new Bundle();

			data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

			data.putBoolean("supportLiteWnd", true);// false：关闭小窗；true：开启小窗；不设置默认true，

			data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

			webView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
					data);
		}
	}

	private void enablePageVideoFunc() {
		Log.i("jsToAndroid", "enablePageVideoFunc happend!");
		if (webView.getX5WebViewExtension() != null) {
			Toast.makeText(this, "页面内全屏播放模式", Toast.LENGTH_LONG).show();
			Bundle data = new Bundle();

			data.putBoolean("standardFullScreen", false);// true表示标准全屏，会调起onShowCustomView()，false表示X5全屏；不设置默认false，

			data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，

			data.putInt("DefaultVideoScreen", 1);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1

			webView.getX5WebViewExtension().invokeMiscMethod("setVideoParams",
					data);
		}
	}

	/**
	 * 按键响应，在WebView中查看网页时，按返回键的时候按浏览历史退回,如果不做此项处理则整个WebView返回退出
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView != null
				&& webView.canGoBack()) {
			// 返回键退回
			webView.goBack();
			return true;
		}
		// If it wasn't the Back key or there's no web page history, bubble up
		// to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	// 内部类
	private class DownloaderTask extends AsyncTask<String, Void, String> {

		public DownloaderTask() {
		}

		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = params[0];
			String fileName = url.substring(url.lastIndexOf("/") + 1);
			fileName = URLDecoder.decode(fileName);
			Log.i("AA", "fileName=" + fileName);

			File directory = Environment.getExternalStorageDirectory();
			File file = new File(directory, fileName);
			if (file.exists()) {
				Log.i("AA", "The file has already exists.");
				return fileName;
			}
			try {
				HttpClient client = new DefaultHttpClient();
				// client.getParams().setIntParameter("http.socket.timeout",3000);//设置超时
				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				if (HttpStatus.SC_OK == response.getStatusLine()
						.getStatusCode()) {
					HttpEntity entity = response.getEntity();
					InputStream input = entity.getContent();

					FileUtils.writeToSDCard(fileName, input);

					input.close();
					// entity.consumeContent();
					return fileName;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			closeProgressDialog();
			if (result == null) {
				Toast t = Toast.makeText(MyX5WebView.this, "连接错误！请稍后再试！",
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				return;
			}

			Toast t = Toast.makeText(MyX5WebView.this, "已保存到SD卡。",
					Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			File directory = Environment.getExternalStorageDirectory();
			File file = new File(directory, result);
			Log.i("AA", "Path=" + file.getAbsolutePath());
			Intent intent = FileUtils.getFileIntent(file);
			startActivity(intent);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showProgressDialog();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

	}

	private ProgressDialog mDialog;

	private void showProgressDialog() {
		if (mDialog == null) {
			mDialog = new ProgressDialog(MyX5WebView.this);
			mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			mDialog.setMessage("正在加载 ，请等待...");
			mDialog.setIndeterminate(false);// 设置进度条是否为不明确
			mDialog.setCancelable(true);// 设置进度条是否可以按退回键取消
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					mDialog = null;
				}
			});
			mDialog.show();

		}
	}

	private void closeProgressDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

}
