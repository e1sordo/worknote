<template>
    <div v-if="dayInfo" class="day-core flex flex-col h-full z-10 overflow-hidden"
        :class="{ 'day-today': day.isToday, 'weekday': dayInfo.nonWorkingDay, 'opacity-25': !isPastDay }" type="button"
        @click="setActiveDayInfo(day.id)" data-bs-toggle="modal" data-bs-target="#staticBackdrop">

        <div class="flex day-label-header">
            <span class="day-label text-dark" :class="{ 'text-muted': !isPastDay, 'text-danger': day.isToday }">
                {{ day.day }}
            </span>
            <span v-if="dayInfo.sequenceNumber === 'number'">
                #{{ dayInfo.sequenceNumber }}
            </span>
        </div>

        <div class="flex-grow overflow-y-auto overflow-x-auto text-xs leading-tight rounded-sm p-1 mt-0 mb-1">
            <div v-if="dayInfo.vacation" class="alert alert-warning" role="alert">
                🏖️ {{ $t("calendar.vacation") }}
            </div>

            <div v-if="dayInfo.additionalInfo" class="alert alert-success" role="alert">
                {{ dayInfo.additionalInfo }}
            </div>

            <progress-bar v-if="isPastDay && !dayInfo.vacation && dayInfo.workingMinutes > 0"
                :synchronized="durationOfSynced" :loggedHereOnly="durationOfLoggedOnly" :total="dayInfo.workingMinutes"
                :isPast="isPastDay" />

            <div v-if="dayInfo.summary && dayInfo.summary.length > 0" class="alert alert-info mb-1">
                {{ dayInfo.summary }}
            </div>

            <worklog-list :data="sortedWorklogs" />
        </div>
    </div>
</template>

<script>
import ProgressBar from '@/components/ProgressBar.vue';
import WorklogList from '@/components/WorklogList.vue';
import { defineComponent } from 'vue';

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
                // Преобразуем строки времени в объекты Date
                var timeA = Date.parse('2000-01-01 ' + a.startTime);
                var timeB = Date.parse('2000-01-01 ' + b.startTime);

                // Сравниваем времена и возвращаем результат сортировки
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
        }
    }
});
</script>
