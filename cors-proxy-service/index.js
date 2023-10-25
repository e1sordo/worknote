const express = require('express');
const axios = require('axios');
const app = express();
const { Service, EventLogger } = require('node-windows');

const log = new EventLogger('Worknote Jira Proxy');

process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = 0

var sslRootCAs = require('ssl-root-cas')
sslRootCAs.inject()

const origin = 'http://worknote:8098';

app.use(express.json());
app.use(function(req, res, next) {
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

app.get('/ping', (req, res) => {
    log.info('PING');
    res.send({pong: origin});
});

app.post('/worklogs/create', async (req, res) => {
  try {
    const issueId = req.query.issue;
    const jiraUrl = req.header('X-Jira-Url');
    log.info('New request. ' + jiraUrl + ' ' + issueId + ' ' + req.body);
    const response = await axiosApi.post(`${jiraUrl}/rest/api/2/issue/${issueId}/worklog?notifyUsers=false`, req.body, {
        headers: { 'Authorization': req.header('Authorization') }
    });
    log.info('Response: ' + response.status);
    res.send(response.data);
  } catch (error) {
    log.error(error.response.data);
    res.status(error.response.status).send(error.response.data);
  }
});

app.listen(13333, () => {
  log.info('The proxy server to Jira Server is running on port 13333');
});

const svc = new Service({
    name: 'Worknote Jira Proxy',
    description: 'Proxy to connect to Jira Server without CORS issue',
    script: 'C:\\cors-proxy-service\\index.js'
});

svc.on('install', function() {
    svc.start();
});

svc.on('uninstall',function(){
  console.log('Uninstall complete.');
  console.log('The service exists: ', svc.exists);
});

// Uninstall the service.
//svc.uninstall();

svc.install();
