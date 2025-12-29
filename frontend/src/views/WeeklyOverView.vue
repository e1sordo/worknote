<template>
    <div class="container-fluid">
        <div class="row">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>Weekly Timesheet Overview</h2>
                    <div class="d-flex align-items-center">
                        <button class="btn btn-outline-secondary me-2" @click="previousWeek">
                            <i class="bi bi-chevron-left"></i>
                        </button>
                        <span class="fw-bold">{{ formatWeekRange(currentWeekStart) }}</span>
                        <button class="btn btn-outline-secondary ms-2" @click="nextWeek">
                            <i class="bi bi-chevron-right"></i>
                        </button>
                    </div>
                </div>

                <div class="card">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead class="table-light sticky-top">
                                    <tr>
                                        <th scope="col" style="width: 200px">Project</th>
                                        <th scope="col" style="width: 120px">Task ID</th>
                                        <th scope="col" style="width: 400px">Task Title</th>
                                        <th scope="col" class="text-center" v-for="day in weekDays" :key="day.date">
                                            <div>{{ formatDayHeader(day.date) }}</div>
                                            <small class="text-muted">{{ formatDayDate(day.date) }}</small>
                                        </th>
                                        <th scope="col" class="text-center fw-bold">Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <!-- Project rows -->
                                    <template v-for="project in sortedProjects" :key="project.projectName">
                                        <!-- Project header row -->
                                        <tr class="table-primary">
                                            <td colspan="3" class="fw-bold">
                                                <i class="bi bi-folder2-open me-2"></i>
                                                {{ project.projectName }}
                                            </td>
                                            <td v-for="day in weekDays" :key="day.date" class="text-center">
                                                {{ formatTime(project.dailyTotals[day.date] || 0) }}
                                            </td>
                                            <td class="text-center fw-bold">
                                                {{ formatTime(project.totalMinutes) }}
                                            </td>
                                        </tr>

                                        <!-- Task rows for this project -->
                                        <tr v-for="task in project.tasks" :key="task.taskCode" class="task-row">
                                            <td class="ps-4">
                                                <span class="badge bg-light text-dark">{{ project.projectName }}</span>
                                            </td>
                                            <td>
                                                <span class="badge bg-primary">{{ task.taskCode }}</span>
                                            </td>
                                            <td class="text-start">
                                                <span
                                                    class="text-truncate d-inline-block"
                                                    style="max-width: 380px"
                                                    :title="task.taskTitle"
                                                >
                                                    {{ task.taskTitle }}
                                                </span>
                                            </td>
                                            <td
                                                v-for="day in weekDays"
                                                :key="day.date"
                                                class="text-center"
                                                :class="{ 'day-today': todayStr == day.date }"
                                            >
                                                <span
                                                    v-if="task.dailyMinutes[day.date]"
                                                    class="badge bg-warning text-dark"
                                                >
                                                    {{ formatTime(task.dailyMinutes[day.date]) }}
                                                </span>
                                                <span v-else class="text-muted">-</span>
                                            </td>
                                            <td class="text-center">
                                                <span class="badge bg-success">
                                                    {{ formatTime(task.totalMinutes) }}
                                                </span>
                                            </td>
                                        </tr>
                                    </template>

                                    <!-- Total row -->
                                    <tr class="table-dark">
                                        <td colspan="3" class="fw-bold">
                                            <i class="bi bi-calculator me-2"></i>
                                            Total ({{ totalTasks }} tasks)
                                        </td>
                                        <td v-for="day in weekDays" :key="day.date" class="text-center fw-bold">
                                            {{ formatTime(dailyTotals[day.date] || 0) }}
                                        </td>
                                        <td class="text-center fw-bold">
                                            {{ formatTime(grandTotal) }}
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Loading state -->
                <div v-if="loading" class="text-center py-5">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <div class="mt-2">Loading timesheet data...</div>
                </div>

                <!-- Error state -->
                <div v-if="error" class="alert alert-danger mt-3">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    Error loading timesheet data: {{ error }}
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
    import { ref, onMounted, computed } from 'vue';
    import api, { DayInfo } from '@/api/backend-api';

    interface ProcessedTask {
        taskCode: string;
        taskTitle: string;
        dailyMinutes: Record<string, number>;
        totalMinutes: number;
    }

    interface ProcessedProject {
        projectName: string;
        tasks: ProcessedTask[];
        dailyTotals: Record<string, number>;
        totalMinutes: number;
    }

    // Reactive data
    const loading = ref(false);
    const error = ref<string | null>(null);
    const weekData = ref<DayInfo[]>([]);
    const currentWeekStart = ref(new Date());

    const todayStr = new Date().toISOString().split('T')[0];

    // Computed properties
    const weekDays = computed(() => {
        const days = [];
        const start = new Date(currentWeekStart.value);

        for (let i = 1; i < 8; i++) {
            const day = new Date(start);
            day.setDate(start.getDate() + i);
            days.push({
                date: day.toISOString().split('T')[0],
                dayOfWeek: day.getDay()
            });
        }

        return days;
    });

    const sortedProjects = computed(() => {
        const projectMap = new Map<string, ProcessedProject>();

        // Process all worklogs
        weekData.value.forEach(dayInfo => {
            dayInfo.worklogs.forEach(worklog => {
                const projectName = worklog.task.code.split('-')[0] || 'Unknown';
                const taskCode = worklog.task.code + '-' + worklog.task.id;
                const taskTitle = worklog.task.title;

                // Initialize project if not exists
                if (!projectMap.has(projectName)) {
                    projectMap.set(projectName, {
                        projectName,
                        tasks: [],
                        dailyTotals: {},
                        totalMinutes: 0
                    });
                }

                const project = projectMap.get(projectName)!;

                // Find or create task
                let task = project.tasks.find(t => t.taskCode === taskCode);
                if (!task) {
                    task = {
                        taskCode,
                        taskTitle,
                        dailyMinutes: {},
                        totalMinutes: 0
                    };
                    project.tasks.push(task);
                }

                // Add minutes to task and project
                const dayKey = dayInfo.date;
                task.dailyMinutes[dayKey] = (task.dailyMinutes[dayKey] || 0) + worklog.durationInMinutes;
                task.totalMinutes += worklog.durationInMinutes;

                project.dailyTotals[dayKey] = (project.dailyTotals[dayKey] || 0) + worklog.durationInMinutes;
                project.totalMinutes += worklog.durationInMinutes;
            });
        });

        // Sort projects by name and tasks by task code
        return Array.from(projectMap.values())
            .sort((a, b) => a.projectName.localeCompare(b.projectName))
            .map(project => ({
                ...project,
                tasks: project.tasks.sort((a, b) => a.taskCode.localeCompare(b.taskCode))
            }));
    });

    const dailyTotals = computed(() => {
        const totals: Record<string, number> = {};

        weekData.value.forEach(dayInfo => {
            const dayTotal = dayInfo.worklogs.reduce((sum, worklog) => sum + worklog.durationInMinutes, 0);
            if (dayTotal > 0) {
                totals[dayInfo.date] = dayTotal;
            }
        });

        return totals;
    });

    const grandTotal = computed(() => {
        return Object.values(dailyTotals.value).reduce((sum, minutes) => sum + minutes, 0);
    });

    const totalTasks = computed(() => {
        return sortedProjects.value.reduce((sum, project) => sum + project.tasks.length, 0);
    });

    // Methods
    const formatTime = (minutes: number): string => {
        if (minutes === 0) return '0h';

        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;

        if (mins === 0) {
            return `${hours}h`;
        } else if (mins === 30) {
            return `${hours}.5h`;
        } else {
            return `${hours}h ${mins}m`;
        }
    };

    const formatDayHeader = (date: string): string => {
        const d = new Date(date);
        return d.toLocaleDateString('en-US', { weekday: 'short' });
    };

    const formatDayDate = (date: string): string => {
        const d = new Date(date);
        return d.getDate().toString();
    };

    const formatWeekRange = (startDate: Date): string => {
        const endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 6);

        const startStr = startDate.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric'
        });
        const endStr = endDate.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });

        return `${startStr} - ${endStr}`;
    };

    const getWeekStart = (date: Date): Date => {
        const d = new Date(date);
        const day = d.getDay();
        const diff = d.getDate() - day + (day === 0 ? -6 : 1); // Monday as first day
        d.setDate(diff);
        d.setHours(0, 0, 0, 0);
        return d;
    };

    const loadWeekData = async () => {
        loading.value = true;
        error.value = null;

        try {
            const response = await api.weeks(3);
            const endOfWeek = addWeeksToDate(new Date(currentWeekStart.value), 1);
            weekData.value = response.data
                .filter(wd => new Date(wd.date) >= currentWeekStart.value)
                .filter(wd => new Date(wd.date) < endOfWeek);
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Unknown error occurred';
            console.error('Failed to load week data:', err);
        } finally {
            loading.value = false;
        }
    };

    const addWeeksToDate = (dateObj: Date, numberOfWeeks: number) => {
        dateObj.setDate(dateObj.getDate() + numberOfWeeks * 7);
        return dateObj;
    };

    const previousWeek = () => {
        const newDate = new Date(currentWeekStart.value);
        newDate.setDate(newDate.getDate() - 7);
        currentWeekStart.value = newDate;
        loadWeekData();
    };

    const nextWeek = () => {
        const newDate = new Date(currentWeekStart.value);
        newDate.setDate(newDate.getDate() + 7);
        currentWeekStart.value = newDate;
        loadWeekData();
    };

    // Initialize
    onMounted(() => {
        currentWeekStart.value = getWeekStart(new Date());
        loadWeekData();
    });
</script>

<style scoped>
    .task-row {
        border-left: 3px solid #e9ecef;
    }

    .task-row:hover {
        background-color: #f8f9fa;
    }

    .day-today {
        background-color: rgba(175, 81, 81, 0.1);
    }

    .table th {
        border-top: none;
        vertical-align: middle;
        font-size: 0.875rem;
    }

    .table td {
        vertical-align: middle;
        font-size: 0.875rem;
    }

    .badge {
        font-size: 0.75rem;
    }

    .sticky-top {
        position: sticky;
        top: 0;
        z-index: 1020;
    }

    .text-truncate {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    @media (max-width: 768px) {
        .container-fluid {
            padding-left: 0.5rem;
            padding-right: 0.5rem;
        }

        .table {
            font-size: 0.75rem;
        }

        .badge {
            font-size: 0.65rem;
        }
    }
</style>
