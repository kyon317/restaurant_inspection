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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.sfu.cmpt_276_project.DBAdapter;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.PegItem;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String EXTRA_NAME = "ca.sfu.cmpt_276_project.UI.extraName";
    private static final String EXTRA_LAT = "ca.sfu.cmpt_276_project.UI.extraLat";
    private static final String EXTRA_LNG = "ca.sfu.cmpt_276_project.UI.extraLng";
    private static final String EXTRA_BOOL = "ca.sfu.cmpt_276_project.UI.extraBool";
    private static final String EXTRA_BOOL2 = "ca.sfu.cmpt_276_project.UI.extraBool2";
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
    private Boolean fromRestaurantList;
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
    private Gson gson = new Gson();//necessary to convert Array list
    private ConstraintLayout searchLayout;

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

    }

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

    private List<Restaurant> restaurantSearcher(){
        String savedSearch = getSearchName(this);
        int savedMinCritIssuesInput = getMinCritIssuesInput(this);
        int savedMaxCritIssuesInput = getMaxCritIssuesInput(this);
        String savedHazardChecked = getHazardLevelChecked(this);
        boolean getFavouritesCheck = getFavouritesChecked(this);
        List<Restaurant> restaurantList = new ArrayList<>();
        if (getFavouritesCheck){
            dbAdapter = new DBAdapter(this);
            dbAdapter.open();
            int size = dbAdapter.getAllRows().getCount();
            Log.d("TAG", "restaurantSearcher: "+size);
            for (int i = 0;i<size;i++){
                restaurantList.add(dbAdapter.getRestaurant(i));
            }
            dbAdapter.close();
        }
        else {
            restaurantList = findRestaurantByNames(savedSearch);
        }
        for (int i = 0;i<restaurantList.size();i++) {
//                Log.d("TAG", "restaurantSearcher: size of list: "+restaurantList.size()
//                +"i: "+i);
            if (restaurantList.get(i).getInspectionDataList().size()<savedMinCritIssuesInput||
                    restaurantList.get(i).getInspectionDataList().size()>savedMaxCritIssuesInput){
                restaurantList.remove(restaurantList.get(i));
                i--;
                continue;
            }
            if (restaurantList.get(i).getInspectionDataList().isEmpty()){
                if (!(savedHazardChecked.equals(String.valueOf("All"))||savedHazardChecked.equalsIgnoreCase("Toutes"))){
                    restaurantList.remove(restaurantList.get(i));
                    i--;
                }
            }else if (!(savedHazardChecked.equals(String.valueOf("All"))||savedHazardChecked.equalsIgnoreCase("Toutes"))){
                Hazard this_hazard = restaurantList.get(i).getInspectionDataList().get(0).getHazard();
                String hazardLevelTranslated  = this_hazard.toString();
                if (Locale.getDefault().getLanguage().equals("fr")){
                    switch (this_hazard){
                        case LOW:
                            hazardLevelTranslated = String.valueOf("BAS");
                            break;
                        case MEDIUM:
                            hazardLevelTranslated = String.valueOf("MOYEN");
                            break;
                        case HIGH:
                            hazardLevelTranslated = String.valueOf("HAUT");
                            break;
                        default:
                            hazardLevelTranslated = String.valueOf("Toutes");
                    }
                }
                if (!hazardLevelTranslated.equals(savedHazardChecked)){
                    restaurantList.remove(restaurantList.get(i));
                    i--;
                }
            }
        }

        return restaurantList;
    }
    private List<Restaurant> findRestaurantByNames(String search_name) {
        List<Restaurant> restaurantList = new ArrayList<>();
        for (Restaurant res: restaurantManager.getRestaurants()) {
            if (res.getRestaurantName().toLowerCase().contains(search_name.toLowerCase())) {
                restaurantList.add(res);
            }
        }
        return restaurantList;
    }

    private boolean withinAYear(int savedMin, int savedMax, Restaurant currentRestaurant){

            int count = 0;
            for(int i=0; i<currentRestaurant.getInspectionDataList().size(); i++){
                int numCritical = currentRestaurant.getInspectionDataList().get(i).getCriticalViolations();
                long date = currentRestaurant.getInspectionDataList().get(i).timeSinceInspection();
                if (date < 365) {
                    count += numCritical;
                }
            }

        if(count >= savedMin && count <= savedMax){
                return true;
            }
            else{
                return false;
            }

    }

    public static MapsActivity getInstance() {
        return instance;
    }

    // allows MapsActivity to be accessed
    public static Intent makeIntent(Context context) {
        return new Intent(context, MapsActivity.class);
    }

    public static Intent makeIntent(Context context, boolean fromRestaurantList) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(EXTRA_BOOL2, fromRestaurantList);
        return intent;
    }

    @SuppressLint("ResourceType")
    private void init() {
        ImageButton btnMain = (ImageButton) findViewById(R.id.btnMain);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = RestaurantListActivity.makeIntent(MapsActivity.this);
                startActivity(intent);
            }
        });
        /*
        * Search funtionality defined below
        * Saves name, minCritInspections, maxCritInspections, showFavourites, hazardRating
        *
        * */

        searchLayout = (ConstraintLayout) findViewById(R.layout.search_window);
        SharedPreferences savePreferences = this.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = savePreferences.edit();

        ImageButton srchBtn = (ImageButton) findViewById(R.id.searchBtn);
        srchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show search layout

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);

                String savedSearch = getSearchName(MapsActivity.this);
                int savedMinCritIssuesInput = getMinCritIssuesInput(MapsActivity.this);
                int savedMaxCritIssuesInput = getMaxCritIssuesInput(MapsActivity.this);
                String savedHazardChecked = getHazardLevelChecked(MapsActivity.this);
                boolean getFavouritesCheck = getFavouritesChecked(MapsActivity.this);

                View mView = getLayoutInflater().inflate(R.layout.search_window, null);
                EditText searchInput = (EditText) mView.findViewById(R.id.searchInput);
                if(savedSearch != ""){
                    searchInput.setText(savedSearch, TextView.BufferType.EDITABLE);
                }
                searchInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        editor.putString("Search Name Input", String.valueOf(charSequence));
                        editor.apply();
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        refreshMapView(temp_restaurant_list);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                EditText minCritIssues = (EditText) mView.findViewById(R.id.minCritInput);
                if(savedMinCritIssuesInput != 0){
                    String intString = Integer.toString(savedMinCritIssuesInput) ;
                    minCritIssues.setText(intString, TextView.BufferType.EDITABLE);
                }
                minCritIssues.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String minvalue = String.valueOf(charSequence);
                        if(!(minvalue.equals(""))){
                            editor.putInt("Minimum Issues Input", Integer.parseInt(minvalue));
                            editor.apply();
                        }else{
                            editor.putInt("Minimum Issues Input", 0);
                            editor.apply();
                        }
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        refreshMapView(temp_restaurant_list);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                EditText maxCritIssues = (EditText) mView.findViewById(R.id.maxCritInput);
                if(savedMaxCritIssuesInput != 50000){
                    String intString = Integer.toString(savedMaxCritIssuesInput) ;
                    maxCritIssues.setText(intString, TextView.BufferType.EDITABLE);
                }
                maxCritIssues.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        String maxValue = String.valueOf(charSequence);

                        if(!maxValue.equals("")){
                            editor.putInt("Maximum Issues Input", Integer.parseInt(maxValue));
                            editor.apply();
                            List<Restaurant> temp_restaurant_list = restaurantSearcher();
                            refreshMapView(temp_restaurant_list);
                        }

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                RadioGroup hazardLevelGroup = (RadioGroup) mView.findViewById(
                        R.id.search_hazard_group);
                if(savedHazardChecked.contains("All")){
                    RadioButton noneRadioButton = (RadioButton) mView.findViewById(R.id.radioButtonNone);
                    noneRadioButton.setChecked(true);
                }
                else if(savedHazardChecked.contains("LOW")){
                    RadioButton lowRadioButton = (RadioButton) mView.findViewById(R.id.radioButtonLow);
                    lowRadioButton.setChecked(true);
                }
                else if(savedHazardChecked.contains("MEDIUM")){
                    RadioButton mediumRadioButton = (RadioButton) mView.findViewById(R.id.radioButtonMedium);
                    mediumRadioButton.setChecked(true);
                }
                else if(savedHazardChecked.contains("HIGH")){
                    RadioButton highRadioButton = (RadioButton) mView.findViewById(R.id.radioButtonHigh);
                    highRadioButton.setChecked(true);
                }
                else{
                    System.out.println(savedHazardChecked);
                }

                hazardLevelGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        RadioButton checked = (RadioButton) mView.findViewById(i);
                        editor.putString("Hazard Check Change", String.valueOf(checked.getText()));
                        editor.apply();
                        String a = String.valueOf(checked.getText());
                        Log.d("TAG", "onCheckedChanged: "+String.valueOf(checked.getText()));

                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        refreshMapView(temp_restaurant_list);
                    }
                });
                Switch favouritesSwitch = (Switch) mView.findViewById(R.id.favouritesSwitch);
                if(getFavouritesCheck){
                    favouritesSwitch.setChecked(true);
                }
                favouritesSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(favouritesSwitch.isChecked()){
                            //favourites has been checked
                            editor.putBoolean("Display Favourites", true);
                            editor.apply();
                            List<Restaurant> temp_restaurant_list = restaurantSearcher();
                            refreshMapView(temp_restaurant_list);
                        }
                        else{
                            //favourites not checked
                            editor.putBoolean("Display Favourites", false);
                            editor.apply();
                            List<Restaurant> temp_restaurant_list = restaurantSearcher();
                            refreshMapView(temp_restaurant_list);
                        }

                    }
                });
                Button resetBtn = (Button) mView.findViewById(R.id.resetBtn);
                resetBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchInput.setText(null);
                        editor.putString("Search Name Input", null);
                        minCritIssues.setText(String.valueOf(0));
                        editor.putInt("Minimum Issues Input", 0);
                        maxCritIssues.setText(String.valueOf(99));
                        editor.putInt("Maximum Issues Input", 99);
                        RadioButton noneRadioButton = (RadioButton) mView.findViewById(R.id.radioButtonNone);
                        noneRadioButton.setChecked(true);
                        editor.putString("Hazard Check Change", getString(R.string.all));
                        favouritesSwitch.setChecked(false);
                        editor.putBoolean("Display Favourites", false);
                        editor.apply();
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        refreshMapView(temp_restaurant_list);

                    }
                });
                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d("TAG", "onCancel: ");
                        String savedSearch = getSearchName(MapsActivity.this);
                        int savedMinCritIssuesInput = getMinCritIssuesInput(MapsActivity.this);
                        int savedMaxCritIssuesInput = getMaxCritIssuesInput(MapsActivity.this);
                        String savedHazardChecked = getHazardLevelChecked(MapsActivity.this);
                        boolean getFavouritesCheck = getFavouritesChecked(MapsActivity.this);
                        editor.putString("Search Name Input", savedSearch);
                        editor.putInt("Minimum Issues Input", savedMinCritIssuesInput);
                        editor.putInt("Maximum Issues Input", savedMaxCritIssuesInput);
                        editor.putString("Hazard Check Change", savedHazardChecked);
                        editor.putBoolean("Display Favourites", getFavouritesCheck);
                        editor.apply();

                        List<Restaurant> temp_list = restaurantSearcher();
                        refreshMapView(temp_list);
                    }
                });
            }
        });
    }

    public void refreshMapView(List<Restaurant> restaurants){
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mClusterManager.getAlgorithm().getItems().isEmpty()){
                    mClusterManager.clearItems();
                    mMap.clear();
                }
                for (int i = 0; i < restaurants.size(); i++) {

                    Restaurant currentRestaurant = restaurants.get(i);
                    BitmapDescriptor hazardIcon = null;
                    restaurantName = currentRestaurant.getRestaurantName();
                    restaurantLat = currentRestaurant.getLatitude();
                    restaurantLng = currentRestaurant.getLongitude();

                    if (!currentRestaurant.getInspectionDataList().isEmpty()) {

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

                    if(currentRestaurant.getInspectionDataList().isEmpty()){
                        MarkerOptions options = new MarkerOptions().title(restaurantName).
                                position(new LatLng(restaurantLat, restaurantLng));

                        mMarker = mMap.addMarker(options);

                    }

                    PegItem newItem = new PegItem(currentRestaurant.getLatitude(),
                            currentRestaurant.getLongitude(),
                            currentRestaurant.getRestaurantName(),
                            hazardIcon);

                    mClusterManager.addItem(newItem);
                }
                mClusterManager.cluster();
                mClusterManager.setRenderer(new MarkerClusterRenderer(getApplicationContext(), mMap, mClusterManager));
            }
        });

    }

    public static String getSearchName(Context context){
        SharedPreferences searchPrefs = context.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        return searchPrefs.getString("Search Name Input", "");
    }

    public static  int getMinCritIssuesInput(Context context){
        SharedPreferences searchPrefs = context.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        return searchPrefs.getInt("Minimum Issues Input", 0);
    }

    public static int getMaxCritIssuesInput(Context context){
        SharedPreferences searchPrefs = context.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        return searchPrefs.getInt("Maximum Issues Input", 50000);
    }

    public static String getHazardLevelChecked(Context context){
        SharedPreferences searchPrefs = context.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        return searchPrefs.getString("Hazard Check Change", "NONE");
    }

    public static boolean getFavouritesChecked(Context context){
        SharedPreferences searchPrefs = context.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        return searchPrefs.getBoolean("Display Favourites", false);

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
//        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();

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
        fromRestaurantList = intent.getBooleanExtra(EXTRA_BOOL2, false);

        if (fromRestaurant == true) {
            mClusterManager.clearItems();

            MarkerOptions options = new MarkerOptions().title(restaurantName).
                    position(new LatLng(restaurantLat, restaurantLng));

            mMarker = mMap.addMarker(options);
            mMarker.showInfoWindow();
            moveCamera(new LatLng(restaurantLat, restaurantLng), DEFAULT_ZOOM);
        }
        else if(fromRestaurantList){
            List<Restaurant> temp_restaurant_list = restaurantSearcher();
            refreshMapView(temp_restaurant_list);
        }
        else if (mLocationPermissionGranted) {
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

            if (!currentRestaurant.getInspectionDataList().isEmpty()) {

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
                    true,restaurant.getTrackNumber());
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
        assert mapFragment != null;
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
                    restaurantLevel.setText(R.string.low);
                } else if (hazard == Hazard.MEDIUM) {
                    restaurantLevel.setTextColor(Color.MAGENTA);
                    restaurantLevel.setText(R.string.medium);
                } else {
                    restaurantLevel.setTextColor((Color.RED));
                    restaurantLevel.setText(R.string.high);
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