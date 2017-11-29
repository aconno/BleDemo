package de.troido.measurementloader;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface SyncApi {
    @POST
    Call<JsonObject> syncMeasurement(@Url String endpoint, @Body Measurement measurement);

    //This was the index on elasticsearch used for the kibana test. Tested on version 5.5.2 of
    //elasticsearch and kibana.

    //    curl -XPUT 'http://35.158.213.46:9200/sensor' -d '
    //    {
    //        "settings" : {
    //        "refresh_interval" : "5s",
    //                "index.mapper.dynamic":false
    //    },
    //        "mappings" : {
    //        "measurement" : {
    //            "dynamic": "strict",
    //                    "properties" : {
    //                "id" : {"type":"string", "index" : "not_analyzed"},
    //                "timestamp" : {"type":"date", "index" : "not_analyzed"},
    //                "light" : {"type":"integer", "index" : "not_analyzed"},
    //                "battery" : {"type":"integer", "index" : "not_analyzed"},
    //                "temperature" : {"type":"double", "index" : "not_analyzed"},
    //                "magnetometer1" : {"type":"double", "index" : "not_analyzed"},
    //                "magnetometer2" : {"type":"double", "index" : "not_analyzed"},
    //                "magnetometer3" : {"type":"double", "index" : "not_analyzed"},
    //                "accelerometer1" : {"type":"double", "index" : "not_analyzed"},
    //                "accelerometer2" : {"type":"double", "index" : "not_analyzed"},
    //                "accelerometer3" : {"type":"double", "index" : "not_analyzed"}
    //            }
    //        }
    //    }
    //    }'
}
