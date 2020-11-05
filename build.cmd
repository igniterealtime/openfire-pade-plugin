call mvn clean package -Dmaven.test.skip=true

copy ofmeet\target\ofmeet.jar C:\openfire_4_6_0\plugins\ofmeet.jar
copy offocus\target\offocus.jar C:\openfire_4_6_0\plugins\offocus.jar
copy pade\target\pade.jar C:\openfire_4_6_0\plugins\pade.jar

pause