package com.cr7.budgetapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.view.ContextThemeWrapper;

public class NumberPickerDialog extends AppCompatDialogFragment {
    // Creating Instance variables
    private NumberPicker numberPicker;
    private NumberPickerDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Creating an AlertDialog by using the Builder and setting it's theme as R.style
        // .AlertDialog
        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                        R.style.AlertDialog));
        // Creating a layout inflater that will later inflate the NumberPickerDialog's layout file
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflating the layout file that was made for the NumberPickerDialog and assigning it to
        // a View "view"
        View view = inflater.inflate(R.layout.number_picker_dailog, /*root view*/null);
        // Creating an AlertDialog and setting it's view as "view"
        final AlertDialog dialog = builder.setView(view)
                // Setting it such that it cannot be dismissed by th user
                .setCancelable(false)
                // Setting the title as "Price per laundry"
                .setTitle("Price per laundry")
                // Setting the Positive button's text and OnClickListener
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Getting the price from the NumberPicker and assigning it to an integer
                        // "price"
                        int price = numberPicker.getValue();
                        // Setting the value of the parameter of NumberPickerDialogListener's
                        // method "sendPrice". This method can later be accessed by the activity
                        // from which it was invoked
                        listener.sendPrice(price);
                    }
                }).create();
        // Changing the positive button's color on calling the show method
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor(
                        "#888888"));
            }
        });
        // Referencing to the NumberPicker
        numberPicker = view.findViewById(R.id.numberPicker);
        // Setting a minimum value of 1 and maximum of 100 as the price of 1 laundry item cannot
        // be 0 and will not be more than 100
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        return dialog;
    }

    // onAttach is called when the Dialog/Fragment is attached to the Activity. We usually check
    // if the listeners are implemented in the
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Trying to initialize the listener, if it isn't successful it means that the interface
        // NumberPickerDialogListener is not implemented in the activity
        try {
            listener = (NumberPickerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " +
                    "NumberPickerDialogListener");
        }
    }

    // Interface that will send data from the NumberPickerDialog to the activity.
    public interface NumberPickerDialogListener {
        void sendPrice(int price);
    }
}
