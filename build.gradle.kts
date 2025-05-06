buildscript {
    repositories {
        google()  // Assure-toi d'avoir ce repository
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")  // Utilise la dernière version disponible
    }
}
