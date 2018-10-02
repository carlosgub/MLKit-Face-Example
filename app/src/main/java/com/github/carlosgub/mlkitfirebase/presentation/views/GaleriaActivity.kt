package com.github.carlosgub.mlkitfirebase.presentation.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.github.carlosgub.mlkitfirebase.R
import com.github.carlosgub.mlkitfirebase.utils.FaceDetectionFirebase
import com.github.carlosgub.mlkitfirebase.utils.PathUtil
import kotlinx.android.synthetic.main.activity_galeria.*
import java.io.IOException


class GaleriaActivity : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private val STORAGE_PERMISSION = 301

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galeria)
        btGaleria.setOnClickListener{
            getPermission()
        }
    }

    /** Intent hacia la galeria */
    private fun intent(){

        val intentGallery = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intentGallery.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        intentGallery.addCategory(Intent.CATEGORY_OPENABLE)
        intentGallery.type = "image/*"
        intentGallery.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentGallery.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intentGallery, PICK_IMAGE)

    }

    /** Obtener la imagen de la galeria que se ha seleccionado */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        pb.indeterminateDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
        pb.visibility = View.VISIBLE
        btGaleria.isEnabled=false
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data!!.data != null) {
                var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(data.data.toString()))
                bitmap =  modifyOrientation(bitmap,PathUtil().getPath(applicationContext,data.data)!!)
                ivPhoto.setImageBitmap(bitmap)

                mGraphicOverlayMenu.setCameraInfo(ivPhoto.drawable.intrinsicWidth,ivPhoto.drawable.intrinsicHeight,0)
                FaceDetectionFirebase(mGraphicOverlayMenu).runFaceRecognition((ivPhoto.drawable as BitmapDrawable).bitmap){ callback->
                    when(callback){
                        "true"->{}
                        else->{
                            Toast.makeText(applicationContext,callback, Toast.LENGTH_SHORT).show()
                            mGraphicOverlayMenu.clear()
                        }
                    }
                    pb.visibility= View.GONE
                    btGaleria.isEnabled=true
                }
            } else{
                Toast.makeText(this, "No haz seleccionado ninguna imagen",
                        Toast.LENGTH_LONG).show()
                pb.visibility= View.GONE
                btGaleria.isEnabled=true
            }
        }else{
            Toast.makeText(this, "No haz seleccionado ninguna imagen",
                    Toast.LENGTH_LONG).show()
            pb.visibility= View.GONE
            btGaleria.isEnabled=true
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
        return createBitmap(bitmap,matrix,true)
    }

    /** Flip */
    private fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return createBitmap(bitmap,matrix,true)
    }

    private fun createBitmap(bitmap: Bitmap,matrix:Matrix,boolean: Boolean):Bitmap{
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, boolean)
    }
}
