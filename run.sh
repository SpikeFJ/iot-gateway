pid=`pgrep -f iot-gateway.jar`
if [ -n "$pid" ]
  then
  kill -9 $pid
fi

nohup java  -Dcom.sun.management.jmxremote -Djava.net.preferIPv4Stack=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false  -Djava.rmi.server.hostname=144.202.100.130 -Dcom.sun.management.jmxremote.port=9093  -jar -Xms512m -Xmx512m -Xss512k iot-gateway.jar >> /dev/null 2>&1 &
