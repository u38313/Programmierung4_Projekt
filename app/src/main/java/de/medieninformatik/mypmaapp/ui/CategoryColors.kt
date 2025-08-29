// app/src/main/java/de/medieninformatik/mypmaapp/ui/CategoryColors.kt
package de.medieninformatik.mypmaapp.ui

import androidx.compose.ui.graphics.Color
import de.medieninformatik.mypmaapp.model.Category

/*
 Farben für meine Kategorien (Hintergrund + Diagramme)
  */
private object CP {
    // Entspannung: blau
    val entspannBg = Color(0x996EB9E5)
    val entspannDia       = Color(0xFF5AC0EF)

    // Kreativität: lila
    val kreativBg = Color(0x999675CB)
    val kreativDia       = Color(0xFF9467D0)

    // Bewegung: orange
    val bewegBg  = Color(0x99D98135)
    val bewegDia        = Color(0xFFFF9742)

    // Default: grau
    val defaultBg           = Color(0xFFECECEC)
    val defaultDia            = Color(0xFF9E9E9E)
}

/* Liefert die Hintergrundfarbe je Kategorie. */
fun categoryBackgroundColor(cat: String): Color = when (cat) {
    Category.ENTSPANNUNG -> CP.entspannBg
    Category.KREATIVITAET -> CP.kreativBg
    Category.BEWEGUNG     -> CP.bewegBg
    else -> CP.defaultBg
}

/* Liefert die Diagrammfarbe je Kategorie. */
fun categoryDiagramColor(cat: String): Color = when (cat) {
    Category.ENTSPANNUNG -> CP.entspannDia
    Category.KREATIVITAET -> CP.kreativDia
    Category.BEWEGUNG     -> CP.bewegDia
    else -> CP.defaultDia
}
