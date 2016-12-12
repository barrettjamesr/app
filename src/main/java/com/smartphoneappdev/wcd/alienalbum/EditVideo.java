package com.smartphoneappdev.wcd.alienalbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Edit video details to aid in search
 */
public class EditVideo extends AppCompatActivity {
    AlienVideo alienVideo;
    private GetVideosTask videosTask = null;
    private UpdateVideoDetails updateDetails = null;

    String[] strCategories;

    private TextView lblVideoRef;
    private EditText editComment;
    private Spinner spnCategory;
    private Switch swFavourite;
    private Switch swPrivacy;
    private EditText editTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);
        setTitle(getTitle() + ": " + "Edit Video");

        Bundle bundle = getIntent().getExtras();
        final String video_ref = bundle.getString("video_ref");
        strCategories = getResources().getStringArray(R.array.categories_array);

        videosTask = new GetVideosTask(this, video_ref);
        videosTask.execute((Void) null);

        lblVideoRef = (TextView) findViewById(R.id.videoRef);
        editComment = (EditText) findViewById(R.id.editComment);
        spnCategory = (Spinner) findViewById(R.id.spnCategory);
        swFavourite = (Switch) findViewById(R.id.swFavourite);
        swPrivacy = (Switch) findViewById(R.id.swPrivacy);
        editTags = (EditText) findViewById(R.id.editTags);

        final Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        final Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cancel = false;
                View focusView = null;

                if (editComment.getText().toString().length()>256){
                    editComment.setError("Name too long");
                    focusView = editComment;
                    cancel = true;
                }

                if (editTags.getText().toString().length()>256){
                    editTags.setError("too many tags, sorry!");
                    focusView = editTags;
                    cancel = true;
                }
                if(!cancel) {
                    updateDetails = new UpdateVideoDetails(EditVideo.this, video_ref,
                            editComment.getText().toString(),
                            strCategories[spnCategory.getSelectedItemPosition()],
                            (swFavourite.isChecked() ? 1 : 0),
                            (swPrivacy.isChecked() ? 1 : 0),
                            editTags.getText().toString()
                    );
                    updateDetails.execute((Void) null);
                    Intent intent = new Intent(EditVideo.this, DisplayVideos.class);
                    EditVideo.this.startActivity(intent);
                } else {
                    focusView.requestFocus();
                }

            }
        });

    }

    public class GetVideosTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private final String video_ref;

        GetVideosTask(Context context, String video_ref) {
            this.context = context;
            this.video_ref = video_ref;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String link = getString(R.string.server_dir) + "single_video_jrb.php"
                        + "?video_ref=" + video_ref;

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
                    JSONObject jsonObj2 = data.getJSONObject(0);
                    if (!jsonObj2.getString("video_ref").isEmpty()) {
                        Log.d("Video Ref", jsonObj2.getString("video_ref"));

                        alienVideo = new AlienVideo(
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
                    }
                    Log.d("Video Ref", "Finished building videos");
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
                lblVideoRef.setText(alienVideo.getVideoRef());
                editComment.setText(alienVideo.getComment());
                editComment.setHint(alienVideo.getComment());
                spnCategory.setSelection(0);
                for(int i=0; i < strCategories.length; i++){
                    if(strCategories[i].equals(alienVideo.getGenre())){
                        spnCategory.setSelection(i);
                    }
                }
                swFavourite.setChecked(alienVideo.getIsFavourite());
                swPrivacy.setChecked(alienVideo.getIsPrivate());
                editTags.setText(alienVideo.getTags());

            } else {

            }
        }

        @Override
        protected void onCancelled() {
            videosTask = null;

        }
    }

    public class UpdateVideoDetails extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        String vid_ref;
        String comment;
        String genre;
        int fav;
        int privacy;
        String tags;

        private String strResult;

        UpdateVideoDetails(Context context, String vid_ref, String comment, String genre, int fav, int privacy, String tags) {
            this.context = context;
            this.vid_ref=vid_ref;
            this.comment=comment;
            this.genre=genre;
            this.fav=fav;
            this.privacy=privacy;
            this.tags=tags.replace("#","").replace(","," ").replace(";"," ").replace("  "," ");

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                String link = getString(R.string.server_dir) + "update_video_jrb.php"
                        + "?video_ref=" + Uri.encode(vid_ref, "UTF-8")
                        + "&comment=" + Uri.encode(comment, "UTF-8")
                        + "&genre=" + Uri.encode(genre, "UTF-8")
                        + "&favourite=" + fav
                        + "&privacy=" + privacy
                        + "&tags=" + Uri.encode(tags, "UTF-8");

                Log.d("TAG", link);

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));
                String parsedString = reader.readLine();

                JSONObject obj = new JSONObject(parsedString);

                strResult = obj.getString("Result");
                Log.d("TAG", strResult);

                return !obj.getBoolean("error");

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Toast.makeText(getApplicationContext(), "Changes Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "error writing to MySQL DB", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}
