# WSL Android開発環境構築とトラブルシューティング実録

このドキュメントは、WSL Ubuntu環境でAndroid開発環境を構築し、テストを実行するまでの実際の試行錯誤プロセスを記録したものです。

## 背景と初期問題

### 発生した問題
当初、WindowsにインストールされたAndroid SDK（Build Tools 35.0.0）を使用してビルドを実行しようとしたところ、以下のエラーが発生：

```
FAILURE: Build failed with an exception.
* What went wrong:
Could not determine the dependencies of task ':app:compileDebugJavaWithJavac'.
> Installed Build Tools revision 35.0.0 is corrupted. Remove and install again using the SDK Manager.
Build-tool 35.0.0 is missing AAPT at /mnt/c/Users/yusuk/AppData/Local/Android/Sdk/build-tools/35.0.0/aapt
```

### 試行した解決策（失敗例）

#### 1. Build Toolsバージョンの変更
```bash
# AGP 8.8.0では最低35.0.0が必要なため失敗
buildToolsVersion = "34.0.0"  # ← 無視される
```

#### 2. AGPバージョンのダウングレード
```kotlin
// libs.versions.toml
agp = "8.7.1"  # 8.8.0から変更
```

#### 3. compileSdkの変更
```kotlin
compileSdk = 34  # 35から変更
targetSdk = 34
```

これらの対症療法的アプローチは根本的な解決にならず、**WSL内にネイティブAndroid SDK環境を構築する**という抜本的解決策を採用。

## WSL内Android SDK構築プロセス

### Phase 1: システム準備

#### 依存関係のインストール
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y wget unzip curl lib32z1 libc6-dev-i386
```

#### Java環境の整備
**最初の試行（Java 11）:**
```bash
sudo apt install -y openjdk-11-jdk-headless
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

**エラー発生:**
```
Android Gradle plugin requires Java 17 to run. You are currently using Java 11.
```

**解決策（Java 17への移行）:**
```bash
sudo apt install -y openjdk-17-jdk-headless
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### Phase 2: Android SDK構築

#### Command Line Toolsのダウンロード
```bash
cd ~
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmd-tools.zip
```

#### ディレクトリ構造の構築
```bash
mkdir -p ~/android/cmdline-tools
unzip -q /tmp/cmd-tools.zip -d ~/android/cmdline-tools
mv ~/android/cmdline-tools/cmdline-tools ~/android/cmdline-tools/latest
rm /tmp/cmd-tools.zip
```

#### 環境変数の設定
```bash
# ~/.bashrcに追加
export ANDROID_HOME=$HOME/android
export ANDROID_SDK_ROOT=$ANDROID_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/build-tools
```

### Phase 3: SDK コンポーネントのインストール

#### ライセンス同意
```bash
yes | sdkmanager --licenses
```

#### 必要コンポーネントのインストール
```bash
sdkmanager "platform-tools"
sdkmanager "platforms;android-34"  # 最初のインストール
sdkmanager "build-tools;34.0.0"
```

### Phase 4: プロジェクト設定の調整

#### local.propertiesの更新
```properties
sdk.dir=/home/yusuke/android
```

#### 依存関係エラーとの遭遇
最初のビルド試行で発生したエラー：
```
Dependency 'androidx.core:core:1.16.0' requires libraries and applications that
depend on it to compile against version 35 or later of the Android APIs.
:app is currently compiled against android-34.
```

#### 解決策の実装
```bash
# Android SDK Platform 35の追加インストール
sdkmanager "platforms;android-35" "build-tools;35.0.0"
```

```kotlin
// build.gradle.ktsの更新
android {
    compileSdk = 35
    defaultConfig {
        targetSdk = 35
    }
}
```

## テスト実行における試行錯誤

### Phase 1: コンパイルエラーの修正

#### 初回テスト実行エラー
```
e: file:///mnt/c/Users/yusuk/workspace/android/TalkToBook/app/src/test/java/com/example/talktobook/domain/model/ChapterTest.kt:15:13 No value passed for parameter 'createdAt'.
e: file:///mnt/c/Users/yusuk/workspace/android/TalkToBook/app/src/test/java/com/example/talktobook/domain/model/ChapterTest.kt:15:13 No value passed for parameter 'updatedAt'.
```

#### 原因分析
Chapterモデルに`createdAt`と`updatedAt`パラメータが追加されていたが、テストコードが古い形式のままだった。

#### 修正作業
```kotlin
// 修正前
val chapter = Chapter(
    id = "chapter-1",
    documentId = "doc-1",
    orderIndex = 0,
    title = "Chapter Title",
    content = "Chapter content"
)

// 修正後
val chapter = Chapter(
    id = "chapter-1",
    documentId = "doc-1",
    orderIndex = 0,
    title = "Chapter Title",
    content = "Chapter content",
    createdAt = 1234567890L,
    updatedAt = 1234567900L
)
```

### Phase 2: Lintエラーの解決

#### Lint実行エラー
```
Unexpected failure during lint analysis (this is a bug in lint or one of the libraries it depends on)
The crash seems to involve the detector `androidx.lifecycle.lint.NonNullableMutableLiveDataDetector`.
```

#### 解決策の実装
```kotlin
// build.gradle.ktsに追加
android {
    lint {
        disable += "NullSafeMutableLiveData"
    }
}
```

## 学んだ教訓とベストプラクティス

### 1. 環境構築の原則

#### ❌ 避けるべきアプローチ
- Windows/WSL間でのSDK共有
- 対症療法的なバージョンダウングレード
- 複数のJavaバージョンの混在

#### ✅ 推奨アプローチ
- プラットフォーム固有のネイティブ環境構築
- 一貫したツールチェーンの使用
- 明示的な環境変数設定

### 2. トラブルシューティングの手法

#### 段階的アプローチ
1. **エラーメッセージの詳細分析**
2. **根本原因の特定**
3. **最小限の変更による検証**
4. **包括的な解決策の実装**

#### 検証の重要性
```bash
# 各段階での動作確認
java -version
adb --version
sdkmanager --version
sdkmanager --list_installed
```

### 3. 設定ファイルの管理

#### 環境固有設定の分離
```properties
# local.properties (gitignore対象)
sdk.dir=/home/yusuke/android
```

#### バージョン整合性の維持
```kotlin
// 依存関係の要求に合わせた設定
compileSdk = 35  # androidx.core:core:1.16.0の要求
targetSdk = 35
```

## 予防策とメンテナンス

### 定期的な確認事項

#### 環境の健全性チェック
```bash
# 毎回の開発開始時に実行
export ANDROID_HOME=$HOME/android
export ANDROID_SDK_ROOT=$ANDROID_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# 動作確認
./gradlew --version
```

#### 依存関係の更新確認
```bash
# SDKコンポーネントの更新チェック
sdkmanager --list | grep -E "(Available|Updates)"
```

### ドキュメント化の重要性

このトラブルシューティングプロセスを通じて、以下の価値が確認された：

1. **試行錯誤の記録**: 失敗例も含めて記録することで、同様の問題の早期発見が可能
2. **段階的解決**: 問題を小さく分割し、段階的に解決するアプローチの有効性
3. **環境の一貫性**: クロスプラットフォーム開発における環境の統一の重要性

## 今後の改善点

### 自動化の検討
```bash
#!/bin/bash
# setup-android-wsl.sh
set -e

echo "Setting up Android development environment in WSL..."

# 環境変数設定
export ANDROID_HOME=$HOME/android
export ANDROID_SDK_ROOT=$ANDROID_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# 依存関係確認
java -version || { echo "Java 17 not found"; exit 1; }

# SDK確認
sdkmanager --version || { echo "SDK Manager not found"; exit 1; }

echo "Environment setup completed successfully"
```

### 継続的監視
- SDK コンポーネントの定期更新
- 新しいAGPバージョンとの互換性確認
- WSLの更新に伴う影響調査

この実録が、同様の環境構築を行う開発者の参考となることを期待する。