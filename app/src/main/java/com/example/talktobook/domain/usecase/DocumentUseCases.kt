package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentByIdUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.DeleteChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChaptersByDocumentUseCase
import com.example.talktobook.domain.usecase.chapter.MergeChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.ReorderChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase

data class DocumentUseCases(
    // Document operations
    val createDocument: CreateDocumentUseCase,
    val updateDocument: UpdateDocumentUseCase,
    val getDocument: GetDocumentUseCase,
    val getDocumentById: GetDocumentByIdUseCase,
    val deleteDocument: DeleteDocumentUseCase,
    val getAllDocuments: GetAllDocumentsUseCase,
    
    // Chapter operations
    val createChapter: CreateChapterUseCase,
    val updateChapter: UpdateChapterUseCase,
    val getChapter: GetChapterUseCase,
    val getChaptersByDocument: GetChaptersByDocumentUseCase,
    val deleteChapter: DeleteChapterUseCase,
    val reorderChapters: ReorderChaptersUseCase,
    val mergeChapters: MergeChaptersUseCase
)