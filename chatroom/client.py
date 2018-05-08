import select
import socket
import sys
import utils

CLIENT_TO_BUFFER = {}


def pad_message(message):
  while len(message) < utils.MESSAGE_LENGTH:
    message += " "
  return message[:utils.MESSAGE_LENGTH]

class Client(object):

	def __init__(self, name, host, port):
		self.name = name
		self.host = host
		self.port = int(port)
		self.socket = socket.socket()
		CLIENT_TO_BUFFER[self.socket] = ""
		



	# Send a message to the server.
	def send(self, message):
		try:
			char_sent = 0
			padded_message = pad_message(message)
			while char_sent < utils.MESSAGE_LENGTH:
				sent = self.socket.send(padded_message)
				char_sent = char_sent + sent
		except:
			print utils.CLIENT_CANNOT_CONNECT.format(self.host, self.port)
			sys.exit()

	# Get rid of all the new lines given to the message. 
	def sanitize(self, msg):
		return msg.rstrip()


	# Run the client socket. 
	def run(self):
		try:
			self.socket.connect((self.host, self.port))
		except:
			print utils.CLIENT_CANNOT_CONNECT.format(self.host, self.port)
			sys.exit()
		self.send(self.name)
		sys.stdout.write("\r" + utils.CLIENT_MESSAGE_PREFIX)
		sys.stdout.flush()
		# Listen in for outputs from StdIn and Server
		while True:
			socket_list = [sys.stdin, self.socket]
			ready_to_read, ready_to_write, in_error = select.select(socket_list, [], [], 0)
			for socket in ready_to_read:
				# Means that you got something from the server so do the receive stuff. 
				if socket == self.socket:
					buff = socket.recv(utils.MESSAGE_LENGTH)
					if len(buff) == 0:
						# If message contains nothing, connection is broken. Remove dead sockets.
						print utils.CLIENT_SERVER_DISCONNECTED.format(self.host, self.port)
						sys.exit()
					else:
						# Put message into buffer. 
						message_so_far = CLIENT_TO_BUFFER[socket] 
						CLIENT_TO_BUFFER[socket] = message_so_far + buff
						message = CLIENT_TO_BUFFER[socket]
						# If buffer is 200 characters, can actually do stuff with it now. 
						if len(message) == utils.MESSAGE_LENGTH:
							message = message.rstrip()
							CLIENT_TO_BUFFER[socket] = ""
						# Syntax stuff. Use ]r to start from the beginning.
							sys.stdout.write(utils.CLIENT_WIPE_ME + "\r" + self.sanitize(message) + "\n")
							sys.stdout.write('\r' + '[Me] ')
							sys.stdout.flush()
				# Means this is what you get from StdIN so you gotta send to the server.
				else:
					message = sys.stdin.readline()
					self.send(message)
					# Message so far is [Me] message. 
					sys.stdout.write("\r" + utils.CLIENT_MESSAGE_PREFIX)
					sys.stdout.flush()




args = sys.argv
if len(args) != 4:
	print "Please supply a name, server address and port."
	sys.exit()
client = Client(args[1], args[2], args[3])

client.run()
