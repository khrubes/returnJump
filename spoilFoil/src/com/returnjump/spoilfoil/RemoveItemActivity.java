package com.returnjump.spoilfoil;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Kelsey on 6/24/2014.
 */
public class RemoveItemActivity extends EditItemActivity {

    LinearLayout okayButton;

    protected void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_item);

        //edit
        TextView tv = (TextView) findViewById(R.id.select_item_text);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),
                "assets/fonts/spoilFoilFont.otf"));


        fridgeListView = (ListView) findViewById(R.id.foodItemListView);
        //override or something
        super.populateListView(foodItems);

        okayButton = (LinearLayout) findViewById(R.id.okay_bar);
        fridgeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //reimplement
                okayButton.setVisibility(View.VISIBLE);
                itemViewArr[0] = view;
            }
        });

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //probably not
                finish();
            }
        });


    }
}
