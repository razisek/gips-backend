const mongoose = require("mongoose");

const User = new mongoose.Schema({
    username: String,
    email: String,
    password: String,
});

module.exports = mongoose.model("User", User);