cd %XF_HOME%
call mvn clean
call mvn install

IF "%JETTY_HOME%" == "" GOTO tomcat
copy %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT.war %JETTY_HOME%\webapps\bitcoin.war

:tomcat
IF "%TOMCAT_HOME%" == "" GOTO end
echo "Attempting to clean TOMCAT_HOME\webapps\bitcoin"
rmdir %TOMCAT_HOME%\webapps\bitcoin /s /q

echo "Copy war file to TOMCAT_HOME\webapps\bitcoin.war"
copy %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT.war %TOMCAT_HOME%\webapps\bitcoin.war

:end
