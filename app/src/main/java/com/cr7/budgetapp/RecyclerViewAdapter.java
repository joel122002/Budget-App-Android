package com.cr7.budgetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemHolder> {
    private List<Item> items;
    private OnItemLongClickListener listenerLongClick;
    private Context context;

    @NonNull
    @Override
    // This method is responsible for inflating the layout so that it can later be
    // added to the recyclerView
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_layout,
                parent, false);
        context = parent.getContext();
        // Returning the view which will later be added to the RecyclerView in the
        // onBindViewHolder method
        return new ItemHolder(itemView);
    }

    // This method is responsible for adding the text to the view and adding it to the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        // We're getting the item on that specific position and setting the item and price on the
        // respective TextViews
        Item item = items.get(position);
        holder.editTextViewItem.setText(item.getItem());
        holder.editTextViewPrice.setText(String.valueOf(item.getPrice()));
    }

    // Getting the number of items in the RecyclerView i.e. it will be equal to the size of the list
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Setting the items List so that the RecyclerView can be populated
    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // This method will set accept a listener so that the he long clicks can be detected
    public void setOnItemLongClickListener(OnItemLongClickListener listenerLongClick) {
        this.listenerLongClick = listenerLongClick;
    }

    // Creating an interface for the onLongClick functionality
    public interface OnItemLongClickListener {
        // This method provides the item that is clicked on so that we can use it in the
        // method
        void onItemLongClick(Item item);
    }

    // The ItemHolder Class extends the ViewHolder class. The ViewHolder class in the class that
    // inflates the layout
    class ItemHolder extends RecyclerView.ViewHolder {
        // We're declaring the TextInputEditTexts present in the inflated layout i.e. temp_layout
        // .xml
        private TextInputEditText editTextViewItem;
        private TextInputEditText editTextViewPrice;
        private ImageView imgcheck;

        // Calling the constructor
        public ItemHolder(@NonNull View itemView) {
            // Calling the constructor of the super/parent class
            super(itemView);
            // Initializing the TextInputEditTexts
            editTextViewItem = itemView.findViewById(R.id.TextInputEditTextNewItemName);
            editTextViewPrice = itemView.findViewById(R.id.TextInputEditTextNewPrice);
            imgcheck = itemView.findViewById(R.id.imgcheck);
            // Setting an onClickListener for the ImageView on which's clicking the note is updated
            imgcheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Getting the position of the current View
                    int position = getAdapterPosition();
                    // Checking if the listener is not null and the position is valid
                    Item item = items.get(position);
                    SQLiteDatabase database = new SQLiteDatabase(context);
                    if (editTextViewItem.getText() != null && editTextViewPrice.getText() != null) {
                        item.setItem(editTextViewItem.getText().toString());
                        item.setPrice(Integer.parseInt(editTextViewPrice.getText().toString()));
                        database.updateItem(item);
                        FancyToast.makeText(context, "Successfully updated", FancyToast.LENGTH_LONG,
                                FancyToast.SUCCESS, false).show();
                        // Trying to close the keyboard
                        InputMethodManager inputMethodManager =
                                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        try {
                            View view1 = editTextViewItem.getRootView();
                            inputMethodManager.hideSoftInputFromWindow(view1.getWindowToken(), 0
                                    /*flag of type int*/);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            View view1 = editTextViewPrice.getRootView();
                            inputMethodManager.hideSoftInputFromWindow(view1.getWindowToken(), 0
                                    /*flag of type int*/);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        editTextViewItem.clearFocus();
                        editTextViewPrice.clearFocus();
                    }
                }
            });
            // Setting an onLongClickListener for temp_layout.xml
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Getting the position of the current View
                    int position = getAdapterPosition();
                    // Checking if the listener is not null and the position is valid
                    if (listenerLongClick != null && position != RecyclerView.NO_POSITION)
                        listenerLongClick.onItemLongClick(items.get(position));
                    return true;
                }
            });
            // Setting an onLongClickListener for the EditText as well as the user may long click
            // on the EditText to delete the item and it wouldn't work without this
            editTextViewItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Getting the position of the current View
                    int position = getAdapterPosition();
                    // Checking if the listener is not null and the position is valid
                    if (listenerLongClick != null && position != RecyclerView.NO_POSITION)
                        listenerLongClick.onItemLongClick(items.get(position));
                    return true;
                }
            });
            // Setting an onLongClickListener for the EditText as well as the user may long click
            // on the EditText to delete the item and it wouldn't work without this
            editTextViewPrice.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Getting the position of the current View
                    int position = getAdapterPosition();
                    // Checking if the listener is not null and the position is valid
                    if (listenerLongClick != null && position != RecyclerView.NO_POSITION)
                        listenerLongClick.onItemLongClick(items.get(position));
                    return true;
                }
            });
        }
    }
}
