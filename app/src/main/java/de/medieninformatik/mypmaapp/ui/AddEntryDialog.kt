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

/* --------------------------------------------------------------
 REGEX Regeln für Textfelder Titel und Beschreibung
  */
private val REGEX_TITLE = Regex("""[^\p{L}\p{M}\p{N}\p{P}\p{S} \u200D\uFE0F]""")
private val REGEX_DESC  = Regex("""[^\p{L}\p{M}\p{N}\p{P}\p{S}\n\t \u200D\uFE0F]""")

private const val TITLE_MAX = 50
private const val DESC_MAX  = 300

/*
    Dialogfenster zum Hinzufügen einer neuen Aktivität mit Titel, Beschreibung, Kategorie, Icon).
    Validiert Eingaben mit REGEX und Überprüfung der Länge
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, category: String, imageRes: Int) -> Unit
) {
    // Textfeld-Inputs
    var title by remember { mutableStateOf("") }
    var desc  by remember { mutableStateOf("") }

    // Kategorie
    val categories  = Category.ALL
    var expanded    by remember { mutableStateOf(false) }
    var selectedCat by remember { mutableStateOf(categories.first()) }

    //Icon-Liste
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

    // Validierung mit REGEX (schaut bei Titel und Beschreibung nach falschen Zeichen und Länge)
    fun firstInvalidChar(s: String, rx: Regex): String? = rx.find(s)?.value

    val titleTooShort    = title.trim().isEmpty()
    val titleTooLong     = title.length > TITLE_MAX
    val titleHasInvalid  = REGEX_TITLE.containsMatchIn(title)
    val titleHasNewline  = title.contains('\n') || title.contains('\r')
    val titleBadChar     = firstInvalidChar(title, REGEX_TITLE)

    val descTooShort     = desc.trim().length < 3
    val descTooLong      = desc.length > DESC_MAX
    val descHasInvalid   = REGEX_DESC.containsMatchIn(desc)
    val descBadChar      = firstInvalidChar(desc, REGEX_DESC)

    val titleError = titleTooShort || titleTooLong || titleHasInvalid || titleHasNewline
    val descError  = descTooShort  || descTooLong  || descHasInvalid
    val canCreate  = !titleError && !descError && selectedCat in categories

    // Textfeld Farben
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

    /* ------------------------------------------------------------------------
    * Aufbau des Fensters
    * */
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = LightBlue,
        title = { Text("Neue Aktivität anlegen", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            // Rahmenfenster hellblau, innen weiß
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .background(Color.White, MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                //Titel
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
                //Beschreibung
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

                //Kategorieauswahl mit Dropdown
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

                //Icon Auswahl
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

        //Bestätigung und Abbrechen Button
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
