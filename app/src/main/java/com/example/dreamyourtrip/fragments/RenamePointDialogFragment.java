package com.example.dreamyourtrip.fragments;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class RenamePointDialogFragment extends DialogFragment implements
        View.OnClickListener {

    private FirebaseFirestore db;

    private EditText etRenamePoint;
    private Button btnUpdateName, btnUpdatePointCancel;

    private String pointID, pointName;

    public RenamePointDialogFragment() {
    }

    public static RenamePointDialogFragment
    newInstance(String pointID, String pointName) {
        RenamePointDialogFragment fragment = new RenamePointDialogFragment();
        Bundle args = new Bundle();
        args.putString("pointID", pointID);
        args.putString("pointName", pointName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rename_point, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pointID = getArguments().getString("pointID");
        pointName = getArguments().getString("pointName");

        initDB();
        initializeViews(view);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void initializeViews(View view) {
        etRenamePoint = view.findViewById(R.id.etRenamePoint);
        etRenamePoint.setText(pointName);

        btnUpdateName = view.findViewById(R.id.btnUpdatePoint);
        btnUpdatePointCancel = view.findViewById(R.id.btnUpdatePointCancel);

        btnUpdateName.setOnClickListener(this);
        btnUpdatePointCancel.setOnClickListener(this);
    }

    private void initDB() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View v) {
        if (v == btnUpdateName) {
            Log.i("CHECK", pointID);
            db.collection("points").document(pointID)
                    .update(
                            "name", etRenamePoint.getText().toString()
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


            dismiss();
        } else if (v == btnUpdatePointCancel) {
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