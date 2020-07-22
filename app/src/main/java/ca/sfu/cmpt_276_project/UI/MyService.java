package ca.sfu.cmpt_276_project.UI;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.google.android.gms.location.LocationResult;

import java.util.Map;

public class MyService extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATE = "ca.sfu.cmpt_276_project.UI.UPDATE_LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)){
                LocationResult result = LocationResult.extractResult(intent);
                if(result !=null){
                    Location location = result.getLastLocation();
                    try{
                        MapsActivity.getInstance().updateLocation(location);
                    }catch (Exception e){
                    }
                }
            }
        }
    }
}
