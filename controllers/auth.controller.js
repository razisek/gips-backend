const config = require("../config/auth.config");
const db = require("../models");
const User = db.user;

var jwt = require("jsonwebtoken");
var bcrypt = require("bcryptjs");

const signup = async (req, res) => {
    const user = new User({
        username: req.body.username,
        email: req.body.email,
        password: bcrypt.hashSync(req.body.password, 8)
    });

    const saved = await user.save();

    if (saved) {
        res.send({ message: "User was registered successfully!" });
    } else {
        res.status(500).send({ message: "signup:unknown error" });
    }
};

const signin = async (req, res) => {
    const user = await User.findOne({
        $or: [
            { username: req.body.username },
            { email: req.body.username }
        ]
    });

    if (!user) {
        return res.status(401).send({ message: "username or password is incorrect!" });
    } else {
        var passwordIsValid = bcrypt.compareSync(
            req.body.password,
            user.password
        );

        if (!passwordIsValid) {
            return res.status(401).send({
                message: "username or password is incorrect!"
            });
        } else {
            var token = jwt.sign({ id: user.id }, config.secret, {
                expiresIn: 604800 // 1 week
            });

            res.status(200).send({
                id: user._id,
                username: user.username,
                email: user.email,
                accessToken: token
            });
        }
    }
};

const authController = {
    signup,
    signin
};

module.exports = authController;