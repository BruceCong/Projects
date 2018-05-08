import sim.api as api
import sim.basics as basics

# We define infinity as a distance of 16.
INFINITY = 16


class DVRouter(basics.DVRouterBase):
    # NO_LOG = True # Set to True on an instance to disable its logging
    # POISON_MODE = True # Can override POISON_MODE here
    # DEFAULT_TIMER_INTERVAL = 5 # Can override this yourself for testing

    def __init__(self):
        """
        Called when the instance is initialized.

        You probably want to do some additional initialization here.

        """
        self.start_timer()  # Starts calling handle_timer() at correct rate
        self.neighbors = {}  # Keeps track of routes directly connected to this router. 
        self.neighbors_distance = {}   # Port --> latency.
        self.distance_vectors = {}  # Destination --> routing table


    def handle_link_up(self, port, latency):
        """
        Called by the framework when a link attached to this Entity goes up.

        The port attached to the link and the link latency are passed
        in.

        """
        self.neighbors_distance[port] = latency 
        # Send the new neighbor some love with a bunch of information it may need. 
        for dest in self.distance_vectors.keys():
            distance = self.distance_vectors[dest].latency
            routing_packet = basics.RoutePacket(dest, distance)
            self.send(routing_packet, port, False)
        

    def handle_link_down(self, port):
        """
        Called by the framework when a link attached to this Entity does down.

        The port number used by the link is passed in.

        """
        del self.neighbors_distance[port]
        # Go through and check and delete the destinations in your routing table that use the port given. 
        for dest in self.distance_vectors.keys():
            table = self.distance_vectors[dest]
            if table.port == port:
                if dest in self.neighbors.keys() and port != self.neighbors[dest].port:
                    backup_table = self.neighbors[dest]
                    table.update_table(backup_table.latency, backup_table.port, api.current_time())
                else:
                    if self.POISON_MODE:
                        table.poison_route(api.current_time())
                    else:
                        del self.distance_vectors[dest]

        

    def handle_rx(self, packet, port):
        """
        Called by the framework when this Entity receives a packet.

        packet is a Packet (or subclass).
        port is the port number it arrived on.

        You definitely want to fill this in.

        """
        # self.log("RX %s on %s (%s)", packet, port, api.current_time())
        # api.userlog.debug(self)
        if isinstance(packet, basics.RoutePacket):
            if self.neighbors_distance[port] + packet.latency < INFINITY:
                self.bellman_ford_update(packet, port)    # This calculates the distance, and updates the table if necessary. 
            # Telling you that it doesn't have that link anymore. 
            if packet.latency >= INFINITY or self.neighbors_distance[port] + packet.latency >= INFINITY:
                if packet.destination in self.distance_vectors:
                    table = self.distance_vectors[packet.destination]
                    # Check if we had this in our destination. 
                    if table.port == port:
                        if self.POISON_MODE:
                            table.poison_route(api.current_time())
                        else:
                            del self.distance_vectors[packet.destination]

        elif isinstance(packet, basics.HostDiscoveryPacket):
            # Get the destination, and get latency from finding the distance from the port. 
            destination = packet.src
            latency = self.neighbors_distance[port]

            # Update the routing table. Change the direct host to 
            self.distance_vectors[destination] = RoutingTable(latency, port, api.current_time())
            self.neighbors[destination] = RoutingTable(latency, port, api.current_time())
        else:
            # Forward the data packet.
            if packet.dst in self.distance_vectors:
                table = self.distance_vectors[packet.dst]
                if table.port != port and table.latency < INFINITY:
                    self.send(packet, table.port, False)





    def bellman_ford_update(self, packet, port):
        packet_distance = self.neighbors_distance[port] + packet.latency
        # If we don't have our own distance, then packet_distance is the shortest one!
        if packet.destination not in self.distance_vectors.keys():           
            self.distance_vectors[packet.destination] = RoutingTable(packet_distance, port, api.current_time())
        else:
            table = self.distance_vectors[packet.destination]
            our_distance = table.latency


            # If packet distance is different despite using that port, that means something has changed on their end, so have to update the distance. 
            if (port == table.port):
                if packet.destination in self.neighbors:
                    direct_table = self.neighbors[packet.destination]
                    if packet_distance > direct_table.latency:
                        table.update_table(direct_table.latency, direct_table.port, api.current_time())
                else:
                    table.update_table(packet_distance, table.port, api.current_time())

            #If our distance is greater than through this packet, then we update our distance and change the port we go through.
            elif our_distance >= packet_distance:
                table.update_table(packet_distance, port, api.current_time())

            

   




    def handle_timer(self):
        """
        Called periodically.

        When called, your router should send tables to neighbors.  It
        also might not be a bad place to check for whether any entries
        have expired.

        """
        for dest in self.distance_vectors.keys():
            table = self.distance_vectors[dest]
            if (api.current_time() - table.time <= self.ROUTE_TIMEOUT) or (dest in self.neighbors.keys()):
                routing_packet = basics.RoutePacket(dest, table.latency)
                self.send(routing_packet, table.port, True)
                if self.POISON_MODE:
                    poison_packet = basics.RoutePacket(dest, INFINITY)
                    self.send(poison_packet, table.port, False)
            else:   
                del self.distance_vectors[dest]



class RoutingTable(object):

    def __init__(self, latency, port, time):
        self.latency = latency
        self.port = port
        self.time = time

    def update_table(self, latency, port, time):
        self.latency = latency
        self.port = port 
        self.time = time

    def poison_route(self, time):
        self.latency = INFINITY
        self.time = time

    def log(self):
        return [self.latency, self.port, self.time]
 
  

