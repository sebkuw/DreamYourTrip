package com.example.dreamyourtrip.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AddTripDialogFragment extends DialogFragment implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList<String> transportTypes;

    private EditText etTripName, etDescription, etActualMoney;
    private Spinner spinnerTransportType;
    private Button btnSaveTrip, btnCancelTrip;

    public AddTripDialogFragment() {
    }

    public static AddTripDialogFragment newInstance(String title) {
        AddTripDialogFragment frag = new AddTripDialogFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_trip, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDB();
        initViews(view);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
    private void initDB(){
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    private void initViews(View v) {
        etTripName = v.findViewById(R.id.et_trip_name);
        etDescription = v.findViewById(R.id.et_description);
        etActualMoney = v.findViewById(R.id.et_actual_money);

        spinnerTransportType = v.findViewById(R.id.spinner_transport_type);
        transportTypes = new ArrayList<>();
        transportTypes.add("Choose transport type");
        transportTypes.add("BY CAR");
        transportTypes.add("BY BIKE");
        transportTypes.add("ON FOOT");
        ArrayAdapter<String> transportTypeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, transportTypes);
        transportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransportType.setAdapter(transportTypeAdapter);

        btnSaveTrip = v.findViewById(R.id.btn_save_trip);
        btnCancelTrip = v.findViewById(R.id.btn_cancel_trip);

        btnSaveTrip.setOnClickListener(this);
        btnCancelTrip.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnSaveTrip) {
            if (etTripName.getText().toString().trim().equals("")) {
                Toast.makeText(getActivity(), "Fill the name of your trip!", Toast.LENGTH_LONG).show();
            } else if(spinnerTransportType.getSelectedItem().toString().equals("Choose transport type")) {
                Toast.makeText(getActivity(), "Choose transport type!", Toast.LENGTH_LONG).show();
            }
            else {
                FirebaseUser currentUser = mAuth.getCurrentUser();

                Trip t = new Trip();
                t.setOwnerID(currentUser.getUid());
                t.setName(etTripName.getText().toString());

                if(!etDescription.getText().toString().trim().equals(""))
                    t.setDescription(etDescription.getText().toString().trim());

                if(!etActualMoney.getText().toString().trim().equals(""))
                    t.setActualMoney(Double.parseDouble(etActualMoney.getText().toString().trim()));

                t.setTransportType(spinnerTransportType.getSelectedItem().toString());

                db.collection("trips")
                        .add(t);

                dismiss();
            }
        }

        if (v == btnCancelTrip) {
            dismiss();
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}