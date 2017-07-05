import face_recognition
import picamera
import pickle
import numpy
import os, time

path_to_watch = "/opt/projekt-it/data/"

before = dict([(f, None) for f in os.listdir (path_to_watch)])

while 1:
	time.sleep(10)
	after = dict([(f, None) for f in os.listdir (path_to_watch)])
	added = [f for f in after if not f in before]
	#print added
	if added:
		print "Added: ", ", ".join(added)
		before = after
		time.sleep(60)
		for uuid in added:
			# Learn face_encoding from picture
			image = face_recognition.load_image_file(path_to_watch+uuid+'/image')
			face_encoding = face_recognition.face_encodings(image)[0]

			# Serialize face_encoding and write to file
			filename = path_to_watch + uuid + '/encoding'
			f = open(filename, 'w')
			pickle.dump(face_encoding,f)
			print 'learned ' + filename

