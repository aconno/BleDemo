package de.troido.bledemo.epd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.troido.bleacon.util.Uuid16;
import de.troido.bledemo.R;
import de.troido.bledemo.epd.bits.BitArray;
import de.troido.bledemo.util.Threads;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public final class EpdResultActivity extends AppCompatActivity {

    public static final String BITMAP_EXTRA = "bitmap";

    public static final String PATH_EXTRA = "path";

    public static final String BITS_EXTRA = "bits";

    private static final String DEVICE_NAME = "EPD";

    private final static UUID UART_SVC_UUID = Uuid16.Companion.fromString("A000").toUuid();

    private final static UUID UART_RX_CHR_UUID = Uuid16.Companion.fromString("A001").toUuid();

    private static final int MAX_PACKET_SIZE = 20;

    private static final long DEVICE_COLLECTION_TIME = 2000L;

    /**
     * Magic delay needed to properly send the picture to mbed EPD.
     */
    private static final long MAGIC_DELAY = 10;

    @Bind(R.id.result_img)
    ImageView resultImg;

    @Bind(R.id.iv_btn_action)
    ImageButton actionButton;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    private BluetoothLeScanner scanner;
    private ScanCallback callback;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epd_result);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        String path = intent.getStringExtra(PATH_EXTRA);
        if (path != null) {
            resultImg.setImageURI(Uri.fromFile(new File(path)));
        } else {
            Bitmap bitmap = intent.getParcelableExtra(BITMAP_EXTRA);
            resultImg.setImageBitmap(bitmap);
        }

        final BitArray bs = intent.getParcelableExtra(BITS_EXTRA);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                doShit(bs);
            }
        });
    }

    @Override
    protected void onStop() {
        if (scanner != null) {
            scanner.stopScan(callback);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null) {
            scanner.stopScan(callback);
        }
    }

    private BluetoothLeScanner obtainScanner(BluetoothAdapter adapter) {
        BluetoothLeScanner sc;
        do {
            sc = adapter.getBluetoothLeScanner();
            Threads.interruptibleSleep(200);
        } while (sc == null);
        return sc;
    }

    private void doShit(final BitArray bits) {
        Log.d("BT", "doShit");

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            adapter.enable();
        }

        final AtomicBoolean done = new AtomicBoolean(false);
        final AtomicBoolean processed = new AtomicBoolean(false);

        scanner = obtainScanner(adapter);
        callback = new ScanCallback() {
            private Map<String, ScanResult> results = new HashMap<>();

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                boolean _done = done.get();
                if (_done && results.size() > 0 && !processed.getAndSet(true)) {
                    scanner.stopScan(this);
                    process();
                    return;
                }
                Log.d("RESULT", "New: " + result);
                results.put(result.getDevice().getAddress(), result);
            }

            private void process() {
                Log.d("RESULT", "All results: " + results);

                ScanResult maxResult = null;
                int maxRssi = -127;
                for (ScanResult result : results.values()) {
                    int rssi = result.getRssi();
                    if (rssi > maxRssi) {
                        maxResult = result;
                        maxRssi = rssi;
                    }
                }

                if (maxResult != null) {
                    final BluetoothDevice device = maxResult.getDevice();
                    Log.d("RESULT", "Max RSSI: " + device.toString());
                    EpdResultActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    EpdResultActivity.this,
                                    "Device found: " + device.getName(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                    gattConnect(bits, device);
                }
            }
        };

        Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();

        Log.d("BT", "Scanning!");
        scanner.startScan(
                Collections.singletonList(
                        new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build()
                ),
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build(),
                callback
        );
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        done.set(true);
                    }
                },
                DEVICE_COLLECTION_TIME
        );
    }

    private void gattConnect(final BitArray bits, BluetoothDevice device) {
        Log.d("BT", "gattConnect");
        device.connectGatt(this, false, new BluetoothGattCallback() {

            private boolean done = false;

            private final byte[] data = bits.toByteArray();

            private boolean started = false;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    EpdResultActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EpdResultActivity.this, "Connected", Toast.LENGTH_SHORT)
                                 .show();
                        }
                    });
                    Log.d("BT", "CONN!");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED && !done) {
                    Log.d("BT", "DCONN!");
                    gatt.close();
                    EpdResultActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(EpdResultActivity.this)
                                    .setTitle("Disconnected")
                                    .setMessage("Please try again.")
                                    .create()
                                    .show();
                            actionButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                if (started) {
                    return;
                }
                started = true;

                BluetoothGattService uartSvc = gatt.getService(UART_SVC_UUID);
                final BluetoothGattCharacteristic rxChr =
                        uartSvc.getCharacteristic(UART_RX_CHR_UUID);

                rxChr.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

                EpdResultActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                EpdResultActivity.this,
                                "Writing...",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final long t1 = System.nanoTime();

                        gatt.writeCharacteristic(rxChr);

                        Log.d("BT", "START!");

                        int totalPackets = (int) Math.ceil((double) data.length / MAX_PACKET_SIZE);
                        int packetsSent = 0;
                        while (packetsSent < totalPackets) {
                            rxChr.setValue(Arrays.copyOfRange(
                                    data,
                                    packetsSent * MAX_PACKET_SIZE,
                                    (packetsSent + 1) * MAX_PACKET_SIZE
                            ));

                            packetsSent++;

                            while (!gatt.writeCharacteristic(rxChr)) {}
                            Threads.interruptibleSleep(MAGIC_DELAY);
                        }

                        final long t2 = System.nanoTime();

                        EpdResultActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(
                                        EpdResultActivity.this,
                                        "Closing GATT",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

                        done = true;

                        Log.d("BT", "END!");

                        Threads.interruptibleSleep(500);
                        gatt.close();

                        EpdResultActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(EpdResultActivity.this)
                                        .setTitle("Done")
                                        .setMessage("Transfer time: " + (t2 - t1) / 1e9)
                                        .create()
                                        .show();
                                actionButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (scanner != null) {
            scanner.stopScan(callback);
        }
        super.onBackPressed();
    }
}
