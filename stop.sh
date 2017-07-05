#!/bin/sh -e

sudo kill $(cat python_server.pid)
rm python_server.pid
rm  python_server.log

kill $(cat java_lib.pid)
rm java_lib.pid
rm java_lib.log
