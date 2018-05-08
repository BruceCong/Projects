"""
Tests that a router, A, trusts another, B, telling it that the cost to a destination
through B has increased, if A has that route as its best.

Creates a topology like the following:

h1 -- s1 -- c1 -- s2 -- h2
        \              /
         -- c2 --------

Sends packet from h1 to h2, where c1 should be triggered.
Increases cost of s1-c1-s2-h2 so that s1-c2-h2 should be cheaper.
"""

import sim
import sim.api as api
import sim.basics as basics

from tests.test_simple import GetPacketHost, NoPacketHost
from tests.test_link_weights import CountingHub

def launch():
    h1 = NoPacketHost.create('h1')
    h2 = GetPacketHost.create('h2')
    s1 = sim.config.default_switch_type.create('s1')
    s2 = sim.config.default_switch_type.create('s2')
    c1 = CountingHub.create('c1')
    c2 = CountingHub.create('c2')

    h1.linkTo(s1, latency=1)
    s1.linkTo(c1, latency=1)
    c1.linkTo(s2, latency=1)
    s2.linkTo(h2, latency=1)
    s1.linkTo(c2, latency=3) # should not take
    c2.linkTo(h2, latency=1)

    def test_tasklet():
        yield 10 # need to wait for routing tables

        api.userlog.debug('Sending ping from h1 to h2')
        h1.ping(h2)

        yield 5 # need to wait for packet to get past c1

        if c1.pings == 1 and c2.pings == 0:
            api.userlog.debug('The ping took the right path')
            good = True
        else:
            api.userlog.error('Wrong initial path!')
            good = False

        api.userlog.debug('Increasing s2-h2 latency')
        s2.unlinkTo(h2)
        s2.linkTo(h2, latency=4)

        yield 5 # wait for update to propagate back to routers

        api.userlog.debug('Sending ping from h1 to h2')
        h1.ping(h2)

        yield 5 # wait for packet to pass c2 and get to h2

        if c1.pings == 1 and c2.pings == 1:
        	api.userlog.debug('Good path!')
        	good = True and good
        else:
        	api.userlog.error('You wrong')
        	good = False

        import sys
        sys.exit(0 if good else 1)

    api.run_tasklet(test_tasklet)