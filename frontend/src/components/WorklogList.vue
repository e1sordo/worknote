<template>
    <div class="list-group worklogs-compact-list">
        <a v-for="worklog in data" :key="worklog.id" class="worklogs-group-item list-group-item-action"
            data-bs-toggle="tooltip" :title="worklog.task.title">
            <div class="d-flex w-100 justify-content-between">
                <h5 class="mb-1">
                    <small><i v-if="!worklog.synced" class="bi bi-patch-question-fill"></i></small>
                    {{ formatTime(worklog.durationInMinutes) }}
                </h5>
                <span class="d-inline-block" tabindex="0">
                    <small>{{ worklog.task.shortCode }}-{{ worklog.task.id }}</small>
                    <small>{{ taskTypeIcons.get(worklog.task.type) }}</small>
                </span>
            </div>
            <div class="worklog-description">
                <mark>{{ worklog.startTime.split(':').slice(0, 2).join(':') }}</mark> {{ worklog.summary }}
                <span v-if="!worklog.summary" class="text-muted"><em>Не заполнено</em></span>
            </div>
        </a>
    </div>
</template>

<script>
import { formatTime, taskTypeIcons } from '@/constants';
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'WorklogList',
    props: {
        data: {
            type: Array,
            required: true,
            default: () => ([])
        }
    },
    setup() {
        return { formatTime, taskTypeIcons }
    }
});
</script>

<style>
.worklog-description {
    line-height: 16px;
    font-size: 13px;
}

.worklogs-group-item {
    position: relative;
    display: block;
    padding: var(--bs-list-group-item-padding-y) 0;
    color: var(--bs-list-group-color);
    text-decoration: none;
    background-color: none;
    border-bottom: var(--bs-list-group-border-width) solid var(--bs-list-group-border-color);
    border-radius: 0;
}

.worklogs-compact-list :last-child {
    border-bottom: 0;
}
</style>