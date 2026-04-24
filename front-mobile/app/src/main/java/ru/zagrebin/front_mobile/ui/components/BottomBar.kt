package ru.zagrebin.front_mobile.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.navigation.BottomNavItem

object BottomBarIslandDefaults {
    val HorizontalPadding: Dp = 28.dp
    val BottomPadding: Dp = 20.dp
    val Shape: Shape = RoundedCornerShape(28.dp)
}

@Composable
fun BottomBar(
    currentRoute: String?,
    onTabClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Recipes,
        BottomNavItem.Articles,
        BottomNavItem.Statistics,
        BottomNavItem.Profile
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BottomBarIslandDefaults.HorizontalPadding)
            .padding(bottom = BottomBarIslandDefaults.BottomPadding),
        shape = BottomBarIslandDefaults.Shape,
        color = Color(0xFF222222).copy(0.5f),
//        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                val tabWeight by animateFloatAsState(
                    targetValue = if (selected) 2.2f else 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "tabWeight"
                )

                Box(
                    modifier = Modifier
                        .weight(tabWeight)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) Color(0xFFB0B0B0).copy(alpha = 0.55f) else Color.Transparent)
                        .then(
                            if (selected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.24f),
                                    shape = RoundedCornerShape(999.dp)
                                )
                            } else Modifier
                        )
                        .clickable { onTabClick(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (selected) 14.dp else 6.dp,
                            vertical = if (selected) 10.dp else 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )

                        if (selected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}