Pàdé Plugin for Openfire
=========================

This project produces four Openfire plugins, offocus, ofmeet, ofgasi and pade, combined, provide a unified communication solution for Openfire.

The pade project bundles various third-party products, notably:
- [Jitsi Videobridge](https://github.com/jitsi/jitsi-videobridge) project;
- [Jitsi Conference Focus (jicofo)](https://github.com/jitsi/jicofo) project; 
- [Jitsi Meet](https://github.com/jitsi/jitsi-meet) webclient.
- [ConverseJS](https://github.com/conversejs/converse.js) webclient.

Installation
------------
Install the offocus, ofmeet, ofgasi and pade plugins into your Openfire instance.

Build instructions
------------------

This project is a Apache Maven project, and is build using the standard Maven invocation:

    mvn clean package

After a successful execution, the four plugins should be available in these locations:

    offocus/target/offocus.jar
    ofmeet/target/ofmeet.jar
    ofgasis/target/ofgasi.jar
    pade/target/pade.jar    
