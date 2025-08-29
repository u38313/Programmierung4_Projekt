@file:OptIn(ExperimentalMaterial3Api::class)

package de.medieninformatik.mypmaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.medieninformatik.mypmaapp.data.DemoData
import de.medieninformatik.mypmaapp.model.Category
import de.medieninformatik.mypmaapp.model.PmaEntry
import de.medieninformatik.mypmaapp.ui.ActivityLog
import de.medieninformatik.mypmaapp.ui.AddEntryDialog
import de.medieninformatik.mypmaapp.ui.MainViewModel
import de.medieninformatik.mypmaapp.ui.categoryDiagramColor
import de.medieninformatik.mypmaapp.ui.categoryBackgroundColor
import de.medieninformatik.mypmaapp.ui.theme.LightBlue
import de.medieninformatik.mypmaapp.ui.theme.MyPmaAppTheme
import de.medieninformatik.mypmaapp.ui.theme.NavBlue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/* ────────────────────────────────────────────────────────────
 * Start
 * ──────────────────────────────────────────────────────────── */

/** Host-Aktivität: setzt das Compose-Theme und rendert den App-Root. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPmaAppTheme { AppRoot() }
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Navigation
 * ──────────────────────────────────────────────────────────── */

/** Zentrale Routen-Bezeichner für den NavHost. */
private object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Activity = "activity"
    const val Impressum = "impressum"
}

/** (Derzeit nicht verwendet) – mögliche Container-Struktur für lokalen UI-State. */
private data class AppState(
    val items: List<PmaEntry>,
    val logs: List<ActivityLog>,
    val nextEntryId: Int,
    val nextLogId: Int
)

/* ────────────────────────────────────────────────────────────
 * App-Root / Navigation + BottomBar
 * ──────────────────────────────────────────────────────────── */

/** Baut Navigation, bindet ViewModel-Streams und zeigt die Bottom Navigation. */
@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    val vm: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val entries by vm.entries.collectAsState()
    val logs by vm.logs.collectAsState()

    // Einmaliges Seed + direkt zum Home-Screen.
    LaunchedEffect(Unit) {
        nav.navigate(Routes.Home) { popUpTo(Routes.Splash) { inclusive = true } }
        vm.seedDemoDefaults(DemoData.initial())
    }

    val bottomDestinations = listOf(Routes.Home to "Aktivitäten", Routes.Activity to "Aufzeichnungen")

    Scaffold(
        bottomBar = {
            val backStack by nav.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val showBar = currentRoute == Routes.Home || currentRoute == Routes.Activity
            if (showBar) {
                NavigationBar(containerColor = LightBlue) {
                    bottomDestinations.forEach { (route, label) ->
                        val selected = currentRoute == route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NavBlue,
                                selectedIconColor = Color.White
                            ),
                            icon = {
                                when (route) {
                                    Routes.Home -> Icon(Icons.Default.Home, null)
                                    Routes.Activity -> Icon(Icons.Default.BarChart, null)
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.Splash,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Splash) { SplashScreen() }
            composable(Routes.Home) {
                HomeScreen(
                    entries = entries,
                    onAdd = { /* via Dialog im Screen */ },
                    onDelete = { entry -> vm.deleteEntry(entry.id) },
                    onLog = { entry -> vm.addLog(entry.id) },
                    onOpenImpressum = { nav.navigate(Routes.Impressum) },
                    onCreateEntry = { t, d, c, res -> vm.addEntry(t, d, c, res) }
                )
            }
            composable(Routes.Activity) {
                ActivityScreen(
                    logs = logs,
                    entries = entries,
                    onOpenImpressum = { nav.navigate(Routes.Impressum) }
                )
            }
            composable(Routes.Impressum) { ImpressumScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Splash
 * ──────────────────────────────────────────────────────────── */

/** Einfache Lade-Ansicht beim Start. */
@Composable
private fun SplashScreen() {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.relax_24px),
                        contentDescription = "Logo",
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("MyPmaApp wird geladen…")
                }
            }
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Aktivitäten Screen
 * ──────────────────────────────────────────────────────────── */

/**
 * Zeigt die Aktivitätskarten, Filter und den Hinzufügen-Dialogfenster.
 * @param onCreateEntry Callback zum Anlegen neuer Einträge.
 */
@Composable
private fun HomeScreen(
    entries: List<PmaEntry>,
    onAdd: () -> Unit,
    onDelete: (PmaEntry) -> Unit,
    onLog: (PmaEntry) -> Unit,
    onOpenImpressum: () -> Unit,
    onCreateEntry: (String, String, String, Int) -> Unit
) {
    var toDelete by remember { mutableStateOf<PmaEntry?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyPmaApp – Momente") },
                actions = { IconButton(onClick = onOpenImpressum) { Icon(Icons.Default.Info, "Impressum") } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                containerColor = LightBlue,
                contentColor = Color.Black
            ) { Text("Hinzufügen") }
        }
    ) { padding ->
        val filters = remember { listOf("Alle") + Category.ALL }
        var selected by rememberSaveable { mutableStateOf("Alle") }

        // Filtern ohne Reihenfolge zu verändern.
        val displayEntries = remember(entries, selected) {
            if (selected == "Alle") entries else entries.filter { it.category == selected }
        }

        Column(Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { f ->
                    val isSelected = selected == f
                    FilterChip(
                        selected = isSelected,
                        onClick = { selected = f },
                        label = { Text(f) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LightBlue,
                            selectedLabelColor = Color.Black,
                            containerColor = Color.White,
                            labelColor = Color.Black
                        )
                    )
                }
            }

            if (displayEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Hier ist noch nichts. Tippe auf „Hinzufügen“.")
                }
            } else {
                // Platz lassen, damit der FAB unten keine Karte überdeckt.
                val fabHeight = 28.dp
                val fabMargin = 8.dp
                val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val fabClearance = fabHeight + fabMargin + bottomInset

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayEntries, key = { it.id }) { entry ->
                        PmaCard(
                            entry = entry,
                            onRequestDelete = { toDelete = entry },
                            onLog = { onLog(entry) }
                        )
                    }
                    item { Spacer(Modifier.height(fabClearance)) }
                }
            }
        }

        if (toDelete != null) {
            AlertDialog(
                onDismissRequest = { toDelete = null },
                confirmButton = {
                    TextButton(
                        onClick = { toDelete?.let(onDelete); toDelete = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Löschen") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { toDelete = null },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Text("Abbrechen") }
                },
                title = { Text("Eintrag löschen?") },
                text = { Text("Möchtest du „${toDelete!!.title}“ wirklich löschen?") }
            )
        }

        if (showAdd) {
            AddEntryDialog(
                onDismiss = { showAdd = false },
                onCreate = { t, d, c, res -> onCreateEntry(t, d, c, res) }
            )
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Aktivitätskarte
 * ──────────────────────────────────────────────────────────── */

/** Einzelne Moment-Karte mit Kategorie, Titel, Beschreibung und Log-Button. */
@Composable
private fun PmaCard(
    entry: PmaEntry,
    onRequestDelete: () -> Unit,
    onLog: () -> Unit
) {
    val bg = categoryBackgroundColor(entry.category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onRequestDelete,
                modifier = Modifier.align(Alignment.TopEnd) // Delete oben rechts platzieren
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = Color.Gray)
            }

            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(entry.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(entry.category, style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(alpha = 0.8f))
                    Text(entry.title, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                    Text(
                        entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = onLog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Aktivität aufzeichnen")
                        }
                    }
                }
            }
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Aufzeichnungen Screen
 * ──────────────────────────────────────────────────────────── */

/**
 * Zeigt 7-Tage-Stacked-Bar, Tages-Pie-Chart und die Liste der geloggten Aktivitäten.
 * Sammlung erfolgt clientseitig aus den Logs.
 */
@Composable
private fun ActivityScreen(
    logs: List<ActivityLog>,
    entries: List<PmaEntry>,
    onOpenImpressum: () -> Unit
) {
    val entryById = remember(entries) { entries.associateBy { it.id } }
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }
    val days = remember(today) { (0..6).map { today.minusDays((6 - it).toLong()) } } // älteste → neueste
    val categories = remember { de.medieninformatik.mypmaapp.model.Category.ALL }

    // 7 Tage → Map<Kategorie, Anzahl> pro Tag berechnen.
    val countsByDay = remember(logs, entries) {
        val initMap = { categories.associateWith { 0 }.toMutableMap() }
        val dayMaps = days.associateWith { initMap() }.toMutableMap()
        logs.forEach { log ->
            val d = Instant.ofEpochMilli(log.timestamp).atZone(zone).toLocalDate()
            val cat = entryById[log.entryId]?.category ?: return@forEach
            if (d in dayMaps) {
                val m = dayMaps.getValue(d)
                m[cat] = (m[cat] ?: 0) + 1
            }
        }
        days.map { dayMaps.getValue(it).toMap() }
    }
    val todayCounts = remember(countsByDay) { countsByDay.last() }

    val chartHPad = 16.dp // gleiches Padding für Chart & Labels

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aktivitäten & Statistik") },
                actions = { IconButton(onClick = onOpenImpressum) { Icon(Icons.Default.Info, "Impressum") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wochenübersicht (Stacked Bar)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightBlue)
                ) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Letzte 7 Tage (gestapelt)", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        WeeklyStackedBarChart(
                            data = countsByDay,
                            categories = categories,
                            height = 180.dp,
                            barSpacingDp = 8.dp,
                            horizontalPadding = chartHPad
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = chartHPad)
                        ) {
                            val labels = days.map { it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
                            labels.forEach { lbl ->
                                Text(
                                    lbl,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        CategoryLegend(categories)
                    }
                }
            }

            // Heute (Pie)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightBlue)
                ) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val dateStr = "%02d.%02d.%04d".format(today.dayOfMonth, today.monthValue, today.year)
                        Text("Heute ($dateStr)", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            DailyPieChart(counts = todayCounts, categories = categories, size = 180.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        CategoryLegend(categories)
                    }
                }
            }

            // Überschrift Liste
            item {
                Text(
                    "Aufgezeichnete Aktivitäten",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Liste der Aktivitäten
            if (logs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Noch keine Aktivitäten protokolliert.") }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    val entry = entryById[log.entryId]
                    ActivityRow(log = log, entry = entry)
                }
            }
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Legende
 * ──────────────────────────────────────────────────────────── */

/** Farblegende für die Kategorien (kleine Farbchips + Label). */
@Composable
private fun CategoryLegend(categories: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { c ->
            val col = categoryDiagramColor(c)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(col, shape = MaterialTheme.shapes.small))
                Spacer(Modifier.width(6.dp))
                Text(c, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Charts
 * ──────────────────────────────────────────────────────────── */

/** Gestapeltes Wochen-Balkendiagramm (7 Tage, fixe Kategorien-Reihenfolge). */
@Composable
private fun WeeklyStackedBarChart(
    data: List<Map<String, Int>>,
    categories: List<String>,
    height: Dp,
    barSpacingDp: Dp = 8.dp,
    horizontalPadding: Dp = 16.dp
) {
    val baselineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val catColorMap = categories.associateWith { categoryDiagramColor(it) }

    val density = LocalDensity.current
    val barSpacingPx = with(density) { barSpacingDp.toPx() }

    val maxTotal = (data.maxOfOrNull { it.values.sum() } ?: 0).coerceAtLeast(1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = horizontalPadding)
    ) {
        val n = data.size
        val totalSpacing = barSpacingPx * (n - 1)
        val barWidth = (size.width - totalSpacing) / n

        // Baseline am unteren Rand
        drawLine(color = baselineColor, start = Offset(0f, size.height), end = Offset(size.width, size.height))

        data.forEachIndexed { i, dayMap ->
            val left = i * (barWidth + barSpacingPx)
            var topCursor = size.height
            categories.forEach { cat ->
                val value = dayMap[cat] ?: 0
                if (value <= 0) return@forEach
                val segHeight = (value.toFloat() / maxTotal) * size.height
                val top = topCursor - segHeight
                drawRect(
                    color = catColorMap[cat] ?: Color.Gray,
                    topLeft = Offset(left, top),
                    size = Size(width = barWidth, height = segHeight)
                )
                topCursor = top // nächste Schicht oben aufsetzen
            }
        }
    }
}

/** Kreisdiagramm für die heutigen Anteile je Kategorie. */
@Composable
private fun DailyPieChart(
    counts: Map<String, Int>,
    categories: List<String>,
    size: Dp
) {
    val segmentColors = categories.associateWith { categoryDiagramColor(it) }
    val total = counts.values.sum().coerceAtLeast(1)

    Canvas(modifier = Modifier.size(size)) {
        var startAngle = -90f // Start oben (12 Uhr)
        categories.forEach { cat ->
            val v = counts[cat] ?: 0
            if (v <= 0) return@forEach
            val sweep = 360f * (v.toFloat() / total.toFloat())
            drawArc(
                color = segmentColors[cat] ?: Color.Gray,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }
}

/* ────────────────────────────────────────────────────────────
 * Eintrag für Aktivitätsliste
 * ──────────────────────────────────────────────────────────── */

/** Einzelner Log-Eintrag in der Liste, farblich passend zur Kategorie. */
@Composable
private fun ActivityRow(log: ActivityLog, entry: PmaEntry?) {
    val zone = remember { ZoneId.systemDefault() }
    val dt = Instant.ofEpochMilli(log.timestamp).atZone(zone)
    val dateStr = "%02d.%02d.%04d %02d:%02d".format(dt.dayOfMonth, dt.monthValue, dt.year, dt.hour, dt.minute)

    val container = entry?.let { categoryBackgroundColor(it.category) } ?: LightBlue

    Surface(
        color = container,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = Color.Black,
                supportingColor = Color.Black
            ),
            headlineContent = { Text(entry?.title ?: "Gelöschter Moment") },
            supportingContent = { Text("${entry?.category ?: "Unbekannt"} • $dateStr") },
            leadingContent = {
                if (entry != null) {
                    Image(
                        painter = painterResource(entry.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color.Black)
                }
            }
        )
    }
}

/* ────────────────────────────────────────────────────────────
 * Impressum + Info
 * ──────────────────────────────────────────────────────────── */

/** Informations- und Impressumsseite */
@Composable
private fun ImpressumScreen(onBack: () -> Unit) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val fabClearance = 56.dp + 16.dp + bottomInset // Platz, damit der FAB nichts überdeckt

    Scaffold(
        topBar = { TopAppBar(title = { Text("Information & Impressum") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onBack,
                containerColor = LightBlue,
                contentColor = Color.Black
            ) { Text("Zurück") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Über die App", style = MaterialTheme.typography.titleLarge)
                val about =
                    "PMA steht für Positive Mental Attitude. Dabei wird versucht, das eigene Denken positiv zu beeinflussen und eine optimistische, gelassene Grundhaltung im Alltag zu erreichen.\n" +
                            "In dieser App kannst du dir kleine Aktivitäten anlegen, die dir helfen, deine PMA zu stärken. Anschließend zeichnest du sie auf und verfolgst Fortschritte in Aktivitätenliste und Statistiken."
                Text(
                    about,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 6.dp, end = 6.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Justify
                )
            }

            item {
                Text("Impressum", style = MaterialTheme.typography.titleLarge)
                val impress =
                    "Autor: Moritz Kube\n" +
                            "Studiengang: Medieninformatik\n" +
                            "Kontext: Erstellt im Sommersemester 2025 für das Modul „Programmierung 4“ an der Hochschule Harz – Hochschule für angewandte Wissenschaften."
                Text(
                    impress,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 6.dp, end = 6.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Justify,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item { Spacer(Modifier.height(fabClearance)) }
        }
    }
}
