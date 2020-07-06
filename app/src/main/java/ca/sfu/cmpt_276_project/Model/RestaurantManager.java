package ca.sfu.cmpt_276_project.Model;

import java.util.ArrayList;
import java.util.List;

public class RestaurantManager {

    private List<Restaurant> restaurants;

    /**
     * Singleton code
     */
    private static RestaurantManager instance;

    public static RestaurantManager getInstance(){
        if(instance == null)
            instance = new RestaurantManager();
        return instance;
    }

    private RestaurantManager(){
        this.restaurants = new ArrayList<>();
    }

    /**
     * Usual model code
     */

    public void setRestaurants(List<Restaurant> restaurantList){
        this.restaurants = restaurantList;
    }

    public List<Restaurant> getRestaurants(){ return this.restaurants; }

    //THIS IS TO RETRIEVE RESTAURANTS BY TRACKING NUMER
    public Restaurant getRestaurantByTrackingNumber(String trackNumber)
            throws IndexOutOfBoundsException{

        for(Restaurant restaurant: restaurants){
            if(restaurant.getTrackNumber().equals(trackNumber))
                return restaurant;
        }
        throw new IndexOutOfBoundsException();
    }

    //THIS IS FOR RETRIEVING RESTAURANTS USING ARRAYLIST INDEXING
    public Restaurant getRestaurantByID(int ID) throws IndexOutOfBoundsException{

        for(int i = 0; i < this.restaurants.size(); i++){
            if(i == ID){
                return restaurants.get(i);
            }
        }
        throw new IndexOutOfBoundsException();
    }
}
