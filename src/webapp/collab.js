/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

var collab = (function (coop) {
    'use strict';

    const FOREGROUND_COLORS = ['#111', '#eee'];
    const CLICK_TRANSITION_TIME = 3000;
    const MOUSE_IDLE_TIMEOUT = 10000;
    const MOUSE_UPDATE_MIN_TIME = 200;

    var _state = null;

    // utility functions
    function clamp(x, min, max) {
        if (x < min)
            return min;
        else if (x > max)
            return max;
        return x;
    }

    function round(value, decimals) {
        return Number(Math.round(value + 'e' + decimals) + 'e-' + decimals);
    }

    function getLargeVideoId() {
        return APP.UI.getLargeVideoID();
    }

    function getLocalVideoId() {
        return APP.conference.getMyUserId();
    }

    function getConference() {
        return _state['features/base/conference'].conference;
    }

    function getParticipant(id) {
        return getConference().participants[id];
    }

    function isTileViewEnabled() {
        let tileViewEnabled = _state['features/video-layout'].tileViewEnabled;

        // Immediately after join, _state['features/video-layout'].tileViewEnabled is undefined.
        if (tileViewEnabled == null) {
            tileViewEnabled = $('#videoconference_page').hasClass('tile-view');
        }

        return tileViewEnabled;
    }

    function getVideoIdFromElementId(id) {
        id = String(id);
        if (id == 'localVideoContainer') {
            return getLocalVideoId();
        } else {
            let m = id.match(/^participant_(.+?)$/);
            if (m) {
                return m[1];
            }
        }
        return null;
    }

    function getColorFromId(id) {
        return '#' + id.substr(0, 6);
    }

    class PointTransform {
        static _invalidPos = { x: -100, y: -100 };
        static _$videos = {};

        static updateVideos() {
            PointTransform._$videos = {};
            if (!isTileViewEnabled()) {
                PointTransform._$videos[getLargeVideoId()] = $('#largeVideoWrapper');
            } else {
                PointTransform._$videos = {};
                $('span.videocontainer').each((idx, elem) => {
                    let $elem = $(elem);
                    PointTransform._$videos[getVideoIdFromElementId($elem.attr('id'))] = $elem;
                });
            }
        }

        static shouldInvert(videoId) {
            return (
                APP.conference.isLocalId(videoId) &&
                APP.UI.getLargeVideo().containers.camera.localFlipX &&
                !APP.conference.isLocalVideoMuted() &&
                APP.UI.getLargeVideo().state == 'camera');
        }

        static videoToPage(point, videoId) {
            let video = PointTransform._$videos[videoId];
            if (!video) {
                // for when someone joins the room during tile view
                PointTransform.updateVideos();
                video = PointTransform._$videos[videoId];
                if (!video) {
                    return PointTransform._invalidPos;
                }
            }

            let offset = video.offset();
            let width = video.width();
            let height = video.height();
            if (!offset || !width || !height) { return PointTransform.invalidPos; }

            let pPoint = { x: point.x, y: point.y };
            if (PointTransform.shouldInvert(videoId)) {
                pPoint.x = 1 - pPoint.x;
            }

            return {
                x: (clamp(pPoint.x, 0, 1) * width) + offset.left,
                y: (clamp(pPoint.y, 0, 1) * height) + offset.top
            };
        }

        static pageToVideo(point, videoId) {
            let video = PointTransform._$videos[videoId];
            if (!video) {
                // for when someone joins the room during tile view
                PointTransform.updateVideos();
                video = PointTransform._$videos[videoId];
                if (!video) {
                    return PointTransform._invalidPos;
                }
            }

            let offset = video.offset();
            let width = video.width();
            let height = video.height();
            if (!offset || !width || !height) { return PointTransform.invalidPos; }

            let vPoint = {
                x: round(clamp((point.x - offset.left) / width, 0, 1), 5),
                y: round(clamp((point.y - offset.top) / height, 0, 1), 5)
            };

            if (PointTransform.shouldInvert(videoId)) {
                vPoint.x = round(1 - vPoint.x, 5);
            }

            return vPoint;
        }
    }

    class Cursor {
        constructor(clientId) {
            this.clientId = clientId;
            this.clientName = '';

            this.videoId = null;
            this.videoPos = { x: 0, y: 0 };
            this.pagePos = { x: 0, y: 0 };

            this.color = getColorFromId(clientId);

            this.$element = $('<div id="togetherjs-template-cursor-' + clientId + '" class="togetherjs-cursor togetherjs"><svg id="Layer" width="11.647" height="20" viewBox="0 0 11.647 20" fill="#000000"><g><path d="M 11.562 13.295 C 11.695 13.067 11.671 12.778 11.5 12.576 L 1.103 0.223 C 0.933 0.022 0.657 -0.052 0.412 0.038 C 0.165 0.128 0 0.363 0 0.625 L 0 16.934 C 0 17.203 0.177 17.43 0.429 17.527 C 0.722 17.641 1.005 17.469 1.127 17.306 L 3.921 13.541 L 5.86 19.088 C 6.108 19.798 6.886 20.172 7.596 19.924 C 8.305 19.675 8.681 18.898 8.432 18.188 L 6.53 12.746 L 10.902 13.592 C 11.166 13.643 11.426 13.522 11.562 13.295 Z"></path></g></svg><span class="togetherjs-cursor-container"><span class="togetherjs-cursor-name"></span></span></div>');
            this.$element.hide();
            Cursor.container.append(this.$element);

            this.status = 'hidden';

            if (APP.conference.isLocalId(clientId)) {
                this.isLocal = true;
                Cursor.localCursor = this;
            } else {
                this.isLocal = false;
            }

            this.lastTime = Date.now();
            this.idleTimer = null;
        }

        shouldShow() {
            return this.isLocal || isTileViewEnabled() || this.videoId == getLargeVideoId();
        }

        setColor(color) {
            if (color && color.match(/^#[0-9a-f]{3}|[0-9a-f]{6}$/i)) {
                this.color = color;
                this.$element.css('color', this.color);

                this.$element.find('.togetherjs-cursor-container').css({
                    backgroundColor: this.color,
                    color: tinycolor.mostReadable(this.color, FOREGROUND_COLORS)
                });

                var path = this.$element.find('svg path');
                path.attr('fill', this.color);
            }
        }

        setName(name) {
            if (name && this.clientName !== name) {
                this.clientName = name;
                this.$element.find('.togetherjs-cursor-name').text(this.clientName);
            }
        }

        setVideoId(videoId) {
            if (videoId && this.videoId !== videoId) {
                this.videoId = videoId;
            }
        }

        setPosition(pos) {
            clearTimeout(this.idleTimer);
            this.idleTimer = setTimeout(() => {
                this.status = 'hidden';
                this.$element.fadeOut(1000);
            }, MOUSE_IDLE_TIMEOUT);

            let now = Date.now();
            let delta = now - this.lastTime;
            if (!this.isLocal && delta >= MOUSE_UPDATE_MIN_TIME && delta < MOUSE_UPDATE_MIN_TIME * 2) {
                this.$element.css('transition', 'transform ' + delta + 'ms linear');
            }
            this.lastTime = now;

            if (this.isLocal) {
                this.pagePos = pos;
            } else {
                this.videoPos = pos;
            }

            this.update();
        }

        update() {
            if (this.shouldShow()) {
                if (this.status !== 'shown') {
                    this.status = 'shown';
                    this.$element.show();
                }

                let pagePos;
                if (this.isLocal) {
                    pagePos = this.pagePos;
                } else {
                    pagePos = PointTransform.videoToPage(this.videoPos, this.videoId);
                }

                this.$element.css('transform', 'translate(' + pagePos.x + 'px,' + pagePos.y + 'px)');
            } else {
                if (this.status !== 'hidden') {
                    this.status = 'hidden';
                    this.$element.hide();
                }
            }
        }

        click(color) {
            // FIXME: should we hide the local click if no one else is going to see it?
            // That means tracking who might be able to see our screen.
            let $element = $('<div class="togetherjs-click togetherjs"></div>');
            Cursor.container.append($element);

            let pagePos;
            if (this.isLocal) {
                pagePos = this.pagePos;
            } else {
                pagePos = PointTransform.videoToPage(this.videoPos, this.videoId);
            }

            $element.css({
                top: pagePos.y,
                left: pagePos.x,
                borderColor: color,
                'animation-duration': CLICK_TRANSITION_TIME + 'ms'
            });

            setTimeout(() => {
                $element.remove();
            }, CLICK_TRANSITION_TIME);
        }

        destroy() {
            this.$element.remove();
            this.$element = null;
            clearTimeout(this.idleTimer);
            if (this.isLocal) {
                Cursor.localCursor = null;
            }
            delete Cursor._cursors[this.clientId];
        }

        static _cursors = {};

        static localCursor = null;

        static create(clientId) {
            let cursor = new Cursor(clientId);

            Cursor._cursors[clientId] = cursor;

            if (APP.conference.isLocalId(clientId)) {
                cursor.setName(ofmeet.getLocalDisplayName());
                cursor.setColor(getConference().getLocalParticipantProperty('cursorColor'));
            } else {
                let participant = getParticipant(clientId);
                if (participant) {
                    cursor.setName(participant._displayName);
                    cursor.setColor(participant._properties.cursorColor);
                }
            }

            return cursor;
        }

        static get(clientId, createNew = true) {
            let cursor = Cursor._cursors[clientId];
            if (!cursor && createNew) {
                cursor = Cursor.create(clientId);
            }
            return cursor;
        }

        static destroy(clientId) {
            let cursor = Cursor._cursors[clientId];
            if (cursor) {
                cursor.destroy();
            }
        }

        static forEach(callback, context) {
            context = context || null;
            for (let a in Cursor._cursors) {
                if (Cursor._cursors.hasOwnProperty(a)) {
                    callback.call(context, Cursor._cursors[a], a);
                }
            }
        }
    }

    class Collab {
        constructor() {
            this.displayMouseClick = true;

            this.lastTime = 0;
            this.lastPosX = -1;
            this.lastPosY = -1;
            this.fixPosTimer = null;

            this.localCursorColor = null;
        }

        sendCommand(action, message) {
            setTimeout(() => {
                APP.conference.commands.sendCommandOnce('CURSOR', {
                    value: action,
                    attributes: {
                        ...message,
                        from: getLocalVideoId(),
                    }
                });
            });
        }

        onMouseMove(event) {
            let posX = event.pageX;
            let posY = event.pageY;

            Cursor.localCursor.setVideoId(event.videoId);
            Cursor.localCursor.setPosition({ x: posX, y: posY });

            clearTimeout(this.fixPosTimer);

            let now = Date.now();
            if (now - this.lastTime < MOUSE_UPDATE_MIN_TIME) {
                this.fixPosTimer = setTimeout(() => this.onMouseMove(event), MOUSE_UPDATE_MIN_TIME * 1.5);
                return;
            }
            this.lastTime = now;

            if (Math.abs(this.lastPosX - posX) < 3 && Math.abs(this.lastPosY - posY) < 3) {
                // Not a substantial enough change
                return;
            }
            this.lastPosX = posX;
            this.lastPosY = posY;

            let videoId = null;
            if (!isTileViewEnabled()) {
                videoId = getLargeVideoId();
            } else {
                videoId = getVideoIdFromElementId(event.currentTarget.id);
            }

            let videoPos = PointTransform.pageToVideo({ x: posX, y: posY }, videoId);

            this.sendCommand('cursor-update', {
                videoId: videoId,
                x: videoPos.x,
                y: videoPos.y
            });

            return false;
        }

        onMouseOut(event) {
            clearTimeout(this.fixPosTimer);

            this.fixPosTimer = setTimeout(() => {
                Cursor.localCursor.setVideoId('#outside');
                Cursor.localCursor.setPosition({ x: -100, y: -100 });

                this.sendCommand('cursor-update', {
                    videoId: '#outside',
                    x: -1,
                    y: -1
                });

            }, MOUSE_UPDATE_MIN_TIME);

            return false;
        }

        onMouseClick(event) {
            let posX = event.pageX;
            let posY = event.pageY;

            Cursor.localCursor.setVideoId(event.videoId);
            Cursor.localCursor.setPosition({ x: posX, y: posY });
            Cursor.localCursor.click('red');

            let videoId = null;
            if (!isTileViewEnabled()) {
                videoId = getLargeVideoId();
            } else {
                videoId = getVideoIdFromElementId(event.currentTarget.id);
            }

            let videoPos = PointTransform.pageToVideo({ x: posX, y: posY }, videoId);

            this.sendCommand('cursor-click', {
                videoId: videoId,
                x: videoPos.x,
                y: videoPos.y,
            });

            return false;
        }

        handleCursorClick(event) {
            if (!('x' in event) || !('y' in event)) return false;

            let cursor = Cursor.get(event.from);

            cursor.setVideoId(event.videoId);
            cursor.setPosition({ x: event.x, y: event.y });

            cursor.click('red');

            return true;
        };

        handleCursorUpdate(event) {
            if (!('x' in event) || !('y' in event)) return false;

            let cursor = Cursor.get(event.from);

            cursor.setVideoId(event.videoId);
            cursor.setPosition({ x: event.x, y: event.y });

            return true;
        }

        onParticipantPropertyChanged(participant, property, value) {
            console.log(participant._displayName + '.' + property + ' ' + value);
            switch (property) {
                case 'cursorShare':
                    if (value == 'true') {
                        Cursor.create(participant._id);
                    } else {
                        Cursor.destroy(participant._id);
                    }
                    break;
                case 'cursorColor':
                    let cursor = Cursor.get(participant._id, false);
                    if (cursor) {
                        cursor.setColor();
                    }
                    break;
            }
        }

        onCommandReceived(action, attributes) {
            if (!action || !attributes || !attributes.from || APP.conference.isLocalId(attributes.from)) return;

            switch (action) {
                case 'cursor-update':
                    this.handleCursorUpdate(attributes);
                    break;
                case 'cursor-click':
                    this.handleCursorClick(attributes);
                    break;
            }
        }

        init() {
            if (!APP) { console.warn("APP object is undefined."); }
            if (!$('#videospace').length) { console.warn("video DOM is not found."); }

            this.isSharing = false;

            _state = APP.store.getState();

            Cursor.container = $('<div id="cursorcontainer"></div>').appendTo(document.body);

            PointTransform.updateVideos();

            APP.UI.addListener('UI.large_video_id_changed', id => {
                PointTransform.updateVideos();
                Cursor.forEach(c => c.update());
            });

            APP.UI.addListener('UI.local_flipx_changed', enabled => {
                Cursor.forEach(c => c.update());
            });

            APP.UI.addListener('UI.tile_view_changed', enabled => {
                PointTransform.updateVideos();
                Cursor.forEach(c => c.update());

                this.unbindMouseEvent();
                if (this.isSharing) {
                    this.bindMouseEvent(enabled);
                }
            });

            APP.conference.commands.addCommandListener('CURSOR', event => {
                this.onCommandReceived(event.value, event.attributes);
            });

            let conference = getConference();

            conference.eventEmitter.setMaxListeners(20);

            conference.on(JitsiMeetJS.events.conference.PARTICIPANT_PROPERTY_CHANGED,
                (participant, property, oldValue, newValue) => {
                    this.onParticipantPropertyChanged(participant, property, newValue);
                });

            conference.on(JitsiMeetJS.events.conference.USER_LEFT,
                (id, participant) => {
                    Cursor.destroy(id);
                });
        }

        bindMouseEvent(tileViewEnabled) {
            if (tileViewEnabled) {
                $('#filmstripRemoteVideosContainer')
                    .on('mousemove.collab', 'span.videocontainer', this.onMouseMove.bind(this))
                    .on('click.collab', 'span.videocontainer', this.onMouseClick.bind(this))
                    .on('mouseleave.collab', 'span.videocontainer', this.onMouseOut.bind(this))
                    .find('span.videocontainer').css('cursor', 'none');
            } else {
                $('#largeVideoContainer')
                    .on('mousemove.collab', this.onMouseMove.bind(this))
                    .on('click.collab', this.onMouseClick.bind(this))
                    .css('cursor', 'none');

                $('#filmstripRemoteVideosContainer').css('pointer-events', 'auto');
                $('#filmstripLocalVideo').css('pointer-events', 'auto');
                $('div.filmstrip__toolbar').css('pointer-events', 'auto');
                $('#videospace>div.filmstrip ').css('pointer-events', 'none');
            }
        }

        unbindMouseEvent() {
            $('#filmstripRemoteVideosContainer')
                .off('mousemove.collab')
                .off('click.collab')
                .off('mouseleave.collab')
                .find('span.videocontainer').css('cursor', '');

            $('#largeVideoContainer')
                .off('mousemove.collab')
                .off('click.collab')
                .css('cursor', '');

            $('#filmstripRemoteVideosContainer').css('pointer-events', '');
            $('#filmstripLocalVideo').css('pointer-events', '');
            $('div.filmstrip__toolbar').css('pointer-events', '');
            $('#videospace>div.filmstrip ').css('pointer-events', '');
        }

        startSharing(color) {
            this.isSharing = true;

            if (color) {
                this.localCursorColor = color;
            } else if (!this.localCursorColor) {
                this.localCursorColor = getColorFromId(getLocalVideoId());
            }

            let conference = getConference();
            conference.setLocalParticipantProperty('cursorColor', color);
            conference.setLocalParticipantProperty('cursorShare', true);

            Cursor.create(getLocalVideoId());

            this.bindMouseEvent(isTileViewEnabled());
        }

        stopSharing() {
            this.isSharing = false;

            let conference = getConference();
            conference.setLocalParticipantProperty('cursorShare', false);

            Cursor.destroy(getLocalVideoId());

            this.unbindMouseEvent();
        }
    }

    if (!(coop instanceof Collab)) {
        coop = new Collab();
    }

    return coop;

}(collab || {}));
