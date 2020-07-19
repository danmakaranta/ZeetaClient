const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const fdb = admin.firestore();


//Create and Deploy Your First Cloud Functions
//https://firebase.google.com/docs/functions/write-firebase-functions

var database = admin.database();

exports.helloWorld = functions.https.onRequest((request, response) => {
    
  response.send("Hello from Firebase!");
 });


exports.pushDataEvery60Minute = functions.pubsub.schedule('every 60 minutes').onRun((context) => {
    var date = new Date();
    database.ref("metadata/lastUpdate/").set(date.getTime());
    return null;
  });

exports.deleteOfflineUsersEvery5Minute = functions.pubsub.schedule('every 5 minutes').onRun((context) => {
    var date = new Date();
    var mechRef = database.ref("Abuja/Mechanic");// ref to where we want in the database 
    var taxiRef = database.ref("Abuja/Taxi");
    var plumberRef = database.ref("Abuja/Plumber");  
   
    mechRef.set(null);
    taxiRef.set(null);  
     
  });  
exports.onMessageCreate = functions.firestore
  .document('Abuja/xXVO7elEFYdH3wgsBEuMneQTOf83')
  .onUpdate((change, context) => {    
     //console.log(change.after.ref.parent);
     console.log(change.document);
  });

  exports.onConnectPurchaseRequest = functions.firestore
  .document('ConnectPurchase')
  .onUpdate((change, context) => {    
     //console.log(change.after.ref.parent);
     console.log(change.document);
  });
