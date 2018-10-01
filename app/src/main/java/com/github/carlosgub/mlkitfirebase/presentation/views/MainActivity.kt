package com.github.carlosgub.mlkitfirebase.presentation.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.github.carlosgub.mlkitfirebase.R
import com.github.carlosgub.mlkitfirebase.utils.FaceDetectionFirebase
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val TAG = "PermissonMain"
    private val CAMERA_REQUEST_CODE = 101
    private val LOGGING_TAG = "Fotoapparat Example"
    private var hasCameraPermission: Boolean = false
    private var activeCamera: Camera = Camera.Back

    private lateinit var fotoapparat: Fotoapparat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = false
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }else{
            hasCameraPermission = true
        }

        fotoapparat = Fotoapparat(
                context = this,
                view = mCameraView,
                logger = logcat(),
                scaleType = ScaleType.CenterCrop,
                lensPosition = activeCamera.lensPosition,
                cameraConfiguration = activeCamera.configuration,
                cameraErrorCallback = { Log.e(LOGGING_TAG, "Camera error: ", it) }
        )


        /** Logica cuando se presiona el boton de tomar foto */
        mCameraButton.setOnClickListener {
            val permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                if (ivPhoto.drawable==null){
                    pb.indeterminateDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
                    pb.visibility = View.VISIBLE
                    mCameraButton.isEnabled=false
                    val photoResult = fotoapparat.takePicture()

                    photoResult
                            .toBitmap()
                            .whenAvailable { bitmapPhoto ->
                                val bitmap = modifyOrientation(bitmapPhoto!!.bitmap,bitmapPhoto.rotationDegrees)
                                ivPhoto.setImageBitmap(bitmap)
                                mGraphicOverlay.setCameraInfo(ivPhoto.drawable.intrinsicWidth,ivPhoto.drawable.intrinsicHeight,0)
                                FaceDetectionFirebase(mGraphicOverlay).runFaceRecognition(bitmap) { callback->
                                    when(callback) {
                                        "true"->{}
                                        else->{
                                            Toast.makeText(applicationContext,callback, Toast.LENGTH_SHORT).show()
                                            mGraphicOverlay.clear()
                                            ivPhoto.setImageDrawable(null)
                                        }
                                    }
                                    pb.visibility=View.GONE
                                    mCameraButton.isEnabled=true
                                }
                            }
                }else{
                    mGraphicOverlay.clear()
                    ivPhoto.setImageDrawable(null)
                }
            }
        }

        /** Listener para cambiar de camara cuando se hace doble tab a la pantalla */
        mGraphicOverlay.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    mGraphicOverlay.clear()
                    changeCamera()
                    ivPhoto.setImageDrawable(null)
                    return super.onDoubleTap(e)
                }

                override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                    return false
                }
            })

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                return true
            }
        })

    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    makeRequest()
                } else {
                    fotoapparat.start()
                }
            }
        }
    }

    /** Rotar la imagen, si esta rotada en una direccion incorrecta*/
    @Throws(IOException::class)
    private fun modifyOrientation(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        var bitmapsito:Bitmap = if(activeCamera==Camera.Front){
             flip(bitmap,false,true)
        }else{
            bitmap
        }
        return when (rotationDegrees) {
            90 -> rotate(bitmapsito, 270f)
            180 -> rotate(bitmapsito, 180f)
            270 -> rotate(bitmapsito, 90f)
            else -> bitmapsito
        }
    }

    /** Rotar */
    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return createBitmap(bitmap,matrix,true)
    }

    /** Flip */
    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return createBitmap(bitmap,matrix,true)
    }

    private fun createBitmap(bitmap: Bitmap, matrix: Matrix, boolean: Boolean):Bitmap{
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, boolean)
    }

    override fun onResume() {
        super.onResume()
        mGraphicOverlay.clear()
        if (hasCameraPermission) {
            fotoapparat.start()
        }
    }

    override fun onPause() {
        super.onPause()
        mGraphicOverlay.clear()
        if (hasCameraPermission) {
            fotoapparat.stop()
        }
    }

    override fun onBackPressed() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 666) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mGraphicOverlay.clear()
                ivPhoto.setImageDrawable(null)
            }
        }
    }

    private fun changeCamera() {
        activeCamera = when (activeCamera) {
            Camera.Front -> Camera.Back
            Camera.Back -> Camera.Front
        }

        fotoapparat.switchTo(
                lensPosition = activeCamera.lensPosition,
                cameraConfiguration = activeCamera.configuration
        )
    }
}


private sealed class Camera(
        val lensPosition: LensPositionSelector,
        val configuration: CameraConfiguration
) {

    object Back : Camera(
            back(),
            CameraConfiguration(
                    previewResolution = firstAvailable(
                            wideRatio(highestResolution()),
                            standardRatio(highestResolution())
                    ),
                    previewFpsRange = highestFps(),
                    flashMode = off(),
                    focusMode = firstAvailable(
                            continuousFocusPicture(),
                            autoFocus()
                    )
            )
    )

    object Front : Camera(
            lensPosition = front(),
            configuration = CameraConfiguration(
                    previewResolution = firstAvailable(
                            wideRatio(highestResolution()),
                            standardRatio(highestResolution())
                    ),
                    previewFpsRange = highestFps(),
                    flashMode = off(),
                    focusMode = firstAvailable(
                            fixed(),
                            autoFocus()
                    )
            )
    )
}



