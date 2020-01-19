package com.hassan.nearbyatm.FindATMs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.hassan.nearbyatm.R;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, SearchView, ATMsRecyclerAdapter.OnATMClickedListener {

    // searching radius in meters. to convert it to kelometers divide by 1000 ,now its (10 KM)
    private static int SEARCHING_RADIUS = 8000;
    private static final int MIN_SEARCHING_RADIUS = 2000;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private GoogleMap mMap;
    LocationManager locationManager;
    SearchInteractor interactor = new RxSearchInteractor();//SearchInteractorImpl();
    SearchPresenter presenter;
    private ProgressDialog progressDialog;
    private BottomSheetBehavior bottomSheetBehavior;
    private Location myLocation;
    private ATMsRecyclerAdapter adapter;
    private List<ATM> list = new ArrayList<>();
    private Polyline currentPolyline;
    private List<Marker> markers = new ArrayList<>();
    private Circle circle;
    private TextView txtSearchingIn;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.add("Exit");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new AlertDialog.Builder(MapsActivity.this)
                        .setCancelable(true)
                        .setMessage("Exit Application")
                        .setNegativeButton("No",null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .show();
                return false;
            }
        });
        item.setIcon(R.drawable.ic_exit);
        item.setTitle("Exit Application");
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Find Nearby ATMs");
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new ATMsRecyclerAdapter(this, list);
        adapter.setOnATMClickListener(this);
        txtSearchingIn = findViewById(R.id.txt_searching_in);
        AppCompatSeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress()*1000 < (MIN_SEARCHING_RADIUS/1000)) {
                    seekBar.setProgress(MIN_SEARCHING_RADIUS / 1000);
                    SEARCHING_RADIUS = MIN_SEARCHING_RADIUS;
                    if (circle != null) {
                        circle.setRadius(SEARCHING_RADIUS);
                    }
                }else{
                    SEARCHING_RADIUS = i * 1000;
                    if (circle != null) {
                        circle.setRadius(i * 1000);
                    }
                }
                txtSearchingIn.setText(String.format("%s Kelometers", String.valueOf(SEARCHING_RADIUS / 1000)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        txtSearchingIn.setText(String.format("%s Kelometers", String.valueOf(SEARCHING_RADIUS / 1000)));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Button btn = findViewById(R.id.btn_find_atms);
        LinearLayout linearLayout = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        btn.setOnClickListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        presenter = new SearchPresenterImpl(this, interactor);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng khartoum = new LatLng(15.504578, 32.585471);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(khartoum, 11));
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
            return;
        }
        if (!isGpsEnabled()) {
            showGpsNotEnabledMessage();
            return;
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ATM atm = (ATM) marker.getTag();
                if (atm != null) {
                    onATMClick(atm);
                }
                return false;
            }
        });
        showMyLocation();
    }

    private void showMyLocation() {
        findATMs();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            findATMs();
        } else {
            Toast.makeText(this, "Sorry, i can't find an ATM because you refused to give me the permission", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void findATMs() {
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
            return;
        }
        if (!isGpsEnabled()) {
            showGpsNotEnabledMessage();
            return;
        }
        myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.removeUpdates(this);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions()
                            .title("My Location")
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_icon)));
                    circle = mMap.addCircle(new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude())).strokeColor(Color.RED).radius(getSearchingRadius()).strokeWidth(1));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));
                    findATMs();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {
                    findATMs();
                }

                @Override
                public void onProviderDisabled(String s) {
                    showGpsNotEnabledMessage();
                }
            });
        } else {
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .title("My Location")
                    .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_icon)));
            circle = mMap.addCircle(new CircleOptions().center(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).strokeColor(Color.RED).radius(getSearchingRadius()).strokeWidth(1));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 12), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    presenter.search(myLocation, SEARCHING_RADIUS);
                }

                @Override
                public void onCancel() {
                    presenter.search(myLocation, SEARCHING_RADIUS);
                }
            });
        }
    }

    private double getSearchingRadius() {
        return SEARCHING_RADIUS;
    }

    private void showGpsNotEnabledMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS Not Enabled");
        builder.setMessage("GPS Not Enabled , Please enable location services to be able to use the application");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(true);
        builder.show();
    }

    private boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onClick(View view) {
        findATMs();
    }

    @Override
    public void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Searching for nearby ATMs ...");
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showList(List<ATM> atms) {
        markers.clear();
        IconGenerator iconGenerator = new IconGenerator(this);
        for (ATM atm : atms) {
            if (atm.isOnline()){
                iconGenerator.setTextAppearance(R.style.online_text_appearance);
            }else{
                iconGenerator.setTextAppearance(R.style.offline_text_appearance);
            }

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(atm.getLocation().getLatitude(), atm.getLocation().getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(atm.getName())))
                    .anchor(iconGenerator.getAnchorU(), iconGenerator.getAnchorV()));
            marker.setTag(atm);
            markers.add(marker);
        }
        list.clear();
        list.addAll(atms);
        adapter.notifyDataSetChanged();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void showError(String err) {
        Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
    }

    private void showFetchingRouteLoading(String atm) {
        progressDialog.setMessage(atm);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideFetchingRouteLoading() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onATMClick(final ATM atm) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(atm.getLocation().getLatitude(), atm.getLocation().getLongitude()), 13), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                drawRoute(atm);
            }

            private void drawRoute(ATM atm) {
                showFetchingRouteLoading("fetching route to " + atm.getName());
                GoogleDirection.withServerKey("AIzaSyDwNBXmHBDQ29JWsRH8gwNVkf7mM0-flaI")
                        .from(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .to(new LatLng(atm.getLocation().getLatitude(), atm.getLocation().getLongitude()))
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                hideFetchingRouteLoading();
                                if (direction.isOK()) {
                                    Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                                    ArrayList<LatLng> latLngs = leg.getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getBaseContext(), latLngs, 5, getResources().getColor(R.color.colorPrimary));
                                    if (currentPolyline != null) {
                                        currentPolyline.remove();
                                    }
                                    currentPolyline = mMap.addPolyline(polylineOptions);
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                hideFetchingRouteLoading();
                                showError(t.getMessage());
                            }
                        });
            }

            @Override
            public void onCancel() {
                drawRoute(atm);
            }
        });
    }
}