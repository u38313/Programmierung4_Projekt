package de.medieninformatik.mypmaapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.medieninformatik.mypmaapp.R

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, category: String, imageRes: Int) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var desc by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf(TextFieldValue("")) }
    val icons = listOf(
        R.drawable.placeholder_entry,
        R.drawable.ic_breath,
        R.drawable.ic_leaf,
        R.drawable.ic_phone_off
    )
    var selected by remember { mutableStateOf(icons.first()) }

    val titleError = title.text.isBlank()
    val descError = desc.text.length < 3
    val catError = category.text.isBlank()
    val canCreate = !titleError && !descError && !catError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neuen Moment anlegen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Titel") }, isError = titleError, singleLine = true
                )
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Beschreibung") }, isError = descError, minLines = 2
                )
                OutlinedTextField(
                    value = category, onValueChange = { category = it },
                    label = { Text("Kategorie") }, isError = catError, singleLine = true
                )
                Text("Icon auswählen:", style = MaterialTheme.typography.labelLarge)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 120.dp)
                ) {
                    items(icons) { res ->
                        val selectedNow = res == selected
                        val shape: Shape = MaterialTheme.shapes.medium
                        val borderColor = if (selectedNow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(shape)
                                .border(2.dp, borderColor, shape)
                                .clickable { selected = res }
                                .padding(8.dp)
                        ) {
                            Image(painter = painterResource(res), contentDescription = null, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canCreate,
                onClick = {
                    onCreate(title.text.trim(), desc.text.trim(), category.text.trim(), selected)
                    onDismiss()
                }
            ) { Text("Erstellen") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
