package com.example.talktobook.domain.usecase

import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.DeleteChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChaptersByDocumentUseCase
import com.example.talktobook.domain.usecase.chapter.MergeChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.ReorderChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase

data class ChapterUseCases(
    val createChapter: CreateChapterUseCase,
    val updateChapter: UpdateChapterUseCase,
    val getChapter: GetChapterUseCase,
    val getChaptersByDocument: GetChaptersByDocumentUseCase,
    val deleteChapter: DeleteChapterUseCase,
    val reorderChapters: ReorderChaptersUseCase,
    val mergeChapters: MergeChaptersUseCase
)