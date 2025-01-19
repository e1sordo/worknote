const express = require("express");
const axios = require("axios");
const sslRootCAs = require("ssl-root-cas");
const { createLogger, transports, format } = require("winston");

const app = express();

// Логгер для логирования событий
const log = createLogger({
	level: "info",
	format: format.combine(
		format.timestamp(),
		format.printf(
			({ timestamp, level, message }) =>
				`${timestamp} [${level}]: ${message}`,
		),
	),
	transports: [new transports.Console()],
});

// Установка окружения для приема невалидных сертификатов
process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;
sslRootCAs.inject();

const origin = "http://worknote:8098";

app.use(express.json());
app.use(function (req, res, next) {
	res.header("Access-Control-Allow-Origin", origin);
	res.header("Access-Control-Allow-Methods", "OPTIONS, GET, POST");
	res.header(
		"Access-Control-Allow-Headers",
		"Origin, Authorization, X-Jira-Url, X-Requested-With, Content-Type, Accept, Sec-Ch-Ua, Sec-Ch-Ua-Mobile, Sec-Ch-Ua-Platform",
	);
	res.header("Access-Control-Allow-Credentials", "true");
	next();
});

const axiosApi = axios.create({
	timeout: 15000,
	headers: { "Content-Type": "application/json", Accept: "application/json" },
});

const proxyRequest = async (req) => {
	const jiraUrl = req.header("X-Jira-Url");

	const url = req.url;
	const headers = { Authorization: req.header("Authorization") };
	const data = req.body;
	const method = req.method.toLowerCase();

	log.info(`New ${method} request: ${jiraUrl}${url}`);

	try {
		const res = await axiosApi({
			method,
			url: `${jiraUrl}${url}`,
			data,
			headers,
		});
		return res;
	} catch (e) {
		return e.response;
	}
};

app.get("/ping", (req, res) => {
	log.info("PING");
	res.send({ pong: origin });
});

app.all("/rest/api/*", async (req, res) => {
	let resp = await proxyRequest(req);

	if (!resp) {
		log.info("Failed to forward the request");
		res.status(200).end();
		return;
	}

	res.status(resp.status);
	res.header(resp.headers);
	res.send(resp.data);
});

const PORT = 13333;

app.listen(PORT, () => {
	log.info(`Proxy server to Jira is running on port ${PORT}`);
});
