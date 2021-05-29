package com.example.dreamyourtrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.models.Point;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPointDialogFragment extends DialogFragment implements
        View.OnClickListener {

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private double longitude, latitude;
    private TextView tvLongitude, tvLatitude;
    private EditText etPointName;
    private Button btnSave, btnCancel;

    public AddPointDialogFragment() {
    }

    public static AddPointDialogFragment newInstance(String title, double longitude, double latitude) {
        AddPointDialogFragment frag = new AddPointDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putDouble("longitude", longitude);
        args.putDouble("latitude", latitude);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_point, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        initializeViews(view);

        getDialog().setTitle(getArguments().getString("title", "Add point"));
        longitude = getArguments().getDouble("longitude");
        latitude = getArguments().getDouble("latitude");

        tvLongitude.setText(String.format("%s", longitude));
        tvLatitude.setText(String.format("%s", latitude));

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initializeViews(View v) {
        tvLongitude = v.findViewById(R.id.tvLongitude);
        tvLatitude = v.findViewById(R.id.tvLatitude);
        etPointName = v.findViewById(R.id.etPointName);

        btnSave = v.findViewById(R.id.btnSave);
        btnCancel = v.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnSave) {
            if (etPointName.getText().toString().trim().equals("")) {
                Toast.makeText(getActivity(), "Fill the name or chose the point type!", Toast.LENGTH_LONG).show();
            } else {
                FirebaseUser currentUser = mAuth.getCurrentUser();

                Point point = new Point();
                point.setOwnerID(currentUser.getUid());
                point.setLongitude(longitude);
                point.setLatitude(latitude);
                point.setName(etPointName.getText().toString());

                CollectionReference points = db.collection("points");
                points.add(point);

                dismiss();
            }
        }

        if (v == btnCancel) {
            dismiss();
        }
    }
}