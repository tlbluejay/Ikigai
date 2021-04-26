const express = require("express");
const session = require("express-session");
var port = process.env.PORT || 1024;
const app = express();

app.use(session({
    secret: 'I still dont know what this is for.',
    cookie: {}
}));

app.set("view engine", "pug");
app.use(express.static(__dirname + "/public"));

app.use(express.urlencoded({extended: true}));

var routes = require('./routes/routes');
app.use("/", routes);

var userRoutes = require('./routes/userRoutes');
app.use("/user/", userRoutes);

var beeRoutes = require('./routes/hiveRoutes');
app.use("/bees/", beeRoutes);

app.listen(port, function () {
    console.log("Express started and listening on port: " + port);
});