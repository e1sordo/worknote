<template>
    <p v-if="minutesRemain > 0" :class="{ 'text-primary': isToday || !isPast, 'text-danger': isPast && !isToday }">
        <small>{{ helpText }}</small>
    </p>
    <p v-if="minutesRemain == 0 && synchronized != total && exceedPercent == 0" class="text-primary">
        <small>{{ $t('worklog.progress.needToSync') }}</small>
    </p>
    <p v-if="exceedPercent > 0" class="text-danger">
        <small>{{ $t('worklog.progress.exceeding', { minutes: minutesExceed }) }}</small>
    </p>

    <div class="progress my-2" :style="{ height: big ? '16px' : '6px' }">
        <div :aria-valuenow="syncedPercent" aria-valuemin="0" aria-valuemax="100" class="progress-bar bg-success"
            :style="{ width: syncedPercent + '%' }" role="progressbar">
        </div>
        <div :aria-valuenow="loggedPercent" aria-valuemin="0" aria-valuemax="100"
            class="progress-bar progress-bar-striped progress-bar-animated bg-warning" role="progressbar"
            :style="{ width: loggedPercent + '%' }">
        </div>
        <div v-if="isPast && syncedPercent < 100" :aria-valuenow="toLogPercent" aria-valuemin="0" aria-valuemax="100"
            class="progress-bar progress-bar-striped progress-bar-animated bg-danger" role="progressbar"
            :style="{ width: toLogPercent + '%' }">
        </div>
        <div :aria-valuenow="exceedPercent" aria-valuemin="0" aria-valuemax="100"
            class="progress-bar progress-bar-striped progress-bar-animated bg-secondary" role="progressbar"
            :style="{ width: exeedPercent + '%' }">
        </div>
    </div>
</template>

<script>
import { formatTime } from '@/constants';
import { defineComponent, reactive, watchEffect } from 'vue';
import { useI18n } from "vue-i18n";

export default defineComponent({
    name: 'ProgressBar',
    props: {
        synchronized: Number,
        loggedHereOnly: Number,
        total: { type: Number, required: true },
        big: { type: Boolean, default: false },
        isPast: Boolean,
        isToday: Boolean
    },
    setup(props) {
        const i18n = useI18n();

        const state = reactive({
            realSynced: 0,
            realLogged: 0,
            syncedPercent: 0,
            loggedPercent: 0,
            toLogPercent: 0,
            exceedPercent: 0,
            minutesRemain: 0,
            minutesExceed: 0,
            helpText: ''
        });

        const calculatePercentages = () => {
            state.realSynced = props.synchronized || 0;
            state.realLogged = props.loggedHereOnly || 0;

            const remainMinutes = props.total - state.realSynced - state.realLogged;
            if (remainMinutes < 0) {
                state.minutesExceed = -remainMinutes;
                state.exceedPercent = Math.ceil(state.minutesExceed / props.total * 100);
                state.toLogPercent = 0;
            } else {
                state.minutesExceed = 0;
                state.exceedPercent = 0;
                state.toLogPercent = Math.ceil(remainMinutes / props.total * 100);
            }

            state.minutesRemain = Math.max(0, remainMinutes);

            state.syncedPercent = Math.ceil(state.realSynced / props.total * 100);
            state.loggedPercent = Math.ceil(state.realLogged / props.total * 100) - state.exceedPercent;

            state.helpText = i18n.t('worklog.progress.remainToLog', { time: formatTime(state.minutesRemain) });
        };

        watchEffect(() => {
            calculatePercentages();
        });

        calculatePercentages();

        return state;
    }
});
</script>
