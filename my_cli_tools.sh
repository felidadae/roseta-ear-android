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
function logs_2 {
	adb logcat -c
	adb logcat -s LooperTouchEvent
}
