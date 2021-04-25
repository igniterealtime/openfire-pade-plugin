/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * This file is part of jVoiceBridge.
 *
 * jVoiceBridge is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License version 2 as 
 * published by the Free Software Foundation and distributed hereunder 
 * to you.
 *
 * jVoiceBridge is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied this 
 * code. 
 */

package com.sun.stun;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.nio.channels.DatagramChannel;
import java.nio.ByteBuffer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StunClient extends Thread {

    private static final Logger logger =
        Logger.getLogger(StunClient.class.getName());

    private static final int TIMEOUT = 3000;  // 3 seconds
    private static final int RETRIES = 5;

    private static int timeout = TIMEOUT;  
    private static int retries = RETRIES;

    private InetSocketAddress stunServer;
    private DatagramSocket datagramSocket;

    private Socket socket;
    private DataInputStream input;

    private InetSocketAddress mappedAddress;

    private boolean done;

    /*
     * Communicate with the stunServer using UDP
     */
    public StunClient(InetSocketAddress stunServer,
	    DatagramSocket datagramSocket) throws IOException {

	String s = System.getProperty("com.sun.stun.CLIENT_TIMEOUT");

	if (s != null && s.length() > 0) {
	    try {
		timeout = Integer.parseInt(s);
	    } catch (NumberFormatException e) {
		System.out.println("Invalid com.sun.stun.CLIENT_TIMEOUT: "
		    + s + ".  Defaulting to " + TIMEOUT);
	    }
	}

	s = System.getProperty("com.sun.stun.CLIENT_RETRIES");

	if (s != null && s.length() > 0) {
	    try {
		retries = Integer.parseInt(s);
	    } catch (NumberFormatException e) {
		System.out.println("Invalid com.sun.stun.CLIENT_RETRIES: "
		    + s + ".  Defaulting to " + RETRIES);
	    }
	}

	this.stunServer = stunServer;
	this.datagramSocket = datagramSocket;

	logger.fine("starting stun client to " + stunServer);
	start();
    }

    /*
     * Connect to the stunServer using TCP
     */
    public StunClient(Socket socket) throws IOException {
	this.socket = socket;

	stunServer = new InetSocketAddress(
	    socket.getInetAddress(), socket.getPort());

	input = new DataInputStream(socket.getInputStream());

	start();
    }

    public static void setLogLevel(Level newLevel) {
        logger.setLevel(newLevel);
    }

    /*
     * Return the mapped public address
     */
    public InetSocketAddress getMappedAddress() throws IOException {
	synchronized (this) {
	    while (!done) {
		try {
		    wait();
		} catch (InterruptedException e) {
		    throw new IOException(
			"Failed to retrieve mapped address:  Interruped");
		}
	    }
	}

	if (mappedAddress == null) {
	    String s = "Failed to retrieve mapped address";
            if (socket != null) {
                s += " for " +  socket.getLocalAddress() + ":" 
		     + socket.getLocalPort();
            } else if (datagramSocket != null) {
                s += " for " + datagramSocket.getLocalAddress() + ":"
                     + datagramSocket.getLocalPort();
            } 
            
            logger.warning(s);

	    logger.warning("IF YOU ARE BEHIND A FIREWALL OR NAT, "
	        + "ADDRESSES ARE NOT LIKELY TO BE CORRECT!");

	    throw new IOException(s);
	}

	logger.fine("mapped address is " + mappedAddress);
	return mappedAddress;
    }

    private void done() {
	synchronized (this) {
	    done = true;
	    notifyAll();
	}
    }
	
    public void run() {
        int socketTimeout;

	logger.info("using STUN server " + stunServer);

	try {
	    if (datagramSocket != null) {
	        socketTimeout = datagramSocket.getSoTimeout();
	        datagramSocket.setSoTimeout(timeout);
	    } else {
		socketTimeout = socket.getSoTimeout();
	        socket.setSoTimeout(timeout);
	    }
	} catch (SocketException e) {
	    logger.warning("Unable to set socket timeout:  " + e.getMessage());
	    done();
	    return;
	}

	for (int i = 0; i < retries; i++) {
	    try {
		logger.fine("Sending stun request " + i);
	        sendStunRequest();

	        waitForReply();
		break;
	    } catch (IOException e) {
	    }
	}

	try {
	    if (datagramSocket != null) {
	        datagramSocket.setSoTimeout(socketTimeout);
	    } else {
	        socket.setSoTimeout(socketTimeout);
	    }
	} catch (SocketException e) {
	    logger.warning("Unable to reset socket timeout:  " 
		+ e.getMessage());
	}

	done();
    }

    private void sendStunRequest() throws IOException {
	InetAddress addressToMap;
	int port;

	mappedAddress = null;

	if (datagramSocket != null) {
   	    addressToMap = datagramSocket.getLocalAddress();
 	    port = datagramSocket.getLocalPort();
	} else {
   	    addressToMap = socket.getLocalAddress();
 	    port = socket.getLocalPort();
	}

	logger.fine("StunClient:  asking STUN server "
	    + stunServer.getAddress().getHostAddress() + ":" 
	    + stunServer.getPort()
	    + " to get mapping for " + addressToMap.getHostAddress()
	    + ":" + port);

	byte[] buf = new byte[StunHeader.STUN_HEADER_LENGTH
	    + StunHeader.TLV_LENGTH + StunHeader.MAPPED_ADDRESS_LENGTH];

	buf[1] = (byte) StunHeader.BINDING_REQUEST;
	buf[3] = (byte) StunHeader.TLV_LENGTH + 
	    StunHeader.MAPPED_ADDRESS_LENGTH;
	
	long time = System.currentTimeMillis();

	for (int i = 0; i < 16; i++) {
	    buf[i + 4] = (byte) (time >> ((i % 4) * 8));
	}

	buf[21] = StunHeader.MAPPED_ADDRESS;
	buf[23] = StunHeader.MAPPED_ADDRESS_LENGTH;

	buf[25] = 1;	// address family

	buf[26] = (byte) (port >> 8);
	buf[27] = (byte) (port & 0xff);

	byte[] address = addressToMap.getAddress();

	buf[28] = address[0];	// address to map
	buf[29] = address[1];
	buf[30] = address[2];
	buf[31] = address[3];

	if (datagramSocket != null) {
	    DatagramPacket packet = new DatagramPacket(buf, buf.length,
	        stunServer.getAddress(), stunServer.getPort());

   	    logger.fine("local addr " + datagramSocket.getLocalAddress()
 	        + " local port " + datagramSocket.getLocalPort());
	    datagramSocket.send(packet);
	} else {
	    DataOutputStream output =
                new DataOutputStream(socket.getOutputStream());

	    output.write(buf, 0, buf.length);
	    output.flush();
	}
    }

    private void waitForReply() throws IOException, SocketTimeoutException {
	byte[] response = new byte[1000];

	/*
	 * Since ports are multiplexed with STUN and data,
	 * it is possible for a us to send a BINDING_REQUEST
	 * and not immediately get a BINDING_RESPONSE but instead
	 * get data.  We will toss data for <retries> times,
	 * then return an error so the STUN request can be resent.
	 */
	for (int i = 0; i < retries; i++) {
	    int length;

	    if (datagramSocket != null) {
	        DatagramPacket packet = new DatagramPacket(
		    response, response.length);

	        datagramSocket.receive(packet);
	        length = packet.getLength();
	    } else {
	        length = input.read(response);
	    }

	    logger.fine("Got response!  " + length
   	        + " local addr " + datagramSocket.getLocalAddress()
 	        + " local port " + datagramSocket.getLocalPort());

	    int type = (int) 
		((response[0] << 8 & 0xff00) | (response[1] & 0xff));

	    if (type == StunHeader.BINDING_RESPONSE) {
	        mappedAddress = StunHeader.getAddress(response, 
	            StunHeader.MAPPED_ADDRESS);
		return;
	    }

	    logger.fine("BAD STUN response, length " + length 
		    + " TCP " + (input != null));
	}

	throw new IOException("Didn't receive BINDING_RESPONE");
    }

}
