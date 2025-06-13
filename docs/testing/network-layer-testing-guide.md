# ネットワーク層テスト実装ガイド

## 概要

Task 5（Network Layer Setup）実装における実践的なテスト手法とベストプラクティスをまとめたガイドです。

## 実装されたテストクラス

### 1. NetworkModuleTest
**目的**: Hilt DIモジュールの正常性確認

```kotlin
class NetworkModuleTest {
    @Test
    fun `network module provides gson instance`()
    @Test  
    fun `network module provides okhttp client with interceptors`()
    @Test
    fun `network module provides retrofit instance`()
    @Test
    fun `network module provides openai api instance`()
    @Test
    fun `okhttp client has correct timeout configuration`()
    @Test
    fun `okhttp client has required interceptors`()
    @Test
    fun `network module is annotated for hilt`()
}
```

**テスト対象**: Retrofit、OkHttp、Gson、OpenAI APIの依存関係注入

### 2. NetworkExceptionTest
**目的**: カスタム例外階層の完全性確認

```kotlin
class NetworkExceptionTest {
    @Test
    fun `NetworkError creates exception with correct message`()
    @Test
    fun `ApiError creates exception with code and message`()
    @Test
    fun `UnauthorizedError has default message`()
    @Test
    fun `RateLimitError has default message`()
    @Test
    fun `FileTooLargeError has default message`()
    @Test
    fun `UnsupportedFormatError has default message`()
    @Test
    fun `TimeoutError has default message`()
    @Test
    fun `NoInternetError has default message`()
    @Test
    fun `UnknownError has default message`()
    @Test
    fun `all exceptions are instance of NetworkException`()
}
```

**テスト対象**: エラーハンドリングの網羅性とメッセージの正確性

### 3. NetworkErrorHandlerTest
**目的**: HTTP応答とエラー処理の正常性確認

```kotlin
class NetworkErrorHandlerTest {
    @Test
    fun `handleResponse returns success for successful response`()
    @Test
    fun `handleResponse returns failure for null body`()
    @Test
    fun `handleResponse returns unauthorized error for 401`()
    @Test
    fun `handleResponse returns rate limit error for 429`()
    @Test
    fun `handleResponse returns file too large error for 413`()
    @Test
    fun `handleResponse returns server error for 5xx`()
    @Test
    fun `handleException returns no internet error for UnknownHostException`()
    @Test
    fun `handleException returns timeout error for SocketTimeoutException`()
    @Test
    fun `handleException returns network error for IOException`()
    @Test
    fun `handleException preserves NetworkException`()
}
```

**テスト対象**: HTTPステータスコード別エラーマッピングの正確性

### 4. AuthInterceptorTest
**目的**: API認証ヘッダー追加の正常性確認

```kotlin
class AuthInterceptorTest {
    @Test
    fun `adds authorization header for openai requests`()
    @Test
    fun `does not add authorization header for non-openai requests`()
}
```

**テスト対象**: 条件付き認証ヘッダー追加ロジック

## テスト実装のベストプラクティス

### 1. モックライブラリの活用

```kotlin
// MockK使用例
private val mockContext = mockk<Context>(relaxed = true)
private val chain = mockk<Interceptor.Chain>()
```

**ポイント**:
- `relaxed = true`: 未定義メソッドに対してデフォルト値を返す
- Android固有のクラス（Context）のモック化

### 2. エラーケースの網羅的テスト

```kotlin
// HTTPステータスコード網羅例
@Test
fun `handleResponse returns unauthorized error for 401`() {
    every { mockResponse.code() } returns 401
    assertTrue(result.exceptionOrNull() is NetworkException.UnauthorizedError)
}
```

**ポイント**:
- 全てのHTTPエラーコードをテスト
- 例外の型と内容の両方を検証

### 3. 依存関係注入のテスト

```kotlin
@Test
fun `network module provides openai api instance`() {
    val api = NetworkModule.provideOpenAIApi(retrofit)
    assertNotNull(api)
    assertTrue(api is OpenAIApi)
}
```

**ポイント**:
- DIコンテナの正常動作確認
- インスタンス生成とタイプ安全性の検証

## 実行コマンド

### ネットワーク関連テストのみ実行
```bash
# パターンマッチングでネットワークテストを特定
./gradlew testDebugUnitTest --tests "*Network*"
./gradlew testDebugUnitTest --tests "*remote*"

# 特定パッケージのテスト実行
./gradlew testDebugUnitTest --tests "com.example.talktobook.data.remote.*"
```

### 個別テストクラス実行
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.di.NetworkModuleTest"
./gradlew testDebugUnitTest --tests "com.example.talktobook.data.remote.exception.NetworkExceptionTest"
```

## 発見された問題と解決方法

### 1. 依存関係の追加忘れ

**問題**: MockKライブラリの依存関係追加を忘れる

**解決方法**:
```toml
# gradle/libs.versions.toml
mockk = "1.13.5"

# app/build.gradle.kts  
testImplementation(libs.mockk)
```

### 2. Android権限の不足

**問題**: `ACCESS_NETWORK_STATE`権限不足によるLintエラー

**解決方法**:
```xml
<!-- app/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**発見プロセス**: Lintチェック → エラー特定 → 権限追加 → 再テスト

### 3. テスト実行効率の最適化

**問題**: フルビルドの実行時間が長い（2分+）

**解決方法**:
```bash
# 高速フィードバック重視の順序
./gradlew testDebugUnitTest    # 30秒
./gradlew lintDebug           # 10秒
# フルビルドは必要時のみ
```

## 品質保証チェックリスト

### コミット前確認事項
- [ ] 単体テスト実行: `./gradlew testDebugUnitTest`
- [ ] Lintチェック: `./gradlew lintDebug`
- [ ] 権限確認: `grep -i permission AndroidManifest.xml`
- [ ] 依存関係確認: `git diff gradle/libs.versions.toml`

### PR作成前確認事項
- [ ] 新機能のテストカバレッジ100%
- [ ] 既存テストの回帰確認
- [ ] パフォーマンス影響の測定
- [ ] エラーハンドリングの網羅性確認

## 継続的改善のポイント

### 1. TDD（テスト駆動開発）の実践

1. **Red**: テストを先に書いて失敗させる
2. **Green**: 最小限の実装でテストを通す
3. **Refactor**: コードを改善する

### 2. テスト設計の原則

- **単一責任**: 1つのテストは1つの機能のみ検証
- **独立性**: テスト間の依存関係を排除
- **再現性**: 実行順序に関係なく同じ結果
- **可読性**: テスト名でテスト内容が理解できる

### 3. エラーハンドリングの完全性

- 全てのHTTPステータスコードをカバー
- ネットワーク例外の適切な変換
- ユーザーフレンドリーなエラーメッセージ

## 参考資料

- [MockK Documentation](https://mockk.io/)
- [Android Testing Fundamentals](https://developer.android.com/training/testing/fundamentals)
- [Retrofit Testing](https://square.github.io/retrofit/)
- [OkHttp Interceptors](https://square.github.io/okhttp/interceptors/)

## まとめ

ネットワーク層のテスト実装を通じて以下の価値が実証されました：

1. **品質保証**: 包括的なテストによる堅牢性確保
2. **開発効率**: TDDによる早期バグ発見
3. **保守性**: 明確なテスト構造による理解しやすさ
4. **拡張性**: 新機能追加時の回帰防止

これらの知見は今後のプロジェクト開発における品質向上と効率化に直接貢献します。