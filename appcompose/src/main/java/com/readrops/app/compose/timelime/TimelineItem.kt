package com.readrops.app.compose.timelime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readrops.api.utils.DateUtils
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.IconText
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.VeryShortSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.pojo.ItemWithFeed
import kotlin.math.roundToInt

@Composable
fun TimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    compactLayout: Boolean = false,
) {
    Card(
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.shortSpacing)
            .alpha(if (itemWithFeed.item.isRead) 0.6f else 1f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.shortSpacing,
                        end = MaterialTheme.spacing.shortSpacing,
                        top = MaterialTheme.spacing.shortSpacing,
                    )
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = itemWithFeed.feedIconUrl,
                            error = painterResource(id = R.drawable.ic_rss_feed_grey),
                            contentDescription = itemWithFeed.feedName,
                            placeholder = painterResource(R.drawable.ic_rss_feed_grey),
                            modifier = Modifier.size(24.dp)
                        )

                        VeryShortSpacer()

                        Text(
                            text = itemWithFeed.feedName,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (itemWithFeed.bgColor != 0) Color(itemWithFeed.bgColor) else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Row {
                    Surface(
                        color = if (itemWithFeed.bgColor != 0) Color(itemWithFeed.bgColor) else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(48.dp)
                    ) {
                        Text(
                            text = DateUtils.formattedDateByLocal(itemWithFeed.item.pubDate!!),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (itemWithFeed.bgColor != 0) Color.White else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.spacing.shortSpacing,
                                vertical = MaterialTheme.spacing.veryShortSpacing
                            )
                        )
                    }
                }
            }

            ShortSpacer()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MaterialTheme.spacing.shortSpacing)
            ) {
                if (itemWithFeed.folder != null) {
                    IconText(
                        icon = painterResource(id = R.drawable.ic_folder_grey),
                        text = itemWithFeed.folder!!.name!!,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.veryShortSpacing)
                    )
                }

                IconText(
                    icon = painterResource(id = R.drawable.ic_hourglass_empty),
                    text = if (itemWithFeed.item.readTime < 1) "< 1 min" else "${itemWithFeed.item.readTime.roundToInt()} mins",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            ShortSpacer()

            Text(
                text = itemWithFeed.item.title!!,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.shortSpacing)
            )

            ShortSpacer()

            if (itemWithFeed.item.cleanDescription != null && !compactLayout) {
                Text(
                    text = itemWithFeed.item.cleanDescription!!,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.shortSpacing)
                )

                ShortSpacer()
            }

            if (itemWithFeed.item.hasImage && !compactLayout) {
                AsyncImage(
                    model = itemWithFeed.item.imageLink,
                    contentDescription = itemWithFeed.item.title!!,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(16f / 9f)
                        .fillMaxWidth()
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.shortSpacing)
            ) {
                Icon(
                    imageVector = if (itemWithFeed.item.isStarred) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.clickable { onFavorite() }
                )

                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    modifier = Modifier.clickable { onShare() }
                )
            }
        }
    }
}