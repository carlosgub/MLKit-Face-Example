package com.github.carlosgub.mlkitfirebase.presentation.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.github.carlosgub.mlkitfirebase.R
import com.github.carlosgub.mlkitfirebase.utils.FaceGraphic
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
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
            override fun onEvent(cameraKitEvent: CameraKitEvent) {

            }

            override fun onError(cameraKitError: CameraKitError) {

            }

            /** Poner codigo de la camara */
            override fun onImage(cameraKitImage: CameraKitImage) {
                var bitmap = cameraKitImage.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, mCameraView.width, mCameraView.height, false)
                mCameraView.stop()
                runFaceRecognition(bitmap)

            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {

            }
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

            @SuppressLint("ClickableViewAccessibility")
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

    /** Data de la libreria de conocimiento facial de Firebase */
    private fun runFaceRecognition(bitmap:Bitmap){
        /** Opciones de la libreria */
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()

        /** Obtener la imagen tomada de la camara*/
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        /** Instanciar la libreria */
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        /** Detectar caras en la imagen */
        detector.detectInImage(image)
                .addOnSuccessListener{processFaceRecognitionResult(it)}
                .addOnFailureListener{Toast.makeText(this,it.toString(),Toast.LENGTH_LONG).show()}
    }

    private fun processFaceRecognitionResult(firebaseVisionList: List<FirebaseVisionFace>){

        /** Comprobar que hay caras en la imagen */
        if (firebaseVisionList.isEmpty()) {
            Toast.makeText(this,"No se han detectado caras",Toast.LENGTH_SHORT).show()
            return
        }

        /** Limpiar la pantalla de lo anteriormente dibujado */
        mGraphicOverlay.clear()

        /** Logica para dibujar cada cara */
        for(i in firebaseVisionList){
            val textGraphic = FaceGraphic(mGraphicOverlay,true)
            textGraphic.updateFace(i,mCameraView.facing)
            mGraphicOverlay.add(textGraphic)
        }
    }
}



