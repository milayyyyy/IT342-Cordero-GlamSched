@echo off
set JAVA_HOME=C:\Users\Admin\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\21
set PATH=%JAVA_HOME%\bin;%PATH%
set MVNBIN=C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798\bin\mvn.cmd
cd /d "c:\Users\Admin\Desktop\glamshed-booking-platform\IT342-Cordero-GlamSched\backend"
"%MVNBIN%" test
