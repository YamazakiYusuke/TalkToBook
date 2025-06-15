package com.example.talktobook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
            PlaceholderRecordingScreen(
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
                }
            )
        }
        
        composable(Screen.DocumentMerge.route) {
            DocumentMergeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDocument = { documentId ->
                    navController.navigate(Screen.DocumentDetail.createRoute(documentId))
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
private fun MainScreen(
    onNavigateToRecording: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    TalkToBookScreen(title = "TalkToBook") {
        TalkToBookPrimaryButton(
            text = "Start Recording",
            onClick = onNavigateToRecording
        )
        TalkToBookPrimaryButton(
            text = "View Documents",
            onClick = onNavigateToDocuments
        )
        TalkToBookPrimaryButton(
            text = "Settings",
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun PlaceholderRecordingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProcessing: () -> Unit
) {
    TalkToBookScreen(title = "Recording") {
        TalkToBookPrimaryButton(
            text = "Start Processing (Demo)",
            onClick = onNavigateToProcessing
        )
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}


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
private fun DocumentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    onNavigateToMerge: () -> Unit
) {
    TalkToBookScreen(title = "Documents") {
        TalkToBookPrimaryButton(
            text = "Open Document (Demo)",
            onClick = { onNavigateToDocument("demo-document-id") }
        )
        TalkToBookPrimaryButton(
            text = "Merge Documents",
            onClick = onNavigateToMerge
        )
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun DocumentDetailScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapters: () -> Unit
) {
    TalkToBookScreen(title = "Document Detail") {
        TalkToBookPrimaryButton(
            text = "View Chapters",
            onClick = onNavigateToChapters
        )
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun DocumentMergeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocument: (String) -> Unit
) {
    TalkToBookScreen(title = "Merge Documents") {
        TalkToBookPrimaryButton(
            text = "Confirm Merge (Demo)",
            onClick = { onNavigateToDocument("merged-document-id") }
        )
        TalkToBookPrimaryButton(
            text = "Cancel",
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun ChapterListScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (String) -> Unit
) {
    TalkToBookScreen(title = "Chapters") {
        TalkToBookPrimaryButton(
            text = "Edit Chapter (Demo)",
            onClick = { onNavigateToChapter("demo-chapter-id") }
        )
        TalkToBookPrimaryButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun ChapterEditScreen(
    chapterId: String,
    onNavigateBack: () -> Unit
) {
    TalkToBookScreen(title = "Edit Chapter") {
        TalkToBookPrimaryButton(
            text = "Save Chapter",
            onClick = onNavigateBack
        )
        TalkToBookPrimaryButton(
            text = "Cancel",
            onClick = onNavigateBack
        )
    }
}

@Composable
private fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    TalkToBookScreen(title = "Settings") {
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