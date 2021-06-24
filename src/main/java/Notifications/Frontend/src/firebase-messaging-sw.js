importScripts('https://www.gstatic.com/firebasejs/8.6.7/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.6.7/firebase-messaging.js');

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
  