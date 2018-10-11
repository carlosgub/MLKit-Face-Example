package com.github.carlosgub.mlkitfirebase.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.vision.CameraSource
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark


class FaceGraphic(overlay: GraphicOverlay,mostrarLandmarks: Boolean) : GraphicOverlay.Graphic(overlay) {

    private var facing: Int = 0

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint
    private var mostrarLandmarks = false



    @Volatile
    private var firebaseVisionFace: FirebaseVisionFace? = null

    /** Instanciar las variables que no se modificaran despues*/
    companion object {
        private val FACE_POSITION_RADIUS = 10.0f
        private val ID_TEXT_SIZE = 40.0f
        private val ID_Y_OFFSET = 50.0f
        private val ID_X_OFFSET = -50.0f
        private val BOX_STROKE_WIDTH = 5.0f
        private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.WHITE, Color.YELLOW)
        private var currentColorIndex = 0
    }

    /** Obtener el color para el cuadrado y los puntos de la cara */
    init {

        this.mostrarLandmarks = mostrarLandmarks
        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[currentColorIndex]

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor
        idPaint.textSize = ID_TEXT_SIZE

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
    }


    /**
     * Agrega las caras detectadas al Canvas
     */
    fun updateFace(face: FirebaseVisionFace, facing: Int) {
        firebaseVisionFace = face
        this.facing = facing
        postInvalidate()
    }

    /** Dibuja el cuadrado y los puntos de la cara en el Canvas  */
    override fun draw(canvas: Canvas) {
        val face = firebaseVisionFace ?: return

        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())

        drawFaceSquare(canvas,face,x,y)

        if(mostrarLandmarks) {
            canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint)
            drawText(canvas,"id: " + face.trackingId, x + ID_X_OFFSET, y + ID_Y_OFFSET)
            drawText(canvas,"happiness: " + String.format("%.2f", face.smilingProbability), x + ID_X_OFFSET * 3, y - ID_Y_OFFSET)

            /** Logica de los ojos segun la camara que se este usando */
            if (facing == CameraSource.CAMERA_FACING_FRONT) {
                drawText(canvas,"right eye: " + String.format("%.2f", face.rightEyeOpenProbability), x - ID_X_OFFSET, y)
                drawText(canvas,"left eye: " + String.format("%.2f", face.leftEyeOpenProbability), x + ID_X_OFFSET * 6, y)
            } else {
                drawText(canvas,"left eye: " + String.format("%.2f", face.leftEyeOpenProbability), x - ID_X_OFFSET, y)
                drawText(canvas,"right eye: " + String.format("%.2f", face.rightEyeOpenProbability), x + ID_X_OFFSET * 6, y)
            }
            drawLandmarksFace(canvas,face)
        }
    }

    private fun drawText(canvas: Canvas,string:String, x:Float, y:Float){
        canvas.drawText(string,x,y,idPaint)
    }

    /** Dibuja el cuadrado alrededor de la cara*/
    private fun drawFaceSquare(canvas: Canvas, face:FirebaseVisionFace, x:Float, y:Float){
        val xOffset = scaleX(face.boundingBox.width() / 2.0f)
        val yOffset = scaleY(face.boundingBox.height() / 2.0f)
        val left = x - xOffset
        val top = y - yOffset
        val right = x + xOffset
        val bottom = y + yOffset
        canvas.drawRect(left, top, right, bottom, boxPaint)
    }

    /** Dibuja en la cara los diferentes puntos que detecta la libreria de firebase */
    private fun drawLandmarksFace(canvas: Canvas,face: FirebaseVisionFace){
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.BOTTOM_MOUTH)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_MOUTH)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.LEFT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.NOSE_BASE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_CHEEK)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EAR)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_EYE)
        drawLandmarkPosition(canvas, face, FirebaseVisionFaceLandmark.RIGHT_MOUTH)
    }

    /** Dibuja los puntos */
    private fun drawLandmarkPosition(canvas: Canvas, face: FirebaseVisionFace, landmarkID: Int) {
        val landmark = face.getLandmark(landmarkID)
        if (landmark != null) {
            val point = landmark.position
            canvas.drawCircle(
                    translateX(point.x!!),
                    translateY(point.y!!),
                    10f, idPaint)
        }
    }
}
