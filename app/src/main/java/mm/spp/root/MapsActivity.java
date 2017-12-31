package mm.spp.root;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mm.spp.R;
import mm.spp.path_agorithm.PathProvider;
import mm.spp.path_agorithm.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  private final int REQ_PERMISSION_CODE = 1;
  //project: rock-nebula-190414
  private GoogleMap mMap;
  private Button findPathB;
  private EditText startEd, destEd;
  private TextView distanceTV, timeTV;
  private ImageView distanceIV, timeIV;
  private PlaceAutocompleteFragment originPAF;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    findPathB = findViewById(R.id.findPB);
    startEd = findViewById(R.id.startET);
    destEd = findViewById(R.id.destET);
    distanceTV = findViewById(R.id.distanceTV);
    timeTV = findViewById(R.id.timeTV);
    distanceIV = findViewById(R.id.distanceIV);
    timeIV = findViewById(R.id.timeIV);

    findPathB.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        //hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(
            Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        sendRequest();
      }
    });

  }


  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    while (!checkPermission()) {
      askPermission();
    }
    mMap.setMyLocationEnabled(true);
  }

  private boolean checkPermission() {
    // Ask for permission if it wasn't granted yet
    return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED);
  }

  private void askPermission() {
    ActivityCompat.requestPermissions(
        this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        REQ_PERMISSION_CODE
    );
  }


  private void sendRequest() {

    if (!startAndDestinationSupplied()) {
      return;
    }

    String start = startEd.getText().toString();
    String dest = destEd.getText().toString();
    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("origin", start);
    parameters.put("destination", dest);

    new PathProvider(this, parameters).start();

  }

  public void findingPathStarted() {
//    progressDialog = ProgressDialog.show(this, "Please wait.",
//        "Finding direction..!", true);
  }

  public void pathFound(List<Route> routes) {
    // FIXME: 12/30/2017 : It doesn't work to dismiss the dialog...
    //progressDialog.dismiss();

    mMap.clear();

    for (int i = 0; i < routes.size(); i++) {
      Route r = routes.get(i);
      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(r.getOriginLatLng(), 17));
      PolylineOptions polylineOptions = new PolylineOptions()
          .geodesic(true)
          .color(Color.BLUE)
          .width(10);

      if (i == 0) {
        mMap.addMarker(new MarkerOptions()
            .position(r.getCoordinatesLatLng().get(0))
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

        distanceTV.setText(r.getDistanceKM() + " km");
        timeTV.setText(r.getTimeM() + " min");
      }

      for (LatLng p : r.getCoordinatesLatLng()
          ) {
        polylineOptions.add(p);
      }

      mMap.addPolyline(polylineOptions);

    }

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
