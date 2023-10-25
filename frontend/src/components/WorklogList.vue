<template>
    <div class="list-group worklogs-compact-list">
        <a v-for="worklog in data" :key="worklog.id" class="worklogs-group-item" data-bs-toggle="tooltip"
            :title="worklog.task.title">
            <div class="d-flex mb-1 w-100 align-items-center justify-content-between">
                <span class="d-inline-block" tabindex="0">
                    <small>{{ taskTypeMeta[worklog.task.type].icon }} </small>
                    <small>{{ worklog.task.shortCode }}-{{ worklog.task.id }}</small>
                </span>
                <h6 class="mb-0">
                    <small><i v-if="!worklog.synced" class="bi bi-patch-question-fill text-danger"></i></small>
                    {{ formatTime(worklog.durationInMinutes) }}
                </h6>
            </div>
            <div class="worklog-description">
                <mark class="opacity-75">
                    {{ worklog.startTime.split(':').slice(0, 2).join(':') }}
                </mark>
                {{ worklog.summary }}
                <span v-if="!worklog.summary" class="text-muted">
                    <em>{{ $t('calendar.placeholder.worklogSummary') }}</em>
                </span>
            </div>
        </a>
    </div>
</template>

<script>
import { formatTime, taskTypeMeta } from '@/constants';
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
        return { formatTime, taskTypeMeta }
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