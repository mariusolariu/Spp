package mm.spp.root;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mm.spp.R;
import mm.spp.path_agorithm.PathProvider;
import mm.spp.path_agorithm.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  private final int REQ_PERMISSION_CODE = 1;
  //project: rock-nebula-190414
  //ui elements
  private Button findPathB;
  private TextView distanceTV, timeTV;
  private AutoCompleteTextView startACTV, destACTV;

  //app logic
  private SharedPreferences sharedPreferences;
  private ArrayAdapter<String> adapterSuggestions;
  private List<String> suggestionsList;
  private Set<String> suggestionsSet;
  private String startAddress;
  private String destAddress;

  //google maps api
  private GoogleMap mMap;
  private Marker carMarker;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);

    findPathB = findViewById(R.id.findPB);
    startACTV = findViewById(R.id.startACTV);
    destACTV = findViewById(R.id.destACTV);
    distanceTV = findViewById(R.id.distanceTV);
    timeTV = findViewById(R.id.timeTV);

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

    suggestionsList = new ArrayList<String>();
    sharedPreferences = getPreferences(Context.MODE_PRIVATE);
    adapterSuggestions = new ArrayAdapter<String>(this,
        android.R.layout.simple_dropdown_item_1line, suggestionsList);

    startACTV.setAdapter(adapterSuggestions);
    destACTV.setAdapter(adapterSuggestions);

    loadSuggestionsFromPrefs();
  }

  private void loadSuggestionsFromPrefs() {
    suggestionsSet = sharedPreferences.getStringSet("locations", new HashSet<String>());

    //this method is called each time a new location is added
    suggestionsList.addAll(suggestionsSet);

    //notifyDataSetChanged() doesn't work to update the list of suggested words
    // adapterSuggestions.notifyDataSetChanged();

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

    startAddress = startACTV.getText().toString();
    destAddress = destACTV.getText().toString();
    HashMap<String, String> parameters = new HashMap<>();
    parameters.put("origin", startAddress);
    parameters.put("destination", destAddress);
    //parameters.put("alternatives", "true");

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
        carMarker = mMap.addMarker(new MarkerOptions()
            .position(r.getCoordinatesLatLng().get(0))
            .icon(BitmapDescriptorFactory
                .fromResource(R.drawable.car)));

        distanceTV.setText(r.getDistanceKM() + " km");
        timeTV.setText(r.getTimeM() + " min");
      }

      for (LatLng p : r.getCoordinatesLatLng()
          ) {
        polylineOptions.add(p);
      }

      mMap.addPolyline(polylineOptions);

    }

    updateSuggestionsSet();

  }

  private void updateSuggestionsSet() {

    if (!suggestionsSet.contains(startAddress) || !suggestionsSet.contains(destAddress)) {
      //persist data
      sharedPreferences.edit().remove("locations").commit();
      suggestionsSet.add(startAddress);
      suggestionsSet.add(destAddress);
      sharedPreferences.edit().putStringSet("locations", suggestionsSet).commit();

      //update the suggested list dynamically
      if (!suggestionsList.contains(startAddress)) {
        suggestionsList.add(startAddress);
      }

      if (!suggestionsList.contains(destAddress)) {
        suggestionsList.add(destAddress);
      }

      adapterSuggestions = new ArrayAdapter<String>(this,
          android.R.layout.simple_dropdown_item_1line, suggestionsList);

      startACTV.setAdapter(adapterSuggestions);
      destACTV.setAdapter(adapterSuggestions);
    }

  }


  private boolean startAndDestinationSupplied() {
    boolean valid = true;

    String start = startACTV.getText().toString();
    if (TextUtils.isEmpty(start)) {
      startACTV.setError("Required!");
      valid = false;
    } else {
      startACTV.setError(null);
    }

    String dest = destACTV.getText().toString();
    if (TextUtils.isEmpty(dest)) {
      destACTV.setError("Required!");
      valid = false;
    } else {
      //if error is null the error mesage will be cleared
      destACTV.setError(null);
    }

    return valid;
  }
}
