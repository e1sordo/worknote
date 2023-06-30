<template>
    <div class="container">
        <div class="row">
            <div v-for="worklog in data" :key="worklog.id" class="col-sm-6 col-lg-4">
                <div class="card">
                    <div class="card-body text-start">
                        <div class="dropdown float-end">
                            <a class="text-muted dropdown-toggle font-size-16" href="#" role="button"
                                data-bs-toggle="dropdown" aria-haspopup="true">
                                <i class="bx bx-dots-horizontal-rounded"></i>
                            </a>
                            <div class="dropdown-menu dropdown-menu-end">
                                <a class="dropdown-item" href="#">Action</a>
                                <a class="dropdown-item" @click="submitDelete(worklog)">Delete</a>
                            </div>
                        </div>
                        <div class="d-flex align-items-center">
                            <div>
                                <span class="candidate-list-images" style="font-size: 42px;">
                                    {{ taskTypeIcons.get(worklog.task.type) }}
                                </span>
                            </div>
                            <div class="flex-1 ms-3">
                                <h5 class="font-size-16 mb-1">
                                    <a class="text-dark">{{ worklog.task.title }}</a>
                                </h5>
                                <span class="badge badge-soft-success mb-0">
                                    {{ worklog.task.code }}-{{ worklog.task.id }}
                                </span>
                            </div>
                        </div>
                        <div class="row pt-1 fs-6">
                            <p class="col-sm-4 mb-0 mt-2 font-size-14">
                                <i class="bi bi-clock pe-2 text-primary"></i>
                                {{ worklog.startTime.split(':').slice(0, 2).join(':') }}
                            </p>
                            <p class="col-sm-8 mb-0 mt-2 font-size-14">
                                <i class="bi bi-hourglass-split pe-2 text-primary"></i>
                                {{ worklog.durationInMinutes }} минут ({{ formatTime(worklog.durationInMinutes) }})
                            </p>
                            <p v-if="worklog.synced" class="mb-0 mt-2 font-size-14">
                                <i class="bi bi-patch-check text-success pe-2"></i>
                                {{ worklog.jiraId }}
                            </p>
                            <p v-else class="text-danger mb-0 mt-2 font-size-14">
                                <i class="bi bi-patch-exclamation text-danger pe-2"></i>
                                Не синхронизировано!
                            </p>
                            <p v-if="worklog.summary" class="mb-0 mt-2 font-size-14">
                                <i class="bi bi-chat-left-text pe-2 text-primary"></i>
                                {{ worklog.summary }}
                            </p>
                            <p v-else class="mb-0 mt-2 font-size-14">
                                <i class="bi bi-chat-left pe-2 text-primary"></i>
                                <em class="text-muted">Не заполнено</em>
                            </p>
                        </div>
                        <div class="d-flex gap-2 pt-4" v-if="!worklog.synced">
                            <button @click="submitSync(worklog)" type="button" class="btn btn-primary btn-sm w-100">
                                <i class="bi bi-arrow-repeat me-1"></i>
                                <span>Синхронизировать с Jira</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { formatTime, taskTypeIcons } from '@/constants';
import { defineComponent } from 'vue';
import api from "../api/backend-api";
import * as moment from 'moment';
import 'moment-timezone';
import clientApi from "../api/client-api";

export default defineComponent({
    name: 'WorklogModalList',
    props: {
        data: {
            type: Array,
            required: true,
            default: () => ([])
        },
        date: {
            type: String,
            required: true,
            default: () => ('')
        }
    },
    methods: {
        submitDelete(worklog) {
            api.deleteWorklog(worklog.id)
                .then(() => this.$emit('deleteWorklog', worklog));
        },
        submitSync(worklog) {
            const issueId = `${worklog.task.code}-${worklog.task.id}`;

            // Получаем текущую дату в формате "yyyy-MM-dd"
            const formattedDate = moment(this.date).format('YYYY-MM-DD');

            // Создаем объект Moment с заданной строкой времени и временной зоной Москвы
            const moscowDateTime = moment.tz(`${formattedDate} ${worklog.startTime}`, 'YYYY-MM-DD HH:mm', 'Europe/Moscow');

            // Форматируем объект Moment в строку формата "yyyy-MM-dd'T'HH:mm:ss.nnn+0300"
            const formattedDateTime = moscowDateTime.format('YYYY-MM-DD[T]HH:mm:ss.SSS') + '+0300';

            clientApi.createWorklog(issueId, worklog.durationInMinutes, worklog.summary, formattedDateTime)
                .then(response => response.data.id)
                .then(jiraWorklogId => api.updateWorklogSync(worklog.id, jiraWorklogId)
                    .then(response => this.$emit('worklogSynced', response.data)))
                .catch(error => console.error(error));
        }
    },
    setup() {
        return { formatTime, taskTypeIcons };
    }
});
</script>

<style>
.card {
    margin-bottom: 24px;
    box-shadow: 0 2px 3px #e4e8f0;
    font-family: var(--font-family-sans-serif);
}

.card {
    position: relative;
    display: flex;
    flex-direction: column;
    min-width: 0;
    word-wrap: break-word;
    background-color: #fff;
    background-clip: border-box;
    border: 1px solid #eff0f2;
    border-radius: 1rem;
}

.card-body {
    padding: 16px var(--bs-card-spacer-x);
}

.avatar-md {
    height: 4rem;
    width: 4rem;
}

.rounded-circle {
    border-radius: 50% !important;
}

.img-thumbnail {
    padding: 0.25rem;
    background-color: #f1f3f7;
    border: 1px solid #eff0f2;
    border-radius: 0.75rem;
}

.avatar-title {
    align-items: center;
    background-color: #3b76e1;
    color: #fff;
    display: flex;
    font-weight: 500;
    height: 100%;
    justify-content: center;
    width: 100%;
}

.bg-soft-primary {
    background-color: rgba(59, 118, 225, .25) !important;
}

a {
    text-decoration: none !important;
}

.badge-soft-danger {
    color: #f56e6e !important;
    background-color: rgba(245, 110, 110, .1);
}

.badge-soft-success {
    color: #63ad6f !important;
    background-color: rgba(99, 173, 111, .1);
}

.mb-0 {
    margin-bottom: 0 !important;
}

.badge {
    display: inline-block;
    padding: 0.25em 0.6em;
    font-size: 75%;
    font-weight: 500;
    line-height: 1;
    color: #fff;
    text-align: center;
    white-space: nowrap;
    vertical-align: baseline;
    border-radius: 0.75rem;
}

.font-size-16 {
    font-size: 13px;
}

.font-size-14 {
    font-size: 15px;
}
</style>