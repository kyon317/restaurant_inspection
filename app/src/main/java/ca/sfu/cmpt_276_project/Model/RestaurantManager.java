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
