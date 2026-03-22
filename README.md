# RemoteCompose KMP

A Kotlin Multiplatform rewrite of [RemoteCompose](https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/compose/remote/) — a library that serializes Compose UI operations into a compact binary document format for remote playback.

This project enables both **playback and creation** of RemoteCompose documents on **Android and iOS** via [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

## Architecture

RemoteCompose documents are platform-independent binary buffers containing drawing operations, layout trees, animations, and expressions. A *creator* serializes UI into this format; a *player* deserializes and renders it using the platform's graphics stack.

```
┌──────────────────┐          ┌──────────────────┐
│  Creation API    │  ─────►  │  Binary Document  │
│  (remote-creation│          │  (wire protocol)  │
│   -core)         │          └────────┬─────────┘
└──────────────────┘                   │
                                       ▼
                              ┌──────────────────┐
                              │  Player           │
                              │  (remote-player   │
                              │   -compose)       │
                              └──────────────────┘
                              Android ◄──► iOS
```

## Modules

| Module | Description |
|--------|-------------|
| `remote-core` | Wire protocol, operations, layout engine, expressions, easing — all in `commonMain` |
| `remote-creation-core` | Procedural document creation API (`RemoteComposeWriter`) |
| `remote-creation` | Platform path abstractions (`expect`/`actual` for Path, Matrix) |
| `remote-player-core` | Player state, document loading, value types |
| `remote-player-compose` | Compose Multiplatform player composable with platform rendering |
| `sample/shared` | Shared sample documents and `SampleApp` composable |
| `sample/androidApp` | Android demo app |
| `sample/iosApp` | iOS demo app (Xcode project) |

## iOS Rendering

iOS rendering uses Skia APIs (via [Skiko](https://github.com/JetBrains/skiko)) that are bundled with Compose Multiplatform:

- **Text**: `Font`, `TextLine`, `Paragraph` API for single-line and multi-line layout
- **Bitmaps**: `Image.makeFromEncoded()` for PNG, `Bitmap` for raw pixel data
- **Gradients**: CMP `LinearGradientShader`, `RadialGradientShader`, `SweepGradientShader`
- **Runtime shaders**: `RuntimeEffect.makeForShader()` with SKSL
- **Graphics layers**: Offscreen `Bitmap` + `ShadowUtils.drawShadow()` + `ImageFilter.makeBlur()`
- **Fonts**: `FontMgr.matchFamily()` with weight/italic style matching
- **Path text**: `PathMeasure` for glyph-by-glyph placement along paths

## Building

### Prerequisites

- JDK 17+
- Android SDK (API 35)
- Xcode 15+ (for iOS)

### Compile all modules

```bash
# iOS
./gradlew compileKotlinIosSimulatorArm64

# Android
./gradlew compileDebugKotlinAndroid
```

### Run tests

```bash
./gradlew :remote-core:testDebugUnitTest
```

### Run the Android sample

```bash
./gradlew :sample:androidApp:installDebug
```

### Run the iOS sample

```bash
open sample/iosApp/iosApp.xcodeproj
```

Build and run from Xcode. The "Compile Kotlin" build phase invokes Gradle to build and embed the shared framework automatically.

## Project Structure

```
remote-compose-kmp/
├── remote-core/                    # Wire protocol & operations (commonMain only)
│   └── src/
│       ├── commonMain/kotlin/      # All operations, layout, expressions
│       ├── commonTest/kotlin/      # 66 unit tests
│       ├── androidMain/kotlin/     # Time/date actuals (java.time)
│       └── nativeMain/kotlin/      # Time/date actuals (Foundation)
│
├── remote-creation-core/           # Document creation API (commonMain only)
├── remote-creation/                # Platform path abstractions
│   └── src/
│       ├── commonMain/             # expect declarations
│       ├── androidMain/            # android.graphics.Path/Matrix
│       └── iosMain/                # Pure Kotlin path implementation
│
├── remote-player-core/             # Player interfaces & state
│   └── src/
│       ├── commonMain/             # PlayerState, RcValue, BitmapLoader
│       ├── androidMain/            # AndroidRemoteContext, AndroidPaintContext
│       └── iosMain/                # Thread utilities
│
├── remote-player-compose/          # CMP player composable
│   └── src/
│       ├── commonMain/             # RemoteComposePlayer, ComposePaintContext
│       ├── androidMain/            # StaticLayout, RenderNode, BitmapFactory
│       └── iosMain/                # Skia text, bitmap, shaders, graphics layers
│
└── sample/
    ├── shared/                     # SampleApp composable + sample documents
    ├── androidApp/                 # Android Activity
    └── iosApp/                     # Xcode project + Swift integration
```

## License

```
Copyright 2023 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
