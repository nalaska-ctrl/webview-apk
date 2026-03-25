package com.support.webapp_to_webview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.webkit.CookieManager;



public class MainActivity extends AppCompatActivity {
    private WebView myWebView;
    ProgressBar progressBar;
    private static final int CAMERA_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        startWebView("https://ap-iwa.com/ap-kiosk/?tenant=d9f3a2b7-6c4e-4f1a-9b2e-3e7f5c8a1d42");
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void startWebView(String url) {

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        myWebView= findViewById(R.id.webview);
        WebSettings webSettings=myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);   // <--- add this
        webSettings.setDatabaseEnabled(true);     // <--- add this
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        // Allow cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(myWebView, true);
        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressBar.isShown()) {
                    progressBar.setVisibility(View.GONE);
                }
                suppressVirtualKeyboard(view);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Error:" + description, Toast.LENGTH_SHORT).show();
            }

        });
        myWebView.loadUrl(url);
    }

    private void suppressVirtualKeyboard(WebView view) {
        String js =
                "(function() {" +
                        "  function noKeyboard(el) {" +
                        "    el.setAttribute('inputmode', 'none');" +
                        "  }" +
                        "  document.querySelectorAll('input, textarea').forEach(noKeyboard);" +
                        "  new MutationObserver(function(mutations) {" +
                        "    mutations.forEach(function(m) {" +
                        "      m.addedNodes.forEach(function(n) {" +
                        "        if (n.nodeType !== 1) return;" +
                        "        if (n.matches('input, textarea')) noKeyboard(n);" +
                        "        n.querySelectorAll('input, textarea').forEach(noKeyboard);" +
                        "      });" +
                        "    });" +
                        "  }).observe(document.documentElement, { childList: true, subtree: true });" +
                        "})();";
        view.evaluateJavascript(js, null);
    }
}

