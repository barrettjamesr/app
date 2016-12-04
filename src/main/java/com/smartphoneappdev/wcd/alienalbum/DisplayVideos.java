package com.smartphoneappdev.wcd.alienalbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class DisplayVideos extends AppCompatActivity {

    AlienVideo[] videos= null;
    private GetVideosTask videosTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_videos);

        videosTask = new GetVideosTask(this, ((AlienAlbum) getApplicationContext()).intUserID, false);
        videosTask.execute((Void) null);

        if(videos!=null) {
            GridView gridView = (GridView) findViewById(R.id.gridview);
            VideoAdapter videoAdapter = new VideoAdapter(this, videos);
            gridView.setAdapter(videoAdapter);
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
    }

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
                String link = getString(R.string.server_dir) + "video_list.php"
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
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonObj2 = data.getJSONObject(i);
                        if (!jsonObj2.getString("video_ref").isEmpty()) {
                            Log.d("Video Ref", jsonObj2.getString("video_ref"));
                            Date dateCreated = fmt.parse(jsonObj2.getString("date_created"));
                            Log.d("Video Ref", "Date: " + dateCreated.toString());

                            Log.d("Video Ref", "Date: " + fmt.parse(jsonObj2.getString("date_created")));
                            Log.d("Video Ref", "played: " + fmt.parse(jsonObj2.getString("last_played")));
                            Log.d("Video Ref", "ID: " + jsonObj2.getInt("userID"));
                            Log.d("Video Ref", "name: " + jsonObj2.getString("user_name"));
                            Log.d("Video Ref", "comment: " + jsonObj2.getString("comment"));
                            Log.d("Video Ref", "thumb: " + jsonObj2.getString("thumbnail_ref"));
                            Log.d("Video Ref", "vid: " + jsonObj2.getString("video_ref"));
                            Log.d("Video Ref", "genre: " + jsonObj2.getString("genre"));
                            Log.d("Video Ref", "tag: " + jsonObj2.getString("tags"));
                            Log.d("Video Ref", "length: " + jsonObj2.getInt("video_length"));
                            Log.d("Video Ref", "favour: " + jsonObj2.getInt("favourite"));
                            Log.d("Video Ref", "priv: " + jsonObj2.getInt("privacy"));

                            AlienVideo newVid = new AlienVideo(
                                fmt.parse(jsonObj2.getString("date_created")),
                                fmt.parse(jsonObj2.getString("last_played")),
                                jsonObj2.getInt("userID"),
                                jsonObj2.getString("user_name"),
                                jsonObj2.getString("comment"),
                                jsonObj2.getString("thumbnail_ref"),
                                jsonObj2.getString("video_ref"),
                                jsonObj2.getString("genre"),
                                jsonObj2.getString("tags"),
                                jsonObj2.getInt("video_length"),
                                jsonObj2.getInt("favourite")==1,
                                jsonObj2.getInt("privacy")==1
                                );
                            Log.d("Video Ref", "Video Created" );
                            videos[i] = newVid;
                            Log.d("Video Ref", "Video Inserted" );
                        }
                    }

                    return true;
                }

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            videosTask = null;

            if (success) {

            } else {
                // TODO
            }
        }

        @Override
        protected void onCancelled() {
            videosTask = null;
        }
    }
}
