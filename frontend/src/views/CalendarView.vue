<template>
  <div v-if="Object.keys(daysMap).length !== 0">
    <full-day-modal :dayInfo="activeDayInfo" @createWorklog="addWorklog" @deleteWorklog="removeWorklog"
      @worklogSynced="syncWorklog" @updateDaySummary="setNewDaySummary" />

    <v-calendar class="custom-calendar max-w-full" :masks="masks" expanded :max-date="endOfWeek" trim-weeks
      disable-page-swipe is-expanded view="weekly" :rows="5" locale="ru">
      <template v-slot:day-content="{ day }">
        <calendar-day v-if="daysMap[day.id]" :day="day" :dayInfo="daysMap[day.id]"
          @selectActiveDay="handleActiveDaySelect" />
      </template>
    </v-calendar>
  </div>
  <div style="width: 100%; height: 320px;" />
</template>

<script lang="ts">
import CalendarDay from '@/components/CalendarDay.vue';
import FullDayModal from '@/components/FullDayModal.vue';
import moment from 'moment';
import { defineComponent, ref } from 'vue';
import api, { DayInfo, Worklog } from "../api/backend-api";

// interface CalendarItemArgument {
//   key: number;
//   dates: Date;
//   class: string;
//   // sequenceNumber: number;
//   // workingMinutes: number;
//   // summary: string;
//   // worklogs: Worklog[];
//   // nonWorkingDay: boolean;
//   // additionalInfo: string;
// }

interface Foo {
  [key: string]: DayInfo;
}

export default defineComponent({
  name: 'CalendarView',
  components: {
    FullDayModal, CalendarDay
  },

  data() {
    return {
      daysMap: {} as Foo
      // calendarAttributes: [] as CalendarItemArgument[]
    };
  },
  beforeMount() {
    api.weeks()
      .then(response => {
        this.daysMap = Object.fromEntries(
          response.data.map(day => [day.date, day])
        );
        // this.calendarAttributes.push({
        //   key: day.getTime(),
        //   dates: day,
        //   class: 'bg-indigo-500 text-whitee'
        // });
      });
  },
  setup() {
    const activeDayInfo = ref();
    const endOfWeek = moment().endOf('week').toDate();
    const masks = ref({ weekdays: 'WWWW' });

    return { activeDayInfo, endOfWeek, masks };
  },
  methods: {
    handleActiveDaySelect(date: string) {
      this.activeDayInfo = this.daysMap[date];
    },
    addWorklog(date: string, worklog: Worklog) {
      const dayInfo = this.daysMap[date];
      dayInfo.worklogs = [...dayInfo.worklogs, worklog];
    },
    removeWorklog(date: string, id: number) {
      const dayInfo = this.daysMap[date];
      dayInfo.worklogs = dayInfo.worklogs.filter(worklog => worklog.id !== id);
    },
    syncWorklog(date: string, worklog: Worklog) {
      const dayInfo = this.daysMap[date];
      var foundIndex = dayInfo.worklogs.findIndex(item => item.id == worklog.id);
      dayInfo.worklogs[foundIndex] = worklog;
    },
    setNewDaySummary(date: string, newSummary: string) {
      const dayInfo = this.daysMap[date];
      dayInfo.summary = newSummary;
    }
  }
});
</script>

<style lang="scss">
@import 'v-calendar/dist/style.css';

.text-whitee {
  color: black !important;
}

::-webkit-scrollbar {
  width: 0px;
}

::-webkit-scrollbar-track {
  display: none;
}

.custom-calendar.vc-container {
  --day-border: 1px solid #d3dbe3;
  --day-border-highlight: 1px solid #b8c2cc;
  --day-width: 90px;
  --day-height: 90px;
  --weekday-bg: #f8fafc;
  --weekday-border: 1px solid #eaeaea;
  border-radius: 0;
  width: 100%;

  & .vc-pane {
    background-color: var(--weekday-bg);
  }

  & .vc-header {
    background-color: var(--weekday-bg);
    padding: 0;
    margin-top: 0;
    height: 60px;
  }

  & .vc-title {
    background-color: var(--weekday-bg);
    color: rgb(53, 74, 92);
    text-transform: capitalize;
  }

  & .vc-weeks {
    padding: 0;
  }

  & .vc-weekday {
    background-color: var(--weekday-bg);
    border-bottom: var(--weekday-border);
    border-top: var(--weekday-border);
    padding: 5px 0;
  }

  & .day-label-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  & .day-label {
    font-size: 24px;
    font-weight: 700;
  }

  & .vc-day {
    text-align: left;
    min-height: var(--day-height);
    min-width: var(--day-width);
    background-color: white;

    &:not(.on-bottom) {
      border-bottom: var(--day-border);
    }

    &:not(.on-right) {
      border-right: var(--day-border);
    }
  }

  & .vc-day-dots {
    margin-bottom: 5px;
  }

  & .day-today {
    background-color: #f7e8ef61 !important;
  }

  & .day-core {
    padding: 5px 8px;
    height: 100%;
  }

  & .weekday {
    background-color: #f5f5f5;
  }
}
</style>
