package com.smartphoneappdev.wcd.alienalbum;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Display all videos in a user-friendly grid view for them to select for viewing
 *
 */
public class DisplayVideos extends AppCompatActivity {

    AlienVideo[] videos= null;
    private GetVideosTask videosTask = null;
    private SearchVideosTask searchVideosTask = null;
    GridView gridView;
    private View mProgressView;
    private View mDisplayView;
    private VideoAdapter videoAdapter;
    private static final int VIDEO_DISPLAY_PERMISSION = 3333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_videos);
        setTitle("Alien Video Album");

        final Button btnDisplay = (Button) findViewById(R.id.goto_display_all);
        //bundle contains search details if coming to this page from search function
        Bundle bundle = getIntent().getExtras();
        if(bundle==null) {
            btnDisplay.setVisibility(View.GONE);
            videosTask = new GetVideosTask(this, ((AlienAlbum) getApplicationContext()).intUserID, false);
            videosTask.execute((Void) null);
        } else {
            btnDisplay.setVisibility(View.VISIBLE);
            searchVideosTask = new SearchVideosTask(this, ((AlienAlbum) getApplicationContext()).intUserID, bundle.getInt("Privacy"), bundle.getInt("Favourite"),  bundle.getString("Category"),  bundle.getString("Tags"));
            searchVideosTask.execute((Void) null);
        }

        mDisplayView = findViewById(R.id.display_videos);
        mProgressView = findViewById(R.id.display_progress);

        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(DisplayVideos.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(DisplayVideos.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(DisplayVideos.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(DisplayVideos.this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if(permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(DisplayVideos.this, permiss,
                    VIDEO_DISPLAY_PERMISSION);
        } else {
            showProgress(true);
        }

        Button btnRecordPage = (Button) findViewById(R.id.goto_record_button);
        btnRecordPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayVideos.this, RecordVideo.class);
                DisplayVideos.this.startActivity(intent);

            }
        });

        Button btnSearchPage = (Button) findViewById(R.id.goto_search_button);
        btnSearchPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayVideos.this, Search.class);
                DisplayVideos.this.startActivity(intent);

            }
        });

        btnDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDisplay.setVisibility(View.GONE);
                videosTask = new GetVideosTask(DisplayVideos.this, ((AlienAlbum) getApplicationContext()).intUserID, false);
                videosTask.execute((Void) null);
            }
        });

    }

    //show round spinner while the videos load
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDisplayView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mDisplayView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // Load all videos to grid view
    public class GetVideosTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private final int userID;
        private final int privacy;

        GetVideosTask(Context context, int userID, boolean privacy) {
            this.context = context;
            this.userID = userID;
            if(privacy){
                this.privacy = 1;
            } else {
                this.privacy = 0;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String link = getString(R.string.server_dir) + "video_list_jrb.php"
                        + "?userID=" + userID
                        + "&privacy=" + privacy;
                Log.d("Video Ref", link);

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));
                String parsedString = reader.readLine();

                JSONObject obj = new JSONObject(parsedString);

                boolean error = obj.getBoolean("error");

                if (error) {
                    return false;
                } else {

                    JSONArray data = obj.getJSONArray("Videos");
                    // Parse the input dates
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    videos = new AlienVideo[obj.getInt("count")];
                    Log.d("Video Ref", "number of videos: " + videos.length);
                    for (int i = 0; i < videos.length; i++) {
                        JSONObject jsonObj2 = data.getJSONObject(i);
                        if (!jsonObj2.getString("video_ref").isEmpty()) {
                            Log.d("Video Ref", jsonObj2.getString("video_ref"));

                            AlienVideo newVid = new AlienVideo(
                                fmt.parse(jsonObj2.getString("date_created")),
                                fmt.parse(jsonObj2.getString("last_played")),
                                jsonObj2.getInt("userID"),
                                jsonObj2.getString("comment"),
                                jsonObj2.getString("thumbnail_ref"),
                                jsonObj2.getString("video_ref"),
                                jsonObj2.getString("genre"),
                                jsonObj2.getString("tags"),
                                jsonObj2.getInt("video_length"),
                                jsonObj2.getInt("favourite")==1,
                                jsonObj2.getInt("privacy")==1
                                );
                            videos[i] = newVid;
                        }
                    }
                    Log.d("Video Ref", "Finished building videos");

                    videoAdapter = new VideoAdapter(DisplayVideos.this, videos);

                    return true;
                }

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            videosTask = null;
            showProgress(false);

            if (success) {
                gridView = (GridView) findViewById(R.id.gridview);
                gridView.setAdapter(videoAdapter);

            } else {

            }

        }

        @Override
        protected void onCancelled() {
            videosTask = null;
            showProgress(false);

        }
    }

    // Load search videos to grid view
    public class SearchVideosTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private final int userID;
        private final int privacy;
        private final int favourite;
        private final String category;
        private final String tags;

        SearchVideosTask(Context context, int userID, int privacy, int favourite, String category, String tags) {
            this.context = context;
            this.userID = userID;
            this.privacy = privacy;
            this.favourite = favourite;
            this.category = category;
            this.tags = tags;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String link = getString(R.string.server_dir) + "search_videos_jrb.php"
                    + "?userID=" + userID
                    + "&privacy=" + privacy
                    + "&favourite=" + favourite
                    + "&genre=" + Uri.encode(category, "UTF-8")
                    + "&tags=" + Uri.encode(tags, "UTF-8");
                Log.d("Video Search", link);

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));
                String parsedString = reader.readLine();

                JSONObject obj = new JSONObject(parsedString);

                boolean error = obj.getBoolean("error");
                int counter = obj.getInt("count");

                Log.d("search", "Count: " + counter);
                if (error || (counter == 0)) {
                    Log.d("search", "No videos found");
                    return false;
                } else {

                    JSONArray data = obj.getJSONArray("Videos");
                    // Parse the input dates
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    videos = new AlienVideo[obj.getInt("count")];
                    Log.d("Video Ref", "number of videos: " + videos.length);
                    for (int i = 0; i < videos.length; i++) {
                        JSONObject jsonObj2 = data.getJSONObject(i);
                        if (!jsonObj2.getString("video_ref").isEmpty()) {
                            Log.d("Video Ref", jsonObj2.getString("video_ref"));

                            AlienVideo newVid = new AlienVideo(
                                    fmt.parse(jsonObj2.getString("date_created")),
                                    fmt.parse(jsonObj2.getString("last_played")),
                                    jsonObj2.getInt("userID"),
                                    jsonObj2.getString("comment"),
                                    jsonObj2.getString("thumbnail_ref"),
                                    jsonObj2.getString("video_ref"),
                                    jsonObj2.getString("genre"),
                                    jsonObj2.getString("tags"),
                                    jsonObj2.getInt("video_length"),
                                    jsonObj2.getInt("favourite")==1,
                                    jsonObj2.getInt("privacy")==1
                            );
                            videos[i] = newVid;
                        }
                    }
                    Log.d("Video Ref", "Finished building videos");

                    videoAdapter = new VideoAdapter(DisplayVideos.this, videos);

                    return true;
                }

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            searchVideosTask = null;
            showProgress(false);

            if (success) {
                gridView = (GridView) findViewById(R.id.gridview);
                gridView.setAdapter(videoAdapter);

            } else {
                Toast.makeText(context, "No videos found", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {
            searchVideosTask = null;
            showProgress(false);

        }
    }

}
