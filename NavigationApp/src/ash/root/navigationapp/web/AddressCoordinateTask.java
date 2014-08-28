/**
 * 
 */
package ash.root.navigationapp.web;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import ash.root.navigationapp.activity.MainActivity;
import ash.root.navigationapp.activity.RouteAddressActivity;
import ash.root.navigationapp.util.Constants;

/**
 * @author Ashwani
 * 
 */
public class AddressCoordinateTask implements Constants {
   private HttpURLConnection                    conn;
   private Context                              _context;
   private AsyncTask<Void, Void, StringBuilder> addressCoordinateTask;
   private int                                  _requestTag;

   public AsyncTask<Void, Void, StringBuilder> getLocationFromAddress(final Context context, final String placeReference,
            int requestTag) {
      _context = context;
      _requestTag = requestTag;
      final StringBuilder jsonResults = new StringBuilder();
      try {
         addressCoordinateTask = new AsyncTask<Void, Void, StringBuilder>() {
            private StringBuilder append;

            protected void onPreExecute() {
            };

            @Override
            protected StringBuilder doInBackground(Void... params) {
               String placeDetailsUrl = getPlaceDetailsUrl(placeReference);
               try {
                  URL url = new URL(placeDetailsUrl);
                  conn = (HttpURLConnection) url.openConnection();

                  InputStream inputStream = conn.getInputStream();
                  InputStreamReader in = new InputStreamReader(inputStream);
                  int read;
                  char[] buff = new char[1024];
                  while ((read = in.read(buff)) != -1) {
                     synchronized (this) {
                        append = jsonResults.append(buff, 0, read);
                     }
                  }

               } catch (Exception e) {
                  e.printStackTrace();
               }
               return append;
            }

            @Override
            protected void onPostExecute(StringBuilder result) {
               try {
                  JSONObject jsonObj = new JSONObject(result.toString());
                  Double placeLatitude = (Double) jsonObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location")
                           .get("lat");
                  Double placeLongitude = (Double) jsonObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location")
                           .get("lng");
                  Location targetLocation = new Location("");// provider name is unecessary
                  targetLocation.setLatitude(placeLatitude);// your coords of course
                  targetLocation.setLongitude(placeLongitude);
                  if (_context instanceof MainActivity) {
                     ((MainActivity) _context).setAddressCoordinate(targetLocation);
                  } else if (_context instanceof RouteAddressActivity) {
                     ((RouteAddressActivity) _context).setAddressCoordinate(targetLocation, _requestTag);
                  }
               } catch (JSONException e) {
                  e.printStackTrace();
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }.execute();

      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (conn != null) {
            conn.disconnect();
         }
      }
      return addressCoordinateTask;
   }

   private String getPlaceDetailsUrl(String placeReference) {

      // Obtain browser key from https://code.google.com/apis/console
      String key = "key=" + Constants.API_KEY;
      String reference = "reference=" + placeReference;
      String sensor = "sensor=false";
      String parameters = reference + "&" + sensor + "&" + key;
      String output = "json";
      String url = "https://maps.googleapis.com/maps/api/place/details/" + output + "?" + parameters;

      return url;
   }
}
