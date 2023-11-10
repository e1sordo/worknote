<template>
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
        element.classList.add('shadow', 'mx-2', 'my-2', 'bg-body', 'rounded');
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
  // --text-body: --text-body-light;
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
    color: rgb(155, 155, 155);
    background: rgba(149, 164, 181, 0.3);
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
    text-align: left;
    min-height: var(--day-height);
    min-width: var(--day-width);
    overflow: hidden;
  }

  @media (prefers-color-scheme: dark) {
    .vc-day {
      background-color: rgb(16, 26, 34) !important;
    }
  }

  & .vc-day-dots {
    margin-bottom: 5px;
  }

  & .day-today {
    background-color: rgba(255, 158, 196, 0.19);
  }

  & .day-core {
    padding: 5px 8px;
    height: 100%;
  }

  & .weekday {
    background-image: linear-gradient(135deg, rgba(84, 90, 101, 0.1) 25%, rgba(45, 49, 58, 0.1) 25%, rgba(45, 49, 58, 0.1) 50%, rgba(84, 90, 101, 0.1) 50%, rgba(84, 90, 101, 0.1) 75%, rgba(45, 49, 58, 0.1) 75%, rgba(45, 49, 58, 0.1) 100%);
    background-size: 40px 40px;
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
