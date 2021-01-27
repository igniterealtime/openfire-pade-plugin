Pàdé for Openfire
=========================
This project provides a web-based unified communication solution for Openfire.
- peer to peer based chat, 
- persistent groupchat, 
- audio and video conferencing,
- telephone access to conferences, 

It includes third-party products, notably:
- [Jitsi Videobridge](https://github.com/jitsi/jitsi-videobridge) project;
- [Jitsi Conference Focus (jicofo)](https://github.com/jitsi/jicofo) project; 
- [Jitsi Meet](https://github.com/jitsi/jitsi-meet) web client.
- [Jitsi SIP Gateway](https://github.com/jitsi/jigasi) project.
- [Pàdé](https://github.com/igniterealtime/pade) web desktop client based on the [ConverseJS](https://github.com/conversejs/converse.js) project.

Pade works with Firefox. It however works best with Chromium based apps like Chrome, Edge, Electron and Opera. 

Pade has minimal network requirements and works out of the box internally on a local area network (LAN) or with a hosted Openfire server on the internet. If your Openfire server is placed behind a NAT and firewall and you want to allow external internet access, then you require some network expertise to configure it. You would need to open a few UDP/TCP ports and provide both the public and private IP addresses of your openfire server.

Pade uses an XMPP user called **jvb** that will join a global conference called **ofmeet** with  the focus user called **focus**. If you enable the SIP gateway, a new user called **jigasi** will be created and it will join a global conference called **jigasi** with the focus user **focus**

![image](https://user-images.githubusercontent.com/110731/99916724-af0dc880-2d03-11eb-80c3-b35b9009910a.png)

Pade will not work out of the box if your Openfire server is configured to use LDAP. You would need to create the jvb, focus and jigasi bot users manually. Give the focus bot user owner/admin permissions to the MUC service.

Installation
------------

Download latest release from [here](https://github.com/igniterealtime/openfire-pade-plugin/releases) and upload the pade.jar from the admin web console of Openfire. Wait for the plugin to appear in the plugins listing and then complete the following steps to confirm it is working.

Make sure this user is online and has joined the **ofmeet** chat room. Confirm focus user is also online and has joined the **ofmeet** room as well.

![image](https://user-images.githubusercontent.com/110731/99916763-eb412900-2d03-11eb-9028-c391713d4384.png)

Jicofo (offocus plugin) is still has as an xmpp component even though I suspect it is not being used. It connects to Openfire now as an external component

![image](https://user-images.githubusercontent.com/110731/99916862-a10c7780-2d04-11eb-838c-0ba134643852.png)

if you have configured a SIP account for jigasi, also confirm that the jigasi user has logged in.

If you have an active focus user, then you can do a quick peer-to-peer test with two browser tabs on your desktop. Open both of them to the same conference like https://your_server:7443/ofmeet/testconf and confirm that it is showing in the conference summary.

If you get audio and video, then focus bot user is working ok and XMPP messages are passing around ok. If not, it is back to the log files and help from the community.

To confirm the video-bridge is working, you need to run the last step again with 3 users. If audio and video stops with third participant, then double check on the network configuration, making sure TCP port 7443 and UDP port 10000 are opened for listening from the openfire server. Otherwise, check the log files and ask for help from ignte-realtime community.

The new summary admin page shows call statistics from JVB2 as well as all active calls
![image](https://user-images.githubusercontent.com/110731/100152444-9e438b00-2e9a-11eb-9294-6df1112446d6.png)

Special cases
--------------

By default, Pade should run out of the box with Openfire default settings. However, if ldap or any other custom user provider is being used, user accounts must be created manually for jvb, focus and jigasi (if needed) as the plugin cannot do this automatically.

On Windows servers, Pade may not work if Openfire is installed in the default location **"Program Files/Openfire"** because of the embedded space in the name. Try using a different location with no embedded spaces. Also note that Jitsi videobridge cannot use the webrtc datachanel because of a missing binary in Windows and **must** use websockets for the data channel to Jitsi Meet. Port 8180 will be used by default in Openfire. A websocket proxy has been implemented in Pade to proxy from the configured Openfire websocket TLS port (7443) to 8180. This allows JVB2 to reuse the Openfire domain certificate for TLS on port 7443.

If port 8180 is in use elsewhere then this needs to be changed. Use the Network web page to do so. If you use iptables, an external web server like nginx or haproxy to redirect standard TLS port 433 to Openfire TLS port 7443, then the public 'advertised port' for websockets (the publicly-accessible port Jitsi Meet web client will use) should be set to 443. Otherwise leave the default value as your Openfire TLS port (7443). 

![image](https://user-images.githubusercontent.com/110731/102720510-ae5d5780-42ec-11eb-9531-2e4b9a9523e8.png)

If you want to allow regular telephone users to join a conference from a home or office telephone, you would need to set up the SIP Gateway to a telephone provider. You would need to script an IVR (interective response) which will allow the caller to use the phone buttons/touch tones to select their destination meeting and convert that into a room name in the SIP header that Jigasi will use to route the call to the appropriate meeting room. For an example, see https://voximplant.com/docs/tutorials/jigasi-setup

The alternative is much simpler if you already have FreeSWITCH with working phones and trunks setup with an external telephone line provider. You enable the Pade to connect to FreeSWITCH via ESL (external socket libary) and Pade will start to monitor every meeting. When the focus user joins, it will create a FreeSWITCH audio conference and initiate a call from the audio conference to Jigasi adding a SIP header with the name of the meeting room. You can now update your FreeSWITCH dial plan with internal and external telephone numbers that can be used by your users to to join the FreeSWITCH audio conference that is bridged to the Jitsi meeting.


Build instructions
------------------

This project is a Apache Maven project. 

Build using the standard Maven invocation:

    mvn clean package
    
After a successful execution, a plugin should be available in this locations:

    pade/target/pade.jar       
    
Pade Chat (Converse) will be available as a web page and progressive web application from /pade

    For example - https://desktop-545pc5b:7443/pade
    
Pade Meetings (Jitsi Meet) will be available as a web page and progressive web application from /ofmeet

    For example - https://desktop-545pc5b:7443/ofmeet    
   
