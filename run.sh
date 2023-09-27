pid=`pgrep -f iot-gateway.jar`
if [ -n "$pid" ]
  then
  kill -9 $pid
fi

nohup java  -jar -Xms512m -Xmx512m -Xss512k iot-gateway.jar >> /dev/null 2>&1 &
