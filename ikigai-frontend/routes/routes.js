const express = require('express');
const bcrypt = require('bcrypt');
const saltRounds = 10;

const router = express.Router();

router.route("/").get(
    function (req, res) {
        var model = {
            title: "Ikigai Task Manager",
            username: req.session.username,
            userUuid: req.session.userUuid,
            isAdmin: req.session.isAdmin
        }
        res.render("index", model);
    }
);

module.exports = router;