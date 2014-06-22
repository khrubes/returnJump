package com.returnjump.spoilfoil;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Kelsey on 5/31/2014.
 */
public class EditNameFragment extends DialogFragment {
    protected OnEditNameButtonClickedListener editNameButtonClickedListener;
    protected EditText editText;
    protected ImageButton button;

    public EditNameFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_Holo_Light_Dialog);
        if(savedInstanceState==null) {
            savedInstanceState = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.edit_name_fragment, container, false);
        editText = (EditText) view.findViewById(R.id.edit_text_dialog);
        String fieldText = getArguments().getString("name");
        if(fieldText.equals("addingItemName")){
            editText.setHint("Item name");
        }else{
            editText.setText(fieldText);
        }
        button = (ImageButton) view.findViewById(R.id.submit_new_item_button_dialog);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!editText.getText().toString().equals("")) {
                    getArguments().putString("name", editText.getText().toString());
                    editNameButtonClickedListener.onEditNameButtonClicked();
                }else{
                    editText.requestFocus();
                }

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                                               @Override
                                               public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                                   if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                                      if(!editText.getText().toString().equals("")){
                                                          getArguments().putString("name", editText.getText().toString());
                                                          editNameButtonClickedListener.onEditNameButtonClicked();
                                                          return true;
                                                      }   else {
                                                          editText.requestFocus();
                                                          return false;
                                                      }
                                                   }
                                                   return false;
                                               }
                                           });

                editText.requestFocus();
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                return view;

            }

            @Override
            public void onAttach(Activity activity) {
                Log.d("$$$$$$$$ on attach called", " on attach");
                super.onAttach(activity);
                try {
                    editNameButtonClickedListener = (OnEditNameButtonClickedListener) getActivity();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }

            public interface OnEditNameButtonClickedListener {
                public void onEditNameButtonClicked();
            }
        }





