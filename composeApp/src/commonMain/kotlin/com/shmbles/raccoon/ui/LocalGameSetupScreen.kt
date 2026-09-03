package com.shmbles.raccoon.ui

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
import com.shmbles.raccoon.ui.components.*
import com.shmbles.raccoon.ui.resources.*
import com.shmbles.raccoon.ui.theme.*
import com.shmbles.raccoon.ui.values.*

/**
 * Configuration screen for local offline "Pass & Play" matches.
 *
 * Allows configuring participant nicknames (between 2 and 6 players), adjusting the
 * target win score threshold, and launching the local game match.
 *
 * @param component The Decompose [LocalGameSetupComponent] managing player roster and rules.
 */
@Composable
fun LocalGameSetupScreen(component: LocalGameSetupComponent) {
    val players by component.players.collectAsState()
    val config by component.config.collectAsState()

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
            Text(R.string.local_game_setup_title, style = MaterialTheme.typography.headlineLarge)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Top
            ) {
                // Player Roster Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameSectionHeader(
                        text = R.string.local_game_setup_players_title,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(players, key = { index, _ -> index }) { index, player ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                StyledTextField(
                                    value = player,
                                    onValueChange = { newName ->
                                        component.onPlayerNameChanged(index, newName.take(9))
                                    },
                                    label = { Text("${R.string.local_game_setup_player_label} ${index + 1}") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                if (players.size > 2) {
                                    IconButton(onClick = { component.onRemovePlayerClicked(index) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = R.string.local_game_setup_remove_player_description,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            if (players.size < 6) {
                                Spacer(modifier = Modifier.height(8.dp))
                                PrimaryButton(
                                    onClick = { component.onAddPlayerClicked() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(R.string.local_game_setup_add_player_button)
                                }
                            }
                        }
                    }
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.8f))

                // Score Configuration & Start Action Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameSectionHeader(
                        text = R.string.local_game_setup_config_title,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    Column(
                        modifier = Modifier.widthIn(max = 360.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = R.string.local_game_setup_win_score_label,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 2f)
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }

                            Stepper(
                                label = "",
                                value = config.winScore,
                                onValueChange = { newScore ->
                                    component.onConfigChanged(config.playerCount, newScore)
                                },
                                range = 3..20,
                                enabled = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        PrimaryButton(
                            onClick = { component.onStartGameClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(R.string.local_game_setup_start_game_button)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Styled section badge with a gradient fill, border, and shadowed typography.
 *
 * @param text Header text to display.
 * @param modifier Layout modifiers.
 */
@Composable
private fun GameSectionHeader(text: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .clip(shape)
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF5C8BC0), GameBluePanel)))
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
                shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(2f, 2f), blurRadius = 3f)
            ),
            textAlign = TextAlign.Center
        )
    }
}
