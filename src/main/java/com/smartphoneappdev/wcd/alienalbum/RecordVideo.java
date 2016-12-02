package com.smartphoneappdev.wcd.alienalbum;


import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.AlphabeticIndex;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class RecordVideo extends Activity {

    private static final String TAG = RecordVideo.class.getSimpleName();

    private static final int VIDEO_CAPTURE_REQUEST = 1111;
    private static final int VIDEO_CAPTURE_PERMISSION = 2222;
    private VideoView mVideoView;
    private Uri viduri;
    private String filePath;
    private String fileName;
    private File mediaFile;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);

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
        Button btnDisplayVideos = (Button) findViewById(R.id.goto_display_button);
        btnDisplayVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordVideo.this, DisplayVideos.class);
                RecordVideo.this.startActivity(intent);
            }
        });

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

            //uploadFile();
            new UploadVideoTask().execute(data.getData());

            Uri videoUri = data.getData();

            // Play video
            MediaController mediaController= new MediaController(this);
            mediaController.setAnchorView(mVideoView);

            mVideoView.setMediaController(mediaController);
            mVideoView.setVideoURI(videoUri);
            mVideoView.requestFocus();

            mVideoView.start();
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

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, viduri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (4 * 1024 * 1024));
        startActivityForResult(intent, VIDEO_CAPTURE_REQUEST);
    }

    @Nullable
    private Uri getOutputMediaFileUri() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        if (isExternalStorageAvailable()) {
            // get the Uri

            //1. Get the external storage directory
//            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath());
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

            String path = mediaStorageDir.getPath() + File.separator;

            filePath = path + ((AlienAlbum) getApplicationContext()).strUserName + timestamp + ".mp4";
            mediaFile = new File(filePath);

            fileName = ((AlienAlbum) getApplicationContext()).strUserName + System.currentTimeMillis()+".mp4";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            //Uri fileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            //5. Return the file's URI
            return FileProvider.getUriForFile(RecordVideo.this, BuildConfig.APPLICATION_ID + ".provider", mediaFile);
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
            //dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Source File Doesn't Exist: " + filePath);
                    Log.i(TAG, "Source File Doesn't Exist: " + selectedFilePath);
                    Log.i(TAG, "Source File Doesn't Exist: " + fileName);

                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(getString(R.string.server_dir) + "upload_video.php");
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
                //dataOutputStream.writeBytes (urlParameters);

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
                    //return sb.toString();
                }else {
                    //return "Could not upload";
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
            //dialog.dismiss();
            return serverResponseCode;
        }
    }

    private class UploadVideoTask extends AsyncTask<Uri, Integer, Long> {

        protected Long doInBackground(Uri... videoUri) {
            int count = videoUri.length;
            long totalSize = 0;
            Log.i(TAG,"Selected File Path:" + filePath);

            uploadFile("" + Uri.fromFile(mediaFile));
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            if(progress[0]%10 == 0) {
                Toast.makeText(RecordVideo.this, progress[0] + "% uploaded", Toast.LENGTH_SHORT).show();
            }

            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            Toast.makeText(RecordVideo.this, "Uploaded", Toast.LENGTH_SHORT).show();
            //showDialog("Downloaded " + result + " bytes");
        }
    }
}



