<template>
    <div v-if="vacation" class="position-absolute" style="top: 40px; left: 20px">
        <next-vacation-widget :data="vacation" />
    </div>

    <!-- svg template for using in children components (for ex. holiday alerts) -->
    <svg xmlns="http://www.w3.org/2000/svg" style="display: none">
        <symbol id="info-fill" fill="currentColor" viewBox="0 0 16 16">
            <path
                d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2z"
            />
        </symbol>
    </svg>

    <div class="position-absolute" style="top: 60px; right: 20px">
        <button @click="handleClick" type="button" class="btn btn-outline-secondary btn-sm mb-2">
            {{ $t('calendar.toggleWeekends') }}
        </button>
    </div>
    <div v-if="Object.keys(daysMap).length !== 0" class="mb-5">
        <full-day-modal
            :dayInfo="activeDayInfo"
            @createWorklog="addWorklog"
            @deleteWorklog="removeWorklog"
            @worklogSynced="syncWorklog"
            @updateDaySummary="setNewDaySummary"
            @updateDayNonWorkingStatus="setNewDayNonWorkingStatus"
            @updateDayVacation="setNewDayVacation"
            @updateWorkingMinutesCount="setNewDayWorkingMinutes"
        />

        <div class="mx-2 mb-2">
            <calendar-future-days 
                :data="futureDaysMap" 
                @selectActiveDay="handleActiveDaySelect"
            />
        </div>

        <custom-calendar
            :days="daysList"
            :hide-weekends="!showWeekends"
        >
            <template v-slot:day-content="{ day, dayInfo }">
                <calendar-day
                    :day="day"
                    :dayInfo="dayInfo"
                    @selectActiveDay="handleActiveDaySelect"
                />
            </template>
        </custom-calendar>
    </div>
</template>

<script lang="ts">
    import api, { DayInfo, Worklog, Vacation } from '@/api/backend-api';
    import CalendarDay from '@/components/CalendarDay.vue';
    import CustomCalendar from '@/components/CustomCalendar.vue';
    import CalendarFutureDays from '@/components/CalendarFutureDays.vue';
    import NextVacationWidget from '@/components/NextVacationWidget.vue';
    import FullDayModal from '@/components/FullDayModal.vue';
    import moment from 'moment';
    import { defineComponent, ref } from 'vue';

    interface DayInfoMap {
        [key: string]: DayInfo;
    }

    export default defineComponent({
        name: 'CalendarView',
        components: {
            FullDayModal,
            CalendarDay,
            CustomCalendar,
            CalendarFutureDays,
            NextVacationWidget
        },

        data() {
            return {
                daysMap: {} as DayInfoMap,
                daysList: [] as DayInfo[],
                futureDaysMap: {} as DayInfoMap,
                vacation: null as Vacation | null,
                daysUntilVacation: 0,
                vacationNeedsConfirmation: false,
                showWeekends: true
            };
        },
        beforeMount() {
            api.weeks(4)
                .then(response => {
                    this.daysList = response.data;
                    this.daysMap = Object.fromEntries(response.data.map(day => [day.date, day]));
                    return api.futureWeeks(2);
                })
                .then(response => {
                    this.showWeekends = this.getWeekendsFlagFromStorage();
                    
                    this.futureDaysMap = Object.fromEntries(
                        response.data.map(day => [day.date, day])
                    );
                });

            // Загружаем отпуск асинхронно
            api.nextVacation().then(res => {
                if (res.data) {
                    this.vacation = res.data;
                    const start = moment(res.data.startDate);
                    this.daysUntilVacation = start.diff(moment(), 'days');
                    this.vacationNeedsConfirmation = !res.data.confirmed && this.daysUntilVacation <= 21;
                }
            });
        },
        setup() {
            const activeDayInfo = ref();
            return { activeDayInfo };
        },
        methods: {
            getDayInfo(date: string): DayInfo {
                return this.daysMap[date] || this.futureDaysMap[date];
            },
            handleActiveDaySelect(date: string) {
                this.activeDayInfo = this.getDayInfo(date);
            },
            addWorklog(date: string, worklog: Worklog) {
                const dayInfo = this.getDayInfo(date);
                dayInfo.worklogs = [...dayInfo.worklogs, worklog];
            },
            removeWorklog(date: string, id: number) {
                const dayInfo = this.getDayInfo(date);
                dayInfo.worklogs = dayInfo.worklogs.filter(worklog => worklog.id !== id);
            },
            syncWorklog(date: string, worklog: Worklog) {
                const dayInfo = this.getDayInfo(date);
                var foundIndex = dayInfo.worklogs.findIndex(item => item.id == worklog.id);
                dayInfo.worklogs[foundIndex] = worklog;
            },
            setNewDaySummary(date: string, newSummary: string) {
                const dayInfo = this.getDayInfo(date);
                dayInfo.summary = newSummary;
            },
            setNewDayWorkingMinutes(date: string, newValue: number) {
                const dayInfo = this.getDayInfo(date);
                dayInfo.workingMinutes = newValue;
            },
            setNewDayNonWorkingStatus(date: string, newValue: boolean, newMinutesCount: number) {
                const dayInfo = this.getDayInfo(date);
                dayInfo.nonWorkingDay = newValue;
                dayInfo.workingMinutes = newMinutesCount;
            },
            setNewDayVacation(date: string, newValue: boolean) {
                const dayInfo = this.getDayInfo(date);
                dayInfo.vacation = newValue;
            },
            handleClick() {
                this.showWeekends = !this.showWeekends;
                this.saveWeekedsFlagToStorage(this.showWeekends);
            },
            getWeekendsFlagFromStorage() {
                const savedValue = localStorage.getItem('showWeekends');
                return savedValue !== 'false'; // Default true
            },
            saveWeekedsFlagToStorage(value: Boolean) {
                localStorage.setItem('showWeekends', value.toString());
            }
        }
    });
</script>

<style lang="scss">
    .text-whitee {
        color: black !important;
    }

    ::-webkit-scrollbar {
        width: 0px;
    }

    ::-webkit-scrollbar-track {
        display: none;
    }

    [data-bs-theme='dark'] {
        --bs-highlight-bg: rgb(107, 114, 10);
    }
</style>
