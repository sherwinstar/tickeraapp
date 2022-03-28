package com.tickera.tickeraapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.Result;


import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Main Activity
 */
public class MainActivity2Activity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    /**
     * Web view
     */
    private WebView webView;

    /**
     * ZXing scanner view
     */
    private ZXingScannerView scannerView;

    /**
     * Relative layout wrapper for web view
     */
    private RelativeLayout webWrapper;

    /**
     * Relative layout wrapper for scanner view
     */
    private RelativeLayout scanWrapper;

    @JavascriptInterface
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_activity2);

        webWrapper = findViewById(R.id.rl_web_wrapper);
        scanWrapper = findViewById(R.id.rl_scanner_wrapper);

        scannerView = findViewById(R.id.zx_scanner_view);

        webView = findViewById(R.id.wv_web_view);
        webView.setWebViewClient(new CustomBrowser());
        webView.addJavascriptInterface(this, "Android");
        webView.loadUrl(Globals.url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);

    }

    /**
     * Open scanner
     */
    @JavascriptInterface
    public void openScanner() {
        System.out.println("##### OPEN SCANNER ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webWrapper.setVisibility(View.GONE);
                scanWrapper.setVisibility(View.VISIBLE);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println("##### Permission is not granted");
            // Permission is not granted

            //Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    Globals.PERMISSION_REQUEST_CODE);

        } else {
            System.out.println("##### Permission is  granted");
            startScanCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Globals.PERMISSION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission was granted. Call your method to open camera
                startScanCamera();
            } else {
                // Permission was denied.......
                // You can again ask for permission from here
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                scanWrapper.setVisibility(View.GONE);
                webWrapper.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Start scanner camera
     */
    private void startScanCamera() {
        System.out.println("##### START SCAN CAMERA ");
        scannerView.setResultHandler(MainActivity2Activity.this);
        scannerView.startCamera();
    }

    @JavascriptInterface
    @Override
    public void handleResult(Result rawResult) {
        System.out.println("##### HANDLE RESULT rawResult " + rawResult);
        //If scan has result hide scanner view and show web view
        scannerView.resumeCameraPreview(MainActivity2Activity.this);
        scannerView.stopCamera();

        scanWrapper.setVisibility(View.GONE);
        webWrapper.setVisibility(View.VISIBLE);

        if (rawResult != null) {
            String result = String.valueOf(rawResult);
            webView.loadUrl("javascript: " + "codeRead(\"" + result + "\")", null);
        } else {
            Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {

        if (scanWrapper.getVisibility() == View.VISIBLE) {
            scannerView.stopCamera();
            scanWrapper.setVisibility(View.GONE);
            webWrapper.setVisibility(View.VISIBLE);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    /**
     * Custom web view client
     */
    private class CustomBrowser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }
}