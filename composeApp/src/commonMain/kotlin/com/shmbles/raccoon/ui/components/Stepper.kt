package com.shmbles.raccoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmbles.raccoon.ui.theme.GameBluePanel
import com.shmbles.raccoon.ui.theme.GameBlueText
import com.shmbles.raccoon.ui.theme.GameWhite

/**
 * Capsule-styled numeric increment/decrement stepper control.
 *
 * @param label Optional section label displayed above the stepper pill.
 * @param value Current integer value.
 * @param onValueChange Callback invoked when the user adjusts the value.
 * @param range Bounded [IntRange] limiting minimum and maximum allowed values.
 * @param enabled Whether user interaction is enabled.
 * @param modifier Layout modifiers.
 */
@Composable
fun Stepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GameBlueText
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(50))
                .border(2.dp, GameBluePanel.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(4.dp)
        ) {
            StepperButton(
                icon = Icons.Default.Remove,
                enabled = enabled && value > range.first,
                onClick = { onValueChange(value - 1) }
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    color = GameBluePanel
                ),
                modifier = Modifier
                    .width(60.dp)
                    .padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            StepperButton(
                icon = Icons.Default.Add,
                enabled = enabled && value < range.last,
                onClick = { onValueChange(value + 1) }
            )
        }
    }
}

/**
 * Circular icon button for incrementing or decrementing the stepper.
 */
@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) GameBluePanel else Color.LightGray)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GameWhite,
            modifier = Modifier.size(24.dp)
        )
    }
}
