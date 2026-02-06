<template>
    <div v-if="dayInfo" class="day-core flex flex-col h-100 z-10 overflow-hidden rounded-3"
        :class="{ 'bg-gradient bg-danger': day.isToday, 'bg-gradient bg-success': dayInfo.nonWorkingDay, 'opacity-50': !isPastDay }"
        type="button" @click="setActiveDayInfo(day.id)" data-bs-toggle="modal" data-bs-target="#staticBackdrop">

        <div class="flex day-label-header mb-1 mx-3">
             <span class="day-label fs-1 fw-bold"
                :class="{ 'text-secondary': isPastDay && !day.isToday, 'text-muted': !isPastDay, 'text-light': day.isToday }">
                {{ day.day }}<span v-if="dayInfo.reducedWorkingDay">*</span>
            </span>
            <span class="font-monospace fs-6"
                :class="{ 'text-light': day.isToday, 'text-muted': !day.isToday }">
                {{ formatDayOfWeek(day.id) }}
            </span>
        </div>

        <div class="d-flex flex-column h-100 rounded-3 my-0">
            <div class="px-3">
                <div v-if="dayInfo.vacation" class="alert alert-warning border-2" role="alert">
                    🏖️ {{ $t("calendar.vacation") }}
                </div>

                <div v-if="dayInfo.additionalInfo" class="alert alert-success d-flex align-items-center border-2"
                    role="alert">
                    <svg class="bi flex-shrink-0 me-2" width="24" height="24" role="img" aria-label="Info:">
                        <use xlink:href="#info-fill" />
                    </svg>
                    <div>{{ dayInfo.additionalInfo }}</div>
                </div>

                <div v-if="dayInfo.summary && dayInfo.summary.length > 0" class="alert alert-secondary mt-1 mb-3 py-2 px-3">
                    <small>{{ dayInfo.summary }}</small>
                </div>
            </div>

            <div class="px-3 pb-3" :class="{ 'bg-body rounded-3 h-100 pt-3': day.isToday }">
                <progress-bar v-if="isPastDay && !dayInfo.vacation && dayInfo.workingMinutes > 0" 
                    :synchronized="durationOfSynced" :loggedHereOnly="durationOfLoggedOnly"
                    :total="dayInfo.workingMinutes" :isPast="isPastDay" :isToday="day.isToday" />

                <worklog-list :data="sortedWorklogs" />
            </div>
        </div>
    </div>
</template>

<script>
import ProgressBar from '@/components/ProgressBar.vue';
import WorklogList from '@/components/WorklogList.vue';
import { defineComponent } from 'vue';
import { useI18n } from 'vue-i18n';

export default defineComponent({
    name: 'CalendarDay',
    components: {
        ProgressBar, WorklogList
    },
    props: {
        day: Object,
        dayInfo: { type: Object, required: true }
    },
    computed: {
        sortedWorklogs() {
            const arr = [...this.dayInfo.worklogs];
            arr.sort(function (a, b) {
                var timeA = Date.parse('2000-01-01 ' + a.startTime);
                var timeB = Date.parse('2000-01-01 ' + b.startTime);
                return timeA - timeB;
            });
            return arr;
        },
        durationOfSynced() {
            return [...this.dayInfo.worklogs]
                .filter(wl => wl.synced)
                .map(wl => wl.durationInMinutes)
                .reduce((prev, next) => prev + next, 0)
        },
        durationOfLoggedOnly() {
            return [...this.dayInfo.worklogs]
                .filter(wl => !wl.synced)
                .map(wl => wl.durationInMinutes)
                .reduce((prev, next) => prev + next, 0)
        },
        isPastDay() {
            return new Date(this.dayInfo.date) < new Date()
        }
    },
    methods: {
        setActiveDayInfo(date) {
            this.$emit('selectActiveDay', date);
        },
        formatDayOfWeek(date) {
            const { t, locale } = useI18n();
            const opts = { weekday: 'long' };
            const d = new Date(date);
            return d.toLocaleDateString(locale.value, opts);
        }
    }
});
</script>

<style>
.day-core.bg-gradient.bg-success {
    --bs-bg-opacity: 0.3;
}

.day-label-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>
