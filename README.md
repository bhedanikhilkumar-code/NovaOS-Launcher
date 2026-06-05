# NovaOS Launcher 🚀
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-orange.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-red.svg)]()

NovaOS Launcher is a premium, high-performance, and deeply customizable Android launcher designed to deliver a modern, smooth, and clutter-free user experience inspired by clean iOS aesthetics. Built from the ground up using **Jetpack Compose** and modern Android development best practices.

---

## ✨ Features

- 📱 **Premium Home Screen Grid**: Responsive multi-page desktop layouts with automatic page indicators.
- 🗂️ **Dynamic Dock & Folders**: Bottom navigation dock and overlay folders with inline renaming and smooth transitions.
- 🔍 **App Library**: Alphabetical directory grouping with fast vertical A-Z scrolling and a real-time search engine.
- 🏝️ **Dynamic Island Notch**: Interactive notification-responsive camera notch overlay that feels alive.
- 🎛️ **iOS-Style Control Center**: Frosted glass toggle hub containing connectivity tiles, media controls, flashlight, auto-rotate, DND, volume, and real-time screen brightness sliders.
- 🔐 **Passcode App Lock & Hide**: Secure app launches with a master 4-digit PIN or hide apps entirely from drawer sidebar and main grids.
- 📅 **Today View Widgets Page**: Dedicated Page 0 widgets panel containing Analog ticking Clock, live system Battery progress gauge, AirPods mock indicator, weather conditions, shortcuts, and a monthly calendar grid.
- 💳 **Play Billing & AdMob Integration**: Integrated billing subscriptions and AdMob ad banner support with simulated developer bypasses.
- 🎨 **Deep Personalization**: Live customization of accent colors, icon shapes, grid structures (columns/rows), and preset beautiful gradients.
- 🛠️ **Modern Tech Stack**: Full MVVM clean architecture utilizing Jetpack Compose, Room Database, Jetpack DataStore, and Hilt Dependency Injection.

---


## 🛠️ Architecture & Tech Stack

This project follows the official Android architecture guidelines, emphasizing separation of concerns, scalability, and testability.

* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for declarative, state-driven, and highly interactive UI components.
* **Architecture Pattern**: MVVM (Model-View-ViewModel) with structured Clean Architecture layers:
  - **Domain**: Holds business logic, models (`AppInfo`, `HomeItem`, `FolderInfo`), and Use Cases.
  - **Data**: Manages databases, network/system APIs, local repositories, and data mapping.
  - **Presentation**: UI screens, widgets, and state holders (`ViewModels`).
* **Database**: [Room](https://developer.android.com/training/data-storage/room) for layout state persistence.
* **Preferences**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for lightweight, thread-safe launcher settings storage.
* **Dependency Injection**: [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for robust modular dependency resolution.
* **Asynchronous Programming**: Kotlin Coroutines and StateFlow for responsive reactive state binding.

---

## 📂 Project Structure

```
├── app
│   ├── src
│   │   └── main
│   │       ├── java/com/novaos/launcher
│   │       │   ├── core/           # Utilities, Receivers & Drag-Drop helpers
│   │       │   ├── data/           # Repositories, DAOs, Room DB & DataStore sources
│   │       │   ├── di/             # Hilt Modules
│   │       │   ├── domain/         # Business Models & Use Cases
│   │       │   └── ui/             # Composable Screens (Home, Settings, Library, etc.)
│   │       └── AndroidManifest.xml # Core app declarations & launcher intents
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala / Ladybug or newer.
- Android SDK 34 (API 34) minimum.
- Gradle 8.5+.

### Build & Run

1. Clone this repository:
   ```bash
   git clone https://github.com/bhedanikhilkumar-code/NovaOS-Launcher.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync project dependencies.
4. Select your Emulator/Physical Device.
5. Click **Run** (`Shift + F10`) or debug the project.

---

## 📜 License

This project is licensed under the Apache 2.0 License - see the LICENSE file for details.

---

*NovaOS Launcher is built as an independent launcher application. All product designs, assets, and implementations are custom and original.*
