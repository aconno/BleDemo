package de.troido.measurementloader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MeasurementUploaderService extends IntentService {

    private static final String EXTRA_PREFIX = "de.troido.measurementloader.extra.";

    public static final String EXTRA_TEMPERATURE = EXTRA_PREFIX + "TEMPERATURE";
    public static final String EXTRA_BUTTON_1 = EXTRA_PREFIX + "BUTTON_1";
    public static final String EXTRA_BUTTON_2 = EXTRA_PREFIX + "BUTTON_2";
    public static final String EXTRA_ILLUMINATION = EXTRA_PREFIX + "ILLUMINATION";
    public static final String EXTRA_POTENTIOMETER = EXTRA_PREFIX + "POTENTIOMETER";

    private static SyncApi syncApi = null;

    public MeasurementUploaderService() {
        super("MeasurementUploaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            SyncApi api = getApi();

            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                double temperature = bundle.getDouble(EXTRA_TEMPERATURE);
                boolean button1 = bundle.getBoolean(EXTRA_BUTTON_1);
                boolean button2 = bundle.getBoolean(EXTRA_BUTTON_2);
                int illumination = bundle.getInt(EXTRA_ILLUMINATION);
                int potentiometer = bundle.getInt(EXTRA_POTENTIOMETER);

                Measurement measurement = new Measurement();

                measurement.setTemperature(temperature);
                measurement.setButton1(button1);
                measurement.setButton2(button2);
                measurement.setIllumination(illumination);
                measurement.setPotentiometer(potentiometer);

                Call<JsonObject> call = api.syncMeasurement("data/", measurement);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call,
                                           retrofit2.Response<JsonObject> response) {
                        Log.e("OnResponse", response.toString());
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("OnError", t.toString());
                    }
                });
            }

        }
    }

    private static SyncApi getApi() {
        if (syncApi == null) {
            synchronized (MeasurementUploaderService.class) {
                if (syncApi == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://your_domain_goes_here/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    syncApi = retrofit.create(SyncApi.class);
                }
            }
        }

        return syncApi;
    }

}
