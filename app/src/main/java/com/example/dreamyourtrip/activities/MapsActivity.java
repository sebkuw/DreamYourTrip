package com.example.dreamyourtrip.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.dreamyourtrip.R;
import com.example.dreamyourtrip.fragments.AddPointDialogFragment;
import com.example.dreamyourtrip.fragments.AddTripStopDialogFragment;
import com.example.dreamyourtrip.models.Point;
import com.example.dreamyourtrip.models.TaskLoadedCallback;
import com.example.dreamyourtrip.models.Trip;
import com.example.dreamyourtrip.models.TripStop;
import com.example.dreamyourtrip.models.parsers.FetchURL;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        View.OnClickListener,
        GoogleMap.OnMarkerClickListener,
        TaskLoadedCallback,
        NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private GoogleMap mMap;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageButton imgBtnCurrentPosition, imgBtnSavePoint, imgBtnAddToTrip;

    private FusedLocationProviderClient fusedLocationClient;

    private double longitude, latitude;
    private String markerTitle;
    private boolean isAnyTrip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        requestLocationPermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initMap();
        initPlaces();
        initViews();
        initMenu();
        initDB();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initPlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), this.getString(R.string.google_maps_api_key));
        }

        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                markerTitle = place.getName();

                moveMap();

                Log.i("PLACE SELECTED", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                Log.i("PLACE SELECTED", "An error occurred: " + status);
            }
        });
    }

    private void initViews() {
        imgBtnCurrentPosition = findViewById(R.id.img_btn_current_position);
        imgBtnSavePoint = findViewById(R.id.img_btn_save_point);
        imgBtnAddToTrip = findViewById(R.id.img_btn_add_to_trip);

        imgBtnCurrentPosition.setOnClickListener(this);
        imgBtnSavePoint.setOnClickListener(this);
        imgBtnAddToTrip.setOnClickListener(this);
    }

    private void initMenu() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.dl_activity_maps);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initDB() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer((GravityCompat.START));
        else
            super.onBackPressed();
    }

    ActivityResultLauncher<Intent> pointsActivityStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        ArrayList<Point> pointsChecked = intent.getParcelableArrayListExtra("pointsList");

                        if (pointsChecked.size() > 0) {
                            mMap.clear();
                            for (Point p : pointsChecked) {
                                LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .draggable(true)
                                        .title(p.getName()));
                            }
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> tripsActivityStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        mMap.clear();
                        Trip t = intent.getParcelableExtra("trip");

                        ArrayList<TripStop> sourcePoints = t.getTripStops();
                        Log.i("CHECK", sourcePoints.toString());

                        String directionMode;
                        if ("BY BIKE".equals(t.getTransportType())) {
                            directionMode = "cycling";
                        } else if ("ON FOOT".equals(t.getTransportType())) {
                            directionMode = "walking";
                        } else {
                            directionMode = "driving";
                        }

                        for (int i = 0; i < sourcePoints.size(); i++) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(sourcePoints.get(i).getLatitude(), sourcePoints.get(i).getLongitude()))
                                    .draggable(true))
                                    .setTitle(sourcePoints.get(i).getName());

                            if (i != sourcePoints.size() - 1) {
                                new FetchURL(MapsActivity.this)
                                        .execute(getUrl(
                                                new LatLng(sourcePoints.get(i).getLatitude(), sourcePoints.get(i).getLongitude()),
                                                new LatLng(sourcePoints.get(i + 1).getLatitude(), sourcePoints.get(i + 1).getLongitude()),
                                                directionMode), directionMode);
                            } else {
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(sourcePoints.get(0).getLatitude(), sourcePoints.get(0).getLongitude())));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            }
                        }
                    }
                }
            });

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.nav_map):
                drawerLayout.closeDrawer((GravityCompat.START));
                break;
            case (R.id.nav_points):
                pointsActivityStartForResult.launch(new Intent(this, PointsActivity.class));
                drawerLayout.closeDrawer((GravityCompat.START));
                break;
            case (R.id.nav_trips):
                tripsActivityStartForResult.launch(new Intent(this, PointsActivity.class));
                drawerLayout.closeDrawer((GravityCompat.START));
                break;
            case (R.id.nav_sign_out):
                mAuth.signOut();
                finish();
                break;
            default:
                return true;
        }
        return true;
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters + "&key=" + getResources().getString(R.string.google_maps_api_key);
        Log.i("URL", url);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getCurrentLocation();

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            moveMap();
                        }
                    }
                });
    }

    private void moveMap() {
        mMap.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(markerTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        markerTitle = "";
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));

        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        moveMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == imgBtnCurrentPosition) {
            getCurrentLocation();
        } else if (v == imgBtnSavePoint) {
            AddPointDialogFragment addPointDialogFragment = AddPointDialogFragment.newInstance("Add point", longitude, latitude);
            addPointDialogFragment.show(getSupportFragmentManager(), "fragment_add_point");
        } else if (v == imgBtnAddToTrip) {
            db.collection("trips")
                    .whereEqualTo("ownerID", currentUser.getUid())
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("CHECK TRIPS", document.getId() + " => " + document.getData());
                                    Trip t = document.toObject(Trip.class);
                                    isAnyTrip = true;
                                }
                            } else {
                                Log.w("CHECK TRIPS", "Error getting documents.", task.getException());
                            }
                        }
                    });

            if (isAnyTrip) {
                AddTripStopDialogFragment addTripStopDialogFragment = AddTripStopDialogFragment.newInstance("Add trip stop", latitude, longitude);
                addTripStopDialogFragment.show(getSupportFragmentManager(), "fragment_add_trip_stop");
            } else {
                Toast.makeText(this, "First add trip and then add points to it", Toast.LENGTH_LONG).show();
            }
        }
    }

    //CHECKING PERMISSIONS
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }
}