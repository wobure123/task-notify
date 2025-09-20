plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("kotlin-kapt") apply false
}

// Centralized versions
ext.apply {
    set("composeBomVersion", "2024.06.00")
    set("hiltVersion", "2.51.1")
}
