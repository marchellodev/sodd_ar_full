package ua.int20h.sodd_warriors.ar_task;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.flutter.embedding.android.FlutterFragment;
import ua.int20h.sodd_warriors.ar_task.ar.ArFragmentSupport;
import ua.int20h.sodd_warriors.ar_task.network.model.Step;
import ua.int20h.sodd_warriors.ar_task.utils.LocationCalc;

/**
 * Created by Amal Krishnan on 27-03-2017.
 */

public class ArCamActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = "ArCamActivity";


    private String srcLatLng;
    private String destLatLng;
    private Step[] steps;

    private LocationManager locationManager;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private ArFragmentSupport arFragmentSupport;
    private World world;

    private Intent intent;


    // Define a tag String to represent the FlutterFragment within this
    // Activity's FragmentManager. This value can be whatever you'd like.
    private static final String TAG_FLUTTER_FRAGMENT = "flutter_fragment";

    // Declare a local variable to reference the FlutterFragment so that you
    // can forward calls to it later.
    private FlutterFragment flutterFragment;


    public static Drawable setTint(Drawable d, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(d);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        flutterFragment.onPostResume();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        flutterFragment.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        flutterFragment.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        flutterFragment.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );
    }

    @Override
    public void onUserLeaveHint() {
        flutterFragment.onUserLeaveHint();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        flutterFragment.onTrimMemory(level);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_camera);
        ButterKnife.bind(this);

        Set_googleApiClient(); //Sets the GoogleApiClient

        // Get a reference to the Activity's FragmentManager to add a new
        // FlutterFragment, or find an existing one.
        FragmentManager fragmentManager = getSupportFragmentManager();


        // Attempt to find an existing FlutterFragment,
        // in case this is not the first time that onCreate() was run.
        flutterFragment = (FlutterFragment) fragmentManager
                .findFragmentByTag(TAG_FLUTTER_FRAGMENT);

        // Create and attach a FlutterFragment if one does not exist.
        if (flutterFragment == null) {

            flutterFragment = FlutterFragment.withCachedEngine("my_engine_id").build();

//            Bundle b = new Bundle();
//            b.putString("test", "test");
//            flutterFragment.setArguments(b);
            //            flutterFragment = new FlutterFragment();


//            MethodChannel channel = new MethodChannel(flutterFragment);
//            channel.invokeMethod("foo", arguments, new MethodChannel.Result() {
//                @Override
//                public void success(Object o) {
//                    // this will be called with o = "some string"
//                }
//
//                @Override
//                public void error(String s, String s1, Object o) {}
//
//                @Override
//                public void notImplemented() {}
//            });

            fragmentManager
                    .beginTransaction()
                    .add(
                            R.id.fl_fragment,
                            flutterFragment,
                            TAG_FLUTTER_FRAGMENT
                    )
                    .commit();
        }

//        BlurBehindLayout bb = findViewById(R.id.blurBehindLayout);
//        bb.setViewBehind(findViewById(R.id.ar_cam_fragment));


//        Directions_call();
    }

    private void Set_googleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void Configure_AR() {
        List<List<LatLng>> polylineLatLng = new ArrayList<>();

        world = new World(getApplicationContext());

        world.setGeoPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Log.d(TAG, "Configure_AR: LOCATION" + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        world.setDefaultImage(R.drawable.ar_sphere_default);

        arFragmentSupport = (ArFragmentSupport) getSupportFragmentManager().findFragmentById(
                R.id.ar_cam_fragment);

        GeoObject[] signObjects = new GeoObject[steps.length];

        Log.d(TAG, "Configure_AR: STEP.LENGTH:" + steps.length);
        //TODO The given below is for rendering MAJOR STEPS LOCATIONS
        for (int i = 0; i < steps.length; i++) {
            polylineLatLng.add(i, PolyUtil.decode(steps[i].getPolyline().getPoints()));

            String instructions = steps[i].getHtmlInstructions();

            if (i == 0) {
                GeoObject signObject = new GeoObject(10000 + i);
                signObject.setImageResource(R.drawable.start);
                signObject.setGeoPosition(steps[i].getStartLocation().getLat(), steps[i].getStartLocation().getLng());
                world.addBeyondarObject(signObject);
                Log.d(TAG, "Configure_AR: START SIGN:" + i);
            }

            if (i == steps.length - 1) {
                GeoObject signObject = new GeoObject(10000 + i);
                signObject.setImageResource(R.drawable.stop);
                LatLng latlng = SphericalUtil.computeOffset(
                        new LatLng(steps[i].getEndLocation().getLat(), steps[i].getEndLocation().getLng()),
                        4f, SphericalUtil.computeHeading(
                                new LatLng(steps[i].getStartLocation().getLat(), steps[i].getStartLocation().getLng()),
                                new LatLng(steps[i].getEndLocation().getLat(), steps[i].getEndLocation().getLng())));
                signObject.setGeoPosition(latlng.latitude, latlng.longitude);
                world.addBeyondarObject(signObject);
                Log.d(TAG, "Configure_AR: STOP SIGN:" + i);
            }

            if (instructions.contains("right")) {
                Log.d(TAG, "Configure_AR: " + instructions);
                GeoObject signObject = new GeoObject(10000 + i);
                signObject.setImageResource(R.drawable.turn_right);
                signObject.setGeoPosition(steps[i].getStartLocation().getLat(), steps[i].getStartLocation().getLng());
                world.addBeyondarObject(signObject);
                Log.d(TAG, "Configure_AR: RIGHT SIGN:" + i);
            } else if (instructions.contains("left")) {
                Log.d(TAG, "Configure_AR: " + instructions);
                GeoObject signObject = new GeoObject(10000 + i);
                signObject.setImageResource(R.drawable.turn_left);
                signObject.setGeoPosition(steps[i].getStartLocation().getLat(), steps[i].getStartLocation().getLng());
                world.addBeyondarObject(signObject);
                Log.d(TAG, "Configure_AR: LEFT SIGN:" + i);
            }
        }

        Log.d(TAG, "DEBUG POLYLINES: " + polylineLatLng);

        int temp_polycount = 0;
        int temp_inter_polycount = 0;

        //TODO The Given below is for rendering all the LatLng in THe polylines , which is more accurate
        for (int j = 0; j < polylineLatLng.size(); j++) {
            for (int k = 0; k < polylineLatLng.get(j).size(); k++) {
//                GeoObject polyGeoObj = new GeoObject(1000 + temp_polycount++);

//                polyGeoObj.setGeoPosition(polylineLatLng.get(j).get(k).latitude,
//                        polylineLatLng.get(j).get(k).longitude);
//                polyGeoObj.setImageResource(R.drawable.ar_sphere_150x);
//                polyGeoObj.setName("arObj" + j + k);

                /*
                To fill the gaps between the Poly objects as AR Objects in the AR View , add some more
                AR Objects which are equally spaced and provide a continuous AR Object path along the route

                Haversine formula , Bearing Calculation and formula to find
                Destination point given distance and bearing from start point is used .
                 */

                try {

                    //Initialize distance of consecutive polyobjects
                    double dist = LocationCalc.haversine(polylineLatLng.get(j).get(k).latitude,
                            polylineLatLng.get(j).get(k).longitude, polylineLatLng.get(j).get(k + 1).latitude,
                            polylineLatLng.get(j).get(k + 1).longitude) * 1000;

                    //Log.d(TAG, "Configure_AR: polyLineLatLng("+j+","+k+")="+polylineLatLng.get(j).get(k).latitude+","+polylineLatLng.get(j).get(k).longitude);
                    //Log.d(TAG, "Configure_AR: polyLineLatLng("+j+","+(k+1)+")="+polylineLatLng.get(j).get(k+1).latitude+","+polylineLatLng.get(j).get(k+1).longitude);

                    //Check if distance between polyobjects is greater than twice the amount of space
                    // intended , here it is (3*2)=6 .
                    if (dist > 6) {

                        //Initialize count of ar objects to be added
                        int arObj_count = ((int) dist / 3) - 1;

                        //Log.d(TAG, "Configure_AR: Dist:" + dist + " # No of Objects: " + arObj_count + "\n --------");

                        double bearing = LocationCalc.calcBearing(polylineLatLng.get(j).get(k).latitude,
                                polylineLatLng.get(j).get(k + 1).latitude,
                                polylineLatLng.get(j).get(k).longitude,
                                polylineLatLng.get(j).get(k + 1).longitude);

                        double heading = SphericalUtil.computeHeading(new LatLng(polylineLatLng.get(j).get(k).latitude,
                                        polylineLatLng.get(j).get(k).longitude),
                                new LatLng(polylineLatLng.get(j).get(k + 1).latitude,
                                        polylineLatLng.get(j).get(k + 1).longitude));

                        LatLng tempLatLng = SphericalUtil.computeOffset(new LatLng(polylineLatLng.get(j).get(k).latitude,
                                        polylineLatLng.get(j).get(k).longitude)
                                , 3f
                                , heading);

                        //The distance to be incremented
                        double increment_dist = 3f;

                        for (int i = 0; i < arObj_count; i++) {
                            GeoObject inter_polyGeoObj = new GeoObject(5000 + temp_inter_polycount++);

                            //Store the Lat,Lng details into new LatLng Objects using the functions
                            //in LocationCalc class.
                            if (i > 0 && k < polylineLatLng.get(j).size()) {
                                increment_dist += 3f;

                                tempLatLng = SphericalUtil.computeOffset(new LatLng(polylineLatLng.get(j).get(k).latitude,
                                                polylineLatLng.get(j).get(k).longitude),
                                        increment_dist,
                                        SphericalUtil.computeHeading(new LatLng(polylineLatLng.get(j).get(k).latitude
                                                , polylineLatLng.get(j).get(k).longitude), new LatLng(
                                                polylineLatLng.get(j).get(k + 1).latitude
                                                , polylineLatLng.get(j).get(k + 1).longitude)));
                            }

                            //Set the Geoposition along with image and name
                            inter_polyGeoObj.setGeoPosition(tempLatLng.latitude, tempLatLng.longitude);
                            inter_polyGeoObj.setImageResource(R.drawable.ar_sphere_default_125x);
                            inter_polyGeoObj.setName("inter_arObj" + j + k + i);

                            //Log.d(TAG, "Configure_AR: LOC: k="+k+" "+ inter_polyGeoObj.getLatitude() + "," + inter_polyGeoObj.getLongitude());

                            //Add Intermediate ArObjects to Augmented Reality World
                            world.addBeyondarObject(inter_polyGeoObj);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Configure_AR: EXCEPTION CAUGHT:" + e.getMessage());
                }

                //Add PolyObjects as ArObjects to Augmented Reality World
//                world.addBeyondarObject(polyGeoObj);
                Log.d(TAG, "\n\n");
            }
        }

        // Send to the fragment
        arFragmentSupport.setWorld(world);
    }

    private void Get_intent() {
        if (getIntent() != null) {
            intent = getIntent();

//            srcDestText.setText(intent.getStringExtra("SRC")+" -> "+intent.getStringExtra("DEST"));
            srcLatLng = intent.getStringExtra("SRCLATLNG");
            destLatLng = intent.getStringExtra("DESTLATLNG");
            steps = (Step[]) new Gson().fromJson(intent.getStringExtra("steps"), Step[].class);

            Directions_call(); //HTTP Google Directions API Call
        }
    }

    private void Directions_call() {
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(getResources().getString(R.string.directions_base_url))
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitInterface apiService =
//                retrofit.create(RetrofitInterface.class);
//
//        final Call<DirectionsResponse> call = apiService.getDirections(srcLatLng, destLatLng,
//                getResources().getString(R.string.google_maps_key), "walking");
//
//        Log.d(TAG, "Directions_call: srclat lng:" + srcLatLng + "\n" + "destLatlng:" + destLatLng);
//
//        call.enqueue(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//
//                DirectionsResponse directionsResponse = response.body();
//                int step_array_size = directionsResponse.getRoutes().get(0).getLegs().get(0).getSteps().size();
//
////                dirDistance.setVisibility(View.VISIBLE);
////                dirDistance.setText(directionsResponse.getRoutes().get(0).getLegs().get(0)
////                        .getDistance().getText());
////
////                dirTime.setVisibility(View.VISIBLE);
////                dirTime.setText(directionsResponse.getRoutes().get(0).getLegs().get(0)
////                        .getDuration().getText());
//
//                steps = new Step[step_array_size];
//
//                for (int i = 0; i < step_array_size; i++) {
//                    steps[i] = directionsResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i);
//                    Log.d(TAG, "onResponse: STEP " + i + ": " + steps[i].getEndLocation().getLat()
//                            + " " + steps[i].getEndLocation().getLng());
//                }
//
//                Log.d(TAG, "STEPSSSSSSSSSSSSSS");
//                Log.d(TAG, Arrays.toString(steps));
        Configure_AR();

//            }
//
//            @Override
//            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//
//                Log.d(TAG, "onFailure: FAIL" + t.getMessage());
//                new AlertDialog.Builder(getApplicationContext()).setMessage("Fetch Failed").show();
//            }
//        });
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BeyondarLocationManager.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BeyondarLocationManager.enable();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.NETWORK_PROVIDER;

            // mLastLocation = locationManager.getLastKnownLocation(locationProvider);

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                try {
                    Get_intent(); //Fetch Intent Values
                } catch (Exception e) {
                    Log.d(TAG, "onCreate: Intent Error");
                }
            }
        }

        startLocationUpdates();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);

        } catch (SecurityException e) {
            Toast.makeText(this, "Location Permission not granted . Please Grant the permissions",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (world != null) {
            world.setGeoPosition(location.getLatitude(), location.getLongitude());

        }
    }
}
