import axios, { AxiosResponse } from 'axios';

const jiraSettignsKey = "settings:jira";

const savedSettings = localStorage.getItem(jiraSettignsKey);
const { proxyUrl, serverUrl, username, password } = savedSettings && JSON.parse(savedSettings)
    || { proxyUrl: "", serverUrl: "", username: "", password: "" };

const axiosJiraApi = axios.create({
    baseURL: proxyUrl,
    headers: { 'Content-Type': 'application/json', 'X-Jira-Url': serverUrl },
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

export default {
    getIssue(issueKey: string): Promise<AxiosResponse<IssueResponse>> {
        return axiosJiraApi.get<IssueResponse>(`/rest/api/2/issue/${issueKey}?fields=summary`);
    },
    createWorklog(issueKey: string, durationInMinutes: number, comment: string, started: string): Promise<AxiosResponse<WorklogResponse>> {
        return axiosJiraApi.post<WorklogResponse>(`/rest/api/2/issue/${issueKey}/worklog?notifyUsers=false`, {
            timeSpentSeconds: durationInMinutes * 60,
            comment,
            started
        } as CreateWorklogRequest);
    }
}
