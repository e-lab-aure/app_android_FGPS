package com.fgps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Activite de lancement de FGPS.
 *
 * Responsabilites :
 * - Demander la permission ACCESS_FINE_LOCATION au premier lancement
 *   (permission d'execution obligatoire sur Android 6+ pour les services
 *   de localisation en premier plan sur Android 14+).
 * - Afficher le statut de la permission et les instructions de configuration.
 * - Fournir un raccourci vers les Options developpeur.
 */
class MainActivity : AppCompatActivity() {

    /** Lanceur de demande de permission de localisation. */
    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            updatePermissionStatus(granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Raccourci Options developpeur
        findViewById<Button>(R.id.btn_open_dev_options).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }

        // Bouton de demande de permission
        findViewById<Button>(R.id.btn_grant_permission).setOnClickListener {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Affichage du statut initial
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        updatePermissionStatus(alreadyGranted)
    }

    /**
     * Met a jour l'affichage du statut de permission et adapte
     * la visibilite du bouton de demande en consequence.
     *
     * @param granted true si ACCESS_FINE_LOCATION est accorde.
     */
    private fun updatePermissionStatus(granted: Boolean) {
        val tvStatus = findViewById<TextView>(R.id.tv_permission_status)
        val btnGrant = findViewById<Button>(R.id.btn_grant_permission)

        if (granted) {
            tvStatus.text = "Permission localisation : accordee"
            tvStatus.setTextColor(0xFF4CAF50.toInt())
            btnGrant.visibility = android.view.View.GONE
        } else {
            tvStatus.text = "Permission localisation : requise"
            tvStatus.setTextColor(0xFFFF5722.toInt())
            btnGrant.visibility = android.view.View.VISIBLE
        }
    }
}
