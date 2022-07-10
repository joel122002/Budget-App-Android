package com.cr7.budgetapp;


import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.shashank.sony.fancytoastlib.FancyToast;
import org.joda.time.DateTime;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ViewUserData extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, DatePickerDialog.OnDateSetListener {
    //Declaring all variables
    private LinearLayout linearLayoutScrollable;
    private Date from,dateOfToday;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private SimpleDateFormat day, month, year, daymonthyear;
    private Spinner spinner;
    private String skipType;
    private Button buttonPreviousDate, buttonNextDate;
    private ImageView imageViewDate;
    private TextView textViewDate;
    private String recievedUsername;
    //OnCreate method
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_data);
        //Getting the username on which the tapped in OtherUsers fragment
        final Intent recievedobj = getIntent();
        recievedUsername = recievedobj.getStringExtra("username");
        //Setting the title as User's Expenses
        setTitle(recievedUsername + "'s Expenses");
        //Defining SimpleDateFormat to get date, month, year and dd/MM/yyyy
        day = new SimpleDateFormat("dd");
        month = new SimpleDateFormat("MM");
        year = new SimpleDateFormat("yyyy");
        daymonthyear = new SimpleDateFormat("dd/MM/yyyy");
        //Setting the default skip to day as the spinners default value is Day which can later be changed by the User
        skipType = "Day";
        //Defing the variables which were previously declared as instance variables
        buttonPreviousDate = findViewById(R.id.buttonPreviousDate);
        buttonNextDate = findViewById(R.id.buttonNextDate);
        textViewDate = findViewById(R.id.textViewDate);
        imageViewDate = findViewById(R.id.imageViewDate);
        //dateOfToday holds the value of today's date
        dateOfToday = Calendar.getInstance().getTime();
        linearLayoutScrollable = findViewById(R.id.linearLayoutScrollable);
        //This is array adapter which holds the values of the spinner
        arrayAdapter = ArrayAdapter.createFromResource(ViewUserData.this, R.array.skip, R.layout.custom_spinner);
        //Setting the dropdown for the spinner
        arrayAdapter.setDropDownViewResource(R.layout.custim_dropdown);
        spinner = findViewById(R.id.spinner);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        spinner.setAdapter(arrayAdapter);
        //Setting an OnItemSelectedListener
        spinner.setOnItemSelectedListener(ViewUserData.this);
        //from was not directly assigned the value dateOfToday because dateOfToday holds the time as well. By this way the time is reset to 00:00:00
        try {
            from = daymonthyear.parse(daymonthyear.format(dateOfToday));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        //Setting the next button as invisible and disabling it so that the user doesn't go ahead of today
        buttonNextDate.setVisibility(INVISIBLE);
        buttonNextDate.setEnabled(false);
        //Calling the getData method which will get all data from the server and display it on the screen of the user's device
        getData();
        //Setting the OnClickListener on the Next and Previous buttons
        buttonPreviousDate.setOnClickListener(ViewUserData.this);
        buttonNextDate.setOnClickListener(ViewUserData.this);
        imageViewDate.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonPreviousDate)
        {
            //As soon a the previous button is clicked the next button becomes visible and clickable
            buttonNextDate.setVisibility(VISIBLE);
            buttonNextDate.setEnabled(true);
            //Checking the skip type so that it goes behind by one day/week/month/year
            if (skipType.matches("Day"))
            {
                from = new DateTime(from).minusDays(1).toDate();
            }
            else if (skipType.matches("Week"))
            {
                from = new DateTime(from).minusWeeks(1).toDate();
            }
            else if (skipType.matches("Month"))
            {
                from = new DateTime(from).minusMonths(1).toDate();
            }
            else
            {
                from = new DateTime(from).minusYears(1).toDate();
            }
            //Removing all the previous views so as to clean linearLayoutScrollable and the entries of the new date
            linearLayoutScrollable.removeAllViews();
            //Calling the getData method which will get all data from the server and display it on the screen of the user's device
            getData();
        }
        else if (v.getId() == R.id.buttonNextDate)
        {
            //Checking the skip type so that it goes ahead by one day/week/month/year
            if (skipType.matches("Day"))
            {
                from = new DateTime(from).plusDays(1).toDate();
            }
            else if (skipType.matches("Week"))
            {
                from = new DateTime(from).plusWeeks(1).toDate();
            }
            else if (skipType.matches("Month"))
            {
                from = new DateTime(from).plusMonths(1).toDate();
            }
            else
            {
                from = new DateTime(from).plusYears(1).toDate();
            }
            //Checking if the given date is greater than today's date so that the date doesn't exceed today's date
            if (from.after(dateOfToday) || from.equals(dateOfToday))
            {
                //Assigning the from date to today's date so that it will show today's entries
                from = dateOfToday;
                //Setting button next as invisible and disabling it
                buttonNextDate.setVisibility(INVISIBLE);
                buttonNextDate.setEnabled(false);
            }
            //Removing all the previous views so as to clean linearLayoutScrollable and the entries of the new date
            linearLayoutScrollable.removeAllViews();
            //Calling the getData method which will get all data from the server and display it on the screen of the user's device
            getData();
        }
        else if (v.getId() == R.id.imageViewDate)
        {
            showDatePickerDialog();
        }
    }
    //Function to convert dp to pixels
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    //Function to get all data from the server and display it on the screen of the user's device
    private void getData()
    {
        //Creating a ParseQuery which will get data of type parse Object
        final ParseQuery<ParseObject> parseQuery1 = new ParseQuery<ParseObject>("Items");
        //Assigning the parameter sp as to get objects from date:from to date:from+1 and order it in the descending order of the date
        parseQuery1.whereEqualTo("username", recievedUsername);
        parseQuery1.orderByDescending("Date");
        parseQuery1.whereGreaterThanOrEqualTo("Date",from);
        //TextView date shows the date in the form of words e.g. 23 January 2020 
        textViewDate.setText(day.format(from) + " " + checkMonth(month.format(from)) + year.format(from) + " (" + day(from.getDay()) + ")");
        //Dateplus is a variable of type date  which holds the value of from + 1
        Date dateplus1 = new DateTime(from).plusDays(1).toDate();
        parseQuery1.whereLessThan("Date", dateplus1);
        //Retrieving all data with the specific parameters
        parseQuery1.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0 && e == null) {
                    for (ParseObject item : objects) {
                        //Declaring and defining variables to be assigned to EditText
                        String ItemName = item.get("ItemName").toString();
                        int Price = Integer.parseInt(item.get("Price").toString());
                        String objectId = item.getObjectId();
                        View v = LayoutInflater.from(ViewUserData.this).inflate(R.layout.temp_layout, null);
                        //Creating TextInputEditTexts and linking them to the TextInputEditTexts of temp_layout
                        TextInputEditText TextInputEditTextNewItemName = v.findViewById(R.id.TextInputEditTextNewItemName);
                        TextInputEditText TextInputEditTextNewPrice = v.findViewById(R.id.TextInputEditTextNewPrice);
                        //Setting a text for the TextInputEditTexts
                        TextInputEditTextNewItemName.setText(ItemName);
                        TextInputEditTextNewPrice.setText(Price + "");
                        //Setting objectId as tags of the TextInputEditTexts
                        TextInputEditTextNewItemName.setTag(objectId);
                        TextInputEditTextNewItemName.setTag(objectId);
                        //Creating a LinearLayout and linking it to the LinearLayout of temp_layout
                        LinearLayout linearLayout = v.findViewById(R.id.linearLayout);
                        //Creating parameters for the previously created LinearLayout
                        LinearLayout.LayoutParams paramslinearLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        //Setting margins for the LinearLayout
                        paramslinearLayout.setMargins(dpToPx(10),0,dpToPx(10),0);
                        //Applying the parameters to the LinearLayout
                        linearLayout.setLayoutParams(paramslinearLayout);
                        //removing the already existing imgcheck view
                        linearLayout.removeView(v.findViewById(R.id.imgcheck));
                        TextInputEditTextNewItemName.setFocusable(false);
                        //adding linearlayout to parent i.e. linearLayoutScrollable
                        linearLayoutScrollable.addView(linearLayout);
                    }
                }
                else if (e != null)
                {
                    FancyToast.makeText(ViewUserData.this,e.getMessage(), FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                }
            }
        });
    }
    //The onItemSelected for the Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        skipType = parent.getItemAtPosition(position).toString();
    }
    //Setting the default value of the spinner to be day
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        skipType = "Day";
    }
    //Function to check the month
    private String checkMonth(String monthString)
    {
        if (monthString.matches("01"))
        {
            return "January ";
        }
        else if (monthString.matches("02"))
        {
            return "February ";
        }
        else if (monthString.matches("03"))
        {
            return "March ";
        }
        else if (monthString.matches("04"))
        {
            return "April ";
        }
        else if (monthString.matches("05"))
        {
            return "May ";
        }
        else if (monthString.matches("06"))
        {
            return "June ";
        }
        else if (monthString.matches("07"))
        {
            return "July ";
        }
        else if (monthString.matches("08"))
        {
            return "August ";
        }
        else if (monthString.matches("09"))
        {
            return "September ";
        }
        else if (monthString.matches("10"))
        {
            return "October ";
        }
        else if (monthString.matches("11"))
        {
            return "November ";
        }
        else if (monthString.matches("12"))
        {
            return "December ";
        }
        return null;
    }
    private String day(int day)
    {
        if (day == 0)
        {
            return "Sunday";
        }
        else if (day == 1)
        {
            return "Monday";
        }
        else if (day == 2)
        {
            return "Tuesday";
        }
        else if (day == 3)
        {
            return "Wednesday";
        }
        else if (day == 4)
        {
            return "Thursday";
        }
        else if (day == 5)
        {
            return "Friday";
        }
        else if (day == 6)
        {
            return "Saturday";
        }
        //Default value(which will never be reached)
        return null;
    }
    private void showDatePickerDialog()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DialogTheme,this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateChosenByUser = dayOfMonth + "/" + (month+1) + "/" + year;
        Log.i("Day", dateChosenByUser);
        try {
            from = daymonthyear.parse(dateChosenByUser);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        //Removing all the previous views so as to clean linearLayoutScrollable and the entries of the new date
        linearLayoutScrollable.removeAllViews();
        //Calling the getData method which will get all data from the server and display it on the screen of the user's device
        getData();
    }
}
