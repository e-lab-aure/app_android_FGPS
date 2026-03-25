package com.fgps

import android.util.Log
import java.time.LocalDate
import kotlin.random.Random

/** Coordonnees GPS representant la position fictive du jour. */
data class GpsPosition(val latitude: Double, val longitude: Double)

/**
 * Fournit une position GPS aleatoire mais deterministe pour la journee courante.
 *
 * Le seed est base sur le numero de jour depuis l'epoque (LocalDate.toEpochDay()),
 * ce qui garantit :
 * - Une position identique tout au long de la journee.
 * - Une nouvelle position automatiquement a chaque minuit.
 */
object DailyPosition {

    private const val TAG = "FGPS.DailyPosition"

    /**
     * Calcule et retourne la position GPS fictive du jour.
     *
     * @return [GpsPosition] avec latitude dans [-75, 75] et longitude dans [-180, 180].
     *         Les poles sont exclus pour eviter les zones geographiquement invalides.
     */
    fun get(): GpsPosition {
        val seed = LocalDate.now().toEpochDay()
        val rng = Random(seed)
        val lat = rng.nextDouble(-75.0, 75.0)
        val lon = rng.nextDouble(-180.0, 180.0)
        val position = GpsPosition(lat, lon)
        Log.d(TAG, "Position du jour calculee : lat=%.4f, lon=%.4f (seed=$seed)"
            .format(lat, lon))
        return position
    }
}
