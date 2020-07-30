/*Google Map API Class
 * */
/*
Displays a Google Map which is centered on the users current location on start. Pegs are placed on
map in locations of restaurants received from a manager instance. These pegs have different icons
and colors based off the restaurants' hazard ratings (low, medium, high). If to many pegs are
clustered in an area of the screen, a marker will replace the pegs, with a number showing how many
restaurants are in the area. The user's current location is on the map with a pulsing blue icon.
The icon will move corresponding to the user's location changing. If a restaurant peg is selected,
an info window will show the restaurant name, address, and most recent hazard rating. If the info
window is selected by the user, they are taken to the SingleRestaurantActivity with details of that
restaurant. A list icon is displayed on the map that allows users to go to RestaurantListActivity.
 */
package ca.sfu.cmpt_276_project.UI;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.DBAdapter;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.PegItem;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String EXTRA_NAME = "ca.sfu.cmpt_276_project.UI.extraName";
    private static final String EXTRA_LAT = "ca.sfu.cmpt_276_project.UI.extraLat";
    private static final String EXTRA_LNG = "ca.sfu.cmpt_276_project.UI.extraLng";
    private static final String EXTRA_BOOL = "ca.sfu.cmpt_276_project.UI.extraBool";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 18f;
    static MapsActivity instance;
    LocationRequest locationRequest;
    private String restaurantName;
    private double restaurantLat;
    private double restaurantLng;
    private Boolean fromRestaurant;
    private GoogleMap mMap;
    private Marker mMarker;
    private UiSettings mUiSettings;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private RestaurantManager restaurantManager;
    private int[] restaurantIcons;
    private Location currentLocation;
    private ClusterManager<PegItem> mClusterManager;
    private DBAdapter dbAdapter;

    public static Intent makeIntent(Context context, String name,
                                    Double latitude, double longitude,
                                    Boolean fromRestaurant) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_LAT, latitude);
        intent.putExtra(EXTRA_LNG, longitude);
        intent.putExtra(EXTRA_BOOL, fromRestaurant);
        return intent;
    }

    /**
     * DATABASE FUNCTIONS
     */
    private void openDB(){
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
    }

    private void closeDB(){
        dbAdapter.close();
    }
    private void addRestaurantsToDB(){
        //TODO: Look over the methods outlined, understand what they do
        Gson gson = new Gson();//necessary to convert Array list

        for(Restaurant restaurant: restaurantManager.getRestaurants()){

            String inspectionJSON = gson.toJson(restaurant.getInspectionDataList());
            /*PRINTER TO COMPARE INSPECTION ARRAY LIST SIZE
            System.out.println("\tInitial InspectionJSON: " + inspectionJSON + "\n");
            if(!restaurant.getInspectionDataList().isEmpty())
                System.out.println("\tInitial Inspection SIZE: " + restaurant.getInspectionDataList().size() + "\n");*/

            //THIS PROCESS ADDS ITEM TO THE DM
            long newID = dbAdapter.insertRow(restaurant.getTrackNumber(),
                    restaurant.getRestaurantName(), restaurant.getPhysicalAddress(),
                    restaurant.getPhysicalCity(), restaurant.getFacType(),
                    restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getIcon(),
                    inspectionJSON);

            //THIS CURSOR IS USED TO SCAN THE DB
            Cursor cursor = dbAdapter.getRow(newID);

            //THIS PAIR OF LINES ARE USED TO DESERIALIZE THE JSON STRING EXTRACTED FROM DB
            Type type = new TypeToken<ArrayList<InspectionData>>() {}.getType();
            List<InspectionData> tempList = gson.fromJson(cursor.getString(DBAdapter.COL_INSPECTION), type);

            //Printer test to check injection
            System.out.println("Injected: \n"
                    + "\tDB-ID#: " + cursor.getInt(DBAdapter.COL_ROWID) + "\n"
                    + "\tTrack#: " + cursor.getString(DBAdapter.COL_TRACK_NUM) + "\n"
                    + "\tName: " + cursor.getString(DBAdapter.COL_RES_NAME) + "\n"
                    + "\tAddr: " + cursor.getString(DBAdapter.COL_ADDRESS) + "\n"
                    + "\tCity: " + cursor.getString(DBAdapter.COL_CITY) + "\n"
                    + "\tFacType: " + cursor.getString(DBAdapter.COL_FAC_TYPE) + "\n"
                    + "\tLatitude: " + cursor.getDouble(DBAdapter.COL_LATITUDE) + "\n"
                    + "\tLongitude: " + cursor.getDouble(DBAdapter.COL_LONGITUDE) + "\n");
            if(!tempList.isEmpty()) {
                System.out.println("\tInspection Details: ");
                for(InspectionData inspectionData: tempList)
                    inspectionData.Display();
            }

            //CLOSE CURSOR TO AVOID RESOURCE LEAKS
            cursor.close();
        }
    }

    public void clearDB() {
        System.out.println("Wiped DB clean");
        dbAdapter.deleteAll();
    }
    /**
     * ENDOF DATABASE FUNCTIONS
     */


    public static MapsActivity getInstance() {
        return instance;
    }

    // allows MapsActivity to be accessed
    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        return intent;
    }

    private void init() {
        ImageButton btnMain = (ImageButton) findViewById(R.id.btnMain);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = RestaurantListActivity.makeIntent(MapsActivity.this);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocationPermission();

        instance = this;

        restaurantManager = RestaurantManager.getInstance();

        init();

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        //opening database
        openDB();
        addRestaurantsToDB();//Instatiating DB data
        clearDB();//Clearing data instantly, cause I have no use for it

    }

    private void updateLocation() {
        buildLocationRequest();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());

    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(15f);
    }

    public void updateLocation(Location newLocation) {
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fromRestaurant) {
                    mClusterManager.clearItems();

                    MarkerOptions options = new MarkerOptions().title(restaurantName).
                            position(new LatLng(restaurantLat, restaurantLng));

                    mMarker = mMap.addMarker(options);
                    mMarker.showInfoWindow();
                    moveCamera(new LatLng(restaurantLat, restaurantLng), DEFAULT_ZOOM);
                } else {
                    moveCamera(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()), DEFAULT_ZOOM);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.finishAffinity();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
        closeDB();
    }


    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            if (location != null) {
                                currentLocation = (Location) task.getResult();

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM);
                            }
                        } else {
                            Toast.makeText(MapsActivity.this,
                                    "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    // move the camera
    private void moveCamera(LatLng latLng, float zoom) {

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(location);

        //Set Custom InfoWindow Adapter
        CustomInfoAdapter adapter = new CustomInfoAdapter(MapsActivity.this);
        mMap.setInfoWindowAdapter(adapter);
    }

    // set Map UI
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();

        mClusterManager = new ClusterManager<>(this, mMap);

        mMap.getUiSettings().setZoomGesturesEnabled(true);

        setUpCluster();
        //Set Custom InfoWindow Adapter
        CustomInfoAdapter adapter = new CustomInfoAdapter(MapsActivity.this);
        mMap.setInfoWindowAdapter(adapter);

        registerClickCallback();

        Intent intent = getIntent();
        restaurantName = intent.getStringExtra(EXTRA_NAME);
        restaurantLat = intent.getDoubleExtra(EXTRA_LAT, 49.1915);
        restaurantLng = intent.getDoubleExtra(EXTRA_LNG, 122.8456);
        fromRestaurant = intent.getBooleanExtra(EXTRA_BOOL, false);
        if (fromRestaurant == true) {
            mClusterManager.clearItems();

            MarkerOptions options = new MarkerOptions().title(restaurantName).
                    position(new LatLng(restaurantLat, restaurantLng));

            mMarker = mMap.addMarker(options);
            mMarker.showInfoWindow();
            moveCamera(new LatLng(restaurantLat, restaurantLng), DEFAULT_ZOOM);
        } else if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void setUpCluster() {
        mMap.setOnCameraIdleListener(mClusterManager);
        showRestaurants();
        mClusterManager.cluster();
        mClusterManager.setRenderer(new MarkerClusterRenderer(getApplicationContext(), mMap, mClusterManager));
    }

    private void showRestaurants() {

        for (int i = 0; i < restaurantManager.getRestaurants().size(); i++) {

            Restaurant currentRestaurant = restaurantManager.getRestaurantByID(i);
            BitmapDescriptor hazardIcon = null;

            // remember restaurant position in list view
            currentRestaurant.setId(i);

            if (currentRestaurant.getInspectionDataList().isEmpty() == false) {

                Hazard hazard = currentRestaurant.getInspectionDataList().get(0).getHazard();
                if (hazard == Hazard.HIGH) {
                    hazardIcon = bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_high);
                } else if (hazard == Hazard.MEDIUM) {
                    hazardIcon = bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_medium);
                } else {
                    hazardIcon = bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_low);
                }
            }

            PegItem newItem = new PegItem(currentRestaurant.getLatitude(),
                    currentRestaurant.getLongitude(),
                    currentRestaurant.getRestaurantName(),
                    hazardIcon);

            mClusterManager.addItem(newItem);
        }

    }

    private void registerClickCallback() {

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                moveCamera(marker.getPosition(), DEFAULT_ZOOM);

                mMap.setInfoWindowAdapter(new CustomInfoAdapter(MapsActivity.this));
                marker.showInfoWindow();
                return true;
            }
        });

        // show restaurant details from info window
        mMap.setOnInfoWindowClickListener(marker -> {
            LatLng latLng = marker.getPosition();
            double lat = latLng.latitude;
            double lng = latLng.longitude;
            Restaurant restaurant = restaurantManager.findRestaurantByLatLng(lat, lng);

            int restaurantPosition = restaurant.getId();
            Intent intent = SingleRestaurantActivity.makeIntent(MapsActivity.this,
                    restaurantPosition,
                    true);
            startActivity(intent);
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Clear everything
                mClusterManager.clearItems();

                // Clear the currently open marker
                mMap.clear();

                // Reinitialize clusterManager
                setUpCluster();

                // Focus map on the position that was clicked on map
                moveCamera(latLng, 15f);
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<PegItem>() {
            @Override
            public boolean onClusterClick(Cluster<PegItem> cluster) {
                moveCamera(cluster.getPosition(), -10f);
                return true;
            }
        });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void initMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permission,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {
        private Activity context;

        public CustomInfoAdapter(Activity context) {
            this.context = context;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View itemView = context.getLayoutInflater().inflate(R.layout.custom_info_window, null);

            // Find the restaurant to work with.
            LatLng latLng0 = marker.getPosition();
            double lat = latLng0.latitude;
            double lng = latLng0.longitude;
            Restaurant restaurant = restaurantManager.findRestaurantByLatLng(lat, lng);

            // Fill view
            TextView restaurantName = itemView.findViewById(R.id.info_restaurant_name);
            restaurantName.setText(restaurant.getRestaurantName());

            TextView restaurantAddress = itemView.findViewById(R.id.info_restaurant_address);
            restaurantAddress.setText(restaurant.getPhysicalAddress());

            TextView restaurantLevel = itemView.findViewById(R.id.info_restaurant_level);
            if (restaurant.getInspectionDataList().isEmpty()) {
                restaurantLevel.setText("None");
            } else {
                Hazard hazard = restaurant.getInspectionDataList().get(0).getHazard();
                if (hazard == Hazard.LOW) {
                    restaurantLevel.setTextColor(Color.rgb(37, 148, 55));
                    restaurantLevel.setText("LOW");
                } else if (hazard == Hazard.MEDIUM) {
                    restaurantLevel.setTextColor(Color.MAGENTA);
                    restaurantLevel.setText("MEDIUM");
                } else {
                    restaurantLevel.setTextColor((Color.RED));
                    restaurantLevel.setText("HIGH");
                }
            }

            return itemView;
        }
    }

    private class MarkerClusterRenderer extends DefaultClusterRenderer<PegItem> {

        public MarkerClusterRenderer(Context context, GoogleMap map,
                                     ClusterManager<PegItem> clusterManager) {
            super(context, map, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(PegItem item, MarkerOptions markerOptions) {
            // use this to make your change to the marker option
            // for the marker before it gets render on the map
            markerOptions.icon(item.getHazard());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }
}