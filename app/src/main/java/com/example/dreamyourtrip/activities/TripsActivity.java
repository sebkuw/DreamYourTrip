package com.example.dreamyourtrip.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.fragments.AddTripDialogFragment;
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

public class TripsActivity extends AppCompatActivity implements
        View.OnClickListener,
        DialogInterface.OnDismissListener {

    private static final int REQUEST_EDIT_TRIP = 4;

    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private LinearLayout.LayoutParams paramsHorizontal = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    private LinearLayout.LayoutParams paramsButtons2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f);

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ArrayList<Trip> userTrips = new ArrayList<>();

    private LinearLayout llForTrips;
    private Button btnAddTrip, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        initDB();

        initViews();
    }


    private void initDB() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        llForTrips = findViewById(R.id.ll_for_trips);

        btnAddTrip = findViewById(R.id.btn_add_new_trip);
        btnBack = findViewById(R.id.btn_back_trips);

        btnAddTrip.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        getUserTrips();
    }

    private void getUserTrips() {
        for (Trip t : userTrips) {
            userTrips.remove(t);
        }
        if (llForTrips.getChildCount() > 0)
            llForTrips.removeAllViews();

        db.collection("trips")
                .whereEqualTo("ownerID", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GET ALL TRIPS", document.getId() + " => " + document.getData());
                                Trip t = document.toObject(Trip.class);
                                t.setDocID(document.getId());

                                LinearLayout llHorizontal = new LinearLayout(llForTrips.getContext());
                                llHorizontal.setLayoutParams(params);
                                llHorizontal.setOrientation(LinearLayout.HORIZONTAL);

                                TextView tvName = new TextView(llHorizontal.getContext());
                                tvName.setLayoutParams(paramsHorizontal);
                                tvName.setText(t.getName());
                                tvName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                tvName.setTag(t);

                                tvName.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        Trip t = (Trip) v.getTag();
                                        Intent intent = new Intent(TripsActivity.this, EditTripActivity.class);
                                        intent.putExtra("trip", t);
                                        startActivityForResult(intent, REQUEST_EDIT_TRIP);
                                        return false;
                                    }
                                });
                                llHorizontal.addView(tvName);

                                ImageButton btnShow = new ImageButton(llHorizontal.getContext());
                                btnShow.setLayoutParams(paramsButtons2);
                                btnShow.setImageResource(R.drawable.ic_show_map);
                                btnShow.setTag(t);
                                btnShow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Trip t = (Trip) v.getTag();

                                        if (t.getTripStops().size() != 0) {
                                            setResult(Activity.RESULT_OK, new Intent()
                                                    .putExtra("trip", t));
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "First add any trip stop!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                llHorizontal.addView(btnShow);

                                ImageButton btnDelete = new ImageButton(llHorizontal.getContext());
                                btnDelete.setLayoutParams(paramsButtons2);
                                btnDelete.setImageResource(R.drawable.ic_remove_foreground);
                                btnDelete.setTag(t);
                                btnDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Trip t = (Trip) v.getTag();

                                        db.collection("trips")
                                                .document(t.getDocID())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("DELETE TRIP", "DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("DELETE TRIP", "Error deleting document", e);
                                                    }
                                                });

                                        getUserTrips();
                                    }
                                });
                                llHorizontal.addView(btnDelete);

                                llForTrips.addView(llHorizontal, params);

                                double fullCost = 0;
                                for (TripStop ts : t.getTripStops())
                                    fullCost += ts.getCost();

                                ProgressBar progressBar = new ProgressBar(llForTrips.getContext(), null, android.R.attr.progressBarStyleHorizontal);
                                progressBar.setLayoutParams(params);
                                progressBar.setMax((int) fullCost);
                                progressBar.setProgress((int) t.getActualMoney());

                                llForTrips.addView(progressBar, params);
                            }
                        } else {
                            Log.w("GET ALL TRIPS", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            finish();
        } else if (v == btnAddTrip) {
            AddTripDialogFragment addTripDialogFragment = AddTripDialogFragment.newInstance("Some Title");
            addTripDialogFragment.show(getSupportFragmentManager(), "fragment_add_trip");
        }
    }

    public void onDismiss(DialogInterface dialog) {
        Log.i("ON DISMISS", "on dismiss");
        getUserTrips();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_TRIP && resultCode == Activity.RESULT_OK) {
            initViews();
        }
    }
}
