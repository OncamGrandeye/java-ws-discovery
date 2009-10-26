/*
SOAPOverUDP.java

Copyright (C) 2008-2009 Magnus Skjegstad

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.skjegstad.soapoverudp;

import com.skjegstad.soapoverudp.threads.SOAPReceiverThread;
import com.skjegstad.soapoverudp.threads.SOAPSenderThread;
import com.skjegstad.soapoverudp.messages.SOAPNetworkMessage;
import com.skjegstad.soapoverudp.interfaces.ISOAPTransport;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.skjegstad.soapoverudp.interfaces.INetworkMessage;
import com.skjegstad.soapoverudp.exceptions.SOAPOverUDPException;
import java.net.NetworkInterface;
import java.util.logging.Logger;

/**
 * Implementation of SOAP-over-UDP for WS-Discovery as specified in 
 * http://schemas.xmlsoap.org/ws/2004/09/soap-over-udp/.
 * 
 * @author Magnus Skjegstad
 */
public class SOAPOverUDP implements ISOAPTransport {
    /**
     * Instance of Logger used for debug messages.
     */
    protected Logger logger;
    
    // Threads and stuff
    private SOAPReceiverThread multicastReceiverThread; // Thread listening for incoming multicast messages
    private SOAPReceiverThread unicastReceiverThread; // Thread listening for incoming unicast messages
    private SOAPSenderThread multicastSenderThread; // Thread sending multicast messages
    private SOAPSenderThread unicastSenderThread; // Thread sending unicast messages
    private LinkedBlockingQueue<INetworkMessage> inQueue = new LinkedBlockingQueue<INetworkMessage>(); // Queue used by the receiver threads
    private DelayQueue<SOAPNetworkMessage> outUnicastQueue = new DelayQueue<SOAPNetworkMessage>(); // Queue used by unicastSenderThread
    private DelayQueue<SOAPNetworkMessage> outMulticastQueue = new DelayQueue<SOAPNetworkMessage>(); // Queue used by multicastSenderThread
    private int multicastPort;
    private InetAddress multicastAddress;
    private int unicastPort;
    
    // Default vaules for retry and back-off algorithm (see Appendix I in the SOAP-over-UDP draft)
    /**
     * Number of times to repeat unicast messages.
     */
    public static final int UNICAST_UDP_REPEAT = 2;
    /**
     * Number of times to repeat multicast messages.
     */
    public static final int MULTICAST_UDP_REPEAT = 4;
    /**
     * Minimum initial delay for resend.
     */
    public static final int UDP_MIN_DELAY = 50;
    /**
     * Maximum initial delay for resend.
     */
    public static final int UDP_MAX_DELAY = 250;
    /**
     * Maximum delay between resent messages.
     */
    public static final int UDP_UPPER_DELAY = 500;

    /**
     * @param multicastPort Port for sending and receiving multicast messages
     * @param multicastAddress Address for sending and listening to multicast messages.
     * @param logger Instance of Logger used for debugging. May be set to null.
     * @throws SOAPOverUDPException if an error occured while opening
     * the sockets or creating child threads.
     */
    public SOAPOverUDP(int multicastPort, InetAddress multicastAddress, Logger logger)
            throws SOAPOverUDPException {
        init(null, multicastPort, multicastAddress, logger);
    }

    /**
     * @param multicastInterface Network interface to use for multicasting. Set to null to use default.
     * @param multicastPort Port for sending and receiving multicast messages
     * @param multicastAddress Address for sending and listening to multicast messages.
     * @param logger Instance of Logger used for debugging. May be set to null.
     * @throws SOAPOverUDPException if an error occured while opening
     * the sockets or creating child threads.
     */
    public SOAPOverUDP(NetworkInterface multicastInterface, int multicastPort, InetAddress multicastAddress, Logger logger)
            throws SOAPOverUDPException {
        init(multicastInterface, multicastPort, multicastAddress, logger);
    }

    /**
     * Empty constructor for use with newInstance(). Call init() to initialize the
     * new instance.
     */
    public SOAPOverUDP() {

    }
            
    @Override
    public void finalize() throws Throwable {
        try {            
            this.done();
        } finally {
            super.finalize();
        }
    }
    
    /**
     * Put SOAP-message in send queue. 
     * 
     * @param message SOAP message.
     * @param blockUntilSent When true the method will wait until the send-queue is empty. False returns immediately.
     * @throws java.lang.InterruptedException if interrupted while waiting for the message to be sent.
     */
    public void send(INetworkMessage message, boolean blockUntilSent) throws InterruptedException {
        // Multicast
        if (message.getDstAddress().equals(this.multicastAddress)) { 
            outMulticastQueue.add(new SOAPNetworkMessage(message, true));
            if (blockUntilSent)
                while (!outMulticastQueue.isEmpty())
                    synchronized (multicastSenderThread) {
                        multicastSenderThread.wait();
                    }
        // Unicast
        } else {
            outUnicastQueue.add(new SOAPNetworkMessage(message, false));
            if (blockUntilSent)
                while (!outUnicastQueue.isEmpty())
                    synchronized (unicastSenderThread) {
                        unicastSenderThread.wait();
                    }            
        }
    }
    /**
     * Put SOAP message in send-queue. Returns immediately.
     * 
     * @param message
     */
    public void send(INetworkMessage message) {
        try {
            send(message, false);
        } catch (InterruptedException ex) {
            // Will never be thrown, as blockUntilSent is false            
        }
    }
    
    /**
     * Receive a SOAP message.
     * 
     * @param timeoutInMillis Time to wait for a message.
     * @return SOAP message. <code>null</code> on timeout.
     * @throws java.lang.InterruptedException if interrupted while waiting for data.
     */
    public INetworkMessage recv(long timeoutInMillis) throws InterruptedException {
        return inQueue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Receive a SOAP message.
     * 
     * @return SOAP message. <code>null</code> if interrupted while waiting.
     */
    public INetworkMessage recv() {
        try {
            return inQueue.take();
        } catch (InterruptedException ex) {
            return null;
        }
    }

    /**
     * Start transport layer.
     */
    public void start() {
        // Start threads
        multicastReceiverThread.start();
        multicastSenderThread.start();
        unicastReceiverThread.start();
        unicastSenderThread.start();
        
        // Wait for threads to get into main loop
        while ((!multicastReceiverThread.isRunning()) ||
               (!multicastSenderThread.isRunning()) ||
               (!unicastReceiverThread.isRunning()) ||
               (!unicastSenderThread.isRunning()))
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                break;
            }
    }

    /**
     * Tell transport layer to stop. Returns immediately. Use 
     * {@link Thread#isAlive()} to determine when thread has ended.
     */
    public void done() {
        // Make sure out queues are empty
        while (!outUnicastQueue.isEmpty()) {
            synchronized (unicastSenderThread) {
                try {
                    unicastSenderThread.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        while (!outMulticastQueue.isEmpty()) {
            synchronized (multicastSenderThread) {
                try {
                    multicastSenderThread.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        
        // Stop child threads
        multicastReceiverThread.done();
        multicastSenderThread.done();
        unicastReceiverThread.done();
        unicastSenderThread.done();
        // Wait for childs to complete
        while (multicastReceiverThread.isRunning() ||
                multicastSenderThread.isRunning() ||
                unicastSenderThread.isRunning() ||
                unicastReceiverThread.isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                break;
            }
        }

    }

    /**
     * Returns the port we listen for multicasts on.
     * @return Multicast port.
     */
    public int getMulticastPort() {
        return multicastPort;
    }

    /**
     * Returns the port used for sending and receiving unicast packets.
     * @return Port used for unicasts.
     */
    public int getUnicastPort() {
        return unicastPort;
    }

    public void init(NetworkInterface multicastInterface, int multicastPort, InetAddress multicastAddress, Logger logger) throws SOAPOverUDPException {
        this.logger = logger;

        this.multicastPort = multicastPort;
        this.multicastAddress = multicastAddress;

        MulticastSocket multicastReceiveSocket = null;
        DatagramSocket mainSocket = null;

        try {
            multicastReceiveSocket = new MulticastSocket(null);
            if (multicastInterface != null)
                multicastReceiveSocket.setNetworkInterface(multicastInterface);
            multicastReceiveSocket.setReuseAddress(true); // Required by spec.
            if (!multicastReceiveSocket.getReuseAddress())
                throw new SOAPOverUDPException("Platform does not support SO_REUSEADDR");
            multicastReceiveSocket.setTimeToLive(1); // Suggested by spec
            multicastReceiveSocket.bind(new InetSocketAddress(multicastPort));
            if (multicastReceiveSocket.getLocalPort() != multicastPort)
                throw new SOAPOverUDPException("Unable to bind multicast socket to multicast port.");
            multicastReceiveSocket.joinGroup(multicastAddress);
        } catch (IOException ex) {
            throw new SOAPOverUDPException("Unable to open multicast socket.", ex);
        }

        try {
            mainSocket = new DatagramSocket();
            mainSocket.setReuseAddress(true);
            if (!mainSocket.getReuseAddress())
                throw new SOAPOverUDPException("Platform doesn't support SO_REUSEADDR");
            this.unicastPort = mainSocket.getLocalPort();
           // mainSocket.bind(new InetSocketAddress(multicastPort));
           // if (mainSocket.getLocalPort() != multicastPort)
           //     throw new WsDiscoveryTransportException("Unable to bind unicast socket to multicast port.");
        } catch (IOException ex) {
            throw new SOAPOverUDPException("Unable to open unicast socket.", ex);
        }

        try {
            multicastReceiverThread = new SOAPReceiverThread("multicast_recv", inQueue, multicastReceiveSocket, logger);
        } catch (SocketException ex) {
            throw new SOAPOverUDPException("Unable to start multicast receiver thread", ex);
        }

        unicastSenderThread = new SOAPSenderThread("unicast_send",
                    outUnicastQueue, mainSocket, logger);
        multicastSenderThread = new SOAPSenderThread("multicast_send",
                        outMulticastQueue, mainSocket, logger);
        try {
            unicastReceiverThread = new SOAPReceiverThread("unicast_recv", inQueue, multicastSenderThread.getSocket(), logger);
        } catch (SocketException ex) {
            throw new SOAPOverUDPException("Unable to start unicast receiver thread", ex);
        }
    }
    
}