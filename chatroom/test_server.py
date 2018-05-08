import utils

import random
import socket
import sys
import time
import client

def test_send(message):
	

if (len(sys.argv)) < 3:
   print "Usage: python test_server.py server_hostname server_port"
   sys.exit(1)

client1 = client.Client("Bruce", sys.argv[1], int(sys.argv[2]))
client2 = client.Client("Bin", sys.argv[1], int(sys.argv[2]))

client1.send("\create Tomato")
client2.send("\join Tomato")

