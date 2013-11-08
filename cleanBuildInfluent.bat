cd %XF_HOME%
call mvn clean
call mvn install

IF "%JETTY_HOME%" == "" GOTO tomcat
echo "Attempting to copy war files to Jetty"
copy %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT.war %JETTY_HOME%\webapps\kiva.war
copy %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT.war %JETTY_HOME%\webapps\bitcoin.war

:tomcat

IF "%TOMCAT_HOME%" == "" GOTO end
rmdir %TOMCAT_HOME%\webapps\kiva /s /q
rmdir %TOMCAT_HOME%\webapps\bitcoin /s /q

echo "Attempting to copy war files to Tomcat"
copy %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT.war %TOMCAT_HOME%\webapps\kiva.war
copy %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT.war %TOMCAT_HOME%\webapps\bitcoin.war

:end
