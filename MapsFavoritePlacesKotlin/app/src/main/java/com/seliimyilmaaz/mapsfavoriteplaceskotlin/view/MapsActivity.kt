package com.seliimyilmaaz.mapsfavoriteplaceskotlin.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.R
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.databinding.ActivityMapsBinding
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.model.Place
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.roomdb.PlaceDao
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean : Boolean? = null
    private var selectedLat : Double? = null
    private var selectedLong : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var dao : PlaceDao
    private var compositeDisposable =CompositeDisposable()
    private var placeFromMain : Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sharedPreferences = this.getSharedPreferences("com.seliimyilmaaz.mapsfavoriteplaceskotlin",Context.MODE_PRIVATE)
        trackBoolean = false

        permissionCheck()
        selectedLat = 0.0
        selectedLong = 0.0

        db = Room.databaseBuilder(applicationContext, PlaceDatabase::class.java, "Places").build()

        dao = db.placeDao()
        binding.btnSave.visibility = View.INVISIBLE

    }

    fun permissionCheck(){

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->

            if (result){
                if (ContextCompat.checkSelfPermission(this.applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(lastKnownLocation != null){
                        val userLastLocation = LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation,15f))
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    mMap.isMyLocationEnabled = true
                }
            }else{
                Toast.makeText(this,"Permission Needed For Galery",Toast.LENGTH_LONG).show()
            }

        }

    }
/*
    fun longClickToMap(){

        mMap.setOnMapLongClickListener(){ latLng ->
            mMap.addMarker(MarkerOptions().position(latLng))
        }

    }*/

    fun requestPermission(){

        if (ContextCompat.checkSelfPermission(this.applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission Needed",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
            }else{
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }

        }else{
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(lastKnownLocation != null){
                val userLastLocation = LatLng(lastKnownLocation.latitude,lastKnownLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation,15f))
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)

            mMap.isMyLocationEnabled = true
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val returnValue = intent.getStringExtra("info")

        if(returnValue == "new"){
            binding.btnSave.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {

                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)

                    if(!trackBoolean!!){

                        val currentLocation = LatLng(location.latitude , location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15f))

                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }

                }
            }
            requestPermission()

        }else{

            mMap.clear()

            placeFromMain = intent.getSerializableExtra("selectedPlace") as? Place

            placeFromMain?.let {

                var oldLocation = LatLng(it.latitude,it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oldLocation,15f))
                mMap.addMarker(MarkerOptions().position(oldLocation).title(it.name))

                binding.txtName.setText(it.name)

                binding.btnSave.visibility = View.GONE
                binding.btnDelete.visibility = View.VISIBLE

            }

        }
    }

    override fun onMapLongClick(p0: LatLng) {

        mMap.clear()

        mMap.addMarker(MarkerOptions().position(p0))
        selectedLat = p0.latitude
        selectedLong = p0.longitude

        binding.btnSave.visibility = View.VISIBLE
    }

    fun delete(view: View){

        placeFromMain?.let {
            compositeDisposable.add(
                dao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    fun save(view: View){

        val place = (Place(binding.txtName.text.toString(),selectedLat!!,selectedLong!!))

        compositeDisposable.add(
            dao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )

    }

    private fun handleResponse(){

        var intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
    }
}