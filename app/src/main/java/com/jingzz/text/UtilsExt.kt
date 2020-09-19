package com.jingzz.text

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.Utils
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

val Uri.toFile: File? get() = uriToFile(Utils.getApp(), this)
fun uriToFile(context: Context, uri: Uri) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        uriToFileQ(context, uri)
    } else UriUtils.uri2File(uri)
@RequiresApi(Build.VERSION_CODES.Q)
private fun uriToFileQ(context: Context, uri: Uri): File? =
    if (uri.scheme == ContentResolver.SCHEME_FILE)
        uri.toFile()
    else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        //把文件保存到沙盒
        val contentResolver = context.contentResolver
        val displayName = run {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if(it.moveToFirst())
                    it.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                else null
            }
        }?:"${System.currentTimeMillis()}${Random.nextInt(0, 9999)}.${
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri))}"

        val ios = contentResolver.openInputStream(uri)
        if (ios != null) {
            File("${context.externalCacheDir!!.absolutePath}/$displayName")
                .apply {
                    val fos = FileOutputStream(this)
                    FileUtils.copy(ios, fos)
                    fos.close()
                    ios.close()
                }
        } else null
    } else null