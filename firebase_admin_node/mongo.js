var MongoClient = require('mongodb').MongoClient,
Format = require('util').format;
var firebase = require("./firebase");

var database;

MongoClient.connect('mongodb://127.0.0.1:27017',{ useNewUrlParser: true } , function(err,db){
  
    // if (err) throw err;
    // var dbo = db.db("vcx_firebase");
    // dbo.createCollection("customers", function(err, res) {
    //   if (err) throw err;
    //   console.log("Collection created!");
    //   db.close();
    // });

    if (err) throw err;
    database = db.db("vcx_firebase");
})
 

exports.registerDevice = function(phoneNumber,token_id){
  var myobj = { phone_number: phoneNumber, token: token_id };
  database.collection("customers").insertOne(myobj, function (err, res) {
      if (err) throw err;
      console.log("document is inserted");
      // database.close();
  });
 }

 exports.getToken = function(localPhoneNumber,phoneNumber,message){

    database.collection("customers").find({ "phone_number": phoneNumber }).toArray(function (err, result) {
        console.log("document is inserted dssdd");
        if (err) throw err;
        if(result.length>0){
        firebase.sendToDevice(result[0]["token"],message,localPhoneNumber,phoneNumber)
        }
    });
 }

 



