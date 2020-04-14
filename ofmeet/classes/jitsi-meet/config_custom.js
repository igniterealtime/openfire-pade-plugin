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

if (location.hash.indexOf("config.webinar=true") > -1) config.webinar = true;
if (location.hash.indexOf("config.webinar=false") > -1) config.webinar = false;

if (config.webinar)
{
    interfaceConfig.TOOLBAR_BUTTONS = [
        'fullscreen', 'fodeviceselection', 'hangup', 'profile', 'info', 'chat',
        'settings', 'videoquality', 'feedback', 'stats', 'shortcuts', 'raisehand'
    ]

    interfaceConfig.FILM_STRIP_MAX_HEIGHT = 1;
}
