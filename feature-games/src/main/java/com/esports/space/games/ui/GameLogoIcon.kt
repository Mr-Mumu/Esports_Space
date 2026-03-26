package com.esports.space.games.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.esports.space.games.domain.model.ClassifiedGame
import com.esports.space.ui.theme.LocalThemeConfig

enum class IconSize(val dp: Dp) {
    MEDIUM(64.dp),
    SMALL(48.dp)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameLogoIcon(
    game: ClassifiedGame,
    size: IconSize,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box {
            AsyncImage(
                model = game.iconUri,
                contentDescription = game.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            )

            if (game.isNewRelease) {
                NewGameBadge(
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = game.displayName,
            color = theme.textSecondary,
            fontSize = if (size == IconSize.MEDIUM) 12.sp else 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(size.dp + 12.dp)
        )
    }
}
