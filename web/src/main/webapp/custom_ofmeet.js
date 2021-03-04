var ofmeet = (function(of)
{
    //-------------------------------------------------------
    //
    //  defines
    //
    //-------------------------------------------------------

    const IMAGES = {
        pad: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M30.122 30.122l-2.102-6.344-16.97-16.97L10 7.858 6.808 11.05l16.97 16.97zM3.98 8.222L8.222 3.98l-2.1-2.1a2.998 2.998 0 00-4.242 0 2.998 2.998 0 000 4.242l2.1 2.1z"/></svg>',
        sheet: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M4 10h4a2 2 0 002-2V4a2 2 0 00-2-2H4a2 2 0 00-2 2v4a2 2 0 002 2zm10 0h4a2 2 0 002-2V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v4a2 2 0 002 2zm10 0h4a2 2 0 002-2V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v4a2 2 0 002 2zM2 18a2 2 0 002 2h4a2 2 0 002-2v-4a2 2 0 00-2-2H4a2 2 0 00-2 2v4zm10 0a2 2 0 002 2h4a2 2 0 002-2v-4a2 2 0 00-2-2h-4a2 2 0 00-2 2v4zm10 0a2 2 0 002 2h4a2 2 0 002-2v-4a2 2 0 00-2-2h-4a2 2 0 00-2 2v4zM2 28a2 2 0 002 2h4a2 2 0 002-2v-4a2 2 0 00-2-2H4a2 2 0 00-2 2v4zm10 0a2 2 0 002 2h4a2 2 0 002-2v-4a2 2 0 00-2-2h-4a2 2 0 00-2 2v4zm10 0a2 2 0 002 2h4a2 2 0 002-2v-4a2 2 0 00-2-2h-4a2 2 0 00-2 2v4z"/></svg>',
        code: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M21.165 21.165l-1.779-5.373L9.118 5.525 5.525 9.118l10.267 10.268zM.765.765a2.54 2.54 0 000 3.594L3.41 7.003 7.003 3.41 4.359.765a2.54 2.54 0 00-3.594 0zm29.217 5.247h-14.16l1.997 1.998h12.163v21.972H8.01V17.819l-1.998-1.997v14.16c0 1.103.895 1.998 1.998 1.998h21.972a1.999 1.999 0 001.998-1.998V8.01a1.999 1.999 0 00-1.998-1.998z"/></svg>',
        slide: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M8.03 16.49c.309.001.6-.142.79-.386l4.404-5.67 3.016 3.542c.192.222.408.32.766.352.29-.002.57-.13.76-.358l7-8.338a.998.998 0 10-1.532-1.284l-6.24 7.432L13.95 8.2c-.198-.228-.562-.334-.784-.35a1.004 1.004 0 00-.768.386l-5.158 6.64a1 1 0 00.79 1.614zM29.978-.01H2.022C1.458-.01 1 .438 1 .99s.458 1 1.022 1H3v18a2 2 0 002 2h10v3.122L9.328 30.25a1 1 0 101.344 1.48L15 27.81v3.18a1 1 0 102 0v-3.18l4.328 3.92a1 1 0 001.344-1.48L17 25.112V21.99h10a2 2 0 002-2v-18h.978c.564 0 1.022-.448 1.022-1s-.458-1-1.022-1zM27 19.99H5v-18h22v18z"/></svg>',
        poll: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M13.774 26.028a2.06 2.06 1080 104.12 0 2.06 2.06 1080 10-4.12 0zm5.69-7.776c2.898-1.596 4.37-3.91 4.37-6.876 0-5.094-4.018-7.376-8-7.376-3.878 0-8 2.818-8 8.042a2 2 0 104 0c0-2.778 2.074-4.042 4-4.042 1.494 0 4 .438 4 3.376 0 1.042-.274 2.258-2.298 3.374-1.376.754-3.702 2.712-3.702 5.25a2 2 0 104 0c0-.372.79-1.286 1.63-1.748z"/></svg>',
        kanban: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M31.966 3.896C31.878 2.866 31.046 2 30 2H2C.954 2 .122 2.866.034 3.896H0V30a2 2 0 002 2h28a2 2 0 002-2V3.896h-.034zM12 14V8h8v6h-8zm8 2v6.166h-8V16h8zM10 8v6H2V8h8zm-8 8h8v6.166H2V16zm0 14v-6h8v6H2zm10 0v-6h8v6h-8zm18 0h-8v-6h8v6zm0-7.834h-8V16h8v6.166zM30 14h-8V8h8v6z"/></svg>',
        whiteboard: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M29 0H3a2 2 0 000 4v16a2 2 0 002 2h10v3.122L9.328 30.26a1 1 0 101.344 1.48L15 27.82V31a1 1 0 002 0v-3.18l4.328 3.92a.997.997 0 001.412-.068 1 1 0 00-.068-1.412L17 25.122V22h10a2 2 0 002-2V4a2 2 0 000-4zm-4.306 7.72l-7 6.75a1 1 0 01-1.384.004l-3.09-2.938-4.47 4.658a1.003 1.003 0 01-1.414.026 1 1 0 01-.028-1.414l5.158-5.376a1.009 1.009 0 011.41-.03l3.12 2.964 6.31-6.086a1 1 0 011.414.028 1 1 0 01-.026 1.414z"/></svg>',
        person: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M16 0C7.164 0 0 7.164 0 16s7.164 16 16 16 16-7.164 16-16c0-8.838-7.164-16-16-16zm.568 7.984a3.323 3.323 0 011.726-1.728A3.12 3.12 0 0119.564 6c.458 0 .88.084 1.27.256.39.17.732.404 1.026.7.296.294.53.638.7 1.028.172.39.258.812.258 1.27 0 .458-.086.88-.258 1.27-.17.39-.404.732-.7 1.028a3.373 3.373 0 01-1.026.7 3.14 3.14 0 01-1.27.256c-.456 0-.88-.086-1.27-.256a3.347 3.347 0 01-1.028-.7 3.376 3.376 0 01-.698-1.028 3.114 3.114 0 01-.258-1.27c0-.458.086-.88.258-1.27zm-5.066-.516a2.395 2.395 0 011.754-.726c.704 0 1.3.242 1.784.726.486.486.728 1.072.728 1.758 0 .704-.244 1.298-.73 1.784-.482.484-1.078.726-1.782.726a2.392 2.392 0 01-1.754-.728 2.426 2.426 0 01-.73-1.784c.002-.684.244-1.27.73-1.756zm-4.366.458c.37-.372.822-.558 1.354-.558.534 0 .98.186 1.342.558.36.37.54.812.54 1.326 0 .534-.182.984-.54 1.356a1.796 1.796 0 01-1.342.558c-.532 0-.984-.186-1.354-.556a1.846 1.846 0 01-.558-1.356c0-.514.186-.958.558-1.328zM24 26h-8.79v-4H9.704v-4H6.418v-3.638c0-.646-.016-1.194.432-1.64.446-.446.994-.67 1.64-.67.552 0 1.03.166 1.428.5.4.334.664.746.8 1.242.322-.38.704-.674 1.14-.886a3.22 3.22 0 011.398-.314c.78 0 1.46.234 2.042.7.58.466.966 1.052 1.156 1.754.4-.38.864-.686 1.4-.912a4.304 4.304 0 011.712-.342c.61 0 1.186.114 1.728.342.54.228 1.012.542 1.412.942.4.4.718.87.954 1.412.238.54.34 1.116.34 1.726V26z"/></svg>',
        contact: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M2 0a2 2 0 00-2 2v28a2 2 0 002 2h2V0H2zm24 0H6v32h20a2 2 0 002-2V2a2 2 0 00-2-2zm-9.11 7.708a3.026 3.026 0 110 6.053 3.026 3.026 0 010-6.053zM12 21.614c0-3.668 2.218-6.64 4.952-6.64s4.952 2.974 4.952 6.64S12 25.28 12 21.614zM30 2h2v6h-2zm0 8h2v6h-2zm0 8h2v6h-2z"/></svg>',
        mic: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M14 25.808V30h-3a1 1 0 000 2h10a1 1 0 000-2h-3v-4.204c4.166-.822 8-4.194 8-9.796v-3a1 1 0 00-2 0v3c0 5.252-4.026 8-8 8-3.854 0-8-2.504-8-8v-3a1 1 0 00-2 0v3c0 5.68 3.766 9.012 8 9.808zM16 20c2.21 0 4-1.79 4-4V4c0-2.21-1.79-4-4-4s-4 1.79-4 4v12c0 2.21 1.79 4 4 4z"/></svg>',
        mail: '<svg width="32" height="32" viewBox="0 0 32 32"><path d="M30 8H2a2 2 0 00-2 2v3.358l16 6.4 16-6.4V10a2 2 0 00-2-2zM0 16.624V30a2 2 0 002 2h28a2 2 0 002-2V16.624l-16 6.4-16-6.4z"/></svg>',
        tags: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M26.667 14.251v-1.833a1.705 1.705 0 00-1.748-1.749h-5.334c-1.001 0-1.833.75-1.833 1.749v7.165c0 1.002.833 1.75 1.833 1.75h5.334a1.706 1.706 0 001.748-1.75v-1.832H24v.918h-3.583v-5.334H24v.918h2.667zm-12.416 0v-1.833c0-1.001-.834-1.749-1.832-1.749H7.085a1.706 1.706 0 00-1.749 1.749v7.165a1.707 1.707 0 001.749 1.75h5.334c1.001 0 1.832-.75 1.832-1.75v-1.832h-2.666v.918H8.001v-5.334h3.584v.918h2.666zM28.416 1.75C30.332 1.75 32 3.418 32 5.334v21.332c0 1.916-1.668 3.584-3.584 3.584H3.584c-2 0-3.584-1.668-3.584-3.584V5.334C0 3.418 1.584 1.75 3.584 1.75h24.832z"/></svg>',
        record: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M29.999 14l-6.001 3.999a3.961 3.961 0 00-1.408-3.02c2.034-1.224 3.406-3.431 3.406-5.978a7 7 0 00-14.001 0A6.97 6.97 0 0014.108 14h-3.667c.957-1.061 1.558-2.455 1.558-4A6 6 0 100 10c0 1.809.816 3.409 2.083 4.509A3.977 3.977 0 000 17.999v8a4.001 4.001 0 004 3.999h15.998c2.207 0 4-1.792 4-3.999v-1l6.001 4.999A2.001 2.001 0 0032 27.997V15.998A2.002 2.002 0 0029.999 14zM2.001 10a4 4 0 118 .002A4 4 0 012 10zm19.998 15.999A2.001 2.001 0 0119.998 28H4a2.002 2.002 0 01-2.002-2.001v-8c0-1.103.895-2.001 2.002-2.001h15.998c1.104 0 2.001.895 2.001 2.001v8zm-3-11.989a5.01 5.01 0 01-5.012-5.012 5.01 5.01 0 015.012-5.012 5.01 5.01 0 015.012 5.012 5.013 5.013 0 01-5.012 5.012zm11 3.487v10.496l-6.001-4.995v-3l6.001-4v1.499z"/></svg>',
        photo: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M6.667 5.667h-4V4.333h4v1.334zm10.666 6.666c-2.205 0-4 1.795-4 4 0 2.206 1.795 4 4 4 2.206 0 4-1.794 4-4 0-2.205-1.794-4-4-4zM32 7v20H0V7h7.907c.892 0 1.724-.445 2.218-1.188L12 3h10.667l1.874 2.812A2.665 2.665 0 0026.76 7H32zM6.667 12.333a1.333 1.333 0 10-2.667 0 1.333 1.333 0 102.667 0zm17.333 4c-.001-5.132-5.557-8.339-10.002-5.773a6.67 6.67 0 00-3.333 5.775c.001 5.132 5.557 8.339 10.002 5.773A6.668 6.668 0 0024 16.333z"/></svg>',
        desktop: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M30 2H2a2 2 0 00-2 2v18a2 2 0 002 2h9.998c-.004 1.446-.062 3.324-.61 4h-.404A.992.992 0 0010 29c0 .552.44 1 .984 1h10.03A.992.992 0 0022 29c0-.552-.44-1-.984-1h-.404c-.55-.676-.606-2.554-.61-4H30a2 2 0 002-2V4a2 2 0 00-2-2zM14 24l-.002.004L14 24zm4.002.004L18 24h.002v.004zM30 20H2V4h28v16z"/><path d="M19.231 11.918a3.305 3.305 0 01-3.305 3.305 3.305 3.305 0 01-3.305-3.305 3.305 3.305 0 013.305-3.305 3.305 3.305 0 013.305 3.305z"/><path d="M16 5.33A6.677 6.677 0 009.33 12 6.677 6.677 0 0016 18.67 6.677 6.677 0 0022.67 12 6.677 6.677 0 0016 5.33zm0 1A5.662 5.662 0 0121.67 12 5.662 5.662 0 0116 17.67 5.662 5.662 0 0110.33 12 5.662 5.662 0 0116 6.33z"/></svg>',
        picture: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M17.228 11.688a2.782 2.782 1080 105.564 0 2.782 2.782 1080 10-5.564 0zM26 28h2V16l-8 6-10-8-6 6v8h2zm4-24H2a2 2 0 00-2 2v24a2 2 0 002 2h28a2 2 0 002-2V6a2 2 0 00-2-2zm0 26H2V6h28v24z"/></svg>',
        cursor: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M26.56 21.272a.998.998 0 00-.098-1.15L9.828.356A1.001 1.001 0 008.062 1v26.094a1 1 0 001.802.596l4.472-6.024 3.102 8.874a2.18 2.18 0 004.116-1.44l-3.044-8.706 6.996 1.354a.998.998 0 001.054-.476z"/></svg>',
        cryptpad: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M16.057 0l-9.9 1.824a2.129 2.129 0 10-2.26 3.583v14.257c0 1.351.607 2.803 1.789 4.292a22.185 22.185 0 004.425 4.087 43.451 43.451 0 003.85 2.441c.148.492.462.938.966 1.229 1.201.694 2.646.058 3.063-1.145a43.4 43.4 0 003.982-2.525 21.993 21.993 0 004.426-4.087c1.184-1.52 1.791-2.977 1.791-4.292V5.407c1.431-.798 1.457-2.846.053-3.685a2.096 2.096 0 00-2.319.102L16.057 0zm-.031 2.096l9.02 1.652c.011.128.035.255.071.376l-5.306 3.479a5.172 5.172 0 00-7.639.033l-5.238-3.38c.051-.153.089-.309.102-.47l8.99-1.69zm9.732 3.04c.18.154.385.282.608.37v13.855c.004.195-.009.385-.037.574a7.22 7.22 0 01-1.382 2.634 19.059 19.059 0 01-3.819 3.516 36.8 36.8 0 01-3.628 2.287 2.127 2.127 0 00-2.575-.346c-.147.084-.27.189-.387.297a37.297 37.297 0 01-3.546-2.239 18.816 18.816 0 01-3.785-3.514 6.668 6.668 0 01-1.488-3.14V5.54c.209-.08.403-.193.574-.336l6.692 4.288a3.413 3.413 0 013.041-1.823 3.39 3.39 0 013.038 1.824l6.694-4.357zM11.024 9.628a5.042 5.042 0 001.421 5.204l-1.819 3.683h-.04a1.573 1.573 0 101.188 2.603h2.7v-1.757h-2.302l2.162-4.327a.905.905 0 00-.27-1.113 3.282 3.282 0 01-1.45-3.245l-1.591-1.048zm9.969.034l-1.587 1.049c.018.134.027.269.032.405a3.377 3.377 0 01-1.452 2.805.91.91 0 00-.269 1.113l2.126 4.327h-2.297v1.757h2.772c.298.344.731.541 1.186.541a1.572 1.572 0 10-.111-3.138l-1.82-3.688a5.133 5.133 0 001.42-5.171zm-5 .17a1.422 1.422 0 00-.714 2.651 1.421 1.421 0 002.134-1.228v-.005c0-.785-.637-1.418-1.42-1.418z"/></svg>',
        confetti: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M30 8H18.084v6H32v-4a2 2 0 00-2-2zM2 8a2 2 0 00-2 2v4h14V8H2zm0 8v14a2 2 0 002 2h10V16H2zm26 16a2 2 0 002-2V16H18.084v16H28zM15.998 5.984h.006A.047.047 0 0016 6h8c2.762 0 4-1.344 4-3s-1.238-3-4-3c-2.586 0-4.622 1.164-6 2.514a4.018 4.018 0 00-2.058-.576c-.724 0-1.394.204-1.982.536C12.584 1.14 10.56 0 8 0 5.238 0 4 1.344 4 3s1.238 3 4 3h8l-.002-.016zM26 3c0 .826-1.088 1-2 1h-4.542c-.016-.028-.03-.058-.046-.084C20.428 2.928 21.968 2 24 2c.912 0 2 .174 2 1zM6 3c0-.826 1.088-1 2-1 1.988 0 3.496.89 4.512 1.844-.032.05-.056.104-.086.156H8c-.912 0-2-.174-2-1z"/></svg>',
    };
    const SMILIES = [":)", ":(", ":D", ":+1:", ":P", ":wave:", ":blush:", ":slightly_smiling_face:", ":scream:", ":*", ":-1:", ":mag:", ":heart:", ":innocent:", ":angry:", ":angel:", ";(", ":clap:", ";)", ":beer:"];
    const nickColors = {}, padsList = [], captions = {msgsDisabled: true, msgs: []}, breakout = {rooms: [], duration: 60, roomCount: 10, wait: 10}, pdf_body = [];
    const lostAudioWorkaroundInterval = 300000; // 5min
    const i18n = i18next.getFixedT(null, 'ofmeet');

    let tagsModal = null, padsModal = null, breakoutModal = null, contactsModal = null;
    let padsModalOpened = false, contactsModalOpened = false, swRegistration = null, participants = {}, recordingAudioTrack = {}, recordingVideoTrack = {}, videoRecorder = {}, recorderStreams = {}, customStore = {}, filenames = {}, dbnames = [];
    let clockTrack = {start: 0, stop: 0, joins: 0, leaves: 0}, handsRaised = 0;
    let tags = {location: "", date: localizedDate(new Date()).format('LL'), subject: "", host: "", activity: ""};
    let audioTemporaryUnmuted = false,  cursorShared = false;
    let breakoutIconVisible = false;
    //-------------------------------------------------------
    //
    //  window events
    //
    //-------------------------------------------------------

    function isElectron()
    {
      return navigator.userAgent.indexOf('Electron') >= 0;
    }

    window.addEventListener("DOMContentLoaded", function()
    {
        console.debug("ofmeet.js load");

        setTimeout(setup);

        if (!config.webinar)
        {
            if (typeof indexedDB.databases == "function")
            {
                indexedDB.databases().then(function(databases)
                {
                    console.debug("ofmeet.js found databases", databases);

                    databases.forEach(function(db)
                    {
                        if (db.name.indexOf("ofmeet-db-") > -1) recoverRecording(db.name);
                    })
                })
            }
            if (window.webkitSpeechRecognition && !isElectron()) setupVoiceCommand()
        }
    });

    window.addEventListener("beforeunload", function(event)
    {
        console.debug("ofmeet.js beforeunload");

        // TODO - remove this to use credential api instead of keeping in localstorage
        //localStorage.removeItem("xmpp_username_override");
        //localStorage.removeItem("xmpp_password_override");

        if (APP.connection && !config.webinar)
        {
            if (dbnames.length > 0 || of.recording)
            {
                event.preventDefault();
                event.returnValue = '';
            }

            if (of.recording) stopRecorder();

            dbnames.forEach(function(dbname)
            {
                const deleteRequest = indexedDB.deleteDatabase(dbname)

                deleteRequest.onsuccess = function(event) {
                  console.debug("ofmeet.js me database deleted successfully", dbname);
                };
            });

            if (dbnames.length > 0 || of.recording)
            {
                return event.returnValue;
            }
        }
    });


    //-------------------------------------------------------
    //  Auto-hide Mouse
    //-------------------------------------------------------

    if (!interfaceConfig.OFMEET_ENABLE_MOUSE_SHARING)
    {
        const mouseIdleTimeout = 10000 // msec
        let mouseIdleTimer;
        let mouseIsHidden = false;
        document.addEventListener("mousemove", function()
        {
            if (mouseIdleTimer)
            {
                clearTimeout(mouseIdleTimer);
            }
            mouseIdleTimer = setTimeout(function()
            {
                if (!mouseIsHidden)
                {
                    document.querySelector("body").style.cursor = "none";
                    mouseIsHidden = true;
                }
            }, mouseIdleTimeout);
            if (mouseIsHidden)
            {
                document.querySelector("body").style.cursor = "auto";
                mouseIsHidden = false;
            }
        });
    }

    //-------------------------------------------------------
    //
    //  setup
    //
    //-------------------------------------------------------

    function newElement(el, id, html, className, label)
    {
        const ele = document.createElement(el);
        if (id) ele.id = id;
        if (html) ele.innerHTML = html;
        if (label) {
            ele.setAttribute('aria-label', label);
            ele.classList.add("ofmeet-tooltip");
        }
        if (className) ele.classList.add(className);
        document.body.appendChild(ele);
        return ele;
    }

    function setupVoiceCommand()
    {
        const enter_room_button = document.getElementById('enter_room_button');

        if (enter_room_button)
        {
            const button = newElement('div', "speak_room_button", IMAGES.mic, 'speaker-room-button', "Speak Meeting Room Name");

            button.addEventListener("click", function(evt)
            {
                evt.stopPropagation();
                button.disabled = true;
                button.style.visibility= "hidden";

                const recognition = new webkitSpeechRecognition();
                recognition.continuous = false;
                recognition.interimResults = false;
                recognition.lang = config.defaultLanguage;

                recognition.onresult = function(e)
                {
                    console.debug("Speech command event", e)

                    if(e.results[e.resultIndex].isFinal)
                    {
                        const resultat = e.results[event.resultIndex][0].transcript;
                        console.debug("Speech command transcript", resultat);

                        document.getElementById('enter_room_field').value = resultat;
                        recognition.stop();
                        window.location.replace(encodeURI(resultat));
                    }
                };

                recognition.onspeechend  = function(event)
                {
                    console.debug("Speech command onspeechend", event);
                }

                recognition.onstart = function(event)
                {
                    console.debug("Speech command started", event);
                }

                recognition.onerror = function(e)
                {
                    console.debug("Speech command error", e)
                    recognition.stop();
                }

                recognition.start();
            });

            button.style.margin = "5px";
            enter_room_button.parentNode.appendChild(button);
        }
    }

    function setup()
    {
        if (!APP.connection || !APP.conference || !APP.conference.isJoined())
        {
            setTimeout(setup, 100);
            return;
        }

        if (!config.webinar)
        {
            listenWebPushEvents();

            APP.conference._room.on(JitsiMeetJS.events.conference.CONFERENCE_LEFT, function()
            {
                console.debug("ofmeet.js me left");

                if (interfaceConfig.OFMEET_RECORD_CONFERENCE)
                {
                    if (of.recording) stopRecorder();

                    const ids = Object.getOwnPropertyNames(recordingVideoTrack);

                    ids.forEach(function(id)
                    {
                        delete recordingAudioTrack[id];
                        delete recordingVideoTrack[id];
                    });
                }
            });
			
			APP.conference._room.on(JitsiMeetJS.events.conference.USER_ROLE_CHANGED, function(user, role) 
			{
                console.debug("ofmeet.js participant role change", user, role);
				
				if (interfaceConfig.OFMEET_ENABLE_BREAKOUT && role == "moderator" && !breakoutIconVisible && user == APP.conference.getMyUserId()) {
					createBreakoutRoomsButton();
					breakoutIconVisible = true;
				}				
			});			

            APP.conference._room.on(JitsiMeetJS.events.conference.TRACK_REMOVED, function(track)
            {
                console.debug("ofmeet.js track removed", track.getParticipantId());

                if (track.getParticipantId() == APP.conference.getMyUserId())
                {
                    clockTrack.leaves = (new Date()).getTime();
                    hideClock();

                    if (of.recognition)
                    {
                        of.recognitionActive = false;
                        of.recognition.stop();
                    }
					
					if (of.recording) stopRecorder();					
                }

            });

            APP.conference._room.on(JitsiMeetJS.events.conference.USER_JOINED, function (id)
            {
                console.debug("user join", id, participants);
                addParticipant(id);
            });

            APP.conference._room.on(JitsiMeetJS.events.conference.USER_LEFT, function (id)
            {
                console.debug("user left", id);

                if (breakout.kanban)
                {
                    breakout.kanban.removeElement(id);
                }

                delete participants[id];
            });

            APP.conference._room.on(JitsiMeetJS.events.conference.TRACK_ADDED, function(track)
            {
                const id = track.getParticipantId();
                console.debug("ofmeet.js track added", id, track.getType());

                if (interfaceConfig.OFMEET_RECORD_CONFERENCE)
                {
                    if (track.getType() == "audio") recordingAudioTrack[id] = track.stream;
                    if (track.getType() == "video") recordingVideoTrack[id] = track.stream;
                }

                if (APP.conference.getMyUserId() == id  && !config.webinar)
                {

                }
            });

            APP.conference._room.on(JitsiMeetJS.events.conference.PARTICIPANT_PROPERTY_CHANGED, function(e, t, n, r)
            {
                console.debug("ofmeet.js property changed", e, t, n, r);
            });

            APP.conference._room.on(JitsiMeetJS.events.conference.TRACK_MUTE_CHANGED, function(track)
            {
                const id = track.getParticipantId();
                console.debug("ofmeet.js track muted", id, track.getType(), track.isMuted());

                if (interfaceConfig.OFMEET_RECORD_CONFERENCE)
                {
                    if (track.getType() == "audio") recordingAudioTrack[id].getAudioTracks()[0].enabled = !track.isMuted();
                    if (track.getType() == "video") recordingVideoTrack[id].getVideoTracks()[0].enabled = !track.isMuted();

                    const recordingStream = recorderStreams[id];

                    if (recordingStream) // recording active
                    {
                        if (track.getType() == "audio") recordingStream.getAudioTracks()[0].enabled = !track.isMuted();
                        if (track.getType() == "video") recordingStream.getVideoTracks()[0].enabled = !track.isMuted();
                    }
                }

                if (APP.conference.getMyUserId() == id)
                {
                    if (of.recognition)
                    {
                        if (track.isMuted())    // speech recog synch
                        {
                            console.debug("muted, stopping speech transcription");

                            of.recognitionActive = false;
                            of.recognition.stop();

                        } else {
                            console.debug("unmuted, starting speech transcription");
                            of.recognition.start();
                        }
                    }
                }
            });

            APP.conference._room.on(JitsiMeetJS.events.conference.PRIVATE_MESSAGE_RECEIVED, function(id, text, ts)
            {
                var participant = APP.conference.getParticipantById(id);
                var displayName = participant ? (participant._displayName || 'Anonymous-' + id) : (APP.conference.getLocalDisplayName() || "Me");

                console.debug("ofmeet.js private message", id, text, ts, displayName);

                const pretty_time = dayjs().format('MMM DD HH:mm:ss');
                pdf_body.push([pretty_time, displayName, text]);
            });

            APP.conference._room.on(JitsiMeetJS.events.conference.MESSAGE_RECEIVED, function(id, text, ts)
            {
                var participant = APP.conference.getParticipantById(id);
                var displayName = participant ? (participant._displayName || 'Anonymous-' + id) : (APP.conference.getLocalDisplayName() || "Me");

                console.debug("ofmeet.js message", id, text, ts, displayName, participant, padsModalOpened);

                if (text.indexOf(interfaceConfig.OFMEET_CRYPTPAD_URL) == 0)
                {
                    if (padsModalOpened) notifyText(displayName, text, id, function(button)
                    {
                        openPad(text);
                    })

                    if (padsModalOpened)
                    {
                        addPad(text);
                    }
                    else {
                        padsList.push(text);
                    }
                } else {

                    if (text.indexOf("http") != 0 && !captions.msgsDisabled)
                    {
                        if (captions.ele) captions.ele.innerHTML = displayName + " : " + text;
                        captions.msgs.push({text: text, stamp: (new Date()).getTime()});
                    }

                    const pretty_time = dayjs().format('MMM DD HH:mm:ss');
                    pdf_body.push([pretty_time, displayName, text]);
                }

                if (breakout.started)
                {
                    messageBreakoutRooms(text);
                }
            });

            captions.ele = document.getElementById("captions");
        }

        if (window.localStorage["ofmeet.settings.avatar"])
        {
            const dataUri = JSON.parse(window.localStorage["ofmeet.settings.avatar"]);
            console.log("ofmeet.js found avatar", dataUri);
            APP.conference.changeLocalAvatarUrl(dataUri);
        }

        setOwnPresence();

        if (APP.connection.xmpp.connection._stropheConn.pass || config.ofmeetWinSSOEnabled)
        {
            if (navigator.credentials && interfaceConfig.OFMEET_CACHE_PASSWORD)
            {
                const id = APP.connection.xmpp.connection.jid.split("/")[0];
                const pass = APP.connection.xmpp.connection._stropheConn.pass;

                localStorage.setItem("xmpp_username_override", id);
                localStorage.setItem("xmpp_password_override", pass);

                navigator.credentials.create({password: {id: id, password: pass}}).then(function(credential)
                {
                    credential.name = APP.conference.getLocalDisplayName();

                    navigator.credentials.store(credential).then(function()
                    {
                        console.debug("credential management api put", credential);

                    }).catch(function (err) {
                        console.error("credential management api put error", err);
                    });

                }).catch(function (err) {
                    console.error("credential management api put error", err);
                });
            }

            getVCard();
            getBookmarks();
        }

        APP.connection.xmpp.connection.addHandler(handleMessage, null, "message");
        APP.connection.xmpp.connection.addHandler(handleMucMessage, "urn:xmpp:json:0", "message");
        APP.connection.xmpp.connection.addHandler(handlePresence, null, "presence");

        getServiceWorker(function(registration)
        {
            console.debug('Service worker registered', registration);
        });

        setTimeout(postLoadSetup);
        setTimeout(postJoinSetup);

        console.log("ofmeet.js setup", APP.connection, captions);

        setTimeout(lostAudioWorkaround, 5000);
    }

    function postLoadSetup()
    {
        var dropZone = document.getElementById("videospace");

        console.debug("postLoadSetup", dropZone);

        if (!dropZone)
        {
            setTimeout(postLoadSetup, 1000);
            return;
        }

        if (interfaceConfig.OFMEET_CONTACTS_MGR) setupPushNotification();

        if (interfaceConfig.OFMEET_ALLOW_UPLOADS)
        {
            console.debug("postLoadSetup", dropZone);
            dropZone.addEventListener('dragover', handleDragOver, false);
            dropZone.addEventListener('drop', handleDropFileSelect, false);
        }
    }

    function postJoinSetup() {
        if (!APP.conference || !APP.conference.isJoined() || !document.getElementById('new-toolbox')) {
            setTimeout(postJoinSetup, 100);
            return;
        }

        console.debug("postJoinSetup");

        // custom events for show/hide toolbox
        const toolboxObserver = new MutationObserver(mutations => {
            mutations.forEach((mutation) => {
                if (mutation.target.className.search('visible') > 0) {
                    $(mutation.target).trigger('show');
                } else {
                    $(mutation.target).trigger('hide');
                }
            });
        })
        toolboxObserver.observe(document.getElementById('new-toolbox'), {
            attributes: true,
            attributeFilter: ['class']
        })

        // fake the interaction
        APP.conference.commands.addCommandListener("___FAKE_INTERACTION", function () {
            if (interfaceConfig.OFMEET_ENABLE_BREAKOUT && APP.conference._room.isModerator() && !breakoutIconVisible) {
                createBreakoutRoomsButton();
                breakoutIconVisible = true;
            }
        });

        if (interfaceConfig.OFMEET_ENABLE_BREAKOUT && APP.conference._room.isModerator()) {
            createBreakoutRoomsButton();
            breakoutIconVisible = true;
        } else {
            APP.conference.commands.sendCommandOnce("___FAKE_INTERACTION", { value: !0 });
        }

        if (interfaceConfig.OFMEET_RECORD_CONFERENCE && !config.webinar) {
			createRecordButton();
			createPhotoButton();
			createDesktopButton();

			if (APP.conference.getMyUserId()) {
				showClock();
				clockTrack.joins = (new Date()).getTime();
            }
        }

        if (interfaceConfig.OFMEET_TAG_CONFERENCE && !config.webinar) {
            if (interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION && window.webkitSpeechRecognition && !isElectron()) {
                setupSpeechRecognition();
            }

            captions.msgsDisabled = !interfaceConfig.OFMEET_SHOW_CAPTIONS;
            captions.transcriptDisabled = !interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION || isElectron();

            createTagsButton();
        }

        if (interfaceConfig.OFMEET_ALLOW_UPLOADS) {
            createAvatarButton();
        }

        if (interfaceConfig.OFMEET_ENABLE_MOUSE_SHARING) {
            createShareCursorButton();
        }

        if (interfaceConfig.OFMEET_ENABLE_CRYPTPAD) {
            createPadsButton();
        }

        if (interfaceConfig.OFMEET_ENABLE_WHITEBOARD && interfaceConfig.OFMEET_WHITEBOARD_URL) {
            createWhiteboardButton();
        }

        if (interfaceConfig.OFMEET_ENABLE_CONFETTI) {
            createConfettiButton();
        }
    }

    //-------------------------------------------------------
    //
    //  setup toolbar buttons
    //
    //-------------------------------------------------------
    
    function createContactsButton() {
        addToolbarItem({
            id: 'ofmeet-contacts',
            icon: IMAGES.contact,
            label: i18n('toolbar.contactsManager'),
            callback:
                (evt) => {
                    evt.stopPropagation();
                    doContacts();
                }
        });
    }

    function createRecordButton() {
        addToolbarItem({
            id: 'ofmeet-record',
            icon: IMAGES.record,
            label: i18n('toolbar.recordMeeting'),
            callback:
                (evt) => {
                    evt.stopPropagation();

                    if (!of.recording) {
                        startRecorder(startMeetingRecorder);
                    } else {
                        stopRecorder();
                    }
                }
        });

        const leaveButton = document.querySelector('div[aria-label="Leave the call"]');

        if (leaveButton) leaveButton.addEventListener("click", function (evt) {
            if (of.recording) stopRecorder();

            if (pdf_body.length > 0) {
                const margins = {
                    top: 70,
                    bottom: 40,
                    left: 30,
                    width: 550
                };
                const pdf = new jsPDF('p', 'pt', 'a4');
                //pdf.setFontSize(18);

                pdf.autoTable({
                    head: [['Date', 'Person', 'Message']],
                    body: pdf_body,
                    columnStyles: {
                        0: { cellWidth: 100 },
                        1: { cellWidth: 100 },
                        2: { cellWidth: 300 }
                    }
                })
                const roomLabel = APP.conference.roomName + '-' + Math.random().toString(36).substr(2, 9);
                pdf.save(roomLabel + '.pdf');
            }
        });
    }

    function createPhotoButton() {
        addToolbarItem({
            id: 'ofmeet-photo',
            icon: IMAGES.photo,
            label: i18n('toolbar.takePhoto'),
            callback: (evt) => {
                evt.stopPropagation();
                takePhoto();

                APP.UI.messageHandler.notify("Recording", "Conference Photo Taken");
            }
        });
    }

    function createDesktopButton() {
        addToolbarItem({
            id: 'ofmeet-desktop',
            icon: IMAGES.desktop,
            label: i18n('toolbar.recordDesktopApp'),
            callback:
                (evt) => {
                    evt.stopPropagation();

                    if (!of.recording) {
                        startRecorder(startDesktopRecorder);
                    } else {
                        stopRecorder();
                    }
                }
        });
    }

    function createBreakoutRoomsButton() {
        console.debug("breakoutRooms");
        addToolbarItem({
            id: 'ofmeet-breakout',
            icon: IMAGES.person,
            label: i18n('toolbar.createBreakoutRooms'),
            callback:
                (evt) => {
                    evt.stopPropagation();
                    doBreakout();
                    getOccupants();
                }
        });
    }

    function createTagsButton() {
        addToolbarItem({
            id: 'ofmeet-tags',
            icon: IMAGES.tags,
            label: i18n('toolbar.enableConferenceTags'),
            callback:
                (evt) => {
                    evt.stopPropagation();
                    doTags();
                }
        });
    }

    function createPadsButton() {
        addToolbarItem({
            id: 'ofmeet-pads',
            icon: IMAGES.cryptpad,
            label: i18n('toolbar.launchCryptPadApplication'),
            group: '.button-group-right',
            callback:
                (evt) => {
                    evt.stopPropagation();
                    doPads();
                }
        });
    }

    function createAvatarButton() {
        addToolbarItem({
            id: 'ofmeet-avatar',
            icon: IMAGES.picture,
            label: i18n('toolbar.changePersonalAvatar'),
            group: '.button-group-right',
            callback:
                (evt) => {
                    evt.stopPropagation();
                    doAvatar();
                }
        }).append('<input style="display:none" id="ofmeet-upload-avatar" type="file" name="files[]">');
    }

    function createShareCursorButton() {
        collab.init();

        addToolbarItem({
            id: 'ofmeet-cursor',
            icon: IMAGES.cursor,
            label: i18n('toolbar.shareCursorMousePointer'),
            group: '.button-group-right',
            callback:
                (evt) => {
                    evt.stopPropagation();

                    if (!cursorShared) {
                        $('#ofmeet-cursor').addClass('toggled');
                        collab.startSharing(Object.values(nickColors)[0]);
                    } else {
                        $('#ofmeet-cursor').removeClass('toggled');
                        collab.stopSharing();
                    }
                    cursorShared = !cursorShared;
                }
        });

        // Observe tile view status.
        // TODO: When the tile view change event is implemented in Jitsi Meet, it will be replaced with it.
        const localVideoTileViewContainer = document.getElementById('localVideoTileViewContainer')

        const observer = new MutationObserver(mutations => {
            APP.UI.emitEvent('UI.tile_view_changed', APP.store.getState()['features/video-layout'].tileViewEnabled);
        })

        observer.observe(localVideoTileViewContainer, {
            childList: true
        })
    }

    function createWhiteboardButton() {
        APP.conference.commands.addCommandListener("WHITEBOARD", function () {
            const url = interfaceConfig.OFMEET_WHITEBOARD_URL.endsWith("/") ? interfaceConfig.OFMEET_WHITEBOARD_URL + APP.conference.roomName : interfaceConfig.OFMEET_WHITEBOARD_URL + "/" + APP.conference.roomName;
            window.open(url, 'ofmeet-whiteboard');
        });

        addToolbarItem({
            id: 'ofmeet-whiteboard',
            icon: IMAGES.whiteboard,
            label: i18n('toolbar.shareaWhiteboard'),
            group: '.button-group-right',
            callback:
                (evt) => {
                    evt.stopPropagation();
                    APP.conference.commands.sendCommandOnce("WHITEBOARD", { value: !0 })
                }
        });
    }

    function createConfettiButton() {
        APP.conference.commands.addCommandListener("CONFETTI", e => {
            window.confetti(JSON.parse(e.value));
        });

        let sendConfettiCommand = (text) => {
            var options = {
                particleCount: 100,
                spread: 75,
                origin: {
                    x: 0.5 + (Math.random() - 0.5) * 0.1,
                    y: 0.6
                },
                angle: 90 + (Math.random() - 0.5) * 15,
                ticks: 250,
                scalar: 1.2,
                dispersion: 0.6
            };

            if (text) {
                options = {
                    ...options,
                    shapes: ['text:' + text]
                };
            } else if ((new Date()).getMonth() == 11) {
                options = {
                    ...options,
                    shapes: [
                        'text:\u2744', 'text:\u2744', 'text:\u2744', 'text:\u2744', 'text:\u2744', 'text:\u2744', 'text:\u2744', // snow flake
                        'text:' + String.fromCodePoint(0x1F381), // :gift:
                        'text:' + String.fromCodePoint(0x1F384), // :christmas_tree:
                        'text:' + String.fromCodePoint(0x1F385), // :santa:
                        'text:' + String.fromCodePoint(0x1F31F), // :star2:
                        'text:' + String.fromCodePoint(0x1F56F), // :candle:
                        'text:' + String.fromCodePoint(0x1F98C), // :deer:
                        'text:' + String.fromCodePoint(0x1F514)  // :bell:
                    ]
                };
            }

            APP.conference.commands.sendCommandOnce("CONFETTI", { value: JSON.stringify(options) })
        };

        let menu = undefined;
        if (interfaceConfig.OFMEET_CONFETTI_EMOTICON_ENABLED) {
            menu = {
                type: 'tile',
                items: [],
                closeOnClick: interfaceConfig.OFMEET_CONFETTI_EMOTICON_CLOSE_MENU,
                callback: (evt) => { sendConfettiCommand($(evt.target).text()) }
            };

            // unescape HTML entities 
            let emoticonList = $('<textarea />').html(interfaceConfig.OFMEET_CONFETTI_EMOTICON_LIST).text();
            for ( let emotiocon of Array.from(emoticonList)) {
                menu.items.push({icon: emotiocon});
            }
        }

        addToolbarItem({
            id: 'ofmeet-confetti',
            icon: IMAGES.confetti,
            label: i18n('toolbar.shareSomeConfetti'),
            group: '.button-group-right',
            callback: (evt) => { sendConfettiCommand() },
            menu: menu,
        });
    }

    function addToolbarItem(option) {
        option = {
            id: undefined,
            icon: undefined,
            label: undefined,
            group: '.button-group-left',
            callback: undefined,
            menu: {},
            ...option
        };

        const $placeHolder = $(option.group);

        if (option.id && option.icon && option.label && $placeHolder.length && option.callback) {
            let $button = $(`
	        <div aria-label="${option.label}" class="toolbox-button ofmeet-tooltip">
				<div id="${option.id}" class="toolbox-icon">
				    <div class="jitsi-icon" style="font-size: 12px;">${option.icon}</div>
				</div>
			</div>`);
            $button.children('.toolbox-icon').on('click.ofmeet-toolbox-icon', option.callback);

            let $toolbarItem = appendMenuToToolbarButton($button, option.menu);

            if ($placeHolder.hasClass('button-group-right')) {
                $placeHolder.children().last().before($toolbarItem);
            } else {
                $placeHolder.append($toolbarItem);
            }

            return $toolbarItem;
        }

        return $();
    }
    
    function appendMenuToToolbarButton($button, option) {
        option = {
            type: 'list',
            items: [],
            closeOnClick: true,
            callback: undefined,
            ...option
        };
        
        let menuClass = undefined;
        switch (option.type) {
            case 'list':
                menuClass = 'ofmeet-toolbox-list-menu';
                break;
            case 'tile':
                menuClass = 'ofmeet-toolbox-tile-menu';
                break;
        }

        if (menuClass && option.items.length && option.callback) {
            let $menuContainer = $(`<div class="ofmeet-toolbox-menu-container" style="display: none"><div class="ofmeet-toolbox-menu ${menuClass}"></div></div>`);
            let $menu = $('<ul class="overflow-menu"></ul>');
            for (let item of option.items) {
                let $item;
                switch (option.type) {
                    case 'list':
                        $item = $(`<li class="ofmeet-toolbox-menu-item"><span class="overflow-menu-item-icon"><div class="jitsi-icon">${item.icon}</div></span><span class="profile-text">${item.text}</span></li>`);
                        break;
                    case 'tile':
                        $item = $(`<li class="ofmeet-toolbox-menu-item"><div class="jitsi-icon">${item.icon}</div></li>`);
                        break;
                }

                if (item.attr) {
                    for (let key in item.attr) {
                        $item.attr(key, item.attr[key]);
                    }
                }
                $menu.append($item);
            }
            $menu.on('click.ofmeet-toolbox-menu', 'li', (e) => {option.callback(e); return option.closeOnClick; });
            $menuContainer.children('.ofmeet-toolbox-menu').append($menu);

            $smallIcon = $('<div class="ofmeet-toolbox-small-icon"><svg fill="none" height="9" width="9" viewBox="0 0 10 6"><path clip-rule="evenodd" d="M8.07.248a.75.75 0 111.115 1.004L5.656 5.193a.75.75 0 01-1.115 0L1.068 1.252A.75.75 0 012.182.248L5.1 3.571 8.07.248z"></path></svg></div>');
            $smallIcon.on('click.ofmeet-toolbox-small-icon', (e) => {
                let hideMenu = () => {
                    $menuContainer.hide();
                    $('#new-toolbox').off('hide.ofmeet-toolbox-menu');
                    $(window).off('click.ofmeet-toolbox-menu');
                };
                if ($menuContainer.css('display') == 'none') {
                    $menuContainer.show();
                    $(window).one('click.ofmeet-toolbox-menu', (e) => hideMenu());
                    $('#new-toolbox').one('hide.ofmeet-toolbox-menu', (e) => hideMenu());
                } else {
                    hideMenu();
                }
                return false;
            });

            return $('<div class="toolbox-button-wth-dialog"></div>').append($button.append($smallIcon), $menuContainer);
        }

        return $button;
    }

    //-------------------------------------------------------
    //  WORKAROUND: prevent disruption of a muted audio connection by a short toggle
    //-------------------------------------------------------

    function lostAudioWorkaround ()
    {
        if (APP.conference.isLocalAudioMuted() || audioTemporaryUnmuted)
        {
          APP.conference.toggleAudioMuted(false);
          audioTemporaryUnmuted = !audioTemporaryUnmuted;
          console.debug("audio " + (audioTemporaryUnmuted ? "temporary un" : "re") + "muted");
        }
        setTimeout(lostAudioWorkaround, audioTemporaryUnmuted ? 1000 : lostAudioWorkaroundInterval);
    }


    //-------------------------------------------------------
    //
    //  functions - vcard/avatar/bookmarks/occupants
    //
    //-------------------------------------------------------

    function addParticipant(id)
    {
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const participant = APP.conference.getParticipantById(id);

        console.debug("addParticipant", id, participant);

        if (participant && !participants[id] && breakout.kanban)
        {
            participants[id] = participant;
            const jid = participants[id]._jid;
            const label = participants[id]._displayName || 'Anonymous-' + id;

            breakout.kanban.addElement("participants",
            {
                id: id,
                title: label,
                drop: function(el) {
                  breakoutDragAndDrop(el);
                }
            });

            const ids = Object.getOwnPropertyNames(participants);
            document.getElementById('breakout-rooms').value = Math.round(ids.length / 2)
        }

        if (participant && id)
        {
            addParticipantDragDropHandlers(document.getElementById("participant_"+id));
        }
    }


    function addParticipantDragDropHandlers(element)
    {
        if (!element) return;
        element.setAttribute("draggable","true");
        element.addEventListener("dragstart",participantDragStart);
        element.addEventListener("dragover" ,participantDragOver);
        element.addEventListener("drop"     ,participantDrop);
    }

    function participantDragStart(event)
    {
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("application/x.id", event.target.id);
        console.debug('dragstart: source ', event.target.id);
    }

    function participantDragOver(event)
    {
        event.preventDefault();
        event.stopPropagation();
        event.dataTransfer.dropEffect = "move";
        // console.debug('dragover: destination ', this.id);
    }

    function participantDrop(event)
    {
        event.preventDefault();
        event.stopPropagation();
        const sourceId = event.dataTransfer.getData("application/x.id");
        const source = document.getElementById(sourceId);
        // const target = event.target; <- this points to any inner childs but not to the parent, use this instead
        const destination = this;
        console.debug('drop: swap ', sourceId, destination.id);
        swapNodes(source, destination);
    }

    function swapNodes(nodeA, nodeB) {
        const afterNodeB = nodeB.nextElementSibling;
        const parent = nodeB.parentNode;
        if (nodeA === afterNodeB) { // just below
            parent.insertBefore(nodeA, nodeB);
        } else {
            nodeA.replaceWith(nodeB);
            parent.insertBefore(nodeA, afterNodeB);
        }
    }


    function setOwnPresence()
    {
        const connection = APP.connection.xmpp.connection;
        const $pres = APP.connection.xmpp.connection.$pres;
        const Strophe = APP.connection.xmpp.connection.Strophe;

        connection.send($pres());
    }

    function getOccupants()
    {
        const connection = APP.connection.xmpp.connection;
        const $iq = APP.connection.xmpp.connection.$iq;
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const thisRoom = APP.conference._room.room.roomjid;

        const stanza = $iq({'to': thisRoom, 'type': 'get'}).c('query', { 'xmlns': "http://jabber.org/protocol/disco#items"});

        connection.sendIQ(stanza, function(iq) {

            iq.querySelectorAll('item').forEach(function(item)
            {
                console.debug("getOccupants", item);
                const id = Strophe.getResourceFromJid(item.getAttribute("jid"));
                addParticipant(id);
            });

        }, function(error){
            console.error("get occupants error", error);
        });
    }

    function getBookmarks()
    {
        const connection = APP.connection.xmpp.connection;
        const $iq = APP.connection.xmpp.connection.$iq;
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const thisRoom = APP.conference._room.room.roomjid;

        const stanza = $iq({'from': connection.jid, 'type': 'get'}).c('query', { 'xmlns': "jabber:iq:private"}).c('storage', { 'xmlns': 'storage:bookmarks' });

        connection.sendIQ(stanza, function(iq) {

            iq.querySelectorAll('conference').forEach(function(conference)
            {
                if (thisRoom == conference.getAttribute("jid"))
                {
                    const ofmeet_recording = conference.getAttribute("ofmeet_recording");
                    const ofmeet_tags = conference.getAttribute("ofmeet_tags");
                    const ofmeet_cryptpad = conference.getAttribute("ofmeet_cryptpad");
                    const ofmeet_captions = conference.getAttribute("ofmeet_captions");
                    const ofmeet_transcription = conference.getAttribute("ofmeet_transcription");
                    const ofmeet_uploads = conference.getAttribute("ofmeet_uploads");
                    const ofmeet_breakout = conference.getAttribute("ofmeet_breakout");

                    // TODO - This cannot be used until Jitsi-Meet is in an iframe and loaded after bookmarks are fetched
                }
            });

        }, function(error){
            console.error("bookmarks error", error);
        });
    }

    function getVCard()
    {
        const connection = APP.connection.xmpp.connection;
        const $iq = APP.connection.xmpp.connection.$iq;
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const username = Strophe.getNodeFromJid(APP.connection.xmpp.connection.jid);

        const iq = $iq({type: 'get', to: Strophe.getBareJidFromJid(APP.connection.xmpp.connection.jid)}).c('vCard', {xmlns: 'vcard-temp'});

        connection.sendIQ(iq, function(response)
        {
            const emailTag = response.querySelector('vCard EMAIL USERID');
            const email = emailTag ? emailTag.innerHTML : "";

            const fullnameTag = response.querySelector('vCard FN');
            const fullname = fullnameTag ? fullnameTag.innerHTML : username;
            const photo = response.querySelector('vCard PHOTO');

            let avatar = (fullname == "") ? createAvatar(username) : createAvatar(fullname);

            if (photo)
            {
                const type = photo.querySelector('TYPE').innerHTML;
                const binval = photo.querySelector('BINVAL').innerHTML;
                if (type != "" && binval != "")
                {
                    avatar = 'data:' + type + ';base64,' + binval;
                    console.debug("getVCard set avatar from photo", avatar);
                }
            }

            console.debug("getVCard", email, fullname, username, avatar);

            APP.conference.changeLocalAvatarUrl(avatar);

            if (email != "") APP.conference.changeLocalEmail(email);
            if (fullname != "") APP.conference.changeLocalDisplayName(fullname);

        }, function(error) {
            console.error(error);
        });
    }

    function createAvatar(nickname, width, height, font)
    {
        console.debug("createAvatar", width, height, font);

        if (!width) width = 128;
        if (!height) height = 128;
        if (!font) font = "64px Arial";

        const canvas = document.createElement('canvas');
        canvas.style.display = 'none';
        canvas.width = width;
        canvas.height = height;
        document.body.appendChild(canvas);
        const context = canvas.getContext('2d');
        context.fillStyle = getRandomColor(nickname);
        context.fillRect(0, 0, canvas.width, canvas.height);
        context.font = font;
        context.fillStyle = "#fff";
        context.textAlign = "center";

        if (nickname)
        {
            // try to split nickname into words at different symbols with preference
            nickname = nickname.replace(/\s/g, ' '); // replace multibyte space with ASCII space
            let words = nickname.split(/[, ]/); // "John W. Doe" -> "John "W." "Doe"  or  "Doe,John W." -> "Doe" "John" "W."
            if (words.length == 1) words = nickname.split("."); // "John.Doe" -> "John" "Doe"  or  "John.W.Doe" -> "John" "W" "Doe"
            if (words.length == 1) words = nickname.split("-"); // "John-Doe" -> "John" "Doe"  or  "John-W-Doe" -> "John" "W" "Doe"

            if (words && words[0] && words.first != '')
            {
                const firstInitial = words[0][0]; // first letter of first word
                var lastInitial = null; // first letter of last word, if any

                const lastWordIdx = words.length - 1; // index of last word
                if (lastWordIdx > 0 && words[lastWordIdx] && words[lastWordIdx] != '')
                {
                    lastInitial = words[lastWordIdx][0]; // first letter of last word
                }

                // if nickname consist of more than one words, compose the initials as two letter
                var initials = firstInitial;
                if (lastInitial) {
                    // if any comma is in the nickname, treat it to have the lastname in front, i.e. compose reversed
                    initials = nickname.indexOf(",") == -1 ? firstInitial + lastInitial : lastInitial + firstInitial;
                }
                
                var metrics = context.measureText(initials.toUpperCase());
                context.fillText(initials.toUpperCase(), width / 2, (height - metrics.actualBoundingBoxAscent - metrics.actualBoundingBoxDescent) / 2 + metrics.actualBoundingBoxAscent);
            }
        }

        document.body.removeChild(canvas);
        return canvas.toDataURL();
    }

    function getRandomColor(nickname)
    {
        if (nickColors[nickname])
        {
            return nickColors[nickname];
        }
        else {
            var letters = '0123456789ABCDEF';
            var color = '#';

            for (var i = 0; i < 6; i++) {
                color += letters[Math.floor(Math.random() * 16)];
            }
            nickColors[nickname] = color;
            return color;
        }
    }

    //-------------------------------------------------------
    //
    //  functions - record, tags and pads
    //
    //-------------------------------------------------------

    function hideClock()
    {
        document.getElementById("clocktext").style.display = "none";
    }

    function showClock()
    {
        const textElem = document.getElementById("clocktext");
        textElem.style.display = "";

        function pad(val) {
          return (10 > val ? "0" : "") + val;
        }

        function updateClock() {
            let totalSeconds = parseInt((Date.now() - clockTrack.joins) / 1000);

            const secs = pad(totalSeconds % 60);
            const mins = pad(parseInt((totalSeconds / 60) % 60));
            const hrs = pad(parseInt((totalSeconds / 3600) % 24, 10));

            textElem.textContent = hrs + ":" + mins + ":" + secs;
            setTimeout(updateClock, 1000);
        }

        updateClock();
    }

    function addPad(text)
    {
        console.debug("addPad", text);

        const container = document.querySelector(".crypt-pads");
        const values =  text.split('/');
        const html = '<span class="pade-col-content">' + IMAGES[values[3]] + '</span><span class="pade-col-content">' + values[8] + '<br/>' + values[3] + '</span>';
        const ele = document.createElement('li');
        ele.innerHTML = html;
        ele.classList.add("pade-col");
        ele.setAttribute("data-url", text);
        ele.setAttribute("data-type", values[3]);
        container.appendChild(ele);
    }

    function doPads()
    {
        const template =
            '<div class="modal-header">' +
            '    <h4 class="modal-title">CryptPad</h4>' +
            '</div>' +
            '<div class="modal-body">' +
            '    <div class="pade-col-container crypt-pads">' +
            '        <li class="pade-col " data-type="pad">' +
            '            <span class="pade-col-content">' + IMAGES.pad + '</span>' +
            '            <span class="pade-col-content">New Rich Text</span>' +
            '        </li>' +
            '        <li class="pade-col " data-type="sheet">' +
            '            <span class="pade-col-content">' + IMAGES.sheet + '</span>' +
            '            <span class="pade-col-content">New Sheet</span>' +
            '        </li>' +
            '        <li class="pade-col " data-type="code">' +
            '            <span class="pade-col-content">' + IMAGES.code + '</span>' +
            '            <span class="pade-col-content">New Code</span>' +
            '        </li>' +
            '        <li class="pade-col " data-type="slide">' +
            '            <span class="pade-col-content">' + IMAGES.slide + '</span>' +
            '            <span class="pade-col-content">New Presentation</span>' +
            '        </li><li class="pade-col " data-type="poll">' +
            '            <span class="pade-col-content">' + IMAGES.poll + '</span>' +
            '            <span class="pade-col-content">New Poll</span>' +
            '        </li><li class="pade-col " data-type="kanban">' +
            '            <span class="pade-col-content">' + IMAGES.kanban + '</span>' +
            '            <span class="pade-col-content">New Kanban</span>' +
            '        </li><li class="pade-col " data-type="whiteboard">' +
            '            <span class="pade-col-content">' + IMAGES.whiteboard + '</span>' +
            '            <span class="pade-col-content">New Whiteboard</span>' +
            '        </li>' +
            '   </div>' +
            '</div>'

        if (!padsModal)
        {
            const largeVideo = document.querySelector("#largeVideo");
            const display = largeVideo.style.display;

            padsModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['custom-class-1', 'custom-class-2'],

                beforeOpen: function() {
                    console.debug("beforeOpen", padsModalOpened, padsList);

                    if (!padsModalOpened)
                    {
                        padsList.forEach(function(text)
                        {
                            addPad(text);
                        });

                        const container = document.querySelector(".crypt-pads");

                        container.addEventListener("click", function(evt)
                        {
                            evt.stopPropagation();
                            const type = evt.target.parentNode.getAttribute("data-type");
                            let url = evt.target.parentNode.getAttribute("data-url");

                            if (type)
                            {
                                console.debug("beforeOpen - click", type);
                                if (!url) url = interfaceConfig.OFMEET_CRYPTPAD_URL + "/" + type + "/";
                                openPad(url);
                            }
                        });

                        padsModalOpened = true;
                    }
                }
            });
            padsModal.addFooterBtn('Share Clipboard', 'btn btn-success tingle-btn tingle-btn--primary', function() {
                navigator.clipboard.readText().then(function(clipText)
                {
                    console.debug("doPads", clipText);

                    padsModal.close();

                    //APP.UI.toggleChat();
                    APP.conference._room.sendTextMessage(clipText)
                    APP.UI.messageHandler.notify("Share Clipboard", "Clipboard shared with other participants");
                });
            });

            padsModal.addFooterBtn('Close', 'btn btn-success tingle-btn tingle-btn--primary', function() {
                padsModal.close();
            });

            padsModal.addFooterBtn('Quit', 'btn btn-danger tingle-btn tingle-btn--danger', function() {
                event.preventDefault();
                padsModal.close();

                const padContent = document.querySelector("#ofmeet-content");
                if (padContent) padContent.parentNode.removeChild(padContent);
                if (largeVideo) largeVideo.style.display = display;
            });

            padsModal.setContent(template);
        }

        padsModal.open();
    }

    function openPad(url)
    {
        console.debug("openPad", url);

        const padContent = document.querySelector("#ofmeet-content");

        if (padContent)
        {
            padsModal.close();
            APP.UI.messageHandler.notify("CryptPad", "Quit active pad before opening a new one");
        }
        else {
            const largeVideo = document.querySelector("#largeVideo");
            const iframe = largeVideo.cloneNode(false);
            largeVideo.parentNode.appendChild(iframe);
            largeVideo.style.display = "none";

            iframe.requestFullscreen();
            iframe.outerHTML = '<iframe src=' + url + ' id="ofmeet-content" style="width: 90%; height: 92%; border: 0;padding-left: 0px; padding-top: 0px;">'

            const largeVideoElementsContainer = document.querySelector("#largeVideoElementsContainer");
            largeVideoElementsContainer.style.visibility = "initial";
            largeVideoElementsContainer.style.opacity = "1";

            const cryptpad = document.querySelector('#ofmeet-content');

            cryptpad.addEventListener("load", function (evt)
            {
                console.debug("loading pad - ", this);
                padsModal.close();
            });
        }
    }

    function notifyText(message, title, notifyId, callback)
    {
        console.debug("notifyText", title, message, notifyId);

        if (!notifyId) notifyId = Math.random().toString(36).substr(2,9);

        const prompt = new Notification(title,
        {
            body: message,
            requireInteraction: true
        });

        prompt.onclick = function(event)
        {
            event.preventDefault();
            if (callback) callback(notifyId, 0);
        }

        prompt.onclose = function(event)
        {
            event.preventDefault();
            if (callback) callback(notifyId, 1);
        }
    }

    function doTags()
    {
        const template =
        '    <!-- Modal Header -->' +
        '    <div class="modal-header">' +
        '      <h4 class="modal-title">' + i18n('tag.conferenceCaptionsSubTitles') + '</h4>' +
        '    </div>' +

        '    <!-- Modal body -->' +
        '    <div class="modal-body">' +
        '       <div class="form-group">' +
        '       <label for="tags-location" class="col-form-label">' + i18n('tag.location') + ':</label>' +
        '       <input id="tags-location" type="text" class="form-control" name="tag-location" value="' + tags.location + '"/>' +
        '       <label for="tags-date" class="col-form-label">' + i18n('tag.date') + ':</label>' +
        '       <input id="tags-date" type="text" class="form-control" name="tags-date"/>' +
        '       <label for="tags-subject" class="col-form-label">' + i18n('tag.subject') + ':</label>' +
        '       <input id="tags-subject" type="text" class="form-control" name="tags-subject" value="' + tags.subject + '"/>' +
        '       <label for="tags-host" class="col-form-label">' + i18n('tag.host') + ':</label>' +
        '       <input id="tags-host" type="text" class="form-control" name="tags-host" value="' + tags.host + '"/>' +
        '       <label for="tags-activity" class="col-form-label">' + i18n('tag.activity') + ':</label>' +
        '       <input id="tags-activity" type="text" class="form-control" name="tags-activity" value="' + tags.activity + '"/>' +
        '       </div>' +
        '    </div>'

        if (!tagsModal)
        {
            tagsModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['custom-class-1', 'custom-class-2'],
                onOpen: function() {
                    console.debug('tags modal open');
                },
                onClose: function() {
                    console.debug('tags modal closed');
                },
                beforeOpen: function() {
                    document.getElementById('tags-date').value = localizedDate(new Date()).format('LL')
                },
                beforeClose: function() {
                    tags.location = document.getElementById('tags-location').value;
                    tags.date = document.getElementById('tags-date').value;
                    tags.subject = document.getElementById('tags-subject').value;
                    tags.host = document.getElementById('tags-host').value;
                    tags.activity = document.getElementById('tags-activity').value;

                    if (tags.location != "")
                    {
                        document.getElementById("subtitles").innerHTML =
                            `<b>` + i18n('tag.location') + `</b>: ${tags.location} <br/><b>` +
                            i18n('tag.date') + `</b>: ${tags.date} <br/><b>` +
                            i18n('tag.subject') + `</b>: ${tags.subject} <br/><b>` +
                            i18n('tag.host') + `</b>: ${tags.host} <br/><b>` +
                            i18n('tag.activity') + `</b>: ${tags.activity}`;
                    }
                    return true;
                }
            });
            tagsModal.setContent(template);

            tagsModal.addFooterBtn(i18n('tag.save'), 'btn btn-success tingle-btn tingle-btn--primary', function() {
                // here goes some logic
                tagsModal.close();
            });

            tagsModal.addFooterBtn(i18n('tag.cancel'), 'btn btn-danger tingle-btn tingle-btn--danger', function() {
                event.preventDefault();
                tags = {location: "", date: localizedDate(new Date()).format('LL'), subject: "", host: "", activity: ""};

                document.getElementById('tags-location').value = tags.location;
                document.getElementById('tags-date').value = tags.date;
                document.getElementById('tags-subject').value = tags.subject;
                document.getElementById('tags-host').value = tags.host;
                document.getElementById('tags-activity').value = tags.activity;

                document.getElementById("subtitles").innerHTML =  "";
                tagsModal.close();
            });

            const msgCaptions = (captions.msgsDisabled ? i18n('tag.enableMessageCaptions') : i18n('tag.disableMessageCaptions'));
            const msgClass = (captions.msgsDisabled ? 'btn-secondary' : 'btn-success') + ' btn tingle-btn tingle-btn--pull-right';

            if (interfaceConfig.OFMEET_SHOW_CAPTIONS)
            {
                tagsModal.addFooterBtn(msgCaptions, msgClass, function(evt) {
                    captions.msgsDisabled = !captions.msgsDisabled;
                    evt.target.classList.remove(captions.msgsDisabled ? 'btn-success' : 'btn-secondary');
                    evt.target.classList.add(captions.msgsDisabled ? 'btn-secondary' : 'btn-success');
                    evt.target.innerHTML = (captions.msgsDisabled ? i18n('tag.enableMessageCaptions') : i18n('tag.disableMessageCaptions'));
                    if (captions.ele) captions.ele.innerHTML = "";
                });
            }

            if (of.recognition)
            {
                const transcriptCaptions = (captions.transcriptDisabled ? i18n('tag.enableVoiceTranscription') : i18n('tag.disableVoiceTranscription'));
                const transcriptClass = (captions.transcriptDisabled ? 'btn-secondary' : 'btn-success') + ' btn tingle-btn tingle-btn--pull-right';

                tagsModal.addFooterBtn(transcriptCaptions, transcriptClass, function(evt)
                {
                    captions.transcriptDisabled = !captions.transcriptDisabled;
                    of.recognitionActive = !captions.transcriptDisabled;
                    evt.target.classList.remove(captions.transcriptDisabled ? 'btn-success' : 'btn-secondary');
                    evt.target.classList.add(captions.transcriptDisabled ? 'btn-secondary' : 'btn-success');
                    evt.target.innerHTML = (captions.transcriptDisabled ? i18n('tag.enableVoiceTranscription') : i18n('tag.disableVoiceTranscription'));

                    if (captions.transcriptDisabled) of.recognition.stop();
                    if (!captions.transcriptDisabled) of.recognition.start();
                    if (captions.ele) captions.ele.innerHTML = "";
                });
            }
        }

        tagsModal.open();
    }


    function getFilename(prefix, suffix)
    {
        return  prefix + "-" +
                (tags.location != "" ? tags.location + "-" : "") +
                tags.date.replace(/\//g, '')  + "-" +
                (tags.subject != "" ? tags.subject + "-" : "") +
                (tags.host != "" ? tags.host + "-" : "") +
                (tags.activity != "" ? tags.activity : "") +
                Math.random().toString(36).substr(2,9) +
                suffix;
    }

    function addTagsToImage(bitmap, callback)
    {
        const font = "20px Arial";
        const canvas = document.createElement('canvas');
        canvas.width = bitmap.width;
        canvas.height = bitmap.height;

        canvas.style.display = 'none';
        document.body.appendChild(canvas);

        const context = canvas.getContext('2d');
        context.drawImage(bitmap, 0, 0);

        if (tags.location != "")
        {
            context.font = font;
            context.fillStyle = "#fff";
            context.fillText(i18n('takePhoto.handsRaised') + ": " + handsRaised, 50, 25);
            context.fillText(i18n('takePhoto.location') + ": " + tags.location, 50, 50);
            context.fillText(i18n('takePhoto.date') + ": " +  tags.date, 50, 75);
            context.fillText(i18n('takePhoto.subject') + ": " +  tags.subject, 50, 100);
            context.fillText(i18n('takePhoto.host') + ": " +  tags.host, 50, 125);
            context.fillText(i18n('takePhoto.activity') + ": " +  tags.activity, 50, 150);
        }

        canvas.toBlob(function(blob)
        {
            callback(blob);
            setTimeout(function() {document.body.removeChild(canvas)});
        });
    }

    function createPhotoViewerHTML()
    {
        console.debug("ofmeet.js createPhotoViewerHTML");

        let imagenames = {};
        const me = APP.conference.getMyUserId();
        const html = ['<html><head><style> img { float: left; } img:first-child:nth-last-child(1) { width: 100%;} img:first-child:nth-last-child(2), img:first-child:nth-last-child(2) ~ img { width: 50%;} img:first-child:nth-last-child(3), img:first-child:nth-last-child(3) ~ img, img:first-child:nth-last-child(4), img:first-child:nth-last-child(4) ~ img { width: 50%;} img:first-child:nth-last-child(5), img:first-child:nth-last-child(5) ~ img, img:first-child:nth-last-child(6), img:first-child:nth-last-child(6) ~ img, img:first-child:nth-last-child(7), img:first-child:nth-last-child(7) ~ img, img:first-child:nth-last-child(8), img:first-child:nth-last-child(8) ~ img, img:first-child:nth-last-child(9), img:first-child:nth-last-child(9) ~ img { width: 33.33%; } </style></head><body>'];
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function(id)
        {
            imagenames[id] = getFilename("ofmeet-" + id, ".png");
            html.push('\n<img id="' + id + '" src="' + imagenames[id] + '"/>');
        });

        html.push('</body></html>');

        const blob = new Blob(html, {type: "text/plain;charset=utf-8"});
        const htmlFile = getFilename("ofmeet-photo-" + me, ".html");
        createAnchor(htmlFile, blob);
        return imagenames;
    }


    function takePhoto()
    {
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);
        const imagenames = createPhotoViewerHTML();

        console.debug("ofmeet.js takePhoto", ids, imagenames);

        ids.forEach(function(id)
        {
            const track = recordingVideoTrack[id].clone().getVideoTracks()[0];
            const imageCapture = new ImageCapture(track);

            imageCapture.grabFrame().then(function(bitmap)
            {
                addTagsToImage(bitmap, function(blob)
                {
                    console.debug("ofmeet.js takePhoto with tags", blob);
                    createAnchor(imagenames[id], blob);
                });
            })
        });
    }

    function stopRecorder()
    {
        console.debug("ofmeet.js stopRecorder");

        $('#ofmeet-record svg').css('fill', '#fff');
        $('#ofmeet-desktop svg').css('fill', '#fff');

        clockTrack.stop = (new Date()).getTime();
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function(id)
        {
            if (videoRecorder[id]) videoRecorder[id].stop();
        });

        if (!config.ofmeetLiveStream)
		{
			createVideoViewerHTML();
			APP.UI.messageHandler.notify("Recording", "Conference recording stopped");			
		}
		else {
			APP.UI.messageHandler.notify("Streaming", "Conference streaming stopped");			
		}
        of.recording = false;
    }

    function createAnchor(filename, blob)
    {
        const anchor = document.createElement('a');
        anchor.href = window.URL.createObjectURL(blob);
        anchor.style = "display: none;";
        anchor.download = filename;
        document.body.appendChild(anchor);
        anchor.click();
        window.URL.revokeObjectURL(anchor.href);
    }

    function createVttDataUrl()
    {
        console.debug("ofmeet.js createVttDataUrl");

        function pad(val) {
          return (10 > val ? "0" : "") + val;
        }

        function getTimeStamp(secs)
        {
            const secondsLabel = pad(secs % 60);
            const minutesLabel = pad(parseInt((secs / 60) % 60));
            const hoursLabel = pad(parseInt((secs/3600) % 24, 10));
            return hoursLabel + ":" + minutesLabel + ":" + secondsLabel;
        }

        const vtt = ["WEBVTT", "00:00:00.000 --> 24:00:00.000 position:10% line:1% align:left size:100%"];

        if (tags.location != "")
        {
            vtt.push("<b>Location</b>: " + tags.location);
            vtt.push("<b>Date</b>: " +  tags.date);
            vtt.push("<b>Subject</b>: " +  tags.subject);
            vtt.push("<b>Host</b>: " +  tags.host);
            vtt.push("<b>Activity</b>: " +  tags.activity);
        }

        let recordSeconds = 0;
        let totalSeconds = parseInt((clockTrack.start - clockTrack.joins) / 1000);

        for (let i=clockTrack.start; i<clockTrack.stop; i+=1000 )
        {
            ++totalSeconds;
            ++recordSeconds;

            const timestamp = getTimeStamp(recordSeconds);

            vtt.push(timestamp + ".000 --> " + timestamp + ".999 position:10% line:-10% align:left size:100%");
            vtt.push(getTimeStamp(totalSeconds));
        }

        captions.msgs.forEach(function(msg)
        {
            const seconds = parseInt((msg.stamp - clockTrack.start) / 1000);

            if (seconds > 0)
            {
                const start = getTimeStamp(seconds);
                const end = getTimeStamp(seconds + 3);
                vtt.push(start + ".000 --> " + end + ".999 position:30% line:-10% align:left size:100%");
                vtt.push(msg.text);
            }
        });

        console.debug("ofmeet.js createVttDataUrl", vtt);
        const url = "data:application/json;base64," + btoa(vtt.join('\n'))
        return url
    }

    function createVideoViewerHTML()
    {
        console.debug("ofmeet.js createVideoViewerHTML");

        const vttUrl = createVttDataUrl();
        const html = ['<html><head><style> video { float: left; } video::cue {font-size: 20px; color: #FFF; opacity: 1;} video:first-child:nth-last-child(1) { width: 100%; height: 100%; } video:first-child:nth-last-child(2), video:first-child:nth-last-child(2) ~ video { width: 50%; height: 100%; } video:first-child:nth-last-child(3), video:first-child:nth-last-child(3) ~ video, video:first-child:nth-last-child(4), video:first-child:nth-last-child(4) ~ video { width: 50%; height: 50%; } video:first-child:nth-last-child(5), video:first-child:nth-last-child(5) ~ video, video:first-child:nth-last-child(6), video:first-child:nth-last-child(6) ~ video, video:first-child:nth-last-child(7), video:first-child:nth-last-child(7) ~ video, video:first-child:nth-last-child(8), video:first-child:nth-last-child(8) ~ video, video:first-child:nth-last-child(9), video:first-child:nth-last-child(9) ~ video { width: 33.33%; height: 33.33%; } </style></head><body>'];
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function(id)
        {
            html.push('\n<video id="' + id + '" controls preload="metadata" src="' + filenames[id] + '"><track default src="' + vttUrl + '"></video>');
        });

        html.push('\n<script>');
        html.push('\n window.addEventListener("load", function() {');

        for (let z=0; z<ids.length; z++)
        {
            html.push('\n   var v' + z + ' = document.getElementById("' + ids[z] + '");');

            html.push('\n    v' + z + '.addEventListener("play", function() {');
            for (let i=0; i<ids.length; i++)
            {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").play();');
            }
            html.push('\n    });');

            html.push('\n    v' + z + '.addEventListener("playing", function() {');
            for (let i=0; i<ids.length; i++)
            {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").play();');
            }
            html.push('\n    });');

            html.push('\n    v' + z + '.addEventListener("pause", function() {');
            for (let i=0; i<ids.length; i++)
            {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").pause();');
            }
            html.push('\n    });');

            html.push('\n    v' + z + '.addEventListener("ended", function() {');
            for (let i=0; i<ids.length; i++)
            {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").pause();');
            }
            html.push('\n    });');

            if (z == 0)
            {
                html.push('\n    v' + z + '.addEventListener("seeking", function() {');
                for (let i=0; i<ids.length; i++)
                {
                    if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").currentTime = v' + z + '.currentTime;');
                }
                html.push('\n    });');

                html.push('\n    v' + z + '.addEventListener("seeked", function() {');
                for (let i=0; i<ids.length; i++)
                {
                    if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").currentTime = v' + z + '.currentTime;');
                }
                html.push('\n    });');
            }
        }
        html.push('\n });');
        html.push('\n</script></body></html>');

        const me = APP.conference.getMyUserId();
        const blob = new Blob(html, {type: "text/plain;charset=utf-8"});
        const htmlFile = getFilename("ofmeet-video-" + me, ".html");
        createAnchor(htmlFile, blob);
    }

    function startRecorder(recorder)
    {
		navigator.mediaDevices.getUserMedia({ audio: true, video: true }).then(function (stream) {
			recordingVideoTrack[APP.conference.getMyUserId()] = stream;
			recordingAudioTrack[APP.conference.getMyUserId()] = stream;
			recorder();
		});		
	}
	
    function startMeetingRecorder()
    {
        console.debug("ofmeet.js startMeetingRecorder");

        $('#ofmeet-record svg').css('fill', '#f00');
        APP.UI.messageHandler.notify("Recording", "Conference Recording Started");

        captions.msgs = [];
        clockTrack.start = (new Date()).getTime();
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function(id)
        {
            filenames[id] = getFilename("ofmeet-video-" + id, ".webm");
            recorderStreams[id] = new MediaStream();

            recorderStreams[id].addEventListener('addtrack', (event) =>
            {
              console.debug(`ofmeet.js new ${event.track.kind} track added`);
            });

            recorderStreams[id].addTrack(recordingVideoTrack[id].clone().getVideoTracks()[0]);
            recorderStreams[id].addTrack(recordingAudioTrack[id].clone().getAudioTracks()[0]);

            console.debug("ofmeet.js startMeetingRecorder stream", id, recorderStreams[id], recorderStreams[id].getVideoTracks()[0].getSettings());

            const dbname = 'ofmeet-db-' + id;
            dbnames.push(dbname);

            customStore[id] = new idbKeyval.Store(dbname, dbname);
            videoRecorder[id] = new MediaRecorder(recorderStreams[id], { mimeType: 'video/webm'});

            videoRecorder[id].ondataavailable = function(e)
            {
                if (e.data.size > 0)
                {
                    const key = "video-chunk-" + (new Date()).getTime();

                    idbKeyval.set(key, e.data, customStore[id]).then(function()
                    {
                        console.debug("ofmeet.js startMeetingRecorder - ondataavailable", id, key, e.data);

                    }).catch(function(err) {
                        console.error('ofmeet.js startMeetingRecorder - ondataavailable failed!', err)
                    });
                }
            }

            videoRecorder[id].onstop = function(e)
            {
                recorderStreams[id].getTracks().forEach(track => track.stop());

                idbKeyval.keys(customStore[id]).then(function(data)
                {
                    const duration = Date.now() - startTime;
                    const blob = new Blob(data, {type: 'video/webm'});

                   console.debug("ofmeet.js startMeetingRecorder - onstop", id, filenames[id], duration, data, blob);

                    ysFixWebmDuration(blob, duration, function(fixedBlob) {
                        createAnchor(filenames[id], fixedBlob);
                        idbKeyval.clear(customStore[id]);

                        delete filenames[id];
                        delete videoRecorder[id];
                        delete recorderStreams[id];
                        delete customStore[id];
                    });
                });
            }
            videoRecorder[id].start(1000);
            const startTime = Date.now();
        });

        of.recording = true;
    }

    function mergeAudioStreams(desktopStream, voiceStream)
    {
        const context = new AudioContext();
        const destination = context.createMediaStreamDestination();
        let hasDesktop = false;
        let hasVoice = false;

        if (desktopStream && desktopStream.getAudioTracks().length > 0) {
          // If you don't want to share Audio from the desktop it should still work with just the voice.
          const source1 = context.createMediaStreamSource(desktopStream);
          const desktopGain = context.createGain();
          desktopGain.gain.value = 0.5;
          source1.connect(desktopGain).connect(destination);
          hasDesktop = true;
        }

        if (voiceStream && voiceStream.getAudioTracks().length > 0) {
          const source2 = context.createMediaStreamSource(voiceStream);
          const voiceGain = context.createGain();
          voiceGain.gain.value = 0.5;
          source2.connect(voiceGain).connect(destination);
          hasVoice = true;
        }

        return (hasDesktop || hasVoice) ? destination.stream.getAudioTracks() : [];
    }
	
	function connectLiveStream (url, streamKey)
	{
		const ws = new WebSocket(url, [streamKey]);

		ws.onopen = (event) => {
		  console.log(`Connection opened: ${JSON.stringify(event)}`);
		};

		ws.onclose = (event) => {
		  console.log(`Connection closed: ${JSON.stringify(event)}`);
		};

		ws.onerror = (event) => {
		  console.log(`An error occurred with websockets: ${JSON.stringify(event)}`);
		};
		return ws;
	}	

    function startDesktopRecorder()
    {
        console.debug("ofmeet.js startDesktopRecorder");
		
		const recConstraints = {video: true, audio: {autoGainControl: false, echoCancellation: false, googAutoGainControl: false, noiseSuppression: false}};
		const streamConstraints = {video: true, audio: true};
		
		if (config.ofmeetLiveStream)
		{
			if (!config.ofmeetStreamKey || config.ofmeetStreamKey.trim() === '') 
			{
				config.ofmeetStreamKey = localStorage.getItem("ofmeet.live.stream.key");
				
				if (!config.ofmeetStreamKey || config.ofmeetStreamKey.trim() === '') 
				{				
					config.ofmeetStreamKey = prompt(i18n('enterStreamKey'));

					if (!config.ofmeetStreamKey || config.ofmeetStreamKey.trim() === '')
						config.ofmeetLiveStream = false;
					else
						localStorage.setItem("ofmeet.live.stream.key", config.ofmeetStreamKey);							
				}
			}
		}			
		
        navigator.mediaDevices.getDisplayMedia(config.ofmeetLiveStream ? streamConstraints : recConstraints).then(stream =>
        {
            $('#ofmeet-desktop svg').css('fill', '#f00');

            captions.msgs = [];
            clockTrack.start = (new Date()).getTime();

            const id = APP.conference.getMyUserId();
            const tracks = [
              ...stream.getVideoTracks(),
              ...mergeAudioStreams(stream, recordingAudioTrack[id].clone())
            ];

            recorderStreams[id] =  new MediaStream(tracks);
			
			if (config.ofmeetLiveStream && APP.conference._room.isModerator())
			{
				const ws_url = config.websocket.split("/");
				console.debug('ofmeet.js startDesktopRecorder - live streaming', tracks, ws_url);
			
				let websocket = connectLiveStream("wss://" + ws_url[2] + "/livestream-ws/", config.ofmeetStreamKey);
				videoRecorder[id] = new MediaRecorder(recorderStreams[id], {mimeType: 'video/webm;codecs=h264', bitsPerSecond: 256 * 8 * 1024});

				videoRecorder[id].ondataavailable = function(e)
				{
					websocket.send(e.data);
				}

				videoRecorder[id].onstop = function(e)
				{
					websocket.close();
					websocket = null;
				}	

				APP.UI.messageHandler.notify("Streaming", "Conference streaming started");
				
			} else {
				filenames[id] = getFilename("ofmeet-video-" + id, ".webm");

				console.debug("ofmeet.js startDesktopRecorder stream", id, recorderStreams[id], recorderStreams[id].getVideoTracks()[0].getSettings());
				const dbname = 'ofmeet-db-' + id;
				dbnames.push(dbname);

				customStore[id] = new idbKeyval.Store(dbname, dbname);
				videoRecorder[id] = new MediaRecorder(recorderStreams[id], { mimeType: 'video/webm'});

				videoRecorder[id].ondataavailable = function(e)
				{
					if (e.data.size > 0)
					{
						const key = "video-chunk-" + (new Date()).getTime();

						idbKeyval.set(key, e.data, customStore[id]).then(function()
						{
							console.debug("ofmeet.js startDesktopRecorder - ondataavailable", id, key, e.data);

						}).catch(function(err) {
							console.error('ofmeet.js startDesktopRecorder - ondataavailable failed!', err)
						});
					}
				}

				videoRecorder[id].onstop = function(e)
				{
					recorderStreams[id].getTracks().forEach(track => track.stop());
					stream.getTracks().forEach(track => track.stop());

					idbKeyval.keys(customStore[id]).then(function(data)
					{
						const duration = Date.now() - startTime;
						const blob = new Blob(data, {type: 'video/webm'});

					   console.debug("ofmeet.js startDesktopRecorder - onstop", id, filenames[id], duration, data, blob);

						ysFixWebmDuration(blob, duration, function(fixedBlob) {
							createAnchor(filenames[id], fixedBlob);
							idbKeyval.clear(customStore[id]);

							delete filenames[id];
							delete videoRecorder[id];
							delete recorderStreams[id];
							delete customStore[id];
						});
					});
				}
				
				APP.UI.messageHandler.notify("Recording", "Conference recording started");				
			}
            videoRecorder[id].start(1000);
            const startTime = Date.now();
            of.recording = true;

        }, error => {
            console.error("ofmeet.js startDesktopRecorder", error);
            APP.UI.messageHandler.showError({title:"Desktop recorder/streamer", error, hideErrorSupportLink: true});			
            of.recording = false;
        });
    }

    function recoverRecording(dbname)
    {
        console.debug("recovering db " + dbname);

        dbnames.push(dbname);
        const store = new idbKeyval.Store(dbname, dbname);
        const filename = getFilename("ofmeet-video-" + dbname, ".webm");

        idbKeyval.keys(store).then(function(data)
        {
            console.debug("ofmeet.js recoverRecording", filename, data);

            const blob = new Blob(data, {type: 'video/webm'});
            createAnchor(filename, blob);

            idbKeyval.clear(store);
        });
    }

    //-------------------------------------------------------
    //
    //  Breakout Rooms
    //
    //-------------------------------------------------------

    function breakoutDragAndDrop(el)
    {
        if (!el.dataset.eid) return;

        const id = el.dataset.eid;
        const label = participants[id]._displayName || i18n('breakout.anonymous', {id: id});
        const jid = participants[id]._jid;
        const webinar = participants[id]._tracks.length > 0 ? "false" : "true";

        const boardId = breakout.kanban.getParentBoardID(id);

        if (boardId && jid)
        {
            if (boardId == "participants") return;

            const roomindex = boardId.substring(5);
            const roomid = breakout.rooms[parseInt(roomindex)]

            console.debug("breakoutDragAndDrop", id, roomindex, roomid, label, jid, webinar);

            el.setAttribute('data-jid', jid);
            el.setAttribute('data-label', label);
            el.setAttribute('data-roomid', roomid);
            el.setAttribute('data-roomindex', roomindex);
            el.setAttribute('data-webinar', webinar);
        }
        else {
            breakout.kanban.removeElement(id);
            breakout.kanban.addElement("participants",
            {
                id: id,
                title: label,
                drop: function(el) {
                  breakoutDragAndDrop(el);
                }
            });
        }
    }

    function handlePresence(presence)
    {
        //console.debug("handlePresence", presence);
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const id = Strophe.getResourceFromJid(presence.getAttribute("from"));
        const raisedHand = presence.querySelector("jitsi_participant_raisedHand");
        const email = presence.querySelector("email");

        if (raisedHand)
        {
            const ofHandRaised = raisedHand.innerHTML == "true";
            if (participants[id]) participants[id].ofHandRaised = ofHandRaised;
            handsRaised = handsRaised + (ofHandRaised ? +1 : ( handsRaised > 0 ? -1 : 0));
            const handsTotal = 1 + APP.conference.listMembers().length;
            const handsPercentage = Math.round(100*handsRaised/handsTotal);
            const label = handsRaised > 0 ? i18n('handsRaised.handsRaised', {raised: handsRaised, total: handsTotal, percentage: handsPercentage}) : "";
            if (captions.ele) captions.ele.innerHTML = label;
            captions.msgs.push({text: label, stamp: (new Date()).getTime()});
        }

        if (email)
        {
            if (participants[id])
            {
                participants[id].ofEmail = email.innerHTML;
                localStorage['pade.email.' + participants[id]._displayName] =  email.innerHTML;
            }
        }

        return true;
    }

    function handleMessage(msg)
    {
        if (!msg.getAttribute("type")) // alert message
        {
            const body = msg.querySelector('body');

            if (body)
            {
                console.debug("alert message", body.innerHTML);
                APP.UI.messageHandler.showError({title:"System Adminitrator", description: body.innerHTML, hideErrorSupportLink: true});
            }
        }

        return true;
    }

    function handleMucMessage(msg)
    {
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const participant = Strophe.getResourceFromJid(msg.getAttribute("from"));

        if (msg.getAttribute("type") == "error")
        {
            console.error(msg);
            return true;
        }

        const payload = msg.querySelector('json');

        if (payload)
        {
            const json = JSON.parse(payload.innerHTML);
            console.debug("handleMucMessage", participant, json);
            const label = json.action == 'breakout' ? 'breakout.joining' : 'breakout.leaving';

            APP.UI.messageHandler.notify(i18n('breakout.breakoutRooms'), i18n(label, {sec: breakout.wait}));
            setTimeout(function() {location.href = json.url}, breakout.wait * 1000);
        }

        return true;
    }

    function broadcastBreakout(type, jid, xmpp, json)
    {
        console.debug("broadcastBreakout", type, jid, xmpp, json);
        const $msg = APP.connection.xmpp.connection.$msg;
        xmpp.send($msg({type: type, to: jid}).c("json", {xmlns: "urn:xmpp:json:0"}).t(JSON.stringify(json)));
    }

    function exitRoom(jid)
    {
        console.debug("exitRoom", jid);
        const xmpp = APP.connection.xmpp.connection._stropheConn;
        const $pres = APP.connection.xmpp.connection.$pres;
        xmpp.send($pres({type: 'unavailable', to: jid + '/' + APP.conference.getLocalDisplayName()}));
    }

    function joinRoom(jid)
    {
        console.debug("joinRoom", jid);
        const xmpp = APP.connection.xmpp.connection._stropheConn;
        const $pres = APP.connection.xmpp.connection.$pres;
        const Strophe = APP.connection.xmpp.connection.Strophe;
        xmpp.send($pres({to: jid + '/' + APP.conference.getLocalDisplayName()}).c("x",{xmlns: Strophe.NS.MUC}));
    }

    function endBreakout()
    {
        if (breakout.started) toggleBreakout();
    }

    function startBreakout()
    {
        if (!breakout.started) toggleBreakout();
    }

    function toggleBreakout()
    {
        const Strophe = APP.connection.xmpp.connection.Strophe;
        const xmpp = APP.connection.xmpp.connection._stropheConn;
        const pos = location.href.lastIndexOf("/");
        const rootUrl = location.href.substring(0, pos);

        console.debug("toggleBreakout", rootUrl, breakout);

        if (!breakout.started)
        {
            breakout.recall = [];

            for (let i=0; i<breakout.roomCount; i++)
            {
                if (breakout.kanban.findBoard("room_" + i))
                {
                    const items = breakout.kanban.getBoardElements("room_" + i);
                    let json = null;

                    items.forEach(function(node)
                    {
                        const id = node.getAttribute("data-eid");
                        const webinar = node.getAttribute("data-webinar");
                        const room = node.getAttribute("data-roomid");
                        const label = node.getAttribute("data-label");
                        const jid = node.getAttribute("data-jid");
                        const url = rootUrl + '/' + room;

                        json = {action: 'breakout', id: id, room: room, label: label, jid: jid, url: url, return: location.href, webinar: webinar};
                        broadcastBreakout("chat", jid, xmpp, json);
                    });

                    breakout.recall.push(json);
                }
            }

            if (breakout.duration > 0)
            {
                breakout.timeout = setTimeout(toggleBreakout, 60000 * breakout.duration);
                breakoutStatus(i18n('breakout.breakoutStartedWithDuration', {min: breakout.duration}));
            }
            else {
                breakoutStatus(i18n('breakout.breakoutStarted'));
            }
        }
        else {
            for (let i=0; i<breakout.recall.length; i++)
            {
                const webinar = breakout.recall[i].webinar;
                const jid = breakout.recall[i].room + "@" + Strophe.getDomainFromJid(breakout.recall[i].jid);
                const json = {action: 'reassemble', jid: jid, url: location.href + '#config.webinar=' + webinar};

                joinRoom(jid);

                setTimeout(function()
                {
                    broadcastBreakout("groupchat", Strophe.getBareJidFromJid(jid), xmpp, json);
                    setTimeout(function() {exitRoom(jid)}, 1000);

                }, 1000);
            }

            breakoutStatus(i18n('breakout.breakoutHasEnded'));
            if (breakout.timeout) clearTimeout(breakout.timeout);
        }

        breakout.started = !breakout.started;
        breakout.button.classList.remove(breakout.started ? 'btn-success' : 'btn-secondary');
        breakout.button.classList.add(breakout.started ? 'btn-secondary' : 'btn-success');
        breakout.button.innerHTML = breakout.started ? i18n('breakout.reassemble') : i18n('breakout.breakout');
    }

    function messageBreakoutRooms(text)
    {
        console.debug("messageBreakoutRooms", text, breakout);

        const xmpp = APP.connection.xmpp.connection._stropheConn;
        const $msg = APP.connection.xmpp.connection.$msg;

        for (let i=0; i<breakout.recall.length; i++)
        {
            const jid = breakout.recall[i].room + "@" + Strophe.getDomainFromJid(breakout.recall[i].jid);

            joinRoom(jid);

            setTimeout(function()
            {
                xmpp.send($msg({type: 'groupchat', to: jid}).c("body").t(text));
                setTimeout(function() {exitRoom(jid)}, 1000);

            }, 1000);
        }
    }

    function allocateToRooms(roomCount)
    {
        console.debug("allocateToRooms", roomCount, breakout);

        for (let i=0; i<breakout.roomCount; i++)
        {
            if (breakout.kanban.findBoard("room_" + i))
            {
                breakout.kanban.removeBoard("room_" + i);
            }
        }

        breakout.kanban.removeBoard("participants");

        const boards = [{
            id: "participants",
            title: i18n('breakout.meetingParticipants'),
            class: "participants",
            item: []
        }]

        const ids = Object.getOwnPropertyNames(participants);

        for (let i=0; i<roomCount; i++)
        {
            boards[i+1] = {
                id: "room_" + i,
                title: i18n('breakout.room', {n: (i+1).toString()}),
                class: "room",
                item: []
            }

            breakout.rooms[i] = APP.conference.roomName + '-' + Math.random().toString(36).substr(2,9);

            for (let j=0; j<ids.length; j++)
            {
                if (j % roomCount == i) // allocate participant j to room i
                {
                    console.debug("allocateToRooms - participant", j, ids[j], participants[ids[j]]);

                    const label = participants[ids[j]]._displayName || i18n('breakout.anonymous', {id: id});
                    const jid = participants[ids[j]]._jid;
                    const webinar = participants[ids[j]]._tracks.length > 0 ? "false" : "true";

                    boards[i+1].item.push({
                        id: ids[j],
                        title: label,
                        label: label,
                        jid: jid,
                        webinar: webinar,
                        roomid: breakout.rooms[i],
                        roomindex: i,
                        drop: function(el) {
                          breakoutDragAndDrop(el);
                        }
                    });
                }
            };
        }

        breakout.kanban.addBoards(boards);
    }

    function visitBreakoutRoom(boardId)
    {
        console.debug("visitBreakoutRoom", boardId);
        const roomindex = boardId.substring(5);
        const roomid = breakout.rooms[parseInt(roomindex)];

        if (roomid)
        {
            const pos = location.href.lastIndexOf("/");
            const rootUrl = location.href.substring(0, pos);
            const url = rootUrl + '/' + roomid;

            open(url, roomid);
        }
    }

    function createBreakout()
    {
        const kanbanConfig =
        {
            element: ".breakout-kanban",
            gutter: "5px",
            widthBoard: "300px",
            dragBoards: false,
            itemHandleOptions:{
              enabled: true,
            },
            buttonClick: function(el, boardId) {
                console.debug("Board clicked", boardId);
                if (boardId != "participants") visitBreakoutRoom(boardId)
            },
            click: function(el) {
              console.debug("Trigger on all items click!");
            },
            dropEl: function(el, target, source, sibling){
              console.debug(target.parentElement.getAttribute('data-id'));
              console.debug(el, target, source, sibling)
            },
            addItemButton: true,
            boards: [
              {
                id: "participants",
                title: i18n('breakout.meetingParticipants'),
                class: "participants",
                dragTo: [],
                item: []
              }
            ]
        }

        const ids = Object.getOwnPropertyNames(participants);

        ids.forEach(function(id)
        {
            kanbanConfig.boards[0].item.push({
                id: id,
                title: participants[id]._displayName || i18n('breakout.anonymous', {id: id}),
                drop: function(el) {
                  breakoutDragAndDrop(el);
                }
            });
        });

        console.debug("createBreakout", kanbanConfig);
        breakout.kanban = new jKanban(kanbanConfig);
    }

    function breakoutStatus(text)
    {
        document.getElementById('breakout-status').innerHTML = text;
    }

    function doBreakout()
    {
        const ids = Object.getOwnPropertyNames(participants);
        const count = Math.round(ids.length / 2);

        const template =
            '<div class="modal-header">' +
            '    <h4 class="modal-title">' + i18n('breakout.breakoutRooms') + ' - ' + i18n('breakout.participants', {title: '<span id="breakout-title">' + ids.length + '</span>'}) + '</h4>' +
            '       <label for="breakout-duration" class="col-form-label">' + i18n('breakout.duration') + '</label>' +
            '       <input id="breakout-duration" type="number" min="0" max="480" step="30" name="breakout-duration" value="' + breakout.duration + '"/>' +
            '       <label for="breakout-rooms" class="col-form-label">' + i18n('breakout.rooms') + '</label>' +
            '       <input id="breakout-rooms" type="number" min="1" max="10" name="breakout-rooms" value="' + count + '"/>' +
            '       <div id="breakout-status" style="width:30%; color:red"></div>' +
            '</div>' +
            '<div class="modal-body">' +
            '    <div class="pade-col-container breakout-kanban"></div>' +
            '</div>'

        if (!breakoutModal)
        {
            breakoutModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['custom-class-1', 'custom-class-2'],

                beforeOpen: function() {
                    console.debug("beforeOpen", breakout);

                    if (!breakout.created)
                    {
                        createBreakout();
                        breakout.created = true;
                    }
                }
            });

            breakoutModal.addFooterBtn(i18n('breakout.close'), 'btn btn-danger tingle-btn tingle-btn--primary', function()
            {
                breakoutModal.close();
            });

            breakoutModal.addFooterBtn(i18n('breakout.allocate'), 'btn btn-success tingle-btn tingle-btn--primary', function()
            {
                const roomCount = parseInt(document.getElementById('breakout-rooms').value);
                const ids = Object.getOwnPropertyNames(participants);

                if (ids.length > 0 && roomCount > 0)
                {
                    allocateToRooms(roomCount);
                    breakoutStatus(i18n('breakout.allocatedMessage', {participants: ids.length, rooms: roomCount}));
                }
                else {
                    breakoutStatus(i18n('breakout.missingParticipants'));
                }

                breakout.roomCount = roomCount;
            });

            const label = breakout.started ? i18n('breakout.reassemble') : i18n('breakout.breakout');

            breakoutModal.addFooterBtn(label, 'btn btn-success tingle-btn tingle-btn--primary', function(evt)
            {
                breakout.button = evt.target;
                breakout.duration = parseInt(document.getElementById('breakout-duration').value);

                if (breakout.roomCount > 0)
                {
                    toggleBreakout();
                }
                else {
                    breakoutStatus(i18n('breakout.allocateParticipantsFirst'));
                }
            });

            breakoutModal.setContent(template);
        }
        else {
            document.getElementById('breakout-rooms').value = count;
            document.getElementById('breakout-duration').innerHTML = breakout.duration;
            document.getElementById('breakout-title').innerHTML = ids.length;
        }

        breakoutModal.open();
    }

    //-------------------------------------------------------
    //
    //  SpeechRecognition
    //
    //-------------------------------------------------------

    function sendSpeechRecognition(result)
    {
        if (result != "" && APP.conference && APP.conference._room && !captions.transcriptDisabled)
        {
            var message = "[" + result + "]";
            console.debug("Speech recog result", APP.conference._room, message);

            APP.conference._room.sendTextMessage(message);
            of.currentTranslation = [];
        }
    }

    function setupSpeechRecognition()
    {
        console.debug("setupSpeechRecognition");

        of.recognition = new webkitSpeechRecognition();
        of.recognition.lang = config.defaultLanguage;
        of.recognition.continuous = true;
        of.recognition.interimResults = false;

        of.recognition.onresult = function(event)
        {
            console.debug("Speech recog event", event)

            if(event.results[event.resultIndex].isFinal==true)
            {
                var transcript = event.results[event.resultIndex][0].transcript;
                console.debug("Speech recog transcript", transcript);
                sendSpeechRecognition(transcript);
            }
        }

        of.recognition.onspeechend  = function(event)
        {
            console.debug("Speech recog onspeechend", event);
        }

        of.recognition.onstart = function(event)
        {
            console.debug("Speech to text started", event);
            of.recognitionActive = true;
        }

        of.recognition.onend = function(event)
        {
            console.debug("Speech to text ended", event);

            if (of.recognitionActive)
            {
                console.debug("Speech to text restarted");
                setTimeout(function() {of.recognition.start()}, 1000);
            }
        }

        of.recognition.onerror = function(event)
        {
            console.debug("Speech to text error", event);
        }

        of.recognition.start();
    }

    //-------------------------------------------------------
    //
    //  File upload handler
    //
    //-------------------------------------------------------

    function doAvatar()
    {
        const upload = document.getElementById("ofmeet-upload-avatar");

        if (upload) upload.addEventListener('change', function(event)
        {
            uploadAvatar(event);
        });
        upload.click();
    }

    function uploadAvatar(event)
    {
        var files = event.target.files;

        for (var i = 0, file; file = files[i]; i++)
        {
            if (file.name.endsWith(".png") || file.name.endsWith(".jpg"))
            {
                var reader = new FileReader();

                reader.onload = function(event)
                {
                    dataUri = event.target.result;
                    console.debug("uploadAvatar", dataUri);
                    APP.conference.changeLocalAvatarUrl(dataUri);
                    window.localStorage["ofmeet.settings.avatar"] = JSON.stringify(dataUri);
                };

                reader.onerror = function(event) {
                    console.error("uploadAvatar - error", event);
                    APP.UI.messageHandler.notify("Avatar Upload", "Image file error " + event);
                };

                reader.readAsDataURL(file);

            } else {
                APP.UI.messageHandler.notify("Avatar Upload", "Image file must be a png or jpg file");
            }
        }
    }

    function handleDragOver(evt)
    {
        evt.stopPropagation();
        evt.preventDefault();
        evt.dataTransfer.dropEffect = 'copy';
    }

    function handleDropFileSelect(evt)
    {
        evt.stopPropagation();
        evt.preventDefault();

        var files = evt.dataTransfer.files;

        for (var i = 0, f; f = files[i]; i++)
        {
            uploadFile(f);
        }
    }

    function uploadFile(file)
    {
        console.debug("uploadFile", file);

        var getUrl = null;
        var putUrl = null;
        var errorText = null;

        const connection = APP.connection.xmpp.connection;
        const $iq = APP.connection.xmpp.connection.$iq;

        const iq = $iq({type: 'get', to: "httpfileupload." + connection.domain}).c('request', {xmlns: 'urn:xmpp:http:upload'}).c('filename').t(file.name).up().c('size').t(file.size);

        connection.sendIQ(iq, function(response)
        {
            response.querySelectorAll('slot').forEach(function(slot)
            {
                const putUrl = slot.querySelector('put').innerHTML;
                const getUrl = slot.querySelector('get').innerHTML;

                console.debug("uploadFile", putUrl, getUrl);

                if (putUrl != null & getUrl != null)
                {
                    var req = new XMLHttpRequest();

                    req.onreadystatechange = function()
                    {
                      if (this.readyState == 4 && this.status >= 200 && this.status < 400)
                      {
                        console.debug("uploadFile ok", this.statusText);
                        APP.conference._room.sendTextMessage(getUrl);
                      }
                      else

                      if (this.readyState == 4 && this.status >= 400)
                      {
                        console.error("uploadFile error", this.statusText);
                        APP.conference._room.sendTextMessage(this.statusText);
                       }

                    };
                    req.open("PUT", putUrl, true);
                    req.send(file);
                }
            });

        }, function(error) {
            console.error(error);
        });
    }

    //-------------------------------------------------------
    //
    //  push notification
    //
    //-------------------------------------------------------

    function getServiceWorker(callback)
    {
        if (swRegistration) callback(swRegistration);
        else {
            if ('serviceWorker' in navigator)
            {
                console.debug('Service Worker is supported');

                navigator.serviceWorker.register('./webpush-sw.js').then(function(registration)
                {
                    swRegistration = registration;
                    callback(registration);

                }).catch(function(error) {
                    console.error('Service Worker Error, cannot register service worker', error);
                    callback();
                });
            } else {
                console.warn('Service Worker is not supported');
                callback();
            }
        }
    }

    function setupPushNotification()
    {
        if ('PushManager' in window)
        {
            getServiceWorker(function(registration)
            {
                console.debug('Push notification is supported');

                if (registration) registration.pushManager.getSubscription().then(function(subscription)
                {
                    if (subscription && !localStorage["pade.vapid.keys"])
                    {
                        subscription.unsubscribe();
                        subscription = null;
                    }

                    if (!subscription) {
                        makeSubscription(function(err, subscription, keys)
                        {
                            if (err)
                            {
                                console.error('makeSubscription error, no push messaging', err);
                            }
                            else {
                                handleSubscription(subscription, keys);
                            }
                        })
                    }
                    else {
                        handleSubscription(subscription, JSON.parse(localStorage["pade.vapid.keys"]));
                    }

                }).catch(function(error) {
                    console.error('Error unsubscribing, no push messaging', error);
                });
            });
        } else {
            console.warn('Push messaging is not supported');
        }
    }

    function makeSubscription(callback)
    {
        const keys = window.WebPushLib.generateVAPIDKeys();
        console.debug('makeSubscription', keys);

        swRegistration.pushManager.subscribe({userVisibleOnly: true, applicationServerKey: urlBase64ToUint8Array(keys.publicKey)}).then(function(subscription)
        {
            console.debug('User is subscribed.');
            localStorage["pade.vapid.keys"] = JSON.stringify(keys);
            if (callback) callback(false, subscription, keys);

        }).catch(function(err) {
            console.error('Failed to subscribe the user: ', err);
            if (callback) callback(true);
        });
    }

    function handleSubscription(subscription, keys)
    {
        console.debug('handleSubscription', subscription, keys);

        const secret = btoa(JSON.stringify({privateKey: keys.privateKey, publicKey: keys.publicKey, subscription: subscription}));
        window.WebPushLib.setVapidDetails('xmpp:' + APP.connection.xmpp.connection.domain, keys.publicKey, keys.privateKey);
        window.WebPushLib.selfSecret = secret;

        listenForWorkerEvents();
        createContactsButton();
        publishWebPush();
    }

    function listenWebPushEvents()
    {
        const connection = APP.connection.xmpp.connection;
        const Strophe = APP.connection.xmpp.connection.Strophe;

        connection.addHandler(function(message)
        {
            console.debug('webpush handler', message);
            const handleElement = message.querySelector('webpush');

            if (handleElement && message.getAttribute("type") != 'error')
            {
                const secret = handleElement.innerHTML;
                const id = Strophe.getResourceFromJid(message.getAttribute("from"));
                const participant = APP.conference.getParticipantById(id);
                const myName = APP.conference.getLocalDisplayName();

                console.debug('webpush contact', id, participant);

                if (participant && participant._displayName)
                {
                    localStorage['pade.webpush.' + participant._displayName] = atob(secret);
                }
                else if (APP.conference.getMyUserId() == id && myName) {
                    localStorage['pade.webpush.' + myName] = atob(secret);
                }

            }

            return true;

        }, "urn:xmpp:push:0", "message");
    }

    function listenForWorkerEvents()
    {
        navigator.serviceWorker.onmessage = function(event)
        {
            console.debug("Broadcasted from service worker : ", event.data);

            if (event.data.options)    // subscription renewal.
            {
                makeSubscription(function(err, subscription, keys)
                {
                    if (!err)
                    {
                        handleSubscription(subscription, keys);
                    }
                })
            }
        }
    }

    function doContacts()
    {
        const template =
            '<div class="modal-header">' +
            '    <h4 class="modal-title">Contacts Manager</h4>' +
            '</div>' +
            '<div class="modal-body">' +
            '    <div class="pade-col-container meeting-contacts">' +
            '   </div>' +
            '</div>'

        if (!contactsModal)
        {
            contactsModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['custom-class-1', 'custom-class-2'],

                beforeOpen: function() {
                    console.debug("beforeOpen");

                    const container = document.querySelector(".meeting-contacts");

                    if (!contactsModalOpened)
                    {
                        container.addEventListener("click", function(evt)
                        {
                            evt.stopPropagation();
                            var parent = evt.target.parentNode;
                            while ( parent.tagName != "LI" )
                            {
                              parent = parent.parentNode;
                            }
                            const contact = parent.getAttribute("data-contact");
                            const email = parent.getAttribute("data-email");
                            const ele = parent.querySelector(".meeting-icon");
                            const selected = parent.querySelector(".meeting-icon > img");
                            const image = email ? IMAGES.mail : IMAGES.contact;

                            if (ele && contact)
                            {
                                console.debug("beforeOpen - click", contact, ele);
                                const emailAttr = email ? 'data-email="' + email + '"' : '';
                                if (ele) ele.innerHTML = selected ? image : '<img ' + emailAttr + ' data-contact="' + contact + '" width="24" height="24" src="./check-solid.png">';
                            }
                        });

                        contactsModalOpened = true;
                    }

                    container.innerHTML = "";

                    var contacts = {};

                    for (var i = 0; i < localStorage.length; i++)
                    {
                        if (localStorage.key(i).indexOf("pade.webpush.") == 0)
                        {
                            const name = localStorage.key(i).substring(13);
                            const email = localStorage.getItem("pade.email." + name);
                            contacts[name] = email;
                        }
                    }

                    Object.entries(contacts).sort( (a,b) => a[0].localeCompare(b[0]) ).forEach(function(n)
                    {
                        addContact(n[0],contacts[n[0]])
                    });
                }
            });
            contactsModal.addFooterBtn('Invite Selected', 'btn btn-danger tingle-btn tingle-btn--primary', function() {
                const container = document.querySelector(".meeting-contacts");

                container.querySelectorAll(".meeting-icon > img").forEach(function(icon) {
                    const contact = icon.getAttribute("data-contact");
                    const message = APP.conference.getLocalDisplayName() + ' invites you to join the room ' + APP.conference.roomName + '.';

                    sendWebPush(message, contact, function(name, error) {
                        let image = './delivered.png';
                        if (error) image = './times-solid.png';
                        icon.outerHTML = '<img data-contact="' + name + '" width="24" height="24" src="' + image + '">';
                    });
                });
            });

            contactsModal.addFooterBtn('Email Selected', 'btn btn-danger tingle-btn tingle-btn--primary', function() {
                const container = document.querySelector(".meeting-contacts");
                let mailto = "mailto:";

                container.querySelectorAll(".meeting-icon > img").forEach(function(icon) {
                    const email = icon.getAttribute("data-email");
                    if (email) mailto = mailto + email + ";"
                });

                if (mailto != "mailto:")
                {
                    mailto = mailto + "?subject=" + interfaceConfig.APP_NAME + "&body="  + location.href + "\n\n";
                    window.open(encodeURI(mailto));
                }
            });

            contactsModal.addFooterBtn('Reset Selected', 'btn btn-success tingle-btn tingle-btn--primary', function() {
                const container = document.querySelector(".meeting-contacts");

                container.querySelectorAll(".meeting-icon > img").forEach(function(icon) {
                    icon.outerHTML = IMAGES.contact;
                });
            });

            contactsModal.addFooterBtn('Close', 'btn btn-success tingle-btn tingle-btn--primary', function() {
                contactsModal.close();
            });

            contactsModal.setContent(template);
        }

        contactsModal.open();
    }

    function addContact(name, email)
    {
        console.debug("addContact", name, email);

        const image = email ? IMAGES.mail : IMAGES.contact;
        const container = document.querySelector(".meeting-contacts");
        const html = '<span class="pade-col-content meeting-icon">' + image + '</span><span class="pade-col-content">' + name + '</span>';
        const ele = document.createElement('li');
        ele.innerHTML = html;
        ele.classList.add("pade-col");
        ele.setAttribute("data-contact", name);
        if (email) ele.setAttribute("data-email", email);
        container.appendChild(ele);
    }

    function publishWebPush()
    {
        const connection = APP.connection.xmpp.connection;
        const $msg = APP.connection.xmpp.connection.$msg;

        if (window.WebPushLib && window.WebPushLib.selfSecret && APP.conference._room.room)
        {
            console.debug("publishWebPush", window.WebPushLib.selfSecret);
            connection.send($msg({to: APP.conference._room.room.roomjid, type: 'groupchat'}).c('webpush', {xmlns: "urn:xmpp:push:0"}).t(window.WebPushLib.selfSecret));
        }
    }

    function sendWebPush(body, name, callback)
    {
        console.debug('sendWebPush', body, name);

        if (localStorage['pade.webpush.' + name])
        {
            const secret = JSON.parse(localStorage['pade.webpush.' + name]);
            const payload = {msgSubject: interfaceConfig.APP_NAME, msgBody: body, msgType: 'meeting', url: location.href};

            console.debug("sendWebPush secret", secret, payload);

            window.WebPushLib.setVapidDetails('xmpp:' + APP.conference._room.room.myroomjid, secret.publicKey, secret.privateKey);

            window.WebPushLib.sendNotification(secret.subscription, JSON.stringify(payload), {TTL: 60}).then(response => {
                console.debug("Web Push Notification is sended!");
                if (callback) callback(name);
            }).catch(e => {
                console.error('Failed to notify', name, e)
                if (callback) callback(name, e);
            })
        }
    }

    function urlBase64ToUint8Array(base64String) {
        const padding = '='.repeat((4 - base64String.length % 4) % 4);
        const base64 = (base64String + padding)
        .replace(/-/g, '+')
        .replace(/_/g, '/');

        const rawData = window.atob(base64);
        const outputArray = new Uint8Array(rawData.length);

        for (let i = 0; i < rawData.length; ++i) {
            outputArray[i] = rawData.charCodeAt(i);
        }
        return outputArray;
    }

    //-------------------------------------------------------
    //
    //  idbKeyval
    //
    //-------------------------------------------------------

    var idbKeyval = (function (exports) {
    'use strict';

    class Store {
        constructor(dbName = 'keyval-store', storeName = 'keyval') {
            this.storeName = storeName;
            this._dbp = new Promise((resolve, reject) => {
                const openreq = indexedDB.open(dbName, 1);
                openreq.onerror = () => reject(openreq.error);
                openreq.onsuccess = () => resolve(openreq.result);
                // First time setup: create an empty object store
                openreq.onupgradeneeded = () => {
                    openreq.result.createObjectStore(storeName);
                };
            });
        }
        _withIDBStore(type, callback) {
            return this._dbp.then(db => new Promise((resolve, reject) => {
                const transaction = db.transaction(this.storeName, type);
                transaction.oncomplete = () => resolve();
                transaction.onabort = transaction.onerror = () => reject(transaction.error);
                callback(transaction.objectStore(this.storeName));
            }));
        }
    }
    let store;
    function getDefaultStore() {
        if (!store)
            store = new Store();
        return store;
    }
    function get(key, store = getDefaultStore()) {
        let req;
        return store._withIDBStore('readonly', store => {
            req = store.get(key);
        }).then(() => req.result);
    }
    function set(key, value, store = getDefaultStore()) {
        return store._withIDBStore('readwrite', store => {
            store.put(value, key);
        });
    }
    function del(key, store = getDefaultStore()) {
        return store._withIDBStore('readwrite', store => {
            store.delete(key);
        });
    }
    function clear(store = getDefaultStore()) {
        return store._withIDBStore('readwrite', store => {
            store.clear();
        });
    }
    function keys(store = getDefaultStore()) {
        let req;
        return store._withIDBStore('readwrite', store => {
            req = store.getAll();
        }).then(() => req.result);
    }

    exports.Store = Store;
    exports.get = get;
    exports.set = set;
    exports.del = del;
    exports.clear = clear;
    exports.keys = keys;

    return exports;

    }({}));

    of.recording = false;

    return of;

}(ofmeet || {}));
