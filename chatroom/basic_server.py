import select
import socket
import sys
import utils

args = sys.argv
SOCKET_LIST = []
NEW_SOCKET_LIST = []
SOCKET_TO_NAME = {}
CHANNELS_TO_SOCKETS = {}
SOCKET_TO_CHANNELS = {}


def pad_message(message):
	while len(message) < utils.MESSAGE_LENGTH:
		message += " "
	return message[:utils.MESSAGE_LENGTH]

class Server(object):


	def __init__(self, port):
		self.socket = socket.socket()
		self.socket.bind(("", int(port)))
		self.socket.listen(5)

	def run(self):
		SOCKET_LIST.append(self.socket)
		while True:
			ready_to_read, ready_to_write, in_error = select.select(SOCKET_LIST, [], [], 0)
			for socket in ready_to_read:
				# if socket that's ready to read is this one, we know we got an incoming connection
				if socket == self.socket:
					(new_socket, host) = self.socket.accept()
					SOCKET_LIST.append(new_socket)	
					NEW_SOCKET_LIST.append(new_socket)
				else:
					# one of our clients just sent out a message
					# try:
						chunks = []
						bytes_recv = 0
						while bytes_recv < utils.MESSAGE_LENGTH:
							chunk = socket.recv(utils.MESSAGE_LENGTH - bytes_recv)
							chunks.append(chunk)
							bytes_recv = bytes_recv + len(chunk)
						message = ' '.join(chunks)
						# Take out the spaces of the message for simplicity sake. 
						message = message.rstrip()
						# If message contains nothing, connection is broken. Remove dead sockets.
						if not message:
							if socket in SOCKET_LIST:
								SOCKET_LIST.remove(socket)
								# If socket is in a channel, let everyone know they left. 
								if socket in SOCKET_TO_CHANNELS:
									channel_name = SOCKET_TO_CHANNELS[socket]
									name = SOCKET_TO_NAME[socket]
									error_message = utils.SERVER_CLIENT_LEFT_CHANNEL.format(name)
									self.broadcast(socket, error_message, channel_name, True)
						# Check if control message
						if message[0] == '/':
							self.do_control_stuff(socket, message)
						else:
							# Check if it's the first message. If it is, connect the name to the socket by putting it in same index of socket list.
							if socket in NEW_SOCKET_LIST:
								SOCKET_TO_NAME[socket] = message
								NEW_SOCKET_LIST.remove(socket)
							else:
								# If client not in a channel, send them an error message.
								if socket not in SOCKET_TO_CHANNELS:
									error_message = utils.SERVER_CLIENT_NOT_IN_CHANNEL
									self.send(socket, error_message)
								else:
									# Get the name and then broadcast to everyone in the channel. 
									channel_name = SOCKET_TO_CHANNELS[socket]
									self.broadcast(socket, message, channel_name)
					# except:
						# if socket in SOCKET_TO_CHANNELS:
						# 	channel_name = SOCKET_TO_CHANNELS[socket]
						# 	name = SOCKET_TO_NAME[socket]
						# 	error_message = utils.SERVER_CLIENT_LEFT_CHANNEL.format(name)
						# 	self.broadcast(socket, error_message, channel_name)
						# continue


	def do_control_stuff(self, socket, msg):
		split_msg = msg.split()
		if len(split_msg) > 1:
			channel_name = split_msg[1]
		if (split_msg[0] == "/create"):
			# if its just "/create"
			if len(split_msg) == 1:
				error_message = utils.SERVER_CREATE_REQUIRES_ARGUMENT
				self.send(socket, error_message)
			# if channel already exists
			elif channel_name in CHANNELS_TO_SOCKETS:
				error_message = utils.SERVER_CHANNEL_EXISTS.format(channel_name)
				self.send(socket, error_message)
			# add socket to channel list.
			else:
				# If socket is in a channel already, remove it. 
				if socket in SOCKET_TO_CHANNELS:
					self.remove_from_old_channel(socket)
				# add socket to correct dictionaries
				CHANNELS_TO_SOCKETS[channel_name] = [socket]
				SOCKET_TO_CHANNELS[socket] = channel_name
		elif (split_msg[0] == "/join"):
			# if they don't specify which channel they want to join.
			if len(split_msg) == 1:
				error_message = utils.SERVER_JOIN_REQUIRES_ARGUMENT
				self.send(socket, error_message)
			# IF there is no channel, send error message. 
			elif channel_name not in CHANNELS_TO_SOCKETS:
				error_message = utils.SERVER_NO_CHANNEL_EXISTS.format(channel_name)
				self.send(socket, error_message)
			# Map socket --> channel name and channel name --> socket. Then broadcast that they joined.
			else:
				# If socket is in a channel already, remove it. 
				if socket in SOCKET_TO_CHANNELS:
					self.remove_from_old_channel(socket)
				CHANNELS_TO_SOCKETS[channel_name].append(socket)
				SOCKET_TO_CHANNELS[socket] = channel_name
				name = SOCKET_TO_NAME[socket]
				message = utils.SERVER_CLIENT_JOINED_CHANNEL.format(name)
				self.broadcast(socket, message, channel_name, True)
		# Sends back a list of items separated by new lines. 
		elif (split_msg[0] == "/list"):
			if len(split_msg) != 1:
				error_message = utils.SERVER_INVALID_CONTROL_MESSAGE.format(msg)
				self.send(socket, error_message)
			message = ""
			count = 0
			length = len(CHANNELS_TO_SOCKETS.keys())
			for channel in CHANNELS_TO_SOCKETS.keys():
				message = message + channel
				if count < length - 1:
					message += "\n"
				count += 1
			self.send(socket, message)
		else:
			error_message = utils.SERVER_INVALID_CONTROL_MESSAGE.format(msg)
			self.send(socket, error_message)

	def remove_from_old_channel(self, socket):
		# If socket is in a channel already, remove it. 
		if socket in SOCKET_TO_CHANNELS:
			old_channel_name = SOCKET_TO_CHANNELS[socket]
			# Remember that its a list of sockets
			CHANNELS_TO_SOCKETS[old_channel_name].remove(socket)
			del SOCKET_TO_CHANNELS[socket]
			# Broadcast to old channel that you left. 
			name = SOCKET_TO_NAME[socket]
			leaving_message = utils.SERVER_CLIENT_LEFT_CHANNEL.format(name)
			self.broadcast(socket, leaving_message, old_channel_name, True)
		

	# Send a socket a message
	def send(self, socket, message):
		try:
			char_sent = 0
			padded_message = pad_message(message)
			while char_sent < utils.MESSAGE_LENGTH:
				sent = socket.send(padded_message)
				char_sent = char_sent + sent
		except:
			socket.close()
			sys.exit()
			if socket in SOCKET_LIST:
				SOCKET_LIST.remove(socket)

	# So we basically broadcast to client socks that isn't the client sending the message. If control info, take out the name. 
	def broadcast(self, this_socket, message, channel_name, control=False):
		for socket in CHANNELS_TO_SOCKETS[channel_name]:
			if self.socket != socket and socket != this_socket:
					name = SOCKET_TO_NAME[this_socket]
					if control:
						self.send(socket, message)
					else:
						message = "[" + name + "] " + message
						self.send(socket, message)


if len(args) != 2:
	print "Please supply a port number."
	sys.exit()
server = Server(args[1])
server.run()
	