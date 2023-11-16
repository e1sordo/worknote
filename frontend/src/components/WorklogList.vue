<template>
    <div class="list-group worklogs-compact-list">
        <a v-for="worklog in sortedData" :key="worklog.id" class="worklogs-group-item" data-bs-toggle="tooltip"
            :title="worklog.task.title">
            <div class="d-flex mb-1 w-100 align-items-center justify-content-between">
                <span class="d-inline-block" tabindex="0">
                    <small>{{ taskTypeMeta[worklog.task.type].icon }}</small>
                    <small>&nbsp;{{ worklog.task.id }}</small>
                </span>
                <div class="d-flex flex-row-reverse" style="width: 60%;">
                    <div class="progress" style="height: 15px;" :style="{ width: progressBarWidths[worklog.id] + '%' }"
                        :title="formatTime(worklog.durationInMinutes)">
                        <div class="progress-bar bg-secondary w-100" role="progressbar"
                            :aria-valuenow="progressBarWidths[worklog.id]">
                            <small v-if="progressBarWidths[worklog.id] > 35">
                                {{ formatTime(worklog.durationInMinutes) }}
                            </small>
                        </div>
                    </div>
                </div>
            </div>
            <div class="worklog-description">
                <span class="badge worklog-start-time"
                    :class="{ 'bg-success': worklog.synced, 'bg-warning text-dark': !worklog.synced }">
                    {{ worklog.startTime.split(':').slice(0, 2).join(':') }}
                </span>
                <span>{{ worklog.summary }}</span>
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
    },
    computed: {
        sortedData() {
            const sortedArray = [...this.data];
            sortedArray.sort((a, b) => b.durationInMinutes - a.durationInMinutes);
            return sortedArray;
        },
        progressBarWidths() {
            const widthMap = {};
            const minWidth = 15; // %

            const minDuration = 0;
            const maxDuration = Math.max(...this.data.map(worklog => worklog.durationInMinutes));

            this.data.forEach((worklog) => {
                const normalizedWidth = minWidth + ((worklog.durationInMinutes - minDuration) / (maxDuration - minDuration)) * (100 - minWidth);

                widthMap[worklog.id] = Math.max(minWidth, Math.min(100, parseInt(normalizedWidth, 10)));
            });

            return widthMap;
        },
    }
});
</script>

<style>
.worklog-start-time {
    margin-right: 5px;
    position: relative;
    top: -1px;
}

.worklog-description {
    line-height: 18px;
    font-size: 14px;
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

.progress-bar.bg-secondary {
    --bs-bg-opacity: 0.7;
}
</style>
