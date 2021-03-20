/**
 * Realtime  API
 */

var realtime = (function(rt)
{
    window.addEventListener("beforeunload", function ()
    {
        console.info("beforeunload");
    });

    window.addEventListener("load", function()
    {
        console.log("event.load");
    });

    function connectSIP(config)
    {
        var getTurnServers = function()
        {
            var turnServers = null;

            if (config.iceServers && config.iceServers.iceServers)
            {
                turnServers = [];

                for (var i=0; i<config.iceServers.iceServers.length; i++)
                {
                    if (config.iceServers.iceServers[i].url.indexOf("turn:") > -1 || config.iceServers.iceServers[i].url.indexOf("turns:") > -1)
                    {
                        turnServers.push({urls: config.iceServers.iceServers[i].url, username: config.iceServers.iceServers[i].username, password: config.iceServers.iceServers[i].credential})
                    }
                }
            }

            return turnServers;
        }

        var getStunServers = function()
        {
            var stunServers = null;

            if (config.iceServers && config.iceServers.iceServers)
            {
                stunServers = [];

                for (var i=0; i<config.iceServers.iceServers.length; i++)
                {
                    if (config.iceServers.iceServers[i].url.indexOf("stun:") > -1 || config.iceServers.iceServers[i].url.indexOf("stuns:") > -1)
                    {
                        stunServers.push(config.iceServers.iceServers[i].url)
                    }
                }
            }

            return stunServers;
        }

        rt.sipUI = new SIP.UA(
        {
            password        : config.sip.password,
            displayName     : config.sip.displayname,
            uri             : 'sip:' + config.sip.authusername + '@' + config.sip.server,
            wsServers       : "wss://" + window.location.host + "/sip/proxy?url=ws://" + config.sip.server + ":5066",
            turnServers     : getTurnServers(),
            stunServers     : getStunServers(),
            registerExpires : 30,
            traceSip        : true,
            log             : {
            level : 99,
            }
        });

        rt.sipUI.on('connected', function(e) {
            console.log("SIP Connected");
        });

        rt.sipUI.on('disconnected', function(e) {
            console.log("SIP Disconnected");
        });

        rt.sipUI.on('registered', function(e) {
            console.log("SIP Ready");
        });

        rt.sipUI.on('registrationFailed', function(e) {
            console.log("Error: Registration Failed");
        });

        rt.sipUI.on('unregistered', function(e) {
            console.log("Error: Registration Failed");
        });

        rt.sipUI.on('message', function(message) {
            console.log("SIP Message", message.body);

            var data = {};

            if (message.body.substring(0, 1) == "{")
            {
                try {
                    data = JSON.parse(message.body);

                    if (data.payload.xmpp)
                    {
                        data.payload.xmpp = atob(data.payload.xmpp);
                    }

                    console.log("JSON Object", data);

                } catch (e) {
                    console.error(e);
                }
            }
        });

        rt.sipUI.on('invite', function (incomingSession) {

            console.log("call type", incomingSession.request.headers["X-Ihive-Calltype"][0].raw);

            var remoteSipMedia = document.getElementById("remoteVideo");
            var localSipMedia = document.getElementById("localVideo");

            incomingSession.accept({
                media : {
                constraints : { audio : true, video : true },
                render      : { remote : remoteSipMedia, local: localSipMedia },
                }
            });
        });

    }

    function getUniqueID()
    {
        return Math.random().toString(36).substr(2, 9);
    }

    //-------------------------------------------------------
    //
    //  realtime - version 0.0.1 (public)
    //
    //-------------------------------------------------------

    rt.login = function(server, username, password)
    {
        var config = {

            sip: {
            username: username,
            authusername: username,
            displayname: username,
            password: password,
            server: server,
            enabled: true,
            voicemail: username,
            outboundproxy: server
            }
        }

        connectSIP(config);
    }

    rt.hangup = function(session)
    {
        if (!session) {
            return;
        } else if (session.startTime) {
            session.bye();
        } else if (session.reject) {
            session.reject();
        } else if (session.cancel) {
            session.cancel();
        }

        delete rt.sipSessions[session.sessionId];
    }

    rt.dial = function(dialstring)
    {
        var session = null;
        try {
        var remoteSipMedia = document.getElementById("remoteVideo");
        var localSipMedia = document.getElementById("localVideo");

        session = rt.sipUI.invite(dialstring,
        {
            media : {
                constraints : { audio : true, video : true },
                render      : { remote : remoteSipMedia, local: localSipMedia},
            },
            extraHeaders: [ 'X-ihive-calltype: video']
        });

        session.direction = 'outgoing';
        session.sessionId  = getUniqueID();

        rt.sipSessions[session.sessionId] = session;

        } catch(e) {
        throw(e);
        }
        return session;
    }

    rt.sipSessions = {};

        return rt;

}(realtime || {}));