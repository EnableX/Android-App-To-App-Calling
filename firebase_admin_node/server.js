const express = require("express");
const app = express();
const bodyParser = require("body-parser")


app.use(bodyParser.json())
app.use("/posts",require("./routes"))


app.listen(3001, function () {
    console.log("Server is running on port 3001")
})