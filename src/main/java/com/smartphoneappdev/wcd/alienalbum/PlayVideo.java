package com.smartphoneappdev.wcd.alienalbum;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlayVideo extends AppCompatActivity {

    // Called when the activity is first created.

    private static final String TAG = PlayVideo.class.getSimpleName();
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

    private WebView mWebView;
    private ImageView editVideo;
    private ImageView imageViewBlank;
    private TextView txtComment;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraVideoPath;
    private String url = "";
    private int currentVideo;
    private int[] users;
    private String[] videos;
    private String[] comments;

    static final int WEB_VIEW_PERMISSION = 7777;

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                if (mCameraVideoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraVideoPath)};
                } else {
                    Toast.makeText(PlayVideo.this, "No video found", Toast.LENGTH_SHORT).show();
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                } else {
                    Toast.makeText(PlayVideo.this, "No video found", Toast.LENGTH_SHORT).show();
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
        return;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        setTitle(getTitle() + ": " + "View");

        Bundle bundle = getIntent().getExtras();
        currentVideo = bundle.getInt("CurrentVid");
        users = bundle.getIntArray("AllUsers");
        videos = bundle.getStringArray("AllVideos");
        comments = bundle.getStringArray("AllComments");

        Button btnRecordPage = (Button) findViewById(R.id.goto_record_button);
        btnRecordPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.loadUrl("about:blank");
                Intent intent = new Intent(PlayVideo.this, RecordVideo.class);
                PlayVideo.this.startActivity(intent);
            }
        });

        final Button btnBackPage = (Button) findViewById(R.id.back_button);
        btnBackPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });
        final Button btnPrevVideo = (Button) findViewById(R.id.prev_button);
        btnPrevVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.loadUrl("about:blank");
                if(currentVideo>0){
                    currentVideo = currentVideo -1;
                } else {
                    currentVideo = videos.length-1;
                }
                txtComment.setText(comments[currentVideo]);
                StartWebView();
            }
        });
        final Button btnNextVideo = (Button) findViewById(R.id.next_button);
        btnNextVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebView.loadUrl("about:blank");
                if(currentVideo<videos.length-1){
                    currentVideo = currentVideo +1;
                } else {
                    currentVideo = 0;
                }
                txtComment.setText(comments[currentVideo]);
                StartWebView();
            }
        });

        editVideo = (ImageView) findViewById(R.id.imageview_edit);
        editVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (users[currentVideo] == ((AlienAlbum) getApplicationContext()).intUserID) {
                    mWebView.loadUrl("about:blank");
                    Intent intent = new Intent(PlayVideo.this, EditVideo.class);
                    intent.putExtra("video_ref", videos[currentVideo]);
                    PlayVideo.this.startActivity(intent);
                }
            }
        });

        mWebView = (WebView) findViewById(R.id.webView);
        txtComment = (TextView) findViewById(R.id.videoComment);

        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(PlayVideo.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(PlayVideo.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }
/*
        if(permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);

            ActivityCompat.requestPermissions(PlayVideo.this, permiss,
                    WEB_VIEW_PERMISSION);
        } else {
        }
*/
        txtComment.setText(comments[currentVideo]);
        StartWebView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WEB_VIEW_PERMISSION) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StartWebView();
            }
            else {
            }
        }
    }

    private void StartWebView(){

        setUpWebViewDefaults(mWebView);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                // Create the File where the video should go
                File videoFile = null;
                try {
                    videoFile = createVideoFile();
                    takeVideoIntent.putExtra("VideoPath", mCameraVideoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "Unable to create video File", ex);
                }

                // Continue only if the File was successfully created
                if (videoFile != null) {
                    mCameraVideoPath = "file:" + videoFile.getAbsolutePath();
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(videoFile));
                } else {
                    takeVideoIntent = null;
                }


                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("video/*");

                Intent[] intentArray;
                if (takeVideoIntent != null) {
                    intentArray = new Intent[]{takeVideoIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Video Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

                return true;
            }
        });

        openBrowser(getString(R.string.server_dir) + videos[currentVideo]);

    }

    // Open a browser on the URL specified in the text box
    private void openBrowser(String url) {
        Log.d(TAG, "Url: " + url);

        if(!url.trim().startsWith("http://")){
            url="http://"+url.trim();
        }
        mWebView.loadUrl(url.trim());
        editVideo.setVisibility(users[currentVideo] == ((AlienAlbum) getApplicationContext()).intUserID ? View.VISIBLE : View.GONE);

    }

    private File createVideoFile() throws IOException {
        // Create an video file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES);
        File videoFile = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        return videoFile;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpWebViewDefaults(WebView webView) {
        WebSettings settings = webView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // We set the WebViewClient to ensure links are consumed by the WebView rather
        // than passed to a browser if it can
        mWebView.setWebViewClient(new WebViewClient());
    }

    @Override
    public void onBackPressed()
    {
        mWebView.loadUrl("about:blank");
        super.onBackPressed();
    }
}





