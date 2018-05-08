"""
A client that sends messages to a server that are split into messages
smaller than the default message length.  This client can be used to
test that the server correctly buffers when it receives partial messages.

This client assumes that the server has an existing channel called
split_messages.  To use this client, you should connect another client
to the server, and have that client create (and join) a split_messages
channel.  Then, you should ensure that the other client sees this output
10 times:

[SplitMessagesChatClient] I think that I shall never see a structure more wasteful than a tree. Most links remain idle and unused while others are overloaded and abused. And with each failure comes disruption caused by...

You may want to modify this client to test a broader range of scenarios
with split messages.  This client randomly determines how to split up
a message, but you may want to hard-code the way messages are divided to
ensure that you've tested all relevant cases.
"""
import utils

import random
import socket
import sys
import time

def pad_message(message):
  while len(message) < utils.MESSAGE_LENGTH:
    message += " "
  return message[:utils.MESSAGE_LENGTH]

class ChatClientSplitMessages:
  def __init__(self, name, server_host, server_port):
    self.server_host = server_host
    self.server_port = server_port
    self.server_name = name
    self.socket = socket.socket()
    try:
      self.socket.connect((self.server_host, self.server_port))
    except:
      print utils.CLIENT_CANNOT_CONNECT.format(self.server_host, self.server_port)
      sys.exit()
    self.send_split_message(self.socket, name)

  def send_split_message(self, client_socket, message):
    chars_sent = 0
    padded_message = pad_message(message)
    # Send random number of characters in the message.
    while chars_sent < utils.MESSAGE_LENGTH:
      last_char_to_send = random.randrange(chars_sent, utils.MESSAGE_LENGTH + 1)
      message_to_send = padded_message[chars_sent:last_char_to_send]
      if len(message_to_send) > 0:
        print "Sending {} characters: {}".format(
          last_char_to_send - chars_sent, message_to_send)
        client_socket.sendall(message_to_send)
        chars_sent = last_char_to_send

  def send_interleaving(self, client_socket, message, number):
    chars_sent = 0
    last_char_to_send = number
    padded_message = pad_message(message)
    while chars_sent < utils.MESSAGE_LENGTH:
      last_char_to_send += chars_sent
      message_to_send = padded_message[chars_sent:last_char_to_send]
      if len(message_to_send) > 0:
        print "Sending {} characters: {}".format(last_char_to_send - chars_sent, message_to_send)
        self.socket.sendall(message_to_send)
        time.sleep(1)
        is_200 = 0
        # while is_200 < utils.MESSAGE_LENGTH:
        #   sent = client_socket.socket.send(pad_message("I am Bin"))
        #   is_200 += sent
        client_socket.socket.sendall(pad_message("I am Bin"))
        chars_sent += last_char_to_send
        time.sleep(1)
  
  def send_multiple_messages(self, message, message2):
    padded_message = pad_message(message)
    padded_message2 = pad_message(message2)
    self.socket.sendall(padded_message[:150])
    time.sleep(1)
    self.socket.sendall(padded_message[150:utils.MESSAGE_LENGTH] + padded_message[:2])
    time.sleep(1)
    self.socket.sendall(padded_message2[2:utils.MESSAGE_LENGTH])
    time.sleep(1)

  def run(self):
    client_socket = socket.socket()
    try:
      client_socket.connect((self.server_host, self.server_port))
    except:
      print utils.CLIENT_CANNOT_CONNECT.format(self.server_host, self.server_port)
      return

    my_name = "SplitMessagesChatClient"
    # Send the server our name.
    self.send_split_message(client_socket, my_name)

    # Join a "split_messages" channel. This code assumes that the channel
    # already exists.
    self.send_split_message(client_socket, "/join split_messages")

    # Send the same message 10 times.
    message = ("I think that I shall never see a structure more wasteful " +
      "than a tree. Most links remain idle and unused while others are " +
      "overloaded and abused.")
    for i in range(10):
      self.send_split_message(client_socket, message)
      time.sleep(1)

if __name__ == "__main__":
  if (len(sys.argv)) < 3:
    print "Usage: python client_split_messages.py server_hostname server_port"
    sys.exit(1)

  chat_client1 = ChatClientSplitMessages("Bruce", sys.argv[1], int(sys.argv[2]))
  chat_client2 = ChatClientSplitMessages("Bin", sys.argv[1], int(sys.argv[2]))
  chat_client1.send_split_message(chat_client1.socket, "/join split_messages")
  chat_client2.send_split_message(chat_client2.socket, "/join split_messages")
  chat_client1.send_interleaving(chat_client2, "hello", 100)
  time.sleep(1)
  chat_client1.send_multiple_messages("Hi, my name is Bruce Cong and I like Coke!", "Hi, my name is Bin Cong and I am a CS professor!")
  time.sleep(1)
  chat_client2.socket.shutdown(1)
  chat_client2.socket.close()
  chat_client1.socket.shutdown(1)
  chat_client1.socket.close()
  sys.exit()




  # sys.exit(chat_client.run())
x