package com.smartphoneappdev.wcd.alienalbum;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

public class RecordVideo extends AppCompatActivity {

    private View mProgressView;
    private TextView mUploadingStatus;

    private static final String TAG = RecordVideo.class.getSimpleName();

    private static final int VIDEO_CAPTURE_REQUEST = 1111;
    private static final int VIDEO_CAPTURE_PERMISSION = 2222;
    private VideoView mVideoView;
    private Uri viduri;
    private String filePath;
    private String fileName;
    private String encodedBitmap;
    private File mediaFile;
    private JSONParser jsonParser = new JSONParser();
    private boolean uploading = false;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        setTitle(getTitle() + ": " + "Record");

        Log.d(TAG, "************************************** enter create...");
        mVideoView = (VideoView) findViewById(R.id.video_image);
        Button btnRecordPage = (Button) findViewById(R.id.goto_record_button);
        btnRecordPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordVideo.this, RecordVideo.class);
                RecordVideo.this.startActivity(intent);
            }
        });
        Button btnEditDetails = (Button) findViewById(R.id.goto_edit_button);
        btnEditDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploading){
                    Toast.makeText(RecordVideo.this, getString(R.string.prompt_wait), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(RecordVideo.this, EditVideo.class);
                    intent.putExtra("video_ref", "videos/" + fileName.replaceFirst("[.][^.]+$", ""));
                    RecordVideo.this.startActivity(intent);
                }
            }
        });
        Button btnDisplayVideos = (Button) findViewById(R.id.goto_display_button);
        btnDisplayVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploading){
                    Toast.makeText(RecordVideo.this, getString(R.string.prompt_wait), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(RecordVideo.this, DisplayVideos.class);
                    RecordVideo.this.startActivity(intent);
                }
            }
        });
        mUploadingStatus = (TextView) findViewById(R.id.uploading_status);
        mUploadingStatus.setVisibility(View.GONE);
        mProgressView = findViewById(R.id.login_progress);

        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(RecordVideo.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(RecordVideo.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(RecordVideo.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(RecordVideo.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(RecordVideo.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }
        if (ContextCompat.checkSelfPermission(RecordVideo.this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if(permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);

            ActivityCompat.requestPermissions(RecordVideo.this, permiss,
                    VIDEO_CAPTURE_PERMISSION);
        } else {
            StartVideoCapture();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIDEO_CAPTURE_REQUEST && resultCode == RESULT_OK) {

            //Uri videoUri = data.getData();

            if (viduri == null){
                Log.i(TAG, "Data URI is null :( " + filePath);
            }
            else{

                // Play video
                MediaController mediaController= new MediaController(this);
                mediaController.setAnchorView(mVideoView);

                mVideoView.setMediaController(mediaController);
                mVideoView.setVideoURI(viduri);
                mVideoView.requestFocus();

                //upload file, thumbnail, and details;
                new UploadVideoTask().execute(viduri);

                MediaPlayer mp = MediaPlayer.create(this, viduri);
                String fileNameWithoutExt = fileName.replaceFirst("[.][^.]+$", "");
                int duration = mp.getDuration();
                mp.release();


                File myFile = new File(viduri.getPath());

                Bitmap thumb = (Bitmap) ThumbnailUtils.createVideoThumbnail(myFile.getAbsolutePath(),MediaStore.Video.Thumbnails.MINI_KIND);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                if (thumb.compress(Bitmap.CompressFormat.JPEG, 30, bao)){
                    byte[] imageArray = bao.toByteArray();
                    encodedBitmap= Base64.encodeToString(imageArray, Base64.DEFAULT);
                } else {
                    encodedBitmap = "";
                }

                new InsertVideoDetails(this, fileNameWithoutExt, duration).execute();

                mVideoView.start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VIDEO_CAPTURE_PERMISSION) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StartVideoCapture();
            }
            else {
                // please give the app permissions!
            }
        }
    }

    private void StartVideoCapture() {
        viduri = getOutputMediaFileUri();
        Log.d(TAG, viduri.getPath());

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, viduri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (6 * 1024 * 1024));
        startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
    }

    @Nullable
    private Uri getOutputMediaFileUri() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (isExternalStorageAvailable()) {
            // get the Uri

            //1. Get the external storage directory

            //File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            Log.d(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            Log.d(TAG, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() );
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), getApplicationContext().getResources().getString(R.string.app_name));

            //2. Create our subdirectory
            if (! mediaStorageDir.exists()) {
                if(! mediaStorageDir.mkdirs()){
                    Log.e(TAG, "Failed to create directory.");
                    return null;
                }
            }
            //3. Create a file name
            //4. Create the file
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

            String path = mediaStorageDir.getAbsolutePath() + File.separator;
            Log.d(TAG, mediaStorageDir.getAbsolutePath());

            fileName = ((AlienAlbum) getApplicationContext()).strUserName + timestamp + ".mp4";
            filePath = path + fileName;
            mediaFile = new File(filePath);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            //5. Return the file's URI

            if(Build.VERSION.SDK_INT> Build.VERSION_CODES.M) {
                return FileProvider.getUriForFile(RecordVideo.this, BuildConfig.APPLICATION_ID + ".provider", mediaFile);
            } else {
                return Uri.fromFile(mediaFile);
            }
        } else {
            return null;
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    //android upload file to server
    //modified code used with permission from http://www.coderefer.com/android-upload-file-to-server/
    public int uploadFile(final String selectedFilePath){

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File selectedFile = new File(filePath);

        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Source File Doesn't Exist: " + filePath);

                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(getString(R.string.server_dir) + "upload_video_jrb.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream

                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + selectedFilePath + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = 0, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    Log.i(TAG, "buffer: " + buffer.toString());
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);
                        }
                        rd.close();
                    } catch (IOException ioex) {

                    }
                }else {
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RecordVideo.this,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(RecordVideo.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(RecordVideo.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }

            return serverResponseCode;
        }
    }

    private class UploadVideoTask extends AsyncTask<Uri, Integer, Long> {

        protected Long doInBackground(Uri... videoUri) {
            uploading = true;
            showProgress(uploading);
            int count = videoUri.length;
            long totalSize = 0;
            Log.i(TAG,"Selected File Path:" + filePath);

            uploadFile("" + Uri.fromFile(mediaFile));

            HashMap<String, String> dataTosend = new HashMap<>();
            dataTosend.put("image", encodedBitmap);
            dataTosend.put("name", fileName.replaceFirst("[.][^.]+$", ""));

            JSONObject json = jsonParser.makeHttpRequest(RecordVideo.this, "POST", dataTosend);

            if (json != null) {
                Log.d("JSON result", json.toString());

            }

            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            if(progress[0]%10 == 0) {
                Toast.makeText(RecordVideo.this, progress[0] + "% uploaded", Toast.LENGTH_SHORT).show();
            }

        }

        protected void onPostExecute(Long result) {
            uploading = false;
            showProgress(uploading);
        }
    }

    public class InsertVideoDetails extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private final String vid_file_name;
        private final long vid_length;

        private String strResult;
        //upload bitmap

        InsertVideoDetails(Context context, String file_name, long length) {
            this.context = context;
            vid_file_name = file_name;
            vid_length = length;

            Log.d(TAG, vid_file_name + " " + vid_length);

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String link = getString(R.string.server_dir) + "video_details_jrb.php"
                        + "?file_name=" + Uri.encode(vid_file_name, "UTF-8")
                        + "&length=" + vid_length
                        + "&userID=" + ((AlienAlbum) getApplicationContext()).intUserID;

                Log.d(TAG, link);

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
                Log.d(TAG, strResult);

                return !obj.getBoolean("error");

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Log.d(TAG, "written to vid db");
            } else {
                Toast.makeText(getApplicationContext(), "error writing to MySQL DB", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    // Used with permission from: http://stackoverflow.com/questions/34085599/upload-image-to-php-server-with-jsonparser
    public class JSONParser {

        String charset = "UTF-8";
        HttpURLConnection conn;
        DataOutputStream wr;
        StringBuilder result = new StringBuilder();
        URL urlObj;
        JSONObject jObj = null;
        StringBuilder sbParams;
        String paramsString;

        public JSONObject makeHttpRequest(final Context context,String method,
                                          HashMap<String, String> params) {

            sbParams = new StringBuilder();
            int i = 0;
            for (String key : params.keySet()) {
                try {
                    if (i != 0){
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), charset));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }

            if (method.equals("POST")) {
                // request method is POST
                try {
                    urlObj = new URL(getString(R.string.server_dir) + "upload_bitmap_jrb.php");
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Charset", charset);
                    conn.setReadTimeout(10*1000);//orijinal 10000
                    conn.setConnectTimeout(15*1000);//orijinal 15000
                    conn.connect();
                    paramsString = sbParams.toString();
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                //Receive the response from the server
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Log.d("JSON Parser", "result: " + result.toString());

            } catch (IOException e) {

                Log.d("JSON Parser", "Connection Problem");
                e.printStackTrace();
            }

            conn.disconnect();

            // try parse the string to a JSON object
            try {
                jObj = new JSONObject(result.toString());
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON Object
            return jObj;
        }
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mUploadingStatus.setVisibility(View.VISIBLE);
            mUploadingStatus.setText(show ? getString(R.string.prompt_wait) : getString(R.string.prompt_uploaded) );

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
            mUploadingStatus.setText(show ? getString(R.string.prompt_uploaded) : getString(R.string.prompt_wait) );
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(RecordVideo.this, DisplayVideos.class);
        RecordVideo.this.startActivity(intent);

    }
}



