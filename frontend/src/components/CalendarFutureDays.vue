<template>
  <div
    v-if="data && Object.keys(data).length"
    class="days-grid"
  >
    <div
      v-for="(day, key) in data"
      :key="key"
      class="day-card shadow-sm rounded py-2 px-1 m-2"
      :class="dayClasses(day)"
      @click="setActiveDayInfo(day.date)"
      data-bs-toggle="modal" 
      data-bs-target="#staticBackdrop"
    >
      <div class="day-of-week mb-2 text-secondary">
        {{ formatDayOfWeek(day.date) }}
      </div>

      <div class="flags h5">
        <span v-if="day.nonWorkingDay">🎉</span>
        <span v-if="day.vacation">🌴</span>
        <span v-if="day.reducedWorkingDay">⏳</span>
        <span v-if="day.workingMinutes > 0">🗓️</span>
      </div>

      <div class="date">
        {{ formatDate(day.date) }}
      </div>

      <div class="summary" v-if="day.summary">
        {{ day.summary }}
      </div>

      <div class="summary" v-if="day.vacation">
        <div v-if="day.worklogs.length == 1" class="alert alert-success p-1 mx-2 mt-1">
            🏖️ {{ day.worklogs[0].summary }}
        </div>
        <div v-else class="alert alert-danger p-1 mx-2 mt-1" role="alert">
            No worklog
        </div>
      </div>

      <div class="summary" v-if="day.reducedWorkingDay">
        {{ day.workingMinutes / 60 }}
      </div>

      <div class="info mt-2" v-if="day.additionalInfo">
        {{ day.additionalInfo }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { DayInfo} from '@/api/backend-api';
import { useI18n } from 'vue-i18n';

defineProps<{
  data: DayInfoMap;
}>();

const emit = defineEmits<{
  (e: 'selectActiveDay', date: string): void;
}>();

function setActiveDayInfo(date: string) {
  emit('selectActiveDay', date);
}

interface DayInfoMap {
    [key: string]: DayInfo;
}

function dayClasses(day: DayInfo) {
  return {
    holiday: day.nonWorkingDay,
    vacation: day.vacation,
    reduced: day.reducedWorkingDay && !day.nonWorkingDay,
  };
}

function formatDayOfWeek(date: string) {
  const { t, locale } = useI18n();
  const opts: Intl.DateTimeFormatOptions = { weekday: 'long' };
  const d = new Date(date);
  return d.toLocaleDateString(locale.value, opts);
}

function formatDate(date: string) {
  const { t, locale } = useI18n();
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long' };
  const d = new Date(date);
  return d.toLocaleDateString(locale.value, opts);
}
</script>

<style scoped>
.days-grid {
  display: grid;
  gap: 8px;
}

/* 🖥️ 1025px и больше — строго один ряд */
@media (min-width: 1025px) {
  .days-grid {
    grid-auto-flow: column;
    grid-auto-columns: minmax(72px, 1fr);
    grid-template-rows: 1fr;
  }
}

/* 📱 1024px и меньше — строго два ряда */
@media (max-width: 1024px) {
  .days-grid {
    grid-auto-flow: column;
    grid-template-rows: repeat(2, auto);
    grid-auto-columns: minmax(72px, 1fr);
  }
}

/* карточка дня */
.day-card {
  flex: 1;
  min-width: 0;
  background: #f8f9fa;
  text-align: center;
  font-size: 14px;
  line-height: 1.4;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.day-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

.day-of-week {
  font-weight: 400;
}

.date {
  font-weight: 600;
}

.minutes {
  font-size: 11px;
  color: #555;
}

.summary {
  margin-top: 2px;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info {
  font-size: 10px;
  color: #777;
}

.holiday {
  background: #d4ede0;
  color: #a10000;
}

.vacation {
  background: #fff8e8;
}

.reduced {
  background: #ffe4e1;
}
</style>