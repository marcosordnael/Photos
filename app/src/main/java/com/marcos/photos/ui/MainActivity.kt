package com.marcos.photos.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.ImageRequest
import com.marcos.photos.R
import com.marcos.photos.databinding.ActivityMainBinding
import com.marcos.photos.model.PhotoJSONAPI
import com.marcos.photos.model.PhotosItem

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val photosList: MutableList<PhotosItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainTb)
        supportActionBar?.title = getString(R.string.app_name)

        PhotoJSONAPI.getInstance(this).getPhotos(
            responseListener = { photos ->
                photosList.clear()
                photosList.addAll(photos)
                val titles = photos.map { it.title }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.photosSp.adapter = adapter
                binding.photosSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        retrievePhotoImages(photosList[position])
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
                if (photosList.isNotEmpty()) retrievePhotoImages(photosList[0])
            },
            errorListener = { error ->
                Toast.makeText(this, "Error loading photos: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun retrievePhotoImages(photo: PhotosItem) {
        binding.photoIv.setImageDrawable(null)
        binding.thumbnailIv.setImageDrawable(null)

        val mainRequest = ImageRequest(
            photo.url,
            { bitmap: Bitmap? ->
                if (bitmap != null) {
                    binding.photoIv.setImageBitmap(bitmap)
                }
            },
            0, 0, null, Bitmap.Config.ARGB_8888,
            { error ->
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        )
        PhotoJSONAPI.getInstance(this).addToRequestQueue(mainRequest)

        val thumbRequest = ImageRequest(
            photo.thumbnailUrl,
            { bitmap: Bitmap? ->
                if (bitmap != null) {
                    binding.thumbnailIv.setImageBitmap(bitmap)
                }
            },
            0, 0, null, Bitmap.Config.ARGB_8888,
            { error ->
                Toast.makeText(this, "Error loading thumbnail", Toast.LENGTH_SHORT).show()
            }
        )
        PhotoJSONAPI.getInstance(this).addToRequestQueue(thumbRequest)
    }
}
