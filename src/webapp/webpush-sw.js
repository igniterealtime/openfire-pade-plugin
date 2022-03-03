// install trigger for sw - cache index.html

self.addEventListener('install', function(event) {
  var indexPage = new Request('index.html');
  event.waitUntil(
    fetch(indexPage).then(function(response) {
      return caches.open('offline').then(function(cache) {
        return cache.put(indexPage, response);
      });
  }));
});

// activate trigger

self.addEventListener('activate', function (event) {
    console.log('Activated', event);
});


// fetch trigger - serve from cache or fetch from server, cache the file if not previously cached
/*
self.addEventListener('fetch', function(event) {
  if (event.request.method == "GET") event.respondWith(
    fetch(event.request).then(function(response) {
      return caches.open('offline').then(function(cache) {
          try {
            cache.put(event.request, response.clone());
        } catch (e) {};
        return response;
      });
    }).catch(function (error) {
      caches.match(event.request).then(function(resp) {
        return resp;
      });
    })
  );
});
*/

// push trigger

self.addEventListener('push', function (event) {
   const data = event.data.json();
   console.debug('Push message', data);

   const options = {
        body: data.msgBody,
        icon: './icon.png',
        requireInteraction: true,
        persistent: true,
        sticky: true,
        vibrate: [100, 50, 100],
        data: data,
        actions: [
          {action: 'join', title: 'Join', icon: './check-solid.png'},
          {action: 'ignore', title: 'Ignore', icon: './times-solid.png'},
        ]
    };
	
	if (data.msgDate && data.msgDate != "") {
		options.actions = [
          {action: 'accept', title: 'Accept', icon: './check-solid.png'},
          {action: 'reject', title: 'Reject', icon: './times-solid.png'},		
		]
	}
	
    event.waitUntil(
        self.registration.showNotification(data.msgSubject, options)
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

    if (event.action === 'ignore' || event.action === 'reject') {
        event.notification.close();
		
	} else if (event.action === 'accept') {
		const source = new BroadcastChannel('ofmeet-notification-event');	
		source.postMessage({action: event.action, payload: event.notification.data});	
	
    } else if (event.action === 'join') {
        event.waitUntil(
            clients.openWindow(event.notification.data.url)
        );
        event.notification.close();
    }
}, false);