echo "------ start jvb"
del ..\classes\jvb\*.jar
pushd jvb
call mvn clean package install -P buildFatJar -DskipTests
popd
cd
dir jvb\jvb\target\
copy jvb\jvb\target\jitsi*.jar ..\classes\jvb
echo "------ copy jvb"


echo "------ start jicofo"
del ..\classes\jicofo\*.jar
pushd jicofo
call mvn clean package install -P buildFatJar -DskipTests
popd
cd
dir jicofo\jicofo\target\
copy jicofo\jicofo\target\jicofo*.jar ..\classes\jicofo
echo "------ copy jicofo"
