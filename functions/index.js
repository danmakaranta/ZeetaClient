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
    var ref = database.ref("Abuja/Mechanic");// ref to where we want in the database   
    ref.on('value', retrieveTime); 

    
    function retrieveAllData(data){
      var keys = Object.keys(data.val());
      for(var i=0; i<=keys.length; i++){
        var k = keys[i];
        console.log(keys[i]);
        //console.log(keys.length);
        //console.log(k);
      }      
      
    }

    function retrieveTime(data){
      var date = new Date();
      var timeD = functions.firestore.document('Abuja/xXVO7elEFYdH3wgsBEuMneQTOf83')
      let timeRef = fdb.collection('Abuja').doc('xXVO7elEFYdH3wgsBEuMneQTOf83');
      let getDoc = timeRef.get()
      .then(doc => {
      if (!doc.exists) {
      console.log('No such document!');
      } else {
      //console.log('Document data:', doc.data().timeStamp._seconds);
      //console.log('Current date:', date.getTime);
      var a = date.getTime;
      var b = doc.data().timeStamp._seconds;
      
      var diffr = date.getTime - doc.data().timeStamp._seconds;
      console.log('time difference', diffr);
      }
     return null;
  })
  .catch(err => {
    console.log('Error getting document', err);
  });
    }
  
    return null;
  });  
exports.onMessageCreate = functions.firestore
  .document('Abuja/xXVO7elEFYdH3wgsBEuMneQTOf83')
  .onUpdate((change, context) => {
    
     //console.log(change.after.ref.parent);
     console.log(change.document);
  });

