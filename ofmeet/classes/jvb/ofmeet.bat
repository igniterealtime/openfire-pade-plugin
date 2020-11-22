@echo off

SETLOCAL enabledelayedexpansion

set mainClass=org.jitsi.videobridge.MainKt
set cp=jitsi-videobridge.jar
FOR %%F IN (lib/*.jar) DO (
  SET cp=!cp!;lib/%%F%
)

java -Dconfig.file=./application.conf -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=. -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=. -Djava.util.logging.config.file=lib/logging.properties -Djdk.tls.ephemeralDHKeySize=2048 -cp %cp% %mainClass% %*
