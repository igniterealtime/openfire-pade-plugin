// push trigger

self.addEventListener('push', function (event) {
   const data = event.data.json();
   console.debug('Push message', data);

   const options = {
        body: data.msgBody,
        icon: './icon.png',
        vibrate: [100, 50, 100],
        data: data,
        actions: [
          {action: 'join', title: 'Join', icon: './check-solid.png'},
          {action: 'ignore', title: 'Ignore', icon: './times-solid.png'},
        ]
    };
    event.waitUntil(
        self.registration.showNotification("Openfire Meetings", options)
    );
});

self.addEventListener('pushsubscriptionchange', function(event) {
  console.log('[Service Worker]: \'pushsubscriptionchange\' event fired.');
});

self.addEventListener("pushsubscriptionchange", event => {
    localStorage.removeItem("pade.vapid.keys"); // force new subscription on next login
    client.postMessage(event.oldSubscription);  // send re-subscribe to web app if running
});

self.addEventListener('notificationclose', function(e) {
    console.debug('Closed notification', e.notification);
});

self.addEventListener('notificationclick', function(event) {
    console.debug('notificationclick', event);

    if (event.action === 'ignore') {
        event.notification.close();
    } else {
        event.waitUntil(
            clients.openWindow(event.notification.data.url)
        );
        event.notification.close();
    }
}, false);