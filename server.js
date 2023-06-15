const express = require('express');
const app = express();
const server = require('http').createServer(app);
const db = require("./models");
const io = require('socket.io')(server);
const port = 8080;
const dbConfig = require("./config/db.config.js");

db.mongoose.connect(`mongodb://${dbConfig.host}:${dbConfig.port}/${dbConfig.db}`, {
    useNewUrlParser: true,
    useUnifiedTopology: true
})
    .then(() => {
        console.log("Successfully connect to MongoDB.");
    })
    .catch(err => {
        console.error("Connection error", err);
        process.exit();
    });

require("./routes/api")(app);
app.get('/', function (req, res) {
    res.json({ message: "Hello world!" });
});
app.use('/static', express.static(__dirname + "/images"));

io.on('connection', (socket) => {
    console.log('user connected');
    socket.on('disconnect', function () {
        console.log('user disconnected');
    });
})

server.listen(port, function () {
    console.log(`Listening on port ${port}`);
});