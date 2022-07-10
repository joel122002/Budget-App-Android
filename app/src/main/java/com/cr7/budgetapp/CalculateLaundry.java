package com.cr7.budgetapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import static com.cr7.budgetapp.DateConverter.HOUR;
import static com.cr7.budgetapp.DateConverter.MINUTE;

public class CalculateLaundry extends AppCompatActivity implements View.OnClickListener,
        NumberPickerDialog.NumberPickerDialogListener {
    // Declaring some instance variables
    private int monthlyLaundryTotal;
    private SimpleDateFormat dateSlashMonthSlashYear;
    private String dayString;
    private String monthString;
    private int monthInInt, yearInInt, pricePerLaundry;
    private Button buttonNext;
    private Button buttonPrevious;
    private int displayNextButton;
    private TextView textViewMonth, textViewYear, textViewLaundryTotal;
    private Date from;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_laundry);
        // Setting the title of the page
        setTitle("Laundry Expense");
        // Linking the variables with their respective UI components
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        textViewMonth = findViewById(R.id.textViewMonth);
        textViewYear = findViewById(R.id.textViewYear);
        Button buttonChangePrice = findViewById(R.id.buttonChangePrice);
        textViewLaundryTotal = findViewById(R.id.textViewLaundryTotal);
        // Defining SimpleDateFormat to get date, month, year and dd/MM/yyyy
        SimpleDateFormat month = new SimpleDateFormat("MM");
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        dateSlashMonthSlashYear = new SimpleDateFormat("dd/MM/yyyy");
        // Date which holds today's date
        Date today = Calendar.getInstance().getTime();
        // Strings which hold the day month and year. Day is set to "01" as we are calculating of 
        // the month and month starts with date "01"
        dayString = "01";
        // Extracting the month as String from today's date
        monthString = month.format(today);
        // Extracting the year as String from today's date
        String yearString = year.format(today);
        // Parsing the above String to an Integer
        monthInInt = Integer.parseInt(monthString);
        yearInInt = Integer.parseInt(yearString);
        // Variable which decides the visibility of the Next Button. It's value will be 
        // decremented by 1 when the previous button is clicked and incremented by one when the 
        // next button is clicked. Also when it's value becomes zero the next button will be 
        // disabled and invisible.
        displayNextButton = 0;
        // These two TextViews will show the month in words and year in numerical
        textViewMonth.setText(checkMonth());
        textViewYear.setText(yearString);
        // Setting the visibility of the next button to invisible and disabling it
        buttonNext.setVisibility(View.INVISIBLE);
        buttonNext.setEnabled(false);
        // Method that will open the dialog so that the user can enter the price per laundry
        openDialog();
        // This function will calculate the monthly total of the current user and all users
        getdata();
        // Setting the onClickListener for the buttons
        buttonNext.setOnClickListener(this);
        buttonPrevious.setOnClickListener(this);
        buttonChangePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // onClickListener for the Previous button
            case R.id.buttonPrevious:
                // Decrementing the value of "displayNextButton" so as to make the next button 
                // visible if the month doesn't exceed the current mont
                displayNextButton--;
                // If displayNextButton >= 0 then it will disable the next button and make it 
                // invisible
                if (displayNextButton >= 0) {
                    buttonNext.setVisibility(View.INVISIBLE);
                    buttonNext.setEnabled(false);
                }
                // Else it will make the button visible and enable clicking on it
                else {
                    buttonNext.setVisibility(View.VISIBLE);
                    buttonNext.setEnabled(true);
                }
                // Setting the texts of the TextViews to empty strings so that the expense of the 
                // previous month(Month which the user previously was viewing) is not seen
                textViewLaundryTotal.setText("");
                // Reducing the month by one month
                monthInInt--;
                // Setting the TextView for year and month with the updated values
                textViewMonth.setText(checkMonth());
                textViewYear.setText(yearInInt + "");
                // This function will calculate the monthly total of the current user and all 
                // users for this newly updated month
                getdata();
                break;
            // onClickListener for the Next button
            case R.id.buttonNext:
                // Incrementing the value of "displayNextButton" and checks if it's value is more 
                // than or equal so that the user doesn't exceed the current month.
                displayNextButton++;
                // If displayNextButton >= 0 then it will disable the next button and make it 
                // invisible
                if (displayNextButton >= 0) {
                    buttonNext.setVisibility(View.INVISIBLE);
                    buttonNext.setEnabled(false);
                } else {
                    buttonNext.setVisibility(View.VISIBLE);
                    buttonNext.setEnabled(true);
                }
                // Setting the texts of the TextViews to empty strings so that the expense of the 
                // previous month(Month which the user previously was viewing) is not seen
                textViewLaundryTotal.setText("");
                // Increasing the month by one month
                monthInInt++;
                // Setting the TextView for year and month with the updated values
                textViewMonth.setText(checkMonth());
                textViewYear.setText(yearInInt + "");
                // This function will calculate the monthly total of the current user and all 
                // users for this newly updated month
                getdata();
                break;
            // This is the onClickListener for the Chang Price Button which will change the price 
            // per laundry
            case R.id.buttonChangePrice:
                openDialog();
                break;
        }
    }

    // Method which checks the month and returns value in words
    private String checkMonth() {
        if (yearInInt <= 1900) {
            monthString = "";
            textViewMonth.setText(monthString);
            buttonPrevious.setVisibility(View.GONE);
        } else if (monthInInt == 1) {
            monthString = "01";
            return "January ";
        } else if (monthInInt == 2) {
            monthString = "02";
            return "February ";
        } else if (monthInInt == 3) {
            monthString = "03";
            return "March ";
        } else if (monthInInt == 4) {
            monthString = "04";
            return "April ";
        } else if (monthInInt == 5) {
            monthString = "05";
            return "May ";
        } else if (monthInInt == 6) {
            monthString = "06";
            return "June ";
        } else if (monthInInt == 7) {
            monthString = "07";
            return "July ";
        } else if (monthInInt == 8) {
            monthString = "08";
            return "August ";
        } else if (monthInInt == 9) {
            monthString = "09";
            return "September ";
        } else if (monthInInt == 10) {
            monthString = "10";
            return "October ";
        } else if (monthInInt == 11) {
            monthString = "11";
            return "November ";
        } else if (monthInInt == 12) {
            monthString = "12";
            return "December ";
        } else if (monthInInt == 0) {
            monthString = "12";
            monthInInt = 12;
            yearInInt--;
            return "December ";
        } else if (monthInInt <= 13) {
            monthString = "01";
            monthInInt = 1;
            yearInInt++;
            return "January ";
        }
        return null;
    }

    // getdata method calculates the monthly laundry
    private void getdata() {
        // A String that holds the date in dd/MM/yyyy so that it can later be parsed and converted
        // to a date object
        String combinedDate = dayString + "/" + monthString + "/" + yearInInt;
        // "from" holds the first Day of the month chosen by user (by default the current month)
        try {
            from = dateSlashMonthSlashYear.parse(combinedDate);
            from = new Date(from.getTime() + 5 * HOUR + 30 * MINUTE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // "to" holds the first day of the next month
        Date to = new DateTime(from).plusMonths(1).toDate();
        // We create a parse query to get all records where date is greater than or equal to 
        // "from" and less than "to". This ensures we get the records of an entire month
        ParseQuery<ParseObject> pparseQuery = new ParseQuery<ParseObject>("Laundry");
        pparseQuery.setLimit(1000000);
        pparseQuery.orderByAscending("Date");
        pparseQuery.whereGreaterThanOrEqualTo("Date", from);
        pparseQuery.whereLessThan("Date", to);
        pparseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                // "monthlyLaundryTotal" holds the total of the laundry
                monthlyLaundryTotal = 0;
                if (objects.size() > 0 && e == null) {
                    for (ParseObject parseObject1 : objects) {
                        monthlyLaundryTotal =
                                monthlyLaundryTotal + Integer.parseInt(parseObject1.get("Laundry").toString());
                    }
                } else if (e != null) {
                    FancyToast.makeText(CalculateLaundry.this, e.getMessage(),
                            FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
                // Setting laundry expense as text of its TextView
                textViewLaundryTotal.setText(String.valueOf(monthlyLaundryTotal * pricePerLaundry));
            }
        });
    }

    // Method to open a NumberPickerDialog that will help the user choose the price per launsry
    private void openDialog() {
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog();
        // This will make sure that the user cannot dismiss the dialog
        numberPickerDialog.setCancelable(false);
        numberPickerDialog.show(getSupportFragmentManager(), "NumberPickerDialog");
    }

    // Method that will set the price per laundry and calculate the monthly total based on the 
    // number chosen by the user
    @Override
    public void sendPrice(int price) {
        pricePerLaundry = price;
        getdata();
    }
}
