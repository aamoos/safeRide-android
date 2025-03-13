package com.saferide;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String URI = "http://192.168.0.39:3000";
    private WebView webView; // WebView 선언

    // 권한 요청 콜백 정의 (알림 권한 요청)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        Toast.makeText(MainActivity.this, "알림 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // WebView 설정
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // /login 페이지가 로드될 때마다 FCM 토큰 전달
                if (url.equals(URI + "/login")) {
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String token = task.getResult();
                                    Log.d(TAG, "FCM Token: " + token);

                                    // JavaScript 함수 호출하여 FCM 토큰 전달
                                    runOnUiThread(() -> webView.evaluateJavascript(
                                            "window.receiveFcmToken('" + token + "')", null
                                    ));
                                } else {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                }
                            });
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.clearCache(true);
        webView.loadUrl(URI + "/login");

        // Android 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
