package ir.javanrood.ngo.adminmobile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { openWebView(); } catch (Throwable t) { showSafeError(t); }
    }

    private void openWebView() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);
        try { webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); } catch (Throwable ignored) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { try { WebView.setWebContentsDebuggingEnabled(true); } catch (Throwable ignored) {} }
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true); settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true); settings.setAllowContentAccess(true); settings.setLoadWithOverviewMode(true); settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false); settings.setDisplayZoomControls(false); settings.setTextZoom(100); settings.setMediaPlaybackRequiresUserGesture(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { settings.setAllowFileAccessFromFileURLs(true); settings.setAllowUniversalAccessFromFileURLs(true); }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); }
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return handleExternalUrl(url); }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null && request.getUrl() != null) return handleExternalUrl(request.getUrl().toString()); return false; }
            @Override public void onPageFinished(final WebView view, String url) { super.onPageFinished(view, url); try { view.postDelayed(new Runnable(){ @Override public void run(){ try { view.evaluateJavascript("try{ if(typeof openArea==='function') openArea('mobileAdmin'); }catch(e){ console.log('admin-open-error', e); }", null); } catch(Throwable ignored){} }}, 250); } catch(Throwable ignored){} }
        });
        webView.setWebChromeClient(new WebChromeClient(){ @Override public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params){ if(MainActivity.this.filePathCallback!=null) MainActivity.this.filePathCallback.onReceiveValue(null); MainActivity.this.filePathCallback=callback; try{ startActivityForResult(params.createIntent(), FILE_CHOOSER_REQUEST); return true; } catch(ActivityNotFoundException e){ MainActivity.this.filePathCallback=null; Toast.makeText(MainActivity.this,"برنامه‌ای برای انتخاب فایل پیدا نشد",Toast.LENGTH_LONG).show(); return false; } catch(Throwable t){ MainActivity.this.filePathCallback=null; Toast.makeText(MainActivity.this,"انتخاب فایل در این دستگاه پشتیبانی نشد",Toast.LENGTH_LONG).show(); return false; }} });
        webView.setDownloadListener(new DownloadListener(){ @Override public void onDownloadStart(String url,String userAgent,String contentDisposition,String mimeType,long contentLength){ try{ startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch(Throwable e){ Toast.makeText(MainActivity.this,"دانلود مستقیم در این نسخه پشتیبانی نشد.",Toast.LENGTH_LONG).show(); } }});
        webView.loadUrl("file:///android_asset/index.html");
    }
    private boolean handleExternalUrl(String url){ if(url==null)return false; if(url.startsWith("http://")||url.startsWith("https://")||url.startsWith("mailto:")||url.startsWith("tel:")){ try{ startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; }catch(Throwable ignored){ return false; }} return false; }
    private void showSafeError(Throwable t){ TextView tv=new TextView(this); tv.setText("اپلیکیشن اجرا شد اما WebView دستگاه آماده نیست.\n\nAndroid System WebView یا Chrome را به‌روزرسانی/فعال کن.\n\nجزئیات خطا:\n"+t.getClass().getSimpleName()+": "+String.valueOf(t.getMessage())); tv.setTextColor(Color.rgb(20,35,55)); tv.setTextSize(15); tv.setGravity(Gravity.CENTER); tv.setPadding(32,32,32,32); setContentView(tv); }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){ super.onActivityResult(requestCode,resultCode,data); if(requestCode==FILE_CHOOSER_REQUEST){ if(filePathCallback==null)return; Uri[] results=null; try{ results=WebChromeClient.FileChooserParams.parseResult(resultCode,data); }catch(Throwable ignored){} filePathCallback.onReceiveValue(results); filePathCallback=null; }}
    @Override public void onBackPressed(){ if(webView!=null&&webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy(){ if(webView!=null){ try{ webView.destroy(); }catch(Throwable ignored){} webView=null; } super.onDestroy(); }
}
