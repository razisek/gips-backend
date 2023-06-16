const jwt = require("jsonwebtoken");
const config = require("../config/auth.config.js");
const db = require("../models")
const User = db.user;

verifyToken = (req, res, next) => {
    let token = req.headers["x-access-token"];

    if (!token) {
        return res.status(401).send({ message: "Unauthorized!" });
    }

    jwt.verify(token, config.secret, async (err, decoded) => {
        if (err) {
            return res.status(401).send({ message: "Unauthorized!" });
        }

        const profile = await User.findById(decoded.id).exec();
        if (!profile) {
            return res.status(401).send({ message: "Unauthorized!" });
        }
        
        req.userId = decoded.id;
        next();
    });
};

const auth = {
    verifyToken
};
module.exports = auth;