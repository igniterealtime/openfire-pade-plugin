Pàdé for Openfire
=========================

This project produces four Openfire plugins, offocus, ofmeet, ofgasi and pade, combined, provide a unified communication solution for Openfire.

The pade project bundles various third-party products, notably:
- [Jitsi Videobridge](https://github.com/jitsi/jitsi-videobridge) project;
- [Jitsi Conference Focus (jicofo)](https://github.com/jitsi/jicofo) project; 
- [Jitsi Meet](https://github.com/jitsi/jitsi-meet) web client.
- [Pàdé](https://github.com/igniterealtime/pade) web desktop client.

Installation
------------
Install the offocus, ofmeet, ofgasi and pade plugins into your Openfire instance.

Build instructions
------------------

This project is a Apache Maven project

First edit the pom.xml file and change the default properties

```
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <openfire.version>4.2.0</openfire.version>
        <videobridge.version>1.1-20191004.132308-120</videobridge.version>        
        <jicofo.version>1.1-20191122.000029-86</jicofo.version>
        <jigasi.version>1.1-20190806.132856-49</jigasi.version>    
        <jicoco.version>1.1-20190509.130302-15</jicoco.version>     
        <pade.url>https://desktop-545pc5b:7443/apps/index.html</pade.url>        
    </properties>
```    

Build using the standard Maven invocation:

    mvn clean package
    
After a successful execution, the four plugins should be available in these locations:

    offocus/target/offocus.jar
    ofmeet/target/ofmeet.jar
    ofgasis/target/ofgasi.jar
    pade/target/pade.jar    
    
Binary packages for Pade as an Electron desktop application are also available in these locations    

    pade/target/pade-x.x.x-SNAPSHOT-darwin-64.zip
    pade/target/pade-x.x.x-SNAPSHOT-linux-64.zip
    pade/target/pade-x.x.x-SNAPSHOT-win-64.zip
    
Pade will be available as a web page and progressive web application from <pade.url>

    For example - https://desktop-545pc5b:7443/pade
