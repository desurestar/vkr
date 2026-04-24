package ru.zagrebin.front_mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarBlurOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = BottomBarIslandDefaults.HorizontalPadding)
            .padding(bottom = BottomBarIslandDefaults.BottomPadding)
            .clip(BottomBarIslandDefaults.Shape)
    ) {
        // Soft blur-like layer in menu bounds for a frosted island effect.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(28.dp)
                .background(Color.White.copy(alpha = 0.10f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF222222).copy(alpha = 0.08f))
                .border(
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    shape = BottomBarIslandDefaults.Shape
                )
        )
    }
}
