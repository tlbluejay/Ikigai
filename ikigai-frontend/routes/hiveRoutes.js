const express = require('express');
const axios = require('axios');
const { response } = require('express');

const router = express.Router();

router.route("/change/:userUuid").post(
    async function (req, res) {
        console.log(req.session.userUuid);
        console.log(req.body.nickname);
        if (req.body.nickname.length > 0) {
            var responseBody = {
                nickname: req.body.nickname
            }

            console.log(responseBody);
            const json = JSON.stringify(responseBody);
            await axios.post('http://ikigai-api.us-west-2.elasticbeanstalk.com/bees/changeName?uuid=' + req.session.userUuid , responseBody, {
                headers: {
                    'Content-Type' : 'application/json'
                }
            }).then(async response => {
                if (response.data.Message_Header == "OK") {
                    res.redirect("/user/profile/:" + req.session.userUuid)
                }
            }).catch(error => { console.log(error); })

        }
    }
);


module.exports = router;
