package com.github.carlosgub.mlkitfirebase.presentation.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.media.ExifInterface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.github.carlosgub.mlkitfirebase.R
import com.github.carlosgub.mlkitfirebase.utils.FaceGraphic
import com.github.carlosgub.mlkitfirebase.utils.PathUtil
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import kotlinx.android.synthetic.main.activity_galeria.*
import java.io.IOException
import android.graphics.drawable.BitmapDrawable




class GaleriaActivity : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private val STORAGE_PERMISSION = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria)
        btGaleria.setOnClickListener{
            getPermission()
        }
    }

    private fun intent(){
        /** Intent hacia la galeria */
        val intentGallery = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intentGallery.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intentGallery.addCategory(Intent.CATEGORY_OPENABLE)
        intentGallery.type = "image/*"
        intentGallery.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentGallery.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intentGallery, PICK_IMAGE)

    }

    /** Obtener la imagen de la galeria que se ha seleccionado */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            if (data.data != null) {
                var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(data.data.toString()))
                bitmap =  modifyOrientation(bitmap,PathUtil.getPath(applicationContext,data.data))
                ivPhoto.setImageBitmap(bitmap)

                mGraphicOverlayMenu.setCameraInfo(ivPhoto.drawable.intrinsicWidth,ivPhoto.drawable.intrinsicHeight,0)
                runFaceRecognition((ivPhoto.drawable as BitmapDrawable).bitmap)
            } else{
                Toast.makeText(this, "You haven't picked any Image",
                        Toast.LENGTH_LONG).show()
            }

        }
    }

    /** Verificar que se tienen los permisos de Storage, si no se pide*/
    private fun getPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION)
        }else{
            intent()
        }
    }

    /** Listener si se aceptaron los permisos*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    getPermission()
                } else {
                    intent()
                }
            }
        }

    }

    /** Data de la libreria de conocimiento facial de Firebase */
    private fun runFaceRecognition(bitmap: Bitmap){
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

        detector.close()
    }

    private fun processFaceRecognitionResult(firebaseVisionList: List<FirebaseVisionFace>){

        /** Limpiar la pantalla de lo anteriormente dibujado */
        mGraphicOverlayMenu.clear()

        /** Comprobar que hay caras en la imagen */
        if (firebaseVisionList.isEmpty()) {
            Toast.makeText(this,"No se han detectado caras",Toast.LENGTH_SHORT).show()
            return
        }

        /** Logica para dibujar cada cara */
        for(i in firebaseVisionList){
            val textGraphic = FaceGraphic(mGraphicOverlayMenu,true)
            textGraphic.updateFace(i,0)
            mGraphicOverlayMenu.add(textGraphic)
        }
    }

    /** Rotar la imagen, si esta rotada en una direccion incorrecta*/
    @Throws(IOException::class)
    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap {
        val ei = ExifInterface(image_absolute_path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate(bitmap, 90f)

            ExifInterface.ORIENTATION_ROTATE_180 -> rotate(bitmap, 180f)

            ExifInterface.ORIENTATION_ROTATE_270 -> rotate(bitmap, 270f)

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(bitmap, true, false)

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(bitmap, false, true)

            else -> bitmap
        }
    }

    /** Rotar */
    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /** Flip */
    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
