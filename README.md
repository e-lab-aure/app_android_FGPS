# FGPS - Fake GPS

> Widget Android minimaliste pour simuler une position GPS aleatoire, fixe pour la journee.

---

## Apercu

| Application | Parametre position fictive |
|:-----------:|:--------------------------:|
| ![Screenshot de l'application](docs/screenshot_app.png) | ![Options developpeur - position fictive](docs/screenshot_dev_options.png) |

> **Comment ajouter vos images :**
> Cree un dossier `docs/` a la racine du projet et depose-y :
> - `docs/screenshot_app.png` - capture de l'ecran principal de l'app
> - `docs/screenshot_dev_options.png` - capture de la page "Application de position fictive" dans les Options developpeur

---

## Fonctionnement

- **Widget ON/OFF** sur l'ecran d'accueil : active ou desactive la simulation en un tap
- **Position quotidienne** : un point aleatoire dans le monde est genere chaque jour a minuit (seed = date du jour)
- **Position stable** : tant que le jour ne change pas, la position reste identique
- **Injection continue** : la position fictive est poussee au systeme toutes les secondes via `LocationManager`

---

## Installation

### Prerequis

- Android 8.0 minimum (API 26)
- Mode developpeur active sur l'appareil

### Etapes

**1. Installer l'APK**

Compile et installe l'application via Android Studio (`Run > Run 'app'`).

**2. Accorder la permission de localisation**

Au premier lancement, l'ecran de configuration affiche un bouton orange.
Appuie dessus et accepte la permission `ACCESS_FINE_LOCATION`.

**3. Definir FGPS comme application de position fictive**

Appuie sur le bouton vert **"Ouvrir les Options developpeur"** depuis l'app,
puis selectionne **FGPS** dans `Application de position fictive`.

> Si les Options developpeur ne sont pas encore activees :
> `Parametres > A propos du telephone > taper 7 fois sur "Numero de build"`

**4. Ajouter le widget**

Appui long sur l'ecran d'accueil > Widgets > FGPS > deposer le widget.

**5. Utiliser**

Appuie sur le widget pour activer (vert) ou desactiver (gris) la simulation.
Les coordonnees du jour s'affichent sous le bouton lorsque le service est actif.

---

## Structure du projet

```
app/src/main/
├── kotlin/com/fgps/
│   ├── MainActivity.kt          # Ecran de configuration et raccourci Options developpeur
│   ├── MockLocationService.kt   # Service de premier plan - injection GPS
│   ├── GpsWidgetProvider.kt     # Widget ON/OFF
│   └── DailyPosition.kt         # Generateur de position quotidienne (seed = date)
└── res/
    ├── layout/
    │   ├── activity_main.xml    # Ecran principal
    │   └── widget_gps.xml       # Mise en page du widget
    ├── mipmap-anydpi-v26/       # Icone adaptative
    └── xml/widget_info.xml      # Metadata du widget
```

---

## Securite

- `android:allowBackup="false"` - aucune donnee sauvegardee via adb
- `ACTION_TOGGLE` protege par une permission `signature` - seule cette app peut declencher le widget
- Minification et obfuscation R8 activees en build release
- Aucune donnee personnelle collectee ou transmise
- Aucune connexion reseau

---

## Licence

Usage personnel. Pas de licence open source definie.
