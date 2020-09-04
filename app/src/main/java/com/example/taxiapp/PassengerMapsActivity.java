package com.example.taxiapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PassengerMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int CHECK_SETTINGS = 654;
    private static final int REQUEST_LOCATION_PERMISSION = 215;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private Button settingsBtn, signOutBtn, bookTaxiBtn;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private int searchRadius = 1;
    private boolean isDriverFound = false;
    private String nearestDriverId;

    GeoFire geoFirePassengersStorage, geoFireDriverStorage;

    DatabaseReference passengerInfoStorage, passengersLocationStorage, driversLocationStorage;

    private Marker currentUserMarker, taxiMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_maps);

        initStorage();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        settingsBtn = findViewById(R.id.driverSettingsBtn);
        signOutBtn = findViewById(R.id.driverSignOutBtn);
        bookTaxiBtn = findViewById(R.id.bookTaxiBtn);

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutDriver();
            }
        });

        bookTaxiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookTaxiBtn.setText("Getting Taxi...");
                gettingNearestTaxi();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        buildLocationRequest();
        buildLocationCallback();
        buildLocationSettingsRequest();

        startLocationUpdates();
    }

    private void initStorage() {
        passengerInfoStorage = FirebaseDatabase.getInstance().getReference().child("passengersInfo");
        passengerInfoStorage.setValue(true);

        passengersLocationStorage = FirebaseDatabase.getInstance().getReference().child("passengersGeoFire");
        driversLocationStorage = FirebaseDatabase.getInstance().getReference().child("driversGeoFire");

        geoFirePassengersStorage = new GeoFire(passengersLocationStorage);
        geoFireDriverStorage = new GeoFire(driversLocationStorage);
    }

    private void gettingNearestTaxi() {
        GeoQuery geoQuery = geoFireDriverStorage.queryAtLocation(
                new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()),
                searchRadius
        );

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (isDriverFound) {
                    return;
                }

                isDriverFound = true;
                nearestDriverId = key;

                gettingNearestTaxiLocation();
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (isDriverFound) {
                    return;
                }

                searchRadius++;
                gettingNearestTaxi();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void gettingNearestTaxiLocation() {
        bookTaxiBtn.setText("Getting Taxi Location...");

        driversLocationStorage.child(nearestDriverId).child("l").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                List<Object> taxiLocationVal = (List<Object>) snapshot.getValue();

                double latitude = 0;
                double longitude = 0;

                if (taxiLocationVal.get(0) != null && taxiLocationVal.get(1) != null) {
                    latitude = Double.parseDouble(taxiLocationVal.get(0).toString());
                    longitude = Double.parseDouble(taxiLocationVal.get(1).toString());
                }

                LatLng taxiLatLngLocation = new LatLng(latitude, longitude);
                Location taxiLocation = new Location("");
                taxiLocation.setLatitude(latitude);
                taxiLocation.setLongitude(longitude);

                float distance = currentLocation.distanceTo(taxiLocation);

                bookTaxiBtn.setText("Distance to Taxi: " + distance);

                if (taxiMarker != null) {
                    taxiMarker.remove();
                }

                taxiMarker = mMap.addMarker(new MarkerOptions().position(taxiLatLngLocation).title("Your Taxi"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signOutDriver() {
        geoFirePassengersStorage.removeLocation(currentUser.getUid());

        auth.signOut();

        Intent intent = new Intent(PassengerMapsActivity.this, ChoseModeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                updateLocationUi(currentLocation);
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        locationSettingsRequest = builder.build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        switch (requestCode) {
            case CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d("PassengerMapsActivity", "User has agreed toi change location settings");
                        startLocationUpdates();
                        break;

                    case Activity.RESULT_CANCELED:
                        Log.d("PassengerMapsActivity", "User has NOT agreed toi change location settings");
                        updateLocationUi(currentLocation);
                        break;

                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkLocationPermissions()) {
            startLocationUpdates();
            return;
        }

        requestLocationPermissions();
    }

    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void startLocationUpdates() {

        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {

            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ActivityCompat.checkSelfPermission(PassengerMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(PassengerMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                updateLocationUi(currentLocation);
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();

                switch (statusCode) {
                    case LocationSettingsStatusCodes
                            .RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(
                                    PassengerMapsActivity.this,
                                    CHECK_SETTINGS
                            );
                        } catch (IntentSender.SendIntentException sie) {
                            sie.printStackTrace();
                        }
                        break;

                    case LocationSettingsStatusCodes
                            .SETTINGS_CHANGE_UNAVAILABLE:
                        String msg = "Adjust location settings on device";
                        Toast.makeText(PassengerMapsActivity.this, msg, Toast.LENGTH_LONG).show();
                        updateLocationUi(currentLocation);
                }
            }
        });
    }

    private void requestLocationPermissions() {
        boolean needDsc = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (needDsc) {
            showSnackBar("Location permissions need set", "OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(PassengerMapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                }
            });

            return;
        }

        ActivityCompat.requestPermissions(
                PassengerMapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }


    private void updateLocationUi(Location currentLocation) {
        if (currentLocation == null) {
            return;
        }

        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        if (currentUserMarker != null) {
            currentUserMarker.remove();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        currentUserMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Your location"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        geoFirePassengersStorage.setLocation(currentUser.getUid(),
                new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE)
                .setAction(action, listener)
                .show();
    }
}