## Installation

### Windows

https://www.npmjs.com/package/node-windows

### Via Docker

1. `cd ./cors-proxy-service/docker`
2. `docker build -t worknote-jira-cors-proxy .`
3. `docker run -d -p 13333:13333 --name worknote-cors-proxy worknote-jira-cors-proxy`

After successful launch, go to App Settings and fill the `Corporate proxy URL` field
with `http://localhost:13333` or `https://worknote-cors-proxy.orb.local` (if you are using OrbStack).

## Possible problems:

### Chromium did not want to send requests to the local proxy to Jira

__Fix:__
`chrome://flags/#block-insecure-private-network-requests`

__Explanation:__
https://stackoverflow.com/questions/66534759/cors-error-on-request-to-localhost-dev-server-from-remote-site
