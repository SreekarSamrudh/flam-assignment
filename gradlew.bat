@echo off
setlocal

set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set GRADLE_OPTS=-Xmx2048m

cd /d %~dp0

if not exist gradle\wrapper\gradle-wrapper.jar (
    echo Downloading Gradle wrapper...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'}"
)

java -jar gradle\wrapper\gradle-wrapper.jar %*
