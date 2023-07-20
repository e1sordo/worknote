<template>
    <div>
        <task-form :task="editedTask" @on-submit="upsertTask" class="mb-3" />
        <input type="text" v-model="filter" placeholder="Фильтр" />
        <table class="table table-striped table-hover table-bordered align-middle">
            <thead>
                <tr>
                    <th scope="row">Project</th>
                    <th scope="row">ID</th>
                    <th>Название</th>
                    <th>Была в работе</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="task in filteredTasks" :key="task.entityId">
                    <th scope="row">{{ task.code }}</th>
                    <th scope="row">{{ task.id }}</th>
                    <td>
                        {{ taskTypeMeta[task.type].icon }}
                        <del v-if="task.closed" class="text-muted">{{ task.title }}</del>
                        <strong v-else>{{ task.title }}</strong>
                        <button type="button" class="btn btn-light btn-sm mx-2" @click="editTask(task)">
                            Edit
                        </button>
                        <button type="button" class="btn btn-light btn-sm" @click="openCloseTask(task)">
                            <span v-if="task.closed">Open</span>
                            <span v-else>Close</span>
                        </button>
                    </td>
                    <td>{{ task.latestWorklogDateTime }}</td>
                </tr>
            </tbody>
        </table>
        <div>
            <button :disabled="currentPage === 1" @click="prevPage">Предыдущая страница</button>
            <span>Страница {{ currentPage }} из {{ totalPages }}</span>
            <button :disabled="currentPage === totalPages" @click="nextPage">Следующая страница</button>
        </div>
    </div>
</template>
  
<script lang="ts">
import TaskForm from '@/components/TaskForm.vue';
import { taskTypeMeta } from '@/constants';
import { defineComponent } from 'vue';
import api, { Task, TaskWithUsage } from "@/api/backend-api";

export default defineComponent({
    name: 'TaskTable',
    components: {
        TaskForm
    },
    data() {
        return {
            tasks: [] as TaskWithUsage[],
            currentPage: 1,
            totalPages: 1,
            pageSize: 10,
            filter: '',
            editedTask: {} as Task,
            taskTypeMeta
        };
    },
    computed: {
        filteredTasks(): TaskWithUsage[] {
            return this.tasks.filter(task => (task.title + task.id + task.type).includes(this.filter));
        }
    },
    methods: {
        async fetchTasks() {
            try {
                // {
                //     params: {
                //         page: this.currentPage,
                //         pageSize: this.pageSize
                //     }
                // }
                const response = await api.tasks();
                this.tasks = response.data;
                this.totalPages = 1;
                // this.totalPages = response.data.totalPages;
            } catch (error) {
                console.error(error);
            }
        },
        editTask(task: Task) {
            console.log("Edit task ", task.id);
            this.editedTask = { ...task };
        },
        openCloseTask(task: Task) {
            console.log("Open/close task ", task.id);
            task.closed = !task.closed;
            this.upsertTask(task);
        },
        upsertTask(taskForm: Task) {
            api.upsertTask(taskForm).then(response => {
                // this.$emit('updateDaySummary', this.dayInfo.date, this.text);
                console.log("Task was upserted. Response status: " + response.status);
                this.editedTask = {} as Task;

                const task = response.data;

                var foundIndex = this.tasks.findIndex(item => item.entityId == task.entityId);

                if (foundIndex == -1) {
                    this.tasks = [{ ...task } as TaskWithUsage, ...this.tasks];
                }
                // else {
                // const foundTask = this.tasks[foundIndex];
                // }
            });
        },
        // async deleteTask(taskId: number) {
        //     // Логика удаления задачи
        //     console.log(taskId);
        // },
        prevPage() {
            if (this.currentPage > 1) {
                this.currentPage--;
                this.fetchTasks();
            }
        },
        nextPage() {
            if (this.currentPage < this.totalPages) {
                this.currentPage++;
                this.fetchTasks();
            }
        }
    },
    mounted() {
        this.fetchTasks();
    }
});
</script>