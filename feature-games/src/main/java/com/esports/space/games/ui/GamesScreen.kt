package com.esports.space.games.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.esports.space.data.db.entity.GameCategory
import com.esports.space.games.data.ScannedGame
import com.esports.space.games.domain.model.ClassifiedGame
import com.esports.space.ui.theme.LayoutMode
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun GamesScreen(
    viewModel: GamesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val theme = LocalThemeConfig.current
    val context = LocalContext.current

    var contextTarget by remember { mutableStateOf<ClassifiedGame?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (theme.layoutMode) {
            LayoutMode.GALAXY_RADIAL -> GalaxyRadialLayout(
                state = uiState,
                onLaunch = { viewModel.launchGame(it, context) },
                onContextMenu = { contextTarget = it }
            )
            else -> GridLayout(
                state = uiState,
                columns = if (theme.layoutMode == LayoutMode.THREE_COLUMN) 3 else 4,
                onLaunch = { viewModel.launchGame(it, context) },
                onContextMenu = { contextTarget = it }
            )
        }

        FloatingActionButton(
            onClick = {
                viewModel.loadInstalledApps()
                showAddDialog = true
            },
            containerColor = theme.primaryAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "手动添加")
        }

        contextTarget?.let { game ->
            GameContextMenu(
                game = game,
                onDismiss = { contextTarget = null },
                onPin = {
                    viewModel.togglePin(game.packageName)
                    contextTarget = null
                },
                onRemove = {
                    viewModel.removeGame(game.packageName)
                    contextTarget = null
                }
            )
        }

        if (showAddDialog) {
            AddGameDialog(
                installedApps = uiState.installedApps,
                onSelect = { pkg ->
                    viewModel.addManually(pkg)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }
    }
}

@Composable
private fun GalaxyRadialLayout(
    state: GamesUiState,
    onLaunch: (String) -> Unit,
    onContextMenu: (ClassifiedGame) -> Unit
) {
    val theme = LocalThemeConfig.current
    val predicted = state.games.filter { it.category == GameCategory.PREDICTED }
    val frequent = state.games.filter { it.category == GameCategory.FREQUENT }
    val infrequent = state.games.filter { it.category == GameCategory.INFREQUENT }
    val newGames = state.games.filter { it.category == GameCategory.NEW }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (predicted.isNotEmpty()) {
            SectionLabel("预测推荐")
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(predicted, key = { it.packageName }) { game ->
                    GamePosterCard(
                        game = game,
                        onClick = { onLaunch(game.packageName) },
                        onLongClick = { onContextMenu(game) },
                        modifier = Modifier.width(280.dp)
                    )
                }
            }
        }

        if (frequent.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("常玩游戏")
            Spacer(Modifier.height(8.dp))
            FlowGrid(
                items = frequent,
                onLaunch = onLaunch,
                onContextMenu = onContextMenu,
                iconSize = IconSize.MEDIUM
            )
        }

        if (infrequent.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("其他游戏")
            Spacer(Modifier.height(8.dp))
            FlowGrid(
                items = infrequent,
                onLaunch = onLaunch,
                onContextMenu = onContextMenu,
                iconSize = IconSize.SMALL
            )
        }

        if (newGames.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionLabel("新游推荐")
            Spacer(Modifier.height(8.dp))
            FlowGrid(
                items = newGames,
                onLaunch = onLaunch,
                onContextMenu = onContextMenu,
                iconSize = IconSize.MEDIUM
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun FlowGrid(
    items: List<ClassifiedGame>,
    onLaunch: (String) -> Unit,
    onContextMenu: (ClassifiedGame) -> Unit,
    iconSize: IconSize
) {
    val chunked = items.chunked(if (iconSize == IconSize.MEDIUM) 5 else 7)
    for (row in chunked) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (game in row) {
                GameLogoIcon(
                    game = game,
                    size = iconSize,
                    onClick = { onLaunch(game.packageName) },
                    onLongClick = { onContextMenu(game) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun GridLayout(
    state: GamesUiState,
    columns: Int,
    onLaunch: (String) -> Unit,
    onContextMenu: (ClassifiedGame) -> Unit
) {
    val predicted = state.games.filter { it.category == GameCategory.PREDICTED }
    val others = state.games.filter { it.category != GameCategory.PREDICTED }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (predicted.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionLabel("预测推荐")
            }
            items(predicted, key = { it.packageName }) { game ->
                GamePosterCard(
                    game = game,
                    onClick = { onLaunch(game.packageName) },
                    onLongClick = { onContextMenu(game) }
                )
            }
        }

        if (others.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionLabel("全部游戏")
            }
            items(others, key = { it.packageName }) { game ->
                GameLogoIcon(
                    game = game,
                    size = if (game.category == GameCategory.FREQUENT) IconSize.MEDIUM else IconSize.SMALL,
                    onClick = { onLaunch(game.packageName) },
                    onLongClick = { onContextMenu(game) }
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val theme = LocalThemeConfig.current
    Text(
        text = text,
        color = theme.textPrimary.copy(alpha = 0.7f),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GameContextMenu(
    game: ClassifiedGame,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onRemove: () -> Unit
) {
    DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("固定 / 取消固定") },
            onClick = onPin,
            leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
        )
        DropdownMenuItem(
            text = { Text("移除") },
            onClick = onRemove,
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
        )
    }
}

@Composable
private fun AddGameDialog(
    installedApps: List<ScannedGame>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalThemeConfig.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("手动添加游戏", color = theme.textPrimary) },
        text = {
            if (installedApps.isEmpty()) {
                Text("未找到可添加的应用", color = theme.textSecondary)
            } else {
                Column(modifier = Modifier.height(400.dp).verticalScroll(rememberScrollState())) {
                    for (app in installedApps) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            AsyncImage(
                                model = app.iconUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = app.displayName,
                                color = theme.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onSelect(app.packageName) }) {
                                Text("添加", color = theme.primaryAccent)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = theme.textSecondary)
            }
        }
    )
}
