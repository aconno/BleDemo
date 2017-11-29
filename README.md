BLE demo for <https://developer.mbed.org/teams/aconno-dev-team/code/ACD52832_Master_Example/>.

SensorActivity contains the code which handles the BLE readings from the beacon.

In the onResume method, the values are gotten from the sharedPreferences. This sharedPreferences
file is updated every time a new reading is gotten from the sensor.

The function updateFromIntent is called every time when you get the broadcast Intent.

updateFromIntent does the following:

1. Creates a map of keys and views. The keys are Strings defined in SensorBleService, while the
views are imported with: import kotlinx.android.synthetic.main.activity_sensor.*

This import adds all the views from the activity_sensor.xml layout. This functionality is thanks
to the kotlin-android-extensions plugin.

2. For each (key, view) pair, the function gets the serializable extra, and if extra is not null,
then it updates the view.

To get the Ble Readings, the app is using the SensorBleService class which contains a
BeaconScanner. This scanner is able to deserialize the values into the proper data types defined in
the Sensor class.

In SensorBleService, you can also find the code which saves the deserialized values into the
sharedPreferences. After persisting the values into sharedPreferences, a broadcast is sent from the
persist functions. This broadcast will trigger the UI update with the sensor values from the Intent.

The update of the views is done in the update method of the SensorCardView card.