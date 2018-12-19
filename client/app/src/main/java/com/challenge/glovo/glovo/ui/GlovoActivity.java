package com.challenge.glovo.glovo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.challenge.glovo.glovo.AppDataManager;
import com.challenge.glovo.glovo.BuildConfig;
import com.challenge.glovo.glovo.MvpApp;
import com.challenge.glovo.glovo.R;
import com.challenge.glovo.glovo.base.BaseActivity;
import com.challenge.glovo.glovo.network.models.City;
import com.challenge.glovo.glovo.network.models.Country;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GlovoActivity extends BaseActivity implements GlovoMvpView,
        OnMapReadyCallback
        , GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {
    GlovoPresenter presenter;
    GoogleMap mMap;
    List<City> cityList = new ArrayList<>();
    List<City> cities = new ArrayList<>();
    List<Country> countryList = new ArrayList<>();
    ArrayAdapter<Country> countryAdapter;
    ArrayAdapter<City> cityAdapter;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000000000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates = true;
    private String countryCode;
    private int cityPosition;
    @BindView(R.id.spinnerCountries)
    Spinner spinnerCountries;
    @BindView(R.id.spinnerCities)
    Spinner spinnerCities;
    BottomSheetBehavior bottomSheetBehavior;
    @BindView(R.id.bottom_sheet)
    ConstraintLayout bottom_sheet;
    @BindView(R.id.city_name_value)
    TextView city_name_value;
    @BindView(R.id.city_code_value)
    TextView city_code_value;
    @BindView(R.id.country_code_value)
    TextView country_code_value;
    List<Marker> hashMapMarker = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AppDataManager dataManager = ((MvpApp) getApplication()).getDataManager();
        presenter = new GlovoPresenter<>(dataManager);
        presenter.onAttach(this);
        presenter.getData();

        //setting views of the app
        setViews();
    }

    // implementing on camera idle

    @Override
    public void onCameraIdle() {
        if (mMap.getCameraPosition().zoom >= 10) {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses;
            try {
                Double lng = bounds.getCenter().longitude;
                Double lat = bounds.getCenter().latitude;
                addresses = geocoder.getFromLocation(lng, lat, 1);
                if (addresses != null && addresses.size() != 0) {
                    Address address = addresses.get(0);
                    String code = address.getCountryCode();
                    List<City> cityIterate = new ArrayList<>();
                    for (int i = 0; i < cityList.size(); i++) {
                        if (cityList.get(i).getCountryCode().equals(code))
                            cityIterate.add(cityList.get(i));
                    }
                    drawIteration(bounds, cityIterate);
                } else {
                    drawIteration(bounds, cityList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            addCityMarkers();
        }
    }

    //calling draw iteration from on camera idle
    private void drawIteration(LatLngBounds bounds, List<City> cityIteration) {
        for (int z = 0; z < cityIteration.size(); z++) {
            City city = cityIteration.get(z);
            if (!city.getVisible()) {
                List<String> areas = new ArrayList<>(city.getWorkingArea());
                drawAreas(bounds, city, areas);
                getData(z);
            } else
                getData(z);
        }
    }

// calling draw areas from draw iteration
    private void drawAreas(LatLngBounds bounds, City city, List<String> areas) {
        for (int i = 0; i < areas.size(); i++) {
            List<LatLng> allPoints = new ArrayList<>(PolyUtil.decode(areas.get(i)));
            for (int z = 0; z < allPoints.size(); z++) {
                LatLng latLng = allPoints.get(z);
                if (bounds.contains(latLng)) {

                    addPolygons(allPoints);
                    break;
                }
            }
            if (i == allPoints.size() - 1)
                city.setVisible(true);
        }
    }


    // add city markers at certain zoom level
    private void addCityMarkers() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        for (int i = 0; i < cityList.size(); i++) {
            City city = cityList.get(i);
            List<String> areas = new ArrayList<>(city.getWorkingArea());
            List<LatLng> allPoints = new ArrayList<>(PolyUtil.decode(areas.get(areas.size() - 1)));
            for (int z = 0; z < allPoints.size(); z++) {
                LatLng latLng = allPoints.get(z);
                if (bounds.contains(latLng)) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    hashMapMarker.add(marker);
                    break;
                }
            }
        }

    }

    // on map ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);

    }


    // setting views
    @Override
    public void setViews() {
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.map, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        spinners();
    }

    // setting spinner for country and city if current location not equivalent to any city
    private void spinners() {
        countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countryList);
        spinnerCountries.setAdapter(countryAdapter);
        spinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countryCode = countryList.get(position).getCode();
                clearCities();
                countrySelection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCities.setAdapter(cityAdapter);

        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cityPosition = position;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

    }

    //submit button for choosing city
    @OnClick(R.id.submit)
    public void chooseCity(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        getCity(cityPosition);
    }

    //getting city based on the position used by the user
    private void getCity(int position) {
        City city = cities.get(position);

        List<String> areas = new ArrayList<>(city.getWorkingArea());
        LatLng animateValue = null;
        for (int i = 0; i < areas.size(); i++) {
            List<LatLng> allPoints = new ArrayList<>(PolyUtil.decode(areas.get(i)));
            addPolygons(allPoints);
            if (i == areas.size() - 1) {
                city.setVisible(true);
                animateValue = allPoints.get(0);
            }
        }

        animateMap(animateValue);

    }

    // country selection for the spinner country
    private void countrySelection() {
        for (int i = 0; i < cityList.size(); i++) {
            if (cityList.get(i).getCountryCode().equals(countryCode)) {
                cities.add(cityList.get(i));
            }
        }

        cityAdapter.notifyDataSetChanged();
        spinnerCities.setSelection(0);
    }

    // clear cities from the spinner
    private void clearCities() {
        cities.clear();
        cityAdapter.notifyDataSetChanged();
    }

    @Override
    public void setCountries(List<Country> countries) {
        countryList.addAll(countries);
        countryAdapter.notifyDataSetChanged();
        spinnerCountries.setSelection(0);
    }

    // adding cities retrieved from the api
    @Override
    public void setCities(List<City> cities) {
        cityList.addAll(cities);
    }


// animate map with given latlng
    private void animateMap(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
    }

    //drawing polygon on background thread
    private void addPolygons(List<LatLng> points) {
        if (points.size() != 0)
            new MapDrawing().execute(points);
    }

    //marker click listener
    @Override
    public boolean onMarkerClick(Marker marker) {
        animateMap(marker.getPosition());
        return true;
    }

    // drawing polygon
    @SuppressLint("StaticFieldLeak")
    private class MapDrawing extends AsyncTask<List<LatLng>, Void, PolygonOptions> {

        @SafeVarargs
        @Override
        protected final PolygonOptions doInBackground(List<LatLng>... params) {
            List<LatLng> latLngs = params[0];
            return new PolygonOptions()
                    .clickable(true)
                    .fillColor(getResources().getColor(R.color.polygoncolor))
                    .addAll(latLngs);
        }

        @Override
        protected void onPostExecute(PolygonOptions result) {
            mMap.addPolygon(result);

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    // showing bottom sheet for chossing city
    private void notGrantedPermission() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    // check  latlng of the user
    private void checkCoordination(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            Double lng = latLng.longitude;
            Double lat = latLng.latitude;
            addresses = geocoder.getFromLocation(lng, lat, 1);
            if (addresses != null && addresses.size() != 0) {
                Address address = addresses.get(0);
                String code = address.getCountryCode();
                List<City> iterateCity = new ArrayList<>();
                for (int i = 0; i < cityList.size(); i++) {
                    if (cityList.get(i).getCountryCode().equals(code))
                        iterateCity.add(cityList.get(i));
                }
                iterateCities(latLng, iterateCity);
            } else {
                iterateCities(latLng, cityList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //check whether user is in any given city
    private void iterateCities(LatLng latLng, List<City> cityIterate) {
        Boolean check = false;
        int index = 0;
        outerLoop:
        for (int i = 0; i < cityIterate.size(); i++) {
            City city = cityIterate.get(i);
            List<String> areas = new ArrayList<>(city.getWorkingArea());
            for (String area : areas) {
                List<LatLng> allPoints = new ArrayList<>(PolyUtil.decode(area));
                if (allPoints.contains(latLng)) {
                    LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    if (bounds.contains(latLng)) {
                        check = true;
                        index = i;
                        break outerLoop;
                    }
                }

            }
        }

        if (check) {
            getData(index);
        } else {
            notGrantedPermission();
        }
    }

    //get data for the city panel
    private void getData(int position) {
        city_name_value.setText(cityList.get(position).getName());
        city_code_value.setText(cityList.get(position).getCode());
        country_code_value.setText(cityList.get(position).getCountryCode());
    }

    //location call back
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {

            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            animateMap(latLng);

            checkCoordination(latLng);
        }

        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(task -> mRequestingLocationUpdates = false);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (mRequestingLocationUpdates) {
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(this, locationSettingsResponse -> {
                        mRequestingLocationUpdates = true;
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                    })
                    .addOnFailureListener(this, e -> {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(GlovoActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                mRequestingLocationUpdates = false;
                        }

                    });
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, view -> {
                        ActivityCompat.requestPermissions(GlovoActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    });
        } else {
            ActivityCompat.requestPermissions(GlovoActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length <= 0) {
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRequestingLocationUpdates = true;
                    startLocationUpdates();
                } else {

                    showSnackbar(R.string.permission_denied_explanation,
                            R.string.action_settings, view -> {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
                    notGrantedPermission();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case AppCompatActivity.RESULT_OK:
                        mRequestingLocationUpdates = true;
                        break;
                    case AppCompatActivity.RESULT_CANCELED:
                        mRequestingLocationUpdates = false;
                        notGrantedPermission();
                        break;

                }
                break;

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }
    }

}
