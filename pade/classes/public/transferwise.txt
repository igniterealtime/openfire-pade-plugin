function getProfileID() {
  return fetch('https://api.sandbox.transferwise.tech/v1/profiles', {
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0' },
      method: 'GET',
    })
      .then(resp => { return resp.json()})
      .then(r => window.alert("Your ID is " + r[0].id));                 
}  

function submit() {
   var x = document.getElementById("frm1");
   var text = "";
   var sender_id = x.elements[0].value;
   var curr = x.elements[2].value;
   var amt = x.elements[3].value;
  
  var fdata = { 
          "type": "BALANCE"   
         };

  function fund(transfer_id) {
    return fetch('https://api.sandbox.transferwise.tech/v1/transfers/' + transfer_id + '/payments', {
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0'},
        body: JSON.stringify(fdata),
        method: 'POST',
    })
    .then(resp => { return resp.json()})
    .then(r => {console.log(r);
               window.alert("Your transaction is " + r.status + ". Your transfer ID is: " + transfer_id + ".");
               // return fetch('https://api.sandbox.transferwise.tech/v1/simulation/transfers/' + transfer_id + '/processing', {
               //   headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0' },
               //   method: 'GET',
               //  });
               // return fetch('https://api.sandbox.transferwise.tech/v1/simulation/transfers/' + transfer_id + '/funds_converted', { 
               //   headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0' },
               //   method: 'GET',
               //  });
               // return fetch('https://api.sandbox.transferwise.tech/v1/simulation/transfers/' + transfer_id + '/outgoing_payment_sent', {
               //   headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0' },
               //   method: 'GET',
               //  });
               // return fetch('https://api.sandbox.transferwise.tech/v1/transfers/' + transfer_id + '/receipt.pdf', {
               //   headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0' },
               //   method: 'GET',
               //  });
               });
  };
    
      
  function transfer(transfer_data) {
    return fetch('https://api.sandbox.transferwise.tech/v1/transfers', {
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0'},
        body: JSON.stringify(transfer_data),
        method: 'POST',
    })
    .then(resp => { return resp.json()})
    .then(r => {console.log(r);
                var transaction_id = r.customerTransactionId;
                var transfer_id = r.id;
                fund(transfer_id);
               })
  };
  
  function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }
  
  function createEmailRecipient(repdata, quote_id) {
    return fetch('https://api.sandbox.transferwise.tech/v1/accounts', {
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0'},
        body: JSON.stringify(repdata),
        method: 'POST',
    })
    .then(resp => { return resp.json()})
    .then(r => {console.log(r); 
               var user_id = r.user;
               var recep_id = r.id;
               console.log(user_id);
               console.log(recep_id);
               
                var uuid = uuidv4();

                var transfer_data = { 
                  "targetAccount": recep_id,   
                  "quote": quote_id,
                  "customerTransactionId": uuid,
                  "details" : {
                      "reference" : "",
                      "transferPurpose": "",
                      "sourceOfFunds": ""
                    } 
                 };

                transfer(transfer_data);
                
                
               })
  };
  
  
  
  var data = { 
    "profile": sender_id,
    "source": curr,
    "target": "GBP",
    "rateType": "FIXED",
    "sourceAmount": amt,
    "type": "BALANCE_PAYOUT"
  };

  function createQuote() {
    return fetch('https://api.sandbox.transferwise.tech/v1/quotes', {
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer 66ce7417-9631-4628-89c5-e812cbf787e0'},
        body: JSON.stringify(data),
        method: 'POST',
    })
    .then(resp => { return resp.json()})
    .then(r => {console.log(r); 
               var quote_id = r.id;
               console.log("Quote ID: " + quote_id);
                
               var repdata = { 
                "profile": sender_id, 
                "accountHolderName": "Money for Madagascar",
                "currency": "GBP", 
                "type": "email", 
                 "details": { 
                    "email": "admin@moneyformadagascar.org"
                 } 
               };
               
               createEmailRecipient(repdata, quote_id);
                
                
               
               })
  };

  createQuote();

  document.getElementById("msg").innerHTML = "You have donated " + amt + " " + curr + " to Money for Madagascar in GBP (Pound Sterling)***. Please wait until the confirmation alert is displayed before closing the window." + "<br>" + "***We try our best to send money to local charities, but in the case where an area lacks official/trusted charities, we divert funds to global charities/schemes who puts focus on said area." + "</br>" ;
  
}