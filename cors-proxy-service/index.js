const express = require('express');
const axios = require('axios');
const app = express();
const { Service, EventLogger } = require('node-windows');

const log = new EventLogger('Worknote Jira Proxy');

//need to be set to accept certs from secure clusters when certs cant be trusted
//this is mainly for SFRP clusters to test against.
process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = 0

var sslRootCAs = require('ssl-root-cas')
sslRootCAs.inject()

const origin = 'http://worknote:8098';

app.use(express.json());
app.use(function (req, res, next) {
    res.header('Access-Control-Allow-Origin', origin);
    res.header('Access-Control-Allow-Methods', 'OPTIONS, GET, POST');
    res.header('Access-Control-Allow-Headers', 'Origin, Authorization, X-Jira-Url, X-Requested-With, Content-Type, Accept, Sec-Ch-Ua, Sec-Ch-Ua-Mobile, Sec-Ch-Ua-Platform');
    res.header('Access-Control-Allow-Credentials', 'true');
    next();
});

const axiosApi = axios.create({
    timeout: 15000,
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
});

const proxyRequest = async (req) => {
    const jiraUrl = req.header('X-Jira-Url');

    const url = req.url;
    const headers = { 'Authorization': req.header('Authorization') };
    const data = req.body;
    const method = req.method.toLowerCase();

    log.info(`New ${method} request. ` + jiraUrl + ' ' + url + ' ' + data);

    let conf = {
        method,
        url: `${jiraUrl}${url}`,
        data,
        headers
    }

    try {
        res = await axiosApi(conf)
        return res;
    }
    //handle axios throwing an error(like 400 level issues) which should just be passed through
    catch (e) {
        return e.response;
    }
}

app.get('/ping', (req, res) => {
    log.info('PING');
    res.send({ pong: origin });
});

app.all('/rest/api/*', async (req, res) => {
    let resp = await proxyRequest(req);

    if (!resp) {
        console.log("failed to forward the request")
        res.status(200).end();
        return;
    }

    res.status(resp.status);
    res.header(resp.headers);
    res.send(resp.data);
});

app.listen(13333, () => {
    log.info('Proxy server to Jira is running on port 13333');
});

const svc = new Service({
    name: 'Worknote Jira Proxy',
    description: 'Proxy to connect to Jira Server without CORS issue',
    script: 'C:\\cors-proxy-service\\index.js'
});

svc.on('install', function () {
    svc.start();
});

svc.on('uninstall', function () {
    console.log('Uninstall complete.');
    console.log('The service exists: ', svc.exists);
});

// Uninstall the service.
//svc.uninstall();

svc.install();
