/* TODO - use this to set localstorage instead of keeping in localstorage

if (navigator.credentials)
{
    navigator.credentials.get({password: true, federated: {providers: [ 'https://accounts.google.com' ]}, mediation: "silent"}).then(function(credential)
    {
        console.log("credential management api get", credential);

        if (credential)
        {
            localStorage.setItem("xmpp_username_override", credential.id);
            localStorage.setItem("xmpp_password_override", credential.password);
        }

    }).catch(function(err){
        console.error ("credential management api get error", err);
    });
}
*/

const welcomeEle = document.getElementById("welcome-page-additional-content-template");
if (welcomeEle) welcomeEle.innerHTML = interfaceConfig.OFMEET_WELCOME_PAGE_CONTENT;

const settingsEle = document.getElementById("settings-toolbar-additional-content-template");
if (settingsEle) settingsEle.innerHTML = interfaceConfig.OFMEET_WELCOME_PAGE_TOOLBARCONTENT;

if (location.hash.indexOf("config.webinar=true") > -1) config.webinar = true;
if (location.hash.indexOf("config.webinar=false") > -1) config.webinar = false;

if (location.hash.indexOf("interfaceConfig.OFMEET_RECORD_CONFERENCE=true") > -1) interfaceConfig.OFMEET_RECORD_CONFERENCE = true;
if (location.hash.indexOf("interfaceConfig.OFMEET_RECORD_CONFERENCE=false") > -1) interfaceConfig.OFMEET_RECORD_CONFERENCE = false;

if (location.hash.indexOf("interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION=true") > -1) interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION = true;
if (location.hash.indexOf("interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION=false") > -1) interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION = false;

if (location.hash.indexOf("interfaceConfig.OFMEET_SHOW_CAPTIONS=true") > -1) interfaceConfig.OFMEET_SHOW_CAPTIONS = true;
if (location.hash.indexOf("interfaceConfig.OFMEET_SHOW_CAPTIONS=false") > -1) interfaceConfig.OFMEET_SHOW_CAPTIONS = false;

if (location.hash.indexOf("interfaceConfig.OFMEET_ENABLE_BREAKOUT=true") > -1) interfaceConfig.OFMEET_ENABLE_BREAKOUT = true;
if (location.hash.indexOf("interfaceConfig.OFMEET_ENABLE_BREAKOUT=false") > -1) interfaceConfig.OFMEET_ENABLE_BREAKOUT = false;

if (location.hash.indexOf("interfaceConfig.OFMEET_CONTACTS_MGR=true") > -1) interfaceConfig.OFMEET_CONTACTS_MGR = true;
if (location.hash.indexOf("interfaceConfig.OFMEET_CONTACTS_MGR=false") > -1) interfaceConfig.OFMEET_CONTACTS_MGR = false;


if (config.webinar)
{
    interfaceConfig.TOOLBAR_BUTTONS = [
        'fullscreen', 'fodeviceselection', 'hangup', 'profile', 'info', 'chat',
        'settings', 'videoquality', 'feedback', 'stats', 'shortcuts', 'raisehand'
    ]

    interfaceConfig.SETTINGS_SECTIONS = [
        'language', 'profile'
    ]

    interfaceConfig.FILM_STRIP_MAX_HEIGHT = 1;
}

//config.googleApiApplicationClientID = "39065779381-bbhnkrgibtf4p0j9ne5vsq7bm49t1tlf.apps.googleusercontent.com",
//config.microsoftApiApplicationClientID = "00000000-0000-0000-0000-000040240063",
//config.enableCalendarIntegration = true,
//config.dialInNumbersUrl = './phoneNumberList.json';
//config.dialInConfCodeUrl = './conferenceMapper.json';
//config.dialOutCodesUrl = './countrycodes.json';
//config.dialOutAuthUrl = './authorizephone.json';
//config.peopleSearchUrl = './directorySearch.json';
//config.inviteServiceUrl = './conferenceInvite.json';
//config.inviteServiceCallFlowsUrl = './conferenceinvitecallflows.json';

config.dialInNumbersUrl = 'https://web-cdn.jitsi.net/beta/phoneNumberList.json';
config.dialInConfCodeUrl = 'https://api.jitsi.net/conferenceMapper';
//config.dialOutCodesUrl = 'https://api.jitsi.net/countrycodes';
//config.dialOutAuthUrl = 'https://api.jitsi.net/authorizephone';
//config.hosts.call_control = "callcontrol.desktop-545pc5b";