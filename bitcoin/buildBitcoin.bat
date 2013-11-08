cd %XF_HOME%
call mvn clean
call mvn install

IF "%JETTY_HOME%" == "" GOTO tomcat
copy %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT.war %JETTY_HOME%\webapps\bitcoin.war

:tomcat
IF "%TOMCAT_HOME%" == "" GOTO end
copy %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT.war %TOMCAT_HOME%\webapps\bitcoin.war

:end
