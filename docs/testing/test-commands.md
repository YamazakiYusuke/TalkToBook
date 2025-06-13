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