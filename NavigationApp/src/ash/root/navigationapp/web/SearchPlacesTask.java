/**
 * 
 */
package ash.root.navigationapp.web;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import ash.root.navigationapp.activity.MainActivity;
import ash.root.navigationapp.activity.RouteAddressActivity;
import ash.root.navigationapp.entity.PlaceInfo;
import ash.root.navigationapp.util.Constants;

/**
 * @author Ashwani
 * 
 */
public class SearchPlacesTask implements Constants {
   private static final String TAG = SearchPlacesTask.class.getSimpleName();
   private HttpURLConnection   conn;
   private Context             _context;
   private AsyncTask<Void, Void, StringBuilder> searchPlacesTask;

   public AsyncTask<Void, Void, StringBuilder> placeAutocompleteTask(Context context, final String searchingString, Location lastLocation) {
      try {
         _context = context;
         final StringBuilder jsonResults = new StringBuilder();
         final StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
         sb.append("?sensor=false&key=" + API_KEY);
         // sb.append("&components=country:uk");
         if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            sb.append("&location=" + latitude + "," + longitude);
            sb.append("&radius=" + RADIOUS);
            sb.append("&input=" + URLEncoder.encode(searchingString, "utf8"));
         }
         searchPlacesTask = new AsyncTask<Void, Void, StringBuilder>() {

            private StringBuilder append;

            @Override
            protected StringBuilder doInBackground(Void... params) {
               URL url;
               try {

                  url = new URL(sb.toString());
                  Log.e(TAG, "url = " + url);
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
                  // System.out.println("as" + append);

               } catch (Exception e) {
                  e.printStackTrace();
               }

               return append;
            }

            protected void onPostExecute(StringBuilder result) {
               try {
                  // Create a JSON object hierarchy from the results
                  JSONObject jsonObj = new JSONObject(result.toString());
                  JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                  // Extract the Place descriptions from the results
                  // resultList = new ArrayList<String>(predsJsonArray.length());
                  ArrayList<PlaceInfo> placesList = new ArrayList<PlaceInfo>();

                  for (int i = 0; i < predsJsonArray.length(); i++) {
                     // resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                     String place = predsJsonArray.getJSONObject(i).getString("description");
                     String reference = predsJsonArray.getJSONObject(i).getString("reference");
                     // resultList.put(place, reference);
                     PlaceInfo placeInfo = new PlaceInfo();
                     placeInfo.setPlace(place);
                     placeInfo.setRefrence(reference);
                     placesList.add(placeInfo);
                  }
                  if(_context instanceof MainActivity){
                  ((MainActivity) _context).populatePlaces(placesList);
                  }else if(_context instanceof RouteAddressActivity){
                     ((RouteAddressActivity) _context).populatePlaces(placesList);   
                  }
               } catch (JSONException e) {
                  Log.e(TAG, "Cannot process JSON results", e);
               }
               // ArrayAdapter<String> adapter = new
               // ArrayAdapter<String>(TranslateAddressActivity.this,
               // android.R.layout.simple_list_item_1, placesList);

            };
         }.execute();
      } catch (Exception e) {
         Log.e(TAG, "Error connecting to Places API", e);
      } finally {
         if (conn != null) {
            conn.disconnect();
         }
      }
      return searchPlacesTask;
   }
}
