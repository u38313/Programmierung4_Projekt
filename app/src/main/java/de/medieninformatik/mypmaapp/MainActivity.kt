@file:OptIn(ExperimentalMaterial3Api::class)

package de.medieninformatik.mypmaapp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.medieninformatik.mypmaapp.ui.MainViewModel
import de.medieninformatik.mypmaapp.ui.AddEntryDialog
import de.medieninformatik.mypmaapp.ui.ActivityLog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.medieninformatik.mypmaapp.data.DemoData
import de.medieninformatik.mypmaapp.model.PmaEntry
import de.medieninformatik.mypmaapp.ui.theme.MyPmaAppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

/* ======================= MainActivity ======================= */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPmaAppTheme {
                AppRoot()
            }
        }
    }
}

/* ======================= Navigation Setup ======================= */

private object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Activity = "activity"
    const val Impressum = "impressum"
}

private data class AppState(
    val items: List<PmaEntry>,
    val logs: List<ActivityLog>,
    val nextEntryId: Int,
    val nextLogId: Int
)

/* ======================= App Root (State + NavHost + BottomBar) ======================= */

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()

    val entries by vm.entries.collectAsState()
    val logs by vm.logs.collectAsState()

    // Splash → Home nach kurzer Zeit
    LaunchedEffect(Unit) {
        nav.navigate(Routes.Home) { popUpTo(Routes.Splash) { inclusive = true } }
    }

    val bottomDestinations = listOf(Routes.Home to "Momente", Routes.Activity to "Aktivitäten")

    Scaffold(
        bottomBar = {
            val backStack by nav.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val showBar = currentRoute == Routes.Home || currentRoute == Routes.Activity
            if (showBar) {
                NavigationBar {
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
                    onAdd = { /* handled in HomeScreen via dialog */ },
                    onDelete = { entry -> vm.deleteEntry(entry.id) },
                    onLog = { entry -> vm.addLog(entry.id) },
                    onOpenImpressum = { nav.navigate(Routes.Impressum) },
                    onCreateEntry = { t, d, c, res -> vm.addEntry(t, d, c, res) }
                )
            }

            composable(Routes.Activity) {
                ActivityScreen(
                    logs = logs,               // <-- direkt, ohne .map { ... }
                    entries = entries,
                    onOpenImpressum = { nav.navigate(Routes.Impressum) }
                )
            }

            composable(Routes.Impressum) { ImpressumScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

/* ======================= Splash ======================= */

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
                        painter = painterResource(id = R.drawable.placeholder_entry),
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

/* ======================= Home (Momente / Cards) ======================= */

@Composable
private fun HomeScreen(
    entries: List<PmaEntry>,
    onAdd: () -> Unit,
    onDelete: (PmaEntry) -> Unit,
    onLog: (PmaEntry) -> Unit,
    onOpenImpressum: () -> Unit,
    onCreateEntry: (String, String, String, Int) -> Unit   // <- NEU
) {
    var toDelete by remember { mutableStateOf<PmaEntry?>(null) }
    var showAdd by remember { mutableStateOf(false) }       // <- NEU

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyPmaApp – Momente") },
                actions = {
                    IconButton(onClick = onOpenImpressum) {
                        Icon(Icons.Default.Info, contentDescription = "Impressum")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAdd = true }) { // <- NEU
                Text("Hinzufügen")
            }
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Noch keine Momente. Tippe auf „Hinzufügen“.") }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    PmaCard(
                        entry = entry,
                        onRequestDelete = { toDelete = entry },
                        onLog = { onLog(entry) }
                    )
                }
            }
        }

        if (toDelete != null) {
            AlertDialog(
                onDismissRequest = { toDelete = null },
                confirmButton = { TextButton(onClick = { toDelete?.let(onDelete); toDelete = null }) { Text("Löschen") } },
                dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Abbrechen") } },
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


@Composable
private fun PmaCard(
    entry: PmaEntry,
    onRequestDelete: () -> Unit,
    onLog: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(entry.imageRes),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    entry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AssistChip(
                    label = { Text(entry.category) },
                    onClick = {},
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                // Protokollieren
                FilledTonalButton(onClick = onLog, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Protokollieren")
                }
                // Löschen
                IconButton(onClick = onRequestDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Löschen")
                }
            }
        }
    }
}

/* ======================= Aktivitäten (Liste + Mini-Chart) ======================= */

@Composable
private fun ActivityScreen(
    logs: List<ActivityLog>,
    entries: List<PmaEntry>,
    onOpenImpressum: () -> Unit
) {
    val entryById = remember(entries) { entries.associateBy { it.id } }

    // Aggregation: letzte 7 Tage (inkl. heute)
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now(zone) }
    val days = remember(today) { (0..6).map { today.minusDays((6 - it).toLong()) } } // 7 Tage, älteste → neueste
    val counts = remember(logs, entries) {
        val map = days.associateWith { 0 }.toMutableMap()
        logs.forEach { log ->
            val d = Instant.ofEpochMilli(log.timestamp).atZone(zone).toLocalDate()
            if (d in map) map[d] = map.getValue(d) + 1
        }
        days.map { map.getValue(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aktivitäten & Statistik") },
                actions = {
                    IconButton(onClick = onOpenImpressum) {
                        Icon(Icons.Default.Info, contentDescription = "Impressum")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Mini-Chart
            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Letzte 7 Tage", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    WeeklyBarChart(
                        counts = counts,
                        barColor = MaterialTheme.colorScheme.primary,
                        height = 160.dp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEach { d ->
                            val label = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Divider()

            // Liste der letzten Aktivitäten
            if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Noch keine Aktivitäten protokolliert.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val entry = entryById[log.entryId]
                        ActivityRow(log = log, entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(log: ActivityLog, entry: PmaEntry?) {
    val zone = remember { ZoneId.systemDefault() }
    val dt = Instant.ofEpochMilli(log.timestamp).atZone(zone)
    val dateStr = "%02d.%02d.%04d %02d:%02d".format(
        dt.dayOfMonth, dt.monthValue, dt.year, dt.hour, dt.minute
    )

    ListItem(
        headlineContent = { Text(entry?.title ?: "Gelöschter Moment") },
        supportingContent = {
            Text("${entry?.category ?: "Unbekannt"} • $dateStr")
        },
        leadingContent = {
            if (entry != null) {
                Image(
                    painter = painterResource(entry.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(Icons.Default.TaskAlt, contentDescription = null)
            }
        }
    )
}

/* ======================= Mini-Bar-Chart (Compose Canvas) ======================= */

@Composable
private fun WeeklyBarChart(
    counts: List<Int>,            // genau 7 Werte (älteste → neueste)
    barColor: Color,
    height: Dp,
    maxBars: Int = 7,
    barSpacingDp: Dp = 8.dp
) {
    val maxValue = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
    val density = LocalDensity.current
    val barSpacingPx = with(density) { barSpacingDp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 4.dp)
    ) {
        val n = maxBars.coerceAtMost(counts.size)
        val totalSpacing = barSpacingPx * (n - 1).toFloat()
        val barWidth = (size.width - totalSpacing) / n.toFloat()

        // Baseline (dezent)
        drawLine(
            color = barColor.copy(alpha = 0.12f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height)
        )

        counts.takeLast(n).forEachIndexed { i, v ->
            val ratio = v.toFloat() / maxValue.toFloat()
            val barHeight = ratio * size.height

            val left = i.toFloat() * (barWidth + barSpacingPx)
            val top = size.height - barHeight
            val right = left + barWidth
            val bottom = size.height

            drawLine(
                color = barColor,
                start = Offset((left + right) / 2f, bottom),
                end = Offset((left + right) / 2f, top),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}


/* ======================= Impressum ======================= */

@Composable
private fun ImpressumScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Impressum / Über uns") }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Autor: Moritz Kube")
            Text("Studiengang: Medieninformatik (MINF)")
            Text("App: MyPmaApp")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Zurück") }
        }
    }
}
