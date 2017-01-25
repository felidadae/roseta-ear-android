#rebuild tools
function re {
	./gradlew assembleDebug
	./gradlew installDebug
	# sudo apt-get install adb
	adb install -r $(find . -name *.apk)
}
function openhere {
	android-studio ../Rosetus &
}
