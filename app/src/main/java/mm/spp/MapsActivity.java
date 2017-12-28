package mm.spp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
  //project: rock-nebula-190414
  private GoogleMap mMap;
  private Button findPathB;
  private EditText startEd, destEd;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    findPathB = (Button)findViewById(R.id.findPB);
    startEd = (EditText) findViewById(R.id.startET);
    destEd = (EditText) findViewById(R.id.destET);
    
    findPathB.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        sendRequest();
      }
    });
  }


  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Add a marker in Sydney and move the camera
    LatLng sydney = new LatLng(-34, 151);
    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
  }

  private void sendRequest() {

    if (!startAndDestinationSupplied()){
      return;
    }

    String start = startEd.getText().toString();
    String dest = destEd.getText().toString();

//    try {
//      new DirectionFinder(this, start, dest).execute();
//
//      // FIXME: 12/28/2017 Should throw UnsuportedEncodingException
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  private boolean startAndDestinationSupplied() {
    boolean valid = true;

    String start = startEd.getText().toString();
    if (TextUtils.isEmpty(start)) {
      startEd.setError("Required!");
      valid = false;
    } else {
      startEd.setError(null);
    }

    String dest = destEd.getText().toString();
    if (TextUtils.isEmpty(dest)) {
      destEd.setError("Required!");
      valid = false;
    } else {
      //if error is null the error mesage will be cleared
      destEd.setError(null);
    }

    return valid;
  }
}
