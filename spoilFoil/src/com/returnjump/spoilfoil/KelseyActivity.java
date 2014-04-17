package com.returnjump.spoilfoil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class KelseyActivity extends Activity {

	private ArrayAdapter<FoodItem> adapter;
	private ArrayList<FoodItem> foodItems = new ArrayList<FoodItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kelsey);
		// Show the Up button in the action bar.
		setupActionBar();

		/*
		 * Change KelseyActivity to whatever activity will be handling push notifications
		 */
		PushService.setDefaultPushCallback(this, KelseyActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();

		// added for
		// testing
		// purposes
		
		
		FoodItem milk = new FoodItem("Milk", getCalendar(1), 0);
		FoodItem bread = new FoodItem("Bread", getCalendar(6), 2);
		FoodItem eggs = new FoodItem("Eggs", getCalendar(7), 12);
		FoodItem apple = new FoodItem("Apple", getCalendar(15), 5);
		FoodItem sugar = new FoodItem("Sugar", getCalendar(500), 1);
		FoodItem cookies = new FoodItem("Cookies", getCalendar(30), 24);
		FoodItem orangeJuice = new FoodItem("Orange Juice", getCalendar(21), 1);
		FoodItem cereal = new FoodItem("Honey Nut Cheerios", getCalendar(180), 1);
		
		foodItems.add(milk);
		foodItems.add(bread);
		foodItems.add(eggs);
		foodItems.add(apple);
		foodItems.add(sugar);
		foodItems.add(cookies);
		foodItems.add(orangeJuice);
		foodItems.add(cereal);

		populateListView(foodItems);

		findViewById(R.id.submitNewItemButton).setOnClickListener(addNewItemToListView);
		findViewById(R.id.daysGoodTextView).setOnClickListener(openCalendarDialogClick);
		findViewById(R.id.daysGoodTextView).setOnFocusChangeListener(openCalendarDialogFocus);
	}
	
	private Calendar getCalendar(int daysFromToday) {
	    Calendar c = GregorianCalendar.getInstance();
	    c.add(Calendar.DATE, daysFromToday);
	    
	    return c;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
	    
	    if (itemId == android.R.id.home) {
	        NavUtils.navigateUpFromSameTask(this);
            return true;
	    } else if (itemId == R.id.action_settings) {
	        return true;
	    } else if (itemId == R.id.action_camera) {
	        Intent intent = new Intent(this, TastiActivity.class);
	        startActivity(intent);
	        
	        return true;
	    } else {
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	// We should sort our list by ascending expiry date
	private void populateListView(ArrayList<FoodItem> list) {
        adapter = new MyFoodAdapter(this, R.layout.list_fooditems, list);

        ListView listView = (ListView) findViewById(R.id.foodItemListView);
        listView.setAdapter(adapter);
    }
	
	private OnClickListener addNewItemToListView = new OnClickListener() {

        @Override
        public void onClick(View v) {
            EditText newItemField = (EditText) findViewById(R.id.newItemEditText);
            TextView daysGoodField = (TextView) findViewById(R.id.daysGoodTextView);
            ListView foodList = (ListView) findViewById(R.id.foodItemListView);
            
            String foodName = newItemField.getText().toString();
            String daysGood = daysGoodField.getText().toString();
            
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            
            if (!foodName.equals("") && !daysGood.equals("")) { 
                int year = (Integer) daysGoodField.getTag(R.id.year_id);
                int month = (Integer) daysGoodField.getTag(R.id.month_id);
                int day = (Integer) daysGoodField.getTag(R.id.day_id);
                
                Calendar expiryDate = new GregorianCalendar(year, month, day);
                
                // Hide the keyboard if showing
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                
                FoodItem newFoodItem = new FoodItem(foodName, expiryDate, 0);
                foodItems.add(newFoodItem);
                adapter.notifyDataSetChanged();
                
                // UI clean up
                newItemField.setText("");
                daysGoodField.setText("");
                newItemField.clearFocus();
                daysGoodField.clearFocus();
                daysGoodField.setTag(R.id.year_id, 0);
                daysGoodField.setTag(R.id.month_id, 0);
                daysGoodField.setTag(R.id.day_id, 0);
                foodList.setSelection(adapter.getCount() - 1); // Should change if list is sorted
                
            } else if (foodName.equals("")) {
                // Set focus and show keyboard
                if (newItemField.requestFocus()) {
                    imm.showSoftInput(newItemField, InputMethodManager.SHOW_IMPLICIT);
                }
            } else if (daysGood.equals("")) {
                daysGoodField.requestFocus();
            }
        }
        
    };
    
    private OnClickListener openCalendarDialogClick = new OnClickListener() {

        @Override
        public void onClick(View v) {            
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getFragmentManager(), "datePicker");
        }
        
    };
    
    private OnFocusChangeListener openCalendarDialogFocus = new OnFocusChangeListener() {
        
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        }
    };
    
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.setTitle("Enter expiry date"); // (Only shows on tablets)
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() / 86400000L * 86400000L);
            // Probably should set a maximum date too
            // Calendar shown on tablets needs to be set to current date
            
            return datePickerDialog;
        }
        
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            TextView daysGood = (TextView) getActivity().findViewById(R.id.daysGoodTextView);
            
            // Display selected date in the TextView
            Calendar expiryDate = new GregorianCalendar(year, month, day);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            daysGood.setText(dateFormat.format(expiryDate.getTime()));
            
            // Set hidden data in view for calculations when Add button is pressed
            daysGood.setTag(R.id.year_id, year);
            daysGood.setTag(R.id.month_id, month);
            daysGood.setTag(R.id.day_id, day);
        }
        
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                Toast.makeText(getActivity(), "CANCELLED",Toast.LENGTH_LONG).show();
            }
        }
        
        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().findViewById(R.id.daysGoodTextView).clearFocus();
            
        }
    }
	
	private static class MyFoodAdapter extends ArrayAdapter<FoodItem> {
	      private Context context;
	      private int layout;
	      private List<FoodItem> foods;
	      
	      public MyFoodAdapter(Context context, int layout, List<FoodItem> foods) {
	          super(context, layout, foods);
	          this.context = context;
	          this.layout = layout;
	          this.foods = foods;
	      }

	      @Override
	      public View getView(int position, View convertView, ViewGroup parent) {
	          // Make sure we have a view to work with (may have been given null)
	          View itemView = convertView;
	          if (itemView == null) {
	              itemView = LayoutInflater.from(context).inflate(layout, parent, false);
	          }
	          
	          FoodItem currentFood = foods.get(position);
	          
	          TextView foodItemName = (TextView) itemView.findViewById(R.id.food_item_name);
	          foodItemName.setText(currentFood.getFoodItemName());
	          
	          TextView expirationNumber = (TextView) itemView.findViewById(R.id.expiration_number);
	          expirationNumber.setText(Integer.toString(currentFood.getExpirationNumber()));
	          
	          TextView expirationUnit = (TextView) itemView.findViewById(R.id.expiration_unit);
	          expirationUnit.setText(currentFood.getExpirationUnit());

	          return itemView;
	      }               
	  }

}
