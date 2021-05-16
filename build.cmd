call mvn clean package -Dmaven.test.skip=true

cd target
rename pade-openfire-plugin-assembly.jar pade.jar
rd "C:\openfire_4_6_3\plugins\pade" /q /s
del "C:\openfire_4_6_3\plugins\pade.jar" 
del "C:\openfire_4_6_3\logs\*.*"
copy pade.jar C:\openfire_4_6_3\plugins\pade.jar

pause