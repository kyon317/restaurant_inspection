/*
 * Activity: RestaurantListActivity
 *
 * Activity description: Displays a list of restaurants from an instance of a manager. Selecting
 * a restaurant from the list will take the user to SingleRestaurantActivity. The back button will
 * close the app. Each restaurant on the list will display it's name, icon, hazard level, date of
 * most recent inspection, and number of issues.
 *
 * */
package ca.sfu.cmpt_276_project.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ca.sfu.cmpt_276_project.CsvIngester.InspectionDataCSVIngester;
import ca.sfu.cmpt_276_project.CsvIngester.RestaurantCSVIngester;
import ca.sfu.cmpt_276_project.DBAdapter;
import ca.sfu.cmpt_276_project.Model.Hazard;
import ca.sfu.cmpt_276_project.Model.InspectionData;
import ca.sfu.cmpt_276_project.Model.Restaurant;
import ca.sfu.cmpt_276_project.Model.RestaurantManager;
import ca.sfu.cmpt_276_project.R;
import ca.sfu.cmpt_276_project.WebScraper.DataManager;

public class RestaurantListActivity extends AppCompatActivity {

    private RestaurantManager restaurantManager;
    private List<Restaurant> restaurants = new ArrayList<>();
    private int[] restaurantIcons;
    //TODO: SET UP DB ACCESS
    private DBAdapter dbAdapter;
    private Gson gson = new Gson();
    // allows MainActivity to be accessed
    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, RestaurantListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);
        getSupportActionBar().setTitle("Surrey Restaurant Inspections");

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#31b1c4"));

        // Set BackgroundDrawable
        getSupportActionBar().setBackgroundDrawable(colorDrawable);


        restaurantManager = RestaurantManager.getInstance();
//        initializeRestaurantList();//method necessary to initialize instance
//        Restaurant dummyRestaurant = new Restaurant();
//        dummyRestaurant = getRestaurantFromDB(1430);
//        dummyRestaurant.Display();

        populateRestaurantIcons();
        populateListView();
        registerClickCallback();
        setUpSearchWindow();

        init();

    }

    private List<Restaurant> restaurantSearcher(){
        String savedSearch = getSearchName(this);
        int savedMinCritIssuesInput = getMinCritIssuesInput(this);
        int savedMaxCritIssuesInput = getMaxCritIssuesInput(this);
        String savedHazardChecked = getHazardLevelChecked(this);
        boolean getFavouritesCheck = getFavouritesChecked(this);
//        Log.d("General", "restaurantSearcher: "
//                +"saved search: "+savedSearch
//                +"saved Min: "+savedMinCritIssuesInput
//                +"saved Max: "+savedMaxCritIssuesInput
//                +"saved Hazard: "+savedHazardChecked
//                +"favoriteCheck: "+getFavouritesCheck);
        List<Restaurant> restaurantList = new ArrayList<>();
        if (getFavouritesCheck){
            //TODO: Waiting for DB data, once DB is provided, uncomment this block will finish favourite btn behaviour
//            dbAdapter = new DBAdapter(this);
//            dbAdapter.open();
//            int size = dbAdapter.getAllRows().getCount();
//            Log.d("TAG", "restaurantSearcher: "+size);
////            for (int i = 0;i<size;i++){
////                restaurantList.add(getRestaurantFromDB(i));
////            }
//            dbAdapter.close();
        }else {
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
                    if (!savedHazardChecked.equals("NONE")){
                        restaurantList.remove(restaurantList.get(i));
                        i--;
                    }
                }else if (!savedHazardChecked.equals("NONE")){
                    Hazard this_hazard = restaurantList.get(i).getInspectionDataList().get(0).getHazard();
//                    System.out.println("this_hazard: "+this_hazard);
//                    System.out.println("hazard_check: "+savedHazardChecked);
                    if (!this_hazard.toString().equals(savedHazardChecked)){
                        restaurantList.remove(restaurantList.get(i));
                        i--;
//                        Log.d("TAG", "restaurantSearcher: removed list based on hazard lvl");
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

    private void setUpSearchWindow() {
        ImageButton srchButton = (ImageButton) findViewById(R.id.srchBtn);
        SharedPreferences savePreferences = this.getSharedPreferences("SavePrefs",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = savePreferences.edit();
        srchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(
                        RestaurantListActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.search_window, null);
                EditText searchInput = (EditText) mView.findViewById(R.id.searchInput);
                String savedSearch = getSearchName(RestaurantListActivity.this);
                int savedMinCritIssuesInput = getMinCritIssuesInput(RestaurantListActivity.this);
                int savedMaxCritIssuesInput = getMaxCritIssuesInput(RestaurantListActivity.this);
                String savedHazardChecked = getHazardLevelChecked(RestaurantListActivity.this);
                boolean getFavouritesCheck = getFavouritesChecked(RestaurantListActivity.this);

                if(!savedSearch.equals("")){
                    searchInput.setText(savedSearch, TextView.BufferType.EDITABLE);
                }
                searchInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        editor.putString("Search Name Input", String.valueOf(charSequence));
                        editor.apply();
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
//                        for (int j =0; j<5;j++){
//                            temp_restaurant_list.get(j).Display();
//                        }
                        System.out.println("result size: "+temp_restaurant_list.size());
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
//                        String result = editable.toString();
//                        System.out.println("result: "+ result);

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
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //TODO: Limit min smaller than max
                        String minvalue = String.valueOf(charSequence);
                        editor.putInt("Minimum Issues Input", Integer.parseInt(minvalue));
                        editor.apply();
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
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
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        //TODO: Limit max larger than min
                        String maxValue = String.valueOf(charSequence);
                        editor.putInt("Maximum Issues Input", Integer.parseInt(maxValue));
                        editor.apply();
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                RadioGroup hazardLevelGroup = (RadioGroup) mView.findViewById(
                        R.id.search_hazard_group);
                if(savedHazardChecked.contains("NONE")){
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
//                        Log.d("TAG", "onClick: "+savedHazardChecked);
//                        Log.d("TAG", "onClick: "+checked.getText().toString());
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
//                        Log.d("radio btn clicked", "onCheckedChanged restaurant size: "+restaurants.size());
                        refreshListView(temp_restaurant_list);
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
                            //Todo: only display favourites ... waiting for DB
                            editor.putBoolean("Display Favourites", true);
                            editor.apply();
                            List<Restaurant> temp_restaurant_list = restaurantSearcher();
                            restaurants = temp_restaurant_list;
                            refreshListView(temp_restaurant_list);
                        }
                        else{
                            //favourites not checked
                            //Todo: display all ... waiting for DB
                            editor.putBoolean("Display Favourites", false);
                            editor.apply();
                            List<Restaurant> temp_restaurant_list = restaurantSearcher();
                            restaurants = temp_restaurant_list;
                            refreshListView(temp_restaurant_list);
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
                        editor.putString("Hazard Check Change", String.valueOf(R.string.none));
                        favouritesSwitch.setChecked(false);
                        editor.putBoolean("Display Favourites", false);
                        editor.apply();
                        List<Restaurant> temp_restaurant_list = restaurantSearcher();
                        restaurants = temp_restaurant_list;
                        refreshListView(temp_restaurant_list);
                    }
                });
                mBuilder.setView(mView);

                //Dialog part, preserve search criteria at UI
                AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Log.d("TAG", "onCancel: ");
                        String savedSearch = getSearchName(RestaurantListActivity.this);
                        int savedMinCritIssuesInput = getMinCritIssuesInput(RestaurantListActivity.this);
                        int savedMaxCritIssuesInput = getMaxCritIssuesInput(RestaurantListActivity.this);
                        String savedHazardChecked = getHazardLevelChecked(RestaurantListActivity.this);
                        boolean getFavouritesCheck = getFavouritesChecked(RestaurantListActivity.this);
                        editor.putString("Search Name Input", savedSearch);
                        editor.putInt("Minimum Issues Input", savedMinCritIssuesInput);
                        editor.putInt("Maximum Issues Input", savedMaxCritIssuesInput);
                        editor.putString("Hazard Check Change", savedHazardChecked);
                        editor.putBoolean("Display Favourites", getFavouritesCheck);
                        editor.apply();
                        List<Restaurant> temp_list = restaurantSearcher();
                        refreshListView(temp_list);
                    }
                });
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


    /**
     * Get restaurant obj from DB by ROW_ID
     * */
    private Restaurant getRestaurantFromDB(int ROW_ID){
        Cursor cursor = dbAdapter.getAllRows();
        Restaurant restaurant = new Restaurant();
        if (cursor.move(ROW_ID)){
            Type type = new TypeToken<ArrayList<InspectionData>>() {}.getType();
            List<InspectionData> tempList = gson.fromJson(cursor.getString(DBAdapter.COL_INSPECTION), type);
            restaurant.setTrackNumber(cursor.getString(DBAdapter.COL_TRACK_NUM));
            restaurant.setRestaurantName(cursor.getString(DBAdapter.COL_RES_NAME));
            restaurant.setPhysicalAddress(cursor.getString(DBAdapter.COL_ADDRESS));
            restaurant.setPhysicalCity(cursor.getString(DBAdapter.COL_CITY));
            restaurant.setFacType(cursor.getString(DBAdapter.COL_FAC_TYPE));
            restaurant.setLatitude(cursor.getDouble(DBAdapter.COL_LATITUDE));
            restaurant.setLongitude(cursor.getDouble(DBAdapter.COL_LONGITUDE));
            if(!tempList.isEmpty()) {
                restaurant.setInspectionDataList(tempList);
            }
        }
        cursor.close();
        return restaurant;
    }

    public void initializeRestaurantList() {
        //get Restaurants from CSV
        RestaurantCSVIngester restaurantImport = new RestaurantCSVIngester();
        List<Restaurant> restaurantList = new ArrayList<>();
        DataManager dataManager = new DataManager();
        try {
            if (dataManager.checkFileExistence(dataManager.getRestaurant_filename())){
                restaurantImport.readRestaurantList(this, dataManager.getDirectory_path()+dataManager.getRestaurant_filename(), 1);
            }else{
                restaurantImport.readRestaurantList(this, null, 0);
            }
            restaurantList = restaurantImport.getRestaurantList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get Inspection Data of Restaurants from CSV
        InspectionDataCSVIngester inspectionDataImport = new InspectionDataCSVIngester();
        try {
            if (dataManager.checkFileExistence(dataManager.getInspection_filename())){
                inspectionDataImport.readInspectionData(this, dataManager.getDirectory_path()+dataManager.getInspection_filename(), 1);
            }else
            inspectionDataImport.readInspectionData(this, null, 0);
            //Sort inspection data into proper Restaurant objects
            if (!restaurantList.isEmpty()) {
                for (Restaurant restaurant : restaurantList) {
                    restaurant.setInspectionDataList(inspectionDataImport.returnInspectionByID
                            (restaurant.getTrackNumber()));
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        //Update existing Restaurant Manager obj instance
        restaurantManager.setRestaurants(restaurantList);
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
    }

    private void populateRestaurantIcons() {
        restaurantIcons = new int[8];
        restaurantIcons[0] = R.drawable.icon_sushi;
        restaurantIcons[1] = R.drawable.icon_dimsum;
        restaurantIcons[2] = R.drawable.icon_dimsum;
        restaurantIcons[3] = R.drawable.icon_beer;
        restaurantIcons[4] = R.drawable.icon_pizza;
        restaurantIcons[5] = R.drawable.icon_pizza;
        restaurantIcons[6] = R.drawable.icon_chicken;
    }

    // start Maps activity
    private void init() {
        Button btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = MapsActivity.makeIntent(RestaurantListActivity.this);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restaurantManager = RestaurantManager.getInstance();
        if (RestaurantManager.getInstance().getRestaurants().isEmpty()){
            initializeRestaurantList();
            restaurantManager = RestaurantManager.getInstance();
        }
        populateListView();
        String savedSearch = getSearchName(this);

        if (!savedSearch.equals("")){
            List<Restaurant> dummy_restaurant = restaurantSearcher();
            refreshListView(dummy_restaurant);
        }
        registerClickCallback();
    }

    private void populateListView() {
        restaurantManager = RestaurantManager.getInstance();
        restaurants = restaurantManager.getRestaurants();
        ArrayAdapter<Restaurant> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }

    private void refreshListView(List<Restaurant> restaurantList){
        restaurants = restaurantList;
        ArrayAdapter<Restaurant> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.restaurantsListView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Restaurant clickedRestaurant = restaurants.get(position);
//                Restaurant clickedRestaurant = getRestaurantFromDB(position);
                // pass clicked restaurant's position to SingleRestaurantActivity
                Intent intent = SingleRestaurantActivity.makeIntent(RestaurantListActivity.this, position, false,clickedRestaurant.getTrackNumber());
                startActivity(intent);
            }
        });
    }


    private class MyListAdapter extends ArrayAdapter<Restaurant> {

        public MyListAdapter() {
            super(RestaurantListActivity.this, R.layout.restaurants_view, restaurants);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View restaurantView = convertView;
            if (restaurantView == null) {
                restaurantView = getLayoutInflater().inflate(R.layout.restaurants_view, parent, false);
            }

//            System.out.println("position: "+position);
            Restaurant currentRestaurant = restaurants.get(position);
//            Restaurant currentRestaurant = getRestaurantFromDB(position);

            // Fill restaurant image
            ImageView resImageView = (ImageView) restaurantView.findViewById(R.id.restaurant_icon);
            // currentRestaurant.setIcon(restaurantIcons.get(position));
            if(currentRestaurant.getRestaurantName().contains("A&W"))
                currentRestaurant.setIcon(R.drawable.icon_aw);
            else if(currentRestaurant.getRestaurantName().contains("Booster"))
                currentRestaurant.setIcon(R.drawable.icon_booster_juice);
            else if(currentRestaurant.getRestaurantName().contains("Boston"))
                currentRestaurant.setIcon(R.drawable.icon_boston_pizza);
            else if(currentRestaurant.getRestaurantName().contains("Canuel"))
                currentRestaurant.setIcon(R.drawable.icon_canuel_cateres);
            else if(currentRestaurant.getRestaurantName().contains("Jugo"))
                currentRestaurant.setIcon(R.drawable.icon_jugo_juice);
            else if(currentRestaurant.getRestaurantName().contains("KFC"))
                currentRestaurant.setIcon(R.drawable.icon_kfc);
            else if(currentRestaurant.getRestaurantName().contains("Little"))
                currentRestaurant.setIcon(R.drawable.icon_little_caesar);
            else if(currentRestaurant.getRestaurantName().contains("McD"))
                currentRestaurant.setIcon(R.drawable.icon_mcd);
            else if(currentRestaurant.getRestaurantName().contains("Papa"))
                currentRestaurant.setIcon(R.drawable.icon_papa_johns);
            else if(currentRestaurant.getRestaurantName().contains("Eleven"))
                currentRestaurant.setIcon(R.drawable.icon_seven_eleven);
            else {//randomly assign icon image
                Random random = new Random();
                int randInt = random.nextInt(6);
                currentRestaurant.setIcon(restaurantIcons[randInt]);
            }

            resImageView.setImageResource(currentRestaurant.getIcon());

            ImageView favIconView = (ImageView) restaurantView.findViewById(R.id.favouriteIcon);
            if(!currentRestaurant.getFavourite()){
                favIconView.setVisibility(View.INVISIBLE);
            }

            // Fill hazard icon
            ImageView hazardIconView = (ImageView) restaurantView.findViewById(
                    R.id.restaurant_hazardicon);

            //Fill hazard level with color
            TextView hazardLevelView = (TextView) restaurantView.findViewById(R.id.hazard_level);
            if (currentRestaurant.getInspectionDataList().isEmpty()) {
                hazardLevelView.setText("None");
            } else {
                Hazard hazard = currentRestaurant.getInspectionDataList().get(0).getHazard();
                if (hazard == Hazard.LOW) {
                    hazardLevelView.setTextColor(Color.rgb(37, 148, 55));
                    hazardIconView.setImageResource(R.drawable.hazardlow);
                    hazardLevelView.setText("LOW");
                } else if (hazard == Hazard.MEDIUM) {
                    hazardLevelView.setTextColor(Color.MAGENTA);
                    hazardIconView.setImageResource(R.drawable.hazardyellow);
                    hazardLevelView.setText("MEDIUM");
                } else {
                    hazardLevelView.setTextColor((Color.RED));
                    hazardIconView.setImageResource(R.drawable.hazardhigh);
                    hazardLevelView.setText("HIGH");
                }


            }

            // Fill name
            TextView nameText = (TextView) restaurantView.findViewById(R.id.restaurant_txtName);
            nameText.setText(currentRestaurant.getRestaurantName());

            //Fill the most recent inspection date
            TextView dateText = (TextView) restaurantView.findViewById(R.id.restaurant_txtDate);

            if (currentRestaurant.getInspectionDataList().isEmpty()) {
                dateText.setText("");
            } else {
                // Get the most recent inspection date
                Date recentInspectDate = currentRestaurant.getInspectionDataList().get(0).getInspectionDate();

                long date = currentRestaurant.getInspectionDataList().get(0).timeSinceInspection();
                if (date < 30) {
                    dateText.setText(String.valueOf(date));
                } else if (date < 365) {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd");
                    String strDate = formatter.format(recentInspectDate);
                    dateText.setText(strDate);
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMMM YYYY");
                    String strDate = formatter.format(recentInspectDate);
                    dateText.setText(strDate);
                }

            }

            // Fill # issues
            TextView numIssuesText = (TextView) restaurantView.findViewById(R.id.restaurant_txtIssues);

            if (currentRestaurant.getInspectionDataList().isEmpty()) {
                numIssuesText.setText("");
            } else {
                int currentCritical = currentRestaurant.getInspectionDataList().get(0).getCriticalViolations();
                int currentNonCritical = currentRestaurant.getInspectionDataList().get(0).getNonCriticalViolations();
                int numIssues = currentCritical + currentNonCritical;

                numIssuesText.setText("" + numIssues);
            }

            return restaurantView;
        }
    }
}
