package com.siproapps.mapstutorial;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener, DialogInterface.OnClickListener {

    private GoogleMap mGoogleMap;
    private LinearLayout lLayout1, lLayout2;
    private EditText editTextLatitude, editTextLongitude;
    CheckBox chkBoxCustomMarker;
    private ArrayList<Marker> markers = new ArrayList<Marker>();   // add marker instances on map in a list
    private final int ZOOM_VALUE = 12;  // map zoom level
    private final int PERMISSIONS_REQ_CODE = 101;  // permissions-check request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        lLayout1 = (LinearLayout) findViewById(R.id.lLayout1);
        lLayout2 = (LinearLayout) findViewById(R.id.lLayout2);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frameLayout, mapFragment).commit();

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnCameraChangeListener(this);
        mGoogleMap = googleMap;
    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        addMarkerAndMoveToLocation(latLng, getString(R.string.current_location), false);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void showLatLngDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        View latLngView = getLayoutInflater().inflate(R.layout.lat_lng_layout, null);
        alertDialogBuilder.setView(latLngView);
        alertDialogBuilder.setPositiveButton("OK", this);
        alertDialogBuilder.setNegativeButton("Cancel", this);
        alertDialogBuilder.show();

        editTextLatitude = (EditText) latLngView.findViewById(R.id.editTextLatitude);
        editTextLongitude = (EditText) latLngView.findViewById(R.id.editTextLongitude);
        chkBoxCustomMarker = (CheckBox) latLngView.findViewById(R.id.chkBoxCustomMarker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fullMap:
                lLayout1.setVisibility(View.GONE);
                lLayout2.setVisibility(View.GONE);
                return true;
            case R.id.windowMap:
                lLayout1.setVisibility(View.VISIBLE);
                lLayout2.setVisibility(View.VISIBLE);
                return true;
            case R.id.addMarker:
                showLatLngDialog(); //see addMarkerAndMoveToLocation(latLng, title, isCustomIcon)
                return true;
            case R.id.removeMarkers:
                removeMarkers();
                return true;
            case R.id.removeRecentMarker:
                removeRecentMarker();
                return true;
            case R.id.toggleRecentMarkerVisibility:
                toggleRecentMarkerVisibility();
                return true;
            case R.id.showCurrentLocation:
                // permissions check for last location and location updates
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(MapActivity.this, permissions, PERMISSIONS_REQ_CODE);
                    return true;
                } else {
                    showCurrentLocation();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == -1) {
            boolean isCustomIcon = chkBoxCustomMarker.isChecked();
            String latitude = editTextLatitude.getText().toString();
            String longitude = editTextLongitude.getText().toString();
            double lat, lng;

            if(latitude.isEmpty()) lat = 0.0;
            else lat = Double.parseDouble(latitude);

            if(longitude.isEmpty()) lng = 0.0;
            else lng = Double.parseDouble(longitude);

            LatLng latLng = new LatLng(lat, lng);

            addMarkerAndMoveToLocation(latLng, getString(R.string.our_example_marker), isCustomIcon);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_REQ_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(MapActivity.this, permissions, PERMISSIONS_REQ_CODE);
            }
        }
    }

    @SuppressWarnings("ResourceType")
    private void showCurrentLocation() {
        mGoogleMap.clear(); // clear existing markers
        mGoogleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(bestProvider, 5000, 10, this);  // starts requesting device's location updates

        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location == null) {
            Toast.makeText(this, R.string.no_gps, Toast.LENGTH_SHORT).show();
        } else {
            onLocationChanged(location);
            Toast.makeText(this, R.string.current_location, Toast.LENGTH_SHORT).show();
        }

    }

    private void addMarkerAndMoveToLocation(LatLng latLng, String title, boolean isCustomIcon) {

        // 1. add marker
        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(getString(R.string.long_press_to_drag)).draggable(true));
        //marker.showInfoWindow();  //marker.hideInfoWindow();

        // 2. move to location
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // 3. zoom
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_VALUE));
        // alternate:
        // mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_VALUE));

        ///marker.setAnchor(0.5f, 0.5f);    // point on the image that will be placed at the LatLng position of the marker.

        if(isCustomIcon) marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.home_marker));

        if(markers ==  null) markers = new ArrayList<>();
        markers.add(marker);

        Toast.makeText(this, "Marker added at lat: "+latLng.latitude+" & lng: "+latLng.longitude, Toast.LENGTH_SHORT).show();
    }

    private void toggleRecentMarkerVisibility() {
        if(markers == null || markers.isEmpty()) {
            Toast.makeText(this, R.string.no_marker_added, Toast.LENGTH_SHORT).show();
            return;
        }
        Marker marker = markers.get(markers.size()-1);
        if(marker.isVisible()) marker.setVisible(false);
        else marker.setVisible(true);
    }

    private void removeRecentMarker() {
        if(markers == null || markers.isEmpty()) {
            Toast.makeText(this, R.string.no_marker_added, Toast.LENGTH_SHORT).show();
            return;
        }
        Marker marker = markers.get(markers.size()-1);

        marker.remove();    // remove marker from map

        markers.remove(markers.size()-1);
    }

    private void removeMarkers() {
        mGoogleMap.clear(); // remove all markers on map

        if(markers == null || markers.isEmpty()) {
            Toast.makeText(this, R.string.no_marker_added, Toast.LENGTH_SHORT).show();
            return;
        }
        markers.clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, R.string.marker_clicked, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Toast.makeText(this, R.string.marker_dragged, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // when Camera is animated or moved.
    }
}
