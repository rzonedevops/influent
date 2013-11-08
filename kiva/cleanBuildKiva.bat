cd %XF_HOME%
call mvn clean install

IF "%JETTY_HOME%" == "" GOTO tomcat
copy %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT.war %JETTY_HOME%\webapps\kiva.war

:tomcat
IF "%TOMCAT_HOME%" == "" GOTO end
echo "Attempting to clean TOMCAT_HOME\webapps\kiva"
rmdir %TOMCAT_HOME%\webapps\kiva /s /q

echo "Copy war file to TOMCAT_HOME\webapps\kiva.war"
copy %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT.war %TOMCAT_HOME%\webapps\kiva.war

:end
