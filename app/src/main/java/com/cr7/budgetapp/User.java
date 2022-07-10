package com.cr7.budgetapp;

import android.app.DatePickerDialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.cr7.budgetapp.DateConverter.HOUR;
import static com.cr7.budgetapp.DateConverter.MINUTE;

/**
 * A simple {@link Fragment} subclass.
 */
public class User extends Fragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener,
        SwipeRefreshLayout.OnRefreshListener {
    private RecyclerViewAdapter recyclerViewAdapter;
    private Date from, dateOfToday;
    private SimpleDateFormat day, month, year, daymonthyear;
    private String skipType;
    private DateConverter converter;
    private TextInputEditText editTextNewItem, editTextNewPrice;
    private Button buttonNextDate;
    private TextView textViewDate, textViewDailyTotal;
    private SQLiteDatabase database;
    private ConstraintLayout rootLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    public User() {
        // Required empty public constructor
    }

    // onCreateView method
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        // Declaring all instance variables
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        // Enabling vector usage in this activity
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        // Referencing to the root layout so that on clicking the root layout all view will go out
        // of focus
        rootLayout = view.findViewById(R.id.rootLayout);
        rootLayout.setOnClickListener(User.this);
        // linking these instance variables to their respective UI components
        Button buttonPreviousDate = view.findViewById(R.id.buttonPreviousDate);
        buttonNextDate = view.findViewById(R.id.buttonNextDate);
        textViewDate = view.findViewById(R.id.textViewDate);
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
        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);
        // Initializing the SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(User.this);
        // Setting up the OnLongClickListener so that we can delete an item on long click
        recyclerViewAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Item item) {
                User.this.onItemLongClick(item);
            }
        });
        // Image view with a tick. Upon clicking it the item will be added
        ImageView imageViewDone = view.findViewById(R.id.imageViewDone);
        // EditText In which the user adds a new Item
        editTextNewItem = view.findViewById(R.id.editTextNewItem);
        // EditText on which the user ads the price of the new Item
        editTextNewPrice = view.findViewById(R.id.editTextNewPrice);
        // TextView that holds the days total of the current user
        textViewDailyTotal = view.findViewById(R.id.textViewDailyTotal);
        // The calender icon on the top left just below the new item EditText on whose clicking 
        // the DatePicker is triggered
        ImageView imageViewDate = view.findViewById(R.id.imageViewDate);
        // Defining SimpleDateFormat for day,month year and dd/MM/yyyy
        day = new SimpleDateFormat("dd");
        month = new SimpleDateFormat("MM");
        year = new SimpleDateFormat("yyyy");
        daymonthyear = new SimpleDateFormat("dd/MM/yyyy");
        dateOfToday = Calendar.getInstance().getTime();
        // Object of type DateConverter that will convert the date to an integer as SQLite does 
        // not have a date data-type
        converter = new DateConverter();
        // from was not directly assigned the value dateOfToday because dateOfToday holds the time
        // as well. By this way the time is reset to 00:00:00
        try {
            from = daymonthyear.parse(daymonthyear.format(dateOfToday));
            assert from != null;
            from = new Date(from.getTime() + 5 * HOUR + 30 * MINUTE);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        // Setting the default skip to day as the spinners default value is Day which can later be
        // changed by the User
        skipType = "Day";
        // Default daily total is ₹0
        textViewDailyTotal.setText("₹0");
        // This is array adapter which holds the values of the spinner
        assert getContext() != null;
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.skip, R.layout.custom_spinner);
        // Setting the dropdown for the spinner
        arrayAdapter.setDropDownViewResource(R.layout.custim_dropdown);
        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent),
                PorterDuff.Mode.SRC_ATOP);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(User.this);
        // Setting the next button as invisible and disabling it so that the user doesn't go ahead
        // of today
        buttonNextDate.setVisibility(View.INVISIBLE);
        buttonNextDate.setEnabled(false);
        // Object of SQLiteDatabase. It is through this object that we will interact with the 
        // database
        database = new SQLiteDatabase(getContext());
        // Doing all the work to get the items and making sure the local database is synced with 
        // the server
        doAllDatabaseTasks();
        // Setting OnClickListeners for the UI components
        buttonPreviousDate.setOnClickListener(User.this);
        buttonNextDate.setOnClickListener(User.this);
        imageViewDone.setOnClickListener(User.this);
        imageViewDate.setOnClickListener(this);
        return view;
    }

    // Updating and adding new entries
    @Override
    public void onClick(View v) {
        // New entry
        if (v.getId() == R.id.imageViewDone) {
            // Checking if the Item and price are not null if they are not null then adding them
            // to the database
            if (editTextNewItem.getText() != null && !editTextNewItem.getText().toString().isEmpty()) {
                if (editTextNewPrice.getText() != null && !editTextNewPrice.getText().toString().isEmpty()) {
                    Item item = new Item(converter.DateToDays(from), null,
                            editTextNewItem.getText().toString(),
                            Integer.parseInt(editTextNewPrice.getText().toString()));
                    database.addItem(item);
                    // Trying to hide the keyboard
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    try {
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0/*flag of type int*/);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editTextNewItem.setText("");
                    editTextNewPrice.setText("");
                    editTextNewItem.clearFocus();
                    editTextNewPrice.clearFocus();
                    doAllDatabaseTasks();
                } else {
                    FancyToast.makeText(getContext(), "Item cannot be empty",
                            FancyToast.LENGTH_SHORT, FancyToast.ERROR, false);
                }
            } else {
                FancyToast.makeText(getContext(), "Item cannot be empty", FancyToast.LENGTH_SHORT
                        , FancyToast.ERROR, false);
            }
        }
        // Previous button is pressed
        else if (v.getId() == R.id.buttonPreviousDate) {
            // Setting focus on the root view so that no EditText is in focus
            rootLayout.requestFocus();
            // As soon a the previous button is clicked the next button becomes visible and clickable
            buttonNextDate.setVisibility(View.VISIBLE);
            buttonNextDate.setEnabled(true);
            textViewDailyTotal.setText("0");
            // Checking the skip type so that it goes behind by one day/week/month/year
            if (skipType.matches("Day")) {
                from = new DateTime(from).minusDays(1).toDate();
            } else if (skipType.matches("Week")) {
                from = new DateTime(from).minusWeeks(1).toDate();
            } else if (skipType.matches("Month")) {
                from = new DateTime(from).minusMonths(1).toDate();
            } else {
                from = new DateTime(from).minusYears(1).toDate();
            }
            // Calling the doAllDatabaseTasks method display the data from the local database and
            // sync the local database with the server
            doAllDatabaseTasks();

        }
        // Next Button is pressed
        else if (v.getId() == R.id.buttonNextDate) {
            // Setting focus on the root view so that no EditText is in focus
            rootLayout.requestFocus();
            textViewDailyTotal.setText("₹0");
            // Checking the skip type so that it goes ahead by one day/week/month/year
            if (skipType.matches("Day")) {
                from = new DateTime(from).plusDays(1).toDate();
            } else if (skipType.matches("Week")) {
                from = new DateTime(from).plusWeeks(1).toDate();
            } else if (skipType.matches("Month")) {
                from = new DateTime(from).plusMonths(1).toDate();
            } else {
                from = new DateTime(from).plusYears(1).toDate();
            }
            // Checking if the given date is greater than today's date so that the date doesn't 
            // exceed today's date
            if (from.after(dateOfToday) || from.equals(dateOfToday)) {
                // Assigning the from date to today's date so that it will show today's entries
                from = dateOfToday;
                // Setting button next as invisible and disabling it
                buttonNextDate.setVisibility(View.INVISIBLE);
                buttonNextDate.setEnabled(false);
            }
            // Getting al Items and syncing local database with the server
            doAllDatabaseTasks();
        } else if (v.getId() == R.id.imageViewDate) {
            showDatePickerDialog();
        } else if (v.getId() == R.id.rootLayout) {
            rootLayout.requestFocus();
        }
    }

    // On long clicking an Item it will ask the user if he or she wants to delete the Item
    private void onItemLongClick(final Item item) {
        assert getContext() != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
        builder.setMessage("Are you sure you want to delete this entry?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.deleteItem(item);
                        getData(from);
                        deleteItemsToBeDeletedFromServer();
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

    // On refreshing
    @Override
    public void onRefresh() {
        doAllDatabaseTasks();
    }

    // Does the task of getting all item and also syncs the local database to the server
    private void doAllDatabaseTasks() {
        getData(from);
        uploadUnfinishedItemsData();
        uploadUnfinishedLaundryData();
        fetchItemsFromServer();
        fetchLaundryFromServer();
        deleteItemsToBeDeletedFromServer();
        deleteLaundryToBeDeletedFromServer();
    }

    // Function to get all data from the server and display it on the screen of the user's device
    private void getData(Date date) {
        final SQLiteDatabase database = new SQLiteDatabase(getContext());
        DateConverter converter = new DateConverter();
        int dateInInt = converter.DateToDays(date);
        List<Item> items = database.getAllItems(dateInInt);
        String dateAsString =
                day.format(from) + " " + checkMonth(month.format(from)) + year.format(from) + " " +
                        "(" + day(from.getDay()) + ")";
        textViewDate.setText(dateAsString);
        for (Item item : items) {
            Log.i("ITems", item.getItem());
            Log.i("ITems", String.valueOf(item.getPrice()));
        }
        recyclerViewAdapter.setItems(items);
        int dailytotal = 0;
        for (Item item : items) {
            dailytotal += item.getPrice();
        }
        String dailyTotalWithPrefix = "₹" + dailytotal;
        textViewDailyTotal.setText(dailyTotalWithPrefix);
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
                parseObject.put("Date", converter.DaysToDate(item.getDate()));
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

    // Getting data from server and making sure that the local database in sin sync with the server
    private void fetchItemsFromServer() {
        List<String> objectIds = database.getObjectIdFromToUploadTableOrDeleteTable();
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Items");
        // Assigning the parameter so as to get objects from "threeDaysBeforeFrom" to "from" and
        // order it in the descending order of "createdAt"
        parseQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        parseQuery.whereNotContainedIn("objectId", objectIds);
        Date threeDaysBeforeFrom = new Date(from.getTime() - 72 * HOUR);
        parseQuery.whereGreaterThanOrEqualTo("Date", threeDaysBeforeFrom);
        parseQuery.whereLessThanOrEqualTo("Date", from);
        parseQuery.orderByDescending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0 && e == null) {
                    List<Integer> dates = new ArrayList<>();
                    List<String> objectIds = new ArrayList<>();
                    int fromInInt = converter.DateToDays(from);
                    for (int i = 0; i <= 3; i++) {
                        dates.add(fromInInt - i);
                    }
                    List<Item> items = new ArrayList<>();
                    for (ParseObject item : objects) {
                        objectIds.add(item.getObjectId());
                        Item item1 = new Item(converter.DateToDays((Date) item.get("Date")),
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
                    database.deleteItemFromToDeleteTableByObjectId(uncommonElementsBetweenServerAndDeleteTable);
                    // Now we finally remove the objectIds of these items(whose objectId exist in
                    // the local database but not in the server)
                    database.deleteObjectIdFromItemInUploadTable(uncommonElementsBetweenServerAndUploadTable);
                    getData(from);
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
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

    // Method to upload the newly created or updated data that has only been updated/created in 
    // the local database and not in the server
    private void uploadUnfinishedLaundryData() {
        List<LaundryItem> items = database.getToUploadLaundry();
        Log.i("HelloGiza", "Size of items ot be uploaded is : " + String.valueOf(items.size()));
        for (final LaundryItem item : items) {
            ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Laundry");
            parseQuery.whereEqualTo("Date", converter.DaysToDate(item.getDate()));
            parseQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() > 0 && e == null) {
                        objects.get(0).put("Laundry", item.getLaundry());
                        objects.get(0).saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                database.moveLaundryToMain(item);
                            }
                        });
                    } else if (objects.size() == 0 && e == null) {
                        final ParseObject parseObject = new ParseObject("Laundry");
                        parseObject.put("Date", converter.DaysToDate(item.getDate()));
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
            });
        }
    }

    private Date getFirstDayOfMonth(int month, int year) {
        // Setting the starting date to the 1st day of the month
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, month, 1);
        return startDate.getTime();
    }

    private Date getLastDayOfMonth(int month, int year) {
        // Setting the starting date to the 1st day of the month
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, month, 1);
        Calendar endDate = startDate;
        // Gets the last day of the month
        endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        // If endDate > today's date then set today's date as end date
        if (endDate.getTime().after(new Date()))
            return new Date();
        return endDate.getTime();
    }

    // Getting data from server and making sure that the local database in sin sync with the server
    private void fetchLaundryFromServer() {
        List<Integer> intDatesFromDatabase =
                database.getDatesOfToUploadTableOrDeleteLaundryTable();
        List<Date> datesFromDatabase = new ArrayList<>();
        for (int i : intDatesFromDatabase) {
            datesFromDatabase.add(converter.DaysToDate(i));
        }
        final ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Laundry");
        parseQuery.whereNotContainedIn("Date", datesFromDatabase);
        // Assigning the parameter sp as to get objects from date:from to date:from+1 and order it
        // in the descending order of the date
        parseQuery.orderByAscending("Date");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0 && e == null) {
                    List<String> objectIds = new ArrayList<>();
                    List<Integer> dates = new ArrayList<>();
                    List<LaundryItem> laundryItems = new ArrayList<>();
                    for (ParseObject item : objects) {
                        objectIds.add(item.getObjectId());
                        dates.add(converter.DateToDays((Date) item.get("Date")));
                        LaundryItem item1 = new LaundryItem(converter.DateToDays((Date) item.get(
                                "Date")), item.getObjectId(),
                                Integer.parseInt(item.get("Laundry").toString()));
                        laundryItems.add(item1);
                    }

                    database.deleteAllFromMainLaundryTable(objectIds);
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
                        if (objects.size() > 0 && e == null) {
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

    // Sets the skip tye for the spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        skipType = parent.getItemAtPosition(position).toString();
    }

    // If nothing is selected by default the "skipType" is set to "Day"
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        skipType = "Day";
    }

    // Method that will determine the month based on the value of the parameter
    private String checkMonth(String monthString) {
        if (monthString.matches("01")) {
            return "Jan ";
        } else if (monthString.matches("02")) {
            return "Feb ";
        } else if (monthString.matches("03")) {
            return "Mar ";
        } else if (monthString.matches("04")) {
            return "Apr ";
        } else if (monthString.matches("05")) {
            return "May ";
        } else if (monthString.matches("06")) {
            return "Jun ";
        } else if (monthString.matches("07")) {
            return "Jul ";
        } else if (monthString.matches("08")) {
            return "Aug ";
        } else if (monthString.matches("09")) {
            return "Sept ";
        } else if (monthString.matches("10")) {
            return "Oct ";
        } else if (monthString.matches("11")) {
            return "Nov ";
        } else if (monthString.matches("12")) {
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

    // Method that changes the variable "from" to the date set by the user via the DatePicker
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateChosenByUser = dayOfMonth + "/" + (month + 1) + "/" + year;
        Log.i("Day", dateChosenByUser);
        try {
            from = daymonthyear.parse(dateChosenByUser);
            from = new Date(from.getTime() + 5 * HOUR + 30 * MINUTE);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        // Calling the getData method which will get all data from the server and display it on 
        // the screen of the user's device
        doAllDatabaseTasks();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.my_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.calculate) {
            Intent intent = new Intent(getContext(), CalculateItem.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.Logout) {
            Intent intent = new Intent(getContext(), LogInActivity.class);
            startActivity(intent);
            getActivity().finish();
            ParseUser.logOut();
        }
        return super.onOptionsItemSelected(item);
    }
}