package ash.root.navigationapp.adapter;

import java.util.ArrayList;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;
import ash.root.navigationapp.R;
import ash.root.navigationapp.entity.PlaceInfo;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<PlaceInfo> implements Filterable {

   private ArrayList<PlaceInfo> placeInfoList;

   public PlacesAutoCompleteAdapter(Context context, int textViewResourceId, ArrayList<PlaceInfo> placeInfo) {
      super(context, textViewResourceId, placeInfo);
      this.placeInfoList = placeInfo;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      View view = null;
      if (view == null) {
         LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         view = inflater.inflate(R.layout.address_translation_list_item, parent, false);
      }

      PlaceInfo placeInfo = placeInfoList.get(position);
      populatePlace(view, placeInfo);

      return view;
   }

   private void populatePlace(View view, PlaceInfo placeInfo) {
      try {
         TextView placeTextView = (TextView) view.findViewById(R.id.bookmarkTextView);
         TextView placeReferenceTextView = (TextView) view.findViewById(R.id.placeReferenceTextView);
         placeTextView.setText(placeInfo.getPlace());
         placeReferenceTextView.setText(placeInfo.getRefrence());

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   @Override
   public int getCount() {
      return placeInfoList.size();
   }

   @Override
   public PlaceInfo getItem(int index) {
      return placeInfoList.get(index);
   }

   @SuppressWarnings("unused")
   @Override
   public android.widget.Filter getFilter() {

      Filter filter = new Filter() {

         @Override
         public boolean isLoggable(LogRecord record) {
            return false;
         }

      };
      return super.getFilter();
   }
   // @Override
   // public Filter getFilter() {
   // Filter filter = new Filter() {
   // @Override
   // protected FilterResults performFiltering(CharSequence constraint) {
   // FilterResults filterResults = new FilterResults();
   // if (constraint != null) {
   // // Retrieve the autocomplete results.
   // placesList = autocomplete(constraint.toString());
   //
   // // Assign the data to the FilterResults
   // filterResults.values = placesList;
   // filterResults.count = placesList.size();
   // }
   // return filterResults;
   // }
   //
   // @Override
   // protected void publishResults(CharSequence constraint, FilterResults results) {
   // if (results != null && results.count > 0) {
   // notifyDataSetChanged();
   // } else {
   // notifyDataSetInvalidated();
   // }
   // }
   // };
   // return filter;
   // }
}
