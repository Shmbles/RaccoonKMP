package com.shmbles.raccoon.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
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
 * Main menu screen allowing users to choose between Local Pass & Play and Online Multiplayer.
 *
 * Provides entrypoints for:
 * 1. Configuring and launching local offline games on a shared device.
 * 2. Creating a new online game room as host.
 * 3. Joining an existing online room using a 5-character alphanumeric room code.
 *
 * @param component The Decompose [MainMenuComponent] managing user inputs, network state, and transitions.
 */
@Composable
fun MainMenuScreen(component: MainMenuComponent) {
    var roomCode by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    val error by component.error.collectAsState()
    val isConnecting by component.isConnecting.collectAsState()

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
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(R.string.main_menu_title, style = MaterialTheme.typography.headlineLarge)

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local Mode Column
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameSectionHeader(
                        text = R.string.main_menu_local_game_title,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )

                    PrimaryButton(
                        onClick = { component.onLocalGameClicked() },
                        modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
                    ) {
                        Text(R.string.main_menu_local_game_button)
                    }
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight(0.7f))

                // Online Multiplayer Mode Column
                Column(
                    modifier = Modifier.weight(1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameSectionHeader(
                        text = R.string.main_menu_online_game_title,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )

                    Column(
                        modifier = Modifier.widthIn(max = 350.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StyledTextField(
                            value = nickname,
                            onValueChange = { nickname = it.take(9) },
                            label = { Text(R.string.main_menu_nickname_label) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isConnecting
                        )

                        PrimaryButton(
                            onClick = { component.onCreateRoomClicked(nickname) },
                            enabled = nickname.isNotBlank() && !isConnecting,
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(R.string.main_menu_create_room_button)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StyledTextField(
                                value = roomCode,
                                onValueChange = {
                                    roomCode = it.uppercase().filter { char -> char.isLetter() }.take(5)
                                },
                                label = { Text(R.string.main_menu_room_code_label) },
                                singleLine = true,
                                modifier = Modifier.weight(2f),
                                enabled = !isConnecting
                            )
                            PrimaryButton(
                                onClick = { component.onJoinRoomClicked(roomCode, nickname) },
                                enabled = nickname.isNotBlank() && !isConnecting,
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text(R.string.main_menu_join_button)
                            }
                        }
                    }
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 400.dp)
                )
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
