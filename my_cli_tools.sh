#rebuild tools
function re {
	./gradlew assembleDebug
	./gradlew installDebug
	adb install -r $(find . -name '*.apk')
}
function logs {
	adb logcat -c
	adb logcat -s LooperEvent
}
