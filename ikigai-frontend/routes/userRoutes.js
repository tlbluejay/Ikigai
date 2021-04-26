const express = require('express');
const bcrypt = require('bcrypt');
const axios = require('axios');
const saltRounds = 10;

const router = express.Router();

router.route("/profile/:userUuid").get(
    async function (req, res) {

        //#region random bee generation
        var names = ["Janna","Karma","Leona","Lulu","Lux","Nami","Senna","Soraka", "Yuumi","Zyra","Ashe","Caitlyn","Jinx","Kai'Sa",
        "Kalista", "Miss Fortune", "Sivir","Tristana","Vayne","Xayah","Elise","Evelynn","Nidalee","Qiyana","Qiyana",
        "Sejuani","Shyvana","Taliyah","Vi","Ahri","Akali","Anivia","Annie","Cassiopeia","Diana","Irelia", "Katarina",
        "Kayle","Leblanc","Lissandra", "Orianna","Syndra","Zoe","Camille","Fiora","Illaoi","Irelia","Poppy", "Quinn",
        "Riven","Seraphine","Rell","Samira", "Lillia",  "Barrynette", "Jackie", "Morgana", "Katherine", "Cindy", "Sona", "Beeko", "Hailie", "Jade", "Hunie" ];
        var buzzyBees = {bees: []};
        for(i = 0; i < 5; i++) {
            bee = {
                name: names[Math.floor(Math.random()*names.length)], 
                distance: "." + Math.floor(Math.random() * 9) + " miles" 
            };
            // console.log(bee);
            buzzyBees.bees.push(JSON.parse(JSON.stringify(bee)));
        }
        //#endregion

        await axios.get('http://ikigai-api.us-west-2.elasticbeanstalk.com/tasks/fetch?uuid=' + req.session.userUuid)
        .then(async response => {
            // console.log(response.data)
            await axios.get('http://ikigai-api.us-west-2.elasticbeanstalk.com/bees/fetch?uuid=' + req.session.userUuid)
            .then(beeResponse => {
                // console.log(beeResponse.data)
                var model = {
                    title: req.session.username + "'s Page",
                    username: req.session.username,
                    userUuid: req.session.userUuid,
                    isAdmin: req.session.isAdmin,
                    bees: buzzyBees.bees,
                    tasks: response.data.data.tasks,
                    hive: beeResponse.data.data.hive
                }
        
                res.render("profile", model);
            })
            .catch(error => { console.log(error); });
        })
        .catch(error => { console.log(error); });
    }
);

router.route("/register").get(
    function (req, res) {
        var model = {
            title: "Register a new Account",
            username: req.session.username,
            userUuid: req.session.userUuid,
            isAdmin: req.session.isAdmin
        }

        res.render("register", model);
    }
);

router.route("/register").post(
    function (req, res) {
        var pass = req.body.password;
        bcrypt.hash(pass, saltRounds, async function (err, hash) {
            var responseBody = {
                name: req.body.name,
                email: req.body.email,
                username: req.body.username,
                password: hash,
                rawAuthorities: [ "ROLE_USER" ],
                tasks: [],
                hive: {
                    UUID: "",
                    nickname: "",
                    owner: "",
                    beeCount: 0,
                    description: ""
                }
            }

            const json = JSON.stringify(responseBody);
            await axios.post('http://ikigai-api.us-west-2.elasticbeanstalk.com/users', json, {
                headers: {
                    'Content-Type' : 'application/json'
                }
            }).then(response => {
                console.log(response.data);
                if (response.data.Message_Header == 'EMAILINUSE') {
                    req.session.username = null;
                    req.session.userUuid = null;
                    req.session.isAdmin = false;

                    var model = {
                        title: "Register a new Account",
                        message: response.data.Message
                    }

                    res.render("register", model);
                } else {
                    req.session.username = response.data.data.username;
                    req.session.userUuid = response.data.data.uuid;
                    req.session.isAdmin = response.data.data.isAdmin;

                    res.redirect("/");
                }
            }).catch(error => {
                console.log(error);
            });
        });
    }
);

router.route("/login").get(
    function (req, res) {
        var model = {
            title: "Login",
            username: req.session.username,
            userUuid: req.session.userUuid,
            isAdmin: req.session.isAdmin
        }

        res.render("login", model);
    }
);

router.route("/login").post(
    async function (req, res) {
        await axios.get('http://ikigai-api.us-west-2.elasticbeanstalk.com/users/fetchUser?username=' + req.body.username)
        .then(response =>  {
            console.log(response.data);
            var valid = false;
            var found = !(response.data.Message_Header == 'NOTFOUND');
            
            if (found) {
                valid = bcrypt.compare(req.body.password, response.data.data.cookie);
            }

            if (valid) {
                req.session.username = response.data.data.username;
                req.session.userUuid = response.data.data.uuid;
                req.session.isAdmin = response.data.data.isAdmin;
                
                res.redirect("/");
            } else {
                req.session.username = null;
                req.session.userUuid = null;
                req.session.isAdmin = false;

                var model = {
                    title: "Login",
                    message: "Login Failed..."
                }

                res.render("login", model);
            }

        }).catch(error => {
            console.log(error);
        });
    }
);

router.route("/logout").get(
    function (req, res) {
        req.session.username = null;
        req.session.userUuid = null;
        req.session.isAdmin = false;

        res.redirect("/");
    }
);

router.route("/createTask").get(
    function (req, res) {
        var model = {
            title: "Add a new task",
            username: req.session.username,
            userUuid: req.session.userUuid,
            isAdmin: req.session.isAdmin
        }

        res.render("createTask", model);
    }
);

router.route("/createTask").post(
    async function (req, res) {
        console.log(req.session.userUuid);
        var responseBody = {
            title: req.body.title,
            content: req.body.content,
            taskOwnerUUID: req.session.userUuid,
            category: req.body.category
        }

        const json = JSON.stringify(responseBody);
        await axios.post('http://ikigai-api.us-west-2.elasticbeanstalk.com/tasks', json, {
            headers: {
                'Content-Type' : 'application/json'
            }
        }).then(async response => {
            if (response.data.Message_Header == "CREATED") {
                res.redirect("/user/profile/:" + req.session.userUuid)
            }

        }).catch(error => { console.log(error); })


    }
);

router.route("/settings/:userUuid").get(
    async function (req, res) {
        await axios.get('http://ikigai-api.us-west-2.elasticbeanstalk.com/users/fetchUpdateDetails?uuid=' + req.session.userUuid)
        .then(async response => {
            // console.log(response.data)
            var model = {
                title: req.session.username + "'s Settings",
                username: req.session.username,
                userUuid: req.session.userUuid,
                isAdmin: req.session.isAdmin,
                name: response.data.data.name,
                email: response.data.data.email
            }
    
            res.render("settings", model);
        })
        .catch(error => { console.log(error); });
    }
);

router.route("/settings/:userUuid").post(
    function (req, res) {
        var pass = req.body.password;
        bcrypt.hash(pass, saltRounds, async function (err, hash) {
            var responseBody = {
                name: req.body.name,
                email: req.body.email,
                username: req.body.username,
                password: hash,
                rawAuthorities: [ "ROLE_USER" ],
                tasks: [],
                hive: {
                    UUID: "",
                    nickname: "",
                    owner: "",
                    beeCount: 0,
                    description: ""
                }
            }

            const json = JSON.stringify(responseBody);
            await axios.post('http://ikigai-api.us-west-2.elasticbeanstalk.com/users', json, {
                headers: {
                    'Content-Type' : 'application/json'
                }
            }).then(response => {
                console.log(response.data);
                if (response.data.Message_Header == 'EMAILINUSE') {
                    req.session.username = null;
                    req.session.userUuid = null;
                    req.session.isAdmin = false;

                    var model = {
                        title: "Register a new Account",
                        message: response.data.Message
                    }

                    res.render("register", model);
                } else {
                    req.session.username = response.data.data.username;
                    req.session.userUuid = response.data.data.uuid;
                    req.session.isAdmin = response.data.data.isAdmin;

                    res.redirect("/");
                }
            }).catch(error => {
                console.log(error);
            });
        });
    }
);

router.route("/complete/:id").post(
    async function (req, res) {
        await axios.get('http://ikigai-api.us-west-2.elasticbeanstalk.com/tasks/complete?id=' + req.body.id)
        .then(async response => {
            if (response.data.Message_Header == "COMPLETED") {

                await axios.get('http://ikigai-api.us-west-2.elasticbeanstalk.com/bees/grow?uuid=' + req.session.userUuid)
                .then(response => {
                    if (response.data.Message_Header == "COMPLETED") {
                        res.redirect("/user/profile/:" + req.session.userUuid)
                    }
                }).catch(error => { console.log(error); });
            }
        }).catch(error => { console.log(error); });
    }
);
module.exports = router;