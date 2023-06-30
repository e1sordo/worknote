import axios, { AxiosResponse } from 'axios';

const jiraSettignsKey = "settings:jira";

const savedSettings = localStorage.getItem(jiraSettignsKey);
const { proxyUrl, serverUrl, username, password } = savedSettings && JSON.parse(savedSettings);

const axiosJiraApi = axios.create({
    baseURL: proxyUrl,
    timeout: 3000,
    headers: { 'Content-Type': 'application/json', 'X-Jira-Url': serverUrl },
    auth: { username, password }
});

interface User {
    id: number;
    firstName: string;
    lastName: string;
}

interface CreateWorklogRequest {
    timeSpentSeconds: number;
    comment?: string;
    started: string;
}

interface WorklogResponse {
    id: number;
}

export default {
    createWorklog(issueKey: string, durationInMinutes: number, comment: string, started: string): Promise<AxiosResponse<WorklogResponse>> {
        return axiosJiraApi.post<WorklogResponse>(`/worklogs/create?issue=${issueKey}`, {
            timeSpentSeconds: durationInMinutes * 60,
            comment,
            started
        } as CreateWorklogRequest);
    }
}
