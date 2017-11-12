./gradlew clean
./gradlew :smuggler-plugin:generateBuildClass :smuggler-runtime:assembleRelease -PskipSample=true
./gradlew publishToMavenLocal -PskipSample=true
