# TalkToBook テスト実施ガイド

このドキュメントでは、TalkToBookプロジェクトのテスト実施方法について説明します。

## 環境設定

### 必要な環境変数
```bash
export ANDROID_HOME=$HOME/android
export ANDROID_SDK_ROOT=$ANDROID_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
```

## テストの種類と実行方法

### 1. 単体テスト (Unit Tests)

**実行コマンド：**
```bash
./gradlew test
```

**個別実行：**
```bash
# デバッグビルドのみ
./gradlew testDebugUnitTest

# リリースビルドのみ
./gradlew testReleaseUnitTest

# 特定のテストクラスのみ実行
./gradlew test --tests "com.example.talktobook.domain.model.ChapterTest"

# 特定のテストメソッドのみ実行
./gradlew test --tests "com.example.talktobook.domain.model.ChapterTest.create Chapter with all parameters"
```

**テスト結果の確認：**
- HTMLレポート: `app/build/reports/tests/testDebugUnitTest/index.html`
- XMLレポート: `app/build/test-results/testDebugUnitTest/*.xml`

### 2. インストルメンテッドテスト (Instrumented Tests)

**注意：** WSL環境では実機/エミュレータへの接続が制限されるため、Windows環境での実行を推奨

**実行コマンド：**
```bash
# エミュレータまたは実機を接続した状態で実行
./gradlew connectedAndroidTest
```

**代替方法（Windows環境）：**
1. Android Studioを使用
2. エミュレータを起動
3. テストを右クリックして「Run」を選択

### 3. Lintチェック

**実行コマンド：**
```bash
./gradlew lint
```

**個別実行：**
```bash
# デバッグビルドのみ
./gradlew lintDebug

# リリースビルドのみ
./gradlew lintRelease

# 特定のバリアントのみ
./gradlew lintDebug
```

**結果の確認：**
- HTMLレポート: `app/build/reports/lint-results-debug.html`
- TXTレポート: `app/build/reports/lint-results-debug.txt`
- XMLレポート: `app/build/reports/lint-results-debug.xml`

### 4. テストカバレッジ

**実行コマンド：**
```bash
# JaCoCoプラグインが必要（現在未設定）
./gradlew testDebugUnitTestCoverage
```

## テスト実行のベストプラクティス

### 開発時の推奨フロー

1. **コード変更前：**
   ```bash
   ./gradlew test
   ```

2. **新機能開発時（TDD）：**
   - テストを先に書く
   - テストが失敗することを確認
   - 実装を行う
   - テストが成功することを確認

3. **コミット前：**
   ```bash
   ./gradlew test lint
   ```

4. **プルリクエスト前：**
   ```bash
   ./gradlew clean build test lint
   ```

### CI/CD環境での実行

```bash
# フルビルドとテスト
./gradlew clean build test lint

# テスト結果をアーティファクトとして保存
cp -r app/build/reports/* ./test-reports/
cp -r app/build/test-results/* ./test-results/
```

## トラブルシューティング

### よくある問題と解決方法

1. **Gradle Daemonのメモリ不足**
   ```bash
   ./gradlew --stop
   ./gradlew test -Dorg.gradle.jvmargs="-Xmx2g"
   ```

2. **テストが見つからない**
   ```bash
   ./gradlew clean test --rerun-tasks
   ```

3. **Lintエラー「NullSafeMutableLiveData」**
   - `build.gradle.kts`に以下を追加済み：
   ```kotlin
   lint {
       disable += "NullSafeMutableLiveData"
   }
   ```

4. **WSLでのADBデバイス認識問題**
   ```bash
   # Windows側でADBをTCPモードに設定
   adb tcpip 5555
   
   # WSL側から接続
   adb connect [デバイスIP]:5555
   ```

## 現在のテスト構成

### 単体テスト
- `ExampleUnitTest`: サンプルテスト
- `ChapterTest`: Chapterモデルのテスト
- `DocumentTest`: Documentモデルのテスト
- `RecordingTest`: Recordingモデルのテスト
- `RecordingStateTest`: RecordingState enumのテスト
- `TranscriptionStatusTest`: TranscriptionStatus enumのテスト

### インストルメンテッドテスト
- `ExampleInstrumentedTest`: サンプルテスト（要実装）

## 継続的改善

### 今後追加すべきテスト

1. **リポジトリ層のテスト**
   - MockやFakeを使用したリポジトリテスト
   - データソースのテスト

2. **ViewModelのテスト**
   - 状態管理のテスト
   - ユーザーインタラクションのテスト

3. **統合テスト**
   - API通信のテスト
   - データベース操作のテスト

4. **UIテスト**
   - Compose UIのテスト
   - ナビゲーションのテスト

### テストカバレッジ目標
- 現在: 未測定
- 目標: 80%以上（仕様書の要件）

## トラブルシューティング実績

### WSL環境でのAndroid SDK問題
当初、Windows SDK（Build Tools 35.0.0）の破損エラーが発生し、以下の試行錯誤を経て解決：

1. **失敗したアプローチ**:
   - Build Toolsバージョンのダウングレード
   - AGP 8.8.0 → 8.7.1への変更
   - compileSdk 35 → 34への変更

2. **成功したアプローチ**:
   - WSL内にAndroid SDKをネイティブインストール
   - Java 11 → Java 17への更新
   - 環境変数の適切な設定

### テストコードの修正
Chapterモデルに`createdAt`, `updatedAt`パラメータが追加されたことによるコンパイルエラーを修正：

```kotlin
// 修正前
Chapter("1", "doc-1", 0, "Chapter 1", "Content 1")

// 修正後  
Chapter("1", "doc-1", 0, "Chapter 1", "Content 1", 1234567890L, 1234567900L)
```

### Lintエラーの解決
`NonNullableMutableLiveDataDetector`の問題を以下で解決：

```kotlin
android {
    lint {
        disable += "NullSafeMutableLiveData"
    }
}
```

詳細な実録は `docs/testing/wsl-android-setup-troubleshooting.md` を参照。

## 参考リンク

- [Android Testing Documentation](https://developer.android.com/training/testing)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Mockito Documentation](https://site.mockito.org/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [WSL Android Setup Troubleshooting](docs/testing/wsl-android-setup-troubleshooting.md)