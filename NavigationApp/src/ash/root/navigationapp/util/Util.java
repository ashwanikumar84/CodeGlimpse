/**
 * 
 */
package ash.root.navigationapp.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import ash.root.navigationapp.location.LocationInfo;

/**
 * @author Ashwani
 * 
 */
public class Util {
   private static Location location;
   private static String   provider;

   public static Location getLocation(Context context) {

      class MyLocationListener implements LocationListener {

         @Override
         public void onLocationChanged(Location argLocation) {
            location = argLocation;
         }

         public void onProviderDisabled(String provider) {
         }

         public void onProviderEnabled(String provider) {
         }

         public void onStatusChanged(String provider, int status, Bundle extras) {
         }

      }

      LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
      Criteria criteria = new Criteria();
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
      criteria.setPowerRequirement(Criteria.POWER_LOW);
      criteria.setAltitudeRequired(false);
      criteria.setBearingRequired(false);
      criteria.setSpeedRequired(false);
      LocationListener myLocationListener = new MyLocationListener();
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
      provider = locationManager.getBestProvider(criteria, true);
      location = locationManager.getLastKnownLocation(provider);
      if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if (location == null) {
            if (provider != null && !provider.equals("")) {
               location = locationManager.getLastKnownLocation(provider);
               if (location == null) {
                  location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                  if (location == null) {
                     location = new Location(provider);
                     setLocationFromOtherSource(context);
                  }
                  return location;
               } else {
                  return location;
               }
            } else {
               location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
               return location;
            }
         } else {
            return location;
         }
      } else {
         location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      }
      setLocationFromOtherSource(context);
      return location;
   }

   private static void setLocationFromOtherSource(Context context) {
      LocationInfo locationInfo = new LocationInfo(context);
      if (location == null) {

         location = new Location(provider);
         // location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude,
         // results)
      }
      if (locationInfo.anyLocationDataReceived() && location != null) {
         location.setLatitude(locationInfo.lastLat);
         location.setLongitude(locationInfo.lastLong);
         location.setAccuracy(locationInfo.lastAccuracy);
         location.setAltitude(locationInfo.lastAltitude);
      }
   }
}
