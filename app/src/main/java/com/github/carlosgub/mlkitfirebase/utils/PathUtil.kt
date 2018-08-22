package com.github.carlosgub.mlkitfirebase.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.net.URISyntaxException


class PathUtil {
    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    @Throws(URISyntaxException::class)
    fun getPath(context: Context, uris: Uri): String? {
        var result = Result(uris,null,null)
        if (DocumentsContract.isDocumentUri(context.applicationContext, result.uri)) {
            when {
                isExternalStorageDocument(result.uri) -> {
                    return doWhenUriIsExternalStorageDocument(result.uri)
                }
                isDownloadsDocument(result.uri) -> {
                    result.uri = doWhenUriIsDownloadsDocument(result.uri)
                }
                isMediaDocument(result.uri) -> {
                    result = doWhenUriIsMediaDocument(result.uri)
                }
            }
        }
        if ("content".equals(result.uri.scheme!!, ignoreCase = true)) {
            return searchPath(context,result.uri,result.selection,result.selectionArgs)
        } else if ("file".equals(result.uri.scheme!!, ignoreCase = true)) {
            return result.uri.path
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDocumentsContract(docId:String):Array<String>{
        return docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    private fun doWhenUriIsExternalStorageDocument(uri:Uri):String{
        val docId = DocumentsContract.getDocumentId(uri)
        val split = getDocumentsContract(docId)
        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
    }

    private fun doWhenUriIsDownloadsDocument(uri: Uri):Uri{
        val id = DocumentsContract.getDocumentId(uri)
        return ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
    }

    private fun doWhenUriIsMediaDocument(uri: Uri):Result{
        var uri = uri
        val docId = DocumentsContract.getDocumentId(uri)
        val split = getDocumentsContract(docId)
        val type = split[0]
        when (type) {
            "image" -> uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return Result(uri,selection,selectionArgs)
    }

    private fun searchPath(context: Context,uri:Uri,selection: String?,selectionArgs: Array<String>?) : String?{
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor?
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                val valor = cursor.getString(column_index)
                cursor.close()
                return valor
            }
        } catch (e: Exception) {
        }
        return null
    }

    data class Result(var uri: Uri, var selection: String?, var selectionArgs:Array<String>?)
}