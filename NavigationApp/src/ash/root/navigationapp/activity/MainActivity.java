/**
 * 
 */
package ash.root.navigationapp.activity;

import java.util.ArrayList;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import ash.root.navigationapp.NavigationApplication;
import ash.root.navigationapp.R;
import ash.root.navigationapp.adapter.PlacesAutoCompleteAdapter;
import ash.root.navigationapp.entity.PlaceInfo;
import ash.root.navigationapp.util.Constants;
import ash.root.navigationapp.util.Util;
import ash.root.navigationapp.web.AddressCoordinateTask;
import ash.root.navigationapp.web.RouteCordinateHandler;
import ash.root.navigationapp.web.SearchPlacesTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * @author Ashwani
 * 
 */
public class MainActivity extends CommonBaseActivity implements Constants {
   private static final String                    TAG = SearchPlacesTask.class.getSimpleName();
   protected ArrayList<PlaceInfo>                 placesList;
   private SearchView                             searchView;
   private EditText                               searchEditText;

   private MapFragment                            mapFragment;
   private PlacesAutoCompleteAdapter              placesAutoCompleteAdapter;
   private ListView                               placesListView;
   private LatLng                                 startLatLng;
   private LatLng                                 endLatLng;
   protected AsyncTask<Void, Void, StringBuilder> addressLocationTask;
   private ArrayList<LatLng>                      list_of_geoPoints;
   private Location                               startAddressLocation;
   private Location                               destAddressLocation;
   private AsyncTask<Void, Void, Void>            routeCoordinateTask;
   private ProgressDialog                         progressDialog;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.init(R.layout.activity_main, savedInstanceState);
      getOverflowMenu();
      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowCustomEnabled(true);
      LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflator.inflate(R.layout.my_search_view, null);

      actionBar.setCustomView(view);
      placesListView = (ListView) findViewById(R.id.placesListView);
      searchView = (SearchView) view.findViewById(R.id.searchEditText);
      int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
      searchEditText = (EditText) searchView.findViewById(id);
      int searchCloseButtonId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
      ImageView closeButton = (ImageView) this.searchView.findViewById(searchCloseButtonId);
      searchView.setIconifiedByDefault(false);
      searchEditText.setTextColor(getResources().getColor(R.color.white));

      placesListView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            PlaceInfo placeInfo = placesList.get(position);
            searchEditText.setText(placeInfo.getPlace());
            if (addressLocationTask == null) {
               addressLocationTask = new AddressCoordinateTask().getLocationFromAddress(MainActivity.this, placeInfo.getRefrence(),
                        START_TAG);
            } else if (addressLocationTask.getStatus() == Status.FINISHED) {
               addressLocationTask = new AddressCoordinateTask().getLocationFromAddress(MainActivity.this, placeInfo.getRefrence(),
                        START_TAG);
            }
         }
      });

      searchEditText.addTextChangedListener(new TextWatcher() {

         @Override
         public void onTextChanged(CharSequence searchingString, int start, int before, int count) {
            if (searchingString.toString().equals("")) {
               hidePlacesListView();
            } else {
               searchPlaceListOnTextSubmit(searchingString.toString());
            }
         }

         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            System.out.println("");
         }

         @Override
         public void afterTextChanged(Editable editText) {
            if (editText.toString().equals("")) {
               hidePlacesListView();
            }
         }
      });

      searchEditText.setOnEditorActionListener(new OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               searchPlaceListOnTextSubmit(textView.getText().toString());
            }
            return false;
         }
      });

      // Set on click listener
      closeButton.setOnClickListener(new View.OnClickListener() {

         @Override
         public void onClick(View v) {
            searchEditText.setText("");
            // Clear query
            searchView.setQuery("", false);
            // Collapse the action view
            searchView.onActionViewCollapsed();
            // Collapse the search widget
            // mSearchMenu.collapseActionView();
            hidePlacesListView();
         }
      });
   }

   @Override
   protected void onResume() {
      super.onResume();
      checkGooglePlayServiceStatus();
      setCurrentPositionOnMap();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {

         case R.id.root:
            Intent intent = new Intent(MainActivity.this, RouteAddressActivity.class);
            startActivityForResult(intent, 1001);
            return true;
         case R.id.action_refresh:

            return true;
         case R.id.action_help:
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
      if (requestCode == Constants.REQUEST_CODE && resultCode == Constants.RESPONSE_CODE) {
         startAddressLocation = intent.getExtras().getParcelable("startLoc");
         destAddressLocation = intent.getExtras().getParcelable("destLoc");
         getRouteCoordinateTask();
      }
      super.onActivityResult(requestCode, resultCode, intent);
   }

   private void getRouteCoordinateTask() {
      routeCoordinateTask = new AsyncTask<Void, Void, Void>() {

         @Override
         protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "", "loading...", true);
         };

         @Override
         protected void onPostExecute(Void result) {
            if (progressDialog.isShowing()) {
               progressDialog.dismiss();
               progressDialog = null;
            }
         }

         @Override
         protected Void doInBackground(Void... params) {
            if (startAddressLocation != null && destAddressLocation != null) {
               RouteCordinateHandler routeCordinateHandler = new RouteCordinateHandler(MainActivity.this,
                        startAddressLocation.getLatitude(), startAddressLocation.getLongitude(), destAddressLocation.getLatitude(),
                        destAddressLocation.getLongitude());
               if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                  routeCordinateHandler.execute();
               } else {
                  routeCordinateHandler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
               }
            }
            return null;
         };
      }.execute();

   }

   public void processResponse(ArrayList<LatLng> list_of_geoPoints) {
      if (list_of_geoPoints != null && list_of_geoPoints.size() > 0) {
         drawRouteOnMap(list_of_geoPoints);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.activity_main_actions, menu);
      return true;
   }

   private void checkGooglePlayServiceStatus() {

      int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
      if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
         int requestCode = 10;
         Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
         dialog.show();

      } else { // Google Play Services are available
         // Getting reference to the SupportMapFragment of activity_main.xml
         mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapView);
         googleMap = mapFragment.getMap();
         if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
            setCurrentPositionOnMap();
            // LocationManager locationManager = (LocationManager)
            // getSystemService(LOCATION_SERVICE);
            // Criteria criteria = new Criteria();
            // String provider = locationManager.getBestProvider(criteria, true);
            // if (provider != null) {
            // Location location = locationManager.getLastKnownLocation(provider);
            // }
            startRouteNavigation();
         }

      }
   }

   private void startRouteNavigation() {
      // TODO Auto-generated method stub
      LocationListener listener = new LocationListener() {

         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
         }

         @Override
         public void onProviderEnabled(String provider) {

         }

         @Override
         public void onProviderDisabled(String provider) {

         }

         @Override
         public void onLocationChanged(Location location) {
            if (location != null) {
               // Toast.makeText(getApplicationContext(), location.getLatitude() + " = " +
               // location.getLongitude(), Toast.LENGTH_LONG)
               // .show();
               googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("my position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
               // drawMarker(location);
            }
         }
      };
   }

   private void hidePlacesListView() {
      placesListView.setVisibility(View.GONE);
   }

   private void showPlacesListView() {
      placesListView.setVisibility(View.VISIBLE);
   }

   public void setAddressCoordinate(Location location) {
      setMarkerOnSelectedAddress(location);
   }

   public void populatePlaces(ArrayList<PlaceInfo> placesList) {
      if (placesList != null) {
         this.placesList = placesList;
         placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(MainActivity.this, R.layout.address_translation_list_item,
                  placesList);
         placesListView.setAdapter(placesAutoCompleteAdapter);
         placesListView.invalidate();
         placesAutoCompleteAdapter.notifyDataSetChanged();
         placesAutoCompleteAdapter.notifyDataSetInvalidated();
         showPlacesListView();
      }
   }

   private void setCurrentPositionOnMap() {
      Location location = Util.getLocation(MainActivity.this);
      if (location != null && googleMap != null) {
         googleMap.clear();
         CameraPosition initialPosition = CameraPosition.builder().target(new LatLng(location.getLatitude(), location.getLongitude()))
                  .bearing(40.0f).tilt(10.33f).zoom(15.4231f).build();
         googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
      } else {
         showMessage(R.string.location_not_found);
      }
      hidePlacesListView();
   }

   private void setMarkerOnSelectedAddress(Location location) {
      if (location != null && googleMap != null) {
         googleMap.clear();
         LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
         googleMap.addMarker(new MarkerOptions().position(currentPosition).snippet(
                  "Lat:" + location.getLatitude() + "Lng:" + location.getLongitude()));
         CameraPosition initialPosition = CameraPosition.builder().target(new LatLng(location.getLatitude(), location.getLongitude()))
                  .bearing(40.0f).tilt(10.33f).zoom(15.4231f).build();
         googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
      } else {
         showMessage(R.string.location_not_found);
      }
      hidePlacesListView();
   }

   public void drawRouteOnMap(ArrayList<LatLng> list_of_geoPoints) {
      try {
         if (googleMap != null) {
            googleMap.clear();
            // Polyline line = googleMap.addPolyline(new PolylineOptions()
            // .add(new LatLng(28.635308, 77.224960), new LatLng(28.597062,
            // 77.348235)).width(5).color(Color.BLUE));
            // line.setVisible(true);
            PolylineOptions options = new PolylineOptions();
            googleMap.addMarker(new MarkerOptions().position(
                     new LatLng(startAddressLocation.getLatitude(), startAddressLocation.getLongitude())).icon(
                     BitmapDescriptorFactory.fromResource(R.drawable.pin_green)));
            googleMap.addMarker(new MarkerOptions().position(
                     new LatLng(destAddressLocation.getLatitude(), destAddressLocation.getLongitude())).icon(
                     BitmapDescriptorFactory.fromResource(R.drawable.pin_pink)));

            options.addAll(list_of_geoPoints);
            options.width(5);
            options.color(Color.RED);
            options.visible(true);
            options.zIndex(30);
            options.geodesic(true);
            googleMap.addPolyline(options);
            CameraPosition initialPosition = CameraPosition.builder().target(list_of_geoPoints.get(0)).bearing(40.0f).tilt(10.33f)
                     .zoom(15.4231f).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(initialPosition));
         } 
      } catch (Exception e) {
         e.printStackTrace();
      }

   }
}
