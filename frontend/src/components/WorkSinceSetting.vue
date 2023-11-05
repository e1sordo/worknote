<template>
    <div>
        <div class="mb-3">
            <p v-if="employmentDate"> {{ $t("settings.workSince.currentValue", { currentDay: humanDate(employmentDate) }) }}</p>
            <p v-else>{{ $t("settings.workSince.default") }}</p>
        </div>
        <form @submit.prevent="updateEmploymentDate">
            <div class="input-group mb-3">
                <input v-model="newDate" type="date" class="form-control" required>
                <button type="submit" class="btn btn-primary" :disabled="updating">
                    <span v-if="updating" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                    <span v-else>{{ $t("settings.workSince.submitButton") }}</span>
                </button>
            </div>
        </form>
    </div>
</template>
  
<script lang="ts">
import api from "@/api/backend-api";
import { defineComponent } from 'vue';

export default defineComponent({
    data() {
        return {
            employmentDate: '',
            newDate: '',
            updating: false
        };
    },
    created() {
        this.fetchEmploymentDate();
    },
    methods: {
        async fetchEmploymentDate() {
            try {
                const response = await api.getSetting("WORK_SINCE");
                this.employmentDate = response.data.value || '';
                this.newDate = this.employmentDate;
            } catch (error) {
                console.error('Ошибка при получении данных:', error);
            }
        },
        async updateEmploymentDate() {
            if (this.updating) {
                return;
            }

            this.updating = true;

            try {
                const response = await api.upsertSetting("WORK_SINCE", this.newDate);

                if (response.status === 200) {
                    this.employmentDate = this.newDate;
                }
            } catch (error) {
                console.error('Ошибка при отправке данных:', error);
            } finally {
                this.updating = false;
            }
        },
        humanDate(date: string) {
            if (Number.isNaN(new Date(date).getTime())) {
                return '';
            }

            const options = {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
            } as Intl.DateTimeFormatOptions;

            const locale = this.$i18n.locale;
            const formatter = new Intl.DateTimeFormat(locale, options);
            return formatter.format(new Date(date));
        },
    },
});
</script>
