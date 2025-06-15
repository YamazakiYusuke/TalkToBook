package com.example.talktobook.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Recording : Screen("recording")
    data object Processing : Screen("processing")
    data object TextView : Screen("text_view/{recordingId}") {
        fun createRoute(recordingId: String) = "text_view/$recordingId"
    }
    data object DocumentList : Screen("document_list")
    data object DocumentDetail : Screen("document_detail/{documentId}") {
        fun createRoute(documentId: String) = "document_detail/$documentId"
    }
    data object DocumentMerge : Screen("document_merge")
    data object ChapterList : Screen("chapter_list/{documentId}") {
        fun createRoute(documentId: String) = "chapter_list/$documentId"
    }
    data object ChapterEdit : Screen("chapter_edit/{chapterId}") {
        fun createRoute(chapterId: String) = "chapter_edit/$chapterId"
    }
    data object Settings : Screen("settings")
    data object Error : Screen("error")
}

const val RECORDING_ID_KEY = "recordingId"
const val DOCUMENT_ID_KEY = "documentId"
const val CHAPTER_ID_KEY = "chapterId"