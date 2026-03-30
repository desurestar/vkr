import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    // Центр поля: высота поля ~56dp → центр ≈ 20.dp
    val labelOffset by animateDpAsState(
        targetValue = if (isFocused || value.isNotEmpty()) (-10).dp else 20.dp,
        animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing),
        label = ""
    )

    val labelSize by animateFloatAsState(
        targetValue = if (isFocused || value.isNotEmpty()) 0.75f else 1f,
        animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing),
        label = ""
    )

    Box(modifier = modifier) {

        Text(
            text = label,
            fontSize = 12.sp * labelSize,
            color = if (isFocused) Color.Gray else Color(0xFF1E1C1F).copy(0.4f),
            modifier = Modifier
                .padding(start = 16.dp)
                .offset(y = labelOffset)
                .background(Color.Transparent)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { isFocused = it.isFocused }
                .background(Color(0xFFF2F2F2).copy(0.6f), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            cursorBrush = SolidColor(Color.DarkGray)
        )
    }
}