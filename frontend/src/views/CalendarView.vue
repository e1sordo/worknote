<template>
  <!-- svg template for using in children components (for ex. holiday alerts) -->
  <svg xmlns="http://www.w3.org/2000/svg" style="display: none;">
    <symbol id="info-fill" fill="currentColor" viewBox="0 0 16 16">
      <path
        d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2z" />
    </symbol>
  </svg>

  <div class="position-absolute" style="top: 50px; right: 20px;">
    <button @click="handleClick" type="button" class="btn btn-outline-secondary btn-sm mb-2">
      {{ $t("calendar.toggleWeekends") }}
    </button>
  </div>
  <div v-if="Object.keys(daysMap).length !== 0" class="mb-5">
    <full-day-modal :dayInfo="activeDayInfo" @createWorklog="addWorklog" @deleteWorklog="removeWorklog"
      @worklogSynced="syncWorklog" @updateDaySummary="setNewDaySummary"
      @updateDayNonWorkingStatus="setNewDayNonWorkingStatus" @updateDayVacation="setNewDayVacation"
      @updateWorkingMinutesCount="setNewDayWorkingMinutes" />

    <v-calendar class="custom-calendar max-w-full" :masks="masks" expanded :max-date="endOfWeek" trim-weeks
      disable-page-swipe is-expanded view="weekly" :rows="5" :locale="$i18n.locale" firstDayOfWeek=2>
      <template v-slot:day-content="{ day }">
        <calendar-day v-if="daysMap[day.id]" :day="day" :dayInfo="daysMap[day.id]"
          @selectActiveDay="handleActiveDaySelect" />
      </template>
    </v-calendar>
  </div>
</template>

<script lang="ts">
import api, { DayInfo, Worklog } from "@/api/backend-api";
import CalendarDay from '@/components/CalendarDay.vue';
import FullDayModal from '@/components/FullDayModal.vue';
import moment from 'moment';
import { defineComponent, ref } from 'vue';

interface DayInfoMap {
  [key: string]: DayInfo;
}

export default defineComponent({
  name: 'CalendarView',
  components: {
    FullDayModal, CalendarDay
  },

  data() {
    return {
      daysMap: {} as DayInfoMap
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
      })
      .then(() => {
        if (this.getWeekendsFlagFromStorage() === false) {
          this.toggleWeekends();
        }
        this.setDayStyles();
        this.setOrderStyle();
      });
  },
  setup() {
    const activeDayInfo = ref();
    // summer
    // const endOfWeek = moment().endOf('week').add(1, 'day').toDate();

    // winter
    const endOfWeek = moment().endOf('week').add(1, 'week').toDate();

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
    },
    setNewDayWorkingMinutes(date: string, newValue: number) {
      const dayInfo = this.daysMap[date];
      dayInfo.workingMinutes = newValue;
    },
    setNewDayNonWorkingStatus(date: string, newValue: boolean, newMinutesCount: number) {
      const dayInfo = this.daysMap[date];
      dayInfo.nonWorkingDay = newValue;
      dayInfo.workingMinutes = newMinutesCount;
    },
    setNewDayVacation(date: string, newValue: boolean) {
      const dayInfo = this.daysMap[date];
      dayInfo.vacation = newValue;
    },
    setOrderStyle() {
      const parentClass = 'vc-pane-layout';
      const parentElement = document.querySelector(`.${parentClass}`);

      if (parentElement) {
        const childElements = Array.from(parentElement.children);
        const reversedChildElements = childElements.reverse();

        reversedChildElements.forEach((child, index) => {
          child.setAttribute('style', `order: ${index + 1};`);
        });
      }
    },
    setDayStyles() {
      const vcDayElements = document.querySelectorAll('.vc-day');
      vcDayElements.forEach((element) => {
        element.classList.add(
          'vc-day-body', 'bg-body', 'shadow-sm', 'mx-xl-3', 'mx-lg-2', 'my-2', 'rounded'
        );
      });
    },
    handleClick() {
      this.saveWeekedsFlagToStorage(!this.getWeekendsFlagFromStorage());
      this.toggleWeekends();
    },
    toggleWeekends() {
      const vcWeekElements = document.querySelectorAll('.vc-week');
      vcWeekElements.forEach((element) => {
        element.classList.toggle('hidden-weekends');
      });

      const vcWeekdaysElements = document.querySelectorAll('.vc-weekdays');
      vcWeekdaysElements.forEach((element) => {
        element.classList.toggle('hidden-weekends');
      });
    },
    getWeekendsFlagFromStorage() {
      const savedValue = localStorage.getItem('showWeekends');
      return savedValue === 'true';
    },
    saveWeekedsFlagToStorage(value: Boolean) {
      localStorage.setItem('showWeekends', value.toString());
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

[data-bs-theme=dark] {
  --bs-highlight-bg: rgb(107, 114, 10);
}

.custom-calendar.vc-container {
  --vc-bg: rgba(255, 255, 255, 0);
  --vc-border: rgba(255, 255, 255, 0);
  --day-border: 1px solid #d3dbe3;
  --day-border-highlight: 1px solid #b8c2cc;
  --day-width: 90px;
  --day-height: 90px;
  --weekday-bg: #f8fafc;
  --weekday-border: 1px solid #eaeaea;
  border-radius: 0;
  width: 100%;

  & .vc-bordered {
    border: 0;
  }

  & .vc-header {
    padding: 0 20px;
    margin-top: 0;
    height: 60px;
  }

  & .vc-title {
    color: rgb(126, 126, 126);
    background: rgba(161, 181, 149, 0.3);
    text-transform: capitalize;
    opacity: 0.8;
  }

  & .vc-weeks {
    padding: 0;
  }

  & .vc-weekday {
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
    background: var(--bs-body-bg);
    text-align: left;
    min-height: var(--day-height);
    min-width: var(--day-width);
    overflow: hidden;
  }

  @media (prefers-color-scheme: dark) {
    .vc-day-body {
      background-color: rgb(16, 26, 34) !important;
    }
  }

  & .vc-day-dots {
    margin-bottom: 5px;
  }

  & .day-core {
    height: 100%;
  }
}

.hidden-weekends {
  grid-template-columns: repeat(5, 1fr) !important;
  grid-auto-flow: dense;
}

.hidden-weekends>*:nth-child(n+6) {
  display: none;
}
</style>
