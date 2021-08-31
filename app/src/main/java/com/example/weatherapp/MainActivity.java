package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Spinner city_list;
    TextView date_time, location, presure, humidity, celcius, description;
    ImageView cloud_image;

    String[] city = {"Gdansk", "Warszawa", "Krakow", "Wroclaw", "Lodz", "Your Current Position"};
    String[] lat = {"54.3521", "52.2298", "50.0833", "51.1", "51.75", ""};
    String[] lon = {"18.6464", "21.0118", "19.9167", "17.0333", "19.4667", ""};

    FusedLocationProviderClient fusedLocationProviderClient;

    String appid = "86146e0ca44f81a71bd3fbf769cee7ac";

    Api api;
    Call<WeatherResponse> getWeather;
    Call<WeatherLocation> getWeatherLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        city_list = findViewById(R.id.city_list);
        date_time = findViewById(R.id.date_time);
        location = findViewById(R.id.location);
        presure = findViewById(R.id.presure);
        humidity = findViewById(R.id.humidity);
        celcius = findViewById(R.id.celcius);
        description = findViewById(R.id.description);
        cloud_image = findViewById(R.id.cloud_image);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        api = RetrofitClient.createService(Api.class);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_city, city);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_city);
        city_list.setAdapter(arrayAdapter);

        city_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 5) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                        getCurrentLocation(5);
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                    }
                } else {
                    getWeatherData(lat[i], lon[i], i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation(5);
        } else {
            Toast.makeText(getApplicationContext(), "Permisson Dennied.", Toast.LENGTH_SHORT);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(int pos) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    final Location location = task.getResult();
                    if (location != null){
                        String tmp_lat = String.valueOf(location.getLatitude());
                        String tmp_lon = String.valueOf(location.getLongitude());
                        getWeatherData(tmp_lat, tmp_lon, pos);
                    } else {
                        @SuppressLint("RestrictedApi") LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                Location location1 = locationResult.getLastLocation();
                                String tmp_lat = String.valueOf(location1.getLatitude());
                                String tmp_lon = String.valueOf(location1.getLongitude());
                                getWeatherData(tmp_lat, tmp_lon, pos);
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void getWeatherData(String lat, String lon, int pos) {
        getWeather = api.getWeather(lat, lon, appid+"");
        getWeather.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    if (pos != 5) {
                        location.setText(city[pos]+", Poland");
                        System.out.println(response.body().getTimezone());
                    } else {
                        getWeatherLocation = api.getWeatherLocation(lat, lon, appid+"");
                        getWeatherLocation.enqueue(new Callback<WeatherLocation>() {
                            @Override
                            public void onResponse(Call<WeatherLocation> call, Response<WeatherLocation> response) {
                                if (response.isSuccessful()) {
                                    location.setText(response.body().getName()+", "+response.body().getSys().getCountry());
                                } else {
                                    System.out.println("Get Weather Location Error ");
                                }
                            }

                            @Override
                            public void onFailure(Call<WeatherLocation> call, Throwable t) {
                                System.out.println("Get Weather Location Error "+t.getMessage());
                            }
                        });
                    }
                    if (response.body().getDaily().get(0).getWeather().get(0).getMain().equals("Thunderstorm")) {
                        cloud_image.setBackgroundResource(R.drawable.thunderstorm);
                    } else if (response.body().getDaily().get(0).getWeather().get(0).getMain().equals("Drizzle")){
                        cloud_image.setBackgroundResource(R.drawable.shower_rain);
                    } else if (response.body().getDaily().get(0).getWeather().get(0).getMain().equals("Rain")){
                        cloud_image.setBackgroundResource(R.drawable.rain);
                    } else if (response.body().getDaily().get(0).getWeather().get(0).getMain().equals("Snow")){
                        cloud_image.setBackgroundResource(R.drawable.snow);
                    } else if (response.body().getDaily().get(0).getWeather().get(0).getMain().equals("Mist")){
                        cloud_image.setBackgroundResource(R.drawable.mist);
                    }
                    description.setText(response.body().getDaily().get(0).getWeather().get(0).getDescription());
                    presure.setText(response.body().getDaily().get(0).getPressure()+" hPa.");
                    humidity.setText(response.body().getDaily().get(0).getHumidity()+"%");
                    celcius.setText(Math.round(response.body().getDaily().get(0).getTemp().getDay())+"\u00B0");
                } else {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}