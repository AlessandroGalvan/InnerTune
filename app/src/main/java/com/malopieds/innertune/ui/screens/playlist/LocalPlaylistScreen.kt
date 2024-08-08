package com.malopieds.innertune.ui.screens.playlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.text.Transliterator
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastSumBy
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.malopieds.innertube.YouTube
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertube.utils.completed
import com.malopieds.innertune.LocalDatabase
import com.malopieds.innertune.LocalDownloadUtil
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.LocalPlayerConnection
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.AlbumThumbnailSize
import com.malopieds.innertune.constants.PlaylistEditLockKey
import com.malopieds.innertune.constants.PlaylistSongSortDescendingKey
import com.malopieds.innertune.constants.PlaylistSongSortType
import com.malopieds.innertune.constants.PlaylistSongSortTypeKey
import com.malopieds.innertune.constants.ThumbnailCornerRadius
import com.malopieds.innertune.db.entities.Playlist
import com.malopieds.innertune.db.entities.PlaylistSong
import com.malopieds.innertune.db.entities.PlaylistSongMap
import com.malopieds.innertune.extensions.move
import com.malopieds.innertune.extensions.toMediaItem
import com.malopieds.innertune.extensions.togglePlayPause
import com.malopieds.innertune.models.toMediaMetadata
import com.malopieds.innertune.playback.ExoDownloadService
import com.malopieds.innertune.playback.queues.ListQueue
import com.malopieds.innertune.ui.component.AutoResizeText
import com.malopieds.innertune.ui.component.DefaultDialog
import com.malopieds.innertune.ui.component.EmptyPlaceholder
import com.malopieds.innertune.ui.component.FontSizeRange
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.component.LocalMenuState
import com.malopieds.innertune.ui.component.SongListItem
import com.malopieds.innertune.ui.component.SortHeader
import com.malopieds.innertune.ui.component.TextFieldDialog
import com.malopieds.innertune.ui.menu.SelectionSongMenu
import com.malopieds.innertune.ui.menu.SongMenu
import com.malopieds.innertune.ui.utils.ItemWrapper
import com.malopieds.innertune.ui.utils.backToMain
import com.malopieds.innertune.utils.makeTimeString
import com.malopieds.innertune.utils.rememberEnumPreference
import com.malopieds.innertune.utils.rememberPreference
import com.malopieds.innertune.viewmodels.LocalPlaylistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val mutableSongs = remember { mutableStateListOf<PlaylistSong>() }
    val playlistLength =
        remember(songs) {
            songs.fastSumBy { it.song.song.duration }
        }
    val (sortType, onSortTypeChange) = rememberEnumPreference(PlaylistSongSortTypeKey, PlaylistSongSortType.CUSTOM)
    val (sortDescending, onSortDescendingChange) = rememberPreference(PlaylistSongSortDescendingKey, true)
    var locked by rememberPreference(PlaylistEditLockKey, defaultValue = false)
    val wrappedSongs = songs.map { item -> ItemWrapper(item) }.toMutableList()
    var searchQuery by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var selection by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(songs) {
        mutableSongs.apply {
            clear()
            addAll(songs)
        }
        if (songs.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it.song.id]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (songs.all {
                        downloads[it.song.id]?.state == Download.STATE_QUEUED ||
                                downloads[it.song.id]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it.song.id]?.state == Download.STATE_COMPLETED
                    }
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    var showEditDialog by remember {
        mutableStateOf(false)
    }

    if (showEditDialog) {
        playlist?.playlist?.let { playlistEntity ->
            TextFieldDialog(
                icon = { Icon(painter = painterResource(R.drawable.edit), contentDescription = null) },
                title = { Text(text = stringResource(R.string.edit_playlist)) },
                onDismiss = { showEditDialog = false },
                initialTextFieldValue = TextFieldValue(playlistEntity.name, TextRange(playlistEntity.name.length)),
                onDone = { name ->
                    database.query {
                        update(playlistEntity.copy(name = name, lastUpdateTime = LocalDateTime.now()))
                    }
                },
            )
        }
    }

    var showRemoveDownloadDialog by remember {
        mutableStateOf(false)
    }

    if (showRemoveDownloadDialog) {
        DefaultDialog(
            onDismiss = { showRemoveDownloadDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.remove_download_playlist_confirm, playlist?.playlist!!.name),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            },
            buttons = {
                TextButton(
                    onClick = { showRemoveDownloadDialog = false },
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        showRemoveDownloadDialog = false
                        songs.forEach { song ->
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                song.song.id,
                                false,
                            )
                        }
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
        )
    }

    val headerItems = 2
    val reorderableState =
        rememberReorderableLazyListState(
            onMove = { from, to ->
                if (to.index >= headerItems && from.index >= headerItems) {
                    mutableSongs.move(from.index - headerItems, to.index - headerItems)
                }
            },
            onDragEnd = { fromIndex, toIndex ->
                val from = if (fromIndex < 2) 2 else fromIndex
                val to = if (toIndex < 2) 2 else toIndex
                database.transaction {
                    move(viewModel.playlistId, from - headerItems, to - headerItems)
                }
            },
        )

    val showTopBarTitle by remember {
        derivedStateOf {
            reorderableState.listState.firstVisibleItemIndex > 0
        }
    }

    var dismissJob: Job? by remember { mutableStateOf(null) }
    var isImageTouched by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isImageTouched) {
                            isImageTouched = false
                        }
                    }
                )
            },
    ) {
        LazyColumn(
            state = reorderableState.listState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            modifier = Modifier.reorderable(reorderableState),
        ) {
            playlist?.let { playlist ->
                if (playlist.songCount == 0) {
                    item {
                        EmptyPlaceholder(
                            icon = R.drawable.music_note,
                            text = stringResource(R.string.playlist_is_empty),
                        )
                    }
                } else {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                var selectedImageUri by rememberSaveable {
                                    mutableStateOf(loadImageUri(context, playlist.playlist.id))
                                }

                                val imagePickerLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetContent()
                                ) { uri: Uri? ->
                                    uri?.let {
                                        selectedImageUri = it
                                        saveImageUri(context, it, playlist.playlist.id)
                                        isImageTouched = false
                                    }
                                }

                                val handleImageClick: () -> Unit = {
                                    if (isImageTouched) {
                                        if (selectedImageUri != null) {
                                            selectedImageUri = null
                                            saveImageUri(context, null, playlist.playlist.id)
                                            isImageTouched = false
                                        } else {
                                            imagePickerLauncher.launch("image/*")
                                        }
                                    } else {
                                        isImageTouched = true
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                        .clickable { handleImageClick() }
                                ) {
                                    when {
                                        selectedImageUri != null -> {

                                            AsyncImage(
                                                model = selectedImageUri,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .let { if (isImageTouched) it.alpha(0.7f) else it }
                                            )
                                        }
                                        playlist.thumbnails.size == 1 -> {
                                            AsyncImage(
                                                model = playlist.thumbnails[0],
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .let { if (isImageTouched) it.alpha(0.7f) else it }
                                            )
                                        }
                                        playlist.thumbnails.size > 1 -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .let { if (isImageTouched) it.alpha(0.7f) else it }
                                            ) {
                                                listOf(
                                                    Alignment.TopStart,
                                                    Alignment.TopEnd,
                                                    Alignment.BottomStart,
                                                    Alignment.BottomEnd,
                                                ).fastForEachIndexed { index, alignment ->
                                                    if (index < 4) {
                                                        AsyncImage(
                                                            model = playlist.thumbnails.getOrNull(index),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .align(alignment)
                                                                .size(AlbumThumbnailSize / 2)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            AsyncImage(
                                                model = R.drawable.music_note,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .let { if (isImageTouched) it.alpha(0.7f) else it }
                                            )
                                        }
                                    }

                                    if (isImageTouched) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (selectedImageUri != null) R.drawable.delete else R.drawable.edit
                                            ),
                                            contentDescription = if (selectedImageUri != null) "Delete" else "Edit",
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(40.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.background,
                                                    shape = CircleShape
                                                )
                                                .padding(8.dp)
                                        )
                                    }
                                }

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    AutoResizeText(
                                        text = playlist.playlist.name,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSizeRange = FontSizeRange(16.sp, 22.sp),
                                    )

                                    Text(
                                        text = pluralStringResource(R.plurals.n_song, playlist.songCount, playlist.songCount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Normal,
                                    )

                                    Text(
                                        text = makeTimeString(playlistLength * 1000L),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Normal,
                                    )

                                    Row {
                                        IconButton(
                                            onClick = { showEditDialog = true },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.edit),
                                                contentDescription = null,
                                            )
                                        }

                                        if (playlist.playlist.browseId != null) {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        val playlistPage =
                                                            YouTube.playlist(playlist.playlist.browseId).completed().getOrNull()
                                                                ?: return@launch
                                                        database.transaction {
                                                            clearPlaylist(playlist.id)
                                                            playlistPage.songs
                                                                .map(SongItem::toMediaMetadata)
                                                                .onEach(::insert)
                                                                .mapIndexed { position, song ->
                                                                    PlaylistSongMap(
                                                                        songId = song.id,
                                                                        playlistId = playlist.id,
                                                                        position = position,
                                                                    )
                                                                }.forEach(::insert)
                                                        }
                                                        snackbarHostState.showSnackbar(context.getString(R.string.playlist_synced))
                                                    }
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.sync),
                                                    contentDescription = null,
                                                )
                                            }
                                        }

                                        when (downloadState) {
                                            Download.STATE_COMPLETED -> {
                                                IconButton(
                                                    onClick = {
                                                        showRemoveDownloadDialog = true
                                                    },
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.offline),
                                                        contentDescription = null,
                                                    )
                                                }
                                            }

                                            Download.STATE_DOWNLOADING -> {
                                                IconButton(
                                                    onClick = {
                                                        songs.forEach { song ->
                                                            DownloadService.sendRemoveDownload(
                                                                context,
                                                                ExoDownloadService::class.java,
                                                                song.song.id,
                                                                false,
                                                            )
                                                        }
                                                    },
                                                ) {
                                                    CircularProgressIndicator(
                                                        strokeWidth = 2.dp,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                }
                                            }

                                            else -> {
                                                IconButton(
                                                    onClick = {
                                                        songs.forEach { song ->
                                                            val downloadRequest =
                                                                DownloadRequest
                                                                    .Builder(song.song.id, song.song.id.toUri())
                                                                    .setCustomCacheKey(song.song.id)
                                                                    .setData(
                                                                        song.song.song.title
                                                                            .toByteArray(),
                                                                    ).build()
                                                            DownloadService.sendAddDownload(
                                                                context,
                                                                ExoDownloadService::class.java,
                                                                downloadRequest,
                                                                false,
                                                            )
                                                        }
                                                    },
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.download),
                                                        contentDescription = null,
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                playerConnection.addToQueue(
                                                    items = songs.map { it.song.toMediaItem() },
                                                )
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.queue_music),
                                                contentDescription = null,
                                            )
                                        }
                                        IconButtonWithDialog(songs, playlist)
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = playlist.playlist.name,
                                                items = songs.map { it.song.toMediaItem() },
                                            ),
                                        )
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.play),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.play))
                                }

                                OutlinedButton(
                                    onClick = {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = playlist.playlist.name,
                                                items = songs.shuffled().map { it.song.toMediaItem() },
                                            ),
                                        )
                                    },
                                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(R.string.shuffle))
                                }
                            }
                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(context.getString(R.string.search)) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .focusRequester(focusRequester),  // Attach the FocusRequester to the TextField
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)
                                        focusManager.clearFocus()
                                    }
                                ),
                                shape = MaterialTheme.shapes.large,
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.search),
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchQuery = TextFieldValue("")
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.hideSoftInputFromWindow((context as Activity).currentFocus?.windowToken, 0)
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.close),
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp),
                        ) {
                            if (selection) {
                                val count = wrappedSongs.count { it.isSelected }
                                Text(text = "$count elements selected", modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        if (count == wrappedSongs.size) {
                                            wrappedSongs.forEach { it.isSelected = false }
                                        } else {
                                            wrappedSongs.forEach { it.isSelected = true }
                                        }
                                    },
                                ) {
                                    Icon(
                                        painter =
                                        painterResource(
                                            if (count ==
                                                wrappedSongs.size
                                            ) {
                                                R.drawable.deselect
                                            } else {
                                                R.drawable.select_all
                                            },
                                        ),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            SelectionSongMenu(
                                                songSelection = wrappedSongs.filter { it.isSelected }.map { it.item.song },
                                                songPosition = wrappedSongs.filter { it.isSelected }.map { it.item.map },
                                                onDismiss = menuState::dismiss,
                                                clearAction = {
                                                    selection = false
                                                    wrappedSongs.clear()
                                                },
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_vert),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = { selection = false },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.close),
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                SortHeader(
                                    sortType = sortType,
                                    sortDescending = sortDescending,
                                    onSortTypeChange = onSortTypeChange,
                                    onSortDescendingChange = onSortDescendingChange,
                                    sortTypeText = { sortType ->
                                        when (sortType) {
                                            PlaylistSongSortType.CUSTOM -> R.string.sort_by_custom
                                            PlaylistSongSortType.CREATE_DATE -> R.string.sort_by_create_date
                                            PlaylistSongSortType.NAME -> R.string.sort_by_name
                                            PlaylistSongSortType.ARTIST -> R.string.sort_by_artist
                                            PlaylistSongSortType.PLAY_TIME -> R.string.sort_by_play_time
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )

                                IconButton(
                                    onClick = { selection = !selection },
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(if (selection) R.drawable.deselect else R.drawable.select_all),
                                        contentDescription = null,
                                    )
                                }

                                IconButton(
                                    onClick = { locked = !locked },
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(if (locked) R.drawable.lock else R.drawable.lock_open),
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                    }

                    if (selection) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 16.dp),
                            ) {
                            }
                        }
                    }
                }
            }
            val searchQueryStr = textNoAccentsOrPunctMark(searchQuery.text.trim())
            val filteredSongs = if (searchQueryStr.isEmpty())
            { mutableSongs }
            else{
                mutableSongs.filter {
                    textNoAccentsOrPunctMark(it.song.song.title).contains(searchQueryStr, ignoreCase = true) or
                            textNoAccentsOrPunctMark(it.song.artists.joinToString("").trim()).contains(searchQueryStr, ignoreCase = true)
                }
            }
            if (!selection) {
                itemsIndexed(
                    items = filteredSongs,
                    key = { _, song -> song.map.id },
                ) { index, song ->
                    ReorderableItem(
                        reorderableState = reorderableState,
                        key = song.map.id,
                    ) {
                        val currentItem by rememberUpdatedState(song)
                        val dismissBoxState =
                            rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance ->
                                    totalDistance
                                },
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                        dismissValue == SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        database.transaction {
                                            move(
                                                currentItem.map.playlistId,
                                                currentItem.map.position,
                                                Int.MAX_VALUE,
                                            )
                                            delete(currentItem.map.copy(position = Int.MAX_VALUE))
                                        }
                                        dismissJob?.cancel()
                                        dismissJob =
                                            coroutineScope.launch {
                                                val snackbarResult =
                                                    snackbarHostState.showSnackbar(
                                                        message =
                                                        context.getString(
                                                            R.string.removed_song_from_playlist,
                                                            currentItem.song.song.title,
                                                        ),
                                                        actionLabel = context.getString(R.string.undo),
                                                        duration = SnackbarDuration.Short,
                                                    )
                                                if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                    database.transaction {
                                                        insert(currentItem.map.copy(position = playlistLength))
                                                        move(
                                                            currentItem.map.playlistId,
                                                            playlistLength,
                                                            currentItem.map.position,
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                    true
                                },
                            )
                        val content: @Composable () -> Unit = {
                            SongListItem(
                                song = song.song,
                                isActive = song.song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                showInLibraryIcon = true,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song.song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }

                                    if (sortType == PlaylistSongSortType.CUSTOM && !locked) {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.detectReorder(reorderableState),
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.drag_handle),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                },
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (song.song.id == mediaMetadata?.id) {
                                                playerConnection.player.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = playlist!!.playlist.name,
                                                        items = filteredSongs.map { it.song.toMediaItem() },
                                                        startIndex = index,
                                                    ),
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song.song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ),
                            )
                        }

                        if (locked) {
                            content()
                        } else {
                            SwipeToDismissBox(
                                state = dismissBoxState,
                                backgroundContent = {},
                            ) {
                                content()
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = wrappedSongs,
                    key = { _, song -> song.item.map.id },
                ) { index, songWrapper ->
                    ReorderableItem(
                        reorderableState = reorderableState,
                        key = songWrapper.item.map.id,
                    ) {
                        val currentItem by rememberUpdatedState(songWrapper.item)
                        val dismissBoxState =
                            rememberSwipeToDismissBoxState(
                                positionalThreshold = { totalDistance ->
                                    totalDistance
                                },
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd ||
                                        dismissValue == SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        database.transaction {
                                            move(
                                                currentItem.map.playlistId,
                                                currentItem.map.position,
                                                Int.MAX_VALUE,
                                            )
                                            delete(currentItem.map.copy(position = Int.MAX_VALUE))
                                        }
                                        dismissJob?.cancel()
                                        dismissJob =
                                            coroutineScope.launch {
                                                val snackbarResult =
                                                    snackbarHostState.showSnackbar(
                                                        message =
                                                        context.getString(
                                                            R.string.removed_song_from_playlist,
                                                            currentItem.song.song.title,
                                                        ),
                                                        actionLabel = context.getString(R.string.undo),
                                                        duration = SnackbarDuration.Short,
                                                    )
                                                if (snackbarResult == SnackbarResult.ActionPerformed) {
                                                    database.transaction {
                                                        insert(currentItem.map.copy(position = playlistLength))
                                                        move(
                                                            currentItem.map.playlistId,
                                                            playlistLength,
                                                            currentItem.map.position,
                                                        )
                                                    }
                                                }
                                            }
                                    }
                                    true
                                },
                            )

                        val content: @Composable () -> Unit = {
                            SongListItem(
                                song = songWrapper.item.song,
                                isActive = songWrapper.item.song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                showInLibraryIcon = true,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = songWrapper.item.song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }

                                    if (sortType == PlaylistSongSortType.CUSTOM && !locked) {
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.detectReorder(reorderableState),
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.drag_handle),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                },
                                isSelected = songWrapper.isSelected && selection,
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (!selection) {
                                                if (songWrapper.item.song.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        ListQueue(
                                                            title = playlist!!.playlist.name,
                                                            items = songs.map { it.song.toMediaItem() },
                                                            startIndex = index,
                                                        ),
                                                    )
                                                }
                                            } else {
                                                songWrapper.isSelected = !songWrapper.isSelected
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = songWrapper.item.song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ),
                            )
                        }

                        if (locked) {
                            content()
                        } else {
                            SwipeToDismissBox(
                                state = dismissBoxState,
                                backgroundContent = {},
                            ) {
                                content()
                            }
                        }
                    }
                }
            }
        }

        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showTopBarTitle) AutoResizeText(
                        text = playlist?.playlist?.name.orEmpty(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSizeRange = FontSizeRange(16.sp, 22.sp)
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain,
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = null,
                    )
                }
            },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
            Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter),
        )
    }
}
fun textNoAccentsOrPunctMark(text: String): String {
    var temp: String = text
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val transliterator = Transliterator.getInstance("Russian-Latin/BGN")
        temp = transliterator
            .transliterate(text)
    }
    return temp
        .lowercase()
        .replace("á", "a")
        .replace("à", "a")
        .replace("ä", "a")
        .replace("é", "e")
        .replace("è", "e")
        .replace("ê", "e")
        .replace("ë", "e")
        .replace("í", "i")
        .replace("î", "i")
        .replace("ï", "i")
        .replace("ó", "o")
        .replace("ô", "o")
        .replace("ö", "o")
        .replace("ú", "u")
        .replace("û", "u")
        .replace("ü", "u")
        .replace("ç", "c")
        .replace("[\\s\\p{P}]".toRegex(), "")
}
fun saveImageUri(context: Context, uri: Uri?, playlistId: String) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val fileName = "playlist_image_$playlistId.jpg"
    val file = File(context.filesDir, fileName)

    uri?.let {
        context.contentResolver.openInputStream(it)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        with(sharedPreferences.edit()) {
            putString("image_file_path_$playlistId", file.absolutePath)
            apply()
        }
    } ?: with(sharedPreferences.edit()) {
        remove("image_file_path_$playlistId")
        apply()
    }
}


fun loadImageUri(context: Context, playlistId: String): Uri? {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val filePath = sharedPreferences.getString("image_file_path_$playlistId", null)
    return filePath?.let { Uri.fromFile(File(it)) }
}

@Composable
fun IconButtonWithDialog(songs: List<PlaylistSong>, playlist: Playlist?) {
    var showDialog by remember { mutableStateOf(false) }
    var showTextFileDialog by remember { mutableStateOf(false) }
    var showQrCodeDialog by remember { mutableStateOf(false) }
    val qrCodeBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Choose an option")
                    Button(
                        onClick = {
                            showDialog = false
                            showTextFileDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Text file")
                    }
                    Button(
                        onClick = {
                            showDialog = false
                            qrCodeBitmap.value = generateQRCode(playlist?.playlist?.name + "\n" + playlist?.playlist?.id + "\n" + songs.joinToString("\n") { it.song.song.id })
                            showQrCodeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("QR Code")
                    }
                }
            }
        )
    }

    if (showTextFileDialog) {
        Dialog(
            onDismissRequest = { showTextFileDialog = false },
            properties = DialogProperties(),
            content = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Condividi il file di testo")
                    Button(
                        onClick = {
                            // Utilizzo di CoroutineScope per eseguire il codice in un thread separato
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // Creazione del file di testo
                                    val fileName = "${playlist?.playlist?.name}.txt"
                                    val content = playlist?.playlist?.name + "\n" + playlist?.playlist?.id + "\n" + songs.joinToString("\n") { it.song.song.id }
                                    val file = File(context.getExternalFilesDir(null), fileName)
                                    FileOutputStream(file).use {
                                        it.write(content.toByteArray())
                                    }

                                    // Verifica che il file esista e sia leggibile
                                    if (file.exists() && file.canRead()) {
                                        // Condivisione del file di testo
                                        val uri = FileProvider.getUriForFile(context, "com.Arturo254.innertune.debug.fileprovider", file)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share as text file"))
                                    } else {
                                        Toast.makeText(context, "File doesn't exists", Toast.LENGTH_SHORT).show()
                                    }

                                    showTextFileDialog = false
                                } catch (e: Exception) {
                                    Log.e("ShareTextFileDialog", "Errore durante la creazione o condivisione del file", e)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Condividi")
                    }
                }
            }
        )
    }

    if (showQrCodeDialog) {
        Dialog(
            onDismissRequest = { showQrCodeDialog = false },
            properties = DialogProperties()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showQrCodeDialog = false }, // Chiudi il dialogo quando si clicca fuori dall'immagine
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(300.dp) // Dimensione fissa del contenitore
                        .aspectRatio(1f) // Mantieni il rapporto di aspetto quadrato
                        .clip(RoundedCornerShape(16.dp)) // Angoli smussati
                        .background(
                            color = androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ) // Nessuno sfondo con bordi stondati
                        .padding(0.dp), // Rimuovi lo spazio interno
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrCodeBitmap.value!!,
                        contentDescription = "QR Code",
                        contentScale = ContentScale.Crop, // Ridimensiona l'immagine per riempire il contenitore
                        modifier = Modifier.fillMaxSize() // Assicura che l'immagine riempia tutta la Box
                    ) ?: Text(text = "Errore nella generazione del QR Code")
                }
            }
        }
    }

    IconButton(
        onClick = {
            showDialog = true
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.share),
            contentDescription = null
        )
    }
}

fun generateQRCode(text: String, size: Int = 250): ImageBitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap.asImageBitmap()
    } catch (e: WriterException) {
        null
    }
}