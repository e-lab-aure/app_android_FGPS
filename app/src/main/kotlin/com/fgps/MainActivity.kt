package com.fgps

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activite de lancement de FGPS.
 *
 * Cette activite n'a pas de role fonctionnel : l'outil s'utilise
 * exclusivement via le widget de l'ecran d'accueil.
 * Elle sert uniquement a informer l'utilisateur des etapes de configuration
 * necessaires au premier lancement.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
