#!/bin/sh -e

nohup sudo python /opt/projekt-it/server.py > python_server.log 2>&1 &
echo $! > python_server.pid

nohup java -jar /opt/projekt-it/PiLib-0.0.1-SNAPSHOT.jar -mode sound > java_lib.log 2>&1  &
echo $! > java_lib.pid

exit 0


