package ch.bfh.mad.eazytime.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import ch.bfh.mad.eazytime.geofence.detail.GeoFenceDetailActivity
import ch.bfh.mad.eazytime.util.PermissionHandler

open class GeoFenceBaseFragment : Fragment() {

    private val permissionFineLocation = Manifest.permission.ACCESS_FINE_LOCATION
    private var permissionFineLocationGranted: Boolean = false
    private lateinit var permissionHandler: PermissionHandler
    protected var hasResult: Boolean = false


    protected fun addGeoFence() {
        permissionFineLocationGranted = permissionHandler.checkPermission()
        // TODO replace with addFragment
        if (permissionFineLocationGranted) {
            startActivityForResult(GeoFenceDetailActivity.newIntent(requireContext()), 23)
        }
    }

    protected fun checkPermission(fragment: Fragment) {
        permissionHandler = PermissionHandler(fragment, permissionFineLocation)
        permissionFineLocationGranted = permissionHandler.checkPermission()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 23) {
            if (resultCode == Activity.RESULT_OK) {
                hasResult = true
            }
        }
    }
}