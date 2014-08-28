/**
 * 
 */
package ash.root.navigationapp.activity;

import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import ash.root.navigationapp.R;
import ash.root.navigationapp.adapter.PlacesAutoCompleteAdapter;
import ash.root.navigationapp.entity.PlaceInfo;
import ash.root.navigationapp.util.Constants;
import ash.root.navigationapp.web.AddressCoordinateTask;
import ash.root.navigationapp.web.RouteCordinateHandler;

/**
 * @author Ashwani
 * 
 */
public class RouteAddressActivity extends CommonBaseActivity implements Constants {

   private SearchView                             startAddressSearchView;
   private SearchView                             destAddressSearchView;
   private EditText                               startAddressEditText;
   private EditText                               destAddressEditText;
   private ListView                               startDestAddListView;
   private PlacesAutoCompleteAdapter              placesAutoCompleteAdapter;
   private ArrayList<PlaceInfo>                   placesList;
   private boolean                                isStartEditTextInFocus;
   protected boolean                              isDestEditTextInFocus;
   protected AsyncTask<Void, Void, StringBuilder> startAddressLocationTask;
   protected AsyncTask<Void, Void, StringBuilder> destAddressLocationTask;
   private Location                               startAddressLocation;
   private Location                               destAddressLocation;
   private RouteCordinateHandler                  routeCordinateHandler;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.init(R.layout.activity_route_address, savedInstanceState);
      // super.onCreate(savedInstanceState);
      // setContentView(R.layout.activity_route_address);
      View startAddView = findViewById(R.id.startAddViewContainer);
      startAddressSearchView = (SearchView) startAddView.findViewById(R.id.searchEditText);
      View destAddView = findViewById(R.id.destAddViewContainer);
      destAddressSearchView = (SearchView) destAddView.findViewById(R.id.searchEditText);

      int startAddId = startAddressSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
      startAddressEditText = (EditText) startAddressSearchView.findViewById(startAddId);

      int destAddId = destAddressSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
      destAddressEditText = (EditText) destAddressSearchView.findViewById(destAddId);

      startDestAddListView = (ListView) findViewById(R.id.startDestAddrListView);

      findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View v) {
            // TODO Auto-generated method stub
            Toast.makeText(RouteAddressActivity.this, "s" + startAddressEditText.getText().toString(), Toast.LENGTH_SHORT).show();
         }
      });
      findViewById(R.id.button2).setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View v) {
            // TODO Auto-generated method stub
            Toast.makeText(RouteAddressActivity.this, "d" + destAddressEditText.getText().toString(), Toast.LENGTH_SHORT).show();
         }
      });

      startDestAddListView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            PlaceInfo placeInfo = placesList.get(position);
            if (isStartEditTextInFocus) {
               startAddressEditText.setText(placeInfo.getPlace());
               if (startAddressLocationTask == null) {
                  startAddressLocationTask = new AddressCoordinateTask().getLocationFromAddress(RouteAddressActivity.this,
                           placeInfo.getRefrence(), START_TAG);
               } else if (startAddressLocationTask.getStatus() == Status.FINISHED) {
                  startAddressLocationTask = new AddressCoordinateTask().getLocationFromAddress(RouteAddressActivity.this,
                           placeInfo.getRefrence(), START_TAG);
               }
            } else if (isDestEditTextInFocus) {
               destAddressEditText.setText(placeInfo.getPlace());
               if (destAddressLocationTask == null) {
                  destAddressLocationTask = new AddressCoordinateTask().getLocationFromAddress(RouteAddressActivity.this,
                           placeInfo.getRefrence(), DEST_TAG);
               } else if (destAddressLocationTask.getStatus() == Status.FINISHED) {
                  destAddressLocationTask = new AddressCoordinateTask().getLocationFromAddress(RouteAddressActivity.this,
                           placeInfo.getRefrence(), DEST_TAG);
               }
            }
            hidePlacesListView();
            // setAddressLocationOnMap(placeInfo.getRefrence());
         }
      });
      startAddressEditText.addTextChangedListener(new TextWatcher() {

         @Override
         public void onTextChanged(CharSequence searchingString, int start, int before, int count) {
            if (searchingString.toString().equals("")) {
               hidePlacesListView();
            } else {
               isStartEditTextInFocus = true;
               isDestEditTextInFocus = false;
               searchPlaceListOnTextSubmit(searchingString.toString());

            }
         }

         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }

         @Override
         public void afterTextChanged(Editable editText) {
            if (editText.toString().equals("")) {
               hidePlacesListView();
            }
         }
      });

      destAddressEditText.addTextChangedListener(new TextWatcher() {

         @Override
         public void onTextChanged(CharSequence searchingString, int start, int before, int count) {
            if (searchingString.toString().equals("")) {
               hidePlacesListView();
            } else {
               isStartEditTextInFocus = false;
               isDestEditTextInFocus = true;
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

      startAddressEditText.setOnEditorActionListener(new OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               searchPlaceListOnTextSubmit(textView.getText().toString());
            }
            return false;
         }
      });

      destAddressEditText.setOnEditorActionListener(new OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               searchPlaceListOnTextSubmit(textView.getText().toString());
            }
            return false;
         }
      });

   }

   public void populatePlaces(ArrayList<PlaceInfo> placesList) {
      if (placesList != null) {
         this.placesList = placesList;
         placesAutoCompleteAdapter = new PlacesAutoCompleteAdapter(RouteAddressActivity.this, R.layout.address_translation_list_item,
                  placesList);
         startDestAddListView.setAdapter(placesAutoCompleteAdapter);
         startDestAddListView.invalidate();
         placesAutoCompleteAdapter.notifyDataSetChanged();
         placesAutoCompleteAdapter.notifyDataSetInvalidated();
         showPlacesListView();
      }
   }

   private void hidePlacesListView() {
      startDestAddListView.setVisibility(View.GONE);
   }

   private void showPlacesListView() {
      startDestAddListView.setVisibility(View.VISIBLE);
   }

   @SuppressWarnings("unchecked")
   public void setAddressCoordinate(Location targetLocation, int requestTag) {
      if (requestTag == START_TAG) {
         startAddressLocation = targetLocation;
      } else {
         destAddressLocation = targetLocation;
      }

      if (!startAddressEditText.getText().toString().equals("") && !destAddressEditText.getText().toString().equals("")) {
         if (startAddressLocation != null && destAddressLocation != null) {
            
            Intent intent = new Intent();
            intent.putExtra("startLoc", startAddressLocation);
            intent.putExtra("destLoc", destAddressLocation);
            setResult(Constants.RESPONSE_CODE, intent);
            finish();
         }
      } else {
         showMessage(R.string.fill_start_dest_address);
      }
   }
}
