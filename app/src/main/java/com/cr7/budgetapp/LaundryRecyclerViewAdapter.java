package com.cr7.budgetapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LaundryRecyclerViewAdapter extends RecyclerView.Adapter<LaundryRecyclerViewAdapter.LaundryItemHolder> {
    private List<LaundryItem> laundryItems = new ArrayList<>();
    private OnItemLongClickListener listenerLongClick;
    private Context context;

    @NonNull
    @Override
    // This method is responsible for inflating the layout so that it can later be
    // added to the recyclerView
    public LaundryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.laundry_item,
                parent, false);
        context = parent.getContext();
        // Returning the view which will later be added to the RecyclerView in the
        // onBindViewHolder method
        return new LaundryItemHolder(itemView);
    }

    // This method is responsible for adding the text to the view and adding it to the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull LaundryItemHolder holder, int position) {
        // We're getting the item on that specific position and setting the item and price on the
        // respective TextViews
        LaundryItem laundryItem = laundryItems.get(position);
        DateConverter dateConverter = new DateConverter();
        Date dateOfItem = dateConverter.DaysToDate(laundryItem.getDate());
        String dateString = dateToddSlashmmSlashyyyy(dateOfItem);
        holder.textViewLaundryDate.setText(dateString);
        holder.textViewLaundryCount.setText(String.valueOf(laundryItem.getLaundry()));
    }

    // Getting the number of items in the RecyclerView i.e. it will be equal to the size of the list
    @Override
    public int getItemCount() {
        return laundryItems.size();
    }

    // Setting the items List so that the RecyclerView can be populated
    public void setLaundryItems(List<LaundryItem> laundryItems) {
        Log.i("Laundry", String.valueOf(laundryItems.size()));
        this.laundryItems.clear();
        this.laundryItems.addAll(laundryItems);
        notifyDataSetChanged();
    }

    // Creating an interface for the onLongClick functionality
    public interface OnItemLongClickListener {
        // This method provides the item that is clicked on so that we can use it in the
        // method
        void onItemLongClick(LaundryItem laundryItem);
    }
    // This method will set accept a listener so that the he long clicks can be detected
    public void setOnItemLongClickListener(LaundryRecyclerViewAdapter.OnItemLongClickListener listenerLongClick) {
        this.listenerLongClick = listenerLongClick;
    }

    // The LaundryItemHolder Class extends the ViewHolder class. The ViewHolder class in the class that
    // inflates the layout
    class LaundryItemHolder extends RecyclerView.ViewHolder {
        // We're declaring the TextViews present in the inflated layout i.e. laundry_item.xml
        private TextView textViewLaundryDate;
        private TextView textViewLaundryCount;
        private ImageView imageViewDate;

        // Calling the constructor
        public LaundryItemHolder(@NonNull View laundryItemView) {
            // Calling the constructor of the super/parent class
            super(laundryItemView);
            // Initializing the TextInputEditTexts
            textViewLaundryDate = laundryItemView.findViewById(R.id.textViewLaundryDate);
            textViewLaundryCount = laundryItemView.findViewById(R.id.textViewLaundryCount);
            imageViewDate = laundryItemView.findViewById(R.id.imageViewDate);
            // Declaring a long l=click listener which will be applied to all the Views/Items in the
            // RecyclerView
            View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Getting the position of the current View
                    int position = getAdapterPosition();
                    // Checking if the listener is not null and the position is valid
                    if (listenerLongClick != null && position != RecyclerView.NO_POSITION)
                        listenerLongClick.onItemLongClick(laundryItems.get(position));
                    return true;
                }
            };
            // Setting an onLongClickListener for the ImageView on which's long clicking the item
            // delete dialog must be shown
            imageViewDate.setOnLongClickListener(longClickListener);
            // Setting an onLongClickListener for laundry_item.xml on which's long clicking the item
            // delete dialog must be shown
            laundryItemView.setOnLongClickListener(longClickListener);
            // Setting an onLongClickListener for the TextView as well as the user may long click
            // on the TextView to delete the item and it wouldn't work without this
            textViewLaundryDate.setOnLongClickListener(longClickListener);
            // Setting an onLongClickListener for the TextView as well as the user may long click
            // on the TextView to delete the item and it wouldn't work without this
            textViewLaundryCount.setOnLongClickListener(longClickListener);
        }
    }

    private String dateToddSlashmmSlashyyyy(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }
}
