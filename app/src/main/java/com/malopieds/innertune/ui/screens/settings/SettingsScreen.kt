package com.malopieds.innertune.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.malopieds.innertune.BuildConfig
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.R
import com.malopieds.innertune.ui.component.IconButton
import com.malopieds.innertune.ui.component.PreferenceEntry
import com.malopieds.innertune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    latestVersion: Long,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current
    var isBetaFunEnabled by remember { mutableStateOf(false) }
    val backgroundImages = listOf(

        R.drawable.cardbg,
        R.drawable.cardbg2,
        R.drawable.cardbg3,
        R.drawable.cardbg4,
        R.drawable.cardbg6,
        R.drawable.cardbg7,
        R.drawable.cardbg8,
        R.drawable.cardbg9,
        R.drawable.cardbg11,
        R.drawable.cardbg12,
        R.drawable.cardbg13,
        R.drawable.cardbg14,
        R.drawable.cardbg15,
        R.drawable.cardbg16,
        R.drawable.cardbg17,
        R.drawable.cardbg18,
        R.drawable.cardbg19,
        R.drawable.cardbg20,
        R.drawable.cardbg22,
        R.drawable.cardbg23,
        R.drawable.cardbg24,
        R.drawable.cardbg25,


        )

    var currentImageIndex by remember { mutableIntStateOf((0..backgroundImages.lastIndex).random()) }


    fun changeBackgroundImage() {
        currentImageIndex = (currentImageIndex + 1) % backgroundImages.size
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .height(220.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(color = Color.Transparent)
                .clickable { changeBackgroundImage() } // change background image on click


        ) {
            Image(
                painter = painterResource(id = backgroundImages[currentImageIndex]),
                contentDescription = "background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
//                .blur( 9.dp)

            )
            Icon(
                painter = painterResource(R.drawable.launcher_monochrome),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(1.dp, 20.dp, 12.dp),

                )

            Text(
                text = "InnerTune",
                color = Color.White,
                fontSize = 26.sp,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleSmall,

                )


        }



        PreferenceEntry(
            title = { Text(stringResource(R.string.appearance)) },
            icon = { Icon(painterResource(R.drawable.palette), null) },
            onClick = { navController.navigate("settings/appearance") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.content)) },
            icon = { Icon(painterResource(R.drawable.language), null) },
            onClick = { navController.navigate("settings/content") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.player_and_audio)) },
            icon = { Icon(painterResource(R.drawable.play), null) },
            onClick = { navController.navigate("settings/player") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.storage)) },
            icon = { Icon(painterResource(R.drawable.storage), null) },
            onClick = { navController.navigate("settings/storage") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.privacy)) },
            icon = { Icon(painterResource(R.drawable.security), null) },
            onClick = { navController.navigate("settings/privacy") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.backup_restore)) },
            icon = { Icon(painterResource(R.drawable.restore), null) },
            onClick = { navController.navigate("settings/backup_restore") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.about)) },
            icon = { Icon(painterResource(R.drawable.info), null) },
            onClick = { navController.navigate("settings/about") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.Donate)) },
            icon = { Icon(painterResource(R.drawable.donatebuy), null) },
            onClick = { uriHandler.openUri("https://buymeacoffee.com/arturocervantes") }
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.betafun)) },
            icon = { Icon(painterResource(R.drawable.funbeta), null) },

            trailingContent = {

                Switch(
                    checked = isBetaFunEnabled,
                    onCheckedChange = { isBetaFunEnabled = it },

                    modifier = Modifier.padding(end = 16.dp)
                )

            },
            onClick = {


            }
        )

        Card(
            modifier = Modifier

                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            border = BorderStroke(1.dp, Color.White),

            ) {
            Column(
                modifier = Modifier.padding(17.dp),
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(Modifier.height(3.dp))
                Text(stringResource(R.string.BetaDescription))
            }

        }


        Spacer(Modifier.height(25.dp))
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,

                ),
            onClick = { uriHandler.openUri("https://github.com/Arturo254/InnerTune/releases/latest") }

        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(38.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center

            ) {

                Spacer(Modifier.height(3.dp))
                Text(
                    text = " Version : ${BuildConfig.VERSION_NAME } \n  "  ,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier

                )

            }
        }
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, Color.Gray),
            onClick = { uriHandler.openUri("https://t.me/+NZXjVj6lETxkYTNh") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(contentDescription = null, painter = painterResource(R.drawable.telegram))
                Text(stringResource(R.string.Telegramchanel))
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.TelegramDescription),
                    color = MaterialTheme.colorScheme.error,
                )

            }
        }


    }

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}