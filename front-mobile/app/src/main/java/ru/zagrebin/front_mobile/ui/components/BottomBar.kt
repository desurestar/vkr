package ru.zagrebin.front_mobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.unit.dp
import ru.zagrebin.front_mobile.ui.navigation.BottomNavItem

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
//            .wrapContentHeight(align = Alignment.Bottom) // <- важно: прибили к низу
            .padding(horizontal = 28.dp)
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF666666),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                val backgroundColor by animateColorAsState(
                    targetValue = if (selected) Color.White.copy(alpha = 0.28f) else Color.Transparent,
                    label = "tabBg"
                )
                val horizontalPadding by animateDpAsState(
                    targetValue = if (selected) 14.dp else 6.dp,
                    label = "tabHorizontalPadding"
                )
                val verticalPadding by animateDpAsState(
                    targetValue = if (selected) 10.dp else 8.dp,
                    label = "tabVerticalPadding"
                )
                val tabWeight by animateFloatAsState(
                    targetValue = if (selected) 1.35f else 0.9f,
                    label = "tabWeight"
                )

                Box(
                    modifier = Modifier
                        .weight(tabWeight)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(backgroundColor)
                        .then(
                            if (selected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.24f),
                                    shape = RoundedCornerShape(999.dp)
                                )
                            } else Modifier
                        )
                        .clickable { onTabClick(item) }
                        .animateContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = horizontalPadding,
                            vertical = verticalPadding
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.height(22.dp)
                        )

                        AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}