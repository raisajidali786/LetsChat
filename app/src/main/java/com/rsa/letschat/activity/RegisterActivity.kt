package com.rsa.letschat.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.rsa.letschat.R
import com.rsa.letschat.databinding.ActivityRegisterBinding
import com.rsa.letschat.utils.ClickHandler
import com.rsa.letschat.utils.Constants.CAPTURE_IMAGE_REQUEST
import com.rsa.letschat.utils.Helper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RegisterActivity : AppCompatActivity(), ClickHandler {
    private lateinit var imageFileName: String
    private var cameraUri: Uri? = null
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var mCurrentPhotoPath: String
    private var photoFile: File? = null
    private val cameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                setPic()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


//    private fun openCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE)
//        cameraResult.launch(intent)
//    }

    private fun storeBitmap(bitmap: Bitmap) {
        val contentResolver = contentResolver
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${Date().time}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/LetsChat")
            }
        }


        val imageUri = contentResolver.insert(imageCollection, contentValues)
        imageUri?.let {
            val outputStream = contentResolver.openOutputStream(it)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream?.close()
            contentValues.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            contentResolver.update(it, contentValues, null, null)
            outputStream?.close()
        }
//        binding.pickImage.setImageURI(imageUri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.onClick = this@RegisterActivity
    }

    private fun showChoiceDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.custom_pick_image_dialog)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val camera: TextView = dialog.findViewById(R.id.camera)
        val gallery: TextView = dialog.findViewById(R.id.gallery)
        camera.setOnClickListener {
            dialog.dismiss()
            dispatchTakePictureIntent()
//            openCamera()
        }
        gallery.setOnClickListener {
            showMessage("Gallery")
            dialog.dismiss()
        }
    }

    private fun requestPermission() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    showChoiceDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.rsa.letschat.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraResult.launch(takePictureIntent)
//                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                }
            }
        }
    }

    private fun openCameraView() {
        try {
            photoFile = createImageFile()
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.rsa.letschat.android.fileprovider",
                    photoFile!!
                )
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
            }
        } catch (ex: Exception) {
            // Error occurred while creating the File
            ex.message?.let { showMessage(it) }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat(getString(R.string.date_format)).format(Date())
        imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
//            setPic()
//        } else {
//            showMessage("Request cancelled or something went wrong.")
//        }
//    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = binding.pickImage.width
        val targetH: Int = binding.pickImage.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(mCurrentPhotoPath)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)?.also { bitmap ->
            binding.pickImage.setImageBitmap(bitmap)
            storeBitmap(bitmap)
        }
    }


    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(mCurrentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun showPictureDialog1() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Choose From")
        val pictureDialogItems = arrayOf(
            "Gallery",
            "Camera"
        )
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> showMessage("gallery")
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun takePhotoFromCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                cameraUri = FileProvider.getUriForFile(
                    this,
                    "com.rsa.letschat.android.fileprovider",
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "pic_" + System.currentTimeMillis().toString() + ".jpg"
                    )
                )
            } else cameraUri = FileProvider.getUriForFile(
                this,
                "com.rsa.letschat.android.fileprovider",
                File(
                    Environment.getExternalStorageDirectory(),
                    "pic_" + System.currentTimeMillis().toString() + ".jpg"
                )
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            //                    intent.setType("image/*");
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 4
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onHandleClick(vararg objects: Any?) {
        val view = objects[0] as View
        when (view.id) {
            R.id.pickImage -> {
                requestPermission()
            }

            R.id.showPassword -> {
                binding.password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showPassword.visibility = View.GONE
                binding.hidePassword.visibility = View.VISIBLE
            }

            R.id.hidePassword -> {
                binding.password.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showPassword.visibility = View.VISIBLE
                binding.hidePassword.visibility = View.GONE
            }

            R.id.btnRegister -> {
                Helper.progressDialog(this, "Processing...")
            }
        }
    }
}