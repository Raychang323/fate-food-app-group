# FatefulSupper - 宵夜探險家

一款貼心的安卓應用程式，專門幫助使用者發現和享受宵夜飲食體驗。應用程式透過互動式輪盤功能和個性化推薦，為使用者找到最適合的晚餐選擇。

## 功能特色

🍽️ **智慧美食探索**
- **輪盤模式**：轉動輪盤，獲得隨機宵夜食物推薦
- **美食家模式**：瀏覽精選餐廳列表，查看詳細資訊
- **懶人模式**：快速推薦，適合無法決定的朋友
- **幸運美食**：驚喜美食推薦
- **幸運餐點**：完整的套餐組合推薦

🗺️ **位置服務**
- 在互動式地圖上查看附近餐廳
- 獲取前往餐廳的路線指引
- 基於 GPS 的位置發現功能

👥 **個性化設定**
- 使用者認證（註冊與登入）
- 自訂食物類型偏好
- 黑名單功能，排除不想吃的菜系
- 電子郵件驗證，增強帳戶安全

⚙️ **設定與通知**
- 通知偏好設定
- 每日宵夜餐點推薦
- 可自訂的通知設定

## 技術棧

- **語言**：Kotlin
- **Android SDK**：API 26（Android 8.0）至 API 36（Android 15）
- **架構**：MVVM（Model-View-ViewModel）
- **資料庫**：Room（本地持久化儲存）
- **導航**：Android Navigation Component with Safe Args
- **位置服務**：Google Maps API
- **構建系統**：Gradle（Kotlin DSL）

## 專案結構

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/fatefulsupper/
│   │   │   └── [應用程式原始碼]
│   │   ├── res/
│   │   │   ├── drawable/     [UI 資源]
│   │   │   ├── layout/       [Activity 和 Fragment 版面配置]
│   │   │   ├── menu/         [菜單資源]
│   │   │   ├── values/       [字串、顏色、尺寸]
│   │   │   └── navigation/   [導航圖表]
│   │   └── AndroidManifest.xml
│   ├── androidTest/          [儀器化測試]
│   └── test/                 [單位測試]
├── build.gradle.kts          [應用程式級別構建設定]
└── proguard-rules.pro        [代碼混淆規則]
```

## 主要 Activity 與 Fragment

- **MainActivity**：主應用程式中樞，含導航抽屜
- **LoginActivity**：使用者認證入口
- **RegisterActivity**：新使用者註冊
- **HomeFragment**：主儀表板
- **RouletteFragment**：食物輪盤轉盤
- **FoodieModeFragment**：美食家模式介面
- **LazyModeFragment**：快速推薦
- **RestaurantListFragment**：瀏覽餐廳
- **RestaurantDetailsFragment**：餐廳詳細資訊
- **MapDirectionFragment**：互動式地圖與路線指引
- **SettingsFragment**：使用者偏好設定

## 所需權限

- `POST_NOTIFICATIONS`：發送通知提醒
- `INTERNET`：網路存取
- `ACCESS_FINE_LOCATION`：GPS 位置服務
- `ACCESS_COARSE_LOCATION`：近似位置
- `RECEIVE_BOOT_COMPLETED`：開機完成處理
- `SCHEDULE_EXACT_ALARM`：排定通知提醒

## 快速開始

### 系統需求
- Android Studio（最新版本）
- JDK 11 或更新版本
- Android SDK 26+（API 級別）
- Git

### 安裝步驟

1. **複製儲存庫**
   ```bash
   git clone https://github.com/Raychang323/fate-food-app-group.git
   cd FatefulSupper-Android-App
   ```

2. **在 Android Studio 中開啟**
   - 檔案 → 開啟 → 選擇專案目錄
   - 讓 Gradle 自動同步和構建

3. **設定 Google Maps API**
   - 應用程式使用 Google Maps 提供位置功能
   - API 金鑰在 `AndroidManifest.xml` 中設定
   - 確保在 Google Cloud 控制台中有適當的權限

4. **執行應用程式**
   - 連接 Android 裝置（API 26+）或使用模擬器
   - 點擊「執行」或按下 `Shift + F10`

## 構建與執行

### 構建應用程式
```bash
./gradlew build
```

### 執行應用程式
```bash
./gradlew installDebug
```

### 構建發行版本
```bash
./gradlew assembleRelease
```

## 應用程式規格

- **最低 SDK**：26（Android 8.0）
- **目標 SDK**：36（Android 15）
- **版本代碼**：1
- **版本名稱**：1.0
- **套件名稱**：`com.fatefulsupper.app`

## 已知問題與注意事項

- 已啟用向量繪圖支援庫以確保相容性
- 某些依賴項有衝突的檔案（透過封裝排除項進行篩選）
- 本地開發已啟用明文流量

## 未來增強計畫

- 使用者評分與評論
- 餐廳收藏書籤功能
- 社交分享功能
- 與外送平台的整合
- 推送通知提醒

## 貢獻指南

此專案是 FatefulSupper-Android-App 群組的一部分。若要貢獻：
1. 建立功能分支
2. 提交您的變更
3. 推送到分支
4. 提交拉取請求

## 貢獻者

感謝所有為本專案做出貢獻的開發者！

| 貢獻者 | 主要貢獻 |
|--------|---------|
| **jostar0322** | 系統架構、-AI建立與導入、資料庫管理 |
| **Samuel** | 核心功能開發、API 整合、身份驗證系統 |
| **r96340** | UI/UX 優化、輪盤功能、地圖功能完善 |
| **Raychang323** |  登入與身份驗證、電郵驗證、專案管理、整合與發佈、 支援與協助|

### 主要功能分工
- **jostar0322**:
  -系統架構
  -AI建立與導入
  
- **Samuel**：
  - 核心應用程式架構設計
  - 使用者認證系統（JWT Token）
  - API 服務層整合
  - 後端連接與資料同步

- **r96340**：
  - 輪盤功能開發與優化
  - 地圖導航功能實作
  - 圖片載入與快取機制
  - UI 主題與暗色模式支援

- **Raychang323**：
  - 入與身份驗證、電郵驗證
  - 專案整合與發佈
  - 文檔建立與維護
  

### 主要開發分支

以下分支記錄了各階段的開發進展：

- `master` - 主分支（穩定版本）
- `feat/UI-UX` - UI/UX 功能優化
- `feat/wheel` - 輪盤轉盤功能
- `feat/explore` - 探索地圖功能
- `fix/email-verification` - 電郵驗證修復
- `fix/explore` - 探索功能修復
- `login` / `login2` - 登入與身份驗證開發
- `Feature_Explore_Newest` - 最新探索功能分支
- `icon` / `mergeicon` - 應用圖示設計與整合

## 授權條款


## 支援

如有問題、功能需求或疑問：
- 在 GitHub 上提出 Issue
- 聯絡開發團隊

---

**祝您宵夜愉快！🍜🍕🍜**
