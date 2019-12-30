const router = require("express").Router();
var mongo = require("./mongo");
var firebase = require("./firebase");


router.post("/sendMessage", async (req, res) => {
    try {
        var type = req.body.type;
        if(type == "answer"){
            await mongo.getToken(req.body.localPhonenumber,req.body.phone_number,"answer");
            res.send({
                "message":"answer success",
                "result":"0"
            });
        }if(type == "reject"){
            await mongo.getToken(req.body.localPhonenumber,req.body.phone_number,"reject");
            res.send({
                "message":"rejection success",
                "result":"0"
            });
        }if(type == "not_available"){
            await mongo.getToken(req.body.localPhonenumber,req.body.phone_number,"not_available");            
            res.send({
                "message":"not_available success",
                "result":"0"
            });
        }else{
            await mongo.getToken(req.body.localPhonenumber,req.body.phone_number,req.body.message);
            res.send({
                "message":"id success",
                "result":"0"
            });
        }
        
    } catch (error) {
        res.status(500).send("message did not send")
    }
});


router.post("/registerDevice", async (req, res) => {
    try {
        await mongo.registerDevice(req.body.phone_number, req.body.token);
        res.send("Device is registered successfully")
    } catch (error) {
        res.status(500).send("Device is not registered")
    }
})

module.exports = router;