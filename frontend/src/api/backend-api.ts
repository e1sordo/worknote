import axios, { AxiosResponse } from 'axios';

const axiosApi = axios.create({
    baseURL: `/api`,
    timeout: 1000,
    headers: { 'Content-Type': 'application/json' }
});

interface User {
    id: number;
    firstName: string;
    lastName: string;
}

export interface DayInfo {
    date: string;
    nonWorkingDay: boolean;
    reducedWorkingDay: boolean;
    workingMinutes: number;
    additionalInfo: string;
    sequenceNumber: number;
    summary: string;
    worklogs: Worklog[];
}

export interface Task {
    id: number;
    code: string;
    shortCode: string;
    type: string;
    title: string;
}

export interface Worklog {
    id: number;
    startTime: string;
    durationInMinutes: number;
    summary: string;
    task: Task;
    jiraId: number;
    synced: boolean;
}

export default {
    weeks(): Promise<AxiosResponse<DayInfo[]>> {
        return axiosApi.get(`/calendar/weeks`);
    },
    day(date: Date): Promise<AxiosResponse<DayInfo>> {
        return axiosApi.get(`/calendar/day?date=${date}`);
    },
    updateDaySummary(date: string, newText: string): Promise<AxiosResponse<void>> {
        return axiosApi.patch(`/calendar/day/${date}/summary`, { newText });
    },
    tasks(query: string): Promise<AxiosResponse<Task[]>> {
        return axiosApi.get(`/tasks?query=${query}`);
    },
    createWorklog(date: string, time: string, minutes: number, summary: string, task: string): Promise<AxiosResponse<Worklog>> {
        return axiosApi.post(`/worklogs`, {
            date: date,
            startTime: time,
            durationInMinutes: minutes,
            summary: summary,
            taskLabel: task
        });
    },
    updateWorklogSync(id: number, jiraWorklogId: number): Promise<AxiosResponse<void>> {
        return axiosApi.put(`/worklogs/${id}`, { jiraWorklogId });
    },
    deleteWorklog(id: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete(`/worklogs/${id}`);
    },
    getUser(userId: number): Promise<AxiosResponse<User>> {
        return axiosApi.get(`/user/` + userId);
    },
    getSecured(user: string, password: string): Promise<AxiosResponse<string>> {
        return axiosApi.get(`/secured/`, {
            auth: {
                username: user,
                password: password
            }
        });
    }
}
