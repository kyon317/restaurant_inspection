package ca.sfu.cmpt_276_project.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Singleton model: Store a collection of restaurants.
 */
/*
public class RestaurantsManager implements Iterable<Restaurants> {

    private List<Restaurants> restaurants = new ArrayList<>();

    private static RestaurantsManager instance;
    public static RestaurantsManager getInstance(){
        if(instance == null ){
            instance = new RestaurantsManager();
        }
        return instance;
    }

    private RestaurantsManager(){
        // Nothing: make sure this is a singleton
    }

    public void add(Restaurants restaurants) { restaurants.add(restaurants);}

    @NonNull
    @Override
    public Iterator<Restaurants> iterator() {
        return Restaurants.iterator();
    }
}

 */
