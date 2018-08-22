package com.github.carlosgub.mlkitfirebase.presentation.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.github.carlosgub.mlkitfirebase.R
import com.github.carlosgub.mlkitfirebase.utils.FaceDetectionFirebase
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Mostrar la cara frontal al iniciar la app */
        mCameraView.facing = CameraKit.Constants.FACING_FRONT

        /** Listener de la camara */
        mCameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {}

            override fun onError(cameraKitError: CameraKitError) {}

            /** Poner codigo de la camara */
            override fun onImage(cameraKitImage: CameraKitImage) {
                var bitmap = cameraKitImage.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, mCameraView.width, mCameraView.height, false)
                mCameraView.stop()
                FaceDetectionFirebase(mGraphicOverlay,applicationContext).runFaceRecognition(bitmap)
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {}
        })

        /** Logica cuando se presiona el boton de tomar foto */
        mCameraButton.setOnClickListener {
            mGraphicOverlay.clear()
            mCameraView.start()
            mCameraView.captureImage()
        }

        /** Listener para cambiar de camara cuando se hace doble tab a la pantalla */
        mGraphicOverlay.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    mGraphicOverlay.clear()
                    mCameraView.start()
                    if(mCameraView.facing==CameraKit.Constants.FACING_FRONT){
                        mCameraView.facing = CameraKit.Constants.FACING_BACK
                    }else{
                        mCameraView.facing = CameraKit.Constants.FACING_FRONT
                    }
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

    override fun onResume() {
        super.onResume()
        mGraphicOverlay.clear()
        mCameraView.start()
    }

    override fun onPause() {
        mGraphicOverlay.clear()
        mCameraView.stop()
        super.onPause()
    }
}



