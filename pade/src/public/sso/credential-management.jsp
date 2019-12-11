<html>
<head>
<style>
    html { 
       width:100%; 
       height:100%; 
       background:url(<%= request.getParameter("url") %>image.png) center center no-repeat;
    }
</style>
</head>
<body>
<script>
    document.title = "<%= request.getParameter("label") %>";

    window.addEventListener("load", function()
    {
        if (!navigator.credentials) 
        {
            // no credentials management api, use basic auth for everything
            
            getCredentials();                         
        }
        else {
          navigator.credentials.get({password: true, federated: {providers: [ 'https://accounts.google.com' ]}, mediation: "optional"}).then(function(credential) 
          {
            if (credential) {              
                getCredentials(credential.id, credential.password); // use creds for basic auth
                
            } else {               
                getCredentials();     // prompt for basic auth        
            }
              
          }).catch(function(err){
            postMessage({action: 'pade.management.credential.api', error: JSON.stringify(err)}, location.href)
          });        
        }        
    });
    
    function getCredentials(username, password)
    {
        var headers = {};
        
        if (username && password)
        {
            headers = {"authorization": "Basic " + btoa(username + ":" + password)};
        }
        
        fetch(location.protocol + "//" + location.host + "/dashboard/credentials.jsp", {method: "GET", headers: headers}).then(function(response){ return response.text()}).then(function(json) 
        {  
            var creds = JSON.parse(json);
            
            if (navigator.credentials && !username && !password && creds.username && creds.password)    // save new credentials
            {
                navigator.credentials.create({password: {id: creds.username, password: creds.password}}).then(function(credential) 
                {
                    navigator.credentials.store(credential).then(function()
                    {
                        postMessage({action: 'pade.management.credential.api', type: 'credentials', creds: creds}, location.href);  

                    }).catch(function (err) {  
                        postMessage({action: 'pade.management.credential.api', error: JSON.stringify(err)}, location.href);                      
                    });                 

                }).catch(function (err) {  
                    postMessage({action: 'pade.management.credential.api', error: JSON.stringify(err)}, location.href);                      
                });        
                     
            }
            else  postMessage({action: 'pade.management.credential.api', type: 'basic-auth', creds: creds}, location.href);  

        }).catch(function (err) {
            postMessage({action: 'pade.management.credential.api', error: JSON.stringify(err)}, location.href);  
        });    
    }
        
</script>
</body>
</html>