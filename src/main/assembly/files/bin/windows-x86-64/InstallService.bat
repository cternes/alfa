set PR_PATH=%CD%
SET PR_SERVICE_NAME=AlfaService
SET PR_JAR=../../alfa.jar
SET PR_DESC=Azure logfile analyzer
SET START_CLASS=de.slackspace.alfa.Main
SET START_METHOD=main
SET START_PARAMS=serviceStart
SET STOP_CLASS=de.slackspace.alfa.Main
SET STOP_METHOD=stop
rem ; separated values
SET STOP_PARAMS=serviceStop
rem ; separated values
SET JVM_OPTIONS=-Dapp.home=%PR_PATH%
prunsrv.exe //IS//%PR_SERVICE_NAME% --Install="%PR_PATH%\prunsrv.exe" --Jvm=auto --Startup=auto --StartMode=jvm --StartClass=%START_CLASS% --StartMethod=%START_METHOD% --StartParams=%START_PARAMS% --StopMode=jvm --StopClass=%STOP_CLASS% --StopMethod=%STOP_METHOD% --StopParams=%STOP_PARAMS% --Classpath="%PR_PATH%\%PR_JAR%" --DisplayName="%PR_SERVICE_NAME%" ++JvmOptions=%JVM_OPTIONS% --LogPath=../../logs --Description=%PR_DESC%