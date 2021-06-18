  // Your web app's Firebase configuration
  // For Firebase JS SDK v7.20.0 and later, measurementId is optional
  var firebaseConfig = {
    apiKey: "AIzaSyBKtv7DoXc7sJXX_zzxFrwcHg6_hQgxtcY",
    authDomain: "client-7c3f9.firebaseapp.com",
    projectId: "client-7c3f9",
    storageBucket: "client-7c3f9.appspot.com",
    messagingSenderId: "1077667021473",
    appId: "1:1077667021473:web:c6b6c2d76e7647dae1e6e0",
    measurementId: "G-J4JSVJ4XLL"
  };

  // Initialize Firebase
  firebase.initializeApp(firebaseConfig);

  const messaging = firebase.messaging();

  navigator.serviceWorker.register('/firebase-messaging-sw.js')
          .then(function (registration) {
            // Registration was successful
            console.log('firebase-message-sw :ServiceWorker registration successful with scope: ', registration.scope);
            messaging.useServiceWorker(registration);
          }, function (err) {
            // registration failed :(
            console.log('firebase-message-sw: ServiceWorker registration failed: ', err);
          });

  var currentToken;
  messaging.requestPermission().
  then(function(){
    console.log("Have Permission");
    return messaging.getToken({vapidKey:"BD5JydjGtsn8MnsWWOupsPrUo1jLhlJxjV_IiTezld5pEy8UoegJU5k_9fow5MwGi6zWhEaoAKesneofHErfUGc"});
  }).then(function(token){
    currentToken = token;
    console.log(token);
    const json = {
      body:{
      userName: 'abdo',
      token: currentToken
      },
      function:"SetNotificationTokenCommand",
	    queue:"user"
    };
    const options = {
      method: 'POST',
      body: JSON.stringify(json),
      headers: {
          'Content-Type': 'application/json'
      }
    }
    fetch('http://127.0.0.1:8080/', options)
  .then(res => res.json())
  .then(res => console.log(res))
  .catch(err => console.error(err));
  
  })
  .catch(function(err){
    console.log(err)
  })

  messaging.onMessage(function(payload){
    console.log('onMessage' ,payload);
  })

