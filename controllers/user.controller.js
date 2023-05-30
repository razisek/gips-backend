const db = require("../models")
const User = db.user;

const profile = async (req, res) => {
    const profile = await User.findById(req.userId, "username email").exec();
    res.status(200).send(profile);
}

const userController = {
    profile
};

module.exports = userController;