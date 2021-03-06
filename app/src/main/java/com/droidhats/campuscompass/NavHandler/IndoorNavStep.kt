package com.droidhats.campuscompass.NavHandler

import com.droidhats.campuscompass.models.IndoorLocation
import com.droidhats.campuscompass.models.NavigationRoute
import com.droidhats.campuscompass.repositories.NavigationRepository

class IndoorNavStep(override val location: IndoorLocation) : NavHandler() {

    override fun getNavigationRoute() {
        when (next) {
            null -> {
                val navRoute = NavigationRoute(
                    IndoorLocation("", "", "0", "", "", 0, 0.0, 0.0),
                    location
                )
                NavigationRepository.getInstance()?.setNavigationRoute(navRoute)
            }
            is OutdoorNavStep -> {
                val navRoute = NavigationRoute(
                    location,
                    IndoorLocation("", "", "0", "", "", 0, 0.0, 0.0
                    )
                )
                NavigationRepository.getInstance()?.setNavigationRoute(navRoute)
            }
            else -> {
                NavigationRepository.getInstance()?.setNavigationRoute(NavigationRoute(location, next?.location))
            }
        }
    }
}