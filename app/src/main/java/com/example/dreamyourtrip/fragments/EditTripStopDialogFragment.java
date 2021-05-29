package com.example.dreamyourtrip.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.models.Trip;
import com.example.dreamyourtrip.models.TripStop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditTripStopDialogFragment extends DialogFragment implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private Trip t;
    private TripStop ts;

    private EditText etName, etDescription, etCost;
    private Button btnUpdate, btnCancel;

    public EditTripStopDialogFragment() {
    }

    public static EditTripStopDialogFragment newInstance(String title, Trip t, TripStop ts) {
        EditTripStopDialogFragment frag = new EditTripStopDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putParcelable("trip", t);
        args.putParcelable("tripStop", ts);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_trip_stop, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        t = getArguments().getParcelable("trip");
        ts = getArguments().getParcelable("tripStop");

        initDB();
        initViews(view);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initDB() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @SuppressLint("SetTextI18n")
    private void initViews(View v) {
        etName = v.findViewById(R.id.et_edit_trip_stop_name);
        etName.setText(ts.getName());
        etDescription = v.findViewById(R.id.et_edit_trip_stop_description);
        etDescription.setText(ts.getDescription());
        etCost = v.findViewById(R.id.et_edit_trip_stop_cost);
        etCost.setText("" + ts.getCost());

        btnUpdate = v.findViewById(R.id.btn_update_edit_trip_stop);
        btnCancel = v.findViewById(R.id.btn_cancel_edit_trip_stop);

        btnUpdate.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnUpdate) {
            TripStop editedTripStop = ts;

            if (!etName.getText().toString().trim().equals(""))
                editedTripStop.setName(etName.getText().toString().trim());
            if (!etDescription.getText().toString().trim().equals(""))
                editedTripStop.setDescription(etDescription.getText().toString().trim());
            if (!etCost.getText().toString().trim().equals(""))
                editedTripStop.setCost(Double.parseDouble(etCost.getText().toString().trim()));

            for (TripStop tripStop : t.getTripStops()) {
                if (tripStop == ts) {
                    tripStop = editedTripStop;
                }
            }

            db.collection("trips").document(t.getDocID())
                    .update(
                            "tripStops", t.getTripStops()
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("UPDATED TRIP STOP", "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("UPDATED TRIP STOP", "Error updating document", e);
                        }
                    });

            dismiss();
        }

        if (v == btnCancel) {
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