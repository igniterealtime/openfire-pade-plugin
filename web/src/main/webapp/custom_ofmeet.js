var ofmeet = (function(of)
{
    //-------------------------------------------------------
    //
    //  defines
    //
    //-------------------------------------------------------

    const IMAGES = {};
    IMAGES.pad = '<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 30.122,30.122L 28.020,23.778L 11.050,6.808L 10,7.858L 6.808,11.050L 23.778,28.020 zM 3.98,8.222L 8.222,3.98l-2.1-2.1c-1.172-1.172-3.070-1.172-4.242,0c-1.172,1.17-1.172,3.072,0,4.242 L 3.98,8.222z"></path></g></svg></span>';
    IMAGES.sheet = '<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 4,10l 4,0 c 1.104,0, 2-0.896, 2-2L 10,4 c0-1.104-0.896-2-2-2L 4,2 C 2.896,2, 2,2.896, 2,4l0,4 C 2,9.104, 2.896,10, 4,10zM 14,10l 4,0 c 1.104,0, 2-0.896, 2-2L 20,4 c0-1.104-0.896-2-2-2L 14,2 C 12.896,2, 12,2.896, 12,4l0,4 C 12,9.104, 12.896,10, 14,10zM 24,10l 4,0 c 1.104,0, 2-0.896, 2-2L 30,4 c0-1.104-0.896-2-2-2l-4,0 c-1.104,0-2,0.896-2,2l0,4 C 22,9.104, 22.896,10, 24,10zM 2,18c0,1.104, 0.896,2, 2,2l 4,0 c 1.104,0, 2-0.896, 2-2L 10,14 c0-1.104-0.896-2-2-2L 4,12 C 2.896,12, 2,12.896, 2,14L 2,18 zM 12,18c0,1.104, 0.896,2, 2,2l 4,0 c 1.104,0, 2-0.896, 2-2L 20,14 c0-1.104-0.896-2-2-2L 14,12 C 12.896,12, 12,12.896, 12,14L 12,18 zM 22,18c0,1.104, 0.896,2, 2,2l 4,0 c 1.104,0, 2-0.896, 2-2L 30,14 c0-1.104-0.896-2-2-2l-4,0 c-1.104,0-2,0.896-2,2L 22,18 zM 2,28c0,1.104, 0.896,2, 2,2l 4,0 c 1.104,0, 2-0.896, 2-2l0-4 c0-1.104-0.896-2-2-2L 4,22 c-1.104,0-2,0.896-2,2L 2,28 zM 12,28c0,1.104, 0.896,2, 2,2l 4,0 c 1.104,0, 2-0.896, 2-2l0-4 c0-1.104-0.896-2-2-2L 14,22 c-1.104,0-2,0.896-2,2L 12,28 zM 22,28c0,1.104, 0.896,2, 2,2l 4,0 c 1.104,0, 2-0.896, 2-2l0-4 c0-1.104-0.896-2-2-2l-4,0 c-1.104,0-2,0.896-2,2L 22,28 z"></path></g></svg></span>';
    IMAGES.code = '<svg width="32.24800109863281" height="32.24800109863281" viewBox="0 0 32.24800109863281 32.24800109863281" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 21.172,21.172L 19.39,15.792L 9.11,5.512L 5.512,9.11L 15.792,19.39 zM 0.746,0.746c-0.994,0.994-0.994,2.604,0,3.598l 2.648,2.648l 3.598-3.598L 4.344,0.746 C 3.35-0.248, 1.74-0.248, 0.746,0.746zM 30,6L 15.822,6 l 2,2L 30,8 l0,22 L 8,30 L 8,17.822 l-2-2L 6,30 c0,1.104, 0.896,2, 2,2l 22,0 c 1.104,0, 2-0.896, 2-2L 32,8 C 32,6.896, 31.104,6, 30,6z"></path></g></svg></span>';
    IMAGES.slide = '<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="32" height="32.11199951171875" viewBox="0 0 32 32.11199951171875" enable-background="new 0 0 16 16" xml:space="preserve" fill="#000000"> <g><path d="M 9.030,16.5c 0.296,0, 0.592-0.132, 0.79-0.386l 4.404-5.67l 3.016,3.542c 0.192,0.222, 0.408,0.32, 0.766,0.352 C 18.296,14.336, 18.576,14.208, 18.766,13.98l 7-8.338c 0.356-0.422, 0.3-1.052-0.124-1.408c-0.422-0.358-1.052-0.298-1.408,0.124 l-6.24,7.432L 14.95,8.21C 14.752,7.982, 14.388,7.876, 14.166,7.86C 13.864,7.868, 13.582,8.008, 13.398,8.246L 8.24,14.886 C 7.9,15.322, 7.98,15.952, 8.416,16.29C 8.598,16.432, 8.814,16.5, 9.030,16.5zM 30.978,0L 28,0 L 6,0 L 3.022,0 C 2.458,0, 2,0.448, 2,1C 2,1.552, 2.458,2, 3.022,2L 4,2 l0,18 c0,1.104, 0.896,2, 2,2l 10,0 l0,3.122 L 10.328,30.26c-0.408,0.37-0.44,1.002-0.068,1.412c 0.374,0.408, 1.006,0.44, 1.412,0.068L 16,27.82l0,3.18 C 16,31.552, 16.448,32, 17,32 S 18,31.552, 18,31l0-3.18 l 4.328,3.92C 22.52,31.914, 22.76,32, 23,32c 0.272,0, 0.542-0.112, 0.74-0.328 c 0.372-0.41, 0.34-1.042-0.068-1.412L 18,25.122L 18,22 l 10,0 c 1.104,0, 2-0.896, 2-2L 30,2 l 0.978,0 C 31.542,2, 32,1.552, 32,1 C 32,0.448, 31.542,0, 30.978,0z M 28,20L 6,20 L 6,2 l 22,0 L 28,20 z"></path></g></svg></span>' ;
    IMAGES.poll = '<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 13.774,26.028A2.060,2.060 1080 1 0 17.894,26.028A2.060,2.060 1080 1 0 13.774,26.028zM 19.464,18.252c 2.898-1.596, 4.37-3.91, 4.37-6.876c0-5.094-4.018-7.376-8-7.376c-3.878,0-8,2.818-8,8.042 c0,1.104, 0.894,2, 2,2s 2-0.896, 2-2c0-2.778, 2.074-4.042, 4-4.042c 1.494,0, 4,0.438, 4,3.376c0,1.042-0.274,2.258-2.298,3.374 C 16.16,15.504, 13.834,17.462, 13.834,20c0,1.104, 0.894,2, 2,2s 2-0.896, 2-2C 17.834,19.628, 18.624,18.714, 19.464,18.252z"></path></g></svg></span>';
    IMAGES.kanban = '<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 31.966,3.896C 31.878,2.866, 31.046,2, 30,2L 2,2 C 0.954,2, 0.122,2.866, 0.034,3.896L0,3.896 l0,0.166 L0,8 l0,14.166 L0,24 l0,6 c0,1.104, 0.896,2, 2,2l 28,0 c 1.104,0, 2-0.896, 2-2L 32,8 L 32,4.062 L 32,3.896 L 31.966,3.896 z M 12,14L 12,8 l 8,0 l0,6 L 12,14 z M 20,16l0,6.166 L 12,22.166 L 12,16 L 20,16 z M 10,8l0,6 L 2,14 L 2,8 L 10,8 z M 2,16l 8,0 l0,6.166 L 2,22.166 L 2,16 z M 2,30l0-6 l 8,0 l0,6 L 2,30 z M 12,30l0-6 l 8,0 l0,6 L 12,30 z M 30,30l-8,0 l0-6 l 8,0 L 30,30 z M 30,22.166l-8,0 L 22,16 l 8,0 L 30,22.166 z M 30,14l-8,0 L 22,8 l 8,0 L 30,14 z"></path></g></svg></span>';
    IMAGES.whiteboard = '<svg version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" width="32" height="32" viewBox="0 0 32 32" enable-background="new 0 0 16 16" xml:space="preserve" fill="#000000"> <g><path d="M 30,20L 30,16 c0-1.104-0.896-2-2-2L 18,14 L 18,10 l 6,0 c 1.104,0, 2-0.896, 2-2L 26,6 c0-1.104-0.896-2-2-2L 10,4 C 8.896,4, 8,4.896, 8,6l0,2 c0,1.104, 0.896,2, 2,2l 6,0 l0,4 L 6,14 C 4.896,14, 4,14.896, 4,16l0,4 c-1.104,0-2,0.896-2,2l0,4 c0,1.104, 0.896,2, 2,2l 2,0 c 1.104,0, 2-0.896, 2-2l0-2 l0-2 c0-1.104-0.896-2-2-2L 6,16 l 10,0 l0,4 c-1.104,0-2,0.896-2,2l0,2 l0,2 c0,1.104, 0.896,2, 2,2l 2,0 c 1.104,0, 2-0.896, 2-2l0-2 l0-2 c0-1.104-0.896-2-2-2L 18,16 l 10,0 l0,4 c-1.104,0-2,0.896-2,2l0,2 l0,2 c0,1.104, 0.896,2, 2,2l 2,0 c 1.104,0, 2-0.896, 2-2l0-4 C 32,20.896, 31.104,20, 30,20z M 10,6l 14,0 l0,2 L 10,8 L 10,6 z M 6,24l0,2 L 4,26 l0-4 l 2,0 L 6,24 z M 18,26L 16,26 l0-4 l 2,0 L 18,26 z M 28,24l0-2 l 2,0 l0,4 l-2,0 L 28,24 z"></path></g></svg></span>';
    IMAGES.person = '<svg width="24" height="24" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 16,0C 7.164,0,0,7.164,0,16s 7.164,16, 16,16s 16-7.164, 16-16C 32,7.162, 24.836,0, 16,0z M 16.568,7.984 C 16.74,7.594, 16.972,7.25, 17.266,6.956c 0.296-0.296, 0.636-0.528, 1.028-0.7C 18.684,6.084, 19.11,6, 19.564,6 c 0.458,0, 0.88,0.084, 1.27,0.256c 0.39,0.17, 0.732,0.404, 1.026,0.7c 0.296,0.294, 0.53,0.638, 0.7,1.028 c 0.172,0.39, 0.258,0.812, 0.258,1.27c0,0.458-0.086,0.88-0.258,1.27c-0.17,0.39-0.404,0.732-0.7,1.028 c-0.296,0.294-0.636,0.528-1.026,0.7c-0.39,0.17-0.812,0.256-1.27,0.256c-0.456,0-0.88-0.086-1.27-0.256 c-0.39-0.172-0.732-0.404-1.028-0.7C 16.972,11.256, 16.74,10.914, 16.568,10.524C 16.396,10.134, 16.31,9.71, 16.31,9.254 C 16.31,8.796, 16.396,8.374, 16.568,7.984z M 11.502,7.468c 0.486-0.484, 1.070-0.726, 1.754-0.726c 0.704,0, 1.3,0.242, 1.784,0.726 C 15.526,7.954, 15.768,8.54, 15.768,9.226c0,0.704-0.244,1.298-0.73,1.784C 14.556,11.494, 13.96,11.736, 13.256,11.736 c-0.682,0-1.268-0.242-1.754-0.728c-0.486-0.486-0.73-1.080-0.73-1.784C 10.774,8.54, 11.016,7.954, 11.502,7.468z M 7.136,7.926 c 0.37-0.372, 0.822-0.558, 1.354-0.558c 0.534,0, 0.98,0.186, 1.342,0.558c 0.36,0.37, 0.54,0.812, 0.54,1.326 c0,0.534-0.182,0.984-0.54,1.356C 9.47,10.98, 9.024,11.166, 8.49,11.166c-0.532,0-0.984-0.186-1.354-0.556 C 6.764,10.238, 6.578,9.786, 6.578,9.254C 6.578,8.74, 6.764,8.296, 7.136,7.926z M 24,26L 15.21,26 l0-4 L 9.704,22 L 9.704,18 L 6.418,18 L 6.418,14.362 c0-0.646-0.016-1.194, 0.432-1.64c 0.446-0.446, 0.994-0.67, 1.64-0.67c 0.552,0, 1.030,0.166, 1.428,0.5 c 0.4,0.334, 0.664,0.746, 0.8,1.242c 0.322-0.38, 0.704-0.674, 1.14-0.886c 0.438-0.208, 0.904-0.314, 1.398-0.314 c 0.78,0, 1.46,0.234, 2.042,0.7c 0.58,0.466, 0.966,1.052, 1.156,1.754c 0.4-0.38, 0.864-0.686, 1.4-0.912 c 0.532-0.228, 1.104-0.342, 1.712-0.342c 0.61,0, 1.186,0.114, 1.728,0.342c 0.54,0.228, 1.012,0.542, 1.412,0.942 c 0.4,0.4, 0.718,0.87, 0.954,1.412C 23.898,17.030, 24,17.606, 24,18.216L 24,26 z"></path></g></svg>'
    IMAGES.contact = '<svg width="24" height="24" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 2,0C 0.896,0,0,0.896,0,2l0,28 c0,1.104, 0.896,2, 2,2l 2,0 L 4,0 L 2,0 zM 26,0L 6,0 l0,32 l 20,0 c 1.104,0, 2-0.896, 2-2L 28,2 C 28,0.896, 27.104,0, 26,0z M 16.89,7.708 c 1.672,0, 3.026,1.356, 3.026,3.026c0,1.672-1.356,3.028-3.026,3.028c-1.672,0-3.028-1.356-3.028-3.028 C 13.862,9.062, 15.218,7.708, 16.89,7.708z M 12,21.614c0-3.668, 2.218-6.64, 4.952-6.64s 4.952,2.974, 4.952,6.64S 12,25.28, 12,21.614zM 30,2L 32,2L 32,8L 30,8zM 30,10L 32,10L 32,16L 30,16zM 30,18L 32,18L 32,24L 30,24z"></path></g></svg>';
    IMAGES.mic = '<svg style="fill:white;margin-top:4px" width="24" height="24" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 14,25.808L 14,30 L 11,30 C 10.448,30, 10,30.448, 10,31C 10,31.552, 10.448,32, 11,32l 4.98,0 L 16.020,32 l 4.98,0 c 0.552,0, 1-0.448, 1-1c0-0.552-0.448-1-1-1L 18,30 l0-4.204 c 4.166-0.822, 8-4.194, 8-9.796L 26,13 C 26,12.448, 25.552,12, 25,12 S 24,12.448, 24,13L 24,16 c0,5.252-4.026,8-8,8c-3.854,0-8-2.504-8-8L 8,13 C 8,12.448, 7.552,12, 7,12S 6,12.448, 6,13L 6,16 C 6,21.68, 9.766,25.012, 14,25.808zM 16,20c 2.21,0, 4-1.79, 4-4L 20,4 c0-2.21-1.79-4-4-4S 12,1.79, 12,4l0,12 C 12,18.21, 13.79,20, 16,20z"></path></g></svg>';
    IMAGES.mail = '<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 30,8L 2,8 C 0.896,8,0,8.896,0,10l0,3.358 l 16,6.4l 16-6.4L 32,10 C 32,8.896, 31.104,8, 30,8zM0,16.624L0,30 c0,1.104, 0.896,2, 2,2l 28,0 c 1.104,0, 2-0.896, 2-2L 32,16.624 l-16,6.4L0,16.624z"></path></g></svg>';
    IMAGES.desktop = '<svg id="ofmeet-desktop" width="24" height="24" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="#000000"><g><path d="M 30,2L 2,2 C 0.896,2,0,2.896,0,4l0,18 c0,1.104, 0.896,2, 2,2l 9.998,0 c-0.004,1.446-0.062,3.324-0.61,4L 10.984,28 C 10.44,28, 10,28.448, 10,29C 10,29.552, 10.44,30, 10.984,30l 10.030,0 C 21.56,30, 22,29.552, 22,29c0-0.552-0.44-1-0.984-1l-0.404,0 c-0.55-0.676-0.606-2.554-0.61-4L 30,24 c 1.104,0, 2-0.896, 2-2L 32,4 C 32,2.896, 31.104,2, 30,2z M 14,24l-0.002,0.004 C 13.998,24.002, 13.998,24.002, 14,24L 14,24z M 18.002,24.004L 18,24l 0.002,0 C 18.002,24.002, 18.002,24.002, 18.002,24.004z M 30,20L 2,20 L 2,4 l 28,0 L 30,20 z"></path></g></svg>';

    const SMILIES = [":)", ":(", ":D", ":+1:", ":P", ":wave:", ":blush:", ":slightly_smiling_face:", ":scream:", ":*", ":-1:", ":mag:", ":heart:", ":innocent:", ":angry:", ":angel:", ";(", ":clap:", ";)", ":beer:"];
    const nickColors = {}, padsList = [], captions = {msgsDisabled: true, msgs: []}, breakout = {rooms: [], duration: 60, roomCount: 10, wait: 10}, pdf_body = [];
;

    let tagsModal = null, padsModal = null, breakoutModal = null, contactsModal = null;
    let padsModalOpened = false, contactsModalOpened = false, swRegistration = null, participants = {}, recordingAudioTrack = {}, recordingVideoTrack = {}, videoRecorder = {}, recorderStreams = {}, customStore = {}, filenames = {}, dbnames = [];
    let clockTrack = {start: 0, stop: 0, joins: 0, leaves: 0}, handsRaised = 0;
    let tags = {location: "", date: (new Date()).toISOString().split('T')[0], subject: "", host: "", activity: ""};

    //-------------------------------------------------------
    //
    //  window events
    //
    //-------------------------------------------------------

    window.addEventListener("DOMContentLoaded", function()
    {
        console.debug("ofmeet.js load");

        setTimeout(setup, 1000);

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
            if (window.webkitSpeechRecognition) setupVoiceCommand()
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
            event.preventDefault();
            event.returnValue = '';

            if (of.recording) stopRecorder();

            dbnames.forEach(function(dbname)
            {
                const deleteRequest = indexedDB.deleteDatabase(dbname)

                deleteRequest.onsuccess = function(event) {
                  console.debug("ofmeet.js me database deleted successfully", dbname);
                };
            });

            return event.returnValue;
        }
    });

    //-------------------------------------------------------
    //
    //  setup
    //
    //-------------------------------------------------------

    function setupVoiceCommand()
    {
        const enter_room_button = document.getElementById('enter_room_button');

        if (enter_room_button)
        {
            const button = newElement('div', "speak_room_button", IMAGES.mic, 'welcome-page-button', "Speak Meeting Room Name");

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
        if (!APP.connection || !APP.conference)
        {
            setTimeout(setup, 100);
            return;
        }

        if (!config.webinar)
        {
            listenWebPushEvents();

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.CONFERENCE_JOINED, function()
            {
                console.debug("ofmeet.js me joined");
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.CONFERENCE_LEFT, function()
            {
                console.debug("ofmeet.js me left");

                if (of.recording) stopRecorder();

                const ids = Object.getOwnPropertyNames(recordingVideoTrack);

                ids.forEach(function(id)
                {
                    delete recordingAudioTrack[id];
                    delete recordingVideoTrack[id];
                });
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.TRACK_REMOVED, function(track)
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
                }

                if (of.recording) stopRecorder();
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.USER_JOINED, function (id)
            {
                console.debug("user join", id, participants);
                addParticipant(id);
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.USER_LEFT, function (id)
            {
                console.debug("user left", id);

                if (breakout.kanban)
                {
                    breakout.kanban.removeElement(id);
                }

                delete participants[id];
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.TRACK_ADDED, function(track)
            {
                const id = track.getParticipantId();
                console.debug("ofmeet.js track added", id, track.getType());

                if (track.getType() == "audio") recordingAudioTrack[id] = track.stream;
                if (track.getType() == "video") recordingVideoTrack[id] = track.stream;

                if (APP.conference.getMyUserId() == id  && !config.webinar)
                {

                }
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.PARTICIPANT_PROPERTY_CHANGED, function(e, t, n, r)
            {
                console.debug("ofmeet.js property changed", e, t, n, r);
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.TRACK_MUTE_CHANGED, function(track)
            {
                const id = track.getParticipantId();
                console.debug("ofmeet.js track muted", id, track.getType(), track.isMuted());

                if (track.getType() == "audio") recordingAudioTrack[id].getAudioTracks()[0].enabled = !track.isMuted();
                if (track.getType() == "video") recordingVideoTrack[id].getVideoTracks()[0].enabled = !track.isMuted();

                const recordingStream = recorderStreams[id];

                if (recordingStream) // recording active
                {
                    if (track.getType() == "audio") recordingStream.getAudioTracks()[0].enabled = !track.isMuted();
                    if (track.getType() == "video") recordingStream.getVideoTracks()[0].enabled = !track.isMuted();
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

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.PRIVATE_MESSAGE_RECEIVED, function(id, text, ts)
            {
                var participant = APP.conference.getParticipantById(id);
                var displayName = participant ? (participant._displayName || 'Anonymous-' + id) : (APP.conference.getLocalDisplayName() || "Me");

                console.debug("ofmeet.js private message", id, text, ts, displayName);

                const pretty_time = dayjs().format('MMM DD HH:mm:ss');
                pdf_body.push([pretty_time, displayName, text]);
            });

            APP.conference.addConferenceListener(JitsiMeetJS.events.conference.MESSAGE_RECEIVED, function(id, text, ts)
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

            navigator.mediaDevices.getUserMedia({audio: true, video: true}).then(function(stream)
            {
                recordingVideoTrack[APP.conference.getMyUserId()] = stream;
                recordingAudioTrack[APP.conference.getMyUserId()] = stream;

                if (interfaceConfig.OFMEET_RECORD_CONFERENCE)
                {
                    createRecordButton();
                    createPhotoButton();
                    createDesktopButton();

                    if (APP.conference.getMyUserId())
                    {
                        showClock();
                        clockTrack.joins = (new Date()).getTime();
                    }
                }
            });

            captions.ele = document.getElementById("captions");

            if (interfaceConfig.OFMEET_TAG_CONFERENCE)
            {
                if (interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION && window.webkitSpeechRecognition)
                {
                    setupSpeechRecognition();
                }

                captions.msgsDisabled = !interfaceConfig.OFMEET_SHOW_CAPTIONS;
                captions.transcriptDisabled = !interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION;

                createTagsButton();
            }

            if (interfaceConfig.OFMEET_ENABLE_CRYPTPAD)
            {
                createPadsButton();
            }
        }

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

        APP.connection.xmpp.connection.addHandler(handleMucMessage, "urn:xmpp:json:0", "message");
        APP.connection.xmpp.connection.addHandler(handlePresence, null, "presence");

        getServiceWorker(function(registration)
        {
            console.debug('Service worker registered', registration);
        });

        setTimeout(postLoadSetup, 5000);

        console.log("ofmeet.js setup", APP.connection, captions);

        setTimeout(lostAudioWorkaround, 5000);
    }


    //-------------------------------------------------------
    //  WORKAROUND: prevent disruption of a muted audio connection by a short toggle
    //-------------------------------------------------------
    let lostAudioWorkaroundInterval = 5*60*1000; // 5min
    let audioTemporaryUnmuted = false;
    function lostAudioWorkaround ()
    {
        if (APP.conference.isLocalAudioMuted() || audioTemporaryUnmuted)
        {
          APP.conference.toggleAudioMuted(false);
          audioTemporaryUnmuted = !audioTemporaryUnmuted;
          console.log("audio " + (audioTemporaryUnmuted ? "temporary un" : "re") + "muted");
        }
        setTimeout(lostAudioWorkaround, audioTemporaryUnmuted ? 1 : lostAudioWorkaroundInterval);
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
                avatar = 'data:' + photo.querySelector('TYPE').innerHTML + ';base64,' + photo.querySelector('BINVAL').innerHTML;
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

        if (!width) width = 32;
        if (!height) height = 32;
        if (!font) font = "16px Arial";

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

        if (nickname)
        {
            // try to split nickname into words at different symbols with preference
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
                if (lastInitial) {
                    // if any comma is in the nickname, treat it to have the lastname in front, i.e. compose reversed
                    const initials = nickname.indexOf(",") == -1 ? firstInitial + lastInitial : lastInitial + firstInitial;
                    context.fillText(initials.toUpperCase(), 3, 23);
                } else {
                    context.fillText(firstInitial.toUpperCase(), 10, 23);
                }
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

    function createRecordButton()
    {
        const recordButton = addToolbarItem('ofmeet-record', '<div class="toolbox-icon "><div class="jitsi-icon "><svg id="ofmeet-record" style="fill: white;" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 1000 1000" enable-background="new 0 0 1000 1000" xml:space="preserve"><g><path d="M928.8,438.8L745,561.3c0-37.2-16.9-70.1-43.1-92.5c62.3-37.5,104.3-105.1,104.3-183.1c0-118.4-96-214.4-214.4-214.4c-118.4,0-214.4,96-214.4,214.4c0,60.1,24.9,114.2,64.7,153.1H329.8c29.3-32.5,47.7-75.2,47.7-122.5c0-101.5-82.3-183.8-183.8-183.8C92.3,132.5,10,214.8,10,316.3c0,55.4,25,104.4,63.8,138.1C35.9,475.2,10,515,10,561.3v245c0,67.6,54.9,122.5,122.5,122.5h490c67.6,0,122.5-54.9,122.5-122.5v-30.6l183.8,153.1c33.8,0,61.3-27.4,61.3-61.3V500C990,466.2,962.6,438.8,928.8,438.8z M71.3,316.3c0-67.7,54.9-122.5,122.5-122.5c67.6,0,122.5,54.8,122.5,122.5s-54.9,122.5-122.5,122.5C126.1,438.7,71.3,383.9,71.3,316.3z M683.8,806.3c0,33.8-27.4,61.3-61.3,61.3h-490c-33.8,0-61.3-27.4-61.3-61.3v-245c0-33.8,27.4-61.3,61.3-61.3h490c33.8,0,61.3,27.4,61.3,61.3V806.3z M591.9,439.1c-84.8,0-153.5-68.7-153.5-153.5c0-84.8,68.7-153.5,153.5-153.5c84.8,0,153.5,68.7,153.5,153.5C745.4,370.4,676.6,439.1,591.9,439.1z M928.8,545.9v281.2c0,1.6,0,2.2,0,2.2v38.1L745,714.4v-61.3c0-16.2,0-12.3,0-30.6L928.8,500C928.8,543,928.8,520.6,928.8,545.9z"/></g></svg></div></div>', "Record Meeting");

        if (recordButton) recordButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();

            if (!of.recording) {
                startMeetingRecorder();
            } else {
                stopRecorder();
            }
        });

        const leaveButton = document.querySelector('div[aria-label="Leave the call"]');

        if (leaveButton) leaveButton.addEventListener("click", function(evt)
        {
            if (of.recording) stopRecorder();

            if (pdf_body.length > 0)
            {
                const margins = {
                  top: 70,
                  bottom: 40,
                  left: 30,
                  width: 550
                };
                const pdf = new jsPDF('p','pt','a4');
                //pdf.setFontSize(18);

                pdf.autoTable({
                    head: [['Date', 'Person', 'Message']],
                    body: pdf_body,
                    columnStyles: {
                        0: {cellWidth: 100},
                        1: {cellWidth: 100},
                        2: {cellWidth: 300}
                    }
                })
                const roomLabel = APP.conference.roomName + '-' + Math.random().toString(36).substr(2,9);
                pdf.save(roomLabel + '.pdf');
            }
        });
    }

    function createDesktopButton()
    {
        const desktopButton = addToolbarItem('ofmeet-desktop', '<div class="toolbox-icon "><div class="jitsi-icon" style="font-size: 12px;">' + IMAGES.desktop + '</div></div>', "Record Desktop App (Including Meeting Browser Tab)");

        if (desktopButton) desktopButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();

            if (!of.recording) {
                startDesktopRecorder();
            } else {
                stopRecorder();
            }
        });
    }

    function createTagsButton()
    {
        const tagsButton = addToolbarItem('ofmeet-tags', '<div id="ofmeet-tags" class="toolbox-icon "><div class="jitsi-icon" style="font-size: 12px;"><svg height="24" width="24" viewBox="0 0 24 24"><path d="M18 11.016V9.985a.96.96 0 00-.984-.984h-3c-.563 0-1.031.422-1.031.984v4.031c0 .563.469.984 1.031.984h3a.96.96 0 00.984-.984v-1.031h-1.5v.516h-2.016v-3H16.5v.516H18zm-6.984 0V9.985c0-.563-.469-.984-1.031-.984h-3a.96.96 0 00-.984.984v4.031a.96.96 0 00.984.984h3c.563 0 1.031-.422 1.031-.984v-1.031h-1.5v.516H7.5v-3h2.016v.516h1.5zm7.968-7.032C20.062 3.984 21 4.922 21 6v12c0 1.078-.938 2.016-2.016 2.016H5.015c-1.125 0-2.016-.938-2.016-2.016V6c0-1.078.891-2.016 2.016-2.016h13.969z"></path></svg></div></div>', "Enable Conference Captions/Subtitles");

        if (tagsButton) tagsButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();
            doTags();
        });
    }

    function createPadsButton()
    {
        const padsButton = addToolbarItem('ofmeet-pads', '<div id="ofmeet-pads" class="toolbox-icon "><div class="jitsi-icon" style="font-size: 12px;"><img width="22" src="https://sandbox.cryptpad.info/customize/images/logo_white.png"/></div></div>', "Launch CryptPad Application");

        if (padsButton) padsButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();
            doPads();
        });
    }

    function hideClock()
    {
        document.getElementById("clocktext").style.display = "none";
    }

    function showClock()
    {
        const textElem = document.getElementById("clocktext");
        textElem.style.display = "";

        let totalSeconds = 0;

        function pad(val) {
          return (10 > val ? "0" : "") + val;
        }

        function updateClock() {
            ++totalSeconds;

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
                    APP.UI.messageHandler.notify("Clipboard shared with other participants", null, null, "");
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
            APP.UI.messageHandler.notify("Quit active pad before opening a new one", null, null, "");
        }
        else {
            const largeVideo = document.querySelector("#largeVideo");
            const iframe = largeVideo.cloneNode(false);

            largeVideo.parentNode.appendChild(iframe);
            largeVideo.style.display = "none";
            iframe.requestFullscreen();

            iframe.outerHTML = '<iframe src=' + url + ' id="ofmeet-content" style="width: 90%; height: 92%; border: 0;padding-left: 0px; padding-top: 0px;">'

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
        '      <h4 class="modal-title">Conference Captions/Sub Titles</h4>' +
        '    </div>' +

        '    <!-- Modal body -->' +
        '    <div class="modal-body">' +
        '       <div class="form-group">' +
        '       <label for="tags-location" class="col-form-label">Location:</label>' +
        '       <input id="tags-location" type="text" class="form-control" name="tag-location" value="' + tags.location + '"/>' +
        '       <label for="tags-date" class="col-form-label">Date:</label>' +
        '       <input id="tags-date" type="text" class="form-control" name="tags-date"/>' +
        '       <label for="tags-subject" class="col-form-label">Subject:</label>' +
        '       <input id="tags-subject" type="text" class="form-control" name="tags-subject" value="' + tags.subject + '"/>' +
        '       <label for="tags-host" class="col-form-label">Host:</label>' +
        '       <input id="tags-host" type="text" class="form-control" name="tags-host" value="' + tags.host + '"/>' +
        '       <label for="tags-activity" class="col-form-label">Activity:</label>' +
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
                    document.getElementById('tags-date').value = (new Date()).toISOString().split('T')[0]
                },
                beforeClose: function() {
                    tags.location = document.getElementById('tags-location').value;
                    tags.date = document.getElementById('tags-date').value;
                    tags.subject = document.getElementById('tags-subject').value;
                    tags.host = document.getElementById('tags-host').value;
                    tags.activity = document.getElementById('tags-activity').value;

                    if (tags.location != "")
                    {
                        document.getElementById("subtitles").innerHTML = `<b>Location</b>: ${tags.location} <br/><b>Date</b>: ${tags.date} <br/><b>Subject</b>: ${tags.subject} <br/><b>Host</b>: ${tags.host} <br/><b>Activity</b>: ${tags.activity}`;
                    }
                    return true;
                }
            });
            tagsModal.setContent(template);

            tagsModal.addFooterBtn('Save', 'btn btn-success tingle-btn tingle-btn--primary', function() {
                // here goes some logic
                tagsModal.close();
            });

            tagsModal.addFooterBtn('Cancel', 'btn btn-danger tingle-btn tingle-btn--danger', function() {
                event.preventDefault();
                tags = {location: "", date: (new Date()).toISOString().split('T')[0], subject: "", host: "", activity: ""};

                document.getElementById('tags-location').value = tags.location;
                document.getElementById('tags-date').value = tags.date;
                document.getElementById('tags-subject').value = tags.subject;
                document.getElementById('tags-host').value = tags.host;
                document.getElementById('tags-activity').value = tags.activity;

                document.getElementById("subtitles").innerHTML =  "";
                tagsModal.close();
            });

            const msgCaptions = (captions.msgsDisabled ? 'Enable' : 'Disable') + ' Message Captions';
            const msgClass = (captions.msgsDisabled ? 'btn-secondary' : 'btn-success') + ' btn tingle-btn tingle-btn--pull-right';

            if (interfaceConfig.OFMEET_SHOW_CAPTIONS)
            {
                tagsModal.addFooterBtn(msgCaptions, msgClass, function(evt) {
                    captions.msgsDisabled = !captions.msgsDisabled;
                    evt.target.classList.remove(captions.msgsDisabled ? 'btn-success' : 'btn-secondary');
                    evt.target.classList.add(captions.msgsDisabled ? 'btn-secondary' : 'btn-success');
                    evt.target.innerHTML = (captions.msgsDisabled ? 'Enable' : 'Disable') + ' Message Captions';
                    if (captions.ele) captions.ele.innerHTML = "";
                });
            }

            if (of.recognition)
            {
                const transcriptCaptions = (captions.transcriptDisabled ? 'Enable' : 'Disable') + ' Voice Transcription';
                const transcriptClass = (captions.transcriptDisabled ? 'btn-secondary' : 'btn-success') + ' btn tingle-btn tingle-btn--pull-right';

                tagsModal.addFooterBtn(transcriptCaptions, transcriptClass, function(evt)
                {
                    captions.transcriptDisabled = !captions.transcriptDisabled;
                    of.recognitionActive = !captions.transcriptDisabled;
                    evt.target.classList.remove(captions.transcriptDisabled ? 'btn-success' : 'btn-secondary');
                    evt.target.classList.add(captions.transcriptDisabled ? 'btn-secondary' : 'btn-success');
                    evt.target.innerHTML = (captions.transcriptDisabled ? 'Enable' : 'Disable') + ' Voice Transcription';

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

    function createPhotoButton()
    {
        const photoButton = addToolbarItem('ofmeet-photo', '<div id="ofmeet-photo" class="toolbox-icon "><div class="jitsi-icon"><svg style="fill: white;" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M5 5h-3v-1h3v1zm8 5c-1.654 0-3 1.346-3 3s1.346 3 3 3 3-1.346 3-3-1.346-3-3-3zm11-4v15h-24v-15h5.93c.669 0 1.293-.334 1.664-.891l1.406-2.109h8l1.406 2.109c.371.557.995.891 1.664.891h3.93zm-19 4c0-.552-.447-1-1-1-.553 0-1 .448-1 1s.447 1 1 1c.553 0 1-.448 1-1zm13 3c0-2.761-2.239-5-5-5s-5 2.239-5 5 2.239 5 5 5 5-2.239 5-5z"/></svg></div></div>', "Take Photo");

        if (photoButton) photoButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();
            takePhoto();

            APP.UI.messageHandler.notify("Conference Photo Taken", null, null, "");
        });
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
            context.fillText("Hands Raised: " + handsRaised, 50, 25);
            context.fillText("Location: " + tags.location, 50, 50);
            context.fillText("Date: " +  tags.date, 50, 75);
            context.fillText("Subject: " +  tags.subject, 50, 100);
            context.fillText("Host: " +  tags.host, 50, 125);
            context.fillText("Activity: " +  tags.activity, 50, 150);
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

        document.getElementById("ofmeet-record").style.fill = "white";
        document.getElementById("ofmeet-desktop").style.fill = "white";
        APP.UI.messageHandler.notify("Conference Recording Stopped", null, null, "");

        clockTrack.stop = (new Date()).getTime();
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function(id)
        {
            if (videoRecorder[id]) videoRecorder[id].stop();
        });

        createVideoViewerHTML();
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

    function startMeetingRecorder()
    {
        console.debug("ofmeet.js startMeetingRecorder");

        const icon = document.getElementById("ofmeet-record");
        icon.style.fill = "red";
        APP.UI.messageHandler.notify("Conference Recording Started", null, null, "");

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
          desktopGain.gain.value = 0.7;
          source1.connect(desktopGain).connect(destination);
          hasDesktop = true;
        }

        if (voiceStream && voiceStream.getAudioTracks().length > 0) {
          const source2 = context.createMediaStreamSource(voiceStream);
          const voiceGain = context.createGain();
          voiceGain.gain.value = 0.7;
          source2.connect(voiceGain).connect(destination);
          hasVoice = true;
        }

        return (hasDesktop || hasVoice) ? destination.stream.getAudioTracks() : [];
    }

    function startDesktopRecorder()
    {
        console.debug("ofmeet.js startDesktopRecorder");

        navigator.mediaDevices.getDisplayMedia({video: true, audio: {autoGainControl: false, echoCancellation: false, googAutoGainControl: false, noiseSuppression: false}}).then(stream =>
        {
            const icon = document.getElementById("ofmeet-desktop");
            icon.style.fill = "red";
            APP.UI.messageHandler.notify("Conference Recording Started", null, null, "");

            captions.msgs = [];
            clockTrack.start = (new Date()).getTime();

            const id = APP.conference.getMyUserId();
            const tracks = [
              ...stream.getVideoTracks(),
              ...mergeAudioStreams(stream, recordingAudioTrack[id].clone())
            ];

            console.log('ofmeet.js startDesktopRecorder tracks', tracks);
            recorderStreams[id] =  new MediaStream(tracks);
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
            videoRecorder[id].start(1000);
            const startTime = Date.now();
            of.recording = true;

        }, error => {
            console.error("ofmeet.js startDesktopRecorder", error);
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
        const label = participants[id]._displayName || 'Anonymous-' + id;
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
        console.debug("handlePresence", presence);
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
            const label = handsRaised > 0 ? ("Hands Raised: " + handsRaised + " of " + handsTotal + " (" + handsPercentage + "%)" ) : "";
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
            const label = json.action == 'breakout' ? 'Joining' : 'Leaving';

            APP.UI.messageHandler.notify(label + " breakout in " + breakout.wait + " seconds", null, null, "");
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
                breakoutStatus("Breakout started and finishes in " + breakout.duration + " minutes ");
            }
            else {
                breakoutStatus("Breakout started. Click on reassemble to end and recall participants");
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

            breakoutStatus("Breakout has ended");
            if (breakout.timeout) clearTimeout(breakout.timeout);
        }

        breakout.started = !breakout.started;
        breakout.button.classList.remove(breakout.started ? 'btn-success' : 'btn-secondary');
        breakout.button.classList.add(breakout.started ? 'btn-secondary' : 'btn-success');
        breakout.button.innerHTML = breakout.started ? 'Reassemble' : 'Breakout';
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
            title: "Meeting Participants",
            class: "participants",
            item: []
        }]

        const ids = Object.getOwnPropertyNames(participants);

        for (let i=0; i<roomCount; i++)
        {
            boards[i+1] = {
                id: "room_" + i,
                title: "Room " + (i+1).toString(),
                class: "room",
                item: []
            }

            breakout.rooms[i] = APP.conference.roomName + '-' + Math.random().toString(36).substr(2,9);

            for (let j=0; j<ids.length; j++)
            {
                if (j % roomCount == i) // allocate participant j to room i
                {
                    console.debug("allocateToRooms - participant", j, ids[j], participants[ids[j]]);

                    const label = participants[ids[j]]._displayName || 'Anonymous-' + id;
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
                title: "Meeting Participants",
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
                title: participants[id]._displayName || 'Anonymous-' + id,
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
            '    <h4 class="modal-title">Breakout Rooms - <span id="breakout-title">' + ids.length + '</span> Participants</h4>' +
            '       <label for="breakout-duration" class="col-form-label">Duration (mins):</label>' +
            '       <input id="breakout-duration" type="number" min="0" max="480" step="30" name="breakout-duration" value="' + breakout.duration + '"/>' +
            '       <label for="breakout-rooms" class="col-form-label">Rooms:</label>' +
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

            breakoutModal.addFooterBtn('Close', 'btn btn-danger tingle-btn tingle-btn--primary', function()
            {
                breakoutModal.close();
            });

            breakoutModal.addFooterBtn('Allocate', 'btn btn-success tingle-btn tingle-btn--primary', function()
            {
                const roomCount = parseInt(document.getElementById('breakout-rooms').value);
                const ids = Object.getOwnPropertyNames(participants);

                if (ids.length > 0 && roomCount > 0)
                {
                    allocateToRooms(roomCount);
                    breakoutStatus("Allocated " + ids.length + " participants to " + roomCount + " rooms");
                }
                else {
                    breakoutStatus("Missing participants or rooms");
                }

                breakout.roomCount = roomCount;
            });

            const label = breakout.started ? 'Reassemble' : 'Breakout';

            breakoutModal.addFooterBtn(label, 'btn btn-success tingle-btn tingle-btn--primary', function(evt)
            {
                breakout.button = evt.target;
                breakout.duration = parseInt(document.getElementById('breakout-duration').value);

                if (breakout.roomCount > 0)
                {
                    toggleBreakout();
                }
                else {
                    breakoutStatus("Allocate participants first before breakout");
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

    function breakoutRooms()
    {
        console.debug("breakoutRooms");
        const breakoutButton = addToolbarItem('ofmeet-breakout', '<div id="ofmeet-breakout" class="toolbox-icon "><div class="jitsi-icon" style="font-size: 12px;">' + IMAGES.person + '</div></div>', "Create Breakout Rooms");

        if (breakoutButton) breakoutButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();
            doBreakout();
            getOccupants();
        });
    }

    //-------------------------------------------------------
    //
    //  Toolbar handler
    //
    //-------------------------------------------------------

    function newElement(el, id, html, className, label)
    {
        const ele = document.createElement(el);
        if (id) ele.id = id;
        if (html) ele.innerHTML = html;
        if (label) ele.title = label;
        if (className) ele.classList.add(className);
        document.body.appendChild(ele);
        return ele;
    }

    function addToolbarItem (id, html, label)
    {
        const placeHolder = document.querySelector('.button-group-left');
        let tool = null;

        if (placeHolder)
        {
            tool = newElement('div', null, html, 'toolbox-button', label);
            placeHolder.appendChild(tool);
        }
        return tool;
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

    function postLoadSetup()
    {
        var dropZone = document.getElementById("videospace");

        console.debug("postLoadSetup", dropZone);

        if (!dropZone)
        {
            setTimeout(postLoadSetup, 1000);
            return;
        }

        if (interfaceConfig.OFMEET_ENABLE_BREAKOUT && APP.conference._room.isModerator())
        {
            breakoutRooms();
        }

        if (interfaceConfig.OFMEET_CONTACTS_MGR) setupPushNotification();

        if (interfaceConfig.OFMEET_ALLOW_UPLOADS)
        {
            console.debug("postLoadSetup", dropZone);
            dropZone.addEventListener('dragover', handleDragOver, false);
            dropZone.addEventListener('drop', handleDropFileSelect, false);
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

    function createContactsButton()
    {
        const contactsButton = addToolbarItem('ofmeet-contacts', '<div id="ofmeet-contacts" class="toolbox-icon "><div class="jitsi-icon" style="font-size: 12px;">' + IMAGES.contact + '</div></div>', "Contacts Manager");

        if (contactsButton) contactsButton.addEventListener("click", function(evt)
        {
            evt.stopPropagation();
            doContacts();
        });
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
                            const parent = evt.target.parentNode;
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

                    for (var i = 0; i < localStorage.length; i++)
                    {
                        if (localStorage.key(i).indexOf("pade.webpush.") == 0)
                        {
                            const name = localStorage.key(i).substring(13);
                            const email = localStorage.getItem("pade.email." + name);
                            addContact(name, email);
                        }
                    }
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
