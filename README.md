# BTAViewDemo

[![JitPack](https://jitpack.io/v/Tanamaz/BTAView.svg)](https://jitpack.io/#Tanamaz/BTAView)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A powerful Android Before/After comparison view with dynamic animations and multiple display effects.

![Demo GIF](https://github.com/Tanamaz/BTAView/blob/959b8dc30aa6ac87cdefd3254a67620da1e17ac6/gif/demo.gif)

## Features

- 🎭 Smooth swipe comparison animation
- ⚡ 4 animation curves (Linear/Accelerate/Decelerate/AccelerateDecelerate)
- 🖼️ Smart image scaling (centerCrop/fitCenter)
- 🔧 Full customization: corner radius, divider color/width
- 🔄 Auto-loop playback control
- 💾 Memory-safe resource management

## Quick Start

### Add Dependency
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.Tanamaz:BTAView:1.0.0'
}
```

### Basic Usage
```xml
    <com.navobytes.ui.btaview
        android:id="@+id/btaView1"
        android:layout_marginTop="120dp"
        android:layout_gravity="center_horizontal"
        android:layout_width="100dp"
        android:layout_height="100dp"
        app:beforeImage="@drawable/before"
        app:afterImage="@drawable/after"
        app:lineColor="#FFFFFF"
        app:lineWidth="1dp"
        app:roundRadius="90dp"/>
    <com.navobytes.ui.btaview
        android:id="@+id/btaView2"
        android:layout_marginTop="40dp"
        android:layout_gravity="center_horizontal"
        android:layout_width="240dp"
        android:layout_height="280dp"
        app:beforeImage="@drawable/before"
        app:afterImage="@drawable/after"
        app:lineColor="#FFFFFF"
        app:lineWidth="1dp"
        app:roundRadius="25dp"/>
```

### Code Control
```kotlin
val btaView = findViewById<BTAView>(R.id.btaView)

// Set images dynamically
btaView.setBeforeImageResource(R.drawable.new_before)
btaView.setAfterImageResource(R.drawable.new_after)

// Control animation
btaView.startAnimation()
btaView.pauseAnimation()
btaView.stopAnimation()

// Advanced configuration
btaView.setInterpolatorType(BTAView.INTERPOLATOR_ACCELERATE_DECELERATE)
btaView.setRoundRadius(16f)
```

## Full Attributes

| XML Attribute     | Type      | Default    | Description               |
| ----------------- | --------- | ---------- | ------------------------- |
| beforeImage       | reference | -          | Front image resource      |
| afterImage        | reference | -          | Back image resource       |
| animationDuration | integer   | 2000       | Single-pass duration (ms) |
| roundRadius       | dimension | 0dp        | View corner radius        |
| lineColor         | color     | #FFFFFF    | Divider color             |
| lineWidth         | dimension | 2dp        | Divider width             |
| autoPlay          | boolean   | true       | Auto-play animation       |
| loop              | boolean   | true       | Loop animation            |
| loopInterval      | integer   | 0          | Loop interval (ms)        |
| interpolatorType  | enum      | linear     | Animation curve type      |
| scaleType         | enum      | centerCrop | Image scaling mode        |

## Advanced Features

### Custom Animation Curves
```kotlin
// Available types:
// - INTERPOLATOR_LINEAR
// - INTERPOLATOR_ACCELERATE
// - INTERPOLATOR_DECELERATE
// - INTERPOLATOR_ACCELERATE_DECELERATE

btaView.setInterpolatorType(BTAView.INTERPOLATOR_ACCELERATE_DECELERATE)
```

### Image Scaling Modes
```kotlin
// Available modes:
// - SCALE_TYPE_CENTER_CROP
// - SCALE_TYPE_FIT_CENTER

btaView.setScaleType(BTAView.SCALE_TYPE_FIT_CENTER)
```

## Contributing

We welcome contributions through Issues and PRs! Please ensure:  
1. Follow existing code style  
2. Pass all tests before submitting  
3. Update relevant documentation  

## License

```
MIT License

Copyright (c) 2024 Tanamaz

Permission is hereby granted...
```
