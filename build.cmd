call mvn clean package -Dmaven.test.skip=true

cd target
rename pade-openfire-plugin-assembly.jar pade.jar

pause