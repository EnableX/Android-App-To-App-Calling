# demo framework for app to app calling with EnableX android toolkit and firebase 

This Application is explaining to send push notification messages from one registered mobile number to another registered mobile number. and by getting a push notification message app will initiate one to one call.

To demonstrate the Android Sample App, We need a middleware server to register mobile numbers and to send push notifications to the mobile application.

For this application, We are using Firebase Cloud Messaging as a middleware server, where this server is storing mobile numbers and send push notifications to the registered mobile.

### Middleware Server
Create push notication NodeJs API's server with [firebase admin sdk](https://firebase.google.com/docs/admin/setup). In this server, you will integrate API's 
1) To register the device with mobile number 
2) To sending push notification to the register mobile number.


### Configure and Build the app

Configure the sample app code with [firebase cloud messageing](https://firebase.google.com/docs/android/setup). Then, build and run the app.
1. The application requires **google-services.json**
2. The Applicaion requires NodeJs Middleware server api for registering device with the mobile number and for sending push notification to device.
3. Push notification contains roomId and calling number.
2. The application **requires** values for **RoomId**.\
4.1 Replace the following empty strings with the corresponding **room_Id** values in `DashBoardActivity`:
```
private String room_Id = "";
```
3. Use Android Studio to build and run the app on an android device.

### Exploring the sample app

4 **Registerd Device**: 

This Application starts with registering the device with a Middleware server. Integrate firebase cloud messaging into an application. After FCM successfully registration, you will get an FCM token. After getting a token, the mobile number should be registered using node js API(Middleware Server). 

![registernumber](./registernumber.png)

**Note:** An incoming native phone call will interrupt the current VoIP call.

**Outgoing call**
Caller call to registered mobile number by using push notification message. A message will be a JSON object which contains
{
	"message":"roomId",
	"receiver_phone_number":"9354401697",
	"caller_phone_number":"3238329829",
	"type":"calling"
}
![calling](./calling.png)
![outgoingcall](./outgoingcall.png)

**Incoming call**
The receiver will get an incoming call, as a push notification message which contains roomId and caller mobile number. The receiver will show the incoming call dialog if the type of message is **calling**.

![incomingcall](./incomingcall.png)

**Receive call**
After joining a room by roomId, the Receiver sends a push notification message to the caller. And that message will be the JSON object which contains.
{
“message”:“answer”,
“receiver_phone_number”:“9354401697”,
“aller_phone_number”:“3238329829”,
“type”:“answer”
}

**Reject call**
If receiver rejected the incoming call then message will be sent to the caller through push notification that message will be JSON object which contains
{
	"message":"reject",
	"receiver_phone_number":"9354401697",
	"aller_phone_number":"3238329829",
	"type":"reject"
}
![conference](./conference.png)

