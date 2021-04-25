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

package com.sun.voip.server;

import com.sun.voip.CallParticipant;
import com.sun.voip.CallState;
import com.sun.voip.Logger;
import com.sun.voip.MediaInfo;
import com.sun.voip.AudioConversion;

import java.io.*;
import java.net.*;

public class MulticastCallAgent extends CallSetupAgent
{
    private CallParticipant cp;
    private MemberReceiver memberReceiver;
    private MemberSender memberSender;
    private MediaInfo mixerMediaPreference;
	private short counter2 = 0;
	private MulticastSocket multicastSocket = null;
	private InetAddress group;
    private boolean started = false;

    public MulticastCallAgent(CallHandler callHandler)
    {
		super(callHandler);

		cp = callHandler.getCallParticipant();

		mixerMediaPreference = callHandler.getConferenceManager().getMediaInfo();

		memberSender 	= callHandler.getMemberSender();
		memberReceiver 	= callHandler.getMemberReceiver();

		callHandler.setEndpointAddress(null, (byte)0, (byte)0, (byte)0);

		if (cp.getPhoneNumber() != null)
		{
			try {
				String[] tokens = cp.getPhoneNumber().split(":");

				if (tokens.length == 3 && !started)
				{
					int port = Integer.parseInt(tokens[2]);
					group = InetAddress.getByName(tokens[1]);

					multicastSocket = new MulticastSocket(port);
					multicastSocket.joinGroup(group);

					Thread thread = new Thread("MulticastCallAgent Thread")
					{
						@Override public void run()
						{
							byte[] buffer = new byte[10*1024];
							DatagramPacket data = new DatagramPacket(buffer, buffer.length);

							while (multicastSocket != null)
							{
								try {
									multicastSocket.receive(data);
									//DatagramPacket dgram = new DatagramPacket(data.getData(), data.getLength(), InetAddress.getByName(ip2), port2);

									onPacket(data);
									data.setLength(buffer.length);

								} catch (Exception e) {
									Logger.println("MulticastCallAgent: Thread exception " + e);
									e.printStackTrace();
								}
							}
						}
					};
					thread.setDaemon(true);
					thread.start();
					started = true;

				} else Logger.println("MulticastCallAgent bad multicast uri " + cp.getPhoneNumber());

			} catch (Exception e) {

				Logger.println("MulticastCallAgent exception " + e);
				e.printStackTrace();
			}

		} else Logger.println("MemberSender MulticastCallAgent uri is NULL ");

	}

	public void initiateCall() throws IOException
	{
		try {
			setState(CallState.ESTABLISHED);

		} catch (Exception e) {

			Logger.println("MulticastCallAgent: initiateCall exception " + e);
			e.printStackTrace();
		}
	}

	public String getSdp()
	{
		return null;
    }

    public void setRemoteMediaInfo(String sdp)
    {
		return;
    }

    public void terminateCall()
    {
		if (multicastSocket != null)
		{
			try {
				multicastSocket.leaveGroup(group);
				multicastSocket.close();
				multicastSocket = null;

			} catch (Exception e) {
				Logger.println("MulticastCallAgent: terminateCall exception " + e);
				e.printStackTrace();
			}
		}
    }

	private void onPacket(DatagramPacket data)
	{
		byte[] audio = data.getData();

 		memberSender.putSSRC(audio);

		if (memberReceiver != null)
		{
			if (counter2 < 20) Logger.println(" MulticastCallAgent onPacket " + data.getLength() + " " + counter2);

			int[] l16Buffer = new int[data.getLength()];
			AudioConversion.ulawToLinear(audio, 0, data.getLength(), l16Buffer);

			l16Buffer = MemberSender.normalize(l16Buffer);
			memberReceiver.handleWebRtcMedia(l16Buffer, counter2++);
		}
	}
}
