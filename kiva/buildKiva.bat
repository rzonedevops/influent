cd %XF_HOME%
call mvn clean
call mvn install

IF "%JETTY_HOME%" == "" GOTO tomcat
copy %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT.war %JETTY_HOME%\webapps\kiva.war

:tomcat
IF "%TOMCAT_HOME%" == "" GOTO end
copy %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT.war %TOMCAT_HOME%\webapps\kiva.war

:end
