/**
 * 
 */
package ash.root.navigationapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import ash.root.navigationapp.location.LocationLibrary;

/**
 * @author Ashwani
 * 
 */
public class NavigationApplication extends Application {
   private static Context appContext;

   @Override
   public void onCreate() {
      super.onCreate();
      NavigationApplication.appContext = getApplicationContext();
      Log.d("TestApplication", "onCreate()");

      // output debug to LogCat, with tag LittleFluffyLocationLibrary
      LocationLibrary.showDebugOutput(true);

      try {
         // in most cases the following initialising code using defaults is probably sufficient:
         //
         // LocationLibrary.initialiseLibrary(getBaseContext(), "com.your.package.name");
         //
         // however for the purposes of the test app, we will request unrealistically frequent
         // location broadcasts
         // every 1 minute, and force a location update if there hasn't been one for 2 minutes.
         LocationLibrary.initialiseLibrary(getBaseContext(), 60 * 1000, 2 * 60 * 1000, "mobi.littlefluffytoys.littlefluffytestclient");
      } catch (UnsupportedOperationException ex) {
         Log.d("TestApplication", "UnsupportedOperationException thrown - the device doesn't have any location providers");
      }
   }
   
   public static Context getAppContext() {
      return appContext;
   }
}
