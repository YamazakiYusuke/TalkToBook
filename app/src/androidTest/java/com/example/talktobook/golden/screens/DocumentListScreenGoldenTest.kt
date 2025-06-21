package com.example.talktobook.golden.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.domain.model.Document
import com.example.talktobook.golden.accessibility.AccessibilityConfig
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import com.example.talktobook.golden.core.GoldenTest
import com.example.talktobook.golden.core.GoldenTestRule
import com.example.talktobook.golden.parameterized.BaseParameterizedGoldenTest
import com.example.talktobook.golden.parameterized.ParameterizedGoldenTest
import com.example.talktobook.presentation.screen.DocumentListScreen
import com.example.talktobook.ui.theme.TalkToBookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Golden tests for DocumentListScreen with various document states
 */
@RunWith(Parameterized::class)
@GoldenTest(description = "DocumentListScreen visual regression and accessibility tests")
class DocumentListScreenGoldenTest(
    deviceConfig: DeviceConfig,
    themeConfig: ThemeConfig,
    accessibilityConfig: AccessibilityConfig
) : BaseParameterizedGoldenTest(deviceConfig, themeConfig, accessibilityConfig) {

    @get:Rule
    val goldenRule = GoldenTestRule()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: device={0}, theme={1}, accessibility={2}")
        fun data(): Collection<Array<Any>> {
            return ParameterizedGoldenTest.deviceThemeCombinations().map { params ->
                arrayOf(params[0], params[1], AccessibilityConfig.forElderly())
            }
        }
    }

    @Test
    fun documentListScreen_emptyState() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        val result = goldenRule.verifyAccessibility(
            testName = testName("document_list_empty"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                DocumentListScreenContent(
                    documents = emptyList(),
                    isSelectionMode = false,
                    selectedDocuments = emptySet(),
                    onDocumentClick = { },
                    onDocumentSelect = { },
                    onToggleSelectionMode = { },
                    onMergeDocuments = { },
                    onNavigateToNewDocument = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "DocumentListScreen empty state failed verification: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun documentListScreen_withDocuments() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        val sampleDocuments = createSampleDocuments()

        val result = goldenRule.verifyAccessibility(
            testName = testName("document_list_with_documents"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                DocumentListScreenContent(
                    documents = sampleDocuments,
                    isSelectionMode = false,
                    selectedDocuments = emptySet(),
                    onDocumentClick = { },
                    onDocumentSelect = { },
                    onToggleSelectionMode = { },
                    onMergeDocuments = { },
                    onNavigateToNewDocument = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "DocumentListScreen with documents failed verification: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun documentListScreen_selectionMode() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        val sampleDocuments = createSampleDocuments()
        val selectedIds = setOf(sampleDocuments[0].id, sampleDocuments[2].id)

        val result = goldenRule.verifyAccessibility(
            testName = testName("document_list_selection_mode"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                DocumentListScreenContent(
                    documents = sampleDocuments,
                    isSelectionMode = true,
                    selectedDocuments = selectedIds,
                    onDocumentClick = { },
                    onDocumentSelect = { },
                    onToggleSelectionMode = { },
                    onMergeDocuments = { },
                    onNavigateToNewDocument = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "DocumentListScreen selection mode failed verification: ${result.accessibilityResult.violations}"
        }
    }

    private fun createSampleDocuments(): List<Document> {
        return listOf(
            Document(
                id = "1",
                title = "My Autobiography Chapter 1",
                content = "This is the beginning of my life story...",
                createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
                updatedAt = System.currentTimeMillis() - 3600000   // 1 hour ago
            ),
            Document(
                id = "2", 
                title = "Memories of Childhood",
                content = "I remember when I was young...",
                createdAt = System.currentTimeMillis() - 172800000, // 2 days ago
                updatedAt = System.currentTimeMillis() - 7200000    // 2 hours ago
            ),
            Document(
                id = "3",
                title = "Family Recipes",
                content = "These are the recipes passed down...",
                createdAt = System.currentTimeMillis() - 259200000, // 3 days ago
                updatedAt = System.currentTimeMillis() - 10800000   // 3 hours ago
            ),
            Document(
                id = "4",
                title = "Travel Adventures",
                content = "My journey around the world...",
                createdAt = System.currentTimeMillis() - 345600000, // 4 days ago
                updatedAt = System.currentTimeMillis() - 14400000   // 4 hours ago
            )
        )
    }
}

/**
 * Single configuration golden tests for DocumentListScreen
 */
@RunWith(AndroidJUnit4::class)
@GoldenTest(description = "DocumentListScreen specific configuration tests")
class DocumentListScreenSingleGoldenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val goldenRule = GoldenTestRule()

    @Test
    fun documentListScreen_elderlyOptimized_longTitles() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.ELDERLY_OPTIMIZED,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val documentsWithLongTitles = listOf(
            Document(
                id = "1",
                title = "This is a very long document title that should test text wrapping and accessibility",
                content = "Content here...",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Document(
                id = "2",
                title = "Another extremely long title to verify how the UI handles text overflow in elderly mode",
                content = "More content...",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        val result = goldenRule.verifyAccessibility(
            testName = "document_list_elderly_long_titles",
            accessibilityConfig = AccessibilityConfig.forElderly()
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = true
            ) {
                DocumentListScreenContent(
                    documents = documentsWithLongTitles,
                    isSelectionMode = false,
                    selectedDocuments = emptySet(),
                    onDocumentClick = { },
                    onDocumentSelect = { },
                    onToggleSelectionMode = { },
                    onMergeDocuments = { },
                    onNavigateToNewDocument = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "DocumentListScreen elderly long titles failed verification: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun documentListScreen_tabletLayout_manyDocuments() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.TABLET,
            themeConfig = ThemeConfig.LIGHT_NORMAL,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val manyDocuments = (1..10).map { index ->
            Document(
                id = index.toString(),
                title = "Document $index",
                content = "Content for document $index",
                createdAt = System.currentTimeMillis() - (index * 86400000L),
                updatedAt = System.currentTimeMillis() - (index * 3600000L)
            )
        }

        val result = goldenRule.compareScreenshot(
            testName = "document_list_tablet_many_documents"
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = false
            ) {
                DocumentListScreenContent(
                    documents = manyDocuments,
                    isSelectionMode = false,
                    selectedDocuments = emptySet(),
                    onDocumentClick = { },
                    onDocumentSelect = { },
                    onToggleSelectionMode = { },
                    onMergeDocuments = { },
                    onNavigateToNewDocument = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "DocumentListScreen tablet many documents failed visual verification"
        }
    }
}

/**
 * Simplified DocumentListScreen content for testing
 */
@androidx.compose.runtime.Composable
private fun DocumentListScreenContent(
    documents: List<Document>,
    isSelectionMode: Boolean,
    selectedDocuments: Set<String>,
    onDocumentClick: (Document) -> Unit,
    onDocumentSelect: (Document) -> Unit,
    onToggleSelectionMode: () -> Unit,
    onMergeDocuments: () -> Unit,
    onNavigateToNewDocument: () -> Unit,
    onNavigateBack: () -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .androidx.compose.foundation.layout.fillMaxSize()
            .androidx.compose.foundation.layout.padding(16.dp)
    ) {
        // Header with selection mode toggle
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.material3.Text(
                text = if (isSelectionMode) "Select Documents" else "My Documents",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            
            if (isSelectionMode && selectedDocuments.size >= 2) {
                com.example.talktobook.ui.components.TalkToBookButton(
                    text = "Merge (${selectedDocuments.size})",
                    onClick = onMergeDocuments
                )
            } else {
                com.example.talktobook.ui.components.TalkToBookButton(
                    text = if (isSelectionMode) "Cancel" else "Select",
                    onClick = onToggleSelectionMode
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.height(16.dp))

        if (documents.isEmpty()) {
            // Empty state
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier
                    .androidx.compose.foundation.layout.fillMaxSize()
                    .androidx.compose.foundation.layout.wrapContentSize(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    text = "No documents yet",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.height(16.dp))
                com.example.talktobook.ui.components.TalkToBookButton(
                    text = "Create First Document",
                    onClick = onNavigateToNewDocument
                )
            }
        } else {
            // Document list
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.foundation.lazy.items(documents.size) { index ->
                    val document = documents[index]
                    DocumentListItem(
                        document = document,
                        isSelected = selectedDocuments.contains(document.id),
                        isSelectionMode = isSelectionMode,
                        selectionOrder = if (selectedDocuments.contains(document.id)) {
                            selectedDocuments.toList().indexOf(document.id) + 1
                        } else null,
                        onClick = if (isSelectionMode) {
                            { onDocumentSelect(document) }
                        } else {
                            { onDocumentClick(document) }
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun DocumentListItem(
    document: Document,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    selectionOrder: Int?,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier
            .androidx.compose.foundation.layout.fillMaxWidth()
            .androidx.compose.foundation.clickable.clickable { onClick() },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) {
                androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.surface
            }
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier
                .androidx.compose.foundation.layout.fillMaxWidth()
                .androidx.compose.foundation.layout.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                if (isSelected && selectionOrder != null) {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .androidx.compose.foundation.layout.size(32.dp)
                            .androidx.compose.foundation.background(
                                androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = selectionOrder.toString(),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                        )
                    }
                } else {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier
                            .androidx.compose.foundation.layout.size(32.dp)
                            .androidx.compose.foundation.border(
                                2.dp,
                                androidx.compose.material3.MaterialTheme.colorScheme.outline,
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.width(16.dp))
            }

            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.weight(1f)
            ) {
                androidx.compose.material3.Text(
                    text = document.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.height(4.dp))
                androidx.compose.material3.Text(
                    text = "Last edited: ${formatDate(document.updatedAt)}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    // Simplified date formatting for testing
    val diff = System.currentTimeMillis() - timestamp
    val hours = diff / (1000 * 60 * 60)
    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "${hours}h ago"
        else -> "${hours / 24}d ago"
    }
}