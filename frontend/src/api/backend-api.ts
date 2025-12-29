import axios, { AxiosResponse } from 'axios';

const axiosApi = axios.create({
    baseURL: `/api`,
    timeout: 2000,
    headers: { 'Content-Type': 'application/json' }
});

export interface KeyValueDto {
    key: string;
    value: string;
}

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
    summary: string;
    worklogs: Worklog[];
}

export interface Project {
    code: string;
    name: string;
    shortCode: string;
    active: boolean;
}

export interface Task {
    entityId: number;
    id: number;
    code: string;
    shortCode: string;
    type: string;
    title: string;
    defaultValue: string;
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

export interface Vacation {
    id: number;
    totalDays: number;
    startDate: string;
    endDate: string;
    confirmed: boolean;
    synced: boolean;
}

export default {
    heartbeat(): Promise<AxiosResponse<void>> {
        return axiosApi.get('/heartbeat');
    },

    // app settings
    getSetting(key: string): Promise<AxiosResponse<KeyValueDto>> {
        return axiosApi.get(`/app/settings/${key}`);
    },
    upsertSetting(key: string, value: string): Promise<AxiosResponse<void>> {
        return axiosApi.put(`/app/settings/${key}`, { value });
    },

    // calendar
    weeks(needsToLoad: number): Promise<AxiosResponse<DayInfo[]>> {
        return axiosApi.get(`/calendar/weeks?needsToLoad=${needsToLoad}`);
    },
    futureWeeks(needsToLoad: number): Promise<AxiosResponse<DayInfo[]>> {
        return axiosApi.get(`/calendar/future-weeks?needsToLoad=${needsToLoad}`);
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

    // projects
    projects(): Promise<AxiosResponse<Project[]>> {
        return axiosApi.get('/projects');
    },
    upsertProject(body: Project): Promise<AxiosResponse<Project>> {
        return axiosApi.post('/projects', body);
    },

    // tasks
    searchTasks(query: string): Promise<AxiosResponse<Task[]>> {
        return axiosApi.get(`/tasks/search?query=${query}`);
    },
    getTaskByCode(code: string): Promise<AxiosResponse<Task>> {
        return axiosApi.get(`/tasks/${code}`);
    },
    tasks(): Promise<AxiosResponse<TaskWithUsage[]>> {
        return axiosApi.get('/tasks');
    },
    activeTasks(): Promise<AxiosResponse<TaskWithUsage[]>> {
        return axiosApi.get('/tasks/active');
    },
    upsertTask(body: Task): Promise<AxiosResponse<Task>> {
        return axiosApi.post('/tasks', body);
    },
    activeProjectCode(): Promise<AxiosResponse<String>> {
        return axiosApi.get('/tasks/active-project-code');
    },

    // worklogs
    createWorklog(
        date: string,
        time: string,
        minutes: number,
        summary: string,
        task: string
    ): Promise<AxiosResponse<Worklog>> {
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

    // analytics
    getTimeDistributionByTypeOfTasksPerWeeks(): Promise<AxiosResponse<TimeDistributionOverviewDto>> {
        return axiosApi.get(`/analytics/time-distribution`);
    },

    // vacations
    vacations(): Promise<AxiosResponse<Vacation[]>> {
        return axiosApi.get('/vacations');
    },
    nextVacation(): Promise<AxiosResponse<Vacation>> {
        return axiosApi.get('/vacations/next');
    },
    upsertVacation(body: Vacation): Promise<AxiosResponse<Vacation>> {
        return axiosApi.post('/vacations', body);
    },
    deleteVacation(id: number): Promise<AxiosResponse<void>> {
        return axiosApi.delete(`/vacations/${id}`);
    },

    // security
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
};
