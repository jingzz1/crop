package com.jingzz.text

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.jingzz.text.databinding.ActivityMainBinding
import com.jingzz.text.ext.permission
import java.io.File

class MainActivity : AppCompatActivity() {
    val binding:ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnCamera.setOnClickListener {
            permission(Manifest.permission.CAMERA
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.READ_EXTERNAL_STORAGE){
                camera()
            }
        }
    }

    //调用摄像头
    private fun camera() {
        val mimeType = "image/jpeg"
        val fileName = "${System.currentTimeMillis()}.jpg"
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName)
            values.put(MediaStore.MediaColumns.MIME_TYPE,mimeType)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DCIM)
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        }else{
            FileProvider.getUriForFile(this,BuildConfig.authorities, File(externalCacheDir!!.absolutePath,fileName))
        }
        registerForActivityResult(ActivityResultContracts.TakePicture()){
            if(it && uri!= null)
                crop(uri)
        }.launch(uri)
    }

    //裁剪图片
    private fun crop(uri: Uri) {
        registerForActivityResult(CropImage()){
            Glide.with(this).load(it).into(binding.ivImage)
        }.launch(CropImageResult(uri,1,1))
    }
}