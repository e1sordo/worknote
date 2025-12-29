<template>
    <div v-if="vacation" class="card shadow-sm p-3 mb-2" role="button" @click="emit('click', vacation)">
        <div class="d-flex align-items-center mb-2">
            <span class="fs-3 me-2">{{ icon }}</span>
            <div class="flex-fill text-start">
                <div class="fw-light">
                    {{ t('calendar.nextVacation.title') }}
                    <span class="fw-semibold">{{ countdown }}</span>
                </div>
                <span class="text-primary fw-bold me-2">{{ period }}</span>
                <span class="text-secondary fw-light">({{ duration }})</span>
            </div>
        </div>

        <div class="d-flex gap-3 small">
            <template v-if="showConfirmation">
                <span :class="['d-inline-flex align-items-center gap-1', confirmClass]">
                    <i :class="confirmIcon"></i>
                    {{ confirmText }}
                </span>
            </template>

            <template v-if="showSync">
                <span :class="['d-inline-flex align-items-center gap-1', syncClass]">
                    <i class="bi bi-arrow-repeat"></i>
                    {{ syncText }}
                </span>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
    import { ref, computed, onMounted, onUnmounted } from 'vue';
    import { useI18n } from 'vue-i18n';
    import api, { Vacation } from '@/api/backend-api';

    const emit = defineEmits<{
        click: [vacation: Vacation];
        error: [error: Error];
    }>();

    const { t, locale } = useI18n();
    const vacation = ref<Vacation | null>(null);
    let timer: number | null = null;

    const daysUntil = (dateStr: string) => {
        const d = new Date(dateStr),
            today = new Date();
        today.setHours(0, 0, 0, 0);
        d.setHours(0, 0, 0, 0);
        return Math.ceil((d.getTime() - today.getTime()) / 86400000);
    };

    const fetchVacation = async () => {
        try {
            vacation.value = (await api.nextVacation()).data;
        } catch (e) {
            emit('error', e instanceof Error ? e : new Error('Unknown error'));
        }
    };

    const period = computed(() => {
        if (!vacation.value) return '';
        const start = new Date(vacation.value.startDate);
        const end = new Date(vacation.value.endDate);
        const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long' };
        return start.getMonth() === end.getMonth() && start.getFullYear() === end.getFullYear()
            ? `${start.getDate()}–${end.getDate()} ${start.toLocaleString(locale.value, { month: 'long' })}`
            : `${start.toLocaleDateString(locale.value, opts)} – ${end.toLocaleDateString(locale.value, opts)}`;
    });

    const countdown = computed(() => {
        if (!vacation.value) return '';
        const d = daysUntil(vacation.value.startDate);
        if (d === 0) return t('calendar.nextVacation.countdown.today');
        if (d === 1) return t('calendar.nextVacation.countdown.tomorrow');
        if (d < 0) return t('calendar.nextVacation.countdown.ongoing', { days: Math.abs(d) });
        return t('calendar.nextVacation.countdown.inDays', { days: d });
    });

    const duration = computed(() =>
        vacation.value ? t('calendar.nextVacation.duration', { days: vacation.value.totalDays }) : ''
    );

    const confirmDeadlineDays = computed(() => (vacation.value ? daysUntil(vacation.value.startDate) - 14 : 0));
    const isConfirmed = computed(() => vacation.value?.confirmed);
    const isUrgentConfirm = computed(() => confirmDeadlineDays.value <= 7 && !isConfirmed.value);

    const showConfirmation = computed(() => {
        if (!vacation.value) return false;
        if (isConfirmed.value) {
            return daysUntil(vacation.value.startDate) > 7; // до недели до начала
        }
        return daysUntil(vacation.value.startDate) > 0; // до начала отпуска
    });

    const confirmClass = computed(() => {
        if (isConfirmed.value) return 'text-success';
        return confirmDeadlineDays.value < 21 ? 'text-danger' : '';
    });

    const confirmIcon = computed(() => (isConfirmed.value ? 'bi bi-check-circle' : 'bi bi-exclamation-circle'));

    const confirmText = computed(() => {
        if (isConfirmed.value) return t('calendar.nextVacation.status.confirmed');
        const daysLeft = confirmDeadlineDays.value;
        return `${t('calendar.nextVacation.status.needsConfirmation')} (${daysLeft} ${t('days')})`;
    });

    const showSync = computed(() => {
        if (!vacation.value) return false;
        const d = daysUntil(vacation.value.startDate);
        if (isConfirmed.value && d <= 7) return true;
        if (d < 0) return true; // уже идет
        return false;
    });

    const syncClass = computed(() => {
        if (!vacation.value) return '';
        return daysUntil(vacation.value.endDate) >= 0 && daysUntil(vacation.value.startDate) < 0
            ? 'text-danger' // отпуск идет
            : '';
    });

    const syncText = computed(() =>
        t(vacation.value?.synced ? 'calendar.nextVacation.status.synced' : 'calendar.nextVacation.status.notSynced')
    );

    const icon = computed(() => (isUrgentConfirm.value ? '🚨' : '🏖️'));

    onMounted(() => {
        fetchVacation();
        timer = window.setInterval(fetchVacation, 300000);
    });
    onUnmounted(() => {
        if (timer) clearInterval(timer);
    });
</script>
