/*
 * Class: RestaurantManager
 *
 * Class description: A Singleton class that uses to maintain data derived from csv ingesters.
 *
 * */
package ca.sfu.cmpt_276_project.Model;

import java.util.ArrayList;
import java.util.List;

public class RestaurantManager {

    /**
     * Singleton code
     */
    private static RestaurantManager instance;
    private List<Restaurant> restaurants;

    private RestaurantManager() {
        this.restaurants = new ArrayList<>();
    }

    public static synchronized RestaurantManager getInstance() {
        if (instance == null)
            instance = new RestaurantManager();
        return instance;
    }

    public List<Restaurant> getRestaurants() {
        return this.restaurants;
    }

    // Modified for filtering restaurant based on search window
    private String searchTerm = "";

    public void setMinimumCritical(int minimumCritical) {
        this.minimumCritical = minimumCritical;
    }

    public void setMaximumCritical(int maximumCritical) {
        this.maximumCritical = maximumCritical;
    }

    public void setFavouriteOnly(boolean favouriteOnly) {
        this.favouriteOnly = favouriteOnly;
    }

    public void setHazardLevelFilter(String hazardLevelFilter) {
        this.hazardLevelFilter = hazardLevelFilter;
    }

    private int minimumCritical = 0;
    private int maximumCritical = 99;
    private boolean favouriteOnly = false;
    private String hazardLevelFilter = "Low";

    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }


    public List<Restaurant> getFilteredRestaurants() {
        searchTerm = searchTerm.trim();
        if (searchTerm == "" /*&& hazardLevelFilter.equalsIgnoreCase("Low")
            && !favouriteOnly && minimumCritical == 0 && maximumCritical == 99
           */) {

            return restaurants; // O(1) when search term is empty.
        }

        List<Restaurant> filteredRestaurants = new ArrayList<>();
        for (Restaurant restaurant : restaurants) {
            if (qualifies(restaurant)) {
                filteredRestaurants.add(restaurant);
            }
        }
        return filteredRestaurants;
    }

    boolean inRange (int criticalViolationCount, int minimumCritical, int maximumCritical, Restaurant restaurant){

        if(criticalViolationCount <= maximumCritical
         && criticalViolationCount >= minimumCritical){
            return true;
        }
        return false;
    }

    private boolean qualifies(Restaurant restaurant) {
        String restaurantName = restaurant.getRestaurantName();
        restaurantName = restaurantName.toLowerCase();
        String hazardLevel = restaurant.getInspectionDataList().get(0).
                getHazard().toString();

        // number of critical violations of the last inspection
        int criticalViolationCount = restaurant.getInspectionDataList().
                get(0).getCriticalViolations();

        if (restaurantName.toLowerCase().contains(searchTerm.toLowerCase())
            && ((hazardLevelFilter.equalsIgnoreCase("All")) ||
                (hazardLevel.equalsIgnoreCase(hazardLevelFilter)))
            && inRange(criticalViolationCount,minimumCritical,maximumCritical,restaurant)
            && (!favouriteOnly || restaurant.getFavourite())) {

            return true;

        } else {

            return false;
        }

    }

    /**
     * Usual model code
     */

    public void setRestaurants(List<Restaurant> restaurantList) {
        this.restaurants = restaurantList;
        java.util.Collections.sort(this.restaurants);
        for (Restaurant restaurant : restaurants
        ) {
            List<InspectionData> inspectionDataList = restaurant.getInspectionDataList();
            java.util.Collections.sort(inspectionDataList);
        }
    }

    //THIS IS TO RETRIEVE RESTAURANTS BY TRACKING NUMBER
    public Restaurant getRestaurantByTrackingNumber(String trackNumber)
            throws IndexOutOfBoundsException {

        for (Restaurant restaurant : restaurants) {
            if (restaurant.getTrackNumber().equals(trackNumber))
                return restaurant;
        }
        throw new IndexOutOfBoundsException();
    }

    public Restaurant findRestaurantByLatLng(double latitude, double longitude) {
        for (Restaurant res: restaurants) {
            if (res.getLatitude() == latitude && res.getLongitude() == longitude) {
                return res;
            }
        }
        return null;
    }


    //THIS IS FOR RETRIEVING RESTAURANTS USING ARRAYLIST INDEXING
    public Restaurant getRestaurantByID(int ID) throws IndexOutOfBoundsException {

        for (int i = 0; i < this.restaurants.size(); i++) {
            if (i == ID) {
                return restaurants.get(i);
            }
        }
        throw new IndexOutOfBoundsException();
    }

}
