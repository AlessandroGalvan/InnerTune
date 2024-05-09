package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.pages.ExplorePage
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    val explorePage = MutableStateFlow<ExplorePage?>(null)
    private suspend fun load(){
        YouTube.explore().onSuccess { page ->
            val artists: MutableMap<Int, String> = mutableMapOf()
            val favouriteArtists: MutableMap<Int, String> = mutableMapOf()
            database.allArtistsByPlayTime().first().let { list ->
                var favIndex = 0
                for ((artistsIndex, artist) in list.withIndex()){
                    artists[artistsIndex] = artist.id
                    if (artist.artist.bookmarkedAt != null){
                        favouriteArtists[favIndex] = artist.id
                        favIndex++
                    }
                }
            }
            explorePage.value = page.copy(
                newReleaseAlbums = page.newReleaseAlbums
                    .sortedBy { album ->
                        val artistIds = album.artists.orEmpty().mapNotNull { it.id }
                        val firstArtistKey = artistIds.firstNotNullOfOrNull { artistId ->
                            if (artistId in favouriteArtists.values) {
                                favouriteArtists.entries.firstOrNull { it.value == artistId }?.key
                            } else {
                                artists.entries.firstOrNull { it.value == artistId }?.key
                            }
                        } ?: Int.MAX_VALUE
                        firstArtistKey
                    }
            )
        }.onFailure {
            reportException(it)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            load()
        }
    }
}