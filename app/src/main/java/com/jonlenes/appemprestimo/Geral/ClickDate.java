package com.jonlenes.appemprestimo.Geral;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Created by Jonlenes on 19/08/2016.
 */
public class ClickDate implements View.OnClickListener {

    private FragmentManager fragmentManager;
    private EditText edtDate;

    public ClickDate(FragmentManager fragmentManager, EditText edtDate) {
        this.fragmentManager = fragmentManager;
        this.edtDate = edtDate;
    }

    @Override
    public void onClick(View v) {
        class DatePickerFragment extends DialogFragment
                implements DatePickerDialog.OnDateSetListener {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                Calendar c = Calendar.getInstance();
                if (edtDate.getText().length() > 0)
                    c.setTime(DateUtil.parseDate(edtDate.getText().toString()));
                return new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            }

            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                edtDate.setText(DateUtil.formatDate(calendar.getTime()));
            }
        }

        new DatePickerFragment().show(fragmentManager, "dialog");
    }
};