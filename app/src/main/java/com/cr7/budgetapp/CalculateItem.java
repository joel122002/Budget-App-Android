package com.cr7.budgetapp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.cr7.budgetapp.DateConverter.HOUR;
import static com.cr7.budgetapp.DateConverter.MINUTE;

public class CalculateItem extends AppCompatActivity implements View.OnClickListener {
    private final String[] columns = {"Date", "Item Name", "Price"};
    // Declaring some instance variables
    private int monthlyTotalCurrentUser, monthlyTotalAllUsers;
    private SimpleDateFormat dateSlashMonthSlashYear;
    private String dayString;
    private String monthString;
    private int monthInInt, yearInInt;
    private Button buttonNext;
    private Button buttonPrevious;
    private DateConverter dateConverter;
    private int displayNextButton;
    private TextView textViewMonth;
    private TextView textViewYear;
    private TextView textViewCurrentUserExpense;
    private TextView textViewAllUsersExpense;
    private Date from, to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_item);
        // Setting the tile of the page
        setTitle("Monthly Expense");
        // Linking the variables with their respective UI components
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        textViewMonth = findViewById(R.id.textViewMonth);
        textViewYear = findViewById(R.id.textViewYear);
        TextView textViewUsername = findViewById(R.id.textViewUsername);
        Button buttonGenerateExcelFile = findViewById(R.id.buttonGenerateExcelFile);
        textViewCurrentUserExpense = findViewById(R.id.textViewCurrentUserExpence);
        textViewAllUsersExpense = findViewById(R.id.textViewAllUsersExpence);
        // Defining SimpleDateFormat to get month, year and dd/MM/yyyy
        SimpleDateFormat month = new SimpleDateFormat("MM");
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        dateSlashMonthSlashYear = new SimpleDateFormat("dd/MM/yyyy");
        // Setting the text for the TextView which shows the current user's name
        textViewUsername.setText(ParseUser.getCurrentUser().getUsername() + ":");
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
        // Setting the dateConverter so that we can store data as an "Item" object so that it can 
        // be easily added to the excel sheet
        dateConverter = new DateConverter();
        // This function will calculate the monthly total of the current user and all users
        getdata();
        // Setting the onClickListener for the buttons
        buttonNext.setOnClickListener(this);
        buttonPrevious.setOnClickListener(this);
        buttonGenerateExcelFile.setOnClickListener(this);
    }

    // onClick method of the OnClickListener interface
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                textViewCurrentUserExpense.setText("");
                textViewAllUsersExpense.setText("");
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
                }
                // Else it will make the button visible and enable clicking on it
                else {
                    buttonNext.setVisibility(View.VISIBLE);
                    buttonNext.setEnabled(true);
                }
                // Setting the texts of the TextViews to empty strings so that the expense of the 
                // previous month(Month which the user previously was viewing) is not seen
                textViewCurrentUserExpense.setText("");
                textViewAllUsersExpense.setText("");
                // Increasing the month by one month
                monthInInt++;
                // Setting the TextView for year and month with the updated values
                textViewMonth.setText(checkMonth());
                textViewYear.setText(yearInInt + "");
                // This function will calculate the monthly total of the current user and all 
                // users for this newly updated month
                getdata();
                break;
            // This is the onClickListener for the generate Excel file which will generate an 
            // excel file in the Downloads folder
            case R.id.buttonGenerateExcelFile:
                if (isStoragePermissionGranted()) {
                    // Creating a ParseQuery that will get all the items of the previous month
                    ParseQuery<ParseObject> pparseQuery = new ParseQuery<ParseObject>("Items");
                    pparseQuery.setLimit(1000000);
                    pparseQuery.orderByAscending("Date");
                    pparseQuery.whereGreaterThanOrEqualTo("Date", from);
                    pparseQuery.whereLessThan("Date", to);
                    pparseQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            // Variables the hold the total spend of the current user and all users
                            monthlyTotalCurrentUser = 0;
                            monthlyTotalAllUsers = 0;
                            // Creating am Excel sheet for the current user and entering all values
                            Workbook workbookCurrentUser = new HSSFWorkbook();
                            Sheet sheetCurrentUser =
                                    workbookCurrentUser.createSheet(checkMonth().substring(0,
                                            checkMonth().length() - 1) + "CurrentUser");
                            Font headerFontCurrentUser = workbookCurrentUser.createFont();
                            headerFontCurrentUser.setBold(true);
                            headerFontCurrentUser.setFontHeightInPoints((short) 14);
                            headerFontCurrentUser.setColor(IndexedColors.BLACK.getIndex());
                            CellStyle headerCellStyleCurrentUser =
                                    workbookCurrentUser.createCellStyle();
                            headerCellStyleCurrentUser.setFont(headerFontCurrentUser);
                            Row headerRowCurrentUser = sheetCurrentUser.createRow(0);
                            // Variable which holds the row number which is empty and on which 
                            // data can be written on. Increments each time a value is written in
                            // a row so as to write the next entry on the next row  
                            int rowNumCurrentUser = 1;
                            // Creating am Excel sheet for the all users and entering all values
                            Workbook workbookAllUsers = new HSSFWorkbook();
                            Sheet sheetAllUsers =
                                    workbookAllUsers.createSheet(checkMonth().substring(0,
                                            checkMonth().length() - 1) + "AllUsers");
                            Font headerFontAllUsers = workbookAllUsers.createFont();
                            headerFontAllUsers.setBold(true);
                            headerFontAllUsers.setFontHeightInPoints((short) 14);
                            headerFontAllUsers.setColor(IndexedColors.BLACK.getIndex());
                            CellStyle headerCellStyleAllUsers = workbookAllUsers.createCellStyle();
                            headerCellStyleAllUsers.setFont(headerFontAllUsers);
                            Row headerRowAllUsers = sheetAllUsers.createRow(0);
                            // Variable which holds the row number which is empty and on which 
                            // data can be written on. Increments each time a value is written in
                            // a row so as to write the next entry on the next row
                            int rowNumAllUsers = 1;
                            // Sets's the column heading for the "Date", "Item" and "Price" column
                            for (int i = 0; i < columns.length; i++) {
                                Cell cell = headerRowCurrentUser.createCell(i);
                                cell.setCellValue(columns[i]);
                            }
                            // Sets's the column heading for the "Date", "Item" and "Price" column
                            for (int i = 0; i < columns.length; i++) {
                                Cell cell = headerRowAllUsers.createCell(i);
                                cell.setCellValue(columns[i]);
                            }
                            // String that hold the username of the current user
                            String user = ParseUser.getCurrentUser().getUsername();
                            if (objects.size() > 0 && e == null) {
                                for (ParseObject parseObject1 : objects) {
                                    // Checking if the parse object was created by this user; if 
                                    // yes adding these records to both the excel sheets; else 
                                    // adding it only to the all users excel sheet
                                    if (parseObject1.get("username").toString().matches(user)) {
                                        // Adding the object's price to the current user's total
                                        monthlyTotalCurrentUser =
                                                monthlyTotalCurrentUser + Integer.parseInt(parseObject1.get("Price").toString());
                                        // Adding that entry to the Excel sheet of the current user
                                        Item item =
                                                new Item(dateConverter.DateToDays((Date) parseObject1.get("Date")), parseObject1.get("ItemName").toString(), Integer.parseInt(parseObject1.get("Price").toString()));
                                        Row row = sheetAllUsers.createRow(++rowNumCurrentUser);
                                        row.createCell(0).setCellValue(dateSlashMonthSlashYear.format(dateConverter.DaysToDate(item.getDate())));
                                        row.createCell(1).setCellValue(item.getItem());
                                        row.createCell(2).setCellValue(item.getPrice());
                                    }
                                    // Adding the objects price to the all users total
                                    monthlyTotalAllUsers =
                                            monthlyTotalAllUsers + Integer.parseInt(parseObject1.get("Price").toString());
                                    // Adding that entry to the Excel sheet of the all the users
                                    Item item =
                                            new Item(dateConverter.DateToDays((Date) parseObject1.get("Date")), parseObject1.get("ItemName").toString(), Integer.parseInt(parseObject1.get("Price").toString()));
                                    Row row = sheetAllUsers.createRow(++rowNumAllUsers);
                                    row.createCell(0).setCellValue(dateSlashMonthSlashYear.format(dateConverter.DaysToDate(item.getDate())));
                                    row.createCell(1).setCellValue(item.getItem());
                                    row.createCell(2).setCellValue(item.getPrice());
                                }
                                // Adding the final row to the current user's excel sheet which 
                                // holds the total
                                Row rowCurrentUser =
                                        sheetCurrentUser.createRow(++rowNumCurrentUser);
                                rowCurrentUser.createCell(0).setCellValue("");
                                rowCurrentUser.createCell(1).setCellValue("Total");
                                rowCurrentUser.createCell(2).setCellValue(Integer.parseInt(textViewCurrentUserExpense.getText().toString()));
                                // Adding the final row to all users excel sheet which holds the 
                                // total
                                Row rowAllUsers = sheetAllUsers.createRow(++rowNumAllUsers);
                                rowAllUsers.createCell(0).setCellValue("");
                                rowAllUsers.createCell(1).setCellValue("Total");
                                rowAllUsers.createCell(2).setCellValue(Integer.parseInt(textViewAllUsersExpense.getText().toString()));
                                // Generating current user's excel sheet
                                File fileCurrentUser =
                                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), checkMonth().substring(0, checkMonth().length() - 1) + "CurrentUser.xls");
                                FileOutputStream fileOutputStreamCurrentUser;
                                try {
                                    fileOutputStreamCurrentUser =
                                            new FileOutputStream(fileCurrentUser);
                                    workbookCurrentUser.write(fileOutputStreamCurrentUser);
                                    fileOutputStreamCurrentUser.close();
                                    workbookCurrentUser.close();
                                    DownloadManager downloadManager =
                                            (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                    downloadManager.addCompletedDownload(fileCurrentUser.getName(), fileCurrentUser.getName(), true, "application/json", fileCurrentUser.getAbsolutePath(), fileCurrentUser.length(), true);
                                    FancyToast.makeText(CalculateItem.this, "Successfully created" +
                                            " Current Users file in downloads folder",
                                            FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                } catch (FileNotFoundException ef) {
                                    ef.printStackTrace();
                                } catch (IOException ef) {
                                    ef.printStackTrace();
                                }
                                // Generating all users excel sheet
                                File fileAllUsers =
                                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), checkMonth().substring(0, checkMonth().length() - 1) + "AllUsersUser.xls");
                                FileOutputStream fileOutputStreamAllUsers;
                                try {
                                    fileOutputStreamAllUsers = new FileOutputStream(fileAllUsers);
                                    workbookAllUsers.write(fileOutputStreamAllUsers);
                                    fileOutputStreamAllUsers.close();
                                    workbookAllUsers.close();
                                    FancyToast.makeText(CalculateItem.this, "Successfully created" +
                                            " All users file in downloads folder",
                                            FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                    DownloadManager downloadManager =
                                            (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                    downloadManager.addCompletedDownload(fileAllUsers.getName(),
                                            fileAllUsers.getName(), true, "application/json",
                                            fileAllUsers.getAbsolutePath(), fileAllUsers.length()
                                            , true);
                                } catch (FileNotFoundException ef) {
                                    ef.printStackTrace();
                                } catch (IOException ef) {
                                    ef.printStackTrace();
                                }
                            } else if (e != null) {
                                FancyToast.makeText(CalculateItem.this, e.getMessage(),
                                        FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                            }
                        }
                    });

                }

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

    // getdata method calculate the current user's an all users monthly budget
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
        to = new DateTime(from).plusMonths(1).toDate();
        // We create a parse query to get all records where date is greater than or equal to 
        // "from" and less than "to". This ensures we get the records of an entire month
        ParseQuery<ParseObject> pparseQuery = new ParseQuery<ParseObject>("Items");
        pparseQuery.setLimit(1000000);
        pparseQuery.orderByAscending("Date");
        pparseQuery.whereGreaterThanOrEqualTo("Date", from);
        pparseQuery.whereLessThan("Date", to);
        pparseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                // "monthlyTotalCurrentUser" and "monthlyTotalAllUsers" hold the total of the 
                // current user and all users respectively
                monthlyTotalCurrentUser = 0;
                monthlyTotalAllUsers = 0;
                // String that holds the username of the current user
                String user = ParseUser.getCurrentUser().getUsername();
                if (objects.size() > 0 && e == null) {
                    // Looping through all the objects acquired from the server.
                    for (ParseObject parseObject1 : objects) {
                        // Checking if the username of the person who entered the object is the 
                        // current user if yes add the item's price to the current user's total
                        if (parseObject1.get("username").toString().matches(user)) {
                            monthlyTotalCurrentUser =
                                    monthlyTotalCurrentUser + Integer.parseInt(parseObject1.get(
                                            "Price").toString());
                        }
                        // Adding each objects price irrespective of the user
                        monthlyTotalAllUsers =
                                monthlyTotalAllUsers + Integer.parseInt(parseObject1.get("Price").toString());
                    }
                } else if (e != null) {
                    FancyToast.makeText(CalculateItem.this, e.getMessage(),
                            FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
                // Setting all users and current user's expense to their respective TextViews
                textViewCurrentUserExpense.setText(String.valueOf(monthlyTotalCurrentUser));
                textViewAllUsersExpense.setText(String.valueOf(monthlyTotalAllUsers));
            }
        });
    }

    // Method for asking storage Permissions to write the Excel file
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("", "Permission is granted");
                return true;
            } else {

                Log.v("", "Permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { // permission is automatically granted on sdk<23 upon installation
            Log.v("", "Permission is granted");
            return true;
        }
    }

    // Method to confirm whether storage permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("", "Permission: " + permissions[0] + "was " + grantResults[0]);

        }
    }

}