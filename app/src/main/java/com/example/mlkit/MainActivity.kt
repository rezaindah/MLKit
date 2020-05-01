package com.example.mlkit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val pickPhotoRequestCode: Int = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnGallery.setOnClickListener { view ->
            pickImage()
        }
        btnCamera.setOnClickListener { view ->
            dispatchTakePictureIntent()
        }
    }
    private val REQUEST_IMAGE_CAPTURE = 320

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }


    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, pickPhotoRequestCode)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.label) {
            Toast.makeText(this, "Image Labelling", Toast.LENGTH_LONG).show()
            return true
        }
        if (id == R.id.textrec) {

            val intent = Intent(this, TextRecognition::class.java)
            this.startActivity(intent)
            Toast.makeText(this, "Text Recognition", Toast.LENGTH_LONG).show()
            return true
        }
        if (id == R.id.objek) {
            val intent = Intent(this, Objek::class.java)
            this.startActivity(intent)
            Toast.makeText(this, "Object Shape", Toast.LENGTH_LONG).show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                pickPhotoRequestCode -> {
                    val bitmap = getImageFromData(data)
                    bitmap?.apply {
                        imageView.setImageBitmap(this)
                        processImageTagging(bitmap)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(imageBitmap)
                    processImageTagging(imageBitmap)
                    }
                }
            }
        }


    private fun getImageFromData(data: Intent?): Bitmap? {
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
    }


    private fun processImageTagging(bitmap: Bitmap) {
        val visionImg = FirebaseVisionImage.fromBitmap(bitmap)
        val labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler().
            processImage(visionImg)
            .addOnSuccessListener { tags ->
                labelTag.text = tags.joinToString(" ") {
                    it.text }
            }
            .addOnFailureListener { ex ->
                Log.wtf("LAB", ex)
            }
    }
}
