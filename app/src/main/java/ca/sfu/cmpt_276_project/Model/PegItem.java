package ca.sfu.cmpt_276_project.Model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PegItem implements ClusterItem {

    private LatLng mPosition;
    private String mTitle;
    private BitmapDescriptor mHazard;

    public PegItem(double lat, double lng, String mTitle, BitmapDescriptor mHazard) {
        this.mPosition = new LatLng(lat, lng);
        this.mTitle = mTitle;
        this.mHazard = mHazard;
    }

    public PegItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public BitmapDescriptor getHazard() {
        return mHazard;
    }

    @Override
    public String getSnippet() {
        return "";
    }
}
