<template>
    <div class="container">
        <div class="text-end my-2" v-if="unsyncedWorklogs.length > 0">
            <button
                class="btn btn-success"
                @click="syncAll"
                :disabled="unsyncedWorklogs.length === 0 || syncingIds.size > 0"
            >
                <i class="bi bi-cloud-upload me-1"></i>
                Sync all
            </button>
        </div>
        <div class="row">
            <div v-for="worklog in data" :key="worklog.id" class="col-sm-6 col-lg-4 my-3">
                <div class="card" :class="{ 'border-warning': !worklog.synced }">
                    <div class="card-header text-start d-flex align-items-center">
                        <div class="d-flex align-items-center w-100">
                            <div
                                class="candidate-list-images"
                                :style="{
                                    backgroundColor: taskTypeMeta[worklog.task.type].bgColor
                                }"
                            >
                                <span>{{ taskTypeMeta[worklog.task.type].icon }}</span>
                            </div>
                            <div class="flex-1 ms-3">
                                <span
                                    class="badge mb-0"
                                    :style="{
                                        backgroundColor: taskTypeMeta[worklog.task.type].bgColor,
                                        color: taskTypeMeta[worklog.task.type].textColor
                                    }"
                                >
                                    {{ worklog.task.code }}-
                                    <strong>{{ worklog.task.id }}</strong>
                                </span>
                                <h5 class="max-lines font-size-16 mb-1" :title="worklog.task.title">
                                    <a class="text-secondary">{{ worklog.task.title }}</a>
                                </h5>
                            </div>
                        </div>
                        <div class="dropdown float-end">
                            <a
                                class="text-muted dropdown-toggle font-size-16"
                                href="#"
                                role="button"
                                data-bs-toggle="dropdown"
                                aria-haspopup="true"
                            >
                                <i class="bx bx-dots-horizontal-rounded"></i>
                            </a>
                            <div class="dropdown-menu dropdown-menu-end">
                                <a class="dropdown-item" href="#">Action</a>
                                <a class="dropdown-item" @click="submitDelete(worklog)">Delete</a>
                            </div>
                        </div>
                    </div>
                    <div class="card-body text-start">
                        <div class="row pt-1 fs-6">
                            <p class="col-sm-5 my-1">
                                <i class="bi bi-clock pe-2 text-primary"></i>
                                {{ worklog.startTime.split(':').slice(0, 2).join(':') }}
                            </p>
                            <p class="col-sm-7 my-1">
                                <i class="bi bi-hourglass-split pe-2 text-primary"></i>
                                {{ formatTime(worklog.durationInMinutes) }}
                            </p>
                            <p v-if="worklog.synced" class="my-1">
                                <i class="bi bi-patch-check text-success pe-2"></i>
                                {{ worklog.jiraId }}
                            </p>
                            <p v-else class="text-danger my-1">
                                <i class="bi bi-patch-exclamation text-danger pe-2"></i>
                                {{ $t('worklog.modalItem.notSynced') }}
                            </p>
                            <p class="my-1">
                                <i
                                    class="bi pe-2 text-primary"
                                    :class="{
                                        'bi-chat-left-text': worklog.summary,
                                        'bi-chat-left': !worklog.summary
                                    }"
                                ></i>
                                {{ worklog.summary }}
                                <em v-if="!worklog.summary" class="text-muted">
                                    {{ $t('worklog.modalItem.summaryPlaceholder') }}
                                </em>
                            </p>
                        </div>
                        <div class="d-flex gap-2 pt-4" v-if="!worklog.synced">
                            <button
                                @click="submitSync(worklog)"
                                type="button"
                                class="btn btn-primary btn-sm w-100"
                                :disabled="syncingIds.has(worklog.id)"
                            >
                                <i
                                    class="bi"
                                    :class="
                                        syncingIds.has(worklog.id)
                                            ? 'bi-arrow-repeat me-1 spinner-border spinner-border-sm'
                                            : 'bi-arrow-repeat me-1'
                                    "
                                ></i>
                                <span>
                                    {{ $t('worklog.modalItem.syncButton') }}
                                </span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent, ref, computed } from 'vue';
    import moment from 'moment-timezone';
    import api from '../api/backend-api';
    import clientApi from '../api/client-api';
    import { formatTime, taskTypeMeta } from '@/constants';

    export default defineComponent({
        name: 'WorklogModalList',
        props: {
            data: {
                type: Array,
                required: true,
                default: () => []
            },
            date: {
                type: String,
                required: true,
                default: () => ''
            }
        },
        setup(props, { emit }) {
            const syncingIds = ref<Set<number>>(new Set());

            const unsyncedWorklogs = computed(() =>
                Array.isArray(props.data) ? props.data.filter((w: any) => !w.synced) : []
            );

            const submitDelete = (worklog: any) => {
                api.deleteWorklog(worklog.id).then(() => emit('deleteWorklog', worklog));
            };

            const submitSync = async (worklog: any) => {
                if (syncingIds.value.has(worklog.id)) return;

                syncingIds.value.add(worklog.id);

                try {
                    const issueId = `${worklog.task.code}-${worklog.task.id}`;
                    const formattedDate = moment(props.date).format('YYYY-MM-DD');
                    const moscowDateTime = moment.tz(
                        `${formattedDate} ${worklog.startTime}`,
                        'YYYY-MM-DD HH:mm',
                        'Europe/Moscow'
                    );
                    const formattedDateTime = moscowDateTime.format('YYYY-MM-DD[T]HH:mm:ss.SSS') + '+0300';

                    // await new Promise(resolve => setTimeout(resolve, 5000));

                    const jiraWorklogId = await clientApi
                        .createWorklog(issueId, worklog.durationInMinutes, worklog.summary, formattedDateTime)
                        .then(response => response.data.id);

                    const updatedWorklog = await api.updateWorklogSync(worklog.id, jiraWorklogId);
                    emit('worklogSynced', updatedWorklog.data);
                } catch (err) {
                    console.error(err);
                } finally {
                    syncingIds.value.delete(worklog.id);
                }
            };

            const syncAll = () => {
                unsyncedWorklogs.value.forEach((worklog: any) => {
                    submitSync(worklog);
                });
            };

            return {
                syncingIds,
                submitSync,
                syncAll,
                submitDelete,
                formatTime,
                taskTypeMeta,
                unsyncedWorklogs
            };
        }
        // props: {
        //     data: {
        //         type: Array,
        //         required: true,
        //         default: () => []
        //     },
        //     date: {
        //         type: String,
        //         required: true,
        //         default: () => ''
        //     }
        // },
        // methods: {
        //     submitDelete(worklog) {
        //         api.deleteWorklog(worklog.id).then(() => this.$emit('deleteWorklog', worklog));
        //     },
        //     submitSync(worklog) {
        //         this.worklodUnderSyncNow = worklog.id;

        //         const issueId = `${worklog.task.code}-${worklog.task.id}`;

        //         // Получаем текущую дату в формате "yyyy-MM-dd"
        //         const formattedDate = moment(this.date).format('YYYY-MM-DD');

        //         // Создаем объект Moment с заданной строкой времени и временной зоной Москвы
        //         const moscowDateTime = moment.tz(
        //             `${formattedDate} ${worklog.startTime}`,
        //             'YYYY-MM-DD HH:mm',
        //             'Europe/Moscow'
        //         );

        //         // Форматируем объект Moment в строку формата "yyyy-MM-dd'T'HH:mm:ss.nnn+0300"
        //         const formattedDateTime = moscowDateTime.format('YYYY-MM-DD[T]HH:mm:ss.SSS') + '+0300';

        //         clientApi
        //             .createWorklog(issueId, worklog.durationInMinutes, worklog.summary, formattedDateTime)
        //             .then(response => response.data.id)
        //             .then(jiraWorklogId =>
        //                 api
        //                     .updateWorklogSync(worklog.id, jiraWorklogId)
        //                     .then(response => this.$emit('worklogSynced', response.data))
        //             )
        //             .catch(error => console.error(error))
        //             .finally(() => (this.worklodUnderSyncNow = 0));
        //     }
        // },
        // setup() {
        //     const worklodUnderSyncNow = ref(0);
        //     return { worklodUnderSyncNow, formatTime, taskTypeMeta };
        // }
    });
</script>

<style>
    .card {
        margin-bottom: 24px;
        box-shadow: 0 2px 3px #7b7d821f;
        font-family: var(--font-family-sans-serif);
    }

    .card {
        position: relative;
        display: flex;
        flex-direction: column;
        min-width: 0;
        word-wrap: break-word;
        background-clip: border-box;
        border: 1px solid #8b8d8f80;
        border-radius: 1rem;
        margin-bottom: 0;
        height: 100%;
    }

    .card-header {
        height: 82px;
    }

    .card-body {
        padding: 16px var(--bs-card-spacer-x);
        padding-top: 0;
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

    a {
        text-decoration: none !important;
    }

    .bg-soft-primary {
        background-color: rgba(59, 118, 225, 0.25) !important;
    }

    .badge-soft-danger {
        color: #f56e6e !important;
        background-color: rgba(245, 110, 110, 0.1);
    }

    .badge-soft-success {
        color: #63ad6f !important;
        background-color: rgba(99, 173, 111, 0.1);
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
        border-radius: 0.5rem;
        opacity: 0.85;
    }

    .max-lines {
        /* display: block;
    text-overflow: ellipsis;
    word-wrap: break-word;
    overflow: hidden;
    height: 2.8em;
    line-height: 1.4em; */
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        padding-left: 4px;
        padding-top: 4px;
    }

    .font-size-16 {
        font-size: 13px;
    }

    .font-size-14 {
        font-size: 15px;
    }

    .candidate-list-images {
        border-radius: 12px;
        /* background-color: #94c29c; */
        text-align: center;
    }

    .candidate-list-images span {
        font-size: 30px;
        width: 50px;
        height: 50px;
        display: block;
    }
</style>
