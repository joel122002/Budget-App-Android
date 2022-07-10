package com.cr7.budgetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OtherUsers extends Fragment implements AdapterView.OnItemClickListener {
    private ListView listViewUsers;
    private ArrayList<String> arrayList;
    private View view;

    public OtherUsers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_other_users, container, false);
        // Creating an ArrayList that will hold the other users
        arrayList = new ArrayList();
        // Creating an array adapter which will be used ti set the text of the ListView that
        // holds all the other users names
        final ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.list_white_text
                , arrayList);
        // Referencing to the ListView
        listViewUsers = view.findViewById(R.id.listViewUsers);
        // Getting all the users from the server
        ParseQuery<ParseUser> users = ParseUser.getQuery();
        users.whereNotEqualTo("username", ParseUser.getCurrentUser().get("username"));
        users.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (objects != null && objects.size() > 0 && e == null) {
                    for (ParseUser user : objects) {
                        arrayList.add(user.getUsername());
                    }
                } else if (e != null) {
                    FancyToast.makeText(getContext(), e.getMessage() + "", FancyToast.LENGTH_LONG
                            , FancyToast.ERROR, false).show();
                }
                // Setting the adapter for the list view so that everything in the ArrayList can
                // be shown by the list
                listViewUsers.setAdapter(arrayAdapter);
            }
        });
        // Setting an OnClickListener for the list items
        listViewUsers.setOnItemClickListener(OtherUsers.this);
        return view;
    }

    // On Clicking one of the items you get all the expenses of that user
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), ViewUserData.class);
        intent.putExtra("username", arrayList.get(position));
        startActivity(intent);
    }
}
