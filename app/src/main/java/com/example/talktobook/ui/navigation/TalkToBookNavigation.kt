package com.example.talktobook.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.talktobook.presentation.screen.MainScreen
import com.example.talktobook.presentation.screen.RecordingScreen
import com.example.talktobook.presentation.screen.SettingsScreen
import com.example.talktobook.presentation.screen.TextViewScreen
import com.example.talktobook.presentation.screen.DocumentListScreen
import com.example.talktobook.presentation.screen.document.DocumentDetailScreen
import com.example.talktobook.presentation.screen.chapter.ChapterListScreen
import com.example.talktobook.presentation.screen.chapter.ChapterEditScreen
import com.example.talktobook.presentation.screen.DocumentMergeScreen
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookPrimaryButton

@Composable
fun TalkToBookNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Main Screen
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToRecording = {
                    navController.navigate(Screen.Recording.route)
                },
                onNavigateToDocuments = {
                    navController.navigate(Screen.DocumentList.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Recording Screen
        composable(Screen.Recording.route) {
            RecordingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProcessing = { recordingId ->
                    navController.navigate(Screen.TextView.createRoute(recordingId))
                }
            )
        }

        // Text Editing Screen
        composable(
            route = Screen.TextView.route,
            arguments = listOf(navArgument(RECORDING_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getString(RECORDING_ID_KEY) ?: ""
            TextViewScreen(
                recordingId = recordingId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocuments = {
                    navController.navigate(Screen.DocumentList.route)
                }
            )
        }
        
        // Document List Screen
        composable(Screen.DocumentList.route) {
            DocumentListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocument = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                },
                onNavigateToMerge = {
                    navController.navigate(Screen.DocumentMerge.route)
                },
                onNavigateToMergeWithSelection = { selectedIds: List<String> ->
                    val idsString = selectedIds.joinToString(",")
                    navController.navigate("${Screen.DocumentMerge.route}?selectedIds=$idsString")
                }
            )
        }
        
        // Document Detail Screen
        composable(
            route = Screen.DocumentDetail.route,
            arguments = listOf(navArgument(DOCUMENT_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString(DOCUMENT_ID_KEY) ?: ""
            DocumentDetailScreen(
                documentId = documentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChapters = { documentId ->
                    navController.navigate(Screen.ChapterList.createRoute(documentId))
                }
            )
        }
        
        // Document Merge Screen
        composable(
            route = "${Screen.DocumentMerge.route}?selectedIds={selectedIds}",
            arguments = listOf(navArgument("selectedIds") { 
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val selectedIdsString = backStackEntry.arguments?.getString("selectedIds") ?: ""
            val selectedIds = if (selectedIdsString.isNotEmpty()) {
                selectedIdsString.split(",")
            } else {
                emptyList()
            }
            
            DocumentMergeScreen(
                selectedDocumentIds = selectedIds,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocument = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                }
            )
        }
        
        // Chapter List Screen
        composable(
            route = Screen.ChapterList.route,
            arguments = listOf(navArgument(DOCUMENT_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString(DOCUMENT_ID_KEY) ?: ""
            ChapterListScreen(
                documentId = documentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChapter = { chapterId ->
                    navController.navigate(Screen.ChapterEdit.createRoute(chapterId))
                }
            )
        }
        
        // Chapter Edit Screen
        composable(
            route = Screen.ChapterEdit.route,
            arguments = listOf(navArgument(CHAPTER_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString(CHAPTER_ID_KEY) ?: ""
            ChapterEditScreen(
                chapterId = chapterId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}