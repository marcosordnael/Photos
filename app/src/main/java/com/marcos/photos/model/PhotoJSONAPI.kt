package com.marcos.photos.model

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_OK

class PhotoJSONAPI private constructor(context: Context) {
    companion object {
        const val PHOTOS_ENDPOINT = "https://jsonplaceholder.typicode.com/photos"
        @Volatile
        private var INSTANCE: PhotoJSONAPI? = null
        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: PhotoJSONAPI(context).also { INSTANCE = it }
        }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }

    fun getPhotos(
        responseListener: Response.Listener<Photos>,
        errorListener: Response.ErrorListener
    ) {
        val request = PhotosRequest(responseListener, errorListener)
        addToRequestQueue(request)
    }

    class PhotosRequest(
        private val responseListener: Response.Listener<Photos>,
        errorListener: Response.ErrorListener
    ) : Request<Photos>(Method.GET, PHOTOS_ENDPOINT, errorListener) {
        override fun parseNetworkResponse(response: NetworkResponse?): Response<Photos> =
            if (response?.statusCode == HTTP_OK || response?.statusCode == HTTP_NOT_MODIFIED) {
                try {
                    val json = String(response.data)
                    val type = object : TypeToken<Photos>() {}.type
                    val photos: Photos = Gson().fromJson(json, type)
                    Response.success(photos, HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: Exception) {
                    Response.error(ParseError(e))
                }
            } else {
                Response.error(VolleyError())
            }

        override fun deliverResponse(response: Photos) {
            responseListener.onResponse(response)
        }
    }
}