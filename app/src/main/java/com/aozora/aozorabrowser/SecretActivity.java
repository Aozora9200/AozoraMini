package com.aozora.aozorabrowser;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.text.InputType;
import android.util.Base64;
import android.util.LruCache;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.support.annotation.Nullable;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebViewDatabase;
import android.webkit.HttpAuthHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.PermissionRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class SecretActivity extends Activity {
    private static final Pattern CACHE_MODE_PATTERN = Pattern.compile("(^|[/.])(?:(chatx2|chatx|chat|auth|login|disk|cgi|session|cloud))($|[/.])", Pattern.CASE_INSENSITIVE);
    private EditText urlEditText;
    private WebView webView; // WebViewをクラス変数として定義
    private ImageButton backButton, forwardButton, bmbutton;
    private FrameLayout webViewContainer;
    private ProgressDialog progressDialog, progressResetDialog;
    private static final String PREF_NAME = "AdvancedBrowserPrefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private boolean darkModeEnabled = false;
    private static final String KEY_BASIC_AUTH = "basic_auth";
    private static final String APPEND_STR = " AozoraBrowser";
    private static final String KEY_ZOOM_ENABLED = "zoom_enabled";
    private static final String KEY_CT3UA_ENABLED = "ct3ua_enabled";
    private static final String KEY_JS_ENABLED = "js_enabled";
    private static Method sSetSaveFormDataMethod;
    private static Method sSetDatabaseEnabledMethod;
    private static Method sSetAppCacheEnabledMethod;
    private static Method sSetAppCachePathMethod;

    private static final int REQUEST_CODE_IMPORT = 1001;

    private RecyclerView recyclerView;

    private ArrayList<WebView> tabs = new ArrayList<>();
    private ArrayList<TabInfo> tabInfos = new ArrayList<>();
    private int currentTabIndex = 0;
    private TabListAdapter_Black tabListAdapter = null;
    private SharedPreferences prefs, pref;
    private int currentHistoryIndex = -1;
    private int totalMatches = 0;
    private int currentMatchIndex = 0;
    private final Map<WebView, Bitmap> webViewFavicons = new HashMap<>();
    private boolean uaEnabled = false;
    private boolean deskuaEnabled = false;
    private boolean ct3uaEnabled = false;
    private boolean jsEnabled = false;
    private boolean imgBlockEnabled = false;

    private WebView preloadedWebView = null;

    private static final String KEY_TABS = "saved_tabs";
    private ValueCallback<Uri[]> filePathCallback;
    private static final String KEY_CURRENT_TAB_ID = "current_tab_id";
    private static final String KEY_BOOKMARKS = "bookmarks";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_UA_ENABLED = "ua_enabled";
    private static final String KEY_DESKUA_ENABLED = "deskua_enabled";
    private static final int REQUEST_CODE_IMPORT_BOOKMARKS = 1001;
    private static final String KEY_IMG_BLOCK_ENABLED = "img_block_enabled";
    private final Map<WebView, String> originalUserAgents = new HashMap<>();
    private int nextTabId = 0;
    private boolean isBackNavigation = false;
    private static final int MAX_HISTORY_SIZE = 100;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LruCache<String, Bitmap> faviconCache;
    private ImageView faviconImageView;
    private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());
    private final ArrayList<WebView> webViews = new ArrayList<>();
    private View findInPageBarView;
    private EditText etFindQuery;
    private TextView tvFindCount;
    private Button btnFindPrev, btnFindNext, btnFindClose;

    private final List<MainActivity.Bookmark> bookmarks = new ArrayList<>();
    private boolean basicAuthEnabled = false;
    private boolean zoomEnabled = false;
    private boolean defaultLoadsImagesAutomatically;
    private boolean defaultLoadsImagesAutomaticallyInitialized = false;
    private final List<MainActivity.HistoryItem> historyItems = new ArrayList<>();
    private AlertDialog dialog;
    // 選択されたURLとタイプを保持
    private String selectedUrl;
    private int selectedType;

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                sSetSaveFormDataMethod = WebSettings.class.getMethod("setSaveFormData", boolean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sSetDatabaseEnabledMethod = WebSettings.class.getMethod("setDatabaseEnabled", boolean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sSetAppCacheEnabledMethod = WebSettings.class.getMethod("setAppCacheEnabled", boolean.class);
                sSetAppCachePathMethod = WebSettings.class.getMethod("setAppCachePath", String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret);
        urlEditText = findViewById(R.id.urlEditText);
        backButton = findViewById(R.id.backButton);
        forwardButton = findViewById(R.id.forwardButton);
        webViewContainer = findViewById(R.id.webViewContainer);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        faviconImageView = (ImageView) findViewById(R.id.favicon);
        ImageButton tabButton = findViewById(R.id.action_tab);
        ImageButton searchButton = findViewById(R.id.action_search);
        ImageButton newtabButton = findViewById(R.id.action_newtab);

        prefs = getSharedPreferences("WebViewTabs", MODE_PRIVATE);
        darkModeEnabled = prefs.getBoolean(KEY_DARK_MODE, false);
        basicAuthEnabled = prefs.getBoolean(KEY_BASIC_AUTH, false);
        zoomEnabled = prefs.getBoolean(KEY_ZOOM_ENABLED, false);
        jsEnabled = prefs.getBoolean(KEY_JS_ENABLED, false);
        imgBlockEnabled = prefs.getBoolean(KEY_IMG_BLOCK_ENABLED, false);
        uaEnabled = prefs.getBoolean(KEY_UA_ENABLED, false);
        deskuaEnabled = prefs.getBoolean(KEY_DESKUA_ENABLED, false);
        ct3uaEnabled = prefs.getBoolean(KEY_CT3UA_ENABLED, false);

        backButton.setOnClickListener(v -> goBack());
        forwardButton.setOnClickListener(v -> goForward());

        // ボタンにクリックイベントを設定
        tabButton.setOnClickListener(v ->
                showTabMenu()
        );

        searchButton.setOnClickListener(v ->
                load("file:///android_asset/secret.html")
        );

        newtabButton.setOnClickListener(v -> {
            addNewTab("file:///android_asset/secret.html");
            Toast.makeText(SecretActivity.this, "新規タブ", Toast.LENGTH_SHORT).show();
        });

        // URL入力でエンターを押したら現在のタブでページを開く
        urlEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                loadUrlInCurrentTab(urlEditText.getText().toString());
                closeKeyboard();
                return true;
            }
            return false;
        });

        urlEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                faviconImageView.setVisibility(hasFocus ? View.GONE : View.VISIBLE);
            }
        });

        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        faviconCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
                WebView current = getCurrentWebView();
                return (current != null && current.getScrollY() > 0);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WebView current = getCurrentWebView();
                if (current != null) current.reload();
            }
        });

        preInitializeWebView();
        if (!defaultLoadsImagesAutomaticallyInitialized && !webViews.isEmpty()) {
            defaultLoadsImagesAutomatically = webViews.get(0).getSettings().getLoadsImagesAutomatically();
            defaultLoadsImagesAutomaticallyInitialized = true;
        }

        // タブ
        addNewTab("file:///android_asset/secret.html");
        switchToTab(currentTabIndex);
        // Action Bar が表示されているか確認
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (selectedUrl == null) return;

        // リンクを長押しした場合のメニュー
        if (selectedType == WebView.HitTestResult.SRC_ANCHOR_TYPE || selectedType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            menu.setHeaderTitle("リンクメニュー");
            menu.add(0, 1, 0, "リンクをコピー");
            menu.add(0, 2, 0, "リンク先をダウンロード");
            menu.add(0, 3, 0, "リンク先を新しいタブで開く");
        }

        // 画像を長押しした場合のメニュー
        if (selectedType == WebView.HitTestResult.IMAGE_TYPE || selectedType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            menu.add(0, 4, 0, "画像を保存");
        }
    }

    // メニュー選択時の処理
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (selectedUrl == null) return false;

        switch (item.getItemId()) {
            case 1: // リンクをコピー
                copyLink(selectedUrl);
                return true;
            case 2: // リンク先をダウンロード
                downloadLink(selectedUrl);
                return true;
            case 3: // 新しいタブで開く
                addNewTab(selectedUrl);
                return true;
            case 4: // 画像を保存
                downloadImage(selectedUrl);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_secret, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem uaItem = menu.findItem(R.id.action_ua);
        if (uaItem != null) uaItem.setChecked(uaEnabled);
        MenuItem deskuaItem = menu.findItem(R.id.action_deskua);
        if (deskuaItem != null) deskuaItem.setChecked(deskuaEnabled);
        MenuItem ct3uaItem = menu.findItem(R.id.action_ct3ua);
        if (ct3uaItem != null) ct3uaItem.setChecked(ct3uaEnabled);
        MenuItem zoomItem = menu.findItem(R.id.action_zoom_toggle);
        if (zoomItem != null) zoomItem.setChecked(zoomEnabled);
        MenuItem jsItem = menu.findItem(R.id.action_js);
        if (jsItem != null) jsItem.setChecked(jsEnabled);
        MenuItem imgItem = menu.findItem(R.id.action_img);
        if (imgItem != null) imgItem.setChecked(imgBlockEnabled);
        MenuItem basicAuthItem = menu.findItem(R.id.action_basic_auth);
        if (basicAuthItem != null) basicAuthItem.setChecked(basicAuthEnabled);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            finish(); // 前の画面に戻る
            return true;
        }
        if (itemId == R.id.menu_tabs) {
            showTabMenu();
            return true;
        } else if (itemId == R.id.menu_downloads) {
            Intent intent = new Intent(this, DownloadListActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_basic_auth) {
            if (!basicAuthEnabled) {
                basicAuthEnabled = true;
                item.setChecked(true);
                Toast.makeText(SecretActivity.this, "Basic認証 ON", Toast.LENGTH_SHORT).show();
            } else {
                basicAuthEnabled = false;
                item.setChecked(false);
                clearBasicAuthCacheAndReload();
                Toast.makeText(SecretActivity.this, "Basic認証 OFF", Toast.LENGTH_SHORT).show();
            }
            prefs.edit().putBoolean(KEY_BASIC_AUTH, basicAuthEnabled).apply();
        } else if (itemId == R.id.action_screenshot) {
            takeScreenshot();
        } else if (itemId == R.id.action_pgdl) {
            startActivity(new Intent(SecretActivity.this, pagedl.class));
        } else if (itemId == R.id.action_negapoji) {
            applyNegapoji();
        } else if (itemId == R.id.action_deskua) {
            if (!deskuaEnabled) {
                if (uaEnabled) {
                    disableUA();
                    uaEnabled = false;
                    prefs.edit().putBoolean(KEY_UA_ENABLED, false).apply();
                }
                if (ct3uaEnabled) {
                    disableCT3UA();
                    ct3uaEnabled = false;
                    prefs.edit().putBoolean(KEY_CT3UA_ENABLED, false).apply();
                }
                enabledeskUA();
                deskuaEnabled = true;
            } else {
                disabledeskUA();
                deskuaEnabled = false;
            }
            item.setChecked(deskuaEnabled);
            prefs.edit().putBoolean(KEY_DESKUA_ENABLED, deskuaEnabled).apply();
            invalidateOptionsMenu();
        } else if (itemId == R.id.action_ct3ua) {
            if (!ct3uaEnabled) {
                if (uaEnabled) {
                    disableUA();
                    uaEnabled = false;
                    prefs.edit().putBoolean(KEY_UA_ENABLED, false).apply();
                }
                if (deskuaEnabled) {
                    disabledeskUA();
                    deskuaEnabled = false;
                    prefs.edit().putBoolean(KEY_DESKUA_ENABLED, false).apply();
                }
                enableCT3UA();
                ct3uaEnabled = true;
            } else {
                disableCT3UA();
                ct3uaEnabled = false;
            }
            item.setChecked(ct3uaEnabled);
            prefs.edit().putBoolean(KEY_CT3UA_ENABLED, ct3uaEnabled).apply();
            invalidateOptionsMenu();
        } else if (itemId == R.id.action_translate) {
            translatePageToJapanese();
            return true;
        } else if (itemId == R.id.action_js) {
            if (item.isChecked()) {
                disablejs();
                jsEnabled = false;
            } else {
                enablejs();
                jsEnabled = true;
            }
            item.setChecked(jsEnabled);
            prefs.edit().putBoolean(KEY_JS_ENABLED, jsEnabled).apply();
        } else if (itemId == R.id.action_img) {
            if (item.isChecked()) {
                disableimgunlock();
                imgBlockEnabled = false;
            } else {
                enableimgblock();
                imgBlockEnabled = true;
            }
            item.setChecked(imgBlockEnabled);
            prefs.edit().putBoolean(KEY_IMG_BLOCK_ENABLED, imgBlockEnabled).apply();
        } else if (itemId == R.id.action_ua) {
            if (!uaEnabled) {
                if (deskuaEnabled) {
                    disabledeskUA();
                    deskuaEnabled = false;
                    prefs.edit().putBoolean(KEY_DESKUA_ENABLED, false).apply();
                }
                if (ct3uaEnabled) {
                    disableCT3UA();
                    ct3uaEnabled = false;
                    prefs.edit().putBoolean(KEY_CT3UA_ENABLED, false).apply();
                }
                enableUA();
                uaEnabled = true;
            } else {
                disableUA();
                uaEnabled = false;
            }
            item.setChecked(uaEnabled);
            prefs.edit().putBoolean(KEY_UA_ENABLED, uaEnabled).apply();
            invalidateOptionsMenu();
        } else if (itemId == R.id.close_secret) {
            finish();
        } else if (itemId == R.id.action_zoom_toggle) {
            if (item.isChecked()) {
                disableZoom();
                zoomEnabled = false;
            } else {
                enableZoom();
                zoomEnabled = true;
            }
            item.setChecked(zoomEnabled);
            prefs.edit().putBoolean(KEY_ZOOM_ENABLED, zoomEnabled).apply();
        }
        return super.onOptionsItemSelected(item);
    }

    private void enableZoom() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setBuiltInZoomControls(true);
        s.setSupportZoom(true);
        reloadCurrentPage();
    }
    private void disableZoom() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        reloadCurrentPage();
    }

    private void translatePageToJapanese() {
        String currentUrl = getCurrentWebView().getUrl();
        if (currentUrl == null || currentUrl.isEmpty()) {
            Toast.makeText(SecretActivity.this, "翻訳するページが見つかりません", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String encoded = URLEncoder.encode(currentUrl, "UTF-8");
            String translateUrl = "https://translate.google.com/translate?hl=ja&sl=auto&tl=ja&u=" + encoded;
            getCurrentWebView().loadUrl(translateUrl);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(SecretActivity.this, "翻訳中にエラーが発生しました", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void applyNegapoji() {
        String js = "javascript:(function(){" +
                "document.documentElement.style.filter='invert(1)';" +
                "var imgs = document.getElementsByTagName('img');" +
                "for(var i=0;i<imgs.length;i++){ imgs[i].style.filter='invert(1)'; }" +
                "})()";
        getCurrentWebView().evaluateJavascript(js, null);
    }

    private void downloadLink(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Uri.parse(url).getLastPathSegment());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(this, "ダウンロードを開始します...", Toast.LENGTH_SHORT).show();
    }

    private void downloadImage(String imageUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, Uri.parse(imageUrl).getLastPathSegment());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Toast.makeText(this, "画像を保存しました", Toast.LENGTH_SHORT).show();
    }

    private void clearBasicAuthCacheAndReload() {
        WebView current = getCurrentWebView();
        if (current != null) {
            current.clearCache(true);
            current.reload();
            reloadCurrentPage();
        }
    }

    private void reloadCurrentPage() {
        WebView current = getCurrentWebView();
        if (current != null) {
            current.clearCache(true);
            String url = current.getUrl();
            if (url != null && !url.isEmpty()) {
                current.loadUrl(url);
            }
        }
    }

    private void takeScreenshot() {
        View root = getWindow().getDecorView().getRootView();
        int w = root.getWidth();
        int h = root.getHeight();
        if (w <= 0 || h <= 0) {
            Toast.makeText(SecretActivity.this, "スクリーンショット取得エラー: ビューサイズが無効", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Handler handler = new Handler(Looper.getMainLooper());
            PixelCopy.request(getWindow(), bmp, new PixelCopy.OnPixelCopyFinishedListener() {
                @Override
                public void onPixelCopyFinished(int copyResult) {
                    if (copyResult == PixelCopy.SUCCESS) {
                        saveScreenshot(bmp);
                    } else {
                        Toast.makeText(SecretActivity.this, "スクリーンショットの取得に失敗しました", Toast.LENGTH_SHORT).show();
                    }
                }
            }, handler);
        } else {
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            root.draw(canvas);
            saveScreenshot(bmp);
        }
    }

    private void saveScreenshot(Bitmap bmp) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(Environment.getExternalStorageDirectory(), "DCIM/AozoraBrowser/Screenshot");
                    if (!dir.exists()) dir.mkdirs();
                    String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String name = ts + ".png";
                    File file = new File(dir, name);
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SecretActivity.this, "スクリーンショットを保存しました: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SecretActivity.this, "スクリーンショット保存中にエラー: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void updateDarkMode() {
        for (WebView wv : webViews) {
            WebSettings s = wv.getSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                s.setForceDark(darkModeEnabled ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
            }
            if (wv == getCurrentWebView()) {
                wv.reload();
            }
        }
    }

    private void enableCT3UA() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; TAB-A03-BR3 Build/02.05.000; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/83.0.4103.106 Safari/537.36");
        Toast.makeText(SecretActivity.this, "CT3UA適用", Toast.LENGTH_SHORT).show();
        reloadCurrentPage();
    }

    private void disableCT3UA() {
        WebSettings s = getCurrentWebView().getSettings();
        String orig = originalUserAgents.get(getCurrentWebView());
        if (orig != null) s.setUserAgentString(orig + APPEND_STR);
        else s.setUserAgentString(APPEND_STR.trim());
        Toast.makeText(SecretActivity.this, "CT3UA解除", Toast.LENGTH_SHORT).show();
        reloadCurrentPage();
    }

    private void enabledeskUA() {
        WebSettings s = getCurrentWebView().getSettings();
        String orig = originalUserAgents.get(getCurrentWebView());
        if (orig == null) orig = s.getUserAgentString();
        String desktop = orig.replace("Mobile", "").replace("Android", "");
        s.setUserAgentString(desktop + APPEND_STR);
        Toast.makeText(SecretActivity.this, "デスクトップ表示有効", Toast.LENGTH_SHORT).show();
        reloadCurrentPage();
    }

    private void disabledeskUA() {
        WebSettings s = getCurrentWebView().getSettings();
        String orig = originalUserAgents.get(getCurrentWebView());
        if (orig != null) s.setUserAgentString(orig + APPEND_STR);
        else s.setUserAgentString(APPEND_STR.trim());
        Toast.makeText(SecretActivity.this, "デスクトップ表示解除", Toast.LENGTH_SHORT).show();
        reloadCurrentPage();
    }

    private void enableUA() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setUserAgentString("DoCoMo/2.0 SH902i(c100;TB)");
        Toast.makeText(SecretActivity.this, "UA適用", Toast.LENGTH_SHORT).show();
        reloadCurrentPage();
    }

    private void disableUA() {
        WebSettings s = getCurrentWebView().getSettings();
        String orig = originalUserAgents.get(getCurrentWebView());
        if (orig != null) s.setUserAgentString(orig + APPEND_STR);
        else s.setUserAgentString(APPEND_STR.trim());
        Toast.makeText(SecretActivity.this, "UA解除", Toast.LENGTH_SHORT).show();
        reloadCurrentPage();
    }

    private void enablejs() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setJavaScriptEnabled(true);
        reloadCurrentPage();
    }
    private void disablejs() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setJavaScriptEnabled(false);
        reloadCurrentPage();
    }
    private void enableimgblock() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setLoadsImagesAutomatically(false);
        reloadCurrentPage();
    }
    private void disableimgunlock() {
        WebSettings s = getCurrentWebView().getSettings();
        s.setLoadsImagesAutomatically(true);
        reloadCurrentPage();
    }

    private void load(String url) {
        tabs.get(currentTabIndex).loadUrl(url);
        tabInfos.get(currentTabIndex).setUrl(url);
    }

    private void applyOptimizedSettings(WebSettings settings) {
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(false);
        settings.setTextZoom(100);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setOffscreenPreRaster(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            settings.setForceDark(darkModeEnabled ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
        }
    }


    private void preInitializeWebView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView wv = new WebView(SecretActivity.this);
                WebSettings s = wv.getSettings();
                applyOptimizedSettings(s);
                String defaultUA = s.getUserAgentString();
                s.setUserAgentString(defaultUA + APPEND_STR);
                preloadedWebView = wv;
            }
        });
    }

    private WebViewClient createWebViewClient(final int index) { // index を追加
        return new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // JavaScript を使用して Favicon を取得
                view.evaluateJavascript("(function() { " +
                        "var link = document.querySelector('link[rel~=\"icon\"]');" +
                        "return link ? link.href : ''; " +
                        "})()", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        value = value.replace("\"", ""); // 取得した URL の " を削除
                        if (!value.isEmpty()) {
                            new SecretActivity.DownloadFaviconTask().execute(value);
                        } else {
                            faviconImageView.setImageResource(R.drawable.transparent_vector); // デフォルトアイコン
                        }
                    }
                });
                String lower = url.toLowerCase();
                boolean matched = CACHE_MODE_PATTERN.matcher(lower).find();
                if (matched) {
                    view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                } else {
                    view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                }
                progressDialog.show();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyCombinedOptimizations(view);
                updateUrlBar(view);
                if (url.startsWith("https://m.youtube.com")) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            injectLazyLoading(view);
                        }
                    }, 1000);
                }
                updateNavigationButtons();
                urlEditText.setText(url);
                updateUrlBar(view);
                int currentTabIndex = tabs.indexOf(view);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                String jsOverride = "javascript:(function(){" +
                        "function notify(){ AndroidBridge.onUrlChange(location.href); }" +
                        "var ps = history.pushState; history.pushState = function(){ ps.apply(history, arguments); notify(); };" +
                        "var rs = history.replaceState; history.replaceState = function(){ rs.apply(history, arguments); notify(); };" +
                        "window.addEventListener('popstate', notify);" +
                        "notify();" +
                        "})()";
                view.loadUrl(jsOverride);

                if (currentTabIndex >= 0) {
                    TabInfo tabInfo = tabInfos.get(currentTabIndex);
                    tabInfo.setUrl(url);
                    tabInfo.setTitle(view.getTitle());
                }
                progressDialog.dismiss();
            }
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                if (!basicAuthEnabled) {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm);
                    return;
                }
                LinearLayout layout = new LinearLayout(SecretActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                int pad = (int)(16 * getResources().getDisplayMetrics().density);
                layout.setPadding(pad, pad, pad, pad);
                final EditText usernameInput = new EditText(SecretActivity.this);
                usernameInput.setHint("ユーザー名");
                usernameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                layout.addView(usernameInput);
                final EditText passwordInput = new EditText(SecretActivity.this);
                passwordInput.setHint("パスワード");
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                layout.addView(passwordInput);
                new AlertDialog.Builder(SecretActivity.this)
                        .setTitle("Basic認証情報を入力")
                        .setView(layout)
                        .setPositiveButton("ログイン", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String user = usernameInput.getText().toString().trim();
                                String pass = passwordInput.getText().toString().trim();
                                if (!user.isEmpty() && !pass.isEmpty()) {
                                    handler.proceed(user, pass);
                                } else {
                                    Toast.makeText(SecretActivity.this, "ユーザー名とパスワードを入力してください", Toast.LENGTH_SHORT).show();
                                    handler.cancel();
                                }
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.cancel();
                            }
                        })
                        .show();
            }
        };
    }

    private class DownloadFaviconTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            try {
                InputStream in = new java.net.URL(url).openStream();
                return BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                faviconImageView.setImageBitmap(result);
            } else {
                faviconImageView.setImageResource(R.drawable.transparent_vector);
            }
        }
    }

    private void updateUrlBar(WebView webView) {
        String currentUrl = webView.getUrl();

        // 指定URLの場合は空白を表示
        if (currentUrl != null && currentUrl.equals("file:///android_asset/secret.html")) {
            urlEditText.setText(""); // URLを非表示
        } else {
            urlEditText.setText(currentUrl); // 通常のURLを表示
        }
    }

    private void injectLazyLoading(WebView wv) {
        String js = "javascript:(function() {" +
                "try {" +
                "var placeholder = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';" +
                "var imgs = document.querySelectorAll('img[src^=\"https://i.ytimg.com/\"]:not([data-lazy-loaded])');" +
                "if(imgs.length===0)return;" +
                "imgs.forEach(function(img){ img.setAttribute('data-lazy-loaded','true');" +
                "if(img.hasAttribute('src')){ img.setAttribute('data-src', img.src); img.src=placeholder; img.style.opacity='0'; img.style.transition='opacity 0.3s'; if(!img.style.transform){ img.style.transform='translateZ(0)'; } } });" +
                "if(window.IntersectionObserver){" +
                "var observer = new IntersectionObserver(function(entries){" +
                "entries.forEach(function(entry){ if(entry.isIntersecting){ var i = entry.target; if(i.dataset.src){ i.src = i.dataset.src; i.removeAttribute('data-src'); i.onload=function(){ i.style.opacity='1'; }; i.onerror=function(){ console.warn('Image load failed: '+i.src); }; } observer.unobserve(i); } });" +
                "}, {root:null, rootMargin:'0px', threshold:0.1});" +
                "imgs.forEach(function(i){ observer.observe(i); });" +
                "} else {" +
                "var loadOnScroll = function(){" +
                "imgs.forEach(function(i){ if(i.dataset.src && (i.getBoundingClientRect().top >=0 && i.getBoundingClientRect().left >=0 && i.getBoundingClientRect().bottom <= (window.innerHeight || document.documentElement.clientHeight) && i.getBoundingClientRect().right <= (window.innerWidth || document.documentElement.clientWidth))){ i.src = i.dataset.src; i.removeAttribute('data-src'); i.onload=function(){ i.style.opacity='1'; }; i.onerror=function(){ console.warn('Image load failed: '+i.src); }; } });" +
                "};" +
                "window.addEventListener('scroll', loadOnScroll);" +
                "window.addEventListener('resize', loadOnScroll);" +
                "window.addEventListener('load', loadOnScroll);" +
                "loadOnScroll();" +
                "}" +
                "} catch(e){ console.error('Lazy loading failed: '+e.message); }" +
                "})();";
        wv.evaluateJavascript(js, null);
    }

    private void applyCombinedOptimizations(WebView wv) {
        String js = "javascript:(function() {" +
                "try {" +
                "var animated = document.querySelectorAll('.animated, .transition');" +
                "animated.forEach(function(el){ if(!el.style.transform){ el.style.transform='translateZ(0)'; } if(!el.style.willChange){ el.style.willChange='transform, opacity'; } });" +
                "var fixedEls = document.querySelectorAll('.fixed');" +
                "fixedEls.forEach(function(el){ if(el.style.position !== 'fixed'){ el.style.position='fixed'; } });" +
                "} catch(e){ console.error('Optimization failed: '+e.message); }" +
                "})();";
        wv.evaluateJavascript(js, null);
    }

    private void startDownload(String url, String userAgent, String contentDisposition, String mimetype) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setMimeType(mimetype);
        request.addRequestHeader("User-Agent", userAgent);
        request.setDescription("ダウンロード中...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String fileName = Uri.parse(url).getLastPathSegment();
        if (fileName == null || fileName.isEmpty()) {
            fileName = "downloaded_file";
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(request);
        Toast.makeText(getApplicationContext(), "ダウンロードを開始します...", Toast.LENGTH_LONG).show();
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void onUrlChange(final String url) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (url.startsWith("https://m.youtube.com/watch") ||
                            url.startsWith("https://chatgpt.com/") ||
                            url.startsWith("https://m.youtube.com/shorts/")) {
                        swipeRefreshLayout.setEnabled(false);
                        urlEditText.setText(url);
                    } else {
                        swipeRefreshLayout.setEnabled(true);
                    }
                }
            });
        }
    }

    private WebView createWebView(int id) {
        WebView webView;
        if (preloadedWebView != null) {
            webView = preloadedWebView;
            preloadedWebView = null;
            preInitializeWebView();
        } else {
            webView = new WebView(this);
        }
        webView.setTag(id); // ✅ タグを設定して、NullPointerExceptionを防止
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setBackgroundColor(Color.WHITE);
        webView.addJavascriptInterface(new SecretActivity.AndroidBridge(), "AndroidBridge");
        webView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        WebSettings s = webView.getSettings();
        webView.setTag(id); // ✅ タグを設定して、NullPointerExceptionを防止

        // ProgressDialog の初期設定
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("ページを読み込み中...");
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (webView != null) {
                    webView.stopLoading(); // ページ読み込みをキャンセル
                }
                dialog.dismiss(); // ダイアログを閉じる
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setGeolocationEnabled(false);
        webView.getSettings().setTextZoom(100);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        String defaultUA = s.getUserAgentString();
        originalUserAgents.put(webView, defaultUA);
        applyOptimizedSettings(s);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (sSetSaveFormDataMethod != null) {
                try {
                    sSetSaveFormDataMethod.invoke(s, false);
                } catch (Exception e) { e.printStackTrace(); }
            }
            if (sSetDatabaseEnabledMethod != null) {
                try {
                    sSetDatabaseEnabledMethod.invoke(s, true);
                } catch (Exception e) { e.printStackTrace(); }
            }
            if (sSetAppCacheEnabledMethod != null && sSetAppCachePathMethod != null) {
                try {
                    sSetAppCacheEnabledMethod.invoke(s, true);
                    sSetAppCachePathMethod.invoke(s, getCacheDir().getAbsolutePath());
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
        if (zoomEnabled) {
            s.setBuiltInZoomControls(true);
            s.setSupportZoom(true);
        } else {
            s.setBuiltInZoomControls(false);
            s.setSupportZoom(false);
        }
        s.setJavaScriptEnabled(!jsEnabled);
        s.setLoadsImagesAutomatically(!imgBlockEnabled);
        if (uaEnabled) {
            s.setUserAgentString("DoCoMo/2.0 SH902i(c100;TB)");
        } else if (deskuaEnabled) {
            String desktopUA = defaultUA.replace("Mobile", "").replace("Android", "");
            s.setUserAgentString(desktopUA + APPEND_STR);
        } else if (ct3uaEnabled) {
            s.setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; TAB-A03-BR3 Build/02.05.000; wv) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/83.0.4103.106 Safari/537.36");
        } else {
            s.setUserAgentString(defaultUA + APPEND_STR);
        }

        // ✅ JavaScript インターフェース追加
        webView.addJavascriptInterface(new SecretActivity.BlobDownloadInterface(), "BlobDownloader");

        // コンテキストメニューを作成
        registerForContextMenu(webView);

        webView.setOnLongClickListener(v -> {
            WebView.HitTestResult result = webView.getHitTestResult();
            if (result == null) {
                return false;
            }

            // メニューを開く
            selectedUrl = result.getExtra(); // 長押しされたURL
            selectedType = result.getType();
            openContextMenu(webView);

            return true;
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                tabInfos.get(currentTabIndex).setTitle(title);
                if (tabListAdapter != null) {
                    tabListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                if (currentTabIndex >= 0 && currentTabIndex < tabInfos.size()) {
                    tabInfos.get(currentTabIndex).setIcon(icon);
                    if (tabListAdapter != null) {
                        tabListAdapter.notifyDataSetChanged();
                    }
                }
            }

        });

        // ✅ ダウンロード機能の追加（API 19 互換）
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (!downloadsDir.canWrite()) {
                // ✅ 権限がない場合に設定画面を開くよう促す
                new AlertDialog.Builder(this)
                        .setTitle("ストレージ権限が必要です")
                        .setMessage("ファイルをダウンロードするにはストレージへの書き込み権限が必要です。設定画面で許可してください。")
                        .setPositiveButton("設定を開く", (dialog, which) -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();
            } else {
                // ✅ 権限がある場合はダウンロード開始
                startDownload(url, userAgent, contentDisposition, mimetype);
            }
        });

        webView.setWebViewClient(createWebViewClient(id)); // id を渡す
        return webView;
    }

    private class BlobDownloadInterface {
        @JavascriptInterface
        public void onBlobDownloaded(String base64Data, String mimeType, String fileName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int comma = base64Data.indexOf(",");
                        String pureBase64 = base64Data.substring(comma + 1);
                        byte[] data = Base64.decode(pureBase64, Base64.DEFAULT);
                        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        if (!downloadDir.exists()) downloadDir.mkdirs();
                        File file = new File(downloadDir, fileName);
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.flush();
                        fos.close();
                        Toast.makeText(SecretActivity.this, "blob ダウンロード完了: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(SecretActivity.this, "blob ダウンロードエラー: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        @JavascriptInterface
        public void onBlobDownloadError(String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SecretActivity.this, "blob ダウンロードエラー: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updateNavigationButtons() {
        WebView currentWebView = tabs.get(currentTabIndex);
        backButton.setEnabled(currentWebView.canGoBack());   // 🔹 戻るボタンの有効/無効を設定
        forwardButton.setEnabled(currentWebView.canGoForward()); // 🔹 進むボタンの有効/無効を設定
    }

    private void goBack() {
        WebView currentWebView = tabs.get(currentTabIndex);
        if (currentWebView.canGoBack()) {
            currentWebView.goBack();
            new android.os.Handler().postDelayed(this::updateNavigationButtons, 300); // 300ms遅延
        }
    }

    private void goForward() {
        WebView currentWebView = tabs.get(currentTabIndex);
        if (currentWebView.canGoForward()) {
            currentWebView.goForward();
            new android.os.Handler().postDelayed(this::updateNavigationButtons, 300);
        }
    }

    private void switchToTab(int index) {
        if (index < 0 || index >= tabs.size()) return;

        // ✅ 全てのWebViewを非表示に
        for (WebView webView : tabs) {
            webView.setVisibility(View.GONE);
        }

        // ✅ 選択したタブを表示
        WebView currentWebView = tabs.get(index);
        currentWebView.setVisibility(View.VISIBLE);
        currentWebView.requestLayout(); // 再描画をリクエスト
        currentWebView.invalidate(); // 画面を再描画

        // JavaScript を使用して Favicon を取得
        currentWebView.evaluateJavascript("(function() { " +
                "var link = document.querySelector('link[rel~=\"icon\"]');" +
                "return link ? link.href : ''; " +
                "})()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                value = value.replace("\"", ""); // 取得した URL の " を削除
                if (!value.isEmpty()) {
                    new SecretActivity.DownloadFaviconTask().execute(value);
                } else {
                    faviconImageView.setImageResource(R.drawable.transparent_vector); // デフォルトアイコン
                }
            }
        });

        currentTabIndex = index;
        urlEditText.setText(currentWebView.getUrl());
        updateUrlBar(currentWebView);
        updateNavigationButtons();
        // 🔹 タブ復元時にタイトルが `null` の場合、強制的に取得
        if (tabInfos.get(index).getTitle().equals("読込中...")) {
            tabInfos.get(index).setTitle(currentWebView.getTitle());
            if (tabListAdapter != null) {
                tabListAdapter.notifyDataSetChanged();
            }
        }
        updateNavigationButtons();
    }

    private void closeTab(int index) {
        if (index < 0 || index >= tabs.size()) return;

        WebView webView = tabs.remove(index);
        tabInfos.remove(index);
        webViewContainer.removeView(webView); // 🔹 WebView を削除

        if (tabs.isEmpty()) {
            addNewTab("file:///android_asset/secret.html");
        } else {
            currentTabIndex = Math.max(0, currentTabIndex - 1);
        }
        switchToTab(currentTabIndex);
    }

    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (view == getCurrentWebView()) {
            faviconImageView.setImageBitmap(icon);
        }
        webViewFavicons.put(view, icon);
        String curUrl = view.getUrl();
        if (curUrl != null) {
            faviconCache.put(curUrl, icon);
            backgroundExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    saveFaviconToFile(curUrl, icon);
                }
            });
        }
    }

    private void saveFaviconToFile(String url, Bitmap bitmap) {
        File faviconsDir = new File(getFilesDir(), "favicons");
        if (!faviconsDir.exists()) {
            faviconsDir.mkdirs();
        }
        File file = new File(faviconsDir, getFaviconFilename(url));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFaviconFilename(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(url.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString() + ".png";
        } catch (Exception e) {
            return Integer.toString(url.hashCode()) + ".png";
        }
    }

    private void loadFaviconFromDisk(String url) {
        File faviconsDir = new File(getFilesDir(), "favicons");
        File file = new File(faviconsDir, getFaviconFilename(url));
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                faviconCache.put(url, bitmap);
            }
        }
    }

    private void showTabMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("タブ一覧");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // タブリスト
        ListView listView = new ListView(this);
        tabListAdapter = new TabListAdapter_Black(this, tabInfos, currentTabIndex);
        listView.setAdapter(tabListAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(currentTabIndex, true);

        // item_tab_add.xml を inflate
        View tabAddView = LayoutInflater.from(this).inflate(R.layout.item_tab_add_black, null);
        ImageButton tabAddButton = tabAddView.findViewById(R.id.tabAddButton);

        dialog = builder.setView(layout)
                .setNeutralButton("閉じる", null)
                .setPositiveButton("すべてのタブを閉じる", (d, which) -> show_check_tabClose())
                .create();

        // タブ追加ボタンの処理
        tabAddButton.setOnClickListener((v) -> {
            addNewTab("file:///android_asset/secret.html");
            dialog.dismiss();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            switchToTab(position);
            dialog.dismiss();
        });

        // レイアウトに追加
        layout.addView(listView);
        layout.addView(tabAddView);

        dialog.show();
    }

    private void show_check_tabClose() {
        new AlertDialog.Builder(this)
                .setTitle("すべてのタブを閉じる")
                .setMessage("本当にすべてのタブを閉じますか？")
                .setPositiveButton("はい", (dialog, which) -> closeAllTabs())
                .setNegativeButton("キャンセル", null)
                .show();
    }

    private void closeAllTabs() {
        if (!tabInfos.isEmpty()) {
            tabInfos.clear();
            tabs.clear();
            currentTabIndex = -1;
            tabListAdapter.notifyDataSetChanged();

            // UI更新を確実に反映した後、新規タブを追加
            new Handler(Looper.getMainLooper()).post(() -> {
                addNewTab("file:///android_asset/secret.html");
                Toast.makeText(this, "すべてのタブを閉じました", Toast.LENGTH_SHORT).show();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            });

        } else {
            Toast.makeText(this, "タブがありません", Toast.LENGTH_SHORT).show();
        }
    }

    public void onTabClose(int position) {
        if (position >= 0 && position < tabInfos.size()) {
            closeTab(position);
            tabListAdapter.notifyDataSetChanged(); // リストビューを更新
            switchToTab(position);
        }
    }


    private void loadUrlInCurrentTab(String url) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            if (url.contains(".")) {
                url = "https://" + url;
            } else {
                // 検索エンジンのURLを付加
                url = "https://www.google.com/search?q=" + url;
            }
        }
        tabs.get(currentTabIndex).loadUrl(url);
        tabInfos.get(currentTabIndex).setUrl(url);
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);
    }

    private void addNewTab(String url) {
        int newId = nextTabId++;
        WebView webView = createWebView(newId);
        webView.loadUrl(url);

        tabs.add(webView);

        // ✅ タブ情報を必ず追加
        if (tabInfos.size() < tabs.size()) {
            tabInfos.add(new TabInfo("読込中...", url, null));
        }

        webViewContainer.addView(webView);
        switchToTab(tabs.size() - 1);
    }

    private WebView getCurrentWebView() {
        return tabs.get(currentTabIndex);
    }

    private void copyLink(String link) {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("リンク", link);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(SecretActivity.this, "リンクをコピーしました", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        WebView currentWebView = tabs.get(currentTabIndex);
        if (currentWebView.canGoBack()) {
            currentWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
