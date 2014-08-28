/**
 * 
 */
package ash.root.navigationapp.activity;

import java.lang.reflect.Field;
import com.google.android.gms.maps.GoogleMap;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewConfiguration;
import android.widget.Toast;
import ash.root.navigationapp.NavigationApplication;
import ash.root.navigationapp.util.Util;
import ash.root.navigationapp.web.SearchPlacesTask;

/**
 * @author Ashwani
 * @param <T>
 * 
 */
public class CommonBaseActivity extends FragmentActivity {
   private AsyncTask<Void, Void, StringBuilder> placeAutocompleteTask; ;
   public GoogleMap                             googleMap;

   public void init(int layOutId, Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(layOutId);
   }

   public void showMessage(int msgId) {
      Toast.makeText(NavigationApplication.getAppContext(), NavigationApplication.getAppContext().getResources().getString(msgId),
               Toast.LENGTH_SHORT).show();
   }

   public void searchPlaceListOnTextSubmit(String searchingString) {
      // TODO Auto-generated method stub
      if (placeAutocompleteTask == null) {
         placeAutocompleteTask = new SearchPlacesTask()
                  .placeAutocompleteTask(this, searchingString.toString(), Util.getLocation(this));
      } else if (placeAutocompleteTask.getStatus() == Status.FINISHED) {
         placeAutocompleteTask = new SearchPlacesTask()
                  .placeAutocompleteTask(this, searchingString.toString(), Util.getLocation(this));
      }
   }

   public void getOverflowMenu() {

      try {
         ViewConfiguration config = ViewConfiguration.get(this);
         Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
         if (menuKeyField != null) {
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
