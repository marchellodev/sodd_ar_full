package ua.int20h.sodd_warriors.ar_task;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.int20h.sodd_warriors.ar_task.network.DirectionsResponse;
import ua.int20h.sodd_warriors.ar_task.network.RetrofitInterface;
import ua.int20h.sodd_warriors.ar_task.network.model.Step;

public class MainActivity extends FlutterActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String CHANNEL = "samples.flutter.dev/battery";
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    MethodChannel.Result channelResult;
    Step[] steps;
    String from;
    String to;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {

        // Somewhere in your app, before your FlutterFragment is needed,
// like in the Application class ...
// Instantiate a FlutterEngine.
//        FlutterEngine fE = new FlutterEngine(getApplicationContext());
//
//        fE.getNavigationChannel().setInitialRoute("/cam");
//
// Start executing Dart code in the FlutterEngine.
//        fE.getDartExecutor().executeDartEntrypoint(
//                DartExecutor.DartEntrypoint.createDefault()
//        );


// Cache the pre-warmed FlutterEngine to be used later by FlutterFragment.
//        FlutterEngineCache
//                .getInstance()
//                .put("my_engine_id", fE);


        MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);

        super.configureFlutterEngine(flutterEngine);

        channel.setMethodCallHandler(
                (call, result) -> {
                    if (call.method.equals("init")) {
                        Set_googleApiClient();


                        FlutterEngine fE = new FlutterEngine(getApplicationContext());
//
                        fE.getNavigationChannel().setInitialRoute("/cam");

// Start executing Dart code in the FlutterEngine.
                        fE.getDartExecutor().executeDartEntrypoint(
                                DartExecutor.DartEntrypoint.createDefault()
                        );
//
//
// Cache the pre-warmed FlutterEngine to be used later by FlutterFragment.
                        FlutterEngineCache
                                .getInstance()
                                .put("my_engine_id", fE);


                    } else if (call.method.equals("get_from")) {
                        result.success(from);
                    } else if (call.method.equals("openAr")) {

                        Intent intent = new Intent(MainActivity.this, ArCamActivity.class);

                        try {
                            String text1 = call.argument("text1");
                            String text2 = call.argument("text2");
//                intent.putExtra("SRC", sourceResultText.getText());
//                intent.putExtra("DEST", destResultText.getText());
                            intent.putExtra("SRCLATLNG", text1);
                            intent.putExtra("DESTLATLNG", text2);
                            intent.putExtra("steps", new Gson().toJson(steps));

                            from = text1;

                            startActivity(intent);
                            result.success(123);

                        } catch (NullPointerException npe) {
                            Log.d("null act", npe.toString());
//                                    result.error("onClick: The IntentExtras are Empty", null);

                        }
                    } else if (call.method.equals("getDirections")) {
                        String text1 = call.argument("text1");
                        String text2 = call.argument("text2");
                        channelResult = result;
                        Directions_call(text1, text2);

//
//                                result.success("{123}");
                    } else {
                        result.notImplemented();
                    }
                }
        );


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


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    private void Directions_call(String origin, String dest) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.directions_base_url))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface apiService =
                retrofit.create(RetrofitInterface.class);

        final Call<DirectionsResponse> call = apiService.getDirections(origin, dest,
                getResources().getString(R.string.google_maps_key), "walking");


        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                DirectionsResponse directionsResponse = response.body();
                int step_array_size = directionsResponse.getRoutes().get(0).getLegs().get(0).getSteps().size();


                Step[] ste = new Step[step_array_size];

                for (int i = 0; i < step_array_size; i++) {
                    ste[i] = directionsResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i);
                    Log.d("ArCamActivity", "onResponse: STEP " + i + ": " + ste[i].getEndLocation().getLat()
                            + " " + ste[i].getEndLocation().getLng());
                }
                steps = ste;

                String r = "";
                for (Step s : ste) {
                    r += "|" + s.getPolyline().getPoints();
//                    Log.d("ArCamActivity", s.getPolyline().toString());
                }

                channelResult.success(r);
                Log.d("ArCamActivity", "STEPSSSSSSSSSSSSSS");
                Log.d("ArCamActivity", new Gson().toJson(ste));


            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                Log.d("ArCamActivity", "onFailure: FAIL" + t.getMessage());

            }
        });
    }

}
