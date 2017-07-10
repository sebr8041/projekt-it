import face_recognition
import picamera
import numpy as np
import io
import time
import sys
import os
import pickle
import RPi.GPIO as GPIO

path = "/opt/projekt-it/data/"
pin_number = 37

face_locations = []
face_encodings = []

faces_to_compare_faces = []
faces_to_compare_uuids = []

# setup LED
GPIO.setmode(GPIO.BOARD)
GPIO.setup(pin_number, GPIO.OUT)

# setup camera
camera = picamera.PiCamera()
camera.resolution = (320, 240)
output = np.empty((240 * 320 * 3), dtype=np.uint8)

# load known encoded faces
data = dict([(f, None) for f in os.listdir (path)])
for uuid in data:
    if os.path.isdir(path+uuid) and os.path.exists(path+uuid+'/encoding'):
        faces_to_compare_faces.append(pickle.load(open(path+uuid+'/encoding', "rb")))
        faces_to_compare_uuids.append(uuid)

while True:
    # watch for new encoded faces online
    new_data = dict([(f, None) for f in os.listdir (path)])
    added = [f for f in new_data if not f in data]
    if added:
        time.sleep(20) # encoding might not be there
        for uuid in added:
		if os.path.exists(path+uuid+'/encoding'):
            		faces_to_compare_faces.append(pickle.load(open(path+uuid+'/encoding', "rb")))
            		faces_to_compare_uuids.append(uuid)
			data = new_data

    # take a picture and indicate with LED
    GPIO.output(pin_number, GPIO.HIGH)
    camera.capture(output, format = 'rgb')
    output_shaped = output.reshape((240,320,3))
    GPIO.output(pin_number, GPIO.LOW)

    # recognize face
    face_locations = face_recognition.face_locations(output_shaped)
    face_encodings = face_recognition.face_encodings(output_shaped, face_locations)
    for face_encoding in face_encodings:
        matches = face_recognition.compare_faces(faces_to_compare_faces, face_encoding)
        name = "<Unknown>"
        if True in matches:
            name = faces_to_compare_uuids[matches.index(True)]

        print("{}".format(name))
        sys.stdout.flush() # instant output for piping
