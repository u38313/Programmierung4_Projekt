package de.medieninformatik.mypmaapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.medieninformatik.mypmaapp.R
import de.medieninformatik.mypmaapp.model.Category
import de.medieninformatik.mypmaapp.ui.theme.LightBlue
import de.medieninformatik.mypmaapp.ui.theme.NavBlue

/* ────────────────────────────────────────────────────────────────
 * Eingaberichtlinien (Regex)
 *  - Erlaubt: Buchstaben (inkl. Ä/Ö/Ü/ß), diakritische Zeichen,
 *    Ziffern, gängige Satz-/Symbols-Zeichen, Emojis (ZWJ + VS16),
 *    Leerzeichen. In der Beschreibung zusätzlich \n und \t.
 *  - Titel ist zwingend einzeilig (separater Check).
 * ──────────────────────────────────────────────────────────────── */
private val INVALID_TITLE_CHARS = Regex("""[^\p{L}\p{M}\p{N}\p{P}\p{S} \u200D\uFE0F]""")
private val INVALID_DESC_CHARS  = Regex("""[^\p{L}\p{M}\p{N}\p{P}\p{S}\n\t \u200D\uFE0F]""")

private const val TITLE_MAX = 50
private const val DESC_MAX  = 300

/**
 * Dialog zum Anlegen eines neuen Moments (Titel, Beschreibung, Kategorie, Icon).
 *
 * - Validiert Eingaben live (Länge, Zeichensatz, Einzeiligkeit beim Titel).
 * - Nutzt App-Farben (weiße Felder, dunkelblauer Fokus, hellblauer Dialog-Rand).
 *
 * @param onDismiss Schließt den Dialog.
 * @param onCreate  Liefert die erfassten Daten an den Caller.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, category: String, imageRes: Int) -> Unit
) {
    // Zustände für Inputs (ohne Filter → Umlaute/Emojis bleiben erhalten)
    var title by remember { mutableStateOf("") }
    var desc  by remember { mutableStateOf("") }

    // Kategorie-/Icon-Auswahl
    val categories  = Category.ALL
    var expanded    by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf(categories.first()) }

    val icons = listOf(
        R.drawable.directions_walk_24px,
        R.drawable.self_improvement_24px,
        R.drawable.nature_people_24px,
        R.drawable.mindfulness_24px,
        R.drawable.menu_book_24px,
        R.drawable.exercise_24px,
        R.drawable.stylus_note_24px,
        R.drawable.relax_24px,
        R.drawable.music_note_24px,
        R.drawable.hotel_24px,
        R.drawable.devices_off_24px,
        R.drawable.conversation_24px,
    )
    var selectedIcon by remember { mutableStateOf(icons.first()) }

    // Validierung mit REGEX und trim
    fun firstInvalidChar(s: String, rx: Regex): String? = rx.find(s)?.value

    val titleTooShort    = title.trim().isEmpty()
    val titleTooLong     = title.length > TITLE_MAX
    val titleHasInvalid  = INVALID_TITLE_CHARS.containsMatchIn(title)
    val titleHasNewline  = title.contains('\n') || title.contains('\r') // Titel soll einzeilig sein
    val titleBadChar     = firstInvalidChar(title, INVALID_TITLE_CHARS)

    val descTooShort     = desc.trim().length < 3
    val descTooLong      = desc.length > DESC_MAX
    val descHasInvalid   = INVALID_DESC_CHARS.containsMatchIn(desc)
    val descBadChar      = firstInvalidChar(desc, INVALID_DESC_CHARS)

    val titleError = titleTooShort || titleTooLong || titleHasInvalid || titleHasNewline
    val descError  = descTooShort  || descTooLong  || descHasInvalid
    val canCreate  = !titleError && !descError && selectedCat in categories

    // Einheitliche TextField-Farben
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor       = Color.White,
        unfocusedContainerColor     = Color.White,
        errorContainerColor         = Color.White,
        disabledContainerColor      = Color.White,
        focusedBorderColor          = NavBlue,
        unfocusedBorderColor        = NavBlue.copy(alpha = 0.6f),
        errorBorderColor            = MaterialTheme.colorScheme.error,
        focusedLabelColor           = NavBlue,
        cursorColor                 = NavBlue,
        focusedTrailingIconColor    = NavBlue,
        unfocusedTrailingIconColor  = NavBlue.copy(alpha = 0.8f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = LightBlue,
        title = { Text("Neuen Moment anlegen", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            // Weißer Content-Block im hellblauen Dialog
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .background(Color.White, MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    isError = titleError,
                    singleLine = true, // UI-seitig zusätzlich abgesichert
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect    = true,
                        keyboardType   = KeyboardType.Text
                    ),
                    supportingText = {
                        when {
                            titleTooShort   -> Text("Bitte gib einen Titel ein.", color = MaterialTheme.colorScheme.error)
                            titleTooLong    -> Text("Max. $TITLE_MAX Zeichen.", color = MaterialTheme.colorScheme.error)
                            titleHasNewline -> Text("Titel darf keinen Zeilenumbruch enthalten.", color = MaterialTheme.colorScheme.error)
                            titleHasInvalid -> Text("Unerlaubtes Zeichen: „$titleBadChar“", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = tfColors
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Beschreibung") },
                    isError = descError,
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect    = true,
                        keyboardType   = KeyboardType.Text
                    ),
                    supportingText = {
                        when {
                            descTooShort   -> Text("Mindestens 3 Zeichen.", color = MaterialTheme.colorScheme.error)
                            descTooLong    -> Text("Max. $DESC_MAX Zeichen.", color = MaterialTheme.colorScheme.error)
                            descHasInvalid -> Text("Unerlaubtes Zeichen: „$descBadChar“", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = tfColors
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCat,
                        onValueChange = {},
                        label = { Text("Kategorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = tfColors
                    )

                    // Menü in Weiß, neutrale Item-Farben
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color.White,
                        shadowElevation = 8.dp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { selectedCat = c; expanded = false },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Text("Icon auswählen:", style = MaterialTheme.typography.labelLarge)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 120.dp)
                ) {
                    items(icons) { res ->
                        val selectedNow: Boolean = res == selectedIcon
                        val shape: Shape = MaterialTheme.shapes.medium
                        val borderColor  = if (selectedNow) NavBlue else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(shape)
                                .background(Color.White, shape)
                                .border(2.dp, borderColor, shape)
                                .clickable { selectedIcon = res }
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(res),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canCreate,
                onClick = {
                    onCreate(title.trim(), desc.trim(), selectedCat, selectedIcon)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = NavBlue,
                    disabledContentColor = NavBlue.copy(alpha = 0.4f)
                )
            ) { Text("Erstellen") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Abbrechen") }
        }
    )
}
