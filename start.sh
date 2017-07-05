#!/bin/sh -e

nohup sh -c "python /opt/projekt-it/learn_face.py" > python_learn_face.log 2>&1 &
echo $! > python_learn_face.pid

nohup sh -c "python /opt/projekt-it/face_service.py | java -jar /opt/projekt-it/PiLib-0.0.1-SNAPSHOT.jar -mode face" > java_lib.log 2>&1 &
echo $! > java_lib.pid

exit 0
