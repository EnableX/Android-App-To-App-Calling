### To integrate firebase messaging with API service with node-js.


---
Here In this app we are using node.js service to store required information of token and push notification message. And also notify to other device.(As a push notification)

## Running Locally

Make sure you have [Node.js](http://nodejs.org/)  installed.

Configure the node js API service with [firebase cloud messaging](https://firebase.google.com/docs/admin/setup). To use firebase admin sdk application we need **firebase serviceAccount**.

```sh
git clone the sample project # or clone your own fork
cd firebase_admin_node
npm install
npm start
```

Your app should now be running on [localhost:3001](http://localhost:3001/).

## Deploying

You can also deploy the node.js API service to your own hosting server.

## WorkFlow

In this Application, We will create two API's
1) To register the device with mobile number 
2) To sending push notification message to the registered mobile number.