# TalkToBook テスト構造

## テストディレクトリ構造

```
app/src/
├── test/                              # 単体テスト
│   └── java/com/example/talktobook/
│       ├── ExampleUnitTest.kt
│       └── domain/model/
│           ├── ChapterTest.kt         # Chapterモデルテスト
│           ├── DocumentTest.kt        # Documentモデルテスト  
│           ├── RecordingTest.kt       # Recordingモデルテスト
│           ├── RecordingStateTest.kt  # RecordingState enumテスト
│           └── TranscriptionStatusTest.kt # TranscriptionStatus enumテスト
└── androidTest/                       # インストルメンテッドテスト
    └── java/com/example/talktobook/
        └── ExampleInstrumentedTest.kt
```

## 現在実装済みテスト

### 1. ドメインモデルテスト

#### ChapterTest.kt
- **テスト対象**: `Chapter` data class
- **テスト項目**:
  - ✅ パラメータ完全指定での作成
  - ✅ データクラスの等価性
  - ✅ copy関数の動作
  - ✅ 順序インデックスの検証

#### DocumentTest.kt
- **テスト対象**: `Document` data class
- **テスト項目**:
  - ✅ パラメータ完全指定での作成
  - ✅ 空のチャプターリストでの作成
  - ✅ データクラスの等価性
  - ✅ copy関数の動作

#### RecordingTest.kt
- **テスト対象**: `Recording` data class
- **テスト項目**:
  - ✅ null transcribedTextでの作成
  - ✅ copy関数の動作
  - ✅ データクラスの等価性
  - ✅ その他のパラメータテスト

#### RecordingStateTest.kt
- **テスト対象**: `RecordingState` enum
- **テスト項目**:
  - ✅ enum値の存在確認
  - ✅ 文字列表現のテスト
  - ✅ valueOf関数のテスト

#### TranscriptionStatusTest.kt
- **テスト対象**: `TranscriptionStatus` enum
- **テスト項目**:
  - ✅ enum値の存在確認
  - ✅ 状態遷移の妥当性

## 今後実装すべきテスト

### 2. リポジトリ層テスト（未実装）

```
app/src/test/java/com/example/talktobook/
└── data/repository/
    ├── AudioRepositoryImplTest.kt
    ├── TranscriptionRepositoryImplTest.kt
    └── DocumentRepositoryImplTest.kt
```

**テスト項目**:
- データソースとの連携
- エラーハンドリング
- キャッシュ機能
- データ変換処理

### 3. ユースケース層テスト（未実装）

```
app/src/test/java/com/example/talktobook/
└── domain/usecase/
    ├── recording/
    │   ├── StartRecordingUseCaseTest.kt
    │   ├── StopRecordingUseCaseTest.kt
    │   └── GetRecordingsUseCaseTest.kt
    ├── transcription/
    │   ├── TranscribeAudioUseCaseTest.kt
    │   └── GetTranscriptionStatusUseCaseTest.kt
    └── document/
        ├── CreateDocumentUseCaseTest.kt
        ├── UpdateDocumentUseCaseTest.kt
        └── GetDocumentsUseCaseTest.kt
```

### 4. プレゼンテーション層テスト（未実装）

```
app/src/test/java/com/example/talktobook/
└── presentation/
    └── viewmodel/
        ├── MainViewModelTest.kt
        ├── RecordingViewModelTest.kt
        └── DocumentViewModelTest.kt
```

### 5. UIテスト（未実装）

```
app/src/androidTest/java/com/example/talktobook/
└── presentation/ui/
    ├── screen/
    │   ├── MainScreenTest.kt
    │   ├── RecordingScreenTest.kt
    │   └── DocumentScreenTest.kt
    └── component/
        ├── RecordingButtonTest.kt
        └── DocumentListTest.kt
```

## テストの品質基準

### TDD原則の適用
- **Red-Green-Refactor** サイクルの実践
- テストファーストアプローチ
- 最小限の実装でテストを通す

### カバレッジ目標
- **全体**: 80%以上
- **ドメイン層**: 90%以上（現在: 100%）
- **ユースケース層**: 85%以上
- **プレゼンテーション層**: 70%以上

### テストパターン

#### 1. データクラステスト
```kotlin
@Test
fun `create entity with all parameters`() {
    // Given
    val entity = Entity(...)
    
    // Then
    assertEquals(expected, entity.property)
}

@Test
fun `entity data class equality`() {
    // Given
    val entity1 = Entity(...)
    val entity2 = Entity(...)
    
    // Then
    assertEquals(entity1, entity2)
    assertEquals(entity1.hashCode(), entity2.hashCode())
}
```

#### 2. ビジネスロジックテスト
```kotlin
@Test
fun `usecase returns success when valid input`() {
    // Given
    val input = ValidInput(...)
    val mockRepository = mockk<Repository>()
    every { mockRepository.getData() } returns Success(...)
    
    // When
    val result = usecase.execute(input)
    
    // Then
    assertTrue(result.isSuccess)
}
```

#### 3. エラーハンドリングテスト
```kotlin
@Test
fun `usecase returns error when repository fails`() {
    // Given
    val mockRepository = mockk<Repository>()
    every { mockRepository.getData() } returns Error(...)
    
    // When
    val result = usecase.execute(input)
    
    // Then
    assertTrue(result.isError)
}
```

## テスト実行結果の分析

### 成功指標
- ✅ テスト成功率: 100% (17/17)
- ✅ ビルド成功
- ✅ Lint警告のみ（エラーなし）

### 品質メトリクス
- **実行時間**: 平均2分以内
- **安定性**: 再現可能な結果
- **保守性**: 明確なテスト名と構造

## ベストプラクティス

### 1. テスト命名規約
```kotlin
// Good
fun `create Document with empty chapters list`()
fun `usecase returns error when repository fails`()

// Bad  
fun testDocumentCreation()
fun test1()
```

### 2. テスト構造
```kotlin
@Test
fun `test description`() {
    // Given (テスト準備)
    
    // When (テスト実行)
    
    // Then (結果検証)
}
```

### 3. アサーション
- 具体的で意味のあるアサーション
- 複数の状態を個別に検証
- エラーメッセージを明確に