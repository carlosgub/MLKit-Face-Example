package com.github.carlosgub.mlkitfirebase.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

class FaceDetectionFirebase(private val mGraphicOverlay: GraphicOverlay, context: Context) {

    private val mContext:Context = context

    /** Data de la libreria de conocimiento facial de Firebase */
    fun runFaceRecognition(bitmap: Bitmap){
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
                .addOnFailureListener{ Toast.makeText(mContext.applicationContext,it.toString(), Toast.LENGTH_LONG).show()}

        detector.close()
    }

    private fun processFaceRecognitionResult(firebaseVisionList: List<FirebaseVisionFace>){

        /** Limpiar la pantalla de lo anteriormente dibujado */
        mGraphicOverlay.clear()

        /** Comprobar que hay caras en la imagen */
        if (firebaseVisionList.isEmpty()) {
            Toast.makeText(mContext.applicationContext,"No se han detectado caras", Toast.LENGTH_SHORT).show()
            return
        }

        /** Logica para dibujar cada cara */
        for(i in firebaseVisionList){
            val textGraphic = FaceGraphic(mGraphicOverlay,true)
            textGraphic.updateFace(i,0)
            mGraphicOverlay.add(textGraphic)
        }
    }

}