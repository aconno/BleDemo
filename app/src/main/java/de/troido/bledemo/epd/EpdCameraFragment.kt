package de.troido.bledemo.epd

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaActionSound
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import de.troido.bledemo.R
import de.troido.bledemo.epd.bits.BitArray
import de.troido.bledemo.epd.conversion.BWConversion
import kotlinx.android.synthetic.main.fragment_epd_camera.*


class EpdCameraFragment : Fragment(), BarcodeCallback {


    var cameraResultListener: CameraResultListener? = null
    var cameraIconLocal: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TAG", "ENTERED IN FRAGMENT")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_epd_camera, container, false)!!
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({ enableScanner() }, 1500)
    }

    override fun onResume() {
        super.onResume()
        qr_preview?.resume()
    }

    override fun onPause() {
        super.onPause()
        if (qr_preview!!.isEnabled && qr_preview!!.isPreviewActive) {
            qr_preview!!.pause()
        }
    }

    private fun sendStatic(bits: BitArray) {
        cameraResultListener?.onCameraResult(bits)
    }

    private fun enableScanner() {
        if (qr_preview?.isEnabled != true) {
            qr_preview?.isEnabled = true
        }

        qr_preview?.resume()
        qr_preview?.decodeSingle(this)
        cam_permission_btn?.visibility = View.GONE
        cam_permission_txt?.visibility = View.GONE

        val width = qr_preview?.width ?: 0
        val height = qr_preview?.height ?: 0

        val landscape = width > height

        val maskWidth = if (landscape) (width - height) / 2 else width
        val maskHeight = if (landscape) height else (height - width) / 2

        Log.d("DIMS", maskWidth.toString() + "x" + maskHeight)

        val mask1 = LinearLayout(context)
        val mask2 = View(context)

        mask1.layoutParams = FrameLayout.LayoutParams(
                maskWidth,
                maskHeight,
                if (landscape) Gravity.END else Gravity.BOTTOM
        )
        mask2.layoutParams = FrameLayout.LayoutParams(
                maskWidth,
                maskHeight,
                if (landscape) Gravity.START else Gravity.TOP
        )

        mask1.weightSum = (StaticPics.PICS.size + 2).toFloat()
        mask1.orientation = if (landscape) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL

        val params = LinearLayout.LayoutParams(
                if (landscape) ViewGroup.LayoutParams.MATCH_PARENT else 0,
                if (landscape) 0 else ViewGroup.LayoutParams.MATCH_PARENT,
                1f
        )

        for ((bitmap, bits) in StaticPics.PICS) {
            val imageView = ImageView(context)
            imageView.setImageBitmap(bitmap)
            imageView.setPadding(8, 8, 8, 8)
            imageView.layoutParams = params

            imageView.setOnClickListener {
                Log.d("BT", "SENDSTATIC")
                sendStatic(bits)
            }

            mask1.addView(imageView)
        }

        val galleryIcon = ImageView(context)
        galleryIcon.setImageResource(R.drawable.ic_perm_media_white_24dp)
        galleryIcon.setPadding(32, 32, 32, 23)
        galleryIcon.layoutParams = params
        galleryIcon.setOnClickListener {
            startActivityForResult(
                    Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ),
                    0
            )
        }

        mask1.addView(galleryIcon)

        val cameraIcon = ImageView(context)
        cameraIcon.setImageResource(R.drawable.ic_camera_alt_white_24dp)
        cameraIcon.setPadding(32, 32, 32, 32)
        cameraIcon.layoutParams = params
        cameraIcon.isSoundEffectsEnabled = true
        cameraIconLocal = cameraIcon


        cameraIcon.setOnClickListener { cameraClick() }

        mask1.addView(cameraIcon)

        mask1.setBackgroundColor(Color.LTGRAY)
        mask2.setBackgroundColor(Color.LTGRAY)

        root!!.addView(mask1)
        root!!.addView(mask2)
    }

    private fun cameraClick() {
        activity?.runOnUiThread {
            MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        }

        val start = System.currentTimeMillis()
        qr_preview?.cameraInstance?.requestPreview { sourceData ->
            val width = if (sourceData.isRotated)
                sourceData.dataWidth
            else
                sourceData.dataHeight

            val height = if (sourceData.isRotated)
                sourceData.dataHeight
            else
                sourceData.dataWidth

            Log.e("TIME 1", "${(System.currentTimeMillis() - start)}ms")

            sourceData.cropRect = Rect(
                    Math.max(0, (height - width) / 2),
                    Math.max(0, (width - height) / 2),
                    Math.min(height, (width + height) / 2),
                    Math.min(width, (width + height) / 2)
            )

            Log.e("TIME 2", "${(System.currentTimeMillis() - start)}ms")
            val bitmap = Bitmap.createScaledBitmap(sourceData.bitmap, 200, 200, false)
            Log.e("TIME 3", "${(System.currentTimeMillis() - start)}ms")
            val bw = BWConversion.convertToBW(bitmap)
            Log.e("TIME FINAL", "${(System.currentTimeMillis() - start)}ms")

            cameraResultListener?.onCameraResult(bw)
        }
    }

    override fun barcodeResult(result: BarcodeResult?) {}

    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                context?.let { ctx ->
                    data?.data?.let { uri ->
                        val start = System.currentTimeMillis()
                        val image = BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri))
                        Log.e("TIME 2", "${(System.currentTimeMillis() - start)}ms")
                        val bitmap = Bitmap.createScaledBitmap(image, 200, 200, false)
                        Log.e("TIME 3", "${(System.currentTimeMillis() - start)}ms")
                        val bw = BWConversion.convertToBW(bitmap)
                        Log.e("TIME FINAL", "${(System.currentTimeMillis() - start)}ms")
                        cameraResultListener?.onCameraResult(bw)
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}