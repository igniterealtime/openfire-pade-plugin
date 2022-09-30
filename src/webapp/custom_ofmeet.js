var ofmeet = (function (ofm) {
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
        avatar: '<svg width="24" height="24" viewBox="0 0 489 489"><path d="M417.4,71.6C371.2,25.4,309.8,0,244.5,0S117.8,25.4,71.6,71.6S0,179.2,0,244.5s25.4,126.7,71.6,172.9S179.2,489,244.5,489 s126.7-25.4,172.9-71.6S489,309.8,489,244.5S463.6,117.8,417.4,71.6z M244.5,462C124.6,462,27,364.4,27,244.5S124.6,27,244.5,27 S462,124.6,462,244.5S364.4,462,244.5,462z"/><path d="M244.5,203.2c35.1,0,63.6-28.6,63.6-63.6s-28.5-63.7-63.6-63.7s-63.6,28.6-63.6,63.6S209.4,203.2,244.5,203.2z M244.5,102.9c20.2,0,36.6,16.4,36.6,36.6s-16.4,36.6-36.6,36.6s-36.6-16.4-36.6-36.6S224.3,102.9,244.5,102.9z"/><path d="M340.9,280.5c-22.3-32.8-54.7-49.5-96.4-49.5s-74.1,16.6-96.4,49.5c-16.6,24.4-27.2,57.7-31.4,98.7 c-0.8,7.4,4.6,14.1,12,14.8c7.4,0.8,14.1-4.6,14.8-12c8.5-82.3,42.5-124,101-124s92.5,41.7,101,124c0.7,6.9,6.6,12.1,13.4,12.1 c0.5,0,0.9,0,1.4-0.1c7.4-0.8,12.8-7.4,12-14.8C368.1,338.1,357.5,304.9,340.9,280.5z"/></svg>',
        deleteAvatar: '<svg width="24" height="24" viewBox="0 0 24 24"><path d="M3.034 21.713a2.241 2.241 0 002.242 2.241h13.449a2.24 2.24 0 002.24-2.241V6.022H3.034zM15.736 9.758a.747.747 0 011.494 0v10.461a.747.747 0 01-1.494 0zm-4.483 0a.747.747 0 011.493 0v10.461a.746.746 0 11-1.493 0zm-4.483 0a.747.747 0 011.494 0v10.461a.747.747 0 11-1.494 0zM21.713 1.54h-5.604l-.438-.872a1.122 1.122 0 00-1.005-.622H9.328a1.104 1.104 0 00-.998.622l-.44.872H2.288a.748.748 0 00-.748.747v1.495c0 .412.335.747.748.747h19.425a.748.748 0 00.747-.747V2.287a.748.748 0 00-.747-.747z"/></svg>',
        changeColor: '<svg width="24" height="24" viewBox="0 0 24 24"><path d="M11.998.078C5.415.078.076 5.415.076 12c0 6.586 5.339 11.922 11.922 11.922 6.586.003 11.925-5.336 11.925-11.922 0-6.585-5.339-11.922-11.925-11.922zm-1.256 2.574h2.524l6.266 16.441H17.22l-1.499-4.218h-7.41l-1.498 4.218H4.466l6.276-16.441zm1.256 2.191L8.98 13.025h6.047l-3.029-8.182z"/></svg>',
        cursor: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M26.56 21.272a.998.998 0 00-.098-1.15L9.828.356A1.001 1.001 0 008.062 1v26.094a1 1 0 001.802.596l4.472-6.024 3.102 8.874a2.18 2.18 0 004.116-1.44l-3.044-8.706 6.996 1.354a.998.998 0 001.054-.476z"/></svg>',
        cryptpad: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M16.057 0l-9.9 1.824a2.129 2.129 0 10-2.26 3.583v14.257c0 1.351.607 2.803 1.789 4.292a22.185 22.185 0 004.425 4.087 43.451 43.451 0 003.85 2.441c.148.492.462.938.966 1.229 1.201.694 2.646.058 3.063-1.145a43.4 43.4 0 003.982-2.525 21.993 21.993 0 004.426-4.087c1.184-1.52 1.791-2.977 1.791-4.292V5.407c1.431-.798 1.457-2.846.053-3.685a2.096 2.096 0 00-2.319.102L16.057 0zm-.031 2.096l9.02 1.652c.011.128.035.255.071.376l-5.306 3.479a5.172 5.172 0 00-7.639.033l-5.238-3.38c.051-.153.089-.309.102-.47l8.99-1.69zm9.732 3.04c.18.154.385.282.608.37v13.855c.004.195-.009.385-.037.574a7.22 7.22 0 01-1.382 2.634 19.059 19.059 0 01-3.819 3.516 36.8 36.8 0 01-3.628 2.287 2.127 2.127 0 00-2.575-.346c-.147.084-.27.189-.387.297a37.297 37.297 0 01-3.546-2.239 18.816 18.816 0 01-3.785-3.514 6.668 6.668 0 01-1.488-3.14V5.54c.209-.08.403-.193.574-.336l6.692 4.288a3.413 3.413 0 013.041-1.823 3.39 3.39 0 013.038 1.824l6.694-4.357zM11.024 9.628a5.042 5.042 0 001.421 5.204l-1.819 3.683h-.04a1.573 1.573 0 101.188 2.603h2.7v-1.757h-2.302l2.162-4.327a.905.905 0 00-.27-1.113 3.282 3.282 0 01-1.45-3.245l-1.591-1.048zm9.969.034l-1.587 1.049c.018.134.027.269.032.405a3.377 3.377 0 01-1.452 2.805.91.91 0 00-.269 1.113l2.126 4.327h-2.297v1.757h2.772c.298.344.731.541 1.186.541a1.572 1.572 0 10-.111-3.138l-1.82-3.688a5.133 5.133 0 001.42-5.171zm-5 .17a1.422 1.422 0 00-.714 2.651 1.421 1.421 0 002.134-1.228v-.005c0-.785-.637-1.418-1.42-1.418z"/></svg>',
        confetti: '<svg width="24" height="24" viewBox="0 0 32 32"><path d="M30 8H18.084v6H32v-4a2 2 0 00-2-2zM2 8a2 2 0 00-2 2v4h14V8H2zm0 8v14a2 2 0 002 2h10V16H2zm26 16a2 2 0 002-2V16H18.084v16H28zM15.998 5.984h.006A.047.047 0 0016 6h8c2.762 0 4-1.344 4-3s-1.238-3-4-3c-2.586 0-4.622 1.164-6 2.514a4.018 4.018 0 00-2.058-.576c-.724 0-1.394.204-1.982.536C12.584 1.14 10.56 0 8 0 5.238 0 4 1.344 4 3s1.238 3 4 3h8l-.002-.016zM26 3c0 .826-1.088 1-2 1h-4.542c-.016-.028-.03-.058-.046-.084C20.428 2.928 21.968 2 24 2c.912 0 2 .174 2 1zM6 3c0-.826 1.088-1 2-1 1.988 0 3.496.89 4.512 1.844-.032.05-.056.104-.086.156H8c-.912 0-2-.174-2-1z"/></svg>',
    };
    const padsList = [],
        captions = { msgsDisabled: true, msgs: [] },
        pdf_body = [];
    const lostAudioWorkaroundInterval = 300000; // 5min
    const i18n = i18next.getFixedT(null, 'ofmeet');
    const avatarFileSizeLimit = 1024 * 1024 * 2; //2MiB
    const AvatarType = {
        UPLOAD: 'avatar.upload',
        VCARD: 'avatar.vcard',
        INITIALS: 'avatar.initials'
    };
    const BreakoutState = {
        STOPPED: 'breakout.stopped',
        STARTED: 'breakout.started',
        STARTING: 'breakout.start',
        STOPPING: 'breakout.stop'
    };

    let tagsModal = null,
        padsModal = null,
        breakoutModal = null,
        contactsModal = null;
    let padsModalOpened = false,
        contactsModalOpened = false,
        swRegistration = null,
        participants = {},
        recordingAudioTrack = {},
        recordingVideoTrack = {},
        videoRecorder = {},
        recorderStreams = {},
        customStore = {},
        filenames = {},
        dbnames = [];
    let clockTrack = { start: 0, stop: 0, joins: 0, leaves: 0 },
        handsRaised = 0;
    let tags = { location: '', date: '', subject: '', host: '', activity: '' };
    let audioTemporaryUnmuted = false,
        cursorShared = false,
        inviteByPhone = false;
    let avatarType = null;
    let localDisplayName = null;
    let vcardAvatar = null;
    let storage = null;
    let breakoutHost = null;
    let breakoutClient = null;
    let hashParams = [];
    let inprogressList = {};

    class DummyStorage {
        constructor() {
            this.data = {};
        }

        get length() {
            return this.data.length;
        }

        key(index) {
            return (Object.keys(this.data))[index];
        }

        getItem(key) {
            return this.data[key];
        }

        setItem(key, value) {
            this.data[key] = value;
        }

        removeItem(key) {
            delete this.data[key];
        }

        clear() {
            this.data = {};
        }
    }

    //-------------------------------------------------------
    //
    //  window events
    //
    //-------------------------------------------------------

    function isElectron() {
        return navigator.userAgent.indexOf('Electron') >= 0;
    }

    function storageAvailable(type) {
        let s;
        try {
            s = window[type];
            var x = '__storage_test__';
            s.setItem(x, x);
            s.removeItem(x);
            return true;
        } catch (e) {
            return false;
        }
    }

    function formatTimeSpan(totalSeconds) {
        const secs = ('00' + parseInt(totalSeconds % 60, 10)).slice(-2);
        const mins = ('00' + parseInt((totalSeconds / 60) % 60, 10)).slice(-2);
        const hrs = ('00' + parseInt((totalSeconds / 3600) % 24, 10)).slice(-2);
        return `${hrs}:${mins}:${secs}`;
    }

    window.addEventListener("DOMContentLoaded", function () {
        console.debug("custom_ofmeet.js DOMContentLoaded");

        parseHashParams();

        if (storageAvailable('localStorage')) {
            storage = window.localStorage;

            if (!isElectron() && navigator.credentials && navigator.credentials.preventSilentAccess && typeof PasswordCredential === 'function') {
                // Credential Management API is supported!
                navigator.credentials.get({ password: true, mediation: "silent" }).then(function (credential) {
                    console.debug("credential management api get", credential);
                    if (credential) {
                        user = credential.id;
                        if (user.indexOf('@') == -1) {
                            user += '@' + config.hosts["domain"];
                        }
                        storage.setItem("xmpp_username_override", user);
                        storage.setItem("xmpp_password_override", credential.password);
                        console.debug("credentials passed to local store");
                    }
                }).catch(function (err) {
                    console.error("credential management api get error", err);
                });
            }

        } else {
            storage = new DummyStorage();
        }

		if (typeof indexedDB.databases == "function") {
			indexedDB.databases().then(function (databases) {
				console.debug("custom_ofmeet.js found databases", databases);

				databases.forEach(function (db) {
					if (db.name.indexOf("ofmeet-db-") > -1) recoverRecording(db.name);
				})
			})
		}
		if (window.webkitSpeechRecognition && !isElectron()) setupVoiceCommand()
			
		const actionChannel = new BroadcastChannel('ofmeet-notification-event');
		
		actionChannel.addEventListener('message', event =>
		{
			console.debug("sw notication action", event.data);
			
			if (event.data.action == "accept") {
				const start = event.data.payload.msgDate + "T" + event.data.payload.msgTime + ":00";
				const key = "ofmeet.calendar." + start;
				const title = "Join " + event.data.payload.roomName + ` (${event.data.payload.sender})`;
				const url = event.data.payload.url;
				
				storage.setItem(key, JSON.stringify({title, url, start}));
			}					

		});			

        if ($('#welcome_page').length) {
			setTimeout(setupWelcomePage);
		} else {
        	setTimeout(preSetup);
        }
    });

    window.addEventListener("beforeunload", function (event) {
        console.debug("custom_ofmeet.js beforeunload");

        if (APP.connection) {
            if (dbnames.length > 0 || ofm.recording) {
                event.preventDefault();
                event.returnValue = '';
            }

            if (ofm.recording) stopRecorder();

            dbnames.forEach(function (dbname) {
                const deleteRequest = indexedDB.deleteDatabase(dbname)

                deleteRequest.onsuccess = function (event) {
                    console.debug("custom_ofmeet.js me database deleted successfully", dbname);
                };
            });

            if (dbnames.length > 0 || ofm.recording) {
                return event.returnValue;
            }
        }
    });

    //-------------------------------------------------------
    //  Auto-hide Mouse
    //-------------------------------------------------------

    if (interfaceConfig.OFMEET_MOUSECURSOR_TIMEOUT && interfaceConfig.OFMEET_MOUSECURSOR_TIMEOUT > 0) {
        console.debug("mousecursor timeout: " + interfaceConfig.OFMEET_MOUSECURSOR_TIMEOUT);
        let mouseIdleTimer;
        let mouseIsHidden = false;
        document.addEventListener("mousemove", function () {
            if (mouseIdleTimer) {
                clearTimeout(mouseIdleTimer);
            }
            mouseIdleTimer = setTimeout(function () {
                if (!mouseIsHidden) {
                    document.querySelector("body").style.cursor = "none";
                    mouseIsHidden = true;
                }
            }, interfaceConfig.OFMEET_MOUSECURSOR_TIMEOUT);
            if (mouseIsHidden) {
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

	function mobileCheck() {
	  let check = false;
	  (function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4))) check = true;})(navigator.userAgent||navigator.vendor||window.opera);
	  return check;
	}

    function parseHashParams() {
        const hash = location.hash.replace(/^#/, '');
        hashParams = [...new URLSearchParams(hash).entries()].reduce((obj, e) => {
            let val;
            try {
                val = JSON.parse(e[1]);
            } catch (error) {
                console.error('Hash param is not a JSON format: ' + e[1]);
                val = e[1];
            }
            return { ...obj, [e[0]]: val };
        }, {});
    }

    function newElement(el, id, html, className, label) {
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

    function setupVoiceCommand() {
        const enter_room_button = document.getElementById('enter_room_button');

        if (enter_room_button) {
            const button = newElement('div', "speak_room_button", IMAGES.mic, 'speaker-room-button', "Speak Meeting Room Name");

            button.addEventListener("click", function (evt) {
                evt.stopPropagation();
                button.disabled = true;
                button.style.visibility = "hidden";

                const recognition = new webkitSpeechRecognition();
                recognition.continuous = false;
                recognition.interimResults = false;
                recognition.lang = config.defaultLanguage;

                recognition.onresult = function (e) {
                    console.debug("Speech command event", e)

                    if (e.results[e.resultIndex].isFinal) {
                        const resultat = e.results[event.resultIndex][0].transcript;
                        console.debug("Speech command transcript", resultat);

                        document.getElementById('enter_room_field').value = resultat;
                        recognition.stop();
                        window.location.replace(encodeURI(resultat));
                    }
                };

                recognition.onspeechend = function (event) {
                    console.debug("Speech command onspeechend", event);
                }

                recognition.onstart = function (event) {
                    console.debug("Speech command started", event);
                }

                recognition.onerror = function (e) {
                    console.debug("Speech command error", e)
                    recognition.stop();
                }

                recognition.start();
            });

            button.style.margin = "5px";
            enter_room_button.parentNode.appendChild(button);
        }
    }
		
    function preSetup() {
        if (!APP.connection || !APP.connection.xmpp.connection) {
            setTimeout(preSetup);
            return;
        }
		
        APP.connection.xmpp.connection.addHandler(handleMessage, null, "message");
        APP.connection.xmpp.connection.addHandler(handleMucMessage, "urn:xmpp:json:0", "message");
        APP.connection.xmpp.connection.addHandler(handlePresence, null, "presence");
		
        console.debug("custom_ofmeet.js pre-setup");		
		setup();	
	}	

    function setup() {
        if (!APP.connection || !APP.conference || !APP.conference.isJoined()) {
            setTimeout(setup);
            return;
        }
		
        console.debug("custom_ofmeet.js setup");
		
		const room = getConference();
		listenWebPushEvents();

		if (hashParams.subject) {
			setConferenceName(hashParams.subject);
		}

		breakoutClient = new BreakoutClient();

		room.on(JitsiMeetJS.events.conference.CONFERENCE_LEFT, function () {
			console.debug("custom_ofmeet.js me left");

			if (interfaceConfig.OFMEET_RECORD_CONFERENCE) {
				if (ofm.recording) stopRecorder();

				const ids = Object.getOwnPropertyNames(recordingVideoTrack);

				ids.forEach(function (id) {
					delete recordingAudioTrack[id];
					delete recordingVideoTrack[id];
				});
			}
		});

		room.on(JitsiMeetJS.events.conference.USER_ROLE_CHANGED, function (user, role) {
			console.debug("custom_ofmeet.js participant role change", user, role);

			if (interfaceConfig.OFMEET_ENABLE_BREAKOUT &&
				role == "moderator" &&
				user == APP.conference.getMyUserId() &&
				breakoutClient.state != BreakoutState.STARTED) {
				$('#ofmeet-breakout').parent().show();
			}
		});

		room.on(JitsiMeetJS.events.conference.TRACK_REMOVED, function (track) {
			console.debug("custom_ofmeet.js track removed", track.getParticipantId());

			if (track.getParticipantId() == APP.conference.getMyUserId()) {
				clockTrack.leaves = (new Date()).getTime();
				hideClock();

				if (ofm.recognition) {
					ofm.recognitionActive = false;
					ofm.recognition.stop();
				}

				if (ofm.recording) stopRecorder();
			}
		});

		room.on(JitsiMeetJS.events.conference.USER_JOINED, function (id) {
			console.debug("user join", id, participants);
			addParticipant(id);
            publishWebPush();
		});


		room.on(JitsiMeetJS.events.conference.USER_LEFT, function (id) {
			console.debug("user left", id);
			removeParticipant(id);
		});
		

		room.on(JitsiMeetJS.events.conference.TRACK_ADDED, function (track) {
			const id = track.getParticipantId();
			console.debug("custom_ofmeet.js track added", id, track.getType());

			if (interfaceConfig.OFMEET_RECORD_CONFERENCE) {
				if (track.getType() == "audio") recordingAudioTrack[id] = track.stream;
				if (track.getType() == "video") recordingVideoTrack[id] = track.stream;
			}
		});

		room.on(JitsiMeetJS.events.conference.PARTICIPANT_PROPERTY_CHANGED, function (participant, property, oldValue, newValue) {
			console.debug("custom_ofmeet.js property changed", participant, property, oldValue, newValue);

			if (property == 'mainRoomUserId') {
				if (breakoutHost) {
					breakoutHost.recallParticipant(participant._id, newValue);
				}
			}
		});

		room.on(JitsiMeetJS.events.conference.TRACK_MUTE_CHANGED, function (track) {
			const id = track.getParticipantId();
			console.debug("custom_ofmeet.js track muted", id, track.getType(), track.isMuted());

			if (interfaceConfig.OFMEET_RECORD_CONFERENCE) {
				if (track.getType() == "audio" && recordingAudioTrack[id]) recordingAudioTrack[id].getAudioTracks()[0].enabled = !track.isMuted();
				if (track.getType() == "video" && recordingVideoTrack[id]) recordingVideoTrack[id].getVideoTracks()[0].enabled = !track.isMuted();

				const recordingStream = recorderStreams[id];

				if (recordingStream) // recording active
				{
					if (track.getType() == "audio") recordingStream.getAudioTracks()[0].enabled = !track.isMuted();
					if (track.getType() == "video") recordingStream.getVideoTracks()[0].enabled = !track.isMuted();
				}
			}

			if (APP.conference.getMyUserId() == id) {
				if (track.getType() == "audio" && ofm.recognition) {
					if (track.isMuted()) // speech recog synch
					{
						console.debug("audio muted, stopping speech transcription");

						ofm.recognitionActive = false;
						ofm.recognition.stop();

					} else {
						console.debug("audio unmuted, starting speech transcription");
						ofm.recognition.start();
					}
				}
			}
		});

		room.on(JitsiMeetJS.events.conference.PRIVATE_MESSAGE_RECEIVED, function (id, text, ts) {
			var participant = APP.conference.getParticipantById(id);
			var displayName = participant ? (participant._displayName || 'Anonymous-' + id) : (getLocalDisplayName() || "Me");

			console.debug("custom_ofmeet.js private message", id, text, ts, displayName);

			const pretty_time = dayjs().format('MMM DD HH:mm:ss');
			pdf_body.push([pretty_time, displayName, text]);
		});

		room.on(JitsiMeetJS.events.conference.MESSAGE_RECEIVED, function (id, text, ts) {
			var participant = APP.conference.getParticipantById(id);
			var displayName = participant ? (participant._displayName || 'Anonymous-' + id) : (getLocalDisplayName() || "Me");

			console.debug("custom_ofmeet.js message", id, text, ts, displayName, participant, padsModalOpened);

			if (text.indexOf(interfaceConfig.OFMEET_CRYPTPAD_URL) == 0) {
				if (padsModalOpened) notifyText(displayName, text, id, function (id, button) {
					if (button == 0) openPad(text);
				})

				if (padsModalOpened) {
					addPad(text);
				} else {
					padsList.push(text);
				}
			} else {

				if (text.indexOf("http") != 0 && !captions.msgsDisabled) {
					if (captions.ele) {
					  captions.ele.innerHTML = displayName + " : " + text;
					  if (interfaceConfig.OFMEET_CHAT_CAPTIONS_TIMEOUT && (interfaceConfig.OFMEET_CHAT_CAPTIONS_TIMEOUT > 0)) {
						if (captions.timerHandle) window.clearTimeout(captions.timerHandle);
						captions.timerHandle = window.setTimeout(function() {
						  captions.ele.innerHTML = "";
						}, interfaceConfig.OFMEET_CHAT_CAPTIONS_TIMEOUT);
					  }
					}
					captions.msgs.push({ text: text, stamp: (new Date()).getTime() });
				}

				const pretty_time = dayjs().format('MMM DD HH:mm:ss');
				pdf_body.push([pretty_time, displayName, text]);
			}

			if (breakoutHost && breakoutHost.started) {
				breakoutHost.broadcastMessage(text);
			}
		});

		captions.ele = document.getElementById("captions");

        if (storage.getItem('ofmeet.settings.avatar')) {
            console.debug('custom_ofmeet.js found avatar');
            const dataUri = JSON.parse(storage.getItem('ofmeet.settings.avatar'));
            changeAvatar(dataUri, AvatarType.UPLOAD);
        } else {
            changeAvatar(createAvatar(getLocalDisplayName()), AvatarType.INITIALS);
        }

        setOwnPresence();

        if (APP.connection.xmpp.connection._stropheConn.pass || config.ofmeetWinSSOEnabled) {
            if (storageAvailable('localStorage')) {

                storage.removeItem("xmpp_username_override");
                storage.removeItem("xmpp_password_override");
                console.debug("credentials in local store cleared");
				
				const storeLocally = function() {
					const jid = APP.connection.xmpp.connection._stropheConn.authzid;
					const pass = APP.connection.xmpp.connection._stropheConn.pass;
					storage.setItem("xmpp_username_override", jid);
					storage.setItem("xmpp_password_override", pass);
					console.debug("credentials put to local store for" + jid);					
				}

                if (interfaceConfig.OFMEET_CACHE_PASSWORD) {
                    if (!isElectron() && navigator.credentials && navigator.credentials.preventSilentAccess && typeof PasswordCredential === 'function') {
                        const id = APP.connection.xmpp.connection._stropheConn.authcid;
                        const pass = APP.connection.xmpp.connection._stropheConn.pass;
						
                        navigator.credentials.create({ password: { id: id, password: pass } }).then(function (credential) {
                            navigator.credentials.store(credential).then(function () {
                                console.debug("credential management api put", credential);

                            }).catch(function (err) {
                                console.error("credential management api put error", err);
								storeLocally();								
                            });
                        }).catch(function (err) {
                            console.error("credential management api put error", err);
							storeLocally();							
                        });
                    } else {
						storeLocally();
                    }
                }
            }

            if (config.ofmeetWebAuthnEnabled) {
                storage.removeItem("ofmeet.webauthn.disable"); // reset user disable

                if (!storage.getItem("ofmeet.webauthn.username")) {
                    registerWebAuthn();
                }
            }

            getVCard();
            getBookmarks();
        }

        getServiceWorker(function (registration) {
            console.debug('Service worker registered', registration);
        });

        setTimeout(postLoadSetup);
        setTimeout(postJoinSetup);

        console.debug("custom_ofmeet.js setup", APP.connection, captions);

        setTimeout(lostAudioWorkaround, 5000);
    }

    function postLoadSetup() {
        var dropZone = document.getElementById("videospace");

        console.debug("postLoadSetup", dropZone);

        if (!dropZone) {
            setTimeout(postLoadSetup, 1000);
            return;
        }

        if (interfaceConfig.OFMEET_CONTACTS_MGR) setupPushNotification();

        if (interfaceConfig.OFMEET_ALLOW_UPLOADS) {
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

        // setup drag& drop
        console.debug("add drag&drop handlers to local and participants windows");
        addParticipantDragDropHandlers(document.getElementById("filmstripLocalVideoThumbnail"));
        getOccupants();

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
            if (interfaceConfig.OFMEET_ENABLE_BREAKOUT && APP.conference._room.isModerator() && breakoutClient.state != BreakoutState.STARTED) {
                $('#ofmeet-breakout').parent().show();
            }
        });

        if (interfaceConfig.OFMEET_ENABLE_BREAKOUT) {
            createBreakoutRoomsButton();
            if (!APP.conference._room.isModerator() || breakoutClient.state == BreakoutState.STARTED) {
                $('#ofmeet-breakout').parent().hide();
                APP.conference.commands.sendCommandOnce("___FAKE_INTERACTION", { value: !0 });
            }
        }

        if (interfaceConfig.OFMEET_RECORD_CONFERENCE) {
            createRecordButton();
            createPhotoButton();
            createDesktopButton();

            if (APP.conference.getMyUserId()) {
                showClock();
                clockTrack.joins = (new Date()).getTime();
            }
        }

        if (interfaceConfig.OFMEET_TAG_CONFERENCE) {
            if (interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION && window.webkitSpeechRecognition && !isElectron()) {
                setupSpeechRecognition();
            }

            captions.msgsDisabled = !interfaceConfig.OFMEET_ENABLE_CAPTIONS || interfaceConfig.OFMEET_STARTWITH_CAPTIONS_DISABLED;
            captions.transcriptDisabled = !interfaceConfig.OFMEET_ENABLE_TRANSCRIPTION || interfaceConfig.OFMEET_STARTWITH_TRANSCRIPTION_DISABLED || isElectron();

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
    //  setup welcome page
    //
    //-------------------------------------------------------

    function setupWelcomePage() {

        if (interfaceConfig.IN_PROGRESS_LIST_ENABLED) {		
			setupInprogressList();
		}
		
        if (interfaceConfig.OFMEET_CONTACTS_MGR) {		
			setupCalendarView();	
		}			
	}

    function setupInprogressList() {
		$('#react').on('click.tab-button', '.tab-buttons .tab', () => setTimeout(() => refreshInprogressListDOM(), 0));

		if (interfaceConfig.IN_PROGRESS_LIST_INTERVAL > 0) 
		{			
			if (window.inprogressListUpdateInterval) {
				clearInterval(window.inprogressListUpdateInterval);
			}			
			window.inprogressListUpdateInterval = setInterval(() => updateInprogressList(), interfaceConfig.IN_PROGRESS_LIST_INTERVAL * 1000);
		}
		updateInprogressList();
    }

    function updateInprogressList() {
        fetch("inProgressList.json")
            .then(res => res.ok && res.json())
            .then(data => {
                inprogressList = data;
                refreshInprogressListDOM();
            })
            .catch(error => console.error(error));
    }
	
	function refreshInprogressListDOM() {
		let $container = $('#inprogress_list')
		
		if ($container.length) {
            if (!$container.hasClass('meetings-list')) {
                $container.attr({
                    'aria-label': i18n('welcomepage.inProgressList'),
                    'class': "meetings-list",
                    'role': "menu",
                    'tabindex': "-1"})
                    .on('click.inprogress-list', '.with-click-handler', (e) => {
                        location.href = $(e.currentTarget).data('url');
                    });
            }

            let html = '';
            if (inprogressList && inprogressList.length) {
                inprogressList.forEach(item => {
                    html +=
                        `<div aria-label="test" class="item with-click-handler" role="menuitem" tabindex="0" data-url="${item.url}">
                            <div class="left-column">
                                <span class="title">${localizedDate(item.date).format('LL')}</span><span class="subtitle">${localizedDate(item.date).format('LT')}</span>
                            </div>
                            <div class="right-column">
                                <span class="title">${item.name}</span><span class="subtitle">${formatTimeSpan(item.duration / 1000)}</span>
                            </div>
                        </div>`;
                })
            } else {
                html =
                    `<div aria-describedby="meetings-list-empty-description" aria-label="${i18n('welcomepage.inProgressList')}" class="meetings-list-empty" role="region">
                        <span class="description" id="meetings-list-empty-description">${i18n('welcomepage.inProgressListEmpty')}</span>
                    </div>`;
            }

            $container.empty().append(html);
		}
	}
	
	function setupCalendarView() {		
		const welcome = document.querySelector('#welcome_page');		
		let container = document.querySelector('#ofmeet_calendar');	
		
		function setupFullCalendar() {
			container = document.querySelector('#ofmeet_calendar');	

			if (!container) {
				setTimeout(setupFullCalendar, 500);
				return;
			}	
			
			let html = "<div id='full_calendar' style='color:black; background-color:white;'></div>";
			container.innerHTML = html;
			calendarEl = document.querySelector('#full_calendar');
			
			const config =  {
				selectable: true,				
				initialView: 'timeGridDay',
				headerToolbar: {
					left: 'prev,next today',
					center: 'title',
					right: 'dayGridMonth,timeGridWeek,timeGridDay'
				},
				dateClick: function(info) {
					console.debug("dateClick", info)
				},
			    select: function(info) {
					console.debug("select", info)
			    },				
				events: []
			};
			
			for (var i = 0; i < storage.length; i++) 
			{
				if (storage.key(i).indexOf("ofmeet.calendar.") == 0) {
					config.events.push(JSON.parse(storage.getItem(storage.key(i))));
				}
			}			

			console.debug("setupCalendarView", calendarEl, config);
			
			calendar = new FullCalendar.Calendar(calendarEl, config);
			calendar.render();	

			if (window.calendarInterval) {
				clearInterval(window.calendarInterval);
			}
			
			window.calendarInterval = setInterval(checkForMeetings, 300000);		
			checkForMeetings();		
		}
			
		if (welcome) {					
			if (!container) {
				setTimeout(setupCalendarView, 500);
				return;
			}	

			let calendarEl = document.querySelector('#full_calendar');
			
			if (!calendarEl) {
				setupFullCalendar();

				const tab = document.querySelector('.tab.selected');
				
				if (tab) tab.addEventListener("click", function (evt) 
				{		
					if (evt.target.innerHTML == "Calendar") {
						setupFullCalendar();
					}
				})
			}				
		}
	}	
	
	function checkForMeetings() {
		console.debug("checkForMeetings");		
        fetch("inProgressList.json").then(res => res.ok && res.json()).then(data => notifyForMeeting(data)).catch(error => console.error(error));		
	}
	
	function notifyForMeeting(list)	{
		console.debug("notifyForMeeting", list);
		
		for (var i = 0; i < storage.length; i++) 
		{
			if (storage.key(i).indexOf("ofmeet.calendar.") == 0) {
				const meeting = JSON.parse(storage.getItem(storage.key(i)));
				const start = dayjs(meeting.start + ".000Z");
				const now = dayjs();
				
				const notifyStart = start.subtract(15, 'minute');
				const notifyStop = start.add(15, 'minute');
				
				const notifyBefore = now.isBefore(start) && now.isAfter(notifyStart);
				const notifyAfter = now.isBefore(notifyStop) && now.isAfter(start);
				
				console.debug("notifyForMeeting", meeting, start, now, notifyStart, notifyBefore, notifyAfter, list);
				
				if (notifyBefore) {
					notifyText(meeting.title, meeting.url, storage.key(i), (id, button) => {
						console.log("clicked button", button);
						if (button == 0) location.href = meeting.url;
					})					
				}
				else {
					list.forEach(item => 
					{
						if (item.url.toLowerCase() == meeting.url.toLowerCase() && notifyAfter) {
							notifyText(item.name, item.url, storage.key(i), (id, button) => {
								console.log("clicked button", button);								
								if (button == 0) location.href = item.url;
							})								
						}
					})					
				}
					
			}
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
            shortcut: 'O',
            callback: (evt) => {
                if (evt) evt.stopPropagation();
                doContacts();
            }
        });
    }

    function createRecordButton() {
        addToolbarItem({
            id: 'ofmeet-record',
            icon: IMAGES.record,
            label: i18n('toolbar.recordMeeting'),
            callback: (evt) => {
                evt.stopPropagation();

                if (!ofm.recording) {
                    startRecorder(startMeetingRecorder);
                } else {
                    stopRecorder();
                }
            }
        });

        const leaveButton = document.querySelector('div[aria-label="Leave the call"]');

        if (leaveButton) leaveButton.addEventListener("click", function (evt) {
            if (ofm.recording) stopRecorder();

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

                APP.UI.messageHandler.showWarning({title:"Recording", description:"Conference Photo Taken"});
            }
        });
    }

    function createDesktopButton() {
        addToolbarItem({
            id: 'ofmeet-desktop',
            icon: IMAGES.desktop,
            label: i18n('toolbar.recordDesktopApp'),
            callback: (evt) => {
                evt.stopPropagation();

                if (!ofm.recording) {
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
            shortcut: 'B',
            callback: (evt) => {
                if (evt) evt.stopPropagation();
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
            callback: (evt) => {
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
            callback: (evt) => {
                evt.stopPropagation();
                doPads();
            }
        });
    }

    function createAvatarButton() {
        addToolbarItem({
            id: 'ofmeet-avatar',
            icon: IMAGES.avatar,
            label: i18n('toolbar.changePersonalAvatar'),
            callback: (evt) => {
                evt.stopPropagation();
                doAvatar();
            },
            menu: {
                type: 'list',
                items: [
                    { id: 'ofmeet-avatar-delete', icon: IMAGES.deleteAvatar, text: i18n('avatar.deletePersonalAvatar') },
                    { id: 'ofmeet-avatar-changeColor', icon: IMAGES.changeColor, text: i18n('avatar.changeInitialsAvatarColor') }
                ],
                closeOnClick: true,
                callback: (evt) => {
                    switch (evt.target.id) {
                        case 'ofmeet-avatar-delete':
                            deleteUploadAvatar();
                            break;
                        case 'ofmeet-avatar-changeColor':
                            changeInitialsAvatarColor();
                            break;
                    }
                }
            }
        }).append('<input style="display:none" id="ofmeet-upload-avatar" type="file" name="files[]" accept="image/jpeg,image/png">');
    }

    function createShareCursorButton() {
        collab.init();

        addToolbarItem({
            id: 'ofmeet-cursor',
            icon: IMAGES.cursor,
            label: i18n('toolbar.shareCursorMousePointer'),
            shortcut: '!',
            callback: (evt) => {
                if (evt) evt.stopPropagation();

                if (!cursorShared) {
                    $('#ofmeet-cursor').addClass('toggled');
                    collab.startSharing(getNickColor());
                } else {
                    $('#ofmeet-cursor').removeClass('toggled');
                    collab.stopSharing();
                }
                cursorShared = !cursorShared;
            }
        });

        // Observe tile view status.
        // TODO: When the tile view change event is implemented in Jitsi Meet, it will be replaced with it.
        const localVideoTileViewContainer = document.getElementById('filmstripLocalVideoThumbnail')

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
            callback: (evt) => {
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

            const now = new Date();
            if (text) {
                const color = getNickColor();
                options = {
                    ...options,
                    particleCount: 1,
                    scalar: 5.0,
                    colors: [color],
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
                        'text:' + String.fromCodePoint(0x1F514) // :bell:
                    ]
                };
            } else if ((now.getTimezoneOffset() == 9 * 60) && ((now.getMonth() == 2 && now.getDate() >= 25) || (now.getMonth() == 3 && now.getDate() <= 15))) { // Hanami
                options = {
                    ...options,
                    shapes: [
                        'text:' + String.fromCodePoint(0x1F338) // :sakura:
                    ]
                };
            } else if ((now.getMonth() == 3 && now.getDate() >= 10) && (now.getMonth() == 3 && now.getDate() <= 18)) { // Holy Week and Easter 2022
                options = {
                    ...options,
                    shapes: [
                        'text:' + String.fromCodePoint(0x26EA), // :church:
                        'text:' + String.fromCodePoint(0x1F423), // :chicken:
                        'text:' + String.fromCodePoint(0x1F426), // :bird:
                        'text:' + String.fromCodePoint(0x1F430), // :rabbit:
                        'text:' + String.fromCodePoint(0x1F337), // :tulip:
                        'text:' + String.fromCodePoint(0x1F95A) // :egg:
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
            for (let emotiocon of Array.from(emoticonList)) {
                menu.items.push({ icon: emotiocon });
            }
        }

        addToolbarItem({
            id: 'ofmeet-confetti',
            icon: IMAGES.confetti,
            label: i18n('toolbar.shareSomeConfetti'),
            shortcut: 'E',
            callback: (evt) => { sendConfettiCommand() },
            menu: menu,
        });
    }

    function addToolbarItem(option) {		
		if (mobileCheck()) {		// don't extend toolbar for mobile browsers only register shortcut
            if (option.shortcut) {
                APP.keyboardshortcut.registerShortcut(option.shortcut, null, () => {
                    option.callback();
                }, option.label);
            }	
			return $();			
		}
		
        option = {
            id: undefined,
            icon: undefined,
            label: undefined,
            group: '.toolbox-content-items',
            shortcut: undefined,
            callback: undefined,
            menu: {},
            ...option
        };

        const $placeHolder = $(option.group);

        if (option.id && option.icon && option.label && $placeHolder.length && option.callback) {
            let $button = $(`
            <div aria-label="${option.label}" class="toolbox-button ofmeet-tooltip">
                <div id="${option.id}" class="toolbox-icon">
                    <div class="jitsi-icon jitsi-icon-default" style="font-size: 12px;">${option.icon}</div>
                </div>
            </div>`);
            $button.children('.toolbox-icon').on('click.ofmeet-toolbox-icon', option.callback);

            if (option.shortcut) {
                APP.keyboardshortcut.registerShortcut(option.shortcut, null, () => {
                    option.callback();
                }, option.label);
            }

            let $toolbarItem = appendMenuToToolbarButton($button, option.menu);

            if ($placeHolder.hasClass('toolbox-content-items')) {
                $placeHolder.children().eq(-2).before($toolbarItem);
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
                item = {
                    icon: '',
                    text: '',
                    shortcut: undefined,
                    ...item
                };

                let $item;
                switch (option.type) {
                    case 'list':
                        $item = $(`<li class="ofmeet-toolbox-menu-item"><span class="overflow-menu-item-icon"><div class="jitsi-icon jitsi-icon-default">${item.icon}</div></span><span class="profile-text">${item.text}</span></li>`);
                        break;
                    case 'tile':
                        $item = $(`<li class="ofmeet-toolbox-menu-item"><div class="jitsi-icon jitsi-icon-default">${item.icon}</div></li>`);
                        break;
                }

                if (item.attr) {
                    for (let key in item.attr) {
                        $item.attr(key, item.attr[key]);
                    }
                }

                if (item.id) {
                    $item.attr('id', item.id);
                }
                $menu.append($item);

                if (item.shortcut) {
                    APP.keyboardshortcut.registerShortcut(item.shortcut, null, () => {
                        $item.trigger('click.ofmeet-toolbox-menu');
                    }, item.text);
                }
            }
            $menu.on('click.ofmeet-toolbox-menu', 'li', (e) => { option.callback(e); return option.closeOnClick; });
            $menuContainer.children('.ofmeet-toolbox-menu').append($menu);

            $smallIcon = $('<div class="ofmeet-toolbox-small-icon"><svg fill="none" height="9" width="9" viewBox="0 0 10 7"><path clip-rule="evenodd" d="M8.411 6.057A.833.833 0 109.65 4.943L5.73.563a.833.833 0 00-1.24 0L.63 4.943a.833.833 0 001.24 1.114l3.24-3.691L8.41 6.057z"></path></svg></div>');
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
    //
    //  functions - common
    //
    //-------------------------------------------------------

    function getConference() {
        const state = APP.store.getState();
        return state['features/base/conference'].conference;
    }

    function getConferenceName() {
        const state = APP.store.getState();
        const { callee } = state['features/base/jwt'];
        const { callDisplayName } = state['features/base/config'];
        const { pendingSubjectChange, room, subject } = state['features/base/conference'];

        return pendingSubjectChange ||
            subject ||
            callDisplayName ||
            (callee && callee.name) ||
            safeStartCase(decodeURIComponent(room));
    }

    function setConferenceName(name) {
        if (name) {
            const room = getConference();
            room.setSubject(name);
            document.title = `${name} | ${interfaceConfig.APP_NAME}`;
        }
    }

    function getConferenceJid() {
        return getConference()?.room?.roomjid;
    }
	
	function getLocalDisplayName() {
		const settings = JSON.parse(storage.getItem("features/base/settings"));
		return settings?.displayName;
	}

	function getParticipantDisplayName(id) {
		const participant = APP.conference.getParticipantById(APP.conference.getMyUserId());
		return participant?._displayName;
	}
	
    function getAllParticipants() {
        const state = APP.store.getState();
        return (state["features/base/participants"].remote);
    }

    function getParticipant(id) {
        return getAllParticipants().get(id);
    }

    function safeStartCase(s = '') {
        return _.words(`${s}`.replace(/['\u2019]/g, '')).reduce(
            (result, word, index) => result + (index ? ' ' : '') + _.upperFirst(word), '');
    }

    function handlePresence(presence) {
        //console.debug("handlePresence", presence);

        const id = Strophe.getResourceFromJid(presence.getAttribute('from'));
        const raisedHand = presence.querySelector('jitsi_participant_raisedHand');
        const email = presence.querySelector('email');
        const nick = presence.querySelector('nick');
        const avatarURL = presence.querySelector('avatar-url');

        if (raisedHand) {			
            const handsRaised = APP.store.getState()["features/base/participants"].raisedHandsQueue.length;
            const handsTotal = APP.conference.membersCount;
            const handsPercentage = Math.round(100 * handsRaised / handsTotal);
            $('div#raisedHandsCountLabel > span').html(handsRaised + '/' + handsTotal + ' (' + handsPercentage + '%)');
            const label = handsRaised > 0 ? i18n('handsRaised.handsRaised', { raised: handsRaised, total: handsTotal, percentage: handsPercentage }) : "";
            if (captions.timerHandle) window.clearTimeout(captions.timerHandle);
            if (captions.ele) captions.ele.innerHTML = label;
            captions.msgs.push({ text: label, stamp: (new Date()).getTime() });      
        }

        if (email) {
            if (participants[id]) {
                participants[id].ofEmail = email.innerHTML;
                storage.setItem('pade.email.' + participants[id]._displayName, email.innerHTML);
            }
        }

        if (nick) {
            if (nick.innerHTML != "") {
                if (APP.conference.getMyUserId() == id) {
                    if (localDisplayName != getLocalDisplayName()) {
                        localDisplayName = getLocalDisplayName();
                        changeAvatar(createAvatar(localDisplayName), AvatarType.INITIALS);
                    }
                } else {
                    if (id in participants && participants[id]._displayName != getParticipantDisplayName(id)) {
                        participants[id] = APP.conference.getParticipantById(id);
                        if (breakoutHost) {
                            breakoutHost.updateParticipant(id);
                        }
                    }
                }
            }
        }

        if (avatarURL) {
            if (avatarURL.innerHTML != "") {
                if (APP.conference.getMyUserId() != id && id in participants && participants[id].avatarURL != avatarURL.innerHTML) {
                    participants[id].avatarURL = avatarURL.innerHTML
                    if (breakoutHost) {
                        breakoutHost.updateParticipant(id);
                    }
                }
            }
        }

        return true;
    }

    function handleMessage(msg) {		
        if (msg.getAttribute("type") == 'headline') // alert message
        {
            const body = msg.querySelector('body');

            if (body) {
                console.debug("alert message", body.innerHTML);
                APP.UI.messageHandler.showError({ title: "System Adminitrator", description: body.innerHTML, hideErrorSupportLink: true });
            }
        }

        return true;
    }

    function handleMucMessage(msg) {
       console.debug("handleMucMessage", getConferenceJid(), msg);		
        const participant = Strophe.getResourceFromJid(msg.getAttribute("from"));

        if (msg.getAttribute("type") == "error") {
            console.error(msg);
            return true;
        }
		
		if (!getConferenceJid()) {
			setTimeout(() => {handleMucMessage(msg)}, 1000 );	// wait for jitsi-meet
		}

        if (getConferenceJid() != Strophe.getBareJidFromJid(msg.getAttribute("from"))) {
            return true;
        }

        const payload = msg.querySelector('json');

        if (payload) {
            const json = JSON.parse(payload.innerHTML);
            console.debug("handleMucMessage", participant, json);

            switch (json.action) {
                case 'start-breakout':
                    if (breakoutClient && !breakoutHost) {
                        breakoutClient.startBreakout(json.url, json.wait, json.room, json.subject, json.startTime, json.endTime);
                    }
                    break;
                case 'stop-breakout':
                    if (breakoutClient && !breakoutHost) {
                        breakoutClient.stopBreakout(json.url, json.wait);
                    }
                    break;
				case 'push-room-properties':
					handleRoomProperties(json);
					break;
				case 'plan-new-meeting':
					handleMeetingInvitation(json);
					break;					
                default:
                    console.error("unknown MUC message");
                    return true;
            }
        }

        return true;
    }
	
	function handleRoomProperties(json)	{
        const keys = Object.getOwnPropertyNames(json);

        keys.forEach(function (key) {
			console.debug("handleRoomProperties", key, json[key]);
			interfaceConfig[key] = json[key];
        })		
	}
	
	function handleMeetingInvitation(json) {
		const message = json.sender + ' invites you to join the room ' + json.roomName + " on " + json.msgDate + " at " + json.msgTime;		
		
		const options = {
			body: message,
			icon: './icon.png',
			data: json,
			requireInteraction: true,
			actions: [
			  {action: 'accept', title: 'Accept', icon: './check-solid.png'},
			  {action: 'reject', title: 'Reject', icon: './times-solid.png'}		  
			]
		};
		
		if (swRegistration) {
			swRegistration.showNotification(interfaceConfig.APP_NAME, options);	
		}
	}

    //-------------------------------------------------------
    //  WORKAROUND: prevent disruption of a muted audio connection by a short toggle
    //-------------------------------------------------------

    function lostAudioWorkaround() {
        if (APP.conference.isLocalAudioMuted() || audioTemporaryUnmuted) {
            APP.conference.toggleAudioMuted(false);
            audioTemporaryUnmuted = !audioTemporaryUnmuted;
            console.debug("audio " + (audioTemporaryUnmuted ? "temporary un" : "re") + "muted");
        }
        setTimeout(lostAudioWorkaround, audioTemporaryUnmuted ? 1 : lostAudioWorkaroundInterval);
    }

    //-------------------------------------------------------
    //
    //  functions - vcard/avatar/bookmarks/occupants
    //
    //-------------------------------------------------------

    function addParticipant(id) {
        const participant = APP.conference.getParticipantById(id);

        console.debug("addParticipant", id, participant);

        if (participant && !participants[id]) {
            participants[id] = { ...participant };

            if (breakoutHost) {
                breakoutHost.addParticipant(id);
            }

            addParticipantDragDropHandlers(document.getElementById("participant_" + id));
        }
    }

    function addParticipantDragDropHandlers(element) {
        if (!element) return;
        element.setAttribute("draggable", "true");
        element.addEventListener("dragstart", participantDragStart);
        element.addEventListener("dragover", participantDragOver);
        element.addEventListener("drop", participantDrop);
    }

    function participantDragStart(event) {
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("application/x.id", event.target.id);
        console.debug('dragstart: source ', event.target.id);
    }

    function participantDragOver(event) {
        event.preventDefault();
        event.stopPropagation();
        event.dataTransfer.dropEffect = "move";
        // console.debug('dragover: destination ', this.id);
    }

    function participantDrop(event) {
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

    function removeParticipant(id) {
        console.debug("removeParticipant", id);

        if (breakoutHost) {
            breakoutHost.removeParticipant(id);
        }

        delete participants[id];
    }

    function setOwnPresence() {
        const connection = APP.connection.xmpp.connection;
        connection.send($pres());
    }

    function getOccupants() {
        const connection = APP.connection.xmpp.connection;
        const thisRoom = APP.conference._room.room.roomjid;

        const stanza = $iq({ 'to': thisRoom, 'type': 'get' }).c('query', { 'xmlns': "http://jabber.org/protocol/disco#items" });

        connection.sendIQ(stanza, function (iq) {
            iq.querySelectorAll('item').forEach(function (item) {
                console.debug("getOccupants", item);
                const id = Strophe.getResourceFromJid(item.getAttribute("jid"));
                addParticipant(id);
            });
        }, function (error) {
            console.error("get occupants error", error);
        });
    }

    function getBookmarks() {
        const connection = APP.connection.xmpp.connection;
        const thisRoom = APP.conference._room.room.roomjid;

        const stanza = $iq({ 'from': connection.jid, 'type': 'get' }).c('query', { 'xmlns': "jabber:iq:private" }).c('storage', { 'xmlns': 'storage:bookmarks' });

        connection.sendIQ(stanza, function (iq) {

            iq.querySelectorAll('conference').forEach(function (conference) {
                if (thisRoom == conference.getAttribute("jid")) {
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

        }, function (error) {
            console.error("bookmarks error", error);
        });
    }

    function getVCard() {
        const connection = APP.connection.xmpp.connection;
        const username = Strophe.getNodeFromJid(APP.connection.xmpp.connection.jid);

        const iq = $iq({ type: 'get', to: Strophe.getBareJidFromJid(APP.connection.xmpp.connection.jid) }).c('vCard', { xmlns: 'vcard-temp' });

        connection.sendIQ(iq, function (response) {
            const emailTag = response.querySelector('vCard EMAIL USERID');
            const email = emailTag ? emailTag.innerHTML : '';

            const fullnameTag = response.querySelector('vCard FN');
            const fullname = fullnameTag ? fullnameTag.innerHTML : username;

            const photoTag = response.querySelector('vCard PHOTO');
            const photoType = photoTag ? photoTag.querySelector('TYPE').innerHTML : null;
            const photoBinVal = photoTag ? photoTag.querySelector('BINVAL').innerHTML : null;

            let avatar = null;
            if (photoType && photoBinVal) {
                console.debug('getVCard set avatar from photo');
                avatar = 'data:' + photoType + ';base64,' + photoBinVal;
                changeAvatar(avatar, AvatarType.VCARD);
            }

            if (email) APP.conference.changeLocalEmail(email);
            if (fullname) APP.conference.changeLocalDisplayName(fullname);

            console.debug('getVCard', email, fullname, username, avatar);

        }, function (error) {
            console.error(error);
        });
    }

    function changeAvatar(avatarUrl, type) {
        console.debug('changeAvatar', avatarUrl, type);

        if (!avatarType || avatarType == AvatarType.INITIALS || type == AvatarType.UPLOAD) {
            avatarType = type;
            APP.conference.changeLocalAvatarUrl(avatarUrl);

            if (type == AvatarType.UPLOAD) {
                storage.setItem('ofmeet.settings.avatar', JSON.stringify(avatarUrl));
            } else if (type == AvatarType.VCARD) {
                vcardAvatar = avatarUrl;
            }
        } else {
            console.warn('Cannot overwrite avatars with the following types. type:' + avatarType + '=> type:' + type);
        }
    }

    function deleteUploadAvatar() {
        console.debug('deleteUploadAvatar');

        avatarType = null;
        storage.removeItem('ofmeet.settings.avatar');
        localDisplayName = null;

        if (vcardAvatar) {
            changeAvatar(vcardAvatar, AvatarType.VCARD);
        } else {
            changeAvatar(createAvatar(getLocalDisplayName()), AvatarType.INITIALS);
        }
    }

    function changeInitialsAvatarColor() {
        getNickColor(true);
        changeAvatar(createAvatar(getLocalDisplayName()), AvatarType.INITIALS);
    }

    function createAvatar(nickname, width, height, font) {
        console.debug('createAvatar', width, height, font);

        if (!width) width = 128;
        if (!height) height = 128;
        if (!font) font = 'Arial';

        let color = getNickColor();

        const $svg = $('<svg />');
        $svg.attr({
            'xmlns': 'http://www.w3.org/2000/svg',
            'version': '1.1',
            'width': width,
            'height': height
        });
        $svg.css('background-color', color);

        if (nickname) {
            let initials = getInitials(nickname);

            $text = $('<text />');
            $text.attr({
                'x': width / 2,
                'y': height / 2
            });
            $text.css({
                'fill': '#fff',
                'fill-opacity': 0.6,
                'font-family': font,
                'font-size': width * Math.pow(0.7, initials.length),
                'font-weight': 400,
                'text-anchor': 'middle',
                'dominant-baseline': 'central'
            });
            $text.text(initials);
            $svg.append($text);
        }

        let dataUrl = null;
        try {
            dataUrl = 'data:image/svg+xml;base64,' + btoa(unescape(encodeURIComponent($svg.prop('outerHTML'))));
        } catch (error) {
            console.error(error);

            // dummy image
            dataUrl = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg"/>';
        }

        return dataUrl;
    }

    function getInitials(nickname) {
        // try to split nickname into words at different symbols with preference
        nickname = nickname.toUpperCase().replace(/\s/g, ' '); // replace multibyte space with ASCII space
        let words = nickname.split(/[, ]/); // "John W. Doe" -> "John "W." "Doe"  or  "Doe,John W." -> "Doe" "John" "W."
        if (words.length == 1) words = nickname.split("."); // "John.Doe" -> "John" "Doe"  or  "John.W.Doe" -> "John" "W" "Doe"
        if (words.length == 1) words = nickname.split("-"); // "John-Doe" -> "John" "Doe"  or  "John-W-Doe" -> "John" "W" "Doe"

        let initials = '';
        if (words && words[0] && words.first != '') {
            const firstInitial = String.fromCodePoint(words[0].codePointAt(0)); // first letter of first word
            let lastInitial = null; // first letter of last word, if any

            const lastWordIdx = words.length - 1; // index of last word
            if (lastWordIdx > 0 && words[lastWordIdx] && words[lastWordIdx] != '') {
                lastInitial = String.fromCodePoint(words[lastWordIdx].codePointAt(0)); // first letter of last word
            }

            // if nickname consist of more than one words, compose the initials as two letter
            initials = firstInitial;
            if (lastInitial) {
                // if any comma is in the nickname, treat it to have the lastname in front, i.e. compose reversed
                initials = nickname.indexOf(",") == -1 ? firstInitial + lastInitial : lastInitial + firstInitial;
            }
        }

        return initials;
    }

    function getNickColor(reset = false) {
        if (!reset && storage.getItem('ofmeet.settings.nickColor')) {
            return storage.getItem('ofmeet.settings.nickColor');
        } else {
            let hsl = tinycolor(tinycolor.random()).toHsl();
            hsl.l = hsl.l * 0.5 + 0.2;

            let color = tinycolor(hsl).toHexString();

            storage.setItem('ofmeet.settings.nickColor', color);
            return color;
        }
    }

    //-------------------------------------------------------
    //
    //  functions - record, tags and pads
    //
    //-------------------------------------------------------

    function hideClock() {
        document.getElementById("clocktext").style.display = "none";
    }

    function showClock() {
        const textElem = document.getElementById("clocktext");
        textElem.style.display = "";

        function updateClock() {
            let clockStr = formatTimeSpan((Date.now() - clockTrack.joins) / 1000);
            if (breakoutClient && breakoutClient.state == BreakoutState.STARTED) {
                clockStr += ' / ' + formatTimeSpan(breakoutClient.duration);
            }
            textElem.textContent = clockStr;
            setTimeout(updateClock, 1000);
        }

        updateClock();
    }

    function addPad(text) {
        console.debug("addPad", text);

        const container = document.querySelector(".crypt-pads");
        const values = text.split('/');
        const html = '<span class="pade-col-content">' + IMAGES[values[3]] + '</span><span class="pade-col-content">' + values[8] + '<br/>' + values[3] + '</span>';
        const ele = document.createElement('li');
        ele.innerHTML = html;
        ele.classList.add("pade-col");
        ele.setAttribute("data-url", text);
        ele.setAttribute("data-type", values[3]);
        container.appendChild(ele);
    }

    function doPads() {
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

        if (!padsModal) {
            const largeVideo = document.querySelector("#largeVideo");
            const display = largeVideo.style.display;

            padsModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['custom-class-1', 'custom-class-2'],

                beforeOpen: function () {
                    console.debug("beforeOpen", padsModalOpened, padsList);

                    if (!padsModalOpened) {
                        padsList.forEach(function (text) {
                            addPad(text);
                        });

                        const container = document.querySelector(".crypt-pads");

                        container.addEventListener("click", function (evt) {
                            evt.stopPropagation();
                            const type = evt.target.parentNode.getAttribute("data-type");
                            let url = evt.target.parentNode.getAttribute("data-url");

                            if (type) {
                                console.debug("beforeOpen - click", type);
                                if (!url) url = interfaceConfig.OFMEET_CRYPTPAD_URL + "/" + type + "/";
                                openPad(url);
                            }
                        });

                        padsModalOpened = true;
                    }
                }
            });
            padsModal.addFooterBtn('Share Clipboard', 'btn btn-primary', function () {
                navigator.clipboard.readText().then(function (clipText) {
                    console.debug("doPads", clipText);

                    padsModal.close();

                    //APP.UI.toggleChat();
                    APP.conference._room.sendTextMessage(clipText)
                    APP.UI.messageHandler.showWarning({title:"Share Clipboard", description:"Clipboard shared with other participants"});
                });
            });

            padsModal.addFooterBtn('Close', 'btn btn-secondary', function () {
                padsModal.close();
            });

            padsModal.addFooterBtn('Quit', 'btn btn-secondary', function () {
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

    function openPad(url) {
        console.debug("openPad", url);

        const padContent = document.querySelector("#ofmeet-content");

        if (padContent) {
            padsModal.close();
            APP.UI.messageHandler.showWarning({title:"CryptPad", description:"Quit active pad before opening a new one"});
        } else {
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

            cryptpad.addEventListener("load", function (evt) {
                console.debug("loading pad - ", this);
                padsModal.close();
            });
        }
    }

    function notifyText(message, title, notifyId, callback) {
        console.debug("notifyText", title, message, notifyId);

        if (!notifyId) notifyId = Math.random().toString(36).substr(2, 9);

        const prompt = new Notification(title, {
            body: message,
            requireInteraction: true
        });

        prompt.onclick = function (event) {
            event.preventDefault();
            if (callback) callback(notifyId, 0);
        }

        prompt.onclose = function (event) {
            event.preventDefault();
            if (callback) callback(notifyId, 1);
        }
    }

    function doTags() {
        const template = `
<div class="modal-header">
    <h4 class="modal-title">${i18n('tag.conferenceCaptionsSubTitles')}</h4>
</div>
<div class="modal-body">
    <form>
        <div class="form-group">
            <label for="tags-location" class="col-form-label"> ${i18n('tag.location')}:</label>
            <input id="tags-location" type="text" class="form-control" name="tag-location" value="${tags.location}"/>
        </div>
        <div class="form-group">
            <label for="tags-date" class="col-form-label"> ${i18n('tag.date')}:</label>
            <input id="tags-date" type="text" class="form-control" name="tags-date"/>
        </div>
        <div class="form-group">
            <label for="tags-subject" class="col-form-label"> ${i18n('tag.subject')}:</label>
            <input id="tags-subject" type="text" class="form-control" name="tags-subject" value="${tags.subject}"/>
        </div>
        <div class="form-group">
            <label for="tags-host" class="col-form-label"> ${i18n('tag.host')}:</label>
            <input id="tags-host" type="text" class="form-control" name="tags-host" value="${tags.host}"/>
        </div>
        <div class="form-group">
            <label for="tags-activity" class="col-form-label"> ${i18n('tag.activity')}:</label>
            <input id="tags-activity" type="text" class="form-control" name="tags-activity" value="${tags.activity}"/>
        </div>
        <div class="form-group pade-control-row" id="tags-message-captions-group">
            <label class="form-check-label col-form-label" for="tags-message-captions">${i18n('tag.enableMessageCaptions')}</label>
            <input type="checkbox" class="form-check-app-input" id="tags-message-captions"><label class="form-check-app-label" for="tags-message-captions"></label>
        </div>
        <div class="form-group pade-control-row" id="tags-voice-transcription-group">
            <label class="form-check-label col-form-label" for="tags-voice-transcription">${i18n('tag.enableVoiceTranscription')}</label>
            <input type="checkbox" class="form-check-app-input" id="tags-voice-transcription"><label class="form-check-app-label" for="tags-voice-transcription"></label>
        </div>
    </form>
</div>`;

        if (!tagsModal) {
            tagsModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['tags-modal'],
                onOpen: function () {
                    console.debug('tags modal open');
                },
                onClose: function () {
                    console.debug('tags modal closed');
                },
                beforeOpen: function () {
                    $('#tags-location').val(tags.location);
                    if (tags.date) {
                        $('#tags-date').val(tags.date);
                    } else {
                        $('#tags-date').val(localizedDate(new Date()).format('LL'));
                    }
                    if (tags.subject) {
                        $('#tags-subject').val(tags.subject);
                    } else {
                        $('#tags-subject').val(getConferenceName());
                    }
                    if (tags.host) {
                        $('#tags-host').val(tags.host);
                    } else {
                        const modrator = Array.from(getAllParticipants().keys()).find(p => p.role == 'moderator');
                        if (modrator) {
                            $('#tags-host').val(modrator.name);
                        }
                    }
                    $('#tags-activity').val(tags.activity);
                }
            });

            tagsModal.setContent(template);

            tagsModal.addFooterBtn(i18n('tag.save'), 'btn btn-primary', function () {
                // here goes some logic
                tags.location = $('#tags-location').val();
                tags.date = $('#tags-date').val();
                tags.subject = $('#tags-subject').val();
                tags.host = $('#tags-host').val();
                tags.activity = $('#tags-activity').val();

                if (tags.location) {
                    const $subtitile = $("#subtitles");
                    $subtitile.empty();
                    $subtitile.append(`<div><b>${i18n('tag.location')}</b>: ${tags.location}</div>`);
                    if (tags.date) {
                        $subtitile.append(`<div><b>${i18n('tag.date')}</b>: ${tags.date}</div>`);
                    }
                    if (tags.subject) {
                        $subtitile.append(`<div><b>${i18n('tag.subject')}</b>: ${tags.subject}</div>`);
                    }
                    if (tags.host) {
                        $subtitile.append(`<div><b>${i18n('tag.host')}</b>: ${tags.host}</div>`);
                    }
                    if (tags.activity) {
                        $subtitile.append(`<div><b>${i18n('tag.activity')}</b>: ${tags.activity}</div>`);
                    }
                }

                if ($('#tags-subject').val()) {
                    setConferenceName($('#tags-subject').val());
                }

                tagsModal.close();
            });

            tagsModal.addFooterBtn(i18n('tag.cancel'), 'btn btn-secondary', function () {
                tagsModal.close();
            });

            if (interfaceConfig.OFMEET_ENABLE_CAPTIONS) {
                $('#tags-message-captions').on('change.tags', evt => {
                    captions.msgsDisabled = !evt.target.checked;
                    if (captions.ele) captions.ele.innerHTML = "";
                }).prop('checked', !captions.msgsDisabled);
            } else {
                $('#tags-message-captions-group').hide();
            }

            if (ofm.recognition) {
                $('#tags-voice-transcription').on('change.tags', evt => {
                    captions.transcriptDisabled = !evt.target.checked;
                    ofm.recognitionActive = !captions.transcriptDisabled;

                    if (captions.transcriptDisabled) {
                        ofm.recognition.stop();
                    } else {
                        ofm.recognition.start();
                    }
                    if (captions.ele) captions.ele.innerHTML = "";
                }).prop('checked', !captions.transcriptDisabled);
            } else {
                $('#tags-voice-transcription-group').hide();
            }
        }

        tagsModal.open();
    }

    function getFilename(prefix, suffix) {
        return prefix + "-" +
            (tags.location != "" ? tags.location + "-" : "") +
            tags.date.replace(/\//g, '') + "-" +
            (tags.subject != "" ? tags.subject + "-" : "") +
            (tags.host != "" ? tags.host + "-" : "") +
            (tags.activity != "" ? tags.activity : "") +
            Math.random().toString(36).substr(2, 9) +
            suffix;
    }

    function addTagsToImage(bitmap, callback) {
        const font = "20px Arial";
        const canvas = document.createElement('canvas');
        canvas.width = bitmap.width;
        canvas.height = bitmap.height;

        canvas.style.display = 'none';
        document.body.appendChild(canvas);

        const context = canvas.getContext('2d');
        context.drawImage(bitmap, 0, 0);

        if (tags.location != "") {
            context.font = font;
            context.fillStyle = "#fff";
            context.fillText(i18n('takePhoto.handsRaised') + ": " + handsRaised, 50, 25);
            context.fillText(i18n('takePhoto.location') + ": " + tags.location, 50, 50);
            context.fillText(i18n('takePhoto.date') + ": " + tags.date, 50, 75);
            context.fillText(i18n('takePhoto.subject') + ": " + tags.subject, 50, 100);
            context.fillText(i18n('takePhoto.host') + ": " + tags.host, 50, 125);
            context.fillText(i18n('takePhoto.activity') + ": " + tags.activity, 50, 150);
        }

        canvas.toBlob(function (blob) {
            callback(blob);
            setTimeout(function () { document.body.removeChild(canvas) });
        });
    }

    function createPhotoViewerHTML() {
        console.debug("custom_ofmeet.js createPhotoViewerHTML");

        let imagenames = {};
        const me = APP.conference.getMyUserId();
        const html = ['<html><head><style> img { float: left; } img:first-child:nth-last-child(1) { width: 100%;} img:first-child:nth-last-child(2), img:first-child:nth-last-child(2) ~ img { width: 50%;} img:first-child:nth-last-child(3), img:first-child:nth-last-child(3) ~ img, img:first-child:nth-last-child(4), img:first-child:nth-last-child(4) ~ img { width: 50%;} img:first-child:nth-last-child(5), img:first-child:nth-last-child(5) ~ img, img:first-child:nth-last-child(6), img:first-child:nth-last-child(6) ~ img, img:first-child:nth-last-child(7), img:first-child:nth-last-child(7) ~ img, img:first-child:nth-last-child(8), img:first-child:nth-last-child(8) ~ img, img:first-child:nth-last-child(9), img:first-child:nth-last-child(9) ~ img { width: 33.33%; } </style></head><body>'];
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function (id) {
            imagenames[id] = getFilename("ofmeet-" + id, ".png");
            html.push('\n<img id="' + id + '" src="' + imagenames[id] + '"/>');
        });

        html.push('</body></html>');

        const blob = new Blob(html, { type: "text/plain;charset=utf-8" });
        const htmlFile = getFilename("ofmeet-photo-" + me, ".html");
        createAnchor(htmlFile, blob);
        return imagenames;
    }

    function takePhoto() {
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);
        const imagenames = createPhotoViewerHTML();

        console.debug("custom_ofmeet.js takePhoto", ids, imagenames);

        ids.forEach(function (id) {
            const track = recordingVideoTrack[id].clone().getVideoTracks()[0];
            const imageCapture = new ImageCapture(track);

            imageCapture.grabFrame().then(function (bitmap) {
                addTagsToImage(bitmap, function (blob) {
                    console.debug("custom_ofmeet.js takePhoto with tags", blob);
                    createAnchor(imagenames[id], blob);
                });
            })
        });
    }

    function stopRecorder() {
        console.debug("custom_ofmeet.js stopRecorder");

        $('#ofmeet-record svg').css('fill', '#fff');
        $('#ofmeet-desktop svg').css('fill', '#fff');

        clockTrack.stop = (new Date()).getTime();
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function (id) {
            if (videoRecorder[id]) videoRecorder[id].stop();
        });

        if (!config.ofmeetLiveStream) {
            createVideoViewerHTML();
            APP.UI.messageHandler.showWarning({title:"Recording", description:"Conference recording stopped"});
        } else {
            APP.UI.messageHandler.showWarning({title:"Streaming", description:"Conference streaming stopped"});
        }
        ofm.recording = false;
    }

    function createAnchor(filename, blob) {
        const anchor = document.createElement('a');
        anchor.href = window.URL.createObjectURL(blob);
        anchor.style = "display: none;";
        anchor.download = filename;
        document.body.appendChild(anchor);
        anchor.click();
        window.URL.revokeObjectURL(anchor.href);
    }

    function createVttDataUrl() {
        console.debug("custom_ofmeet.js createVttDataUrl");

        function getTimeStamp(secs) {
            return formatTimeSpan(secs);
        }

        const vtt = ["WEBVTT", "00:00:00.000 --> 24:00:00.000 position:10% line:1% align:left size:100%"];

        if (tags.location != "") {
            vtt.push("<b>Location</b>: " + tags.location);
            vtt.push("<b>Date</b>: " + tags.date);
            vtt.push("<b>Subject</b>: " + tags.subject);
            vtt.push("<b>Host</b>: " + tags.host);
            vtt.push("<b>Activity</b>: " + tags.activity);
        }

        let recordSeconds = 0;
        let totalSeconds = parseInt((clockTrack.start - clockTrack.joins) / 1000);

        for (let i = clockTrack.start; i < clockTrack.stop; i += 1000) {
            ++totalSeconds;
            ++recordSeconds;

            const timestamp = getTimeStamp(recordSeconds);

            vtt.push(timestamp + ".000 --> " + timestamp + ".999 position:10% line:-10% align:left size:100%");
            vtt.push(getTimeStamp(totalSeconds));
        }

        captions.msgs.forEach(function (msg) {
            const seconds = parseInt((msg.stamp - clockTrack.start) / 1000);

            if (seconds > 0) {
                const start = getTimeStamp(seconds);
                const end = getTimeStamp(seconds + 3);
                vtt.push(start + ".000 --> " + end + ".999 position:30% line:-10% align:left size:100%");
                vtt.push(msg.text);
            }
        });

        console.debug("custom_ofmeet.js createVttDataUrl", vtt);
        const url = "data:application/json;base64," + btoa(vtt.join('\n'))
        return url
    }

    function createVideoViewerHTML() {
        console.debug("custom_ofmeet.js createVideoViewerHTML");

        const vttUrl = createVttDataUrl();
        const html = ['<html><head><style> video { float: left; } video::cue {font-size: 20px; color: #FFF; opacity: 1;} video:first-child:nth-last-child(1) { width: 100%; height: 100%; } video:first-child:nth-last-child(2), video:first-child:nth-last-child(2) ~ video { width: 50%; height: 100%; } video:first-child:nth-last-child(3), video:first-child:nth-last-child(3) ~ video, video:first-child:nth-last-child(4), video:first-child:nth-last-child(4) ~ video { width: 50%; height: 50%; } video:first-child:nth-last-child(5), video:first-child:nth-last-child(5) ~ video, video:first-child:nth-last-child(6), video:first-child:nth-last-child(6) ~ video, video:first-child:nth-last-child(7), video:first-child:nth-last-child(7) ~ video, video:first-child:nth-last-child(8), video:first-child:nth-last-child(8) ~ video, video:first-child:nth-last-child(9), video:first-child:nth-last-child(9) ~ video { width: 33.33%; height: 33.33%; } </style></head><body>'];
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function (id) {
			if (filenames[id]) {
				html.push('\n<video id="' + id + '" controls preload="metadata" src="' + filenames[id] + '"><track default src="' + vttUrl + '"></video>');
			}
        });

        html.push('\n<script>');
        html.push('\n window.addEventListener("load", function() {');

        for (let z = 0; z < ids.length; z++) {
            html.push('\n   var v' + z + ' = document.getElementById("' + ids[z] + '");');

            html.push('\n    v' + z + '.addEventListener("play", function() {');
            for (let i = 0; i < ids.length; i++) {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").play();');
            }
            html.push('\n    });');

            html.push('\n    v' + z + '.addEventListener("playing", function() {');
            for (let i = 0; i < ids.length; i++) {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").play();');
            }
            html.push('\n    });');

            html.push('\n    v' + z + '.addEventListener("pause", function() {');
            for (let i = 0; i < ids.length; i++) {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").pause();');
            }
            html.push('\n    });');

            html.push('\n    v' + z + '.addEventListener("ended", function() {');
            for (let i = 0; i < ids.length; i++) {
                if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").pause();');
            }
            html.push('\n    });');

            if (z == 0) {
                html.push('\n    v' + z + '.addEventListener("seeking", function() {');
                for (let i = 0; i < ids.length; i++) {
                    if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").currentTime = v' + z + '.currentTime;');
                }
                html.push('\n    });');

                html.push('\n    v' + z + '.addEventListener("seeked", function() {');
                for (let i = 0; i < ids.length; i++) {
                    if (i != z) html.push('\n       document.getElementById("' + ids[i] + '").currentTime = v' + z + '.currentTime;');
                }
                html.push('\n    });');
            }
        }
        html.push('\n });');
        html.push('\n</script></body></html>');

        const me = APP.conference.getMyUserId();
        const blob = new Blob(html, { type: "text/plain;charset=utf-8" });
        const htmlFile = getFilename("ofmeet-video-" + me, ".html");
        createAnchor(htmlFile, blob);
    }

    function startRecorder(recorder) {
        navigator.mediaDevices.getUserMedia({ audio: true, video: true }).then(function (stream) {
            recordingVideoTrack[APP.conference.getMyUserId()] = stream;
            recordingAudioTrack[APP.conference.getMyUserId()] = stream;
            recorder();
        });
    }

    function startMeetingRecorder() {
        console.debug("custom_ofmeet.js startMeetingRecorder");

        $('#ofmeet-record svg').css('fill', '#f00');
        APP.UI.messageHandler.showWarning({title:"Recording", description:"Conference Recording Started"});

        captions.msgs = [];
        clockTrack.start = (new Date()).getTime();
        const ids = Object.getOwnPropertyNames(recordingVideoTrack);

        ids.forEach(function (id) {
            filenames[id] = getFilename("ofmeet-video-" + id, ".webm");
            recorderStreams[id] = new MediaStream();

            recorderStreams[id].addEventListener('addtrack', (event) => {
                console.debug(`ofmeet.js new ${event.track.kind} track added`);
            });

            recorderStreams[id].addTrack(recordingVideoTrack[id].clone().getVideoTracks()[0]);
            recorderStreams[id].addTrack(recordingAudioTrack[id].clone().getAudioTracks()[0]);

            console.debug("custom_ofmeet.js startMeetingRecorder stream", id, recorderStreams[id], recorderStreams[id].getVideoTracks()[0].getSettings());

            const dbname = 'ofmeet-db-' + id;
            dbnames.push(dbname);

            customStore[id] = new idbKeyval.Store(dbname, dbname);
            videoRecorder[id] = new MediaRecorder(recorderStreams[id], { mimeType: 'video/webm' });

            videoRecorder[id].ondataavailable = function (e) {
                if (e.data.size > 0) {
                    const key = "video-chunk-" + (new Date()).getTime();

                    idbKeyval.set(key, e.data, customStore[id]).then(function () {
                        console.debug("custom_ofmeet.js startMeetingRecorder - ondataavailable", id, key, e.data);

                    }).catch(function (err) {
                        console.error('ofmeet.js startMeetingRecorder - ondataavailable failed!', err)
                    });
                }
            }

            videoRecorder[id].onstop = function (e) {
                recorderStreams[id].getTracks().forEach(track => track.stop());

                idbKeyval.keys(customStore[id]).then(function (data) {
                    const duration = Date.now() - startTime;
                    const blob = new Blob(data, { type: 'video/webm' });

                    console.debug("custom_ofmeet.js startMeetingRecorder - onstop", id, filenames[id], duration, data, blob);

                    ysFixWebmDuration(blob, duration, function (fixedBlob) {
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

        ofm.recording = true;
    }

    function mergeAudioStreams(desktopStream, voiceStream) {
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

    function connectLiveStream(url, streamKey) {
        const metadata = { user: getLocalDisplayName(), room: APP.conference.roomName, key: streamKey };
        const ws = new WebSocket(url, [streamKey]);

        ws.onopen = (event) => {
            console.debug(`Connection opened: ${JSON.stringify(event)}`);
            ws.send(JSON.stringify(metadata));
        };

        ws.onclose = (event) => {
            console.debug(`Connection closed: ${JSON.stringify(event)}`);
        };

        ws.onerror = (event) => {
            console.debug(`An error occurred with websockets: ${JSON.stringify(event)}`);
        };
        return ws;
    }

    function startDesktopRecorder() {
        console.debug("custom_ofmeet.js startDesktopRecorder");

        const recConstraints = { video: true, preferCurrentTab: true, audio: { autoGainControl: false, echoCancellation: false, googAutoGainControl: false, noiseSuppression: false } };
        const streamConstraints = { video: true, audio: true, preferCurrentTab: true };

        if (config.ofmeetLiveStream) {
            if (!config.ofmeetStreamKey || config.ofmeetStreamKey.trim() === '') {
                config.ofmeetStreamKey = storage.getItem("ofmeet.live.stream.key");

                if (!config.ofmeetStreamKey || config.ofmeetStreamKey.trim() === '') {
                    config.ofmeetStreamKey = prompt(i18n('enterStreamKey'));

                    if (!config.ofmeetStreamKey || config.ofmeetStreamKey.trim() === '')
                        config.ofmeetLiveStream = false;
                    else
                        storage.setItem("ofmeet.live.stream.key", config.ofmeetStreamKey);
                }
            }
        }

        navigator.mediaDevices.getDisplayMedia(config.ofmeetLiveStream ? streamConstraints : recConstraints).then(stream => {
            $('#ofmeet-desktop svg').css('fill', '#f00');

            captions.msgs = [];
            clockTrack.start = (new Date()).getTime();

            const id = APP.conference.getMyUserId();
            const tracks = [
                ...stream.getVideoTracks(),
                ...mergeAudioStreams(stream, recordingAudioTrack[id].clone())
            ];

            recorderStreams[id] = new MediaStream(tracks);

            if (config.ofmeetLiveStream && APP.conference._room.isModerator()) {
                const ws_url = config.bosh.split("/");
                console.debug('ofmeet.js startDesktopRecorder - live streaming', tracks, ws_url);

                let websocket = connectLiveStream("wss://" + ws_url[2] + "/livestream-ws/", config.ofmeetStreamKey);
                videoRecorder[id] = new MediaRecorder(recorderStreams[id], { mimeType: 'video/webm;codecs=h264', bitsPerSecond: 256 * 8 * 1024 });

                videoRecorder[id].ondataavailable = function (e) {
                    websocket.send(e.data);
                }

                videoRecorder[id].onstop = function (e) {
                    websocket.close();
                    websocket = null;
                }

                APP.UI.messageHandler.showWarning({title:"Streaming", description:"Conference streaming started"});

            } else {
                filenames[id] = getFilename("ofmeet-video-" + id, ".webm");

                console.debug("custom_ofmeet.js startDesktopRecorder stream", id, recorderStreams[id], recorderStreams[id].getVideoTracks()[0].getSettings());
                const dbname = 'ofmeet-db-' + id;
                dbnames.push(dbname);

                customStore[id] = new idbKeyval.Store(dbname, dbname);
                videoRecorder[id] = new MediaRecorder(recorderStreams[id], { mimeType: 'video/webm' });

                videoRecorder[id].ondataavailable = function (e) {
                    if (e.data.size > 0) {
                        const key = "video-chunk-" + (new Date()).getTime();

                        idbKeyval.set(key, e.data, customStore[id]).then(function () {
                            console.debug("custom_ofmeet.js startDesktopRecorder - ondataavailable", id, key, e.data);

                        }).catch(function (err) {
                            console.error('ofmeet.js startDesktopRecorder - ondataavailable failed!', err)
                        });
                    }
                }

                videoRecorder[id].onstop = function (e) {
                    recorderStreams[id].getTracks().forEach(track => track.stop());
                    stream.getTracks().forEach(track => track.stop());

                    idbKeyval.keys(customStore[id]).then(function (data) {
                        const duration = Date.now() - startTime;
                        const blob = new Blob(data, { type: 'video/webm' });

                        console.debug("custom_ofmeet.js startDesktopRecorder - onstop", id, filenames[id], duration, data, blob);

                        ysFixWebmDuration(blob, duration, function (fixedBlob) {
                            createAnchor(filenames[id], fixedBlob);
                            idbKeyval.clear(customStore[id]);

                            delete filenames[id];
                            delete videoRecorder[id];
                            delete recorderStreams[id];
                            delete customStore[id];
                        });
                    });
                }

                APP.UI.messageHandler.showWarning({title:"Recording", description:"Conference recording started"});
            }
            videoRecorder[id].start(1000);
            const startTime = Date.now();
            ofm.recording = true;

        }, error => {
            console.error("custom_ofmeet.js startDesktopRecorder", error);
            APP.UI.messageHandler.showError({ title: "Desktop recorder/streamer", error, hideErrorSupportLink: true });
            ofm.recording = false;
        });
    }

    function recoverRecording(dbname) {
        console.debug("recovering db " + dbname);

        dbnames.push(dbname);
        const store = new idbKeyval.Store(dbname, dbname);
        const filename = getFilename("ofmeet-video-" + dbname, ".webm");

        idbKeyval.keys(store).then(function (data) {
            console.debug("custom_ofmeet.js recoverRecording", filename, data);

            const blob = new Blob(data, { type: 'video/webm' });
            createAnchor(filename, blob);

            idbKeyval.clear(store);
        });
    }

    //-------------------------------------------------------
    //
    //  Breakout Rooms
    //
    //-------------------------------------------------------

    class BreakoutClient {
        constructor() {
            this.countdownTimer = null;
            this.endBreakoutTimer = null;
            this.state = BreakoutState.STOPPED;

            if (hashParams.mainRoomUserId) {
                storage.setItem('mainRoomUserId', hashParams.mainRoomUserId);
            }

            if (hashParams.breakoutRoom) {
                storage.setItem('breakoutRoom', hashParams.breakoutRoom);
            }

            if (hashParams.startTime) {
                storage.setItem('startTime', hashParams.startTime);
            }

            if (hashParams.endTime) {
                storage.setItem('endTime', hashParams.endTime);
            }

            if (this.mainRoomUserId) {
                const room = getConference();
                room.setLocalParticipantProperty('mainRoomUserId', this.mainRoomUserId);
            }

            console.debug(decodeURI(APP.conference.roomName));
            if (decodeURI(APP.conference.roomName) != this.breakoutRoom) {
                storage.removeItem('breakoutRoom');
                storage.removeItem('startTime');
                storage.removeItem('endTime');
                return;
            }

            const remainingTime = this.remainingTime;
            const elapsedTime = this.elapsedTime;
            if (remainingTime > 0) {
                this.endBreakoutTimer = setTimeout(() => { this.stopBreakout(this.getMainRoomUrl(), BreakoutHost.wait) }, remainingTime * 1000);
                this.state = BreakoutState.STARTED;
            } else if(elapsedTime > 0) {
                this.state = BreakoutState.STARTED;
            }
        }

        get mainRoomUserId() {
            return storage.getItem('mainRoomUserId');
        }

        get breakoutRoom() {
            return storage.getItem('breakoutRoom');
        }

        get startTime() {
            return storage.getItem('startTime');
        }

        get endTime() {
            return storage.getItem('endTime');
        }

        get remainingTime() {
            const now = Math.floor((new Date()).getTime() / 1000);
            if (this.startTime && this.endTime && this.startTime <= now && now < this.endTime) {
                return this.endTime - now;
            }
            return -1;
        }

        get elapsedTime() {
            const now = Math.floor((new Date()).getTime() / 1000);
            if (this.startTime && this.startTime <= now) {
                return now - this.startTime;;
            }
            return -1;
        }

        get duration() {
            return storage.getItem('endTime') - storage.getItem('startTime');
        }

        startBreakout(url, wait, room, subject, startTime, endTime) {
            if (this.countdownTimer) {
                clearTimeout(this.countdownTimer);
                this.countdownTimer = null;
            }

            this.state = BreakoutState.STARTING;
            
            storage.setItem('breakoutRoom', room);
            storage.setItem('mainRoomUserId', APP.conference.getMyUserId());
            storage.setItem('startTime', startTime);
            storage.setItem('endTime', endTime);

            const params = {
                breakoutRoom: room,
                subject: subject,
                startTime: startTime,
                endTime: endTime,
                mainRoomUserId: APP.conference.getMyUserId()
            };

            const roomUrl = new URL(url);
            roomUrl.hash = this.toParamString(params, roomUrl.hash);

            APP.UI.messageHandler.showWarning({title:i18n('breakout.breakoutRooms'), description:i18n('breakout.joining', { sec: wait })});
            this.countdownTimer = setTimeout(() => { location.replace(roomUrl.href) }, wait * 1000);
        }

        stopBreakout(url, wait) {
            if (this.state == BreakoutState.STOPPING && this.countdownTimer) {
                return;
            }

            if (this.countdownTimer) {
                clearTimeout(this.countdownTimer);
                this.countdownTimer = null;
            }

            if (url == location.href) {
                this.state = BreakoutState.STOPPED;
                APP.UI.messageHandler.showWarning({title:i18n('breakout.breakoutRooms'), description:i18n('breakout.cancelled')});
            } else {
                this.state = BreakoutState.STOPPING;

                const roomUrl = new URL(url);
                roomUrl.hash = this.toParamString({ mainRoomUserId: this.mainRoomUserId }, roomUrl.hash);

                APP.UI.messageHandler.showWarning({title:i18n('breakout.breakoutRooms'), description:i18n('breakout.leaving', { sec: wait })});
                this.countdownTimer = setTimeout(() => { location.replace(roomUrl.href) }, wait * 1000);
            }
        }

        toParamString(params, baseStr = '') {
            return ((baseStr && baseStr.length > 1) ? baseStr + '&' : '') +
                Object.keys(params).map(key => `${key}=${JSON.stringify(params[key])}`).join('&');
        }

        getMainRoomUrl() {
            return location.href.replace(/#.+?$/, '').replace(/-room\d+-[\da-z]{9}$/, '');
        }
    }

    class BreakoutHost {
        static defaultDuration = 60;
        static wait = 10;

        constructor() {
            this.startedRooms = [];
            this.$status = $('#breakout-status');
            this.$clock =$('#breakout-clock');
            this.endBreakoutTimer = null;
            this.countdownTimer = null;
            this.clockTimer = null;
            this.domain = Strophe.getDomainFromJid(getConferenceJid());
            this.startTime = null;
            this.endTime = null;

            const kanbanConfig = {
                element: ".breakout-kanban",
                gutter: "5px",
                widthBoard: "250px",
                dragBoards: false,
                buttonClick: (el, boardId) => {
                    console.debug("Board clicked", boardId);
                    if (boardId != "participants") this.openRoom(boardId)
                },
                itemAddOptions: {
                    enabled: true,
                    content: i18n('breakout.join'),
                    class: 'kanban-title-button btn btn-default btn-sm btn-primary',
                },
                boards: [
                    {
                        id: 'participants',
                        title: i18n('breakout.meetingParticipants'),
                        class: 'participants',
                        item: []
                    }
                ]
            }

            for (let id in participants) {
                kanbanConfig.boards[0].item.push(this.elementFromParticipant(participants[id]));
            }

            console.debug("createBreakout", kanbanConfig);
            this.kanban = new jKanban(kanbanConfig);
        }

        get roomCount() {
            return $('.breakout-kanban .kanban-board .room').length;
        }

        get started() {
            return $('.btn-breakout').hasClass('btn-breakout-stop');
        }

        refresh() {
            const count = Object.keys(participants).length;

            if (parseInt($('#breakout-rooms').val(), 10) < 1) {
                $('#breakout-rooms').val(Math.round(count / 2));
                this.resizeRoom(Math.round(count / 2));
            }

            if (parseInt($('#breakout-duration').val(), 10) < 1) {
                $('breakout-duration').val(BreakoutHost.defaultDuration);
            }

            if (!this.started) {
                this.startedRooms = [];
                $('.breakout-kanban .kanban-item').removeClass('disabled-item');
            }

            $('.breakout-kanban .kanban-item').each((index, element) => {
                const $item = $(element);
                const id = $item.attr('data-eid');
                this.updateParticipant(id);
            })

            for (const id in participants) {
                if (!this.kanban.findElement(id)) {
                    this.addParticipant(id);
                }
            }

            $('#breakout-title').text(count);

            this.setStatusMessage('');
        }

        elementFromParticipant(participant) {
            const id = participant._id;
            const displayName = participant._displayName || i18n('breakout.anonymous', { id: id });
            const avatarURL = participant.avatarURL ?? '/images/avatar.png';
            return {
                id: id,
                title: `<div class="header-icon"><img class="avatar" src="${avatarURL}" style=""></div>${displayName}`
            };
        }

        addParticipant(id) {
            const participant = participants[id];
            if (participant) {
                this.kanban.addElement('participants', this.elementFromParticipant(participants[id]));
            }
            $('#breakout-title').text(participants.length);
        }

        updateParticipant(id) {
            if (id in participants) {
                this.kanban.replaceElement(id, this.elementFromParticipant(participants[id]));
            } else if (!this.started) {
                this.kanban.removeElement(id);
            }
        }

        removeParticipant(id) {
            if (this.started) {
                const elem = this.kanban.findElement(id);
                if (elem) {
                    $(elem).addClass('disabled-item');
                }
            } else {
                this.kanban.removeElement(id);
            }
            $('#breakout-title').text(participants.length);
        }

        recallParticipant(id, oldId) {
            const elem = this.kanban.findElement(oldId);
            if (elem) {
                this.kanban.removeElement(id);
                $(elem).removeClass('disabled-item').attr('data-eid', id);
                this.kanban.replaceElement(id, this.elementFromParticipant(participants[id]));
                $('#breakout-title').text(participants.length);
            }
        }

        getRoomParticipants(index) {
            let roomParticipants = []
            if (this.kanban.findBoard('room_' + index)) {
                for (let elem of this.kanban.getBoardElements('room_' + index)) {
                    let p = participants[elem.getAttribute('data-eid')];
                    if (p) {
                        roomParticipants.push(p);
                    }
                }
            }
            return roomParticipants;
        }

        moveRoomParticipant(roomIndex, participant) {
            if (participant) {
                const pid = participant._id;
                if (roomIndex != this.kanban.getParentBoardID(pid)) {
                    this.kanban.removeElement(pid);
                    this.kanban.addElement(roomIndex, this.elementFromParticipant(participant));
                }
            }
        }

        resizeRoom(size) {
            if (size > 0 && size < 50) {
                const currentSize = this.roomCount;
                if (currentSize < size) {
                    const boards = [];
                    for (let i = currentSize; i < size; i++) {
                        boards.push({
                            id: 'room_' + (i + 1),
                            title: i18n('breakout.room', { n: (i + 1).toString() }),
                            class: 'room',
                            item: []
                        });
                    }
                    this.kanban.addBoards(boards);

                    for (let i = currentSize; i < size; i++) {
                        const $board = $(this.kanban.findBoard('room_' + (i + 1)));
                        $board.attr('data-room-name', APP.conference.roomName + '-room' + (i + 1) + '-' + Math.random().toString(36).substr(2, 9));
                        // TODO: Replace with a nice-looking input from.
                        $board.find('.kanban-title-board').on('keydown keyup', e => {
                            e.stopPropagation();
                            if (e.which == 13) {
                                return false;
                            }
                        }).attr('contenteditable', 'true');
                    }
                } else {
                    for (let i = currentSize - 1; i >= size; i--) {
                        const roomParticipants = this.getRoomParticipants(i + 1);
                        for (let p of roomParticipants) {
                            console.debug(p);
                            this.moveRoomParticipant('participants', p);
                        }
                        this.kanban.removeBoard('room_' + (i + 1));
                    }
                }
            }
        }

        allocateToRooms() {
            const ids = Object.getOwnPropertyNames(participants);
            const roomCount = this.roomCount;

            console.debug("allocateToRooms", roomCount, breakoutHost);

            if (ids.length > 0 && roomCount > 0) {
                for (let i = 0; i < roomCount; i++) {
                    for (let j = 0; j < ids.length; j++) {
                        // allocate participant j to room i
                        if (j % roomCount == i) {
                            this.moveRoomParticipant('room_' + (i + 1), participants[ids[j]]);
                        }
                    }
                }
                this.setStatusMessage(i18n('breakout.allocatedMessage', { participants: ids.length, rooms: roomCount }));
            } else {
                this.setStatusMessage(i18n('breakout.missingParticipants'));
            }
        }

        startBreakout() {
            if (this.started) {
                console.error("startBreakout error. breakout has already started.");
                return;
            }

            if (this.roomCount < 1 || $('.breakout-modal header.room+main .kanban-item').length < 1) {
                this.setStatusMessage(i18n('breakout.allocateParticipantsFirst'));
                return;
            }

            const duration = $('#breakout-duration').val();

            this.startTime = Math.floor((new Date()).getTime() / 1000 + BreakoutHost.wait);
            this.endTime = duration > 0 ? this.startTime + duration * 60 : null;
            this.startedRooms = [];

            for (let i = 0; i < this.roomCount; i++) {
                const $board = $(this.kanban.findBoard('room_' + (i + 1)));
                const roomName = $board.attr("data-room-name");
                const roomTitle = $board.find('.kanban-title-board').text();
                const roomParticipants = this.getRoomParticipants(i + 1);

                this.startedRooms.push(roomName);

                for (let p of roomParticipants) {
                    const jid = p._jid;
                    const url = new URL(roomName, location.href);

                    const payload = {
                        action: 'start-breakout',
                        room: roomName,
                        url: url.href,
                        wait: BreakoutHost.wait,
                        subject: getConferenceName() + ' - ' + roomTitle,
                        startTime: this.startTime,
                        endTime: this.endTime
                    };

                    this.broadcastBreakout('chat', jid, payload);
                }
            }

            $('.btn-breakout')
                .removeClass('btn-primary btn-breakout-start')
                .addClass('btn-danger btn-breakout-stop')
                .text(i18n('breakout.reassemble'));

            let count = BreakoutHost.wait;
            this.setStatusMessage(i18n('breakout.breakoutWillStart', { sec: count }));
            this.countdownTimer = setInterval(() => {
                if (--count <= 0) {
                    clearInterval(this.countdownTimer);
                    this.countdownTimer = null;

                    this.clockTimer = setInterval(() => this.updateClock());

                    if (duration > 0) {
                        this.endBreakoutTimer = setTimeout(() => this.stopBreakout(), 60000 * duration);
                        this.setStatusMessage(i18n('breakout.breakoutStartedWithDuration', { min: duration }));
                    } else {
                        this.setStatusMessage(i18n('breakout.breakoutStarted'));
                    }
                } else {
                    this.setStatusMessage(i18n('breakout.breakoutWillStart', { sec: count }));
                }
            }, 1000);
        }

        stopBreakout() {
            if (!this.started) {
                console.error("stopBreakout error. no breakout has been started.");
                return;
            }

            let immediate = false;
            if (this.countdownTimer) {
                clearInterval(this.countdownTimer);
                this.countdownTimer = null;
                immediate = true;
            }

            clearInterval(this.clockTimer);
            this.clockTimer = null;

            const payload = {
                action: 'stop-breakout',
                url: location.href,
                wait: BreakoutHost.wait,
            };

            this.broadcastBreakout('groupchat', getConferenceJid(), payload);
            
            for (const room of this.startedRooms) {
                const jid = room + '@' + this.domain;
                this.joinRoom(jid);
                setTimeout(() => {
                    this.broadcastBreakout('groupchat', jid, payload);
                    setTimeout(() => { this.exitRoom(jid) }, 1000);
                }, 1000);
            }

            let endBreakout = () => {
                this.setStatusMessage(i18n('breakout.breakoutHasEnded'));
                $('.btn-breakout')
                    .removeClass('btn-danger btn-breakout-stop')
                    .addClass('btn-primary btn-breakout-start')
                    .text(i18n('breakout.breakout'))
                    .prop('disabled', false);
            };

            if (this.endBreakoutTimer) {
                clearTimeout(this.endBreakoutTimer);
                this.endBreakoutTimer = null;
            }

            this.startTime = null;
            this.endTime = null;
            this.updateClock()

            $('.btn-breakout').prop('disabled', true);

            if (immediate) {
                endBreakout();
            } else {
                let count = BreakoutHost.wait;
                this.setStatusMessage(i18n('breakout.breakoutWillEnd', { sec: count }));
                this.countdownTimer = setInterval(() => {
                    if (--count <= 0) {
                        clearInterval(this.countdownTimer);
                        this.countdownTimer = null;
                        endBreakout();
                    } else {
                        this.setStatusMessage(i18n('breakout.breakoutWillEnd', { sec: count }));
                    }
                }, 1000);
            }
        }

        toggleBreakout() {
            console.debug("toggleBreakout");
            if (this.started) {
                this.stopBreakout();
            } else {
                this.startBreakout();
            }
        }

        updateClock() {
            if (this.startTime) {
                const now = Math.floor((new Date()).getTime() / 1000);
                if (this.endTime) {
                    this.$clock.text(i18n('breakout.remainingTime') + ' ' + formatTimeSpan(this.endTime - now));
                } else {
                    this.$clock.text(i18n('breakout.elapsedTime') + ' ' + formatTimeSpan(now - this.startTime));
                }
            } else {
                this.$clock.text('');
            }
        }

        setStatusMessage(message) {
            this.$status.text(message);
        }

        exitRoom(jid) {
            console.debug("exitRoom", jid);

            const xmpp = APP.connection.xmpp.connection._stropheConn;
            const to = Strophe.getBareJidFromJid(jid) + '/' + getLocalDisplayName();
            xmpp.send($pres({ type: 'unavailable', to: to }));
        }

        joinRoom(jid) {
            console.debug("joinRoom", jid);

            const xmpp = APP.connection.xmpp.connection._stropheConn;
            const to = Strophe.getBareJidFromJid(jid) + '/' + getLocalDisplayName();
            xmpp.send($pres({ to: to }).c("x", { xmlns: Strophe.NS.MUC }));
        }

        broadcastBreakout(type, jid, json) {
            console.debug("broadcastBreakout", type, jid, json);

            const xmpp = APP.connection.xmpp.connection._stropheConn;
            xmpp.send($msg({ type: type, to: jid }).c('json', { xmlns: 'urn:xmpp:json:0' }).t(JSON.stringify(json)));
        }

        broadcastMessage(text) {
            console.debug("broadcastMessage", text, breakoutHost);

            const xmpp = APP.connection.xmpp.connection._stropheConn;
            for (const room of this.startedRooms) {
                const jid = room + '@' + this.domain;

                this.joinRoom(jid);
                setTimeout(() => {
                    xmpp.send($msg({ type: 'groupchat', to: jid }).c("body").t(text));
                    setTimeout(() => { this.exitRoom(jid) }, 1000);
                }, 1000);
            }
        }

        openRoom(boardId) {
            console.debug("joinRoom", boardId);
            const $board = $(this.kanban.findBoard(boardId));
            const roomName = $board.attr("data-room-name");
            const roomTitle = $board.find('.kanban-title-board').text();

            if (roomName) {
                const url = new URL(roomName, location.href);
                url.hash = 'subject=' + getConferenceName() + ' - ' + roomTitle;
                open(url.href, roomName);
            }
        }
    }

    function doBreakout() {
        const template = `
<div class="modal-header">
    <h4 class="modal-title">${i18n('breakout.breakoutRooms')} - ${i18n('breakout.participants', { title: '<span id="breakout-title"></span>' })}</h4>
    <form id="breakout-option" class="form-inline">
        <div class="form-group">
            <label for="breakout-duration">${i18n('breakout.duration')}</label>
            <input id="breakout-duration" class="form-control" type="number" min="0" max="480" step="30" name="breakout-duration" value="${BreakoutHost.defaultDuration}"/>
        </div>
        <div class="form-group">
            <label for="breakout-rooms">${i18n('breakout.rooms')}</label>
            <input id="breakout-rooms" class="form-control" type="number" min="1" max="100" name="breakout-rooms" value="0"/>
        </div>
    </form>
    <div id="breakout-status"></div>
    <div id="breakout-clock"></div>
</div>
<div class="modal-body">
    <div class="pade-col-container breakout-kanban"></div>
</div>`;

        if (!breakoutModal) {
            breakoutModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: 'Close',
                cssClass: ['breakout-modal', 'modal-lg', 'modal-fix-height'],

                beforeOpen: function () {
                    console.debug("beforeOpen", breakoutHost);
                    breakoutHost.refresh();
                }
            });

            breakoutModal.setContent(template);

            breakoutModal.addFooterBtn(i18n('breakout.allocate'), 'btn btn-primary', () => {
                breakoutHost.allocateToRooms();
            });

            breakoutModal.addFooterBtn(i18n('breakout.breakout'), 'btn btn-primary btn-breakout btn-breakout-start', () => {
                breakoutHost.toggleBreakout();
            });

            breakoutModal.addFooterBtn(i18n('breakout.close'), 'btn btn-secondary', () => {
                breakoutModal.close();
            });

            if (!breakoutHost) {
                breakoutHost = new BreakoutHost();
            }

            $('#breakout-rooms').on('input.breakoutroom focusout.breakoutroom', (evt) => {
                if (!evt.originalEvent.inputType || !evt.originalEvent.inputType.match(/delete/)) {
                    breakoutHost.resizeRoom(evt.target.value);
                }
                return false;
            });
        }

        breakoutModal.open();
    }

    //-------------------------------------------------------
    //
    //  SpeechRecognition
    //
    //-------------------------------------------------------

    function sendSpeechRecognition(result) {
        if (result != "" && APP.conference && APP.conference._room && !captions.transcriptDisabled) {
            var message = "[" + result + "]";
            console.debug("Speech recog result", APP.conference._room, message);

            APP.conference._room.sendTextMessage(message);
            ofm.currentTranslation = [];
        }
    }

    function setupSpeechRecognition() {
        console.debug("setupSpeechRecognition");

        ofm.recognition = new webkitSpeechRecognition();
        ofm.recognition.lang = config.defaultLanguage;
        ofm.recognition.continuous = true;
        ofm.recognition.interimResults = false;

        ofm.recognition.onresult = function (event) {
            console.debug("Speech recog event", event)

            if (event.results[event.resultIndex].isFinal == true) {
                var transcript = event.results[event.resultIndex][0].transcript;
                console.debug("Speech recog transcript", transcript);
                sendSpeechRecognition(transcript);
            }
        }

        ofm.recognition.onspeechend = function (event) {
            console.debug("Speech recog onspeechend", event);
        }

        ofm.recognition.onstart = function (event) {
            console.debug("Speech to text started", event);
            ofm.recognitionActive = true;
        }

        ofm.recognition.onend = function (event) {
            console.debug("Speech to text ended", event);

            if (ofm.recognitionActive) {
                console.debug("Speech to text restarted");
                setTimeout(function () { ofm.recognition.start() }, 1000);
            }
        }

        ofm.recognition.onerror = function (event) {
            console.debug("Speech to text error", event);
            console.debug('Speech recognition error detected: ' + event.error);
            console.debug('Additional information: ' + event.message);
            if (event.error && event.error == "network") {
                ofm.recognitionActive = false;
            }
        }

        ofm.recognition.start();
    }

    //-------------------------------------------------------
    //
    //  File upload handler
    //
    //-------------------------------------------------------

    function doAvatar() {
        const upload = document.getElementById("ofmeet-upload-avatar");

        if (upload) upload.addEventListener('change', function (event) {
            uploadAvatar(event);
        });
        upload.click();
    }

    function uploadAvatar(event) {
        var files = event.target.files;

        for (const file of files) {
            if (file.name.toLowerCase().endsWith(".png") || file.name.toLowerCase().endsWith(".jpg") || file.type == 'image/png' || file.type == 'image/jpeg') {
                if (file.size <= avatarFileSizeLimit) {
                    var reader = new FileReader();

                    reader.onload = function (event) {
                        console.debug('uploadAvatar');
                        changeAvatar(event.target.result, AvatarType.UPLOAD);
                    };

                    reader.onerror = function (event) {
                        console.error("uploadAvatar - error", event);
                        APP.UI.messageHandler.showWarning({title:i18n('avatar.avatarUpload'), description:i18n('avatar.imageFileError') + event});
                    };

                    reader.readAsDataURL(file);
                } else {
                    APP.UI.messageHandler.showWarning({title:i18n('avatar.avatarUpload'), description:i18n('avatar.imageFileSizeError', { size: (avatarFileSizeLimit / 1024 / 1024) })});
                }
            } else {
                APP.UI.messageHandler.showWarning({title:i18n('avatar.avatarUpload'), description:i18n('avatar.imageFileTypeError')});
            }
        }
    }

    function handleDragOver(evt) {
        evt.stopPropagation();
        evt.preventDefault();
        evt.dataTransfer.dropEffect = 'copy';
    }

    function handleDropFileSelect(evt) {
        evt.stopPropagation();
        evt.preventDefault();

        var files = evt.dataTransfer.files;

        for (var i = 0, f; f = files[i]; i++) {
            uploadFile(f);
        }
    }

    function uploadFile(file) {
        console.debug("uploadFile", file);

        const connection = APP.connection.xmpp.connection;

        const iq = $iq({ type: 'get', to: "httpfileupload." + connection.domain }).c('request', { xmlns: 'urn:xmpp:http:upload' }).c('filename').t(file.name).up().c('size').t(file.size);

        connection.sendIQ(iq, function (response) {
            response.querySelectorAll('slot').forEach(function (slot) {
                const putUrl = slot.querySelector('put').innerHTML;
                const getUrl = slot.querySelector('get').innerHTML;

                console.debug("uploadFile", putUrl, getUrl);

                if (putUrl != null & getUrl != null) {
                    var req = new XMLHttpRequest();

                    req.onreadystatechange = function () {
                        if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
                            console.debug("uploadFile ok", this.statusText);
                            APP.conference._room.sendTextMessage(getUrl);
                        } else

                        if (this.readyState == 4 && this.status >= 400) {
                            console.error("uploadFile error", this.statusText);
                            APP.conference._room.sendTextMessage(this.statusText);
                        }

                    };
                    req.open("PUT", putUrl, true);
                    req.send(file);
                }
            });

        }, function (error) {
            console.error(error);
        });
    }

    //-------------------------------------------------------
    //
    //  push notification
    //
    //-------------------------------------------------------

    function getServiceWorker(callback) {
        if (swRegistration) callback(swRegistration);
        else {
            if ('serviceWorker' in navigator) {
                console.debug('Service Worker is supported');

                navigator.serviceWorker.register('./webpush-sw.js').then(function (registration) {
                    swRegistration = registration;
                    callback(registration);

                }).catch(function (error) {
                    console.error('Service Worker Error, cannot register service worker', error);
                    callback();
                });
            } else {
                console.warn('Service Worker is not supported');
                callback();
            }
        }
    }

    function setupPushNotification() {
        if ('PushManager' in window) {
            getServiceWorker(function (registration) {
                console.debug('Push notification is supported');

                if (registration) registration.pushManager.getSubscription().then(function (subscription) {
                    if (subscription && !storage.getItem('pade.vapid.keys')) {
                        subscription.unsubscribe();
                        subscription = null;
                    }

                    if (!subscription) {
                        makeSubscription(function (err, subscription, keys) {
                            if (err) {
                                console.error('makeSubscription error, no push messaging', err);
                            } else {
                                handleSubscription(subscription, keys);
                            }
                        })
                    } else {
                        handleSubscription(subscription, JSON.parse(storage.getItem('pade.vapid.keys')));
                    }

                }).catch(function (error) {
                    console.error('Error unsubscribing, no push messaging', error);
                });
            });
        } else {
            console.warn('Push messaging is not supported');
        }
    }

    function makeSubscription(callback) {
        const keys = window.WebPushLib.generateVAPIDKeys();
        console.debug('makeSubscription', keys);

        swRegistration.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: urlBase64ToUint8Array(keys.publicKey) }).then(function (subscription) {
            console.debug('User is subscribed.');
            storage.setItem('pade.vapid.keys', JSON.stringify(keys));
            if (callback) callback(false, subscription, keys);

        }).catch(function (err) {
            console.error('Failed to subscribe the user: ', err);
            if (callback) callback(true);
        });
    }

    function handleSubscription(subscription, keys) {
        console.debug('handleSubscription', subscription, keys);

        const secret = btoa(JSON.stringify({ privateKey: keys.privateKey, publicKey: keys.publicKey, subscription: subscription, lastModified: Date.now() }));
        window.WebPushLib.setVapidDetails('xmpp:' + APP.connection.xmpp.connection.domain, keys.publicKey, keys.privateKey);
        window.WebPushLib.selfSecret = secret;

        listenForWorkerEvents();
        createContactsButton();
        publishWebPush();
    }

    function listenWebPushEvents() {
        const connection = APP.connection.xmpp.connection;

        connection.addHandler(function (message) {
            console.debug('webpush handler', message);
            const handleElement = message.querySelector('webpush');

            if (handleElement && message.getAttribute("type") != 'error') {
                const secret = handleElement.innerHTML;
                const id = Strophe.getResourceFromJid(message.getAttribute("from"));
                const participant = APP.conference.getParticipantById(id);

                if (participant && participant._displayName) {
                    console.debug('webpush contact', id, participant, participant._displayName);
                    storage.setItem('pade.webpush.' + participant._displayName, atob(secret));
                } else if (APP.conference.getMyUserId() == id) {
                    // storage.setItem('pade.webpush._self' , atob(secret)); // activate this line for debugging
                }

            }

            return true;

        }, "urn:xmpp:push:0", "message");
    }

    function listenForWorkerEvents() {
        navigator.serviceWorker.onmessage = function (event) {
            console.debug("Broadcasted from service worker : ", event.data);

            if (event.data.options) // subscription renewal.
            {
                makeSubscription(function (err, subscription, keys) {
                    if (!err) {
                        handleSubscription(subscription, keys);
                    }
                })
            }
        }
    }

    function doContacts() {
        const template =
            '<div class="modal-header">' +
            '    <h4 class="modal-title">Contacts Manager</h4><p><br/></p>' +
			'    <span style="float: right;"><b>Date: &nbsp;</b><input id="meeting-date" type="date"><input type="time" id="meeting-time" min="09:00" max="18:00"></span>' + 					
            '</div>' +
            '<div class="modal-body">' +
            '    <div class="pade-col-container meeting-contacts">' +
            '   </div>' +	
            '</div>'

        if (!contactsModal) {
            contactsModal = new tingle.modal({
                footer: true,
                stickyFooter: false,
                closeMethods: ['overlay', 'button', 'escape'],
                closeLabel: "Close",
                cssClass: ['custom-class-1', 'custom-class-2'],

                beforeOpen: function () {
                    console.debug("beforeOpen");

                    const container = document.querySelector(".meeting-contacts");

                    if (!contactsModalOpened) {
                        container.addEventListener("click", function (evt) {
                            evt.stopPropagation();
                            var parent = evt.target.parentNode;

							while (parent && parent.tagName != "LI") {
								parent = parent.parentNode;
							}	
								
							if (parent) {
								const contact = parent.getAttribute("data-contact");
								const email = parent.getAttribute("data-email");
								const ele = parent.querySelector(".meeting-icon");
								const selected = parent.querySelector(".meeting-icon > img");
								const image = email ? IMAGES.mail : IMAGES.contact;

								if (ele && contact) {
									console.debug("beforeOpen - click", contact, ele);
									const emailAttr = email ? 'data-email="' + email + '"' : '';
									if (ele) ele.innerHTML = selected ? image : '<img ' + emailAttr + ' data-contact="' + contact + '" width="24" height="24" src="./check-solid.png">';
								}
							}
                        });

                        contactsModalOpened = true;
                    }

                    container.innerHTML = "";

                    var contacts = {};

                    for (var i = 0; i < storage.length; i++) {
                        if (storage.key(i).indexOf("pade.webpush.") == 0) {
                            const name = storage.key(i).substring(13);
                            const email = storage.getItem("pade.email." + name);
                            contacts[name] = email;
                        }
                    }

                    Object.entries(contacts).sort((a, b) => a[0].localeCompare(b[0])).forEach(function (n) {
                        addContact(n[0], contacts[n[0]])
                    });
                }
            });

            contactsModal.addFooterBtn('Invite Selected', 'btn btn-danger tingle-btn tingle-btn--primary', function () {
                const container = document.querySelector(".meeting-contacts");
				const msgTime = document.querySelector("#meeting-time").value.trim();
				const msgDate = document.querySelector("#meeting-date").value.trim();
				
				console.debug("addFooterBtn", msgTime, msgDate);
				
                container.querySelectorAll(".meeting-icon > img").forEach(function (icon) {
                    const contact = icon.getAttribute("data-contact");
					const sender = getLocalDisplayName();
                    let message = sender + ' invites you to join the room ' + APP.conference.roomName;
					
					if (msgDate != "") {
						message = message + " on " + msgDate + " at " + msgTime;
					}

                    sendWebPush(message, sender, contact, msgTime, msgDate, function (name, error) {
                        let image = './delivered.png';
                        if (error) image = './times-solid.png';
                        icon.outerHTML = '<img data-contact="' + name + '" width="24" height="24" src="' + image + '">';
                    });
                });
            });

            contactsModal.addFooterBtn('Email Selected', 'btn btn-danger tingle-btn tingle-btn--primary', function () {
                const container = document.querySelector(".meeting-contacts");
                let mailto = "mailto:";

                container.querySelectorAll(".meeting-icon > img").forEach(function (icon) {
                    const email = icon.getAttribute("data-email");
                    if (email) mailto = mailto + email + ";"
                });

                if (mailto != "mailto:") {
                    mailto = mailto + "?subject=" + interfaceConfig.APP_NAME + "&body=" + location.href + "\n\n";
                    window.open(encodeURI(mailto));
                }
            });
			
            contactsModal.addFooterBtn('Reset Selected', 'btn btn-success btn-primary', function () {
                const container = document.querySelector(".meeting-contacts");

                container.querySelectorAll(".meeting-icon > img").forEach(function (icon) {
                    icon.outerHTML = IMAGES.contact;
                });
            });

            contactsModal.addFooterBtn('Close', 'btn btn-success btn-secondary', function () {
                contactsModal.close();
            });	


            contactsModal.addFooterBtn('Send Invite to next Meeting', 'btn btn-primary', function () {
                const container = document.querySelector(".meeting-contacts");
				const msgTime = document.querySelector("#meeting-time").value.trim();
				const msgDate = document.querySelector("#meeting-date").value.trim();
				
				if (msgTime == "" || msgDate == "") {
					APP.UI.messageHandler.showError({ title: "Next Meeting Invite Error", description: "Date or Time missing", hideErrorSupportLink: true });
					return;
				}
				
				const action = 'plan-new-meeting';
				const sender = getLocalDisplayName();
				const url = location.href;
				const roomName = APP.conference.roomName;			
				
				const json = {action, sender, url, roomName, msgDate, msgTime};	
				const xmpp = APP.connection.xmpp.connection._stropheConn;
				xmpp.send($msg({ type: 'groupchat', to: getConferenceJid() }).c('json', { xmlns: 'urn:xmpp:json:0' }).t(JSON.stringify(json)));				

            });				

            contactsModal.setContent(template);
        }

        if (APP.conference._room.isSIPCallingSupported() && !inviteByPhone) {
            inviteByPhone = true;

            contactsModal.addFooterBtn('Invite by Phone', 'btn btn-primary', function () {
                const phoneNumber = prompt("Please enter phone number");

                if (phoneNumber && phoneNumber != "") {
                    contactsModal.close();
                    APP.conference._room.dial(phoneNumber);
                }

            });
        }
        contactsModal.open();
    }

    function addContact(name, email) {
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

    function publishWebPush() {
        const connection = APP.connection.xmpp.connection;

        if (window.WebPushLib && window.WebPushLib.selfSecret && APP.conference._room.room) {
            console.debug("publishWebPush", window.WebPushLib.selfSecret);
            connection.send($msg({ to: APP.conference._room.room.roomjid, type: 'groupchat' }).c('webpush', { xmlns: "urn:xmpp:push:0" }).t(window.WebPushLib.selfSecret));
        }
    }

    function sendWebPush(body, sender, name, time, date, callback) {
        console.debug('sendWebPush', body, name, time, date);

        if (storage.getItem('pade.webpush.' + name)) {
            const secret = JSON.parse(storage.getItem('pade.webpush.' + name));
            const payload = { msgSubject: interfaceConfig.APP_NAME, msgBody: body, msgType: 'meeting', url: location.href, msgTime: time, msgDate: date, name, sender, roomName: APP.conference.roomName};
			const data = {payload, publicKey: secret.publicKey, privateKey: secret.privateKey, subscription: secret.subscription};
			const host = config.bosh.split("/")[2];
			
            console.debug("sendWebPush data", host, data);
			
            fetch(location.protocol + "//" + host + "/rest/api/restapi/v1/meet/webpushsend", { method: "POST", body: JSON.stringify(data) }).then((success) => {
                console.debug("webpushsend ok");
                if (callback) callback(name);
            }).catch((error) => {
                console.error("webpushsend error", error);
                if (callback) callback(name, error);				
            })

			/* TODO go back to client-side when free proxy available or browser vendors support CORS
		
				window.WebPushLib.setVapidDetails('xmpp:' + APP.conference._room.room.myroomjid, secret.publicKey, secret.privateKey);

				window.WebPushLib.sendNotification(secret.subscription, JSON.stringify(payload), { TTL: 60 }).then(response => {
					console.debug("Web Push Notification is sended!");
					if (callback) callback(name);
				}).catch(e => {
					console.error('Failed to notify', name, e)
					if (callback) callback(name, e);
				})
			*/			
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
    //  Web Authentication 
    //
    //-------------------------------------------------------

    function registerWebAuthn() {
        console.debug("registerWebAuthn");
        const username = Strophe.getNodeFromJid(APP.connection.xmpp.connection._stropheConn.authzid);

        let bufferDecode = function (e) {
            const t = "==".slice(0, (4 - e.length % 4) % 4),
                n = e.replace(/-/g, "+").replace(/_/g, "/") + t,
                r = atob(n),
                o = new ArrayBuffer(r.length),
                c = new Uint8Array(o);
            for (let e = 0; e < r.length; e++) c[e] = r.charCodeAt(e);
            return o
        }

        let bufferEncode = function (e) {
            const t = new Uint8Array(e);
            let n = "";
            for (const e of t) n += String.fromCharCode(e);
            return btoa(n).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "")
        }

        let displayName = "Unknown";

        if (storage.getItem('features/base/settings')) {
            const json = JSON.parse(storage.getItem("features/base/settings"));

            if (json && json.displayName) {
                displayName = json.displayName;
            }
        }

        fetch(location.protocol + "//" + location.host + "/rest/api/restapi/v1/meet/webauthn/register/start/" + username, { method: "POST", body: displayName }).then(function (response) { return response.json() }).then((credentialCreationOptions) => {
            console.debug("/webauthn/register/start", credentialCreationOptions);

            // confirm webauthn register after first step because second step fails with a re-register
            storage.setItem("ofmeet.webauthn.username", username);

            if (credentialCreationOptions.excludeCredentials) {
                credentialCreationOptions.excludeCredentials.forEach(function (listItem) {
                    listItem.id = bufferDecode(listItem.id)
                });
            }

            credentialCreationOptions.challenge = bufferDecode(credentialCreationOptions.challenge);
            credentialCreationOptions.user.id = bufferDecode(credentialCreationOptions.user.id);
            return navigator.credentials.create({ publicKey: credentialCreationOptions });

        }).then((cred) => {
            console.debug("/webauthn/register/start - cred", cred);
            const credential = {};
            credential.id = cred.id;
            credential.rawId = bufferEncode(cred.rawId);
            credential.type = cred.type;

            if (cred.response) {
                const clientDataJSON = bufferEncode(cred.response.clientDataJSON);
                const attestationObject = bufferEncode(cred.response.attestationObject);
                credential.response = { clientDataJSON, attestationObject };
                if (!credential.clientExtensionResults) credential.clientExtensionResults = {};
            }

            fetch(location.protocol + "//" + location.host + "/rest/api/restapi/v1/meet/webauthn/register/finish/" + username, { method: "POST", body: JSON.stringify(credential) }).then((success) => {
                console.debug("webauthn/register/finish ok");

            }).catch((error) => {
                console.error("webauthn/register/finish error", error);
            })

        }).catch((error) => {
            console.error("webauthn/register/start error", error);
        })
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

    ofm.recording = false;
	ofm.getAllParticipants = getAllParticipants;
	ofm.getLocalDisplayName = getLocalDisplayName;
	ofm.getParticipantDisplayName = getParticipantDisplayName;

    return ofm;

}(ofmeet || {}));
