package com.returnjump.spoilfoil;

import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.squareup.seismic.ShakeDetector;

public class KelseyActivity extends FragmentActivity implements ShakeDetector.Listener, CalendarDatePickerDialog.OnDateSetListener, EditNameFragment.OnEditNameButtonClickedListener{

    protected ArrayAdapter<FoodItem> adapter;
    protected ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();
    protected FridgeDbHelper dbHelper;
    private SwipeDismissListViewTouchListener touchListener;
    protected ListView fridgeListView;
    private SensorManager sensorManager;
    private ShakeDetector sd;
    //used to communicate views back from EditItemActivity
    protected View[] itemViewArr;
    //private Sensor mAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelsey);
        dbHelper = new FridgeDbHelper(this);
        // dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1); // Use this to delete database
        fridgeListView = (ListView) findViewById(R.id.foodItemListView);
        this.initializeSwipeDismissListener();
        fridgeListView.setOnTouchListener(touchListener);
        fridgeListView.setOnScrollListener(touchListener.makeScrollListener());
        this.initializeLongClickListener();
        this.setUpToolbar();
        populateListView(foodItems);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sd = new ShakeDetector(this);
        sd.start(sensorManager);
        itemViewArr = new View[1];
        setupFonts();
        Toast.makeText(this, " on create called", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        populateListView(foodItems);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sd = new ShakeDetector(this);
        sd.start(sensorManager);
        if(itemViewArr[0]!=null){
            editItem(itemViewArr[0]);
        }
        Toast.makeText(this, " on resume called", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sd = new ShakeDetector(this);
        sd.stop();
    }

    private Calendar getCalendar(int daysFromToday) {
        Calendar c = GregorianCalendar.getInstance();
        c.add(Calendar.DATE, daysFromToday);
        return c;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.kelsey, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_arturo) {
            Intent intent = new Intent(this, ArturoActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void copyDatabaseToList() {
        Cursor c = dbHelper.read(null);
        c.moveToFirst();
        foodItems.clear();
        while (!c.isAfterLast()) {
            long id = c.getLong(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable._ID)
            );
            String foodName = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM)
            );
            Calendar expiryDate = FridgeDbHelper.stringToCalendar(c.getString(
                            c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE)), DatabaseContract.FORMAT_DATE
            );
            boolean dismissed = c.getInt(c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED)) != 0;

            if (!dismissed) {
                foodItems.add(new FoodItem(id, foodName, expiryDate, 0));
            }
            c.moveToNext();
        }
    }

    // We should sort our list by descending expiry date
    public void populateListView(ArrayList<FoodItem> list) {
        copyDatabaseToList();
        adapter = new MyFoodAdapter(this, R.layout.list_fooditems, list);
        TextView emptyFridge = (TextView) findViewById(R.id.empty_fridge);
        fridgeListView.setAdapter(adapter);
        //toggles the "your fridge is empty :(" eventually we should
        // have a cool graphic of an empty fridge here or something
        if (list.size() == 0) {
            fridgeListView.setVisibility(View.GONE);
            emptyFridge.setVisibility(View.VISIBLE);
        } else {
            emptyFridge.setVisibility(View.GONE);
            fridgeListView.setVisibility(View.VISIBLE);
        }
    }

    protected void setupFonts(){
        TextView tv = (TextView) findViewById(R.id.spoilFoil);
        tv.setTypeface(Typeface.createFromAsset(getAssets(),
                "assets/fonts/spoilFoilFont.otf"));
    }

   //helper method to consolidate setting views
    private void updateListView(ArrayList<FoodItem> list) {
        TextView emptyFridge = (TextView) findViewById(R.id.empty_fridge);
        adapter.notifyDataSetChanged();
        //toggles the "your fridge is empty :(" eventually we should
        // have a cool graphic of an empty fridge here or something
        if (list.size() == 0) {
            fridgeListView.setVisibility(View.GONE);
            emptyFridge.setVisibility(View.VISIBLE);
        } else {
            emptyFridge.setVisibility(View.GONE);
            fridgeListView.setVisibility(View.VISIBLE);
        }
    }

    private int insertToSortedList(FoodItem item) {
        int n = foodItems.size();
        int i = 0;
        while ((i < n) && ((item.getDaysGood() > foodItems.get(i).getDaysGood()) ||
                ((item.getDaysGood() == foodItems.get(i).getDaysGood()) &&
                        (item.getFoodName().compareTo(foodItems.get(i).getFoodName())) > 0))) {
            i++;
        }
        foodItems.add(i, item);
        return i;
    }

    public void initializeLongClickListener() {
        fridgeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                editItem(view);
                return true;
            }
        });
    }

    public void initializeSwipeDismissListener() {
        touchListener =
                new SwipeDismissListViewTouchListener(fridgeListView, new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        // reverseSortedPositions always has a length = 1
                        int position = reverseSortedPositions[0];
                        //View child = MyApplication.getViewByPosition(position, fridgeListView);
                        View child = adapter.getView(position, null, fridgeListView);
                        // Set visible to false in the database for the item that was swiped
                        if (child != null)
                        {
                            long rowId = (Long) child.getTag(R.id.food_item_id);
                            dbHelper.update(rowId, null, null, null, DatabaseContract.BOOL_TRUE, null, null, null, null, DatabaseContract.BOOL_TRUE, null, null);
                            adapter.remove(adapter.getItem(position));
                            updateListView(foodItems);
                        } else
                        {
                            Toast.makeText(getApplicationContext(), "Delete failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void hearShake() {
        Toast.makeText(this, "Expired food has been dismissed.", Toast.LENGTH_SHORT).show();
    }

    //TODO edit activity, delete, undo
    private void setUpToolbar(){
        ImageButton addItem = (ImageButton) findViewById(R.id.toolbar_add_item);
        addItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               addItem();

            }
        });
        ImageButton editItem = (ImageButton) findViewById(R.id.toolbar_edit_item);
        editItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KelseyActivity.this, EditItemActivity.class);
                startActivity(intent);
            }
        });
        ImageButton cameraButton = (ImageButton) findViewById(R.id.toolbar_camera);
        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KelseyActivity.this, TastiActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * used to handle add, edit, undo, remove, and camera operations
     */
        private String EDIT_FRAG_TAG = "editNameTag";
        private String CAL_PICKER_TAG = "editDateTag";
        private EditNameFragment editNameFragment;
        private boolean addingNewItem;
        private Calendar expiryDate;
        private View editItemView;
        //undoOperation class   && stack of undoOperations
        //TODO editing number of items.
        public void addItem(){
            addingNewItem = true;
            editNameDialog();
        }

        public void editItem(View v){
            editItemView = v;
            editNameDialog();
        }
        //TODO remove item
        //TODO undo item operation
        public void editNameDialog(){
            String itemName;
            String itemDate;
            Long editItemRowId;
            if(!addingNewItem) {
                editItemRowId = (Long) editItemView.getTag(R.id.food_item_id);
                FridgeItem fridgeItem = dbHelper.getRowById(editItemRowId, true);
                itemName = fridgeItem.getFoodItem();
                itemDate = fridgeItem.getExpiryDate();
            }else{
                editItemRowId = (long)0;
                itemName = "addingItemName";
                itemDate = "addingItemDate";
            }
            editNameFragment = new EditNameFragment();
            Bundle args = new Bundle();
            args.putString("name", itemName);
            args.putString("date", itemDate);
            args.putLong("rowId", editItemRowId);
            editNameFragment.setArguments(args);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(editNameFragment, EDIT_FRAG_TAG);
            fragmentTransaction.addToBackStack(EDIT_FRAG_TAG);
            fragmentTransaction.commit();
            //onEditNameButtonClicked called next
        }

        @Override
        public void onEditNameButtonClicked() {
            editDateDialog();
        }

        private void editDateDialog(){
            Calendar c;
            if(!addingNewItem) {
                c = FridgeDbHelper.stringToCalendar(editNameFragment.getArguments().getString("date"), DatabaseContract.FORMAT_DATE);
            }else {
                c = Calendar.getInstance();
            }
            CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                    .newInstance(KelseyActivity.this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            calendarDatePickerDialog.setYearRange(c.get(Calendar.YEAR), calendarDatePickerDialog.getMaxYear());
            calendarDatePickerDialog.show(getSupportFragmentManager(), CAL_PICKER_TAG);
            //onDateSet called next
        }

        @Override
        public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
            expiryDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            getUpdatedItem();
        }

        private FoodItem getUpdatedItem(){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(getSupportFragmentManager().findFragmentByTag(EDIT_FRAG_TAG));
            ft.commit();
            FoodItem foodItem;
            if(!addingNewItem){
                foodItem = new FoodItem(editNameFragment.getArguments().getLong("rowId"),editNameFragment.getArguments().getString("name"), expiryDate, 0);
                Toast.makeText(getApplicationContext(), "Item edited!", Toast.LENGTH_SHORT).show();
                dbHelper.update(editNameFragment.getArguments().getLong("rowId"), editNameFragment.getArguments().getString("name"), expiryDate, null, null, null, null, DatabaseContract.BOOL_TRUE,
                        null, null, null, null);
            }else{
                String foodName = editNameFragment.getArguments().getString("name");
                long id = dbHelper.put(foodName, expiryDate, foodName, null, null);
                foodItem = new FoodItem(id, foodName, expiryDate, 0);
               addingNewItem = false;
            }
            int index = insertToSortedList(foodItem);
            updateListView(foodItems);
            fridgeListView.smoothScrollToPosition(index);
            editNameFragment=null;
            itemViewArr[0] = null;
            return foodItem;
        }

}
