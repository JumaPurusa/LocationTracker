package com.example.locationtracker;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver resultReceiver;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if(intent == null){
            return;
        }

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(), 1);
        } catch (IOException e) {
            errorMessage = getString(R.string.service_not_available);
        } catch (IllegalArgumentException e){
            errorMessage = getString(R.string.invalid_lat_long_used);
        }

        // Handle case where no address was found.
        if(addresses == null || addresses.size() == 0){
            if(errorMessage.isEmpty()){
                errorMessage = getString(R.string.no_address_found);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }else{

            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();


            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i=0; i<address.getMaxAddressLineIndex(); i++){

                addressFragments.add(address.getAddressLine(i));
            }

            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                    addressFragments));
        }
    }

    public void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }
}
