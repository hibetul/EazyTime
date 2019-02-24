package ch.bfh.mad.eazytime.geofence.detail

interface GeoFenceFlow {

    enum class Step {
        MARKER, RADIUS, DETAIL;

        companion object {
            fun getStepByName(name: String) = valueOf(name.toUpperCase())
        }
    }

    fun setStep(step: GeoFenceFlow.Step)
    fun goToMarker()
    fun goToRadius()
    fun goToEdit()
    fun goToDetail()
    fun deleteGeoFence()
    fun saveGeoFence(geoFenceName: String)
    fun leaveGeoFenceDetail()
}