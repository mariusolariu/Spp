package mm.spp.path_agorithm;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.google.android.gms.maps.model.LatLng;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mm.spp.root.MapsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by molariu on 12/28/2017.
 */

public class PathProvider {

  private static final String DIRECTION_URL = "https://maps.googleapis.com/maps/api/directions/json?";
  private static final String GOOGLE_DIRECTIONS_KEY = "AIzaSyBO2nU0Y8fSAG2uo8E4Glzgad6ccl60DWw";
  private MapsActivity mapsActivity;
  private HashMap<String, String> parameters;

  public PathProvider(MapsActivity mapsActivity, HashMap<String, String> parameters) {
    this.mapsActivity = mapsActivity;
    this.parameters = parameters;
  }

  public void start() {
    mapsActivity.findingPathStarted(); //launch progress bar

    String requestUrl = null;

    try {
      requestUrl = createUrl();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    new DownloadJsonTask().execute(requestUrl);
  }


  private String createUrl() throws UnsupportedEncodingException {
    StringBuilder requestUrl = new StringBuilder(
        ""); //SB not synchronized (I don't think is necessary)

    requestUrl.append(DIRECTION_URL);

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String paramNameEscaped = entry.getKey().replace(" ", "+");
      String valueStringEscaped = entry.getValue().replace(" ", "+");

      String paramName = URLEncoder.encode(paramNameEscaped, "utf-8");
      String value = URLEncoder.encode(valueStringEscaped, "utf-8");
      String option = paramName + "=" + value + "&";

      requestUrl.append(option);
    }

    requestUrl.append("key=" + GOOGLE_DIRECTIONS_KEY);

    return requestUrl.toString();
  }

  //<Params, Progress, Result>
  private class DownloadJsonTask extends AsyncTask<String, Void, String> {

    @Override
    protected void onPreExecute() {
      mapsActivity.findingPathStarted();
    }

    @Override
    protected String doInBackground(String... strings) {
      String requestUrl = strings[0];

      try {
        URL url = new URL(requestUrl);
        InputStream inputStream = url.openConnection().getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuffer stringBuffer = new StringBuffer("");

        String line;
        while ((line = bufferedReader.readLine()) != null) {
          stringBuffer.append(line + "\n");
        }

        String result = stringBuffer.toString();
        return result; //this will call onPostExecute(result)
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }


    @Override
    protected void onPostExecute(String jsonRep) {
      if (jsonRep == null) {
        return;
      }

      //parse the json
      List<Route> routes = new ArrayList<>();

      try {
        JSONObject root = new JSONObject(jsonRep);
        JSONArray routesJsonArray = root.getJSONArray("routes");

        for (int i = 0; i < routesJsonArray.length(); i++) {
          JSONObject currentRouteJsonO = routesJsonArray.getJSONObject(i);

          JSONArray legs = currentRouteJsonO.getJSONArray("legs");

          JSONObject leg = null;
          JSONObject legStartLocation = null;
          JSONObject legEndLocation = null;
          String originAddress = null;
          LatLng originLatLng = null;
          String destinationAddress = null;
          LatLng destinationLatLng = null;
          int distanceM = 0; //in meters
          int timeS = 0; //in minutes

          for (int j = 0; j < legs.length(); j++) {
            leg = legs.getJSONObject(i); // a portion of the route
            legStartLocation = leg.getJSONObject("start_location");
            legEndLocation = leg.getJSONObject("end_location");
            distanceM += leg.getJSONObject("distance").getInt("value");
            timeS += leg.getJSONObject("duration").getInt("value");

            if (j == 0) {
              originAddress = leg.getString("start_address");
              double lat = legStartLocation.getDouble("lat");
              double lng = legStartLocation.getDouble("lng");

              originLatLng = new LatLng(lat, lng);
            }

            //process other info from this leg
          }

          destinationAddress = leg.getString("end_address");
          double lng = legEndLocation.getDouble("lng");
          double lat = legEndLocation.getDouble("lat");

          destinationLatLng = new LatLng(lat, lng);

          JSONObject o_polyline = currentRouteJsonO.getJSONObject("overview_polyline");
          String encodedLatLangs = o_polyline.getString("points");
          List<LatLng> coordinates = decodePolyLine(encodedLatLangs);

          float timeM = formatFloat((float) timeS / 60);
          float distanceKM = formatFloat((float) distanceM / 1000);
          Route routeDto = new Route(originAddress, destinationAddress, originLatLng,destinationLatLng, timeM,
              distanceKM, coordinates);
          routes.add(routeDto);
        }

        mapsActivity.pathFound(routes);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    private float formatFloat(float f) {
      DecimalFormat decimalFormat = new DecimalFormat("#.0");
      f = Float.valueOf(decimalFormat.format(f));

      return f;
    }

    //TODO : obfuscate this method
    private List<LatLng> decodePolyLine(final String poly) {
      int len = poly.length();
      int index = 0;
      List<LatLng> decoded = new ArrayList<LatLng>();
      int lat = 0;
      int lng = 0;

      while (index < len) {
        int b;
        int shift = 0;
        int result = 0;
        do {
          b = poly.charAt(index++) - 63;
          result |= (b & 0x1f) << shift;
          shift += 5;
        } while (b
            >= 0x20); // the last value of encoding is always somehow less than 32 - > that's how you
        // know that you are at the end of encoding

        //step 5: if the number is negative invert the result and shift to right once, else just
        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
        lat += dlat;

        shift = 0;
        result = 0;
        do {
          b = poly.charAt(index++) - 63;
          result |= (b & 0x1f) << shift;
          shift += 5;
        } while (b >= 0x20);
        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
        lng += dlng; // why do you have to sum these up?

        decoded.add(new LatLng(
            lat / 100000d, lng / 100000d
        ));
      }

      return decoded;
    }
  }
}
