package com.fgps

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

/**
 * Fournisseur du widget Android permettant d'activer ou desactiver
 * la simulation GPS d'une simple pression.
 *
 * Le widget affiche :
 * - Un bouton colore indiquant l'etat (vert = ON, gris = OFF).
 * - Les coordonnees de la position fictive du jour lorsque le service est actif.
 */
class GpsWidgetProvider : AppWidgetProvider() {

    /**
     * Appele par Android lors de la creation ou du rafraichissement des widgets.
     * Deleguee a [updateAll] pour centraliser la logique d'affichage.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate : ${appWidgetIds.size} widget(s) a rafraichir")
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    /**
     * Intercepte l'action [ACTION_TOGGLE] emise lors de la pression sur le bouton.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            Log.i(TAG, "Action TOGGLE recue")
            toggle(context)
        }
    }

    /**
     * Demarre ou arrete [MockLocationService] selon son etat courant,
     * puis force le rafraichissement de tous les widgets.
     *
     * Si la permission ACCESS_FINE_LOCATION n'est pas accordee, ouvre
     * [MainActivity] pour guider l'utilisateur plutot que de crasher.
     *
     * @param context Contexte Android necessaire pour demarrer/arreter le service.
     */
    private fun toggle(context: Context) {
        val svc = Intent(context, MockLocationService::class.java)
        if (MockLocationService.isRunning) {
            Log.i(TAG, "Arret du service demande depuis le widget")
            context.stopService(svc)
        } else {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                Log.w(TAG, "Permission de localisation manquante - ouverture de MainActivity")
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(mainIntent)
                return
            }

            Log.i(TAG, "Demarrage du service demande depuis le widget")
            context.startForegroundService(svc)
        }
        // Pas de updateAll() ici : onCreate() et onDestroy() du service
        // appellent refreshWidget() une fois isRunning reellement mis a jour,
        // ce qui evite la race condition (widget affiche OFF alors que le
        // service tourne encore).
    }

    companion object {
        private const val TAG = "FGPS.GpsWidgetProvider"
        const val ACTION_TOGGLE = "com.fgps.TOGGLE"

        /**
         * Met a jour l'affichage de tous les widgets passes en parametre.
         *
         * - Couleur du bouton : vert (#4CAF50) si actif, gris (#607D8B) si inactif.
         * - Coordonnees visibles uniquement lorsque le service est actif.
         *
         * @param context       Contexte Android.
         * @param manager       Gestionnaire de widgets.
         * @param widgetIds     Tableau des identifiants de widgets a mettre a jour.
         */
        fun updateAll(
            context: Context,
            manager: AppWidgetManager,
            widgetIds: IntArray
        ) {
            val running = MockLocationService.isRunning
            val pos = DailyPosition.get()

            for (id in widgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_gps)

                // Etat du bouton
                views.setTextViewText(R.id.btn_toggle, if (running) "ON" else "OFF")
                views.setInt(
                    R.id.btn_toggle, "setBackgroundColor",
                    if (running) 0xFF4CAF50.toInt() else 0xFF607D8B.toInt()
                )

                // Coordonnees (masquees si service inactif)
                views.setTextViewText(
                    R.id.tv_coords,
                    if (running) "%.3f°, %.3f°".format(pos.latitude, pos.longitude) else ""
                )

                // Intent envoye au BroadcastReceiver lors de la pression
                val pi = PendingIntent.getBroadcast(
                    context, 0,
                    Intent(context, GpsWidgetProvider::class.java).apply {
                        action = ACTION_TOGGLE
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_toggle, pi)

                manager.updateAppWidget(id, views)
                Log.d(TAG, "Widget $id mis a jour (running=$running)")
            }
        }
    }
}
