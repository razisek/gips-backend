const socketioJwt = require("socketio-jwt");
const db = require("../models");

module.exports = (server) => {
	const io = require("socket.io")(server);
	const config = require("../config/auth.config.js");

	io.use(
		socketioJwt.authorize({
			secret: config.secret,
			handshake: true,
			auth_header_required: true,
		})
	);
	io.on("connection", async (socket) => {
		console.log("user connected");
		console.log("hello!", socket.decoded_token.id);
		let userId = socket.decoded_token.id;
		let { username } = await db.user.findById(userId, "username").exec();
		let rooms = [];

		// Add a user to a room
		socket.on("join_room", (data) => {
			const { room } = data;
			console.log("join_room", `${username} has joined the chat room`, room);

			// check if room already exist
			let roomExist = rooms.find((item) => item.room === room);
			if (!roomExist) {
				rooms.push({ room, users: [] });
			}

			// check if user already exist
			let userExist = roomExist?.users.find((item) => item.userId === userId);
			if (!userExist) {
				roomExist?.users.push({ userId, username, socketId: socket.id });
			}

			let __createdtime__ = Date.now();
			socket.to(room).emit("receive_message", {
				message: `${username} has joined the chat room`,
				username: "Bot",
				__createdtime__,
			});
		});

		socket.on("send_message", (data) => {
			let socketId = socket.id;

			const { room, message } = data;

			let __createdtime__ = Date.now();
			socket.to(room).emit("receive_message", {
				message,
				username,
				__createdtime__,
			});
		});

		socket.on("leave", (room) => {
			console.log("leave", room);
			socket.leave(room);
		});

		socket.on("disconnect", function () {
			console.log("user disconnected");
		});
	});

	io.on("unauthorized", function (error, socket) {
		console.log("unauthorized: ", error.data);
		if (
			error.data.type == "UnauthorizedError" ||
			error.data.code == "invalid_token"
		) {
			console.log("User's token has expired");
		}
	});
};
