package com.smartphoneappdev.wcd.alienalbum;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Search extends AppCompatActivity {

    private Spinner spnCategory;
    private Switch swFavourite;
    private Switch swPrivacy;
    private EditText editTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle(getTitle() + ": " + "Search");

        spnCategory = (Spinner) findViewById(R.id.spnCategory);
        swFavourite = (Switch) findViewById(R.id.swFavourite);
        swPrivacy = (Switch) findViewById(R.id.swPrivacy);
        editTags = (EditText) findViewById(R.id.editTags);

        List<String> Categories = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.categories_array)));
        Categories.add(0, getString(R.string.allCats));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, Categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(dataAdapter);
        spnCategory.setSelection(0);

        final Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        final Button btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Search.this, DisplayVideos.class);
                intent.putExtra("Category", spnCategory.getSelectedItem().toString());
                intent.putExtra("Favourite", (swFavourite.isChecked() ? 1 : 0));
                intent.putExtra("Privacy", (swPrivacy.isChecked() ? 1 : 0));
                intent.putExtra("Tags", editTags.getText().toString().replace("#","").replace(",","").replace(";","").replace(" ",""));
                Search.this.startActivity(intent);
            }
        });
    }
}
