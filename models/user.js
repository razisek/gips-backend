const mongoose = require("mongoose");

const User = new mongoose.Schema({
    username: String,
    email: String,
    password: String,
    image: String,
});

module.exports = mongoose.model("User", User);