#!/bin/bash
# chkconfig: 345 88 14
# description: Tomcat Daemon

# Source function library.
. /etc/rc.d/init.d/functions

JAVA_HOME=/usr/local/java/jdk1.8.0_131
JAVA_BIN=$JAVA_HOME/bin
CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
PATH=$JAVA_BIN:$PATH
export PATH

# 根据参数选择调用   
case "$1" in   
  start)   
echo -n $"Starting Tomcat service: "  
su - app_zhuanquan -c '/home/zhuanquan/apache-tomcat-7.0.77/bin/startup.sh'
RETVAL=$?   
;;   
  stop)   
echo -n $"Stopping Tomcat service: " 
su - app_zhuanquan -c '/home/zhuanquan/apache-tomcat-7.0.77/bin/catalina.sh stop -force'
RETVAL=$?   
echo   
;;   
  restart)   
echo -n $"Stopping Tomcat service: " 
su - app_zhuanquan -c '/home/zhuanquan/apache-tomcat-7.0.77/bin/catalina.sh stop -force'
RETVAL=$?   
echo   
echo -n $"Starting Tomcat service: "  
su - app_zhuanquan -c '/home/zhuanquan/apache-tomcat-7.0.77/bin/startup.sh'
RETVAL=$?   
echo     
;;   
  *)   
echo $"Usage: $0 start|stop|restart"  
exit 1  
esac   
  
exit 0  
