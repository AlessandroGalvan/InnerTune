package com.zionhuang.music.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.zionhuang.innertube.models.Artist
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.innertube.utils.parseCookieString
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.constants.GridThumbnailHeight
import com.zionhuang.music.constants.InnerTubeCookieKey
import com.zionhuang.music.constants.ListItemHeight
import com.zionhuang.music.extensions.togglePlayPause
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.queues.YouTubeAlbumRadio
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.component.AlbumSmallGridItem
import com.zionhuang.music.ui.component.ArtistSmallGridItem
import com.zionhuang.music.ui.component.HideOnScrollFAB
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.NavigationTile
import com.zionhuang.music.ui.component.NavigationTitle
import com.zionhuang.music.ui.component.SongListItem
import com.zionhuang.music.ui.component.SongSmallGridItem
import com.zionhuang.music.ui.component.YouTubeGridItem
import com.zionhuang.music.ui.component.YouTubeSmallGridItem
import com.zionhuang.music.ui.menu.ArtistMenu
import com.zionhuang.music.ui.menu.SongMenu
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.ui.menu.YouTubeArtistMenu
import com.zionhuang.music.ui.menu.YouTubePlaylistMenu
import com.zionhuang.music.ui.utils.SnapLayoutInfoProvider
import com.zionhuang.music.utils.rememberPreference
import com.zionhuang.music.viewmodels.HomeViewModel
import kotlin.random.Random

@SuppressLint("UnrememberedMutableState")
@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()

    val forgottenFavorite by viewModel.forgottenFavorite.collectAsState()
    val homeFirstAlbumRecommendation by viewModel.homeFirstAlbumRecommendation.collectAsState()
    val homeSecondAlbumRecommendation by viewModel.homeSecondAlbumRecommendation.collectAsState()

    val homeFirstArtistRecommendation by viewModel.homeFirstArtistRecommendation.collectAsState()
    val homeSecondArtistRecommendation by viewModel.homeSecondArtistRecommendation.collectAsState()
    val homeThirdArtistRecommendation by viewModel.homeThirdArtistRecommendation.collectAsState()
    val home by viewModel.home.collectAsState()

    val keepListeningSongs by viewModel.keepListeningSongs.collectAsState()
    val keepListeningAlbums by viewModel.keepListeningAlbums.collectAsState()
    val keepListeningArtists by viewModel.keepListeningArtists.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()

    val homeFirstContinuation by viewModel.homeFirstContinuation.collectAsState()
    val homeSecondContinuation by viewModel.homeSecondContinuation.collectAsState()
    val homeThirdContinuation by viewModel.homeThirdContinuation.collectAsState()

    val youtubePlaylists by viewModel.youtubePlaylists.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val mostPlayedLazyGridState = rememberLazyGridState()

    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val listenAgainLazyGridState = rememberLazyGridState()

    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = viewModel::refresh,
        indicatorPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
            val snapLayoutInfoProviderQuickPicks = remember(mostPlayedLazyGridState) {
                SnapLayoutInfoProvider(
                    lazyGridState = mostPlayedLazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }
            val snapLayoutInfoProviderForgottenFavorite = remember(forgottenFavoritesLazyGridState) {
                SnapLayoutInfoProvider(
                    lazyGridState = forgottenFavoritesLazyGridState,
                    positionInLayout = { layoutSize, itemSize ->
                        (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                    }
                )
            }

            Column(
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateTopPadding()))

                Row(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    NavigationTile(
                        title = stringResource(R.string.history),
                        icon = R.drawable.history,
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.weight(1f)
                    )

                    NavigationTile(
                        title = stringResource(R.string.stats),
                        icon = R.drawable.trending_up,
                        onClick = { navController.navigate("stats") },
                        modifier = Modifier.weight(1f)
                    )
                    NavigationTile(
                        title = "Liked",
                        icon = R.drawable.corcircu,
                        onClick = { navController.navigate("auto_playlist/liked") },
                        modifier = Modifier.weight(1f)
                    )

                    if (isLoggedIn) {
                        NavigationTile(
                            title = stringResource(R.string.account),
                            icon = R.drawable.person,
                            onClick = {
                                navController.navigate("account")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                NavigationTitle(
                    title = stringResource(R.string.quick_picks)
                )

                quickPicks?.let { quickPicks ->
                    if (quickPicks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * 4)
                        ) {
                            Text(
                                text = stringResource(R.string.quick_picks_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyHorizontalGrid(
                            state = mostPlayedLazyGridState,
                            rows = GridCells.Fixed(4),
                            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProviderQuickPicks),
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * 4)
                        ) {
                            items(
                                items = quickPicks,
                                key = { it.id }
                            ) { originalSong ->
                                val song by database.song(originalSong.id).collectAsState(initial = originalSong)

                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .combinedClickable (
                                            onClick = {
                                                if (song!!.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue(
                                                            WatchEndpoint(videoId = song!!.id),
                                                            song!!.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                if (youtubePlaylists?.isNotEmpty() == true) {
                    NavigationTitle(
                        title = "Your Youtube playlists",
                        onClick = {
                            navController.navigate("account")
                        }
                    )
                    LazyRow(
                        contentPadding = WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                            .asPaddingValues()
                    ) {
                        items(
                            items = youtubePlaylists.orEmpty(),
                            key = { it.id }
                        ) { item ->
                            YouTubeGridItem(
                                item = item,
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("online_playlist/${item.id}")
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                YouTubePlaylistMenu(
                                                    playlist = item,
                                                    coroutineScope = coroutineScope,
                                                    onDismiss = menuState::dismiss
                                                )
                                            }
                                        }
                                    )
                            )
                        }
                    }
                }

                if (keepListening?.isNotEmpty() == true) {
                    keepListening?.let {
                        NavigationTitle(
                            title = stringResource(R.string.keep_listening),
                        )

                        LazyHorizontalGrid(
                            state = listenAgainLazyGridState,
                            rows = GridCells.Fixed(if (keepListening!!.size > 6) 2 else 1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(GridThumbnailHeight * if (keepListening!!.size > 6) 2.4f else 1.2f)
                        ) {
                            keepListening?.forEach {
                                when (it) {
                                    in 0..4 -> item {
                                        ArtistSmallGridItem(
                                            artist = keepListeningArtists!![it],
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("artist/${keepListeningArtists!![it].id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            ArtistMenu(
                                                                originalArtist = keepListeningArtists!![it],
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                ),
                                        )
                                    }

                                    in 5..9 -> item {
                                        AlbumSmallGridItem(
                                            song = keepListeningAlbums!![it - 5],
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("album/${keepListeningAlbums!![it - 5].song.albumId}")
                                                    },
                                                ),
                                        )
                                    }
                                    in 10..19 -> item {
                                        SongSmallGridItem(
                                            song = keepListeningSongs!![it - 10],
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (keepListeningSongs!![it - 10].id == mediaMetadata?.id) {
                                                            playerConnection.player.togglePlayPause()
                                                        } else {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue(
                                                                    WatchEndpoint(videoId = keepListeningSongs!![it - 10].id),
                                                                    keepListeningSongs!![it - 10].toMediaMetadata()
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            SongMenu(
                                                                originalSong = keepListeningSongs!![it - 10],
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                ),
                                                isActive = keepListeningSongs!![it - 10].song.id == mediaMetadata?.id,
                                                isPlaying = isPlaying,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                homeFirstArtistRecommendation?.let { albums  ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                        )

                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id }
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        else -> navController.navigate("artist/${item.id}")
                                                    }

                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        when (item) {
                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )

                                                            else -> {
                                                                YouTubeArtistMenu(
                                                                    artist = item as ArtistItem,
                                                                    onDismiss = menuState::dismiss
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                forgottenFavorite?.let { forgottenFavorite ->
                    if (forgottenFavorite.isNotEmpty() && forgottenFavorite.size > 5) {
                        NavigationTitle(
                            title = stringResource(R.string.forgotten_favorites)
                        )

                        LazyHorizontalGrid(
                            state = forgottenFavoritesLazyGridState,
                            rows = GridCells.Fixed(4),
                            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProviderForgottenFavorite),
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * 4)
                        ) {
                            items(
                                items = forgottenFavorite,
                                key = { it.id }
                            ) { originalSong ->
                                val song by database.song(originalSong.id).collectAsState(initial = originalSong)
                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .combinedClickable(
                                            onClick = {
                                                if (song!!.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue(
                                                            WatchEndpoint(videoId = song!!.id),
                                                            song!!.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                home?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()){
                        homePlaylists.let { playlists  ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues()
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id }
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author = Artist(name="YouTube Music", id=null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("online_playlist/${playlist.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubePlaylistMenu(
                                                            playlist = playlist,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeFirstAlbumRecommendation?.albums?.let { albums  ->
                    if (albums.recommendationAlbum.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.recommendedAlbum.name,
                        )

                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        ) {
                            items(
                                items = albums.recommendationAlbum,
                                key = { it.id }
                            ) { album ->
                                if (!album.title.contains("Presenting")) {
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("online_playlist/${album.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubePlaylistMenu(
                                                            playlist = album,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeFirstContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()){
                        homePlaylists.let { playlists  ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues()
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id }
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author = Artist(name="YouTube Music", id=null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("online_playlist/${playlist.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubePlaylistMenu(
                                                            playlist = playlist,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondArtistRecommendation?.let { albums  ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                        )

                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id }
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        else -> navController.navigate("artist/${item.id}")
                                                    }

                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        when (item) {
                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                            else -> {
                                                                YouTubeArtistMenu(
                                                                    artist = item as ArtistItem,
                                                                    onDismiss = menuState::dismiss
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()){
                        homePlaylists.let { playlists  ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues()
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id }
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author = Artist(name="YouTube Music", id=null)
                                    }
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("online_playlist/${playlist.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubePlaylistMenu(
                                                            playlist = playlist,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondAlbumRecommendation?.albums?.let { albums  ->
                    if (albums.recommendationAlbum.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.recommendedAlbum.name,
                        )

                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        ) {
                            items(
                                items = albums.recommendationAlbum,
                                key = { it.id }
                            ) { album ->
                                if (!album.title.contains("Presenting")) {
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("online_playlist/${album.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubePlaylistMenu(
                                                            playlist = album,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeThirdContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()){
                        homePlaylists.let { playlists  ->
                            NavigationTitle(
                                title = playlists.playlistName,
                            )

                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues()
                            ) {
                                items(
                                    items = playlists.playlists,
                                    key = { it.id }
                                ) { playlist ->
                                    playlist.author ?: run {
                                        playlist.author = Artist(name="YouTube Music", id=null)
                                    }
//                                    if (playlist.author?.name?.isEmpty() == true || playlist.author?.name?.contains("Youtube Music") != true)
//                                        playlist.author = Artist(name="YouTube Music", id=null)
                                    YouTubeGridItem(
                                        item = playlist,
                                        isActive = mediaMetadata?.album?.id == playlist.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("online_playlist/${playlist.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubePlaylistMenu(
                                                            playlist = playlist,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                homeThirdArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                        )

                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id }
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        else -> navController.navigate("artist/${item.id}")
                                                    }

                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        when (item) {
                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                            else -> {
                                                                YouTubeArtistMenu(
                                                                    artist = item as ArtistItem,
                                                                    onDismiss = menuState::dismiss
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                            .animateItemPlacement()
                                    )
                                }
                            }
                        }
                    }
                }

                explorePage?.newReleaseAlbums?.let { newReleaseAlbums ->
                    NavigationTitle(
                        title = stringResource(R.string.new_release_albums),
                        onClick = {
                            navController.navigate("new_release")
                        }
                    )

                    LazyRow(
                        contentPadding = WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                            .asPaddingValues()
                    ) {
                        items(
                            items = newReleaseAlbums,
                            key = { it.id }
                        ) { album ->
                            YouTubeGridItem(
                                item = album,
                                isActive = mediaMetadata?.album?.id == album.id,
                                isPlaying = isPlaying,
                                coroutineScope = coroutineScope,
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            navController.navigate("album/${album.id}")
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                YouTubeAlbumMenu(
                                                    albumItem = album,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss
                                                )
                                            }
                                        }
                                    )
                                    .animateItemPlacement()
                            )
                        }
                    }
                }
                Spacer(Modifier.height(LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()))
            }

            HideOnScrollFAB(
                visible = !quickPicks.isNullOrEmpty() || !forgottenFavorite.isNullOrEmpty() || explorePage?.newReleaseAlbums?.isNotEmpty() == true,
                scrollState = scrollState,
                icon = R.drawable.casino,
                onClick = {
                    if (Random.nextBoolean() && !quickPicks.isNullOrEmpty()) {
                        val song = quickPicks!!.random()
                        playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                    } else if (explorePage?.newReleaseAlbums?.isNotEmpty() == true) {
                        val album = explorePage?.newReleaseAlbums!!.random()
                        playerConnection.playQueue(YouTubeAlbumRadio(album.playlistId))
                    }
                }
            )
        }
    }
}
