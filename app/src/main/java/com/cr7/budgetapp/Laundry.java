package com.cr7.budgetapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.cr7.budgetapp.DateConverter.HOUR;
import static com.cr7.budgetapp.DateConverter.MINUTE;

public class Laundry extends Fragment implements AdapterView.OnItemSelectedListener,
        View.OnClickListener, DatePickerDialog.OnDateSetListener, SwipeRefreshLayout.OnRefreshListener {
    // "date" is a variable and can be changed using the DatePicker. It indicated the date of any
    // new laundry item. Hence by default it has today's date
    private Date date;
    private LaundryRecyclerViewAdapter laundryRecyclerViewAdapter;
    private TextInputEditText editTextNewLaundryCount;
    private DateConverter dateConverter;
    private SQLiteDatabase database;
    private int month, year;
    private String skipType;
    private Button buttonNextDate;
    private TextView textViewDate, textViewMonthAndYear, textViewTotal;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_laundry, container, false);
        // Initializing the SQLiteDatabase object so as to interact with the database
        database = new SQLiteDatabase(getContext());
        // Enabling options menu (the menu on the action bar). It has to be specifically done as
        // this is a fragment. This need not be done if it is an activity
        this.setHasOptionsMenu(true);
        // Linking the instance variables to their respective UI components
        editTextNewLaundryCount = view.findViewById(R.id.editTextNewLaundryCount);
        Button buttonPreviousDate = view.findViewById(R.id.buttonPreviousDate);
        buttonNextDate = view.findViewById(R.id.buttonNextDate);
        textViewDate = view.findViewById(R.id.textViewDate);
        textViewMonthAndYear= view.findViewById(R.id.textViewMonthAndYear);
        textViewTotal = view.findViewById(R.id.textViewTotal);
        ImageView imageViewDate = view.findViewById(R.id.imageViewDate);
        ImageView imgcheck = view.findViewById(R.id.imgcheck);
        imgcheck.setOnClickListener(Laundry.this);
        final Calendar c = Calendar.getInstance();
        // Getting Month and year of today
        month = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        // Setting the default skip to day as the spinners default value is Day which can later be
        // changed by the User
        skipType = "Month";
        // This is array adapter which holds the values of the spinner
        assert getContext() != null;
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.laundrySkip, R.layout.custom_spinner);
        // Setting the dropdown for the spinner
        arrayAdapter.setDropDownViewResource(R.layout.custim_dropdown);
        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent),
                PorterDuff.Mode.SRC_ATOP);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(Laundry.this);
        // Recycler View that will hold the already added items
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        // Creating a layout manager for our recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // Setting the orientation of the LinearLayoutManager to Vertical
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        // Setting the above created LinearLayoutManager as the LayoutManager of the RecyclerView
        recyclerView.setLayoutManager(linearLayoutManager);
        // Creating an object of the RecyclerViewAdapter class and setting the RecyclerViewAdapter
        // class' object as the adapter
        laundryRecyclerViewAdapter = new LaundryRecyclerViewAdapter();
        recyclerView.setAdapter(laundryRecyclerViewAdapter);
        // Initializing the SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(Laundry.this);
        // Setting up the OnLongClickListener so that we can delete an item on long click
        laundryRecyclerViewAdapter.setOnItemLongClickListener(new LaundryRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(LaundryItem laundryItem) {
                Laundry.this.onItemLongClick(laundryItem);
            }
        });
        // Setting the next button as invisible and disabling it so that the user doesn't go ahead
        // of today
        buttonNextDate.setVisibility(View.INVISIBLE);
        buttonNextDate.setEnabled(false);
        // Object of SQLiteDatabase. It is through this object that we will interact with the
        // database
        database = new SQLiteDatabase(getContext());
        // Object of type DateConverter that will convert the date to an integer as SQLite does
        // not have a date data-type
        dateConverter = new DateConverter();
        date = getLastDayOfMonth(month, year);
        textViewDate.setText(dateAsStringWithDay(date));
        textViewMonthAndYear.setText(getMonthAndYearAsString());
        // Doing all the work to get the items and making sure the local database is synced with
        // the server
        doAllDatabaseTasks();
        // Setting OnClickListeners for the UI components
        buttonPreviousDate.setOnClickListener(Laundry.this);
        buttonNextDate.setOnClickListener(Laundry.this);
        imageViewDate.setOnClickListener(this);
        return view;
    }

    // Changes the "skipType" based on the the item chosen by the user in the spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        skipType = parent.getItemAtPosition(position).toString();
    }

    // If nothing is selected sets the "skipType" to "Day"
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        skipType = "Day";
    }

    // OnClickListeners for the different UI components
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonPreviousDate) {
            // As soon a the previous button is clicked the next button becomes visible and
            // clickable
            buttonNextDate.setVisibility(View.VISIBLE);
            buttonNextDate.setEnabled(true);
            // Checking the skip type so that it goes behind by one day/week/month/year
            if (skipType.matches("Month")) {
                decrementMonth();
            } else {
                decrementYear();
            }
            textViewMonthAndYear.setText(getMonthAndYearAsString());
            // Calling the doAllDatabaseTasks method which will sync the local database and server
            // and also display the day's record (remember a day can only have one record) on the
            // screen
            doAllDatabaseTasks();
        } else if (v.getId() == R.id.buttonNextDate) {
            boolean cannotIncrement;
            // Checking the skip type so that it goes ahead by one day/week/month/year
            if (skipType.matches("Month")) {
                cannotIncrement = incrementMonth();
            } else {
                cannotIncrement = incrementYear();
            }
            textViewMonthAndYear.setText(getMonthAndYearAsString());
            // Checking if the given date is greater than today's date so that the date doesn't
            // exceed today's date
            if (cannotIncrement) {
                // Setting button next as invisible and disabling it
                buttonNextDate.setVisibility(View.INVISIBLE);
                buttonNextDate.setEnabled(false);
            }
            // Calling the doAllDatabaseTasks method which will sync the local database and server
            // and also display the day's record (remember a day can only have one record) on the
            // screen
            doAllDatabaseTasks();
        } else if (v.getId() == R.id.imageViewDate) {
            // Showing the DatePickerDialog for the user to pick a date
            showDatePickerDialog();
        } else if (v.getId() == R.id.imgcheck) {
            // Checking if itemForDate exist as if it doesn't exist we'll have to create one and
            // add; if it does exist, depending on the value of the NumberPicker it will update
            // or delete the entry

            LaundryItem item = new LaundryItem(dateConverter.DateToDays(date), null,
                    Integer.parseInt(editTextNewLaundryCount.getText().toString()));
            database.addLaundry(item);
            editTextNewLaundryCount.setText("");
            editTextNewLaundryCount.clearFocus();
            // Calling the "uploadUnfinishedLaundryData" method which will upload the current
            // item as well as other items that previously could not be uploaded (maybe if
            // there was a network issue) to the server
            doAllDatabaseTasks();

        }
//        else if (v.getId() == R.id.imageViewRefresh) {
//            // Code for rotating the image view to make it look like we are refreshing the data
//            rotate = new RotateAnimation(
//                    360, 0,
//                    Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f
//            );
//            rotate.setDuration(1000);
//            rotate.setRepeatCount(Animation.INFINITE);
//            imageViewRefresh.startAnimation(rotate);
//            // Doing all the work to get the items and making sure the local database is synced
//            // with the server
//            doAllDatabaseTasks();
//        }
    }

    private String getMonthAndYearAsString() {
        return new DateFormatSymbols().getMonths()[month] + " " + year;
    }

    // Method to show the DatePickerDialog
    private void showDatePickerDialog() {
        assert getContext() != null;
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                R.style.DialogTheme, this, Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        date = new Date(calendar.getTime().getTime() + 5 * HOUR + 30 * MINUTE);
        textViewDate.setText(dateAsStringWithDay(date));
        Log.i("Day", dateAsStringWithDay(date));
    }

    // Returns true if year or month cannot be further incremented
    private boolean incrementMonth() {

        if (month + 1 >= Calendar.getInstance().get(Calendar.MONTH) && year >= Calendar.getInstance().get(Calendar.YEAR)) {
            month = Calendar.getInstance().get(Calendar.MONTH);
            year = Calendar.getInstance().get(Calendar.YEAR);
            return true;
        }
        if (month == 11) {
            month = 0;
            year += 1;
            return false;
        }
        month++;
        return false;
    }

    // Returns true if year or month cannot be further incremented
    private boolean incrementYear() {
        if (month >= Calendar.getInstance().get(Calendar.MONTH) && year + 1 >= Calendar.getInstance().get(Calendar.YEAR)) {
            month = Calendar.getInstance().get(Calendar.MONTH);
            year = Calendar.getInstance().get(Calendar.YEAR);
            return true;
        }
        year++;
        return false;
    }

    private void decrementMonth() {
        if (month == 0) {
            month = 11;
            year -= 1;
        } else
            month -= 1;
    }

    private void decrementYear() {
        year -= 1;
    }

    private String dateAsStringWithDay(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd MMMM yyyy (EEEE)");
        return formatter.format(date);
    }

    // On long clicking an Item it will ask the user if he or she wants to delete the Item
    private void onItemLongClick(final LaundryItem laundryItem) {
        assert getContext() != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setMessage("Are you sure you want to delete this entry?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.deleteLaundry(laundryItem);
                        doAllDatabaseTasks();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Does the task of getting all item and also syncs the local database to the server
    public void doAllDatabaseTasks() {
        getData();
        fetchItemsFromServer();
        fetchLaundryFromServer();
        uploadUnfinishedItemsData();
        uploadUnfinishedLaundryData();
        deleteItemsToBeDeletedFromServer();
        deleteLaundryToBeDeletedFromServer();
    }

    // Function to get all data from the server and display it on the screen of the user's device
    private void getData() {
        final SQLiteDatabase database = new SQLiteDatabase(getContext());
        Date startDate = getFirstDayOfMonth(month, year);
        int startDateInt = dateConverter.DateToDays(startDate);
        // Setting the ending date to the last day of the month
        Date endDate = getLastDayOfMonth(month, year);
        int endDateInt = dateConverter.DateToDays(endDate);
        List<LaundryItem> laundryItems = database.getLaundry(startDateInt, endDateInt);
        int total = 0;
        for (LaundryItem laundryItem : laundryItems) {
            total += laundryItem.getLaundry();
        }
        textViewTotal.setText(String.valueOf(total));
        Log.i("Laundry", String.valueOf(laundryItems.size()));
        laundryRecyclerViewAdapter.setLaundryItems(laundryItems);
    }

    private Date getFirstDayOfMonth(int month, int year) {
        // Setting the starting date to the 1st day of the month
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, month, 1, 0, 0, 0);
        return new Date(startDate.getTime().getTime() + 5 * HOUR + 30 * MINUTE);
    }

    private Date getLastDayOfMonth(int month, int year) {
        // Setting the starting date to the 1st day of the month
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, month, 1, 0, 0, 0);
        Calendar endDate = startDate;
        // Gets the last day of the month
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        // If endDate > today's date then set today's date as end date
        if (endDate.getTime().after(new Date())) {
            Calendar c = Calendar.getInstance();
            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            return new Date(c.getTime().getTime() + 5 * HOUR + 30 * MINUTE);
        }

        return new Date(endDate.getTime().getTime() + 5 * HOUR + 30 * MINUTE);
    }

    // Method to upload the newly created or updated data that has only been updated/created in
    // the local database and not in the server
    private void uploadUnfinishedLaundryData() {
        // Get's the list of all LaundryItems that have to be uploaded
        List<LaundryItem> items = database.getToUploadLaundry();
        Log.i("HelloGiza", "Size of items ot be uploaded is : " + items.size());
        for (final LaundryItem item : items) {
            final ParseObject parseObject = new ParseObject("Laundry");
            parseObject.put("Date", dateConverter.DaysToDate(item.getDate()));
            parseObject.put("Laundry", item.getLaundry());
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    item.setObjectId(parseObject.getObjectId());
                    database.moveLaundryToMain(item);
                }
            });
        }
    }

    // Getting data from server and making sure that the local database in sin sync with the server
    private void fetchLaundryFromServer() {
        List<Integer> intDatesFromDatabase =
                database.getDatesOfToUploadTableOrDeleteLaundryTable();
        List<Date> datesFromDatabase = new ArrayList<>();
        for (int i : intDatesFromDatabase) {
            datesFromDatabase.add(dateConverter.DaysToDate(i));
        }
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Laundry");
        parseQuery.whereNotContainedIn("Date", datesFromDatabase);
        // Setting the start and end dates of the laundry that will be retrieved from the server.
        // All laundry between "startDate" and "endDate" (Both inclusive) will be retrieved from the
        // server
        final Date startDate = getFirstDayOfMonth(month, year);
        Date endDate = getLastDayOfMonth(month, year);
        final int startDateInt = dateConverter.DateToDays(startDate);
        final int endDateInt = dateConverter.DateToDays(endDate);
        parseQuery.whereLessThanOrEqualTo("Date", endDate);
        parseQuery.whereGreaterThanOrEqualTo("Date", startDate);
        parseQuery.orderByAscending("Date");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && e == null) {
                    // Creating a List of the objectIds that we'll be getting from the server
                    List<String> objectIds = new ArrayList<>();
                    // Creating a list of LaundryItems which will be populated by the data from
                    // the server and then each LaundryItem will be added to the local database
                    List<LaundryItem> laundryItems = new ArrayList<>();
                    // Looping through objects from the server and populating the objectIds
                    // ArrayList as well as the laundryItems ArrayList
                    for (ParseObject item : objects) {
                        objectIds.add(item.getObjectId());
                        LaundryItem item1 =
                                new LaundryItem(dateConverter.DateToDays((Date) item.get("Date"))
                                        , item.getObjectId(),
                                        Integer.parseInt(item.get("Laundry").toString()));
                        laundryItems.add(item1);
                    }
                    // Deleting all items between these 20 days and adding the newly acquired
                    // data. This ensures that if any entry is deleted on another device it deletes
                    // the entry on this device also
                    database.deleteAllFromMainLaundryTable(startDateInt, endDateInt);
                    database.addToMainLaundryTable(laundryItems);
                    // Getting all the objectIds from the delete and upload tables
                    List<String> objectIdsFromUploadTable =
                            database.getAllObjectIdsFromLaundryUploadTable();
                    List<String> objectIdsFromDeleteTable =
                            database.getAllObjectIdsFromLaundryDeleteTable();
                    // These two will hold the common objectIds between the server and the local
                    // database
                    List<String> commonObjectIdsBetweenServerAndUploadTable =
                            new ArrayList<>(objectIdsFromUploadTable);
                    List<String> commonObjectIdsBetweenServerAndDeleteTable =
                            new ArrayList<>(objectIdsFromDeleteTable);
                    commonObjectIdsBetweenServerAndUploadTable.retainAll(objectIds);
                    commonObjectIdsBetweenServerAndDeleteTable.retainAll(objectIds);
                    // These two will hold objects that exist in the local database but not in the
                    // server
                    List<String> uncommonElementsBetweenServerAndUploadTable =
                            objectIdsFromUploadTable;
                    List<String> uncommonElementsBetweenServerAndDeleteTable =
                            objectIdsFromDeleteTable;
                    uncommonElementsBetweenServerAndUploadTable.removeAll(commonObjectIdsBetweenServerAndUploadTable);
                    uncommonElementsBetweenServerAndDeleteTable.removeAll(commonObjectIdsBetweenServerAndDeleteTable);
                    // Now we finally remove the objectIds of these items(whose objectId exist in
                    // the local database but not in the server)
                    database.deleteItemFromLaundryToDeleteTableByObjectId(uncommonElementsBetweenServerAndDeleteTable);
                    database.deleteObjectIdFromLaundryInUploadTable(uncommonElementsBetweenServerAndUploadTable);
                    // Cancelling the rotation of the SwipeRefreshLayout if it is rotating
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                } else if (e != null) {
                    // Displaying the error message
                    FancyToast.makeText(getContext(), e.getMessage(), FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR, false);
                    // Cancelling the rotation of the SwipeRefreshLayout if it is rotating
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                } else {
                    // Cancelling the rotation of the SwipeRefreshLayout if it is rotating
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    // Deleting items from the server that have been deleted from the local database
    private void deleteLaundryToBeDeletedFromServer() {
        List<LaundryItem> items = database.getLaundryToDelete();
        for (final LaundryItem item : items) {
            if (item.getObjectId() != null) {
                ParseQuery<ParseObject> parseQueryDelete = new ParseQuery<ParseObject>("Laundry");
                parseQueryDelete.whereEqualTo("objectId", item.getObjectId());
                parseQueryDelete.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects != null && objects.size() > 0 && e == null) {
                            objects.get(0).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        database.deleteLaundryFromToDeleteTable(item);
                                    } else {
                                        FancyToast.makeText(getContext(), e.getMessage(),
                                                FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                                    }
                                }
                            });
                        } else if (e != null) {
                            FancyToast.makeText(getContext(), e.getMessage(),
                                    FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        }
                    }
                });
            } else {
                database.deleteLaundryFromToDeleteTable(item);
            }
        }
    }

    // Method to upload the newly created or updated data that has only been updated/created in
    // the local database and not in the server
    private void uploadUnfinishedItemsData() {
        List<Item> items = database.getToUploadItems();
        for (final Item item : items) {
            if (item.getObjectId() == null) {
                final ParseObject parseObject = new ParseObject("Items");
                parseObject.put("ItemName", item.getItem());
                parseObject.put("Price", item.getPrice());
                parseObject.put("Date", dateConverter.DaysToDate(item.getDate()));
                parseObject.put("username", ParseUser.getCurrentUser().getUsername());
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        item.setObjectId(parseObject.getObjectId());
                        database.moveToMain(item);
                    }
                });
            } else {
                final ParseQuery<ParseObject> query = ParseQuery.getQuery("Items");
                query.getInBackground(item.getObjectId(), new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            object.put("ItemName", item.getItem());
                            object.put("Price", item.getPrice());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        database.moveToMain(item);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    // Getting data from server and making sure that the local database is in sync with the server
    private void fetchItemsFromServer() {
        List<String> objectIds = database.getObjectIdFromToUploadTableOrDeleteTable();
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Items");
        // Assigning the parameter so as to get objects from "threeDaysBeforeFrom" to "from" and
        // order it in the descending order of "createdAt"
        parseQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        parseQuery.whereNotContainedIn("objectId", objectIds);
        Date threeDaysBeforeFrom = new Date(date.getTime() - 72 * HOUR);
        parseQuery.whereGreaterThanOrEqualTo("Date", threeDaysBeforeFrom);
        parseQuery.whereLessThanOrEqualTo("Date", date);
        parseQuery.orderByDescending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0 && e == null) {
                    List<Integer> dates = new ArrayList<>();
                    List<String> objectIds = new ArrayList<>();
                    int dateInInt = dateConverter.DateToDays(date);
                    for (int i = 0; i <= 3; i++) {
                        dates.add(dateInInt - i);
                    }
                    List<Item> items = new ArrayList<>();
                    for (ParseObject item : objects) {
                        objectIds.add(item.getObjectId());
                        Item item1 = new Item(dateConverter.DateToDays((Date) item.get("Date")),
                                item.getObjectId(), item.get("ItemName").toString(),
                                Integer.parseInt(item.get("Price").toString()));
                        items.add(item1);
                    }
                    database.deleteAllFromMainTable(dates);
                    database.addToMainTable(items);
                    // Getting all the objectIds from the delete and upload tables
                    List<String> objectIdsFromUploadTable =
                            database.getAllObjectIdsFromUploadTable();
                    List<String> objectIdsFromDeleteTable =
                            database.getAllObjectIdsFromDeleteTable();
                    // These two will hold the common objectIds between the server and the local
                    // database
                    List<String> commonObjectIdsBetweenServerAndUploadTable =
                            new ArrayList<>(objectIdsFromUploadTable);
                    List<String> commonObjectIdsBetweenServerAndDeleteTable =
                            new ArrayList<>(objectIdsFromDeleteTable);
                    commonObjectIdsBetweenServerAndUploadTable.retainAll(objectIds);
                    commonObjectIdsBetweenServerAndDeleteTable.retainAll(objectIds);
                    // These two will hold objects that exist in the local database but not in the
                    // server
                    List<String> uncommonElementsBetweenServerAndUploadTable =
                            objectIdsFromUploadTable;
                    List<String> uncommonElementsBetweenServerAndDeleteTable =
                            objectIdsFromDeleteTable;
                    uncommonElementsBetweenServerAndUploadTable.removeAll(commonObjectIdsBetweenServerAndUploadTable);
                    uncommonElementsBetweenServerAndDeleteTable.removeAll(commonObjectIdsBetweenServerAndDeleteTable);
                    // Now we finally remove the objectIds of these items(whose objectId exist in
                    // the local database but not in the server)
                    database.deleteItemFromToDeleteTableByObjectId(uncommonElementsBetweenServerAndDeleteTable);
                    database.deleteObjectIdFromItemInUploadTable(uncommonElementsBetweenServerAndUploadTable);
                }
            }
        });
    }

    // Deleting items from the server that have been deleted from the local database
    private void deleteItemsToBeDeletedFromServer() {
        List<Item> items = database.getItemsToDelete();
        for (final Item item : items) {
            if (item.getObjectId() != null) {
                ParseQuery<ParseObject> parseQueryDelete = new ParseQuery<ParseObject>("Items");
                parseQueryDelete.whereEqualTo("objectId", item.getObjectId());
                parseQueryDelete.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            objects.get(0).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        database.deleteItemFromToDeleteTable(item);
                                    } else {
                                        FancyToast.makeText(getContext(), e.getMessage(),
                                                FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                                    }
                                }
                            });
                        } else {
                            FancyToast.makeText(getContext(), e.getMessage(),
                                    FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        }
                    }
                });
            } else {
                database.deleteItemFromToDeleteTable(item);
            }
        }
    }

    // Method that will determine the month based on the value of the parameter
    private String checkMonth(int month) {
        if (month == Calendar.JANUARY) {
            return "Jan ";
        } else if (month == Calendar.FEBRUARY) {
            return "Feb ";
        } else if (month == Calendar.MARCH) {
            return "Mar ";
        } else if (month == Calendar.APRIL) {
            return "Apr ";
        } else if (month == Calendar.MAY) {
            return "May ";
        } else if (month == Calendar.JUNE) {
            return "Jun ";
        } else if (month == Calendar.JULY) {
            return "Jul ";
        } else if (month == Calendar.AUGUST) {
            return "Aug ";
        } else if (month == Calendar.SEPTEMBER) {
            return "Sept ";
        } else if (month == Calendar.OCTOBER) {
            return "Oct ";
        } else if (month == Calendar.NOVEMBER) {
            return "Nov ";
        } else if (month == Calendar.DECEMBER) {
            return "Dec ";
        }
        return null;
    }

    // Method to determine the day of the week based on it's parameter
    private String day(int day) {
        if (day == 0) {
            return "Sun";
        } else if (day == 1) {
            return "Mon";
        } else if (day == 2) {
            return "Tue";
        } else if (day == 3) {
            return "Wed";
        } else if (day == 4) {
            return "Thu";
        } else if (day == 5) {
            return "Fri";
        } else if (day == 6) {
            return "Sat";
        }
        return null;
    }

    // Creating the options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Setting the action to be done when a menu icon is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.calculate) {
            Intent intent = new Intent(getContext(), CalculateLaundry.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.Logout) {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            startActivity(intent);
            getActivity().finish();
            ParseUser.logOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        doAllDatabaseTasks();
    }
}
