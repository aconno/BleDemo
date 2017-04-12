package de.troido.bledemo.epd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.SourceData;
import com.journeyapps.barcodescanner.camera.PreviewCallback;
import de.troido.bledemo.R;
import de.troido.bledemo.epd.bits.BitArray;
import de.troido.bledemo.epd.conversion.BWConversion;
import kotlin.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class EpdCameraActivity extends AppCompatActivity implements BarcodeCallback {

    // Camera permission callback
    private static final int EMPTY_RESULT = 0;

    @Bind(R.id.root)
    ViewGroup rootView;

    @Bind(R.id.qr_preview)
    BarcodeView barcodeView;

    @Bind(R.id.cam_permission_txt)
    TextView camPermissionText;

    @Bind(R.id.cam_permission_btn)
    Button camPermissionBtn;

    private boolean shouldOpenPermissionSettings = false;
    private boolean isReturnedFromSettings = false;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epd_camera);
        ButterKnife.bind(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enableScanner();
                    }
                });
            }
        }, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(data.getData())
                );
                Bitmap cropped = Bitmap.createScaledBitmap(bitmap, 200, 200, false);

                BitArray bw = BWConversion.convertToBW(cropped);
                Bitmap converted = BWConversion.toBitmap(bw, 200, 200);

                Intent intent = new Intent(this, EpdResultActivity.class);
                intent.putExtra(EpdResultActivity.BITS_EXTRA, bw);
                intent.putExtra(EpdResultActivity.BITMAP_EXTRA, converted);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView.isEnabled() && barcodeView.isPreviewActive()) {
            barcodeView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableScanner();
    }

    void enableScanner() {
        if (!barcodeView.isEnabled()) {
            barcodeView.setEnabled(true);
        }

        barcodeView.resume();
        barcodeView.decodeSingle(this);
        camPermissionBtn.setVisibility(View.GONE);
        camPermissionText.setVisibility(View.GONE);

        int width = barcodeView.getWidth();
        int height = barcodeView.getHeight();

        boolean landscape = width > height;

        int maskWidth = landscape ? (width - height) / 2 : width;
        int maskHeight = landscape ? height : (height - width) / 2;

        Log.d("DIMS", maskWidth + "x" + maskHeight);

        LinearLayout mask1 = new LinearLayout(this);
        View mask2 = new View(this);

        mask1.setLayoutParams(new FrameLayout.LayoutParams(
                maskWidth,
                maskHeight,
                landscape ? Gravity.RIGHT : Gravity.BOTTOM
        ));
        mask2.setLayoutParams(new FrameLayout.LayoutParams(
                maskWidth,
                maskHeight,
                landscape ? Gravity.LEFT : Gravity.TOP
        ));

        mask1.setWeightSum(StaticPics.PICS.size() + 2);
        mask1.setOrientation(landscape ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                landscape ? ViewGroup.LayoutParams.MATCH_PARENT : 0,
                landscape ? 0 : ViewGroup.LayoutParams.MATCH_PARENT,
                1
        );

        for (final Pair<Bitmap, BitArray> pic : StaticPics.PICS) {
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(pic.getFirst());
            imageView.setPadding(8, 8, 8, 8);
            imageView.setLayoutParams(params);

            final Bitmap bitmap = pic.getFirst();
            final BitArray bits = pic.getSecond();

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("BT", "SENDSTATIC");
                    sendStatic(bitmap, bits);
                }
            });

            mask1.addView(imageView);
        }

        ImageView galleryIcon = new ImageView(this);
        galleryIcon.setImageResource(R.drawable.ic_perm_media_white_24dp);
        galleryIcon.setPadding(48, 48, 48, 48);
        galleryIcon.setLayoutParams(params);
        galleryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        ),
                        0
                );
            }
        });

        mask1.addView(galleryIcon);

        ImageView cameraIcon = new ImageView(this);
        cameraIcon.setImageResource(R.drawable.ic_camera_alt_white_24dp);
        cameraIcon.setPadding(48, 48, 48, 48);
        cameraIcon.setLayoutParams(params);

        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mask1.addView(cameraIcon);

        mask1.setBackgroundColor(Color.BLACK);
        mask2.setBackgroundColor(Color.BLACK);

        rootView.addView(mask1);
        rootView.addView(mask2);
    }

    private void sendStatic(Bitmap bitmap, BitArray bits) {
        Intent intent = new Intent(
                this,
                EpdResultActivity.class
        );
        intent.putExtra(EpdResultActivity.BITMAP_EXTRA, bitmap);
        intent.putExtra(EpdResultActivity.BITS_EXTRA, bits);
        startActivity(intent);
    }

    private void toggle() {
        barcodeView.getCameraInstance().requestPreview(new PreviewCallback() {
            @Override
            public void onPreview(SourceData sourceData) {
                int width = sourceData.isRotated()
                        ? sourceData.getDataWidth()
                        : sourceData.getDataHeight();

                int height = sourceData.isRotated()
                        ? sourceData.getDataHeight()
                        : sourceData.getDataWidth();

                sourceData.setCropRect(new Rect(
                        Math.max(0, (height - width) / 2),
                        Math.max(0, (width - height) / 2),
                        Math.min(height, (width + height) / 2),
                        Math.min(width, (width + height) / 2)
                ));

                Bitmap bitmap = Bitmap.createScaledBitmap(sourceData.getBitmap(), 200, 200, false);

                BitArray bw = BWConversion.convertToBW(bitmap);
                Bitmap converted = BWConversion.toBitmap(bw, 200, 200);

                try {
                    File dir = new File(
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES)
                                       .getPath()
                                       .concat("/GeneralicTestDirectory")
                    );
                    dir.mkdirs();

                    long time = System.currentTimeMillis();

                    OutputStream originalStream = new FileOutputStream(
                            dir.getPath().concat(String.format("/%d.jpg", time))
                    );
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, originalStream);

                    String path = dir.getPath().concat(String.format("/%d.bw.png", time));
                    OutputStream bwStream = new FileOutputStream(path);
                    converted.compress(Bitmap.CompressFormat.PNG, 100, bwStream);

                    Intent intent = new Intent(
                            EpdCameraActivity.this,
                            EpdResultActivity.class
                    );
                    intent.putExtra(EpdResultActivity.PATH_EXTRA, path);
                    intent.putExtra(EpdResultActivity.BITS_EXTRA, bw);
                    startActivity(intent);

                } catch (FileNotFoundException e) {
                    Log.e("CAMERA_ERROR", e.getMessage());
                }
            }
        });

    }

    @Override
    public void barcodeResult(BarcodeResult result) {}

    @Override
    public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
}
