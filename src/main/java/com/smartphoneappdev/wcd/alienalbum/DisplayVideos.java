package com.smartphoneappdev.wcd.alienalbum;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DisplayVideos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_videos);


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
}
