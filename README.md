# BTAView

[![JitPack](https://jitpack.io/v/Tanamaz/BTAView.svg)](https://jitpack.io/#Tanamaz/BTAView)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A powerful Android Before To After comparison view with dynamic animations and multiple display effects.

![Demo GIF](https://github.com/Tanamaz/BTAView/blob/959b8dc30aa6ac87cdefd3254a67620da1e17ac6/gif/demo.gif)

## Features

- 🎭 Smooth swipe comparison animation
- ⚡ 4 animation curves (Linear/Accelerate/Decelerate/AccelerateDecelerate)
- 🔍 Support for zoomed-in details
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
    implementation 'com.github.Tanamaz:BTAView:1.0.1'
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
        app:beforeImage="@drawable/before_1"
        app:afterImage="@drawable/after_1"
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
        app:roundRadius="25dp"
        app:forwardDuration="1500"
        app:backwardDuration="1000"
        app:maxScale="1.5"
        app:loopInterval="1500"
        app:interpolatorType="accelerate" />
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

| XML Attribute    | Type      | Default               | Description          |
| ---------------- | --------- | --------------------- | -------------------- |
| beforeImage      | reference | -                     | Front image resource |
| afterImage       | reference | -                     | Back image resource  |
| scaleType        | enum      | centerCrop            | Image scaling mode   |
| roundRadius      | dimension | 0dp                   | View corner radius   |
| lineColor        | color     | #FFFFFF               | Divider color        |
| lineWidth        | dimension | 1dp                   | Divider width        |
| autoPlay         | boolean   | true                  | Auto-play animation  |
| loop             | boolean   | true                  | Loop animation       |
| loopInterval     | integer   | 1500                  | Loop interval (ms)   |
| interpolatorType | enum      | accelerate decelerate | Animation curve type |
| forwardDuration  | integer   | 1500                  | duration (ms)        |
| backwardDuration | integer   | 1000                  | duration (ms)        |
| maxScale         | float     | 1.0                   | No zoom by default   |

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
