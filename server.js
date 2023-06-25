const express = require("express");
const app = express();
const server = require("http").createServer(app);
const db = require("./models");
const port = 8080;
const dbConfig = require("./config/db.config.js");

db.mongoose
	.connect(`mongodb://${dbConfig.host}:${dbConfig.port}/${dbConfig.db}`, {
		useNewUrlParser: true,
		useUnifiedTopology: true,
	})
	.then(() => {
		console.log("Successfully connect to MongoDB.");
	})
	.catch((err) => {
		console.error("Connection error", err);
		process.exit();
	});

require("./socketIo")(server);
require("./routes/api")(app);
app.get("/", function (req, res) {
	res.json({ message: "Hello world!" });
});
app.use("/static", express.static(__dirname + "/images"));

server.listen(port, function () {
	console.log(`Listening on port ${port}`);
});
