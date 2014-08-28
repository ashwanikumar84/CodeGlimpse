package ash.root.navigationapp.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import ash.root.navigationapp.activity.MainActivity;
import com.google.android.gms.maps.model.LatLng;

public class RouteCordinateHandler extends AsyncTask<Hashtable<String, Double>, Void, ArrayList<LatLng>> {

   private MainActivity      mainActivity;
   private double            userCurrentLatitude;
   private double            userCurrentLongitude;
   private double            opponentCurrentLatitude;
   private double            opponentCurrentLongitude;
   private ArrayList<LatLng> list_of_geoPoints = null;

   public RouteCordinateHandler(MainActivity mainActivity, double userCurrentLatitude, double userCurrentLongitude,
            double opponentCurrentLatitude, double opponentCurrentLongitude) {
      this.mainActivity = mainActivity;
      this.userCurrentLatitude = userCurrentLatitude;
      this.userCurrentLongitude = userCurrentLongitude;
      this.opponentCurrentLatitude = opponentCurrentLatitude;
      this.opponentCurrentLongitude = opponentCurrentLongitude;

   }

   @Override
   protected void onPostExecute(final java.util.ArrayList<LatLng> result) {
      this.mainActivity.runOnUiThread(new Runnable() {

         @Override
         public void run() {
             mainActivity.processResponse(result);
         }
      });

   };

   @Override
   protected ArrayList<LatLng> doInBackground(Hashtable<String, Double>... params) {
      try {
         String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + userCurrentLatitude + "," + userCurrentLongitude
                  + "&destination=" + opponentCurrentLatitude + "," + opponentCurrentLongitude + "&sensor=false&units=metric";

         HttpResponse response = null;
         DefaultHttpClient httpClient = new DefaultHttpClient();
         HttpContext localContext = new BasicHttpContext();
         HttpGet httpPost = new HttpGet(url);
         response = httpClient.execute(httpPost, localContext);
         HttpEntity entity = response.getEntity();
         InputStream is = entity.getContent();
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
         StringBuilder sb = new StringBuilder();

         sb.append(reader.readLine() + "\n");
         String line = "0";
         while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
         }
         is.close();
         reader.close();

         String jsonString = sb.toString();
         list_of_geoPoints = getParsedJson(jsonString);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return list_of_geoPoints;
   }

   private ArrayList<LatLng> getParsedJson(String json) throws JSONException {
      ArrayList<LatLng> list_of_geoPoints = null;
      JSONObject jsonObject = new JSONObject(json);
      JSONArray routeArray = jsonObject.getJSONArray("routes");
      JSONObject routes = routeArray.getJSONObject(0);
      JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
      String encodedString = overviewPolylines.getString("points");
      list_of_geoPoints = (ArrayList<LatLng>) decodePoly(encodedString);
      if (list_of_geoPoints != null) {
         return list_of_geoPoints;
      }
      return list_of_geoPoints;
   }

   private List<LatLng> decodePoly(String encoded) {

      List<LatLng> poly = new ArrayList<LatLng>();
      int index = 0, len = encoded.length();
      int lat = 0, lng = 0;

      while (index < len) {
         int b, shift = 0, result = 0;
         do {
            b = encoded.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
         } while (b >= 0x20);
         int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
         lat += dlat;

         shift = 0;
         result = 0;
         do {
            b = encoded.charAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
         } while (b >= 0x20);
         int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
         lng += dlng;
         LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
         // LatLng p = new LatLng((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) *
         // 1E6));
         poly.add(p);
      }

      return poly;
   }

}
