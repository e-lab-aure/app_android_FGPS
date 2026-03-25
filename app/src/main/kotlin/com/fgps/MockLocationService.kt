package com.fgps

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Service de premier plan qui injecte en continu une position GPS fictive.
 *
 * Ce service :
 * - Enregistre un fournisseur de test sur [LocationManager.GPS_PROVIDER].
 * - Pousse une nouvelle localisation toutes les [INTERVAL_MS] millisecondes.
 * - Affiche une notification persistante indiquant les coordonnees actives.
 *
 * Prerequis : l'application doit etre selectionnee comme "Application de position fictive"
 * dans les Options developpeur de l'appareil.
 */
class MockLocationService : Service() {

    private lateinit var locationManager: LocationManager
    private val handler = Handler(Looper.getMainLooper())

    /** Tache repetee periodiquement pour injecter la position courante. */
    private val ticker = object : Runnable {
        override fun run() {
            pushLocation()
            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    // ── Cycle de vie du service ────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Demarrage du service de simulation GPS")
        isRunning = true
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        addMockProvider()
        handler.post(ticker)
        // Rafraichissement du widget apres que isRunning est a true
        refreshWidget()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Arret du service de simulation GPS")
        isRunning = false
        handler.removeCallbacks(ticker)
        removeMockProvider()
        refreshWidget()
    }

    /** Ce service ne supporte pas la liaison (bind). */
    override fun onBind(intent: Intent?) = null

    // ── Gestion du fournisseur de localisation fictive ─────────────────────

    /**
     * Enregistre un fournisseur de test GPS et l'active.
     * Journalise une erreur si l'ajout echoue (permissions manquantes ou
     * application non definie comme fournisseur fictif).
     */
    private fun addMockProvider() {
        try {
            locationManager.addTestProvider(
                LocationManager.GPS_PROVIDER,
                /* requiresNetwork    */ false,
                /* requiresSatellite  */ false,
                /* requiresCell       */ false,
                /* hasMonetaryCost    */ false,
                /* supportsAltitude   */ true,
                /* supportsSpeed      */ true,
                /* supportsBearing    */ true,
                /* powerUsage         */ ProviderProperties.POWER_USAGE_LOW,
                /* accuracy           */ ProviderProperties.ACCURACY_FINE
            )
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            Log.i(TAG, "Fournisseur GPS fictif enregistre et active")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Echec d'enregistrement du fournisseur fictif : ${e.message}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission refusee pour le fournisseur fictif : ${e.message}")
        }
    }

    /**
     * Desactive et supprime le fournisseur de test GPS.
     * Les erreurs sont journalisees sans interruption du flux d'arret.
     */
    private fun removeMockProvider() {
        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            Log.i(TAG, "Fournisseur GPS fictif supprime")
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Fournisseur fictif introuvable lors de la suppression : ${e.message}")
        }
    }

    /**
     * Construit un objet [Location] a partir de la position du jour
     * et l'injecte dans le [LocationManager].
     */
    private fun pushLocation() {
        val pos = DailyPosition.get()
        val location = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = pos.latitude
            longitude = pos.longitude
            altitude = 10.0
            accuracy = 3.0f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            verticalAccuracyMeters = 3.0f
            speedAccuracyMetersPerSecond = 0.0f
            bearingAccuracyDegrees = 0.0f
        }
        try {
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
            Log.d(TAG, "Position injectee : lat=%.4f, lon=%.4f".format(pos.latitude, pos.longitude))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Echec de l'injection de position : ${e.message}")
        }
    }

    // ── Notification persistante ───────────────────────────────────────────

    /**
     * Cree le canal de notification (minSdk 26 = Android 8, toujours requis).
     * L'importance est basse pour ne pas deranger l'utilisateur.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FGPS",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Simulation de position GPS active" }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Construit la notification persistante affichant les coordonnees actives.
     *
     * @return [Notification] prete a etre passee a [startForeground].
     */
    private fun buildNotification(): Notification {
        val pos = DailyPosition.get()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FGPS actif")
            .setContentText("Position : %.4f°, %.4f°".format(pos.latitude, pos.longitude))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    /** Force la mise a jour visuelle du widget apres l'arret du service. */
    private fun refreshWidget() {
        val mgr = AppWidgetManager.getInstance(this)
        val ids = mgr.getAppWidgetIds(ComponentName(this, GpsWidgetProvider::class.java))
        GpsWidgetProvider.updateAll(this, mgr, ids)
    }

    // ── Companion ──────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "FGPS.MockLocationService"
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "fgps_channel"
        private const val INTERVAL_MS = 1_000L

        /**
         * Indique si le service est actuellement en cours d'execution.
         * Mis a jour dans [onCreate] et [onDestroy].
         */
        var isRunning = false
            private set
    }
}
