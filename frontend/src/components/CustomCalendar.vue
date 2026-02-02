<template>
    <div class="custom-calendar-container">
        <div v-for="week in groupedWeeks" :key="week.id" class="week-block">

            <!-- Month Label (Above the week) -->
            <div class="month-label my-3">
                {{ week.monthLabel }}
            </div>

            <!-- Days Grid -->
            <div class="week-grid" :class="{ 'hide-weekends': hideWeekends }">
                <div
                    v-for="(dayInfo, index) in week.days"
                    :key="dayInfo.date"
                    class="calendar-cell bg-body shadow-sm mx-xl-3 mx-lg-2 my-2 rounded"
                    :class="{ 'today': isToday(dayInfo.date), 'offset-cell': index === 0 && getStartOffset(dayInfo.date) > 1 }"
                    :style="index === 0 ? { gridColumnStart: getStartOffset(dayInfo.date) } : {}"
                >
                    <slot name="day-content" :day="createDayObject(dayInfo)" :dayInfo="dayInfo"></slot>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, computed, PropType } from 'vue';
import { DayInfo } from '@/api/backend-api';
import moment from 'moment';
import { useI18n } from 'vue-i18n';

interface WeekGroup {
    id: string;
    days: DayInfo[];
    monthLabel: string;
}

export default defineComponent({
    name: 'CustomCalendar',
    props: {
        days: {
            type: Array as PropType<DayInfo[]>,
            required: true
        },
        hideWeekends: {
            type: Boolean,
            default: false
        }
    },
    setup(props) {
        const { locale } = useI18n();

        const groupedWeeks = computed<WeekGroup[]>(() => {
            const groups: Record<string, WeekGroup> = {};
            
            const filteredDays = props.days.filter(day => {
                if (!props.hideWeekends) return true;
                const d = moment(day.date);
                const dayOfWeek = d.isoWeekday(); // 1-7
                return dayOfWeek < 6; // Keep 1-5
            });

            filteredDays.forEach(day => {
                const d = moment(day.date);
                const weekStart = d.clone().startOf('isoWeek').format('YYYY-MM-DD');
                
                if (!groups[weekStart]) {
                    groups[weekStart] = {
                        id: weekStart,
                        days: [],
                        monthLabel: d.locale(locale.value).format('MMMM YYYY')
                    };
                }
                groups[weekStart].days.push(day);
            });

            // Convert to array and sort DESC
            const result = Object.values(groups).sort((a, b) => {
                return moment(b.id).diff(moment(a.id));
            });
            
            // Sort days within each week ASC
            result.forEach(group => {
                group.days.sort((a, b) => moment(a.date).diff(moment(b.date)));
            });

            return result;
        });

        const getStartOffset = (date: string) => {
            if (props.hideWeekends) {
                // Mon=1, Tue=2...
                return moment(date).isoWeekday(); 
            }
            return moment(date).isoWeekday();
        };

        const isToday = (date: string) => {
            return moment(date).isSame(moment(), 'day');
        };

        const createDayObject = (dayInfo: DayInfo) => {
            return {
                id: dayInfo.date,
                day: moment(dayInfo.date).date(),
                isToday: isToday(dayInfo.date),
                date: new Date(dayInfo.date)
            };
        };

        return {
            groupedWeeks,
            isToday,
            createDayObject,
            getStartOffset
        };
    }
});
</script>

<style scoped lang="scss">

.custom-calendar-container {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 30px; /* Space between weeks */
    font-family: BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.week-block {
    display: flex;
    flex-direction: column;
}

.week-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    width: 100%;
    gap: 15px; /* Spacing between cards */
}

.week-grid.hide-weekends {
    grid-template-columns: repeat(5, 1fr);
}

/* Month Label */
.month-label {
    text-align: center;
    font-weight: bold;
    color: #adb5bd; /* text-muted-ish */
    background: rgba(233, 236, 239, 0.5); /* subtle bg */
    border-radius: 4px;
    align-self: center;
    padding: 2px 10px;
}

.calendar-cell {
    min-height: var(--day-height, 150px);
    min-width: var(--day-width, 90px); 
    background: var(--bs-body-bg);
    text-align: initial;
}
</style>
