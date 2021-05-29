package com.example.dreamyourtrip.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.fragments.EditTripStopDialogFragment;
import com.example.dreamyourtrip.models.Trip;
import com.example.dreamyourtrip.models.TripStop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class EditTripActivity extends AppCompatActivity implements
        View.OnClickListener,
        DialogInterface.OnDismissListener {

    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private LinearLayout.LayoutParams paramsHorizontal = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 4);
    private LinearLayout.LayoutParams paramsButtons2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f);
    private LinearLayout.LayoutParams paramsButtons3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.125f);

    private FirebaseFirestore db;

    private Trip t;

    private ArrayList<String> transportTypes;

    private EditText etTripName, etDescription, etActualMoney;
    private Spinner spinnerTransportType;
    private LinearLayout llForTripStops;
    private Button btnUpdateTrip, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);

        t = getIntent().getParcelableExtra("trip");

        initDB();
        initViews();
    }

    private void initDB() {
        db = FirebaseFirestore.getInstance();

    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        etTripName = findViewById(R.id.et_edit_trip_name);
        etTripName.setText(t.getName());
        etDescription = findViewById(R.id.et_edit_description);
        if (t.getDescription() != null)
            etDescription.setText(t.getDescription());
        etActualMoney = findViewById(R.id.et_edit_actual_money);
        if (t.getActualMoney() != 0)
            etActualMoney.setText(t.getActualMoney() + "");

        spinnerTransportType = findViewById(R.id.spinner_edit_transport_type);
        transportTypes = new ArrayList<>();
        transportTypes.add("Choose transport type");
        transportTypes.add("BY CAR");
        transportTypes.add("BY BIKE");
        transportTypes.add("ON FOOT");
        ArrayAdapter<String> transportTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, transportTypes);
        transportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransportType.setAdapter(transportTypeAdapter);
        if ("BY CAR".equals(t.getTransportType())) {
            spinnerTransportType.setSelection(1);
        } else if ("BY BIKE".equals(t.getTransportType())) {
            spinnerTransportType.setSelection(2);
        } else if ("ON FOOT".equals(t.getTransportType())) {
            spinnerTransportType.setSelection(3);
        }

        llForTripStops = findViewById(R.id.ll_for_trip_stops);
        if (llForTripStops.getChildCount() > 0)
            llForTripStops.removeAllViews();

        for (int i = 0; i < t.getTripStops().size(); i++) {
            TripStop ts = t.getTripStops().get(i);

            LinearLayout llHorizontal = new LinearLayout(llForTripStops.getContext());
            llHorizontal.setLayoutParams(params);
            llHorizontal.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(llHorizontal.getContext());
            tvName.setLayoutParams(paramsHorizontal);
            tvName.setText(ts.getName());
            tvName.setTag(ts);

            tvName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TripStop ts = (TripStop) v.getTag();
                    EditTripStopDialogFragment editPointDialogFragment = EditTripStopDialogFragment.newInstance("Edit trip stop", t, ts);
                    editPointDialogFragment.show(getSupportFragmentManager(), "fragment_edit_trip_stop");
                    return false;
                }
            });
            llHorizontal.addView(tvName);

            if (i != 0) {
                ImageButton btnUp = new ImageButton(llHorizontal.getContext());
                btnUp.setLayoutParams(paramsButtons2);
                btnUp.setImageResource(R.drawable.ic_up_arrow);
                btnUp.setTag(ts);
                btnUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TripStop ts = (TripStop) v.getTag();

                        TripStop tsToSwap = null;

                        for (int i = 0; i < t.getTripStops().size(); i++) {
                            if (t.getTripStops().get(i).getOrder() == ts.getOrder() - 1)
                                tsToSwap = t.getTripStops().get(i);
                        }
                        if (tsToSwap != null) {
                            int order = ts.getOrder();
                            ts.setOrder(tsToSwap.getOrder());
                            tsToSwap.setOrder(order);
                        }

                        Collections.sort(t.getTripStops());
                        updateTripStops();
                        initViews();
                    }
                });
                llHorizontal.addView(btnUp);
            }

            if (i != t.getTripStops().size() - 1) {
                ImageButton btnDown = new ImageButton(llHorizontal.getContext());
                btnDown.setLayoutParams(paramsButtons3);
                btnDown.setImageResource(R.drawable.ic_down_arrow);
                btnDown.setTag(ts);
                btnDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TripStop ts = (TripStop) v.getTag();

                        TripStop tsToSwap = null;

                        for (int i = 0; i < t.getTripStops().size(); i++) {
                            if (t.getTripStops().get(i).getOrder() == ts.getOrder() + 1)
                                tsToSwap = t.getTripStops().get(i);
                        }
                        if (tsToSwap != null) {
                            int order = ts.getOrder();
                            ts.setOrder(tsToSwap.getOrder());
                            tsToSwap.setOrder(order);
                        }

                        Collections.sort(t.getTripStops());
                        updateTripStops();
                        initViews();
                    }
                });
                llHorizontal.addView(btnDown);
            }

            ImageButton btnDelete = new ImageButton(llHorizontal.getContext());
            btnDelete.setLayoutParams(paramsButtons2);
            btnDelete.setImageResource(R.drawable.ic_remove);
            btnDelete.setTag(ts);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TripStop ts = (TripStop) v.getTag();

                    for (TripStop tripStop : t.getTripStops()) {
                        if (tripStop.getOrder() > ts.getOrder())
                            tripStop.setOrder(tripStop.getOrder() - 1);
                    }

                    t.getTripStops().remove(ts);

                    updateTripStops();
                    initViews();
                }
            });
            llHorizontal.addView(btnDelete);

            llForTripStops.addView(llHorizontal, params);
        }

        btnUpdateTrip = findViewById(R.id.btn_update_trip);
        btnBack = findViewById(R.id.btn_edit_trip_back);

        btnUpdateTrip.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void updateTripStops() {
        db.collection("trips").document(t.getDocID())
                .update(
                        "tripStops", t.getTripStops()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("UPDATE TRIP STOPS", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("UPDATE TRIP STOPS", "Error updating document", e);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnUpdateTrip) {
            double actualMoney = 0;
            if (!etActualMoney.getText().toString().trim().equals(""))
                actualMoney = Double.parseDouble(etActualMoney.getText().toString().trim());

            if (etTripName.getText().toString().trim().equals("")) {
                Toast.makeText(this, "Fill the name of your trip!", Toast.LENGTH_LONG).show();
            } else if (spinnerTransportType.getSelectedItem().toString().equals("Choose transport type")) {
                Toast.makeText(this, "Choose transport type!", Toast.LENGTH_LONG).show();
            } else {

                db.collection("trips").document(t.getDocID())
                        .update(
                                "name", etTripName.getText().toString().trim(),
                                "transportType", spinnerTransportType.getSelectedItem().toString(),
                                "description", etDescription.getText().toString().trim(),
                                "actualMoney", actualMoney
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("UPDATE POINT", "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("UPDATE POINT", "Error updating document", e);
                            }
                        });

                setResult(Activity.RESULT_OK);
                finish();
            }
        }

        if (v == btnBack) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.i("ON DISMISS", "on dismiss");
        initViews();
    }
}