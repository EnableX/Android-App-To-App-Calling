var admin = require('firebase-admin');

var serviceAccount = require("D:/NodeJs/callkitsample-firebase-adminsdk-ahfrn-29c33c060a.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://callkitsample.firebaseio.com"
});

var payload = {

    // notification: {
    //     title: "RoomId",
    //     body: ""
    // },
    data: {
        localphoneNumber : "",
        remotephoneNumber : "",
        message: ""
    }
};

var options = {
    priority: "high",
    timeToLive: 60 * 60 * 24
};


exports.sendToDevice = function(registrationToken,message,localphoneNumber,remotephoneNumber) {
    // payload.notification.body = message;
    payload.data.localphoneNumber = localphoneNumber;
    payload.data.remotephoneNumber = remotephoneNumber;
    payload.data.message = message;
    console.log("payload:", payload);
    admin.messaging().sendToDevice(registrationToken, payload, options)
        .then(function (response) {
            console.log("Successfully sent message:", response);
        })
        .catch(function (error) {
            console.log("Error sending message:", error);
        });
}