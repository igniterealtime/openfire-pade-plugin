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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StunHeader {

    private static final Logger logger =
        Logger.getLogger(StunHeader.class.getName());

    public static final int STUN_HEADER_LENGTH = 20;

    public static final int TLV_LENGTH = 4;

    public static final int ERROR_CODE_LENGTH = 4;

    public static final int BINDING_REQUEST = 1;
    public static final int BINDING_RESPONSE = 0x101;

    public static final int MAPPED_ADDRESS = 1;
    public static final int MAPPED_ADDRESS_LENGTH = 8;

    public static final int RESPONSE_ADDRESS = 2;
    public static final int RESPONSE_ADDRESS_LENGTH = 8;

    public static final int CHANGE_REQUEST = 3;
    public static final int CHANGE_REQUEST_LENGTH = 4;
    public static final int CHANGE_PORT_MASK = 2;
    public static final int CHANGE_IP_MASK = 4;

    public static final int CHANGED_ADDRESS = 5;
    public static final int CHANGED_ADDRESS_LENGTH = 8;

    public static final int BAD_REQUEST = 400;
    public static final int GLOBAL_ERROR = 600;

    public static void setLogLevel(Level newLevel) {
        logger.setLevel(newLevel);
    }

    /*
     * Get the response address attribute if present.
     */
    public static InetSocketAddress getAddress(byte[] request, int desiredType) {
	InetSocketAddress isa = null;

	int length = (int) (((request[2] << 8) & 0xff00) | 
	    (request[3] & 0xff));

	int offset = STUN_HEADER_LENGTH;

	logger.finest("Searching for type " + Integer.toHexString(desiredType));

	while (length > 0) {
	    int type = (int) request[offset + 1];

	    int attributeLength = (int) (((request[offset + 2] << 8) & 0xff00) |
		(request[offset + 3] & 0xff));

	    if (type != desiredType) {
		logger.finest("Skipping type " + type);

	        offset += (TLV_LENGTH + attributeLength);
		length -= (TLV_LENGTH + attributeLength);
		continue;
	    }

	    if (attributeLength != MAPPED_ADDRESS_LENGTH) {
		logger.warning("Invalid Response Address Length "
		    + attributeLength);
		return null;
	    }

	    int port = (int) (((request[offset + 6] << 8) & 0xff00) |
		(request[offset + 7] & 0xff));

	    InetAddress ia;

	    try {
		byte[] address = new byte[4];

		address[0] = request[offset + 8];
		address[1] = request[offset + 9];
		address[2] = request[offset + 10];
		address[3] = request[offset + 11];

		ia = InetAddress.getByAddress(address);
	    } catch (UnknownHostException e) {
		logger.warning("Invalid Response Address:  " + e.getMessage());
		return null;
	    }

	    isa = new InetSocketAddress(ia, port);
	    logger.finest("Found Address " + isa);
	    break;
	}

	return isa;
    }

    /*
     * Get the change request mask if present
     */
    public static int getChangeRequest(byte[] request) {
	int changeRequest = 0;

	int length = (int) (((request[2] << 8) & 0xff00) | 
	    (request[3] & 0xff));

	int offset = STUN_HEADER_LENGTH;

	logger.finest("Searching for change request attribute");

	while (length > 0) {
	    int type = (int) request[offset + 1];

	    int attributeLength = (int) (((request[offset + 2] << 8) & 0xff00) |
		(request[offset + 3] & 0xff));

	    if (type != CHANGE_REQUEST) {
		logger.finest("Skipping type " + type);
	        offset += (TLV_LENGTH + attributeLength);
		length -= (TLV_LENGTH + attributeLength);
		continue;
	    }

	    if (attributeLength != CHANGE_REQUEST_LENGTH) {
		logger.warning("Invalid Change Request Length " +
		    attributeLength);
		return 0;
	    }

	    changeRequest = (int) request[offset + 7];
	    logger.finest("Found change request " + changeRequest);
	    break;
	}

	return changeRequest;
    }

    public static void dump(String msg, byte[] data, int offset,
            int length) {

        logger.info(msg);

        String s = "";

        String t = "";

        char[] v = new char[1];

        for (int i = 0; i < length; i++) {
            if ((i % 16) == 0) {
                if (i > 0) {
                    logger.info(s + "\t" + t);
                }

                s = Integer.toHexString(i + offset) + ":  ";

                t = "";
            }

            s += Integer.toHexString(data[i] & 0xff) + " ";

            v[0] = (char)(data[i + offset] & 0xff);

            if (v[0] < 0x20 || v[0] > 0x7e) {
                t += ".";
            } else {
                t += String.copyValueOf(v);
            }
        }

        logger.info(s + "\t" + t);
    }

}
