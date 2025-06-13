# TalkToBook テストコマンド クイックリファレンス

## 基本的なテストコマンド

### すべてのテストを実行
```bash
./gradlew test
```

### 単体テストのみ実行
```bash
./gradlew testDebugUnitTest
./gradlew testReleaseUnitTest
```

### Lintチェック実行
```bash
./gradlew lint
```

### ビルドとテストを一括実行
```bash
./gradlew clean build test lint
```

## 特定のテストを実行

### クラス単位で実行
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.domain.model.ChapterTest"
./gradlew testReleaseUnitTest --tests "com.example.talktobook.domain.model.ChapterTest"
```

### メソッド単位で実行
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.domain.model.ChapterTest.create Chapter with all parameters"
```

### パッケージ単位で実行
```bash
./gradlew testDebugUnitTest --tests "com.example.talktobook.domain.model.*"
```

## テスト結果の確認

### HTMLレポートを開く（WSL環境）
```bash
# Firefoxがインストールされている場合
firefox app/build/reports/tests/testDebugUnitTest/index.html

# Windows側のブラウザで開く場合
explorer.exe app/build/reports/tests/testDebugUnitTest/index.html
```

### テスト結果をコンソールで確認
```bash
# XMLファイルから結果を抽出
cat app/build/test-results/testDebugUnitTest/*.xml | grep -E "(testcase|failure|error)"

# Lint結果を確認
cat app/build/reports/lint-results-debug.txt
```

## デバッグオプション付き実行

### 詳細なログ出力
```bash
./gradlew test --info
./gradlew test --debug
```

### スタックトレース表示
```bash
./gradlew test --stacktrace
```

### 並列実行を無効化（デバッグ時に便利）
```bash
./gradlew test -Dorg.gradle.parallel=false
```

## 環境変数の設定（WSL環境）

### 一時的に環境変数を設定して実行
```bash
export ANDROID_HOME=$HOME/android && \
export ANDROID_SDK_ROOT=$ANDROID_HOME && \
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && \
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH && \
./gradlew test
```

### エイリアスの設定（.bashrcに追加）
```bash
alias gradletest='export ANDROID_HOME=$HOME/android && export ANDROID_SDK_ROOT=$ANDROID_HOME && export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 && export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH && ./gradlew test'
```

## トラブルシューティング

### Gradleキャッシュをクリア
```bash
./gradlew clean
rm -rf ~/.gradle/caches/
```

### Gradle Daemonを再起動
```bash
./gradlew --stop
./gradlew test
```

### 強制的に再実行
```bash
./gradlew test --rerun-tasks
```

## 最適化されたテスト実行手順（2025年6月13日更新）

### 開発時の効率的なテスト実行順序
```bash
# 1. 高速フィードバック重視の実行順序
./gradlew testDebugUnitTest    # 約30秒 - 単体テスト先行
./gradlew lintDebug           # 約10秒 - Lint即座実行

# 2. 必要時のみ実行（時間がかかる処理）
./gradlew assembleDebug       # 2分+ - フルビルド
./gradlew test               # 全テスト（Debug+Release）
```

### ネットワーク関連テストの実行
```bash
# ネットワーク層の単体テスト
./gradlew testDebugUnitTest --tests "*Network*"
./gradlew testDebugUnitTest --tests "*remote*"

# 特定の新機能テスト（例：Task 5関連）
./gradlew testDebugUnitTest --tests "com.example.talktobook.data.remote.*"
./gradlew testDebugUnitTest --tests "com.example.talktobook.di.NetworkModuleTest"
```

### 依存関係追加後の検証コマンド
```bash
# 新しい依存関係（例：mockk）追加後の確認
./gradlew dependencies --configuration testImplementation | grep mockk

# コンパイル確認
./gradlew compileDebugKotlin
./gradlew compileDebugUnitTestKotlin
```

## 継続的インテグレーション用

### JUnit XMLレポート生成
```bash
./gradlew test
# レポートは app/build/test-results/testDebugUnitTest/ に生成される
```

### テスト失敗時も続行
```bash
./gradlew test --continue
```

### 権限関連の検証
```bash
# AndroidManifest.xmlの権限確認
grep -i "permission" app/src/main/AndroidManifest.xml

# Lint権限エラーの特定
./gradlew lintDebug 2>&1 | grep -i "permission\|MissingPermission"
```