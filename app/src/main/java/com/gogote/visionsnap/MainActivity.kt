package com.gogote.visionsnap

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    // UI elements: ImageView to display the captured image, Button to trigger capture, and TextView for label results
    private lateinit var objectImg: ImageView
    private lateinit var captureImgBtn: Button
    private lateinit var labelText: TextView

    // ActivityResultLauncher handles camera intent and result
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    // ML Kit's Image Labeler to process the captured image and identify objects in it
    private lateinit var labeler: ImageLabeler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI elements to their respective views
        objectImg = findViewById(R.id.objectImg)
        captureImgBtn = findViewById(R.id.captureImgBtn)
        labelText = findViewById(R.id.labelText)

        // Check for Camera permission at the start
        checkCameraPermission()

        // Initialize the Image Labeler with default options
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        // Register an activity result launcher to handle the camera activity result
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Check if the camera activity was successful
            if (result.resultCode == Activity.RESULT_OK) {
                // Retrieve the captured image from the result data as a Bitmap
                val extras = result.data?.extras
                val imageBitmap = extras?.getParcelable<Bitmap>("data")

                // If the image was captured successfully, display it in the ImageView
                if (imageBitmap != null) {
                    objectImg.setImageBitmap(imageBitmap)
                    // Process the image using ML Kit to get labels
                    labelImage(imageBitmap)
                } else {
                    // If image capture failed, show an error message
                    labelText.text = "Unable to capture image"
                }
            }
        }

        // Set up a click listener for the capture button
        captureImgBtn.setOnClickListener {
            // Create an intent to open the camera
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Ensure there's a camera app that can handle the intent
            if (cameraIntent.resolveActivity(packageManager) != null) {
                // Launch the camera activity
                cameraLauncher.launch(cameraIntent)
            }
        }
    }

    // Function to process the captured image and label it using ML Kit
    private fun labelImage(bitmap: Bitmap) {
        // Create an InputImage object from the Bitmap
        val image = InputImage.fromBitmap(bitmap, 0)

        // Use the labeler to process the image
        labeler.process(image)
            .addOnSuccessListener { labels ->
                // If successful, display the labels
                displayLabels(labels)
            }
            .addOnFailureListener { e ->
                // If there's an error, show the error message
                labelText.text = "Error: ${e.message}"
            }
    }

    // Function to display the most confident label from the list of labels
    private fun displayLabels(labels: List<ImageLabel>) {
        if (labels.isNotEmpty()) {
            // Get the label with the highest confidence and display it
            val mostConfidentLabel = labels[0]
            labelText.text = mostConfidentLabel.text
        } else {
            // If no labels were found, show a message indicating that
            labelText.text = "No labels found"
        }
    }

    // Function to check if the app has Camera permission
    private fun checkCameraPermission() {
        // If Camera permission is not granted, request it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }
}
