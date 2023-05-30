const db = require("../models")
const User = db.user;

const checkDuplicateUser = async (req, res, next) => {
    const username = await User.findOne({ username: req.body.username }).exec();
    if (username) {
        res.status(400).send({ message: "Failed! Username is already in use!" });
        return;
    }

    const email = await User.findOne({ email: req.body.email }).exec();
    if (email) {
        res.status(400).send({ message: "Failed! Email is already in use!" });
        return;
    }

    next();
};

const verifySignUp = {
    checkDuplicateUser
};

module.exports = verifySignUp;