package com.github.carlosgub.mlkitfirebase

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.github.carlosgub.mlkitfirebase.utils.FaceGraphic
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //LIstener de la camara
        mCameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {

            }

            override fun onError(cameraKitError: CameraKitError) {

            }

            //Poner codigo de la camara
            override fun onImage(cameraKitImage: CameraKitImage) {
                var bitmap = cameraKitImage.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, mCameraView.width, mCameraView.height, false)
                mCameraView.stop()
                runTextRecognition(bitmap)

            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {

            }
        })

        //Logica cuando se
        mCameraButton.setOnClickListener {
            mGraphicOverlay.clear()
            mCameraView.start()
            mCameraView.captureImage()
        }

        mChangeCamera.setOnClickListener{
            if(mCameraView.facing==CameraKit.Constants.FACING_FRONT){
                mCameraView.facing = CameraKit.Constants.FACING_BACK
            }else{
                mCameraView.facing = CameraKit.Constants.FACING_FRONT
            }
        }

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

    private fun runTextRecognition(bitmap:Bitmap){
        var options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        detector.detectInImage(image)
                .addOnSuccessListener({

                    processTextRecognitionResult(it)
                })
                .addOnFailureListener({
                    Toast.makeText(this,it.toString(),Toast.LENGTH_LONG).show()
                })
    }

    private fun processTextRecognitionResult(firebaseVisionList: List<FirebaseVisionFace>){
        if (firebaseVisionList.isEmpty()) {
            return
        }
        mGraphicOverlay.clear()
        for(i in firebaseVisionList){
            val textGraphic = FaceGraphic(mGraphicOverlay)
            textGraphic.updateFace(i,mCameraView.facing)
            mGraphicOverlay.add(textGraphic)
        }
    }
}



