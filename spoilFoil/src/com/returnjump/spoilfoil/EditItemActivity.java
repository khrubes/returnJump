package com.returnjump.spoilfoil;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Kelsey on 6/23/2014.
 */
public class EditItemActivity extends KelseyActivity {

    LinearLayout okayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_item);

        TextView tv = (TextView) findViewById(R.id.select_item_text);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),
                "assets/fonts/spoilFoilFont.otf"));

        fridgeListView = (ListView) findViewById(R.id.foodItemListView);
        super.populateListView(foodItems);
        okayButton = (LinearLayout) findViewById(R.id.okay_bar);
        fridgeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                okayButton.setVisibility(View.VISIBLE);
                itemViewArr[0] = view;
            }
        });

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  finish();
            }
        });


    }
}


