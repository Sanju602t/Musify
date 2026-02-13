package com.github.musicyou.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.innertube.Innertube
import com.github.innertube.models.NavigationEndpoint
import com.github.musicyou.Database
import com.github.musicyou.LocalPlayerPadding
import com.github.musicyou.LocalPlayerServiceBinder
import com.github.musicyou.R
import com.github.musicyou.enums.QuickPicksSource
import com.github.musicyou.models.LocalMenuState
import com.github.musicyou.query
import com.github.musicyou.ui.components.HomeScaffold
import com.github.musicyou.ui.components.NonQueuedMediaItemMenu
import com.github.musicyou.ui.items.LocalSongItem
import com.github.musicyou.ui.items.SongItem
import com.github.musicyou.utils.asMediaItem
import com.github.musicyou.utils.forcePlay
import com.github.musicyou.utils.quickPicksSourceKey
import com.github.musicyou.utils.rememberPreference
import com.github.musicyou.viewmodels.QuickPicksViewModel

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun QuickPicks(
    openSearch: () -> Unit,
    openSettings: () -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onOfflinePlaylistClick: () -> Unit
) {
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val playerPadding = LocalPlayerPadding.current

    val quickPicksSource = rememberPreference(
    quickPicksSourceKey,
    QuickPicksSource.Trending
).value

    LaunchedEffect(quickPicksSource) {
        viewModel.loadQuickPicks(quickPicksSource = quickPicksSource)
    }

    HomeScaffold(
        title = R.string.quick_picks,
        openSearch = openSearch,
        openSettings = openSettings
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp + playerPadding)
        ) {

            viewModel.relatedPageResult?.getOrNull()?.let { related ->

                item {
                    Text(
                        text = stringResource(id = R.string.quick_picks),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        viewModel.trending?.let { song ->
                            item {
                                LocalSongItem(
                                    modifier = Modifier.width(240.dp),
                                    song = song,
                                    onClick = {
                                        val mediaItem = song.asMediaItem
                                        binder?.stopRadio()
                                        binder?.player?.forcePlay(mediaItem)
                                        binder?.setupRadio(
                                            NavigationEndpoint.Endpoint.Watch(
                                                videoId = mediaItem.mediaId
                                            )
                                        )
                                    },
                                    onLongClick = {
                                        menuState.display {
                                            NonQueuedMediaItemMenu(
                                                onDismiss = menuState::hide,
                                                mediaItem = song.asMediaItem,
                                                onRemoveFromQuickPicks = {
                                                    query {
                                                        Database.clearEventsFor(song.id)
                                                    }
                                                },
                                                onGoToAlbum = onAlbumClick,
                                                onGoToArtist = onArtistClick
                                            )
                                        }
                                    }
                                )
                            }
                        }
items(
    items = related.songs ?: emptyList(),
    key = Innertube.SongItem::key
) 
                        ) { song ->

                            SongItem(
                                modifier = Modifier.width(240.dp),
                                song = song,
                                onClick = {
                                    val mediaItem = song.asMediaItem
                                    binder?.stopRadio()
                                    binder?.player?.forcePlay(mediaItem)
                                    binder?.setupRadio(
                                        NavigationEndpoint.Endpoint.Watch(
                                            videoId = mediaItem.mediaId
                                        )
                                    )
                                },
                                onLongClick = {
                                    menuState.display {
                                        NonQueuedMediaItemMenu(
                                            onDismiss = menuState::hide,
                                            mediaItem = song.asMediaItem,
                                            onGoToAlbum = onAlbumClick,
                                            onGoToArtist = onArtistClick
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
