<template>
    <div class="modal fade" id="staticBackdrop" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1"
        aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <div class="modal-dialog modal-xl modal-fullscreen-xl-down modal-dialog-centered modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="staticBackdropLabel">{{ humanDate(dayInfo.date) }}</h5>
                    <span ref="editableText" contenteditable="true" @keydown.enter="saveText" @blur="saveText"
                        @keydown="handleKeyDown" class="day-summary mx-3 px-1 text-start" style="flex-grow: 1;"
                        :placeholder="$t('calendar.placeholder.daySummary')">
                        {{ dayInfo.summary }}
                    </span>

                    <div class="btn-toolbar" role="toolbar" aria-label="Toolbar with button groups">
                        <div v-if="!dayInfo.nonWorkingDay" class="btn-group me-2" role="group"
                            aria-label="Working minutes group">
                            <button type="button" class="btn btn-light" @click="changeWorkingMinutes(false)">-</button>
                            <button type="button" class="btn btn-light">{{ dayInfo.workingMinutes / 60 }}</button>
                            <button type="button" class="btn btn-light" @click="changeWorkingMinutes(true)">+</button>
                        </div>

                        <div class="btn-group me-2" role="group" aria-label="Indicators group">
                            <button type="button" class="btn" @click="toggleNonWorkingStatus(!dayInfo.nonWorkingDay)">
                                <span v-if="!dayInfo.nonWorkingDay" :title="$t('calendar.actions.markHoliday')">🎉</span>
                                <span v-else :title="$t('calendar.actions.markWorking')">📆</span>
                            </button>
                            <button type="button" class="btn" @click="toggleVacation(!dayInfo.vacation)">
                                <span v-if="!dayInfo.vacation" :title="$t('calendar.actions.markVacation')">🏖️</span>
                                <span v-else :title="$t('calendar.actions.unmarkVacation')">💼</span>
                            </button>
                            <button type="button" class="btn" data-bs-dismiss="modal" aria-label="Close">X</button>
                        </div>
                    </div>
                </div>

                <div class="modal-body">
                    <p v-if="dayInfo.nonWorkingDay" class="text-muted mb-2 mt-2">
                        {{ $t("calendar.weekend") }}. {{ dayInfo.additionalInfo }}
                    </p>
                    <p v-if="dayInfo.vacation" class="mb-2 mt-2">
                        🏖️ {{ $t("calendar.vacation") }}
                    </p>

                    <worklog-form v-if="!dayInfo.vacation" :date="dayInfo.date" @createWorklog="addWorklog" />

                    <br />

                    <div v-if="!dayInfo.nonWorkingDay && !dayInfo.vacation" class="w-100 container">
                        <progress-bar :big="true" v-if="dayInfo.workingMinutes > 0" :synchronized="durationOfSynced"
                            :loggedHereOnly="durationOfLoggedOnly" :total="dayInfo.workingMinutes"
                            :isPast="new Date(dayInfo.date) < new Date()"
                            :isToday="(new Date(dayInfo.date)).setHours(0, 0, 0, 0) === (new Date()).setHours(0, 0, 0, 0)" />
                    </div>

                    <worklog-modal-list :date="dayInfo.date" :data="sortedWorklogs" @deleteWorklog="removeWorklog"
                        @worklogSynced="syncWorklog" />
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import api from "@/api/backend-api";
import ProgressBar from '@/components/ProgressBar.vue';
import WorklogForm from '@/components/WorklogForm.vue';
import WorklogModalList from '@/components/WorklogModalList.vue';
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'FullDayModal',
    components: {
        WorklogModalList, WorklogForm, ProgressBar
    },
    methods: {
        humanDate(date) {
            if (Number.isNaN(new Date(date).getTime())) {
                return '';
            }

            const options = {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
            };

            const locale = this.$i18n.locale;
            const formatter = new Intl.DateTimeFormat(locale, options);
            return formatter.format(new Date(date));
        },
        handleKeyDown(event) {
            if (event.keyCode === 13) {
                event.preventDefault(); // Prevent new line
                this.saveText();
                this.$refs.editableText.blur(); // Drop focus from editable text
            }
        },
        saveText() {
            this.text = this.$refs.editableText.innerText;
            api.updateDaySummary(this.dayInfo.date, this.text)
                .then(response => {
                    this.$emit('updateDaySummary', this.dayInfo.date, this.text);
                    console.log("Day summary was updated. Response status: " + response.status)
                });
        },
        changeWorkingMinutes(direction) {
            const valToChange = 30 // half an hour
            const newValue = this.dayInfo.workingMinutes + valToChange * (direction ? 1 : -1);
            if (newValue >= 0 && newValue <= 24 * 60) {
                api.updateWorkingMinutesCount(this.dayInfo.date, newValue)
                    .then(response => {
                        this.$emit('updateWorkingMinutesCount', this.dayInfo.date, newValue);
                        console.log("Day working minutes were updated. Response status: " + response.status)
                    });
            }
        },
        toggleNonWorkingStatus(newValue) {
            api.updateDayNonWorkingStatus(this.dayInfo.date, newValue)
                .then(response => {
                    const newMinutesCount = response.data;
                    this.$emit('updateDayNonWorkingStatus', this.dayInfo.date, newValue, newMinutesCount);
                    console.log("Day non-working status was updated. Response status: " + response.status)
                });
        },
        toggleVacation(newValue) {
            api.updateDayVacation(this.dayInfo.date, newValue)
                .then(response => {
                    this.$emit('updateDayVacation', this.dayInfo.date, newValue);
                    console.log("Day vacation status was updated. Response status: " + response.status)
                });
        },
        addWorklog(worklog) {
            this.$emit('createWorklog', this.dayInfo.date, worklog);
        },
        removeWorklog(worklogToDelete) {
            this.$emit('deleteWorklog', this.dayInfo.date, worklogToDelete.id);
        },
        syncWorklog(worklog) {
            this.$emit('worklogSynced', this.dayInfo.date, worklog);
        }
    },
    data() {
        return {
            text: "Click to edit"
        };
    },
    props: {
        dayInfo: { type: Object, required: true, default: () => ({}) }
    },
    computed: {
        sortedWorklogs() {
            try {
                const arr = [...this.dayInfo.worklogs];
                arr.sort(function (a, b) {
                    // Map string dates to Date object
                    var timeA = Date.parse('2000-01-01 ' + a.startTime);
                    var timeB = Date.parse('2000-01-01 ' + b.startTime);

                    // Compare times
                    return timeA - timeB;
                });
                return arr;
            } catch (error) {
                return {};
            }
        },
        durationOfSynced() {
            return [...this.dayInfo.worklogs]
                .filter(wl => wl.synced)
                .map(wl => wl.durationInMinutes)
                .reduce((prev, next) => prev + next, 0)
        },
        durationOfLoggedOnly() {
            return [...this.dayInfo.worklogs]
                .filter(wl => !wl.synced)
                .map(wl => wl.durationInMinutes)
                .reduce((prev, next) => prev + next, 0)
        }
    },
});
</script>

<style>
.day-summary:empty:before {
    content: attr(placeholder);
    pointer-events: none;
    display: block;
    /* For Firefox */
    color: rgba(0, 0, 0, 0.15);
    font-style: italic;
}


@media (prefers-color-scheme: dark) {
    .modal-body {
        background-color: rgb(16, 26, 34) !important;
    }

    .day-summary:empty:before {
        color: rgba(233, 233, 233, 0.15);
    }

    .btn.btn-light {
        --bs-btn-color: #c1c1c1;
        --bs-btn-bg: #2e3541;
        --bs-btn-border-color: #2e3237;
    }
}

.modal-title:first-letter {
    text-transform: capitalize;
}
</style>
