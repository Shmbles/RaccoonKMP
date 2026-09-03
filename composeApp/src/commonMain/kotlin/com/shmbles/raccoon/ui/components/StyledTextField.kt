package com.shmbles.raccoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shmbles.raccoon.ui.theme.GameBlueText

/**
 * Text input field styled with rounded corners, subtle drop shadow, and customized typography.
 *
 * @param value Current string value.
 * @param onValueChange Callback invoked when the user modifies text.
 * @param modifier Layout modifiers.
 * @param enabled Whether the text field is editable.
 * @param label Optional placeholder or floating label composable displayed when input is empty.
 * @param singleLine True to restrict input to a single line.
 */
@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(2.dp, Color(0xFFE0E0E0), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && label != null) {
            Box(modifier = Modifier.alpha(0.6f)) {
                label()
            }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = GameBlueText,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(GameBlueText),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
