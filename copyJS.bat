xcopy %XF_HOME%\kiva\src\main\resources\config.js %TOMCAT_HOME%\webapps\kiva\WEB-INF\classes\ /Y
xcopy %XF_HOME%\kiva\src\main\webapp\img\* %TOMCAT_HOME%\webapps\kiva\img\ /SY
xcopy %XF_HOME%\kiva\src\main\webapp\scripts\* %TOMCAT_HOME%\webapps\kiva\scripts\ /SY
xcopy %XF_HOME%\kiva\src\main\webapp\theme\* %TOMCAT_HOME%\webapps\kiva\theme\ /SY
xcopy %XF_HOME%\influent-client\src\main.jsp %TOMCAT_HOME%\webapps\kiva\ /SY
xcopy %XF_HOME%\influent-client\src\img\* %TOMCAT_HOME%\webapps\kiva\img\ /SY
xcopy %XF_HOME%\influent-client\src\scripts\* %TOMCAT_HOME%\webapps\kiva\scripts\ /SY
xcopy %XF_HOME%\influent-client\src\theme\* %TOMCAT_HOME%\webapps\kiva\theme\ /SY

xcopy %XF_HOME%\kiva\src\main\resources\config.js %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\WEB-INF\classes\ /Y
xcopy %XF_HOME%\kiva\src\main\webapp\img\* %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\img\ /SY
xcopy %XF_HOME%\kiva\src\main\webapp\scripts\* %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\scripts\ /SY
xcopy %XF_HOME%\kiva\src\main\webapp\theme\* %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\theme\ /SY
xcopy %XF_HOME%\influent-client\src\main.jsp %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\ /SY
xcopy %XF_HOME%\influent-client\src\img\* %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\img\ /SY
xcopy %XF_HOME%\influent-client\src\scripts\* %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\scripts\ /SY
xcopy %XF_HOME%\influent-client\src\theme\* %XF_HOME%\kiva\target\kiva-1.0.0-SNAPSHOT\webapps\kiva\theme\ /SY

xcopy %XF_HOME%\bitcoin\src\main\resources\config.js %TOMCAT_HOME%\webapps\bitcoin\WEB-INF\classes\ /Y
xcopy %XF_HOME%\bitcoin\src\main\webapp\img\* %TOMCAT_HOME%\webapps\bitcoin\img\ /SY
xcopy %XF_HOME%\bitcoin\src\main\webapp\scripts\* %TOMCAT_HOME%\webapps\bitcoin\scripts\ /SY
xcopy %XF_HOME%\bitcoin\src\main\webapp\theme\* %TOMCAT_HOME%\webapps\bitcoin\theme\ /SY
xcopy %XF_HOME%\influent-client\src\main.jsp %TOMCAT_HOME%\webapps\bitcoin\ /SY
xcopy %XF_HOME%\influent-client\src\img\* %TOMCAT_HOME%\webapps\bitcoin\img\ /SY
xcopy %XF_HOME%\influent-client\src\scripts\* %TOMCAT_HOME%\webapps\bitcoin\scripts\ /SY
xcopy %XF_HOME%\influent-client\src\theme\* %TOMCAT_HOME%\webapps\bitcoin\theme\ /SY

xcopy %XF_HOME%\bitcoin\src\main\resources\config.js %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\WEB-INF\classes\ /Y
xcopy %XF_HOME%\bitcoin\src\main\webapp\img\* %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\img\ /SY
xcopy %XF_HOME%\bitcoin\src\main\webapp\scripts\* %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\scripts\ /SY
xcopy %XF_HOME%\bitcoin\src\main\webapp\theme\* %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\theme\ /SY
xcopy %XF_HOME%\influent-client\src\main.jsp %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\ /SY
xcopy %XF_HOME%\influent-client\src\img\* %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\img\ /SY
xcopy %XF_HOME%\influent-client\src\scripts\* %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\scripts\ /SY
xcopy %XF_HOME%\influent-client\src\theme\* %XF_HOME%\bitcoin\target\bitcoin-1.0.0-SNAPSHOT\webapps\bitcoin\theme\ /SY