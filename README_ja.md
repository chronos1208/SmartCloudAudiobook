# 📚 SmartCloud Audiobook

SmartCloud Audiobook は、**Google ドライブ上の音声ファイルを直接ストリーミング再生**でき、さらに**PDF ビューア**も搭載した Android 向け高機能オーディオブックプレーヤーです。

> ひとことで言うと、クラウド連携・連続再生・メタデータ自動取得を備えた実用的なオーディオブック体験を提供するアプリです。

---

## 🚀 プロジェクト概要 (Project Overview)

**SmartCloud Audiobook** は、Google Drive に保存したオーディオブックを快適に聴くために設計されています。

- ☁️ ローカルへ全ファイルを事前コピーせずにクラウドから再生
- 📄 音声に関連する PDF 資料をアプリ内で閲覧
- 🎧 Media3/ExoPlayer ベースのモダンな再生体験

日常利用を意識し、認証・再生・ライブラリ表示の品質を重視しています。

---

## ✨ 主な機能 (Features)

### 1) Google Drive 連携（オンデマンド同期・ストリーミング）
- Google アカウントと安全に連携
- Drive 上のコンテンツをスキャン/同期
- 必要時にストリーミング再生

### 2) Media3 (ExoPlayer) による高度な再生
- 複数ファイルをシームレスにつなぐ連続再生
- Media Session / Service によるバックグラウンド再生
- 長時間コンテンツ向けの操作性

### 3) iTunes API を使ったメタデータ拡張
- カバー画像やメタデータの自動取得
- 視認性の高いライブラリ表示
- コンテンツ識別性の向上

---

## 🧰 技術スタック (Tech Stack)

このプロジェクトでは主に以下を利用しています。

- **言語**: Kotlin
- **UI**: Jetpack Compose
- **デザイン**: Material Design 3
- **メディア再生**: Media3 (ExoPlayer)
- **ローカル DB**: Room
- **DI**: Hilt
- **認証**: Credential Manager + Google Identity
- **通信**: Retrofit（+ Moshi）
- **画像表示**: Coil
- **画面遷移**: Navigation Compose

---

## ⚠️ 重要な事前準備 (Important Setup / Prerequisites)【最重要】

アプリをビルド/実行する前に、以下の手順を**必ず**実施してください。
未設定の場合、Google 認証および Drive 連携は動作しません。

### ✅ 手順 1: Google Cloud Console でプロジェクト作成 & Drive API 有効化
1. Google Cloud Console を開く
2. 本アプリ用のプロジェクトを作成（または既存を選択）
3. そのプロジェクトで **Google Drive API** を有効化

### ✅ 手順 2: OAuth 2.0 クライアント ID を **Web アプリケーション** で作成
1. OAuth 認証情報の作成画面へ移動
2. OAuth 2.0 クライアント ID を新規作成
3. 種別は **Web アプリケーション** を選択

> ❗ 注意: この設定では **Android タイプのクライアント ID は使用しません**。

### ✅ 手順 3: 取得した Client ID を `strings.xml` に設定
以下ファイルへ貼り付けます。

`app/src/main/res/values/strings.xml`

```xml
<string name="google_web_client_id">YOUR_CLIENT_ID</string>
```

---

## 🛠 ビルドと実行方法 (Getting Started / How to Run)

### 必要環境
- 最新の **Android Studio**（安定版推奨）
- **JDK 17**
- 本プロジェクトに対応した Android SDK（compile/target SDK 34）

### 1) リポジトリをクローン
```bash
git clone <your-repo-url>
cd SmartCloudAudiobook
```

### 2) Google OAuth Client ID を設定
- 上記「重要な事前準備」を完了
- `app/src/main/res/values/strings.xml` の `google_web_client_id` を更新

### 3) Android Studio で開く
- プロジェクトを開き、Gradle Sync 完了を待つ

### 4) ビルド
Android Studio から実行可能。CLI の場合:

```bash
./gradlew assembleDebug
```

### 5) 実行

#### A. エミュレーターで実行（認証テストに推奨）
**Google Play アイコン付き（Play Store enabled）** のシステムイメージを使用してください。

理由:
- Google サインイン関連機能がより安定して検証できます。

手順例:
1. Android Studio → Device Manager
2. Create Virtual Device
3. **Google Play** ラベルのイメージを選択
4. エミュレーター起動後、`app` 構成で実行

#### B. 実機で実行
1. 端末で **開発者向けオプション** を有効化
   - 設定 → 端末情報 → ビルド番号を連続タップ
2. **USB デバッグ** を有効化
   - 設定 → 開発者向けオプション → USB デバッグ
3. USB で PC と接続
4. 端末側のデバッグ許可ダイアログ（RSA 指紋）を許可
5. Android Studio で端末を選択し Run

---

## 📦 便利な Gradle コマンド

```bash
./gradlew assembleDebug        # Debug APK をビルド
./gradlew installDebug         # 接続中デバイスへインストール
./gradlew test                 # ユニットテスト
./gradlew connectedAndroidTest # 端末/エミュレーターで計測テスト
```

---

## 🧪 トラブルシューティング

- **サインインが失敗する**
  - OAuth クライアント種別が **Web アプリケーション** か確認
  - `google_web_client_id` の値を再確認

- **エミュレーターで Google 認証が不安定**
  - **Google Play 対応イメージ** を使用しているか確認

- **Drive のデータが取得できない**
  - 対象 Google Cloud プロジェクトで Drive API が有効か確認

- **Gradle/JDK 関連エラー**
  - Android Studio の JDK が **17** になっているか確認

---

## 🤝 コントリビューション

改善提案・PR は歓迎です。大きめの変更の場合は、先に Issue で以下を共有してください。

- 課題の背景
- 提案内容
- UX / 技術面への影響

---

## 📄 ライセンス

必要に応じてライセンス情報（例: MIT / Apache-2.0）を追記してください。


## 🆕 Phase 6 実装メモ

- ExoPlayer は Google Drive `files/{fileId}?alt=media` へ Bearer トークン付きでアクセスし、ストリーミング再生します。
- Player 画面は Room の `AudioTrackEntity` から対象オーディオブックのトラック一覧を読み込み、プレイリスト再生します。
- アプリ内 PDF ビューアを追加し、Drive の PDF をキャッシュへ保存して `PdfRenderer` + Compose `LazyColumn` で表示します。
- 画面遷移は `Player(audiobookId)` / `PdfViewer(pdfFileId)` のルートパラメータに対応しました。
