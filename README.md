Pàdé for Openfire
=========================

This project produces two Openfire plugins, ofmeet and pade to provide a unified communication solution for Openfire.

The ofmeet plugin (Openfire Meetings) includes various third-party products, notably:
- [Jitsi Videobridge](https://github.com/jitsi/jitsi-videobridge) project;
- [Jitsi Conference Focus (jicofo)](https://github.com/jitsi/jicofo) project; 
- [Jitsi Meet](https://github.com/jitsi/jitsi-meet) web client.
- [Jitsi SIP Gateway](https://github.com/jitsi/jigasi) project.

The pade plugin hosts the web and pwa version of
- [Pàdé](https://github.com/igniterealtime/pade) web desktop client based on the [ConverseJS](https://github.com/conversejs/converse.js) project.

Installation
------------
First, there are few things you need to know. These include:

* ofmeet does work with Firefox. It however works best with Chromium based apps like Chrome, Edge, Electron and Opera. 

* ofmeet plugin will not work out of the box if your Openfire server is configured to use LDAP. You would need to create the jvb, focus and jigasi bot users manually. Give the focus bot user owner/admin permissions to the MUC service.

* ofmeet has minimal network requirements and works out of the box internally on a local area network (LAN) or with a hosted Openfire server on the internet. If your Openfire server is placed behind a NAT and firewall and you want to allow external internet access, then you require some network expertise to configure it. You would need to open a few UDP/TCP ports and provide both the public and private IP addresses of your openfire server.

Download latest release from [here](https://github.com/igniterealtime/openfire-pade-plugin/releases) and upload the pade.jar and ofmeet.jar in any order from the admin web console of Openfire. Wait for both to appear in the plugins listing and then complete the following steps to confirm it is working.

* JVB  (ofmeet plugin) does not exposed an xmpp component anymore. Instead, it uses an XMPP user called **jvb** that will join a global conference called **ofmeet**

![image](https://user-images.githubusercontent.com/110731/99916724-af0dc880-2d03-11eb-80c3-b35b9009910a.png)

Make sure this user is online and has joined the **ofmeet** chat room. Confirm focus user is also onlie and has joined the **ofmeet** room as well.

![image](https://user-images.githubusercontent.com/110731/99916763-eb412900-2d03-11eb-9028-c391713d4384.png)

* Jicofo (offocus plugin) is still has as an xmpp component even though I suspect it is not being used. It connects to Openfire now as an external component

![image](https://user-images.githubusercontent.com/110731/99916862-a10c7780-2d04-11eb-838c-0ba134643852.png)

* if you have configured a SIP account for jigasi, also confirm that the jigasi user has logged in.

* If you have an active focus user, then you can do a quick peer-to-peer test with two browser tabs on your desktop. Open both of them to the same https://your_server:7443/ofmeet/testconf and confirm that it is showing in the conference summary.

* If you get audio and video, then focus bot user is working ok and XMPP messages are passing around ok. If not, it is back to the log files and help from the community.

To confirm the video-bridge is working, you need to run the last step again with 3 users. If audio and video stops with third participant, then double check on the network configuration, making sure TCP port 7443 and UDP port 10000 are opened for listening from the openfire server. Otherwise, check the log files and ask for help from ignte-realtime community.

The new summary admin page shows call statistics from JVB2 as well as all active calls
![image](https://user-images.githubusercontent.com/110731/100152444-9e438b00-2e9a-11eb-9294-6df1112446d6.png)

## Special cases
By default, ofmeet should run out of the box with Openfire default settings. However, if ldap or any other custom user provider is being used, user accounts must be created manually for jvb, focus and jigasi (if needed) as the plugin cannot do this automatically.

On Windows servers, JVB2 cannot use the webrtc datachanel and **must** use websockets for the data channel to Jitsi Meet. Ports 8080/8443 will be used by default in Openfire. A websocket proxy has been implemented in ofmeet to proxy from the configured Openfire websocket TLS port (7443) to 8080. This removes the need to open port 8443 externally and allows JVB2 to reuse the Openfire domain certificate for TLS on port 7443.

If ports 8080/8443 are in use elsewhere and this needs to be changed, use the Network web page to do so.
![image](https://user-images.githubusercontent.com/110731/100220971-f4064a80-2f0f-11eb-9af3-b3e8716a8252.png)


Build instructions
------------------

This project is a Apache Maven project

First edit the pom.xml file and change the default properties. Electron build is disabled by default.

Build using the standard Maven invocation:

    mvn clean package
    
After a successful execution, the four plugins should be available in these locations:

    ofmeet/target/ofmeet.jar
    pade/target/pade.jar    
    
Binary packages for Pade as an Electron desktop application are also available in these locations    

    pade/target/pade-x.x.x-SNAPSHOT-darwin-64.zip
    pade/target/pade-x.x.x-SNAPSHOT-linux-64.zip
    pade/target/pade-x.x.x-SNAPSHOT-win-64.zip
    
Pade will be available as a web page and progressive web application from <pade.url>

    For example - https://desktop-545pc5b:7443/pade
   
