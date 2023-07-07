package com.gips.feature.drawing

import com.gips.feature.common.BaseResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.*
import kotlin.random.Random

import com.gips.feature.drawing.*


typealias ConnectedClients = MutableMap<String, DefaultWebSocketServerSession>

val gameRooms = mutableMapOf<String, GameRoom>()
var drawingPoint: DrawingPoint? = null

fun Routing.drawingWebsocket() {
    post("/create-room") {
        try {
            val id = uuid()

            val theme = call.request.queryParameters["theme"] ?: ""
            val maxPlayers = call.request.queryParameters["maxPlayer"]?.toInt() ?: 25
            val maxRound = call.request.queryParameters["maxRound"]?.toInt() ?: 5
            val duration = call.request.queryParameters["duration"]?.toInt() ?: 60
            val gameRoom = GameRoom(id = id, currentTheme = theme, maxPlayers = maxPlayers, maxRound = maxRound, duration = duration)
            print("Game Room Created: $gameRoom")

            gameRooms[id] = gameRoom
            gameRooms[id]?.currentAnswer = questionList[GameTheme.fromStringIndex(theme)].getRandomAnimal()

            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.OK.value,
                    message = "Game Created",
                    data = gameRoom,
                )
            )
        } catch (e: Exception) {
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Terjadi Kesalahan!",
                    data = null,
                )
            )
        }

    }

    get("/game") {

        try {
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.OK.value,
                    message = "Game Rooms",
                    data = gameRooms.map {
                        it.value
                    }.toList(),
                )
            )
        } catch (e: Exception) {
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Terjadi Kesalahan!",
                    data = null,
                )
            )
        }

    }

    get("/list-theme") {

        try {
           call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.OK.value,
                    message = "Game Rooms",
                    data = GameTheme.values().map {
                        it.theme
                    }.toList(),
                )
            )
        } catch (e: Exception) {
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Terjadi Kesalahan!",
                    data = null,
                )
            )
        }

    }

    get("/game/{id}") {
        try {
            val id = call.parameters["id"] ?: ""
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.OK.value, message = "Game Rooms", data = gameRooms[id]
                )
            )
        } catch (e: Exception) {
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Terjadi Kesalahan!",
                    data = null,
                )
            )
        }

    }

    post("/game/{id}/join") {
        try {
            val id = call.parameters["id"] ?: ""
            val username = call.request.queryParameters["username"] ?: ""

            if (username.isEmpty()) {
                call.respond(
                    status = HttpStatusCode.OK, message = BaseResponse(
                        status = HttpStatusCode.Forbidden.value,
                        message = "Username tidak boleh kosong",
                        data = null,
                    )
                )
            } else if (!gameRooms.containsKey(id)) {
                call.respond(
                    status = HttpStatusCode.OK, message = BaseResponse<GameRoom>(
                        status = HttpStatusCode.Forbidden.value,
                        message = "Tidak ada ruangan dengan  ID yang dituju",
                        data = null,
                    )
                )
            } else if (gameRooms[id]?.connectedClientsSession?.contains(username) == true) {
                call.respond(
                    status = HttpStatusCode.OK, message = BaseResponse<GameRoom>(
                        status = HttpStatusCode.Forbidden.value,
                        message = "Username telah ada di Room, silahkan gunakan username unik lain",
                        data = null,
                    )
                )
            } else if (gameRooms[id]?.maxPlayers == gameRooms[id]?.connectedClients?.size) {
                call.respond(
                    status = HttpStatusCode.OK, message = BaseResponse<GameRoom>(
                        status = HttpStatusCode.Forbidden.value,
                        message = "Server Penuh!",
                        data = null,
                    )
                )
            } 
            else {
                call.respond(
                    status = HttpStatusCode.OK, message = BaseResponse(
                        status = HttpStatusCode.OK.value,
                        message = "You Can Join the Game",
                        data = gameRooms[id],
                    )
                )
            }
        } catch (e: Exception) {
            call.respond(
                status = HttpStatusCode.OK, message = BaseResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Terjadi Kesalahan!",
                    data = null,
                )
            )
        }
    }

    webSocket("/game/{id}") {
        try {
            val id = call.parameters["id"] ?: ""
            val username = call.request.queryParameters["username"] ?: ""
            val gameRoom = gameRooms[id]

            if (id.isEmpty() || username.isEmpty() || gameRoom == null) return@webSocket

            gameRoom.connectedClientsSession[username] = this
            gameRoom.connectedClients.add(GamePlayer(username = username))

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val decodedJson = Json.decodeFromString<IncomingMessage>(text)
                    when (decodedJson.method) {
                        "join" -> {
                            gameRoom.totalPlayers = gameRoom.connectedClients.size
                            if (gameRoom.isPlaying) {
                                /// just notify all client/player
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"join\",\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            } else {
                                if (gameRoom.currentPlayer == null) {
                                    gameRoom.currentPlayer = gameRoom.connectedClients.first()
                                }
                                ///  notify all client/player
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"join\",\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            }
                        }

                        "start" -> {
                             gameRoom.isPlaying = gameRoom.connectedClients.size > 1
                                if (gameRoom.currentPlayer == null) {
                                    gameRoom.currentPlayer = gameRoom.connectedClients.first()
                                }

                                if (gameRoom.isPlaying){
                                    for (client in gameRoom.connectedClientsSession) {
                                        client.value.outgoing.send(
                                            Frame.Text(
                                                "{\"method\":\"start\",\"game_room\":${
                                                    Json.encodeToString(gameRoom)
                                                }}"
                                            )
                                        )
                                    }
                                }
                        }

                        "drawing" -> {
                            when (decodedJson.type) {
                                "start" -> {
                                    decodedJson.offset?.let {
                                        drawingPoint = DrawingPoint(
                                            offsets = mutableListOf(it)
                                        )
                                        gameRoom.currentDrawingPoint?.add(drawingPoint!!)
                                        gameRoom.historyDrawingPoint = gameRoom.currentDrawingPoint?.toMutableList()
                                    }
                                }

                                "update" -> {
                                    decodedJson.offset?.let {
                                        drawingPoint = drawingPoint?.copy(offsets = drawingPoint?.offsets?.apply {
                                            add(it)
                                        } ?: mutableListOf())

                                        gameRoom.currentDrawingPoint?.removeLast()
                                        gameRoom.currentDrawingPoint?.add(drawingPoint!!)
                                        gameRoom.historyDrawingPoint = gameRoom.currentDrawingPoint?.toMutableList()
                                    }
                                }

                                "end" -> {
                                    drawingPoint = null
                                }

                                "undo" -> {
                                    if (gameRoom.currentDrawingPoint?.isNotEmpty() == true && gameRoom.historyDrawingPoint?.isNotEmpty() == true) {
                                        gameRoom.currentDrawingPoint?.removeLast()
                                    }
                                }

                                "redo" -> {
                                    if ((gameRoom.currentDrawingPoint?.size ?: 0) < (gameRoom.historyDrawingPoint?.size
                                            ?: 0)
                                    ) {
                                        val index = gameRoom.currentDrawingPoint?.size ?: 0
                                        gameRoom.currentDrawingPoint?.add(gameRoom.historyDrawingPoint!![index])
                                    }
                                }
                            }

                            for (client in gameRoom.connectedClientsSession) {
                                if (client.key == username) continue
                                client.value.outgoing.send(Frame.Text(text))
                            }
                        }

                        "answer" -> {
                            val isCorrect =
                                decodedJson.answer?.equals(gameRoom.currentAnswer, ignoreCase = true) ?: false
                            if (isCorrect) {
                                val indexPlayerThatDraw = gameRoom.connectedClients.indexOfFirst { it.username == gameRoom.currentPlayer?.username }
                                val indexPlayer = gameRoom.connectedClients.indexOfFirst { it.username == username }
                                gameRoom.connectedClients[indexPlayerThatDraw].score++
                                gameRoom.connectedClients[indexPlayer].score++
                                gameRoom.connectedClients[indexPlayer].isAnswered = true
                            }
                            val isAllAnswered = isAllAnsweredExcept(gameRoom.connectedClients, gameRoom.currentPlayer)
                            if (isAllAnswered) {
                                gameRoom.historyDrawingPoint = mutableListOf()
                                gameRoom.currentDrawingPoint = mutableListOf()

                                val indexOfCurrentPlayer =
                                    gameRoom.connectedClients.indexOfFirst { it.username == gameRoom.currentPlayer?.username }
                                val isLast = indexOfCurrentPlayer == gameRoom.connectedClients.size - 1
                                if (isLast) gameRoom.currentRound++

                                gameRoom.currentPlayer =
                                    if (isLast) gameRoom.connectedClients[0] else gameRoom.connectedClients[indexOfCurrentPlayer + 1]

                                    for ( client in gameRoom.connectedClients){
                                        client.isAnswered = false
                                    }

                                gameRoom.currentAnswer = questionList[GameTheme.fromStringIndex(gameRoom.currentTheme)].getRandomAnimal()
                            }

                            if (gameRoom.currentRound < gameRoom.maxRound){
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"answer\",\"username\":\"$username\",\"isCorrect\":$isCorrect,\"answer\":${if (isCorrect) "\"Tebakan benar!\"" else "\"${decodedJson.answer}\""},\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            } else {
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"finish\",\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            }
                        }
                        "ticker" ->{
                            gameRoom.currentDuration = decodedJson.duration

                            for (client in gameRoom.connectedClientsSession) {
                                client.value.outgoing.send(
                                    Frame.Text(
                                        "{\"method\":\"ticker\",\"game_room\":${
                                            Json.encodeToString(gameRoom)
                                        }}"
                                    )
                                )
                            }
                        }
                        "timeout" -> {
                            gameRoom.historyDrawingPoint = mutableListOf()
                            gameRoom.currentDrawingPoint = mutableListOf()

                            val indexOfCurrentPlayer =
                                gameRoom.connectedClients.indexOfFirst { it.username == gameRoom.currentPlayer?.username }
                            val isLast = indexOfCurrentPlayer == gameRoom.connectedClients.size - 1
                            if (isLast) gameRoom.currentRound++
                            gameRoom.currentPlayer =
                                if (isLast) gameRoom.connectedClients[0] else gameRoom.connectedClients[indexOfCurrentPlayer + 1]

                            for ( client in gameRoom.connectedClients){
                                client.isAnswered = false
                            }

                            gameRoom.currentAnswer = questionList[GameTheme.fromStringIndex(gameRoom.currentTheme)].getRandomAnimal()

                            if (gameRoom.currentRound < gameRoom.maxRound){
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"timeout\",\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            } else {
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"finish\",\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            }
                        }

                        "disconnect" -> {
                            gameRoom.connectedClients.removeIf { it.username == username }
                            gameRoom.connectedClientsSession.remove(username)
                            gameRoom.totalPlayers = gameRoom.connectedClients.size

                            if (gameRoom.connectedClients.isEmpty()) {
                                gameRooms.remove(id)
                            } else {
                                gameRoom.isPlaying = gameRoom.connectedClients.size > 1

                                if (gameRoom.currentPlayer == null) {
                                    gameRoom.currentPlayer = gameRoom.connectedClients.first()
                                }

                                ///  notify all client/player
                                for (client in gameRoom.connectedClientsSession) {
                                    client.value.outgoing.send(
                                        Frame.Text(
                                            "{\"method\":\"disconnect\",\"game_room\":${
                                                Json.encodeToString(gameRoom)
                                            }}"
                                        )
                                    )
                                }
                            }
                        }
                    }

                }
            }
        } catch (e: Exception) {
            print("TAGGS : $e")
            val errorMessage = Json.encodeToString("{\"error_message\":${e.message ?: "Something went Wrong"}\"}")
            outgoing.send(Frame.Text(errorMessage))
        }

    }
}

fun isAllAnsweredExcept(clients: MutableList<GamePlayer>, currentPlayer: GamePlayer?): Boolean {
    for (client in clients) {
        if (client.username == currentPlayer?.username) continue
        if (!client.isAnswered) return false
    }
    return true
}

fun uuid(): String {
    return "${Random.nextInt().toString(16).substring(1)}-${
        Random.nextInt().toString(16).substring(1)
    }-${System.currentTimeMillis()}"
}

@Serializable
data class IncomingMessage(
    @SerialName("method") val method: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("answer") val answer: String? = null,
    @SerialName("offset") val offset: DrawingOffset? = null,
    @SerialName("duration") val duration: Int? = null,
)

@Serializable
data class GameRoom(
    val id: String = "-1",
    @Transient val connectedClientsSession: ConnectedClients = mutableMapOf(),
    var connectedClients: MutableList<GamePlayer> = mutableListOf(),
    var isPlaying: Boolean = false,
    var currentPlayer: GamePlayer? = null,
    var totalPlayers: Int? = 0,
    var currentDuration: Int? = null,
    var currentRound: Int = 0,
    var maxPlayers: Int = 25,
    var maxRound: Int = 10,
    var duration: Int = 30,
    val currentTheme: String = "",
    var currentAnswer: String? = "",
    var currentDrawingPoint: MutableList<DrawingPoint>? = mutableListOf(),
    @Transient var historyDrawingPoint: MutableList<DrawingPoint>? = mutableListOf(),
)

@Serializable
data class GamePlayer(
    var username: String = "",
    var score: Int = 0,
    var isAnswered: Boolean = false,
)


@Serializable
data class DrawingPoint(
    val offsets: MutableList<DrawingOffset> = mutableListOf(),
)

@Serializable
data class DrawingOffset(
    val dx: Double? = null,
    val dy: Double? = null,
)
