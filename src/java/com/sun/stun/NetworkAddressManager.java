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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Enumeration;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.stun.StunClient;
import com.sun.stun.StunServer;

public class NetworkAddressManager {

    private static final Logger logger =
        Logger.getLogger(NetworkAddressManager.class.getName());

    private static InetAddress privateLocalHost;

    private static String stunServer;
    private static int stunServerPort = StunServer.STUN_SERVER_PORT;

    private static int timeout = 100;

    static {
        stunServer = System.getProperty("com.sun.mc.stun.STUN_SERVER");
    
        String s = System.getProperty("com.sun.mc.stun.STUN_SERVER_PORT");

	if (s != null) {
	    try {
		stunServerPort = Integer.parseInt(s);
	    } catch (NumberFormatException e) {
		logger.info("Invalid STUN server port " + s
		    + ".  Defaulting to " + stunServerPort);
	    }
	}

	try {
	    getLocalHost();
	} catch (IOException e) {
	    logger.info("Unable to initialize localHost: "
		+ e.getMessage());
	}

	s = System.getProperty(
	    "com.sun.mc.stun.NETWORK_INTERFACE_TIMEOUT", "100");

	try {
	    timeout = Integer.parseInt(s);
	} catch (NumberFormatException e) {
	    logger.info("Invalid timeout value for isReachable(): " + s);
	}
    }

    public NetworkAddressManager(String stunServer) throws IOException {
	this(stunServer, stunServerPort);
    }

    public NetworkAddressManager(String stunServer, int stunServerPort) 
	    throws IOException {

	try {
	    stunServer = InetAddress.getByName(stunServer).getHostAddress();
	} catch (UnknownHostException e) {
	    logger.info("Invalid stunServer:  " + e.getMessage());
	    throw new IOException("Invalid stunServer:  " + e.getMessage());
	}

	stunServerPort = stunServerPort;
    }

    public static void setLogLevel(Level newLevel) {
	logger.setLevel(newLevel);
    }

    public static void getLocalHost() throws IOException {
	/*
	 * If there's a preferred address, use it.
	 */
	try {
	    privateLocalHost = getLocalHostFromPreferredAddress();
	    logger.info("Using preferred address " 
		+ privateLocalHost.getHostAddress());
	    showDefaultAddress(true);
	    return;
	} catch (IOException e) {
	}

	/*
	 * Try connecting to the STUN server to get our local address
	 */
	try {
	    privateLocalHost = getLocalHostFromStun();
	    logger.info("Using local address " 
		+ privateLocalHost.getHostAddress()
	        + " as determined by connecting to " + stunServer
		+ ":" + stunServerPort);

	    showDefaultAddress(false);
	    return;
	} catch (IOException e) {
	}

	try {
	    privateLocalHost = getLocalHostFromInterfaces();

	    logger.info("Using local address " 
		+ privateLocalHost.getHostAddress()
		+ " selected from the list of interfaces");
	} catch (IOException e) {
	    logger.info(e.getMessage());
	}
    }
	
    /*
     * Show the default address which would have been used if there
     * wasn't a preferred address.
     */
    public static void showDefaultAddress(boolean useStun) {
	InetAddress defaultAddress = null;

	if (useStun) {
	    try {
	        defaultAddress = getLocalHostFromStun();

	        logger.info("If localHost had not been specified, "
		    + defaultAddress.getHostAddress() 
		    + " would have been chosen by using STUN.");
	    } catch (IOException e) {
	    }
	}

	if (defaultAddress == null) {
	    try {
	    	defaultAddress = getLocalHostFromInterfaces();

		logger.info("If localHost had not been specified "
		    + "and could not be determined by using STUN, "
		    + defaultAddress.getHostAddress() 
		    + " would have been chosen from the interface list.");
	    } catch (IOException e) {
		logger.info("If localHost had not been specified "
		    + " it would not have been able to determine local host!");
	    }
	}
    }

    public static InetAddress getLocalHostFromPreferredAddress() 
	    throws IOException {

	String preferredAddress = System.getProperty(
	    "com.sun.mc.stun.LOCAL_IP_ADDRESS");

	if (preferredAddress == null || preferredAddress.length() == 0) {
	    throw new IOException("No preferred local address");
	}

	logger.fine("Trying preferred local address " + preferredAddress);

	try {
	    InetAddress address = InetAddress.getByName(preferredAddress);

	    logger.info("Using specified local address " + address);

	    return address;
	} catch (UnknownHostException e) {
	    String s = "Unknown local address "
		+ preferredAddress + " " + e.getMessage();

	    logger.info(s);
	    throw new IOException(s);
	}
    }

    /*
     * Try to connect to the STUN Server to get our private local address
     */
    public static InetAddress getLocalHostFromStun() throws IOException {
	if (stunServer == null) {
	    throw new IOException("No Stun Server specified");
	}

	Socket socket = new Socket();

	InetSocketAddress isa = new InetSocketAddress(
	    stunServer, stunServerPort);

	socket.connect(isa, 10000);

	InetAddress address = socket.getLocalAddress();

	socket.close();
	return address;
    }

    public static InetAddress getLocalHostFromInterfaces() throws IOException {
	InetAddress possibleAddress = null;

	/*
	 * Look for addresses at each interface and pick one that's usable.
	 */
        try {
            Enumeration localIfaces = NetworkInterface.getNetworkInterfaces();

	    while (localIfaces.hasMoreElements()) {
                NetworkInterface iFace = (NetworkInterface) 
		    localIfaces.nextElement();

                Enumeration addresses = iFace.getInetAddresses();

		logger.fine("Interface name: " + iFace.getName());

		InetAddress address;

                while (addresses.hasMoreElements()) {
                    address = (InetAddress) addresses.nextElement();

		    logger.fine("Address: " + address);

        	    if (address instanceof Inet4Address == false) {
			logger.fine("Skipping non-IPV4 address " + address);
			continue;
		    }

		    if (address.isAnyLocalAddress() || 
                            isWindowsAutoConfiguredIPv4Address(address) ||
                            address.toString().substring(0,3).equals("/0.")) {

                        logger.fine("Skipping " + address);
                        continue;
                    }

                    if (address.isLinkLocalAddress()) {
                        logger.fine("Found Linklocal ipv4 address " + address);
			return address;
                    } 

		    if (possibleAddress == null && 
			    address.isLoopbackAddress() == false &&
                	    address.toString().substring(0,3).equals("/0.") == false) {

			logger.fine("Setting possible address to " + possibleAddress);
			possibleAddress = address;
		    }

		    if (iFace.getName().startsWith("cipsec") && 
			    isReachable(address)) {

			logger.fine("Using cipsec " + address);
			return address;
		    }

		    if (iFace.getName().startsWith("ip.tun") &&
                    	    isReachable(address)) {

			logger.fine("Using ip.tun " + address);
                        return address;
                    }
		}
	    }

	    if (possibleAddress != null) {
		return possibleAddress;
	    }

	    /*
	     * We didn't find anything we liked so try the default.
	     */
            InetAddress address = InetAddress.getLocalHost();

	    if (address.toString().substring(0,3).equals("/0.")) {
		String s = "Local address " + address + " is not usable!";

		logger.fine(s);
		throw new IOException(s);
	    }

	    logger.fine("private local host is " + address);
	    return address;
        } catch (Exception e) {
	    throw new IOException("Failed to get local host! " 
		+ e.getMessage());
        }
    }
   
    /*
     * Ask stunServer to resolve socket.getAddress().
     */
    public static InetSocketAddress getPublicAddressFor(
	    InetSocketAddress stunServer, DatagramSocket socket) 
	    throws IOException {

	StunClient stunClient = new StunClient(stunServer, socket);

	return stunClient.getMappedAddress();
    }
	    
    /* 
     * Ask stunServer to resolve address
     */
    public static InetAddress getPublicAddressFor(
	    InetAddress address) throws IOException {

	if (stunServer != null) {
	    Socket socket = new Socket();

	    InetSocketAddress isa = new InetSocketAddress(
		stunServer, stunServerPort);

	    socket.connect(isa, 10000);

	    StunClient stunClient = new StunClient(socket);

	    InetAddress ia = stunClient.getMappedAddress().getAddress();

	    socket.close();
            
	    return ia;
	}

	return address;
    }

    public static InetAddress getPrivateLocalHost() throws IOException {
        if (privateLocalHost == null) {
	    throw new IOException("Unable to determine localHost!");
	}

	return privateLocalHost;
    }

    public static InetAddress getPublicLocalHost() throws IOException {
        return getPublicAddressFor(privateLocalHost);
    }


    private static boolean isReachable(InetAddress address) {
	try {
            if (address.isReachable(timeout) == false) {
                return false;
            }
	} catch (IOException e) {
	    logger.info("can't reach " + address + " " + e.getMessage());
	    return false;
	}

        return true;
    }   

    private static boolean isLinkLocalIPv4Address(InetAddress addr) {
        byte address[] = addr.getAddress();

        if ((address[0] & 0xff) == 10) {
            return true;
	}

        if ((address[0] & 0xff) == 172
            && (address[1] & 0xff) >= 16 && address[1] <= 31) {
            return true;
	}

        if ((address[0] & 0xff) == 192
            && (address[1] & 0xff) == 168) {
            return true;
	}

        return false;
    }

    public static boolean isWindowsAutoConfiguredIPv4Address(InetAddress addr) {
        return (addr.getAddress()[0] & 0xff) == 169
            && (addr.getAddress()[1] & 0xff) == 254;
    }

    public static void main(String[] args) {
	if (args.length != 4) {
	    System.out.println("Usage:  java com.sun.stun.NetworkAddressManager <stun server> "
		+ "<stun port> <private address> <private port>");
	    System.exit(1);
	}

	NetworkAddressManager.setLogLevel(Level.FINEST);

	int stunPort = Integer.parseInt(args[1]);

	InetSocketAddress isa = new InetSocketAddress(args[0], stunPort);

	System.out.println("stun server " + isa);

	InetAddress ia = null;

	try {
	    ia = InetAddress.getByName(args[2]);
	} catch (UnknownHostException e) {
	    System.out.println(e.getMessage());
	    System.exit(1);
	}

	int privatePort = Integer.parseInt(args[3]);

	DatagramSocket socket = null;

	try {
	    socket = new DatagramSocket(privatePort, ia);
	} catch (SocketException e) {
	    System.out.println(e.getMessage());
	    System.exit(1);
	}

	try {
	    System.out.println("public address " 
	        + NetworkAddressManager.getPublicAddressFor(isa, socket));
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	    System.exit(1);
	}
    }

}
