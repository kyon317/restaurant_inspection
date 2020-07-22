/*Google Map API Class
* */
package ca.sfu.cmpt_276_project.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.PegItem;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static Intent makeIntent(Context context, Double latitude, double longitude,
                                    Boolean fromRestaurant) {
        Intent intent= new Intent(context, MapsActivity.class);
        intent.putExtra(EXTRA_LAT, latitude);
        intent.putExtra(EXTRA_LNG, longitude);
        intent.putExtra(EXTRA_BOOL, fromRestaurant);
        return intent;
    }

    private double restaurantLat;
    private double restaurantLng;
    private Boolean fromRestaurant;

    private static final String EXTRA_LAT = "ca.sfu.cmpt_276_project.UI.extraLat";
    private static final String EXTRA_LNG = "ca.sfu.cmpt_276_project.UI.extraLng";
    private static final String EXTRA_BOOL = "ca.sfu.cmpt_276_project.UI.extraBool";
    private GoogleMap mMap;
    private Marker mMarker;
    private UiSettings mUiSettings;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 18f;

    private RestaurantManager restaurantManager;
    private int[] restaurantIcons;
    private List<Restaurant> restaurants;

    private ClusterManager<PegItem> mClusterManager;

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
                Intent intent = MainActivity.makeIntent(MapsActivity.this);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocationPermission();

        restaurantManager = RestaurantManager.getInstance();

        init();

        Intent intent = getIntent();
        restaurantLat = intent.getDoubleExtra(EXTRA_LAT, 49.1915);
        restaurantLng = intent.getDoubleExtra(EXTRA_LNG, 122.8456);
        fromRestaurant = intent.getBooleanExtra(EXTRA_BOOL, false);
        if(fromRestaurant == true){
            //TODO: have camera move to given location
            moveCamera(new LatLng(restaurantLat, restaurantLng), DEFAULT_ZOOM);

            mMap.setInfoWindowAdapter(new CustomInfoAdapter(MapsActivity.this));

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onBackPressed(){
        finish();
        super.onBackPressed();
    }

    @Override
    public void onDestroy()
    {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

/*    public void initializeRestaurantList(){
        //get Restaurants from CSV
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        List<Restaurant> restaurantList = new ArrayList<>();

        try {
            restaurantImport.readRestaurantList(this, null, 0 );
            restaurantList = restaurantImport.getRestaurantList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get Inspection Data of Restaurants from CSV
        InspectionDataCSVIngester inspectionDataImport = new InspectionDataCSVIngester();
        try {
            inspectionDataImport.readInspectionData(this, null, 0 );
            //Sort inspection data into proper Restaurant objects
            if (!restaurantList.isEmpty()) {
                for (Restaurant restaurant : restaurantList) {
                    restaurant.setInspectionDataList(inspectionDataImport.returnInspectionByID
                            (restaurant.getTrackNumber()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager.setRestaurants(restaurantList);

    }*/


    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
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
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
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

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            mClusterManager = new ClusterManager<>(this, mMap);

            // somehow UI gesture doesn't work,
            // can't pinch to zoom in and out
            mMap.getUiSettings().setZoomGesturesEnabled(true);

            setUpCluster();
            //Set Custom InfoWindow Adapter
            CustomInfoAdapter adapter = new CustomInfoAdapter(MapsActivity.this);
            mMap.setInfoWindowAdapter(adapter);

            registerClickCallback();

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

    private void setUpCluster() {
        mMap.setOnCameraIdleListener(mClusterManager);
        showRestaurants();
        mClusterManager.cluster();
        mClusterManager.setRenderer(new MarkerClusterRenderer(getApplicationContext(), mMap, mClusterManager));
    }

    private void showRestaurants() {
        /*
        //takes each restaurant in the manager and places it on the map.
        MarkerOptions options;

        for(int i = 0; i < restaurantManager.getRestaurants().size();i++){

            Restaurant currentRestaurant = restaurantManager.getRestaurantByID(i);

            // remember restaurant position in list view
            currentRestaurant.setId(i);

            String snippet = "Address:          " + currentRestaurant.getPhysicalAddress() + "\n"
                            + "Recent Inspection Level:            ";

            options = new MarkerOptions();
            options.position(new LatLng(currentRestaurant.getLatitude(),
                    currentRestaurant.getLongitude()));
            options.title(currentRestaurant.getRestaurantName());

            if(currentRestaurant.getInspectionDataList().isEmpty() == false) {

                Hazard hazard = currentRestaurant.getInspectionDataList().get(0).getHazard();
                if(hazard == Hazard.HIGH) {
                    options.icon(bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_high));
                    snippet = snippet + "High";
                }
                else if(hazard == Hazard.MEDIUM){
                    options.icon(bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_medium));
                    snippet = snippet + "Medium";
                }
                else {
                    options.icon(bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_low));
                    snippet = snippet + "Low";
                }
            }

            options.snippet(snippet);

            mMarker = mMap.addMarker(options);

        }

         */

        for(int i = 0; i < restaurantManager.getRestaurants().size();i++){

            Restaurant currentRestaurant = restaurantManager.getRestaurantByID(i);
            BitmapDescriptor hazardIcon = null;

            // remember restaurant position in list view
            currentRestaurant.setId(i);

            if(currentRestaurant.getInspectionDataList().isEmpty() == false) {

                Hazard hazard = currentRestaurant.getInspectionDataList().get(0).getHazard();
                if(hazard == Hazard.HIGH) {
                    hazardIcon = bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_high);
                }
                else if(hazard == Hazard.MEDIUM){
                    hazardIcon = bitmapDescriptorFromVector(getApplicationContext(),
                            R.drawable.icon_map_medium);
                }
                else {
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

        //testCluster();
    }
/*
    public void testCluster (){
        double lat = 49.19166309;
        double lng = -122.8136499;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            PegItem offsetItem = new PegItem(lat, lng);
            mClusterManager.addItem(offsetItem);
        }
    }
 */
    private void registerClickCallback() {

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                moveCamera(marker.getPosition(),DEFAULT_ZOOM);

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
            Intent intent = SingleRestaurantActivity.makeIntent(MapsActivity.this, restaurantPosition);
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

    private void initMap(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission(){
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            } else{
                ActivityCompat.requestPermissions(this,
                        permission,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i= 0; i<grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
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
/*
    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
        private final View mWindow;
        private Context mContext;

        public CustomInfoWindowAdapter(Context context) {
            mContext = context;
            mWindow = LayoutInflater.from(context).inflate(R.layout.info_window,null);
        }

        private void renderWindowText(Marker marker, View view) {
            String title = marker.getTitle();
            TextView tvTitle = (TextView) view.findViewById(R.id.title);
            if(!title.equals("")){
                tvTitle.setText(title);
            }
            String snippet = marker.getSnippet();
            TextView tvSnippet = (TextView) view.findViewById(R.id.snippet);
            if(!snippet.equals("")){
                tvSnippet.setText(snippet);
            }
        }

        @Override
        public View getInfoWindow(Marker marker) {
            renderWindowText(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            renderWindowText(marker, mWindow);
            return mWindow;
        }
    }

 */

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