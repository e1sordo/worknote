<template>
    <p v-if="minutesRemain > 0" class="text-primary"><small>{{ helpText }}</small></p>
    <p v-if="minutesRemain == 0 && synchronized != total" class="text-primary">
        <small>Нужно синхронизировать</small>
    </p>
    <p v-if="minutesRemain < 0" class="text-danger">
        <small>Внесено больше, чем нужно</small>
    </p>

    <!-- часть будет отвечать за общее время из целевого по дню, часть -- за то, что уже было синх-но с джирой и т/д -->
    <div class="progress my-2" style="height: 6px;">
        <!-- <div class="progress-bar" role="progressbar" style="width: 15%" aria-valuenow="15" aria-valuemin="0"
            aria-valuemax="100"></div> -->
        <div class="progress-bar bg-success" role="progressbar" :style="{ width: syncPercent + '%' }"
            :aria-valuenow="syncPercent" aria-valuemin="0" aria-valuemax="100">
        </div>
        <div class="progress-bar progress-bar-striped progress-bar-animated bg-warning" role="progressbar"
            :style="{ width: loggedPercent + '%' }" :aria-valuenow="loggedPercent" aria-valuemin="0" aria-valuemax="100">
        </div>
        <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar"
            :style="{ width: toLogPercent + '%' }" :aria-valuenow="toLogPercent" aria-valuemin="0" aria-valuemax="100">
        </div>
        <div class="progress-bar progress-bar-striped progress-bar-animated bg-danger" role="progressbar"
            :style="{ width: exeedPercent + '%' }" :aria-valuenow="exeedPercent" aria-valuemin="0" aria-valuemax="100">
        </div>
    </div>
</template>

<script>
import { formatTime } from '@/constants';
import { defineComponent, watch, reactive } from 'vue';

export default defineComponent({
    name: 'ProgressBar',
    props: {
        synchronized: Number,
        loggedHereOnly: Number,
        total: { type: Number, required: true }
    },
    setup(props) {
        const state = reactive({
            realSync: 0,
            realLogged: 0,
            syncPercent: 0,
            loggedPercent: 0,
            toLogPercent: 0,
            exeedPercent: 0,
            minutesRemain: 0,
            helpText: ''
        });

        const calculatePercentages = () => {
            state.realSync = props.synchronized || 0;
            state.realLogged = props.loggedHereOnly || 0;

            state.syncPercent = Math.round(state.realSync / props.total * 100);
            state.loggedPercent = Math.round(state.realLogged / props.total * 100);
            state.toLogPercent = 100 - state.syncPercent - state.loggedPercent;
            state.exeedPercent = -state.toLogPercent;

            state.minutesRemain = props.total - state.realSync - state.realLogged
            state.helpText = `Осталось внести ${formatTime(state.minutesRemain)}`;
        };

        watch(() => {
            calculatePercentages();
        });

        calculatePercentages();

        return state;
    }
});
</script>

<style></style>