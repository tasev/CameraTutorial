package com.gsixacademy.android.cameratutorial

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_select_image.setOnClickListener {
            if(requestCameraAndStoragePermission())
            showPhotoDialog()
        }

    }


    private fun showPhotoDialog() {

        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.access_to_camera)
        builder.setPositiveButton(R.string.take_photo) { a, b ->

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "NewImage")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
            startActivityForResult(intent, 2006)

        }
        builder.setNegativeButton(R.string.choose_from_gallery) { a, b ->

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, 2007)
        }
        builder.setNeutralButton(R.string.cancel) { a, b ->
            Toast.makeText(this, "cancel clicked", Toast.LENGTH_LONG).show()
        }
        builder.show()

    }

    fun requestCameraAndStoragePermission(): Boolean {
        val permissions = ArrayList<String>()

        if (!isPermissionGranted(android.Manifest.permission.CAMERA)) {
            permissions.add(android.Manifest.permission.CAMERA)
        }

        if (!isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), 2004)
            return false
        } else {
            return true
        }

    }

    fun isPermissionGranted(permission: String): Boolean {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 2004){
            if(grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                showPhotoDialog()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 2006){
                //from camera
                val bitmap = imageUri?.let { getImage(it, this) }

                if(bitmap!=null)
                image_view_preview.setImageBitmap(bitmap)

            }else if (requestCode == 2007){
                //from gallery
                val bitmap = data?.data?.let { getImage(it, this) }

                if(bitmap!=null)
                    image_view_preview.setImageBitmap(bitmap)

            }

        }
    }



    fun getImage(uri: Uri, activity: Activity): Bitmap? {
        var input = activity.contentResolver.openInputStream(uri)
        val bitmap: Bitmap?
        val selectedImagePath = getRealPathFromURI(uri, activity)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(input, Rect(), options)
        input?.close()
        var scale = 1
        while (options.outWidth / scale / 2 >= 400 && options.outHeight / scale / 2 >= 400) {
            scale *= 2
        }
        options.inSampleSize = scale
        options.inJustDecodeBounds = false
        if (selectedImagePath == null) {
            input = activity.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(input, Rect(), options)
            input?.close()
        } else {
            bitmap = rotateImageIfRequired(BitmapFactory.decodeFile(selectedImagePath, options), selectedImagePath)
        }
        return bitmap
    }

    fun getRealPathFromURI(uri: Uri, activity: Activity): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        return if (cursor == null) { // Source is Dropbox or other similar local file
            uri.path
        } else {
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
        }
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, selectedImage: String): Bitmap {
        try {
            // Detect rotation
            val rotation = getRotation(selectedImage)

            return if (rotation != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                val rotatedImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                rotatedImg
            } else {
                bitmap
            }
        } catch (e: Exception) {
            return bitmap
        }
    }

    private fun getRotation(selectedImage: String): Int {
        var rotation = 0
        try {
            val exifInterface = ExifInterface(selectedImage)
            val exifRotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            if (exifRotation != ExifInterface.ORIENTATION_UNDEFINED) {
                when (exifRotation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rotation
    }


}