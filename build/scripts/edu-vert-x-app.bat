@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  edu-vert-x-app startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and EDU_VERT_X_APP_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\edu-vert-x-app-1.0.0-SNAPSHOT.jar;%APP_HOME%\lib\vertx-launcher-application-5.0.0.jar;%APP_HOME%\lib\vertx-web-5.0.0.jar;%APP_HOME%\lib\vertx-pg-client-5.0.0.jar;%APP_HOME%\lib\vertx-web-common-5.0.0.jar;%APP_HOME%\lib\vertx-auth-common-5.0.0.jar;%APP_HOME%\lib\vertx-bridge-common-5.0.0.jar;%APP_HOME%\lib\vertx-sql-client-5.0.0.jar;%APP_HOME%\lib\vertx-core-5.0.0.jar;%APP_HOME%\lib\flyway-database-postgresql-11.8.2.jar;%APP_HOME%\lib\flyway-core-11.8.2.jar;%APP_HOME%\lib\postgresql-42.7.5.jar;%APP_HOME%\lib\slf4j-simple-2.0.17.jar;%APP_HOME%\lib\slf4j-api-2.0.17.jar;%APP_HOME%\lib\lombok-1.18.38.jar;%APP_HOME%\lib\scram-client-3.1.jar;%APP_HOME%\lib\netty-handler-proxy-4.2.1.Final.jar;%APP_HOME%\lib\netty-codec-http2-4.2.1.Final.jar;%APP_HOME%\lib\netty-codec-http-4.2.1.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.2.1.Final.jar;%APP_HOME%\lib\netty-handler-4.2.1.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.2.1.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.2.1.Final.jar;%APP_HOME%\lib\netty-codec-compression-4.2.1.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.2.1.Final.jar;%APP_HOME%\lib\netty-codec-base-4.2.1.Final.jar;%APP_HOME%\lib\netty-transport-4.2.1.Final.jar;%APP_HOME%\lib\netty-buffer-4.2.1.Final.jar;%APP_HOME%\lib\netty-resolver-4.2.1.Final.jar;%APP_HOME%\lib\netty-common-4.2.1.Final.jar;%APP_HOME%\lib\jackson-core-2.18.2.jar;%APP_HOME%\lib\jackson-databind-2.18.2.jar;%APP_HOME%\lib\jackson-annotations-2.18.2.jar;%APP_HOME%\lib\vertx-core-logging-5.0.0.jar;%APP_HOME%\lib\checker-qual-3.48.3.jar;%APP_HOME%\lib\picocli-4.7.4.jar;%APP_HOME%\lib\scram-common-3.1.jar;%APP_HOME%\lib\saslprep-2.2.jar;%APP_HOME%\lib\stringprep-2.2.jar


@rem Execute edu-vert-x-app
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %EDU_VERT_X_APP_OPTS%  -classpath "%CLASSPATH%" io.vertx.launcher.application.VertxApplication %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable EDU_VERT_X_APP_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%EDU_VERT_X_APP_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
