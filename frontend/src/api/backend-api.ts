import axios, { AxiosResponse } from 'axios';

const axiosApi = axios.create({
    baseURL: `/api`,
    timeout: 2000,
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
    vacation: boolean;
    reducedWorkingDay: boolean;
    workingMinutes: number;
    additionalInfo: string;
    sequenceNumber: number;
    summary: string;
    worklogs: Worklog[];
}

export interface Task {
    entityId: number;
    id: number;
    code: string;
    shortCode: string;
    type: string;
    title: string;
    examples: string;
    closed: boolean;
}

export interface TaskWithUsage extends Task {
    latestWorklogDateTime: string;
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

export interface TimeDistributionOverviewDto {
    data: TimeDistributionDto[];
}

export interface TimeDistributionDto {
    byTypes: {
        [key: string]: number;
    };
    periodEnd: string;
}

export default {
    heartbeat(): Promise<AxiosResponse<void>> {
        return axiosApi.get('/heartbeat');
    },
    weeks(): Promise<AxiosResponse<DayInfo[]>> {
        return axiosApi.get(`/calendar/weeks`);
    },
    day(date: Date): Promise<AxiosResponse<DayInfo>> {
        return axiosApi.get(`/calendar/day?date=${date}`);
    },
    updateDaySummary(date: string, newText: string): Promise<AxiosResponse<void>> {
        return axiosApi.patch(`/calendar/day/${date}/summary`, { newText });
    },
    updateWorkingMinutesCount(date: string, value: boolean): Promise<AxiosResponse<void>> {
        return axiosApi.patch(`/calendar/day/${date}/minutes/${value}`);
    },
    updateDayNonWorkingStatus(date: string, value: boolean): Promise<AxiosResponse<void>> {
        return axiosApi.patch(`/calendar/day/${date}/non-working`, { value });
    },
    updateDayVacation(date: string, value: boolean): Promise<AxiosResponse<void>> {
        return axiosApi.patch(`/calendar/day/${date}/vacation`, { value });
    },
    searchTasks(query: string): Promise<AxiosResponse<Task[]>> {
        return axiosApi.get(`/tasks/search?query=${query}`);
    },
    tasks(): Promise<AxiosResponse<TaskWithUsage[]>> {
        return axiosApi.get('/tasks');
    },
    upsertTask(body: Task): Promise<AxiosResponse<Task>> {
        return axiosApi.post('/tasks', body);
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

    getTimeDistributionByTypeOfTasksPerWeeks(): Promise<AxiosResponse<TimeDistributionOverviewDto>> {
        return axiosApi.get(`/analytics/time-distribution`);
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
