call mvn clean package -Dmaven.test.skip=true

cd target
rename pade-openfire-plugin-assembly.jar pade.jar
rd "D:\Openfire\openfire_4_9_0\plugins\pade" /q /s
del "D:\Openfire\openfire_4_9_0\plugins\pade.jar" 
del /q "D:\Openfire\openfire_4_9_0\logs\*.*"
copy pade.jar D:\Openfire\openfire_4_9_0\plugins\pade.jar

rd "D:\Projects\openfire-cluster\node1\plugins\pade" /q /s
del "D:\Projects\openfire-cluster\node1\plugins\pade.jar" 
del /q "D:\Projects\openfire-cluster\node1\logs\*.*"
copy pade.jar D:\Projects\openfire-cluster\node1\plugins\pade.jar

rd "D:\Projects\openfire-cluster\node2\plugins\pade" /q /s
del "D:\Projects\openfire-cluster\node2\plugins\pade.jar" 
del /q "D:\Projects\openfire-cluster\node2\logs\*.*"
copy pade.jar D:\Projects\openfire-cluster\node2\plugins\pade.jar

rd "D:\Projects\openfire-fmuc\node1\plugins\pade" /q /s
del "D:\Projects\openfire-fmuc\node1\plugins\pade.jar" 
del /q "D:\Projects\openfire-fmuc\node1\logs\*.*"
copy pade.jar D:\Projects\openfire-fmuc\node1\plugins\pade.jar

rd "D:\Projects\openfire-fmuc\node2\plugins\pade" /q /s
del "D:\Projects\openfire-fmuc\node2\plugins\pade.jar" 
del /q "D:\Projects\openfire-fmuc\node2\logs\*.*"
copy pade.jar D:\Projects\openfire-fmuc\node2\plugins\pade.jar

pause