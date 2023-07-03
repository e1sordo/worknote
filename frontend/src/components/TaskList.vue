<template>
    <div>
        <input type="text" v-model="filter" placeholder="Фильтр по типу" />
        <table>
            <thead>
                <tr>
                    <th>Код</th>
                    <th>Краткий код</th>
                    <th>Тип</th>
                    <th>Название</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="task in filteredTasks" :key="task.id">
                    <td>{{ task.code }}</td>
                    <td>{{ task.shortCode }}</td>
                    <td>{{ task.type }}</td>
                    <td>{{ task.title }}</td>
                    <td>
                        <button @click="editTask(task.id)">Редактировать</button>
                        <button @click="deleteTask(task.id)">Удалить</button>
                    </td>
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
import { defineComponent } from 'vue';
import api, { Task } from "../api/backend-api";

export default defineComponent({
    name: 'TaskList',
    data() {
        return {
            tasks: [] as Task[],
            currentPage: 1,
            totalPages: 1,
            pageSize: 10,
            filter: ''
        };
    },
    computed: {
        filteredTasks(): Task[] {
            return this.tasks.filter(task => task.type.includes(this.filter));
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
                const response = await api.tasks('');
                this.tasks = response.data;
                this.totalPages = 1;
                // this.totalPages = response.data.totalPages;
            } catch (error) {
                console.error(error);
            }
        },
        async editTask(taskId: number) {
            // Логика редактирования задачи
            console.log(taskId);
        },
        async deleteTask(taskId: number) {
            // Логика удаления задачи
            console.log(taskId);
        },
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