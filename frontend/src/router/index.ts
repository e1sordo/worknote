import { createRouter, createWebHistory } from 'vue-router';
import CalendarView from '@/views/CalendarView.vue';
import WeeklyOverView from '@/views/WeeklyOverView.vue';
import AnalyticsView from '@/views/AnalyticsView.vue';

const routes = [
    {
        path: '/',
        name: 'home',
        component: CalendarView
    },
    {
        path: '/weekly',
        name: 'weekly',
        component: WeeklyOverView
    },
    {
        path: '/overview',
        name: 'overview',
        component: AnalyticsView
    },
    {
        path: '/settings',
        name: 'settings',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import(/* webpackChunkName: "about" */ '../views/SettingsView.vue')
    }
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
});

export default router;
