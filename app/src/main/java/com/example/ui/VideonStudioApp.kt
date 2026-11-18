package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.AppLanguage
import com.example.domain.model.MediaType
import com.example.domain.model.StudioRoute
import com.example.ui.screens.AiAudioScreen
import com.example.ui.screens.AiImageScreen
import com.example.ui.screens.AiVideoScreen
import com.example.ui.screens.CharacterStudioScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HelpCenterScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyProjectsScreen
import com.example.ui.screens.PromptLibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LiquidGlassBackground
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.VideonStudioTheme
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.launch

private fun iconForRoute(route: StudioRoute): ImageVector = when (route) {
    StudioRoute.HOME -> Icons.Rounded.Home
    StudioRoute.DASHBOARD -> Icons.Rounded.Dashboard
    StudioRoute.AI_VIDEO -> Icons.Rounded.MovieCreation
    StudioRoute.AI_IMAGE -> Icons.Rounded.Image
    StudioRoute.AI_AUDIO -> Icons.Rounded.GraphicEq
    StudioRoute.VIDEO_EDITOR -> Icons.Rounded.Edit
    StudioRoute.CHARACTER_STUDIO -> Icons.Rounded.Face
    StudioRoute.PROMPT_LIBRARY -> Icons.Rounded.Psychology
    StudioRoute.MY_PROJECTS -> Icons.Rounded.FolderSpecial
    StudioRoute.HISTORY -> Icons.Rounded.History
    StudioRoute.SETTINGS -> Icons.Rounded.Settings
    StudioRoute.HELP_CENTER -> Icons.Rounded.Help
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideonStudioApp(viewModel: StudioViewModel = viewModel()) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val prompts by viewModel.promptTemplates.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val totalVideos by viewModel.totalVideosCount.collectAsStateWithLifecycle()
    val totalImages by viewModel.totalImagesCount.collectAsStateWithLifecycle()
    val totalAudio by viewModel.totalAudioCount.collectAsStateWithLifecycle()
    val storageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()

    val videoUi by viewModel.videoUi.collectAsStateWithLifecycle()
    val imageUi by viewModel.imageUi.collectAsStateWithLifecycle()
    val audioUi by viewModel.audioUi.collectAsStateWithLifecycle()
    val promptUi by viewModel.promptUi.collectAsStateWithLifecycle()
    val editorUi by viewModel.editorUi.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val pendingNav by viewModel.navigateToRoute.collectAsStateWithLifecycle()
    val selfHostedStatus by viewModel.selfHostedStatus.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNotifications by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    LaunchedEffect(pendingNav) {
        val target = pendingNav
        if (target != null) {
            navController.navigate(target.route) {
                launchSingleTop = true
            }
            viewModel.consumeNavigation()
        }
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRouteName = backStack?.destination?.route ?: StudioRoute.HOME.route
    val currentRoute = StudioRoute.entries.firstOrNull { it.route == currentRouteName } ?: StudioRoute.HOME

    fun navigateTo(route: StudioRoute) {
        navController.navigate(route.route) {
            launchSingleTop = true
        }
        scope.launch { drawerState.close() }
    }

    VideonStudioTheme(darkTheme = prefs.isDarkMode) {
        LiquidGlassBackground {
            if (showNotifications) {
                ModalBottomSheet(onDismissRequest = { showNotifications = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (prefs.language == AppLanguage.ID) "Pusat Notifikasi" else "Notification Center",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = if (prefs.language == AppLanguage.ID) "Tandai Dibaca" else "Mark Read",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonCyan,
                                modifier = Modifier.clickable { viewModel.markAllNotificationsRead() }
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        if (notifications.isEmpty()) {
                            Text(
                                text = if (prefs.language == AppLanguage.ID) "Belum ada notifikasi." else "No notifications yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 28.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 28.dp)
                            ) {
                                items(notifications, key = { it.id }) { n ->
                                    LiquidGlassCard {
                                        Text(n.title, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                                        Text(n.message, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 600.dp

                val mainScaffold: @Composable (Boolean) -> Unit = { showHamburger ->
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            VideonTopGlassBar(
                                currentRoute = currentRoute,
                                language = prefs.language,
                                isDark = prefs.isDarkMode,
                                unreadCount = notifications.count { !it.isRead },
                                showMenuButton = showHamburger,
                                onOpenMenu = { scope.launch { drawerState.open() } },
                                onToggleTheme = { viewModel.setDarkMode(!prefs.isDarkMode) },
                                onToggleLanguage = {
                                    viewModel.setLanguage(if (prefs.language == AppLanguage.ID) AppLanguage.EN else AppLanguage.ID)
                                },
                                onOpenNotifications = { showNotifications = true }
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen) {
                                VideonBottomGlassBar(
                                    currentRoute = currentRoute,
                                    language = prefs.language,
                                    onNavigate = ::navigateTo
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            VideonNavGraph(
                                navController = navController,
                                viewModel = viewModel,
                                language = prefs.language,
                                prefs = prefs,
                                projects = projects,
                                jobs = jobs,
                                characters = characters,
                                prompts = prompts,
                                downloads = downloads,
                                totalVideos = totalVideos,
                                totalImages = totalImages,
                                totalAudio = totalAudio,
                                storageBytes = storageBytes,
                                videoUi = videoUi,
                                imageUi = imageUi,
                                audioUi = audioUi,
                                promptUi = promptUi,
                                editorUi = editorUi,
                                selfHostedStatus = selfHostedStatus,
                                onNavigate = ::navigateTo
                            )
                        }
                    }
                }

                if (isWideScreen) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        VideonPersistentSidebar(
                            currentRoute = currentRoute,
                            language = prefs.language,
                            creatorName = prefs.userDisplayName,
                            onNavigate = ::navigateTo
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            mainScaffold(false)
                        }
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = LocalLiquidGlass.current.glassSurfaceElevated,
                                modifier = Modifier.width(290.dp)
                            ) {
                                SidebarContent(
                                    currentRoute = currentRoute,
                                    language = prefs.language,
                                    creatorName = prefs.userDisplayName,
                                    onNavigate = ::navigateTo
                                )
                            }
                        }
                    ) {
                        mainScaffold(true)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideonTopGlassBar(
    currentRoute: StudioRoute,
    language: AppLanguage,
    isDark: Boolean,
    unreadCount: Int,
    showMenuButton: Boolean,
    onOpenMenu: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val tokens = LocalLiquidGlass.current
    Surface(
        color = tokens.glassSurface,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, tokens.glassBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showMenuButton) {
                    IconButton(onClick = onOpenMenu, modifier = Modifier.testTag("open_sidebar_btn")) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                    }
                }
                Column(modifier = Modifier.padding(start = if (showMenuButton) 2.dp else 10.dp)) {
                    Text(
                        text = "VIDEON",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Text(
                        text = currentRoute.label(language),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onToggleLanguage)
                        .background(ElectricBlue.copy(alpha = 0.18f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("top_lang_toggle")
                ) {
                    Text(
                        text = language.code.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                }
                IconButton(onClick = onToggleTheme, modifier = Modifier.testTag("top_theme_toggle")) {
                    Icon(
                        imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = "Toggle Theme"
                    )
                }
                IconButton(onClick = onOpenNotifications, modifier = Modifier.testTag("top_notifications_btn")) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text("$unreadCount") }
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notifications")
                    }
                }
            }
        }
    }
}

@Composable
private fun VideonBottomGlassBar(
    currentRoute: StudioRoute,
    language: AppLanguage,
    onNavigate: (StudioRoute) -> Unit
) {
    val tokens = LocalLiquidGlass.current
    val primaryRoutes = StudioRoute.entries.filter { it.isPrimaryNav }

    Surface(
        color = tokens.glassSurfaceElevated,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, tokens.glassBorder, RoundedCornerShape(24.dp))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            primaryRoutes.forEach { route ->
                val selected = currentRoute == route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(route) },
                    icon = { Icon(iconForRoute(route), contentDescription = route.label(language)) },
                    label = {
                        Text(
                            text = route.label(language),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = ElectricBlue.copy(alpha = 0.22f)
                    ),
                    modifier = Modifier.testTag("nav_bottom_${route.route}")
                )
            }
        }
    }
}

@Composable
private fun VideonPersistentSidebar(
    currentRoute: StudioRoute,
    language: AppLanguage,
    creatorName: String,
    onNavigate: (StudioRoute) -> Unit
) {
    val tokens = LocalLiquidGlass.current
    Surface(
        color = tokens.glassSurfaceElevated,
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(10.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, tokens.glassBorder, RoundedCornerShape(24.dp))
    ) {
        SidebarContent(
            currentRoute = currentRoute,
            language = language,
            creatorName = creatorName,
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun SidebarContent(
    currentRoute: StudioRoute,
    language: AppLanguage,
    creatorName: String,
    onNavigate: (StudioRoute) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(ElectricBlue.copy(0.3f), DeepViolet.copy(0.35f))))
                .padding(14.dp)
        ) {
            Column {
                Text("VIDEON AI STUDIO", style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                Text(creatorName, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(10.dp))
        StudioRoute.entries.forEach { route ->
            NavigationDrawerItem(
                label = { Text(route.label(language)) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(iconForRoute(route), contentDescription = null) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = ElectricBlue.copy(alpha = 0.22f),
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan
                ),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("sidebar_${route.route}")
            )
        }
    }
}

@Composable
private fun VideonNavGraph(
    navController: NavHostController,
    viewModel: StudioViewModel,
    language: AppLanguage,
    prefs: com.example.data.local.StudioPreferences,
    projects: List<com.example.data.local.ProjectEntity>,
    jobs: List<com.example.data.local.GenerationJobEntity>,
    characters: List<com.example.data.local.CharacterEntity>,
    prompts: List<com.example.data.local.PromptTemplateEntity>,
    downloads: List<com.example.data.local.DownloadRecordEntity>,
    totalVideos: Int,
    totalImages: Int,
    totalAudio: Int,
    storageBytes: Long,
    videoUi: com.example.ui.viewmodel.VideoGeneratorUiState,
    imageUi: com.example.ui.viewmodel.ImageStudioUiState,
    audioUi: com.example.ui.viewmodel.AudioStudioUiState,
    promptUi: com.example.ui.viewmodel.PromptAssistantUiState,
    editorUi: com.example.domain.model.EditorTimelineState,
    selfHostedStatus: String?,
    onNavigate: (StudioRoute) -> Unit
) {
    NavHost(navController = navController, startDestination = StudioRoute.HOME.route) {
        composable(StudioRoute.HOME.route) {
            HomeScreen(language = language, jobs = jobs, onNavigate = onNavigate)
        }
        composable(StudioRoute.DASHBOARD.route) {
            DashboardScreen(
                language = language,
                totalVideos = totalVideos,
                totalImages = totalImages,
                totalAudio = totalAudio,
                storageBytes = storageBytes,
                jobs = jobs,
                projects = projects,
                downloads = downloads,
                onCancelJob = viewModel::cancelJob,
                onToggleFavorite = viewModel::toggleJobFavorite,
                onOpenProject = viewModel::openProjectInStudio,
                onNavigate = onNavigate
            )
        }
        composable(StudioRoute.AI_VIDEO.route) {
            AiVideoScreen(
                language = language,
                uiState = videoUi,
                characters = characters,
                jobs = jobs,
                onSelectMode = viewModel::updateVideoMode,
                onSelectModel = viewModel::updateVideoModel,
                onPromptChange = viewModel::updateVideoPrompt,
                onEnhancePrompt = viewModel::enhanceVideoPrompt,
                onSelectPrimaryMedia = viewModel::updateVideoRefImage,
                onSelectSecondaryMedia = viewModel::updateVideoSecondaryImage,
                onSelectCharacter = viewModel::updateVideoCharacter,
                onUpdateConfig = viewModel::updateVideoConfig,
                onSubmitRender = viewModel::submitVideoGeneration,
                onExportVideo = { path, title -> viewModel.exportMediaToDevice(path, title, MediaType.VIDEO) },
                onShareVideo = { path, title -> viewModel.shareMedia(path, MediaType.VIDEO, title) },
                onOpenInEditor = { path, title -> viewModel.openVideoInEditor(path, null, title) }
            )
        }
        composable(StudioRoute.AI_IMAGE.route) {
            AiImageScreen(
                language = language,
                uiState = imageUi,
                onSelectMode = viewModel::updateImageMode,
                onPromptChange = viewModel::updateImagePrompt,
                onSelectStyle = viewModel::updateImageStyle,
                onSelectAspect = viewModel::updateImageAspect,
                onSelectReference = viewModel::updateImageRefUri,
                onGenerate = viewModel::generateStudioImage,
                onSaveImage = { path, title -> viewModel.exportMediaToDevice(path, title, MediaType.IMAGE) },
                onShareImage = { path, title -> viewModel.shareMedia(path, MediaType.IMAGE, title) },
                onSendToVideo = viewModel::sendImageToVideoStudio
            )
        }
        composable(StudioRoute.AI_AUDIO.route) {
            AiAudioScreen(
                language = language,
                uiState = audioUi,
                onSelectMode = viewModel::updateAudioMode,
                onTextChange = viewModel::updateAudioText,
                onLangChange = viewModel::updateAudioLang,
                onPitchChange = viewModel::updateAudioPitch,
                onRateChange = viewModel::updateAudioRate,
                onPresetChange = viewModel::updateAudioPreset,
                onDurationChange = viewModel::updateAudioDuration,
                onSynthesize = viewModel::synthesizeAudio,
                onTogglePlay = viewModel::toggleAudioPlayback,
                onSaveAudio = { path, title -> viewModel.exportMediaToDevice(path, title, MediaType.AUDIO) },
                onShareAudio = { path, title -> viewModel.shareMedia(path, MediaType.AUDIO, title) },
                onSendToEditor = viewModel::sendAudioToEditor
            )
        }
        composable(StudioRoute.VIDEO_EDITOR.route) {
            VideoEditorScreen(
                language = language,
                state = editorUi,
                completedVideos = jobs,
                onUpdateState = viewModel::updateEditorState,
                onExportMp4 = viewModel::exportEditedVideo,
                onSaveToGallery = { path, title -> viewModel.exportMediaToDevice(path, title, MediaType.VIDEO) },
                onShareVideo = { path, title -> viewModel.shareMedia(path, MediaType.VIDEO, title) }
            )
        }
        composable(StudioRoute.CHARACTER_STUDIO.route) {
            CharacterStudioScreen(
                language = language,
                characters = characters,
                onSaveCharacter = viewModel::saveCharacter,
                onDeleteCharacter = viewModel::deleteCharacter,
                onUseInVideoStudio = viewModel::useCharacterInVideoStudio,
                onGenerateConsistentScenes = viewModel::generateCharacterConsistentScenes
            )
        }
        composable(StudioRoute.PROMPT_LIBRARY.route) {
            PromptLibraryScreen(
                language = language,
                assistantState = promptUi,
                templates = prompts,
                onTopicChange = viewModel::updatePromptAssistantTopic,
                onStyleChange = viewModel::updatePromptAssistantStyle,
                onCameraChange = viewModel::updatePromptAssistantCamera,
                onRunEnhanceAndTranslate = viewModel::runPromptEnhanceAndTranslate,
                onRunStoryboard = viewModel::runStoryboardGenerator,
                onRunImageToPrompt = viewModel::runImageToPrompt,
                onUsePrompt = viewModel::usePromptInVideoStudio,
                onToggleFavorite = viewModel::togglePromptFavorite
            )
        }
        composable(StudioRoute.MY_PROJECTS.route) {
            MyProjectsScreen(
                language = language,
                projects = projects,
                onOpenProject = viewModel::openProjectInStudio,
                onOpenInEditor = { path, title -> viewModel.openVideoInEditor(path, null, title) },
                onToggleFavorite = viewModel::toggleProjectFavorite,
                onDeleteProject = viewModel::deleteProject,
                onShareFile = viewModel::shareMedia
            )
        }
        composable(StudioRoute.HISTORY.route) {
            HistoryScreen(
                language = language,
                jobs = jobs,
                downloads = downloads,
                onToggleFavorite = viewModel::toggleJobFavorite,
                onCancelJob = viewModel::cancelJob,
                onDeleteJob = viewModel::deleteJob,
                onExportMedia = viewModel::exportMediaToDevice,
                onShareMedia = viewModel::shareMedia,
                onOpenInEditor = { path, title -> viewModel.openVideoInEditor(path, null, title) }
            )
        }
        composable(StudioRoute.SETTINGS.route) {
            SettingsScreen(
                prefs = prefs,
                selfHostedStatus = selfHostedStatus,
                onDarkModeChange = viewModel::setDarkMode,
                onLanguageChange = viewModel::setLanguage,
                onSelfHostedUrlChange = viewModel::setSelfHostedEndpoint,
                onTestSelfHosted = viewModel::testSelfHostedServer,
                onUserNameChange = viewModel::setUserDisplayName,
                onDefaultFpsChange = viewModel::setDefaultFps,
                onAutosaveChange = viewModel::setAutosaveEnabled
            )
        }
        composable(StudioRoute.HELP_CENTER.route) {
            HelpCenterScreen(language = language)
        }
    }
}
