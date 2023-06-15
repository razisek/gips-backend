const { verifySignUp, authJwt } = require("../middlewares");
const controller = require("../controllers");
const bodyParser = require('body-parser');
const multer = require("multer");

const upload = multer({
    limits: {
        fileSize: 1000000
    },
    fileFilter(req, file, cb) {
        if (!file.originalname.match(/\.(jpg|jpeg|png)$/)) {
            return cb(new Error('Please upload a valid image file'))
        }
        cb(undefined, true)
    }
})

module.exports = function (app) {
    app.use(function (req, res, next) {
        res.header(
            "Access-Control-Allow-Headers",
            "x-access-token, Origin, Content-Type, Accept"
        );
        next();
    });

    app.post(
        "/auth/signup",
        bodyParser.json(),
        [
            verifySignUp.checkDuplicateUser
        ],
        controller.auth.signup
    );

    app.post("/auth/signin", bodyParser.json(), controller.auth.signin);

    app.use('/api', [
        authJwt.verifyToken
    ], (req, res, next) => {
        next()
    })

    app.get("/api/profile", controller.user.profile);
    app.put('/api/profile', upload.single('image'), controller.user.updateProfile)
};