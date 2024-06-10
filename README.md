[![](https://jitpack.io/v/jkatzwinkel/tla-common.svg)](https://jitpack.io/#jkatzwinkel/tla-common)
![build](https://github.com/JKatzwinkel/tla-common/workflows/build/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-89.59%25-brightgreen.svg)
![METHOD](https://img.shields.io/badge/method--coverage-83.85%25-brightgreen.svg)

Copyright (c) 2020-2021 Berlin-Brandenburgische Akademie der Wissenschaften

# TLA data transfer model library

Use this to speak to speak to TLA backend endpoints.

## Installation

Add this to your gradle file:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jkatzwinkel:tla-common:main-SNAPSHOT'
}
```

## Misc

You can check for the newest version of package dependencies by running:

    ./gradlew dependencyUpdates


publish to local maven repository:

    ./gradlew publishToMavenLocal
