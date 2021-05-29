package com.example.dreamyourtrip.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.fragments.RenamePointDialogFragment;
import com.example.dreamyourtrip.models.Point;
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

public class PointsActivity extends AppCompatActivity implements
        View.OnClickListener,
        DialogInterface.OnDismissListener {

    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private LinearLayout.LayoutParams paramsHorizontal = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    private LinearLayout.LayoutParams paramsButtons = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f);


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ArrayList<Point> userPoints = new ArrayList<>();
    private ArrayList<Point> checkedPoints = new ArrayList<>();

    private LinearLayout llForPoints;
    private Button btnShowOnMap, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        initDB();

        initializeViews();
    }


    private void initDB() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        llForPoints = findViewById(R.id.llForPoints);


        btnShowOnMap = findViewById(R.id.btn_show_on_map);
        btnBack = findViewById(R.id.btnBack);

        btnShowOnMap.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        getUserPoints();
    }

    private void getUserPoints() {
        for (Point p : userPoints) {
            userPoints.remove(p);
        }
        if (llForPoints.getChildCount() > 0)
            llForPoints.removeAllViews();

        db.collection("points")
                .whereEqualTo("ownerID", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("GET ALL POINTS", document.getId() + " => " + document.getData());
                                Point p = document.toObject(Point.class);
                                p.setDocID(document.getId());

                                // Layout for checkbox and delete imgbtn
                                LinearLayout llHorizontal = new LinearLayout(llForPoints.getContext());
                                llHorizontal.setLayoutParams(params);
                                llHorizontal.setOrientation(LinearLayout.HORIZONTAL);

                                CheckBox checkBox = new CheckBox(llHorizontal.getContext());
                                checkBox.setLayoutParams(paramsHorizontal);
                                checkBox.setText(p.getName());
                                checkBox.setTag(p);
                                checkBox.setChecked(false);
                                checkBox.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        Point p = (Point) v.getTag();
                                        FragmentManager fm = getSupportFragmentManager();
                                        RenamePointDialogFragment renamePointDialogFragment = RenamePointDialogFragment.newInstance(p.getDocID(), p.getName());
                                        renamePointDialogFragment.show(fm, "edit_point");

                                        return false;
                                    }
                                });
                                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        Point p = (Point) buttonView.getTag();
                                        if (isChecked && !checkedPoints.contains(p))
                                            checkedPoints.add(p);
                                        else if (!isChecked)
                                            checkedPoints.remove(p);

                                        Log.i("CHECKED POINTS", checkedPoints.toString());
                                    }
                                });
                                llHorizontal.addView(checkBox);

                                ImageButton button = new ImageButton(llHorizontal.getContext());
                                button.setLayoutParams(paramsButtons);
                                button.setImageResource(R.drawable.ic_remove);
                                button.setTag(p);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Point p = (Point) v.getTag();

                                        db.collection("points")
                                                .document(p.getDocID())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("DELETE POINT", "DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("DELETE POINT", "Error deleting document", e);
                                                    }
                                                });

                                        getUserPoints();
                                    }
                                });
                                llHorizontal.addView(button);

                                llForPoints.addView(llHorizontal, params);
                            }
                        } else {
                            Log.w("GET ALL POINTS", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            finish();
        } else if (v == btnShowOnMap) {
            if (checkedPoints.isEmpty()) {
                Toast.makeText(PointsActivity.this, R.string.choose_points, Toast.LENGTH_LONG).show();
            } else {
                setResult(Activity.RESULT_OK, new Intent()
                        .putParcelableArrayListExtra("pointsList", checkedPoints));
                finish();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.i("ON DISMISS", "on dismiss");
        getUserPoints();
    }
}