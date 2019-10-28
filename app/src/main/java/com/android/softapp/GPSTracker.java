package com.android.softapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

public abstract class GPSTracker extends Service implements LocationListener
{
    public static final String KEY_LOCATION_URL = "https://www.google.com/maps/search/?api=1&query=";
    public static final String KEY_LOCATION_SHORT_URL = "https://goo.gl/rJ371K";

    private final Context mContext;
    //flag for Network passive
    boolean isNetworkPassiveEnabled = false;
    //flag for GPS Status
    boolean isGPSEnabled = false;

    //flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    boolean isLocationGet=false;

    //The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 metters

    //The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 1 minute

    //Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(Context context)
    {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation()
    {
        try
        {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
           //getting Network passive
            isNetworkPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled)
            {
                // no network provider is enabled
                Toast.makeText(mContext, "No GPs ,No Network", Toast.LENGTH_SHORT).show();
            }
            else
            {
                this.canGetLocation = true;

                //if GPS Enabled get lat/long using GPS Services
                 if (isGPSEnabled)
                {
                    getLocationFromGPS();
                }
                //First get location from Network Provider
              else if (isNetworkEnabled)
                {
                    getLocationFromNetwork();
                }
                //if PASSIVE Enabled get lat/long using PASSIVE Services
               else if (isNetworkPassiveEnabled)
                {
                    getPassiveProvideLocation();
                }


            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Error : Location", "Impossible to connect to LocationManager", e);
        }

        return location;
    }

    private void getLocationFromGPS() {

        //getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (location == null)
        {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            Log.d("GPS Enabled", "GPS Enabled");
        if(isGPSEnabled)
        {
            Toast.makeText(mContext, "GPS Available", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(mContext, "GPS not available", Toast.LENGTH_SHORT).show();
        }

            if (locationManager != null)
            {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location!=null)
                {
                    updateGPSCoordinates(location);
                }
                else
                {
                    getLocationFromNetwork();
                }
            }
        }
    }

    private void getPassiveProvideLocation() {

        //getting Network passive
        isNetworkPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        if(isNetworkPassiveEnabled)
        {
            Toast.makeText(mContext, "Network2 Available", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(mContext, "Network2 not available", Toast.LENGTH_SHORT).show();
        }

        if (location == null)
        {
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            Toast.makeText(mContext, "Network2 Available", Toast.LENGTH_SHORT).show();

            if (locationManager != null)
            {
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if(location!=null)
                {
                    updateGPSCoordinates(location);
                }
                else
                {
                    getLocationFromGPS();
                }
            }
        }
    }

    private void getLocationFromNetwork() {
        //getting network status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isNetworkEnabled)
        {
            Toast.makeText(mContext, "Network Available", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(mContext, "Network not available", Toast.LENGTH_SHORT).show();
        }

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

        Log.d("Network", "Network");
        Toast.makeText(mContext, "Network Available", Toast.LENGTH_SHORT).show();

        if (locationManager != null)
        {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location!=null)
            {
                updateGPSCoordinates(location);
            }
            else
            {
                getPassiveProvideLocation();
            }

        }
    }

    public void updateGPSCoordinates(final Location location)
    {
        if (location != null)
        {
            Toast.makeText(mContext, "Location" + location.getLatitude() +" "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            if(isLocationGet==false) {
                onLocationRecieved(location, "");
            isLocationGet=true;
            }
            }
            else
        {
            Toast.makeText(mContext, "Failed to find Location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */

    public void stopUsingGPS()
    {
        if (locationManager != null)
        {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude()
    {
        if (location != null)
        {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude()
    {
        if (location != null)
        {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     */
    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }


    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context)
    {
        if (location != null)
        {
            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            try 
            {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);



                return addresses;
            } 
            catch (IOException e)
            {
                //e.printStackTrace();
                Log.e("Error : Geocoder", "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }


    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context)
    {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
Log.e("GPSLvs",address+"\n"+addressLine);
            return addressLine;
        }
        else
        {
            Log.e("GPSLvs","null");
            return null;

        }
    }

    /**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context)
    {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        }
        else
        {
            return null;
        }
    }

    /**
     * Try to get Postal Code
     * @return null or postalCode
     */
    public String getPostalCode(Context context)
    {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        }
        else
        {
            return null;
        }
    }

    /**
     * Try to get CountryName
     * @return null or postalCode
     */
    public String getCountryName(Context context)
    {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location)

    {
        updateGPSCoordinates(location);
        Toast.makeText(mContext, "Location Update "+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider)

    {
        Toast.makeText(mContext, "Disable "+provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) 
    {
        Toast.makeText(mContext, "Enable "+provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Toast.makeText(mContext, "Status Change "+provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public abstract void onLocationRecieved(Location location, String addressLine);

}