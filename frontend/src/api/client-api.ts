import axios, { AxiosResponse, AxiosInstance } from 'axios';

const jiraSettignsKey = "settings:jira";

const savedSettings = localStorage.getItem(jiraSettignsKey);
const { proxyUrl, serverUrl, username, password } = savedSettings && JSON.parse(savedSettings)
    || { proxyUrl: "", serverUrl: "", username: "", password: "" };

const axiosProxyJiraApi = axios.create({
    baseURL: proxyUrl,
    headers: { 'Content-Type': 'application/json', 'X-Jira-Url': serverUrl },
    auth: { username, password }
});

const axiosJiraApi = axios.create({
    baseURL: serverUrl,
    headers: { 'Content-Type': 'application/json' },
    auth: { username, password }
});

interface CreateWorklogRequest {
    timeSpentSeconds: number;
    comment?: string;
    started: string;
}

interface WorklogResponse {
    id: number;
}

interface IssueFieldsDto {
    summary: string;
}

export interface IssueResponse {
    id: number;
    fields: IssueFieldsDto;
}

function getClient(): AxiosInstance {
    if (proxyUrl && proxyUrl.length > 0) {
        return axiosProxyJiraApi;
    }
    return axiosJiraApi;
}

export default {
    getIssue(issueKey: string): Promise<AxiosResponse<IssueResponse>> {
        return getClient().get<IssueResponse>(`/rest/api/2/issue/${issueKey}?fields=summary`);
    },
    createWorklog(issueKey: string, durationInMinutes: number, comment: string, started: string): Promise<AxiosResponse<WorklogResponse>> {
        return getClient().post<WorklogResponse>(`/rest/api/2/issue/${issueKey}/worklog?notifyUsers=false`, {
            timeSpentSeconds: durationInMinutes * 60,
            comment,
            started
        } as CreateWorklogRequest);
    }
}
