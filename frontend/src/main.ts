import App from '@/App.vue';
import i18n from '@/i18n';
import '@/logout-scheduler';
import router from '@/router';
import { listenForThemeChanges } from '@/theme';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import VCalendar from 'v-calendar';
import 'v-calendar/style.css';
import { createApp } from 'vue';
import VueApexCharts from 'vue3-apexcharts';

listenForThemeChanges();

createApp(App)
    .use(router)
    .use(i18n as any)
    .use(VCalendar, {})
    .use(VueApexCharts)
    .mount('#app');

import 'bootstrap/dist/js/bootstrap.js';
