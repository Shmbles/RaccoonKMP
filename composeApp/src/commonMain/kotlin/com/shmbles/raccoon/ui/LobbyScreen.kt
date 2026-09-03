package com.shmbles.raccoon.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.shmbles.raccoon.component.*
import com.shmbles.raccoon.model.*
import com.shmbles.raccoon.ui.components.*
import com.shmbles.raccoon.ui.resources.*
import com.shmbles.raccoon.ui.theme.*
import com.shmbles.raccoon.ui.values.*

/**
 * Online pre-game staging lobby screen.
 *
 * Displays connected room participants, current match configuration (player count capacity and
 * points required to win), and host controls for adjusting parameters or launching the match.
 *
 * @param component The Decompose [LobbyComponent] managing room session state and network sync.
 */
@Composable
fun LobbyScreen(component: LobbyComponent) {
    val players by component.players.collectAsState()
    val config by component.config.collectAsState()
    val error by component.error.collectAsState()
    val isHost = component.isHost

    ResponsiveLayout {
        Image(
            painter = getLobbyBackgroundPainter(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(R.string.lobby_title, style = MaterialTheme.typography.headlineLarge)

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "${R.string.lobby_code_prefix}${component.roomCode}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                // Connected Players Section
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val title = "${R.string.lobby_players_title} (${players.size}/${config?.playerCount ?: ""})"
                    GameSectionHeader(
                        text = title,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(players) { player ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Text(
                                    text = player,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = GameBlueText,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f))

                // Room Settings & Host Actions Section
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    config?.let { currentConfig ->
                        LobbyConfigView(
                            config = currentConfig,
                            isEnabled = isHost,
                            playerCount = players.size,
                            onConfigChange = { newConfig ->
                                component.onConfigChanged(newConfig.playerCount, newConfig.winScore)
                            },
                            onStartGame = { component.onStartGameClicked() }
                        )
                    }
                }
            }

            // Error Banner
            AnimatedVisibility(visible = error != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = error ?: R.string.lobby_error_unknown,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { component.clearError() }) {
                        Icon(Icons.Default.Close, contentDescription = R.string.lobby_close_error)
                    }
                }
            }
        }
    }
}

/**
 * Renders the room configuration controls (player count, win score threshold, and game start button).
 *
 * @param config Current [GameConfig] parameters.
 * @param isEnabled True if local user is host and possesses permissions to alter configuration.
 * @param playerCount Number of players currently connected to the lobby.
 * @param onConfigChange Handler invoked when settings are adjusted.
 * @param onStartGame Handler invoked to begin the match.
 */
@Composable
private fun LobbyConfigView(
    config: GameConfig,
    isEnabled: Boolean,
    playerCount: Int,
    onConfigChange: (GameConfig) -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameSectionHeader(
            text = R.string.lobby_config_title,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Target Player Capacity Stepper
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GameLabelSurface(text = R.string.lobby_config_max_players)

            Stepper(
                label = "",
                value = config.playerCount,
                onValueChange = { newCount -> onConfigChange(config.copy(playerCount = newCount)) },
                range = 2..6,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Target Score Threshold Stepper
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GameLabelSurface(text = R.string.lobby_config_win_score)

            Stepper(
                label = "",
                value = config.winScore,
                onValueChange = { newScore -> onConfigChange(config.copy(winScore = newScore)) },
                range = 3..20,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start Match Action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val canStart = isEnabled && playerCount >= 2

            PrimaryButton(
                onClick = onStartGame,
                enabled = canStart,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text(R.string.lobby_config_start_game)
            }

            if (!isEnabled) {
                Text(
                    R.string.lobby_config_waiting_for_host,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (!canStart) {
                Text(
                    R.string.lobby_config_waiting_for_players,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Semi-transparent dark pill surface utilized for field labels.
 *
 * @param text Label text.
 */
@Composable
private fun GameLabelSurface(text: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 2f
                )
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

/**
 * Styled section badge with a gradient fill, border, and shadowed typography.
 *
 * @param text Header text to display.
 * @param modifier Layout modifiers.
 */
@Composable
private fun GameSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5C8BC0),
                        GameBluePanel
                    )
                )
            )
            .border(2.dp, Color.White, shape)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 3f
                )
            ),
            textAlign = TextAlign.Center
        )
    }
}
