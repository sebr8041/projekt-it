#!/usr/bin/env python
"""
Usage::
    ./server.py [<port>]
"""
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
from cgi import parse_header, parse_multipart, parse_qs
import SocketServer
import base64
import uuid
import os
import httplib, urllib

def request():
    params = urllib.urlencode({'query': """PREFIX gruppe2: <http://gruppe02.pit.itm.uni-luebeck.de/>
PREFIX itm: <https://pit.itm.uni-luebeck.de/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

SELECT ?name WHERE {
	?cam itm:isType "Camera"^^xsd:string.
	?cam itm:hasStatus ?status.
	?status itm:hasValue ?name.
	?status itm:hasScaleUnit "Name"^^xsd:string.}"""})
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/csv"}
    conn = httplib.HTTPConnection("141.83.151.196:8080")
    conn.request("POST", "/services/sparql-endpoint", params, headers)
    response = conn.getresponse()
    data = response.read()
    conn.close()
    return data

class S(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        self._set_headers()
        if self.path == "/status":
            self.wfile.write(open('/opt/projekt-it/status.html', 'r').read())
        elif self.path == "/status/value":
            self.wfile.write(request())
        else:
            self.wfile.write(open('/opt/projekt-it/upload.html', 'r').read())

    def do_POST(self):
        ctype, pdict = parse_header(self.headers.getheader('content-type'))
        if ctype == 'multipart/form-data':
            postvars = parse_multipart(self.rfile, pdict)
        elif ctype == 'application/x-www-form-urlencoded':
            length = int(self.headers.getheader('content-length'))
            postvars = parse_qs(self.rfile.read(length), keep_blank_values=1)
        else:
            postvars = {}

        parts = postvars['image'][0].split(',')

        id = uuid.uuid1()
        os.mkdir("/opt/projekt-it/data/{}/".format(id))
        with open("/opt/projekt-it/data/{}/image".format(id), "wb") as fh:
            fh.write(base64.b64decode(parts[1]))
        with open("/opt/projekt-it/data/{}/name".format(id), "wb") as fh:
            fh.write(postvars['name'][0])
        with open("/opt/projekt-it/data/{}/cold".format(id), "wb") as fh:
            fh.write(postvars['cold'][0])
        with open("/opt/projekt-it/data/{}/warm".format(id), "wb") as fh:
            fh.write(postvars['warm'][0])

        self._set_headers()
        self.wfile.write("{}")

def run(server_class=HTTPServer, handler_class=S, port=8080):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print 'Starting httpd...'
    httpd.serve_forever()

if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
