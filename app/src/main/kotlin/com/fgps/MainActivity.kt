package com.fgps

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Activite de lancement de FGPS.
 *
 * Cette activite n'a pas de role fonctionnel : l'outil s'utilise
 * exclusivement via le widget de l'ecran d'accueil.
 * Elle guide l'utilisateur lors de la configuration initiale et
 * fournit un raccourci direct vers les Options developpeur.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ouvre directement les Options developpeur au tap
        findViewById<Button>(R.id.btn_open_dev_options).setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
    }
}
