const db = require("../models")
const User = db.user;
const sharp = require('sharp');
const path = require('path');
const { urlPath } = require("../functions/url");

const profile = async (req, res) => {
    const profile = await User.findById(req.userId, "username email image").exec();
    profile.image = `${req.protocol}://${req.hostname}:8080` + urlPath('avatar') + profile.image;
    res.status(200).send(profile);
}

const updateProfile = async (req, res) => {
    try {
        const body = req.body;
        const profile = await User.findById(req.userId, "username email image").exec();
        let nameFile;
        if (req.file) {
            nameFile = req.userId + '-' + Date.now() + '-' + req.file.originalname;
            await sharp(req.file.buffer).png().toFile(path.join(__dirname, '..') + `/images/avatar/${nameFile}`)
        }

        if (body.username) {
            const username = await User.findOne({
                $and: [
                    { _id: { $ne: req.userId } },
                    { username: body.username }
                ]
            }).exec();

            if (username) {
                res.status(400).send({ message: "Failed! Username is already in use!" });
                return;
            }
        }

        if (body.email) {
            const email = await User.findOne({
                $and: [
                    { _id: { $ne: req.userId } },
                    { email: body.email }
                ]
            }).exec();

            if (email) {
                res.status(400).send({ message: "Failed! email is already in use!" });
                return;
            }
        }

        profile.username = body.username || profile.username;
        profile.email = body.email || profile.email;
        profile.image = req.file ? nameFile : profile.image;
        await profile.save();

        profile.image = `${req.protocol}://${req.hostname}:8080` + urlPath('avatar') + profile.image;
        console.log(req);
        res.send({ message: 'Update profile success', profile })
    } catch (error) {
        console.log(error)
        res.status(500).send({ message: "update-profile:unknown error" })
    }
}

const userController = {
    profile,
    updateProfile
};

module.exports = userController;