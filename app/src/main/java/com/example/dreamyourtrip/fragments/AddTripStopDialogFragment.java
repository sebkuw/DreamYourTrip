package com.example.dreamyourtrip.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.example.dreamyourtrip.models.TripStop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AddTripStopDialogFragment extends DialogFragment implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private double longitude, latitude;
    private ArrayList<Trip> userTrips = new ArrayList<>();
    private ArrayList<String> userTripsString = new ArrayList<>();

    private Spinner spinnerTrips;
    private EditText etName, etDescription, etCost;
    private Button btnAdd, btnCancel;

    public AddTripStopDialogFragment() {
    }

    public static AddTripStopDialogFragment newInstance(String title, double latitude, double longitude) {
        AddTripStopDialogFragment frag = new AddTripStopDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putDouble("longitude", longitude);
        args.putDouble("latitude", latitude);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_trip_stop, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initDB();
        initViews(view);

        getDialog().setTitle(getArguments().getString("title", "Add trip stop"));
        longitude = getArguments().getDouble("longitude");
        latitude = getArguments().getDouble("latitude");

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initDB() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View v) {
        spinnerTrips = v.findViewById(R.id.spinner_add_trip_stop_trip);
        getUserTrips();
        ArrayAdapter<String> tripsAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, userTripsString);
        tripsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrips.setAdapter(tripsAdapter);

        etName = v.findViewById(R.id.et_add_trip_stop_name);
        etDescription = v.findViewById(R.id.et_add_trip_stop_description);
        etCost = v.findViewById(R.id.et_add_trip_stop_cost);

        btnAdd = v.findViewById(R.id.btn_add_trip_stop);
        btnCancel = v.findViewById(R.id.btn_cancel_add_trip_stop);

        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    public void getUserTrips() {
        userTripsString.add("Select trip");
        db.collection("trips")
                .whereEqualTo("ownerID", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GET ALL TRIPS", document.getId() + " => " + document.getData());
                                Trip t = document.toObject(Trip.class);
                                t.setDocID(document.getId());
                                userTripsString.add(t.getName());
                                userTrips.add(t);
                            }
                        } else {
                            Log.w("GET ALL TRIPS", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnAdd) {
            if (etName.getText().toString().trim().equals("")) {
                Toast.makeText(getActivity(), "Fill the name!", Toast.LENGTH_LONG).show();
            } else if (spinnerTrips.getSelectedItem().toString().equals("Select trip")) {
                Toast.makeText(getActivity(), "Choose your trip!", Toast.LENGTH_LONG).show();
            } else {
                Trip t = userTrips.get(spinnerTrips.getSelectedItemPosition() - 1);
                Log.i("SELECTED TRIP", t.toString());
                TripStop ts = new TripStop();
                ts.setOwnerID(currentUser.getUid());
                ts.setName(etName.getText().toString().trim());
                ts.setLatitude(latitude);
                ts.setLongitude(longitude);

                if (!etCost.getText().toString().trim().equals(""))
                    ts.setCost(Double.parseDouble(etCost.getText().toString().trim()));
                if (!etDescription.getText().toString().trim().equals(""))
                    ts.setDescription(etDescription.getText().toString().trim());

                ts.setOrder(t.getTripStops().size());

                t.addTripStop(ts);

                db.collection("trips").document(t.getDocID())
                        .update(
                                "tripStops", t.getTripStops()
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("UPDATE TRIP", "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("UPDATE TRIP", "Error updating document", e);
                            }
                        });


                dismiss();
            }
        }

        if (v == btnCancel) {
            dismiss();
        }
    }
}