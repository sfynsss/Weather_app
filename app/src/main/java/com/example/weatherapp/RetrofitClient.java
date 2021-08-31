package com.example.weatherapp;

import com.example.weatherapp.UnsafeOkHttpClient;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private final static OkHttpClient client = UnsafeOkHttpClient.getUnsafeOkHttpClient();
    private static RetrofitClient mInstance;
    private static String BASE_URL = "http://192.168.1.8/bares/index.php/";
    private static Retrofit retrofit = buildRetrofit(client);

    private RetrofitClient() {

    }

    private static Retrofit buildRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static <T> T createService(Class<T> service){
        return retrofit.create(service);
    }

    public static <T> T createServiceWithUrl(Class<T> service, final String url) {

        OkHttpClient newClient = client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                Request.Builder builder = request.newBuilder();
                builder.header("Connection", "close");
                request = builder.build();
                return chain.proceed(request);
            }
        }).build();

        Retrofit newRetrofit = retrofit.newBuilder().baseUrl(url).client(newClient).build();
        return newRetrofit.create(service);
    }
}
