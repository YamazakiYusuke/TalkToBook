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
import com.example.talktobook.presentation.screen.RecordingScreen
import com.example.talktobook.presentation.screen.TextViewScreen
import com.example.talktobook.presentation.screen.DocumentDetailScreen
import com.example.talktobook.presentation.screen.document.DocumentListScreen
import com.example.talktobook.presentation.screen.ChapterEditScreen
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
        // Main Screen - Placeholder for now
        composable(Screen.Main.route) {
            TalkToBookScreen(
                title = "TalkToBook",
                scrollable = false
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    TalkToBookPrimaryButton(
                        text = "Start Recording",
                        onClick = {
                            navController.navigate(Screen.Recording.route)
                        }
                    )
                    
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    
                    TalkToBookPrimaryButton(
                        text = "View Documents",
                        onClick = {
                            navController.navigate(Screen.DocumentList.route)
                        }
                    )
                }
            }
        }

        // Recording Screen
        composable(Screen.Recording.route) {
            RecordingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTextView = { recordingId ->
                    navController.navigate(Screen.TextEdit.createRoute(recordingId))
                }
            )
        }

        // Text Editing Screen
        composable(
            route = Screen.TextEdit.route,
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
        
        composable(Screen.DocumentList.route) {
            DocumentListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocument = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                }
            )
        }
        
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
                onNavigateToChapters = {
                    navController.navigate(Screen.ChapterList.createRoute(documentId))
                },
                onNavigateToChapterEdit = { chapterId ->
                    navController.navigate(Screen.ChapterEdit.createRoute(chapterId))
                }
            )
        }
        
        composable(Screen.DocumentMerge.route) {
            DocumentMergeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
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
                onNavigateToChapterEdit = { chapterId ->
                    navController.navigate(Screen.ChapterEdit.createRoute(chapterId))
                }
            )
        }
        
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
    }
}

// Screen definitions
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Recording : Screen("recording")
    object TextEdit : Screen("text_edit/{$RECORDING_ID_KEY}") {
        fun createRoute(recordingId: String) = "text_edit/$recordingId"
    }
    object DocumentList : Screen("document_list")
    object DocumentDetail : Screen("document_detail/{$DOCUMENT_ID_KEY}") {
        fun createRoute(documentId: String) = "document_detail/$documentId"
    }
    object DocumentMerge : Screen("document_merge")
    object ChapterList : Screen("chapter_list/{$DOCUMENT_ID_KEY}") {
        fun createRoute(documentId: String) = "chapter_list/$documentId"
    }
    object ChapterEdit : Screen("chapter_edit/{$CHAPTER_ID_KEY}") {
        fun createRoute(chapterId: String) = "chapter_edit/$chapterId"
    }
}

// Navigation argument keys
const val RECORDING_ID_KEY = "recordingId"
const val DOCUMENT_ID_KEY = "documentId"
const val CHAPTER_ID_KEY = "chapterId"

// Placeholder composables for missing screens
@Composable
private fun DocumentMergeScreen(onNavigateBack: () -> Unit) {
    TalkToBookScreen(title = "Document Merge") {
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun ChapterListScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapterEdit: (String) -> Unit
) {
    TalkToBookScreen(title = "Chapters") {
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}