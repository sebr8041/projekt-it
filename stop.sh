#!/bin/sh -e

echo "stoppe: "
echo $(cat python_learn_face.pid)
echo "stoppe: "
echo $(cat java_lib.pid)

sudo kill $(cat python_learn_face.pid)
rm python_learn_face.pid
rm python_learn_face.log

kill $(cat java_lib.pid)
rm java_lib.pid
rm java_lib.log
