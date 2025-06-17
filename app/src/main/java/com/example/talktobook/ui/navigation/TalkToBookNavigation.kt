package com.example.talktobook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.example.talktobook.presentation.screen.document.DocumentListScreen
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
        
        composable(Screen.Recording.route) {
            RecordingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProcessing = {
                    navController.navigate(Screen.Processing.route)
                }
            )
        }
        
        composable(Screen.Processing.route) {
            ProcessingScreen(
                onNavigateToTextView = { recordingId ->
                    navController.navigate(Screen.TextView.createRoute(recordingId))
                },
                onNavigateToError = {
                    navController.navigate(Screen.Error.route)
                }
            )
        }
        
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
                onNavigateToMergeWithSelection = { selectedIds ->
                    val selectedIdsParam = selectedIds.joinToString(",")
                    navController.navigate("${Screen.DocumentMerge.route}?selectedIds=$selectedIdsParam")
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
                onNavigateToChapters = { docId ->
                    navController.navigate(Screen.ChapterList.createRoute(docId))
                }
            )
        }
        
        composable(
            route = "${Screen.DocumentMerge.route}?selectedIds={selectedIds}",
            arguments = listOf(navArgument("selectedIds") { 
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val selectedIdsParam = backStackEntry.arguments?.getString("selectedIds") ?: ""
            val selectedIds = if (selectedIdsParam.isNotEmpty()) {
                selectedIdsParam.split(",")
            } else {
                emptyList()
            }
            
            DocumentMergeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocument = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
                },
                selectedDocumentIds = selectedIds
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
                onNavigateToChapter = { chapterId ->
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
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Error.route) {
            ErrorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Placeholder screens for demonstration

@Composable
private fun ProcessingScreen(
    onNavigateToTextView: (String) -> Unit,
    onNavigateToError: () -> Unit
) {
    TalkToBookScreen(title = "Processing...") {
        TalkToBookPrimaryButton(
            text = "View Text (Demo)",
            onClick = { onNavigateToTextView("demo-recording-id") }
        )
        TalkToBookPrimaryButton(
            text = "Simulate Error",
            onClick = onNavigateToError
        )
    }
}

@Composable
private fun TextViewScreen(
    recordingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: () -> Unit
) {
    TalkToBookScreen(title = "Text View") {
        TalkToBookPrimaryButton(
            text = "Save to Documents",
            onClick = onNavigateToDocuments
        )
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}






@Composable
private fun ErrorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    TalkToBookScreen(title = "Error") {
        TalkToBookPrimaryButton(
            text = "Retry",
            onClick = onNavigateBack
        )
        TalkToBookPrimaryButton(
            text = "Home",
            onClick = onNavigateToMain
        )
    }
}