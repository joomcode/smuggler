./gradlew clean -PskipSample=true
./gradlew :smuggler-plugin:generateBuildClass :smuggler-runtime:assembleRelease -PskipSample=true
./gradlew bintrayUpload -PskipSample=true
