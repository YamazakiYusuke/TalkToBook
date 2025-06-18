package com.example.talktobook.domain.processor

import androidx.navigation.NavController
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.model.VoiceCommandResult
import com.example.talktobook.ui.navigation.Screen
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes voice commands and executes appropriate actions
 */
@Singleton
class VoiceCommandProcessor @Inject constructor() {

    /**
     * Process a voice command and execute the appropriate action
     */
    suspend fun processCommand(
        command: VoiceCommand,
        navController: NavController? = null,
        context: VoiceCommandContext? = null
    ): VoiceCommandResult {
        return try {
            when (command) {
                // Navigation Commands
                is VoiceCommand.GoBack -> processGoBack(navController)
                is VoiceCommand.GoToDocuments -> processGoToDocuments(navController)
                is VoiceCommand.GoToMain -> processGoToMain(navController)
                is VoiceCommand.OpenChapter -> processOpenChapter(command.chapterNumber, navController, context)
                is VoiceCommand.OpenDocument -> processOpenDocument(command.documentName, navController, context)
                
                // Recording Commands
                is VoiceCommand.StartRecording -> processStartRecording(context)
                is VoiceCommand.StopRecording -> processStopRecording(context)
                is VoiceCommand.PauseRecording -> processPauseRecording(context)
                is VoiceCommand.ResumeRecording -> processResumeRecording(context)
                
                // Text Editing Commands
                is VoiceCommand.SelectAll -> processSelectAll(context)
                is VoiceCommand.DeleteSelection -> processDeleteSelection(context)
                is VoiceCommand.InsertText -> processInsertText(command.text, context)
                is VoiceCommand.UndoLastAction -> processUndoLastAction(context)
                
                // Document Management Commands
                is VoiceCommand.SaveDocument -> processSaveDocument(context)
                is VoiceCommand.CreateNewDocument -> processCreateNewDocument(navController, context)
                is VoiceCommand.CreateNewChapter -> processCreateNewChapter(navController, context)
                is VoiceCommand.DeleteDocument -> processDeleteDocument(command.documentName, context)
                
                // Reading Commands
                is VoiceCommand.ReadAloud -> processReadAloud(context)
                is VoiceCommand.StopReading -> processStopReading(context)
                
                // Unknown Command
                is VoiceCommand.Unknown -> processUnknownCommand(command.originalCommand)
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = command,
                isSuccess = false,
                message = "コマンドの実行中にエラーが発生しました: ${e.message}"
            )
        }
    }

    // Navigation Command Processors
    private fun processGoBack(navController: NavController?): VoiceCommandResult {
        return if (navController?.popBackStack() == true) {
            VoiceCommandResult(
                command = VoiceCommand.GoBack,
                isSuccess = true,
                message = "前の画面に戻りました"
            )
        } else {
            VoiceCommandResult(
                command = VoiceCommand.GoBack,
                isSuccess = false,
                message = "戻る画面がありません"
            )
        }
    }

    private fun processGoToDocuments(navController: NavController?): VoiceCommandResult {
        return try {
            navController?.navigate(Screen.DocumentList.route)
            VoiceCommandResult(
                command = VoiceCommand.GoToDocuments,
                isSuccess = true,
                message = "ドキュメント一覧を開きました"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.GoToDocuments,
                isSuccess = false,
                message = "ドキュメント一覧を開けませんでした"
            )
        }
    }

    private fun processGoToMain(navController: NavController?): VoiceCommandResult {
        return try {
            navController?.navigate(Screen.Main.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
            VoiceCommandResult(
                command = VoiceCommand.GoToMain,
                isSuccess = true,
                message = "メイン画面を開きました"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.GoToMain,
                isSuccess = false,
                message = "メイン画面を開けませんでした"
            )
        }
    }

    private suspend fun processOpenChapter(
        chapterNumber: Int,
        navController: NavController?,
        context: VoiceCommandContext?
    ): VoiceCommandResult {
        return try {
            // For now, return a placeholder implementation
            // This would need to be implemented with proper Flow handling
            VoiceCommandResult(
                command = VoiceCommand.OpenChapter(chapterNumber),
                isSuccess = false,
                message = "章の操作は現在実装中です"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.OpenChapter(chapterNumber),
                isSuccess = false,
                message = "章を開けませんでした"
            )
        }
    }

    private suspend fun processOpenDocument(
        documentName: String,
        navController: NavController?,
        context: VoiceCommandContext?
    ): VoiceCommandResult {
        return try {
            // For now, return a placeholder implementation
            // This would need to be implemented with proper Flow handling
            VoiceCommandResult(
                command = VoiceCommand.OpenDocument(documentName),
                isSuccess = false,
                message = "ドキュメントの操作は現在実装中です"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.OpenDocument(documentName),
                isSuccess = false,
                message = "ドキュメントを開けませんでした"
            )
        }
    }

    // Recording Command Processors
    private suspend fun processStartRecording(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val result = context?.recordingViewModel?.startRecording()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.StartRecording,
                    isSuccess = true,
                    message = "録音を開始しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.StartRecording,
                    isSuccess = false,
                    message = "録音を開始できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.StartRecording,
                isSuccess = false,
                message = "録音の開始中にエラーが発生しました"
            )
        }
    }

    private suspend fun processStopRecording(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val result = context?.recordingViewModel?.stopRecording()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.StopRecording,
                    isSuccess = true,
                    message = "録音を停止しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.StopRecording,
                    isSuccess = false,
                    message = "録音を停止できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.StopRecording,
                isSuccess = false,
                message = "録音の停止中にエラーが発生しました"
            )
        }
    }

    private suspend fun processPauseRecording(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val result = context?.recordingViewModel?.pauseRecording()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.PauseRecording,
                    isSuccess = true,
                    message = "録音を一時停止しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.PauseRecording,
                    isSuccess = false,
                    message = "録音を一時停止できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.PauseRecording,
                isSuccess = false,
                message = "録音の一時停止中にエラーが発生しました"
            )
        }
    }

    private suspend fun processResumeRecording(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val result = context?.recordingViewModel?.resumeRecording()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.ResumeRecording,
                    isSuccess = true,
                    message = "録音を再開しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.ResumeRecording,
                    isSuccess = false,
                    message = "録音を再開できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.ResumeRecording,
                isSuccess = false,
                message = "録音の再開中にエラーが発生しました"
            )
        }
    }

    // Text Editing Command Processors
    private fun processSelectAll(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            context?.textEditingContext?.selectAll()
            VoiceCommandResult(
                command = VoiceCommand.SelectAll,
                isSuccess = true,
                message = "すべてのテキストを選択しました"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.SelectAll,
                isSuccess = false,
                message = "テキストの選択中にエラーが発生しました"
            )
        }
    }

    private fun processDeleteSelection(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val deleted = context?.textEditingContext?.deleteSelection()
            if (deleted == true) {
                VoiceCommandResult(
                    command = VoiceCommand.DeleteSelection,
                    isSuccess = true,
                    message = "選択したテキストを削除しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.DeleteSelection,
                    isSuccess = false,
                    message = "削除するテキストが選択されていません"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.DeleteSelection,
                isSuccess = false,
                message = "テキストの削除中にエラーが発生しました"
            )
        }
    }

    private fun processInsertText(text: String, context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            context?.textEditingContext?.insertText(text)
            VoiceCommandResult(
                command = VoiceCommand.InsertText(text),
                isSuccess = true,
                message = "テキストを挿入しました"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.InsertText(text),
                isSuccess = false,
                message = "テキストの挿入中にエラーが発生しました"
            )
        }
    }

    private fun processUndoLastAction(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val undone = context?.textEditingContext?.undo()
            if (undone == true) {
                VoiceCommandResult(
                    command = VoiceCommand.UndoLastAction,
                    isSuccess = true,
                    message = "操作を元に戻しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.UndoLastAction,
                    isSuccess = false,
                    message = "元に戻せる操作がありません"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.UndoLastAction,
                isSuccess = false,
                message = "操作を元に戻すことができませんでした"
            )
        }
    }

    // Document Management Command Processors
    private suspend fun processSaveDocument(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val result = context?.documentViewModel?.saveDocument()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.SaveDocument,
                    isSuccess = true,
                    message = "ドキュメントを保存しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.SaveDocument,
                    isSuccess = false,
                    message = "ドキュメントを保存できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.SaveDocument,
                isSuccess = false,
                message = "ドキュメントの保存中にエラーが発生しました"
            )
        }
    }

    private suspend fun processCreateNewDocument(
        navController: NavController?,
        context: VoiceCommandContext?
    ): VoiceCommandResult {
        return try {
            val result = context?.documentViewModel?.createNewDocument()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.CreateNewDocument,
                    isSuccess = true,
                    message = "新しいドキュメントを作成しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.CreateNewDocument,
                    isSuccess = false,
                    message = "新しいドキュメントを作成できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.CreateNewDocument,
                isSuccess = false,
                message = "ドキュメントの作成中にエラーが発生しました"
            )
        }
    }

    private suspend fun processCreateNewChapter(
        navController: NavController?,
        context: VoiceCommandContext?
    ): VoiceCommandResult {
        return try {
            val result = context?.chapterViewModel?.createNewChapter()
            if (result?.isSuccess == true) {
                VoiceCommandResult(
                    command = VoiceCommand.CreateNewChapter,
                    isSuccess = true,
                    message = "新しい章を作成しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.CreateNewChapter,
                    isSuccess = false,
                    message = "新しい章を作成できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.CreateNewChapter,
                isSuccess = false,
                message = "章の作成中にエラーが発生しました"
            )
        }
    }

    private suspend fun processDeleteDocument(
        documentName: String,
        context: VoiceCommandContext?
    ): VoiceCommandResult {
        return try {
            // This would require confirmation in a real implementation
            VoiceCommandResult(
                command = VoiceCommand.DeleteDocument(documentName),
                isSuccess = false,
                message = "ドキュメントの削除には確認が必要です"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.DeleteDocument(documentName),
                isSuccess = false,
                message = "ドキュメントの削除中にエラーが発生しました"
            )
        }
    }

    // Reading Command Processors
    private suspend fun processReadAloud(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            val result = context?.textToSpeechContext?.startReading()
            if (result == true) {
                VoiceCommandResult(
                    command = VoiceCommand.ReadAloud,
                    isSuccess = true,
                    message = "読み上げを開始しました"
                )
            } else {
                VoiceCommandResult(
                    command = VoiceCommand.ReadAloud,
                    isSuccess = false,
                    message = "読み上げを開始できませんでした"
                )
            }
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.ReadAloud,
                isSuccess = false,
                message = "読み上げの開始中にエラーが発生しました"
            )
        }
    }

    private suspend fun processStopReading(context: VoiceCommandContext?): VoiceCommandResult {
        return try {
            context?.textToSpeechContext?.stopReading()
            VoiceCommandResult(
                command = VoiceCommand.StopReading,
                isSuccess = true,
                message = "読み上げを停止しました"
            )
        } catch (e: Exception) {
            VoiceCommandResult(
                command = VoiceCommand.StopReading,
                isSuccess = false,
                message = "読み上げの停止中にエラーが発生しました"
            )
        }
    }

    private fun processUnknownCommand(originalCommand: String): VoiceCommandResult {
        return VoiceCommandResult(
            command = VoiceCommand.Unknown(originalCommand),
            isSuccess = false,
            message = "「$originalCommand」は認識できませんでした。利用可能なコマンドを確認してください。"
        )
    }
}