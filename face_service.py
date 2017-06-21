import face_recognition
import picamera
import numpy as np
import io
import time
import sys
import os
import pickle

path = "./data/"

face_locations = []
face_encodings = []

faces_to_compare_faces = []
faces_to_compare_uuids = []

camera = picamera.PiCamera()
camera.resolution = (320, 240)
output = np.empty((240, 320, 3), dtype=np.uint8)

data = dict([(f, None) for f in os.listdir (path)])
for uuid in data:
    if os.path.isdir(path+uuid):
        faces_to_compare_faces.append(pickle.load(open(path+uuid+'/encoding', "rb")))
        faces_to_compare_uuids.append(uuid)

while True:
    camera.capture('image.jpg')
    output = face_recognition.load_image_file("image.jpg")

    face_locations = face_recognition.face_locations(output)
    face_encodings = face_recognition.face_encodings(output, face_locations)

    for face_encoding in face_encodings:
        matches = face_recognition.compare_faces(faces_to_compare_faces, face_encoding)
        name = "<Unknown>"
		
        if True in matches:
            name = faces_to_compare_uuids[matches.index(True)]

        print("{}".format(name))
        sys.stdout.flush()
