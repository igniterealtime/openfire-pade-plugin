call mvn clean package -Dmaven.test.skip=true

cd target
rename pade-openfire-plugin-assembly.jar pade.jar
copy pade.jar C:\openfire_4_6_3\plugins\pade.jar

pause