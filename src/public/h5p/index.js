function urlParam(name)
{
    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (!results) { return undefined; }
    return unescape(results[1] || undefined);
};

window.addEventListener("unload", function()
{
    var setSetting = function(name, value)
    {
        window.localStorage["store.settings." + name] = JSON.stringify(value);
    }

    setSetting(location.href, {top: window.screenTop, left: window.screenLeft, width: window.outerWidth, height: window.outerHeight});
});

(function($) {
  $(function() {
    var path = urlParam('path');
    document.title = "H5P Viewer - " + path

    $('.h5p-container').h5p({
      frameJs: './dist/js/h5p-standalone-frame.js',
      frameCss: './dist/styles/h5p.css',
      h5pContent: '/' + path
    });
  });
})(H5P.jQuery);

(function () {
    function findSubContentLibrary(id, params)
    {
        for (var prop in params)
        {
          if (!params.hasOwnProperty(prop)) {
            continue;
          }

          if (prop === 'subContentId' && params[prop] === id) {
            return params.library; // Found it
          }
          else if (typeof params[prop] === 'object') {
            // Look in next level
            var result = findSubContentLibrary(id, params[prop]);
            if (result) {
              return result;
            }
          }
        }
    }

  if (window.H5P) {
    H5P.jQuery(window).on('ready', function () {
      subContentIdToLibraryMap = {};

      H5P.externalDispatcher.on('xAPI', function (event) {
        try {
          // First we need to find the category.
          var category;
          var path = urlParam('path');

          // Determine content IDs
          var contentId = event.data.statement.object.definition.extensions['http://h5p.org/x-api/h5p-local-content-id'];
          var subContentId = event.data.statement.object.definition.extensions['http://h5p.org/x-api/h5p-subContentId'];

          if (subContentId) {
            if (subContentIdToLibraryMap[subContentId]) {
              // Fetch from cache
              category = subContentIdToLibraryMap[subContentId];
            }
            else {
              // Find
              category = findSubContentLibrary(subContentId, JSON.parse(H5PIntegration.contents['cid-' + contentId].jsonContent));
              if (!category) {
                return; // No need to continue
                // TODO: Remember that it wasnt found?
              }

              // Remember for next time
              subContentIdToLibraryMap[subContentId] = category;
            }
          }
          else {
            // Use main content library
            category = H5PIntegration.contents['cid-' + contentId].library;
          }

          // Strip version number
          category = category.split(' ', 2)[0];

          // Next we need to determine the action.
          var action = event.getVerb();

          // Now we need to find an unique label

          var label = contentId;

          if (event.data.statement.object.definition.name)
          {
              label = event.data.statement.object.definition.name['en-US']; // Title

              // Add contentID to make it eaiser to find
              label += ' (' + contentId;
              if (subContentId) {
                label += ' ' + subContentId;
              }
              label += ')';
          }

          // Find value
          var value;

          // Use result if possible
          var result = event.data.statement.result;
          if (result && result.score) {
            // Calculate percentage
            value = result.score.raw / ((result.score.max - result.score.min) / 100);
          }

          // ... or slide number
          if (action === 'Progressed') {
            var progress = event.data.statement.object.definition.extensions['http://id.tincanapi.com/extension/ending-point'];
            if (progress) {
              value = progress;
            }
          }

          // Validate number
          value = Number(value);
          if (isNaN(value)) {
            value = undefined;
          }

          //console.log('**xapi**', category, action, label, value);
          window.parent.postMessage({ event: 'ofmeet.event.xapi', id: path, category: category, action: action, value: value}, '*');
        }
        catch (err) {
          // No error handling
          console.error("H5P.externalDispatcher", err);
        }
      });
    });
  }
})();
