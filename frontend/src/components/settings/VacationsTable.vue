<template>
    <div>
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h5>{{ $t('settings.vacations.title') }}</h5>
            <button class="btn btn-primary btn-sm" @click="openModal()">
                {{ $t('settings.vacations.create') }}
            </button>
        </div>

        <table class="table table-bordered table-hover align-middle">
            <thead class="table-light">
                <tr>
                    <th>#</th>
                    <th>{{ $t('settings.vacations.startDate') }}</th>
                    <th>{{ $t('settings.vacations.endDate') }}</th>
                    <th>{{ $t('settings.vacations.totalDays') }}</th>
                    <th>{{ $t('settings.vacations.relativeTime') }}</th>
                    <th>{{ $t('settings.vacations.confirmed') }}</th>
                    <th>{{ $t('settings.vacations.synced') }}</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="vac in vacations" :key="vac.id">
                    <td>{{ vac.id }}</td>
                    <td>{{ formatDate(vac.startDate) }}</td>
                    <td>{{ formatDate(vac.endDate) }}</td>
                    <td>{{ calcDays(vac.startDate, vac.endDate) }}</td>
                    <td>{{ relativeTime(vac.startDate, vac.endDate) }}</td>
                    <td>
                        <span class="badge" :class="vac.confirmed ? 'bg-success' : 'bg-warning text-dark'">
                            {{
                                vac.confirmed
                                    ? $t('settings.vacations.confirmed')
                                    : $t('settings.vacations.notConfirmed')
                            }}
                        </span>
                    </td>
                    <td>
                        <span class="badge" :class="vac.synced ? 'bg-success' : 'bg-secondary'">
                            {{ vac.synced ? $t('settings.vacations.synced') : $t('settings.vacations.notSynced') }}
                        </span>
                    </td>
                    <td class="d-flex gap-2">
                        <button class="btn btn-sm btn-outline-primary" @click="openModal(vac)">
                            {{ $t('settings.vacations.edit') }}
                        </button>
                        <button v-if="!vac.synced" class="btn btn-sm btn-outline-danger" @click="deleteVacation(vac)">
                            {{ $t('settings.vacations.delete') }}
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>

        <!-- Modal -->
        <div class="modal fade" ref="vacationModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            {{
                                editingVacation?.id
                                    ? $t('settings.vacations.editVacation')
                                    : $t('settings.vacations.createVacation')
                            }}
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <form @submit.prevent="saveVacation">
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label">{{ $t('settings.vacations.startDate') }}</label>
                                <input
                                    type="date"
                                    class="form-control"
                                    v-model="form.startDate"
                                    @change="syncEndDate('start')"
                                    required
                                />
                            </div>
                            <div class="mb-3">
                                <label class="form-label">{{ $t('settings.vacations.endDate') }}</label>
                                <input
                                    type="date"
                                    class="form-control"
                                    v-model="form.endDate"
                                    @change="syncEndDate('end')"
                                    required
                                />
                            </div>
                            <div class="mb-3">
                                <label class="form-label">{{ $t('settings.vacations.totalDays') }}</label>
                                <div class="form-control-plaintext">
                                    {{ calcDays(form.startDate, form.endDate) }}
                                </div>
                            </div>
                            <div class="form-check mb-2">
                                <input
                                    class="form-check-input"
                                    type="checkbox"
                                    v-model="form.confirmed"
                                    id="confirmedCheck"
                                />
                                <label class="form-check-label" for="confirmedCheck">
                                    {{ $t('settings.vacations.confirmed') }}
                                </label>
                            </div>
                            <div class="form-check">
                                <input
                                    class="form-check-input"
                                    type="checkbox"
                                    v-model="form.synced"
                                    id="syncedCheck"
                                />
                                <label class="form-check-label" for="syncedCheck">
                                    {{ $t('settings.vacations.synced') }}
                                </label>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                                {{ $t('settings.vacations.cancel') }}
                            </button>
                            <button type="submit" class="btn btn-primary">
                                {{ $t('settings.vacations.save') }}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent, ref, onMounted } from 'vue';
    import api, { Vacation } from '@/api/backend-api';
    import * as bootstrap from 'bootstrap';
    import moment from 'moment';
    import { useI18n } from 'vue-i18n';

    export default defineComponent({
        name: 'VacationsTable',
        setup() {
            const { t, locale } = useI18n();

            const vacations = ref<Vacation[]>([]);
            const vacationModal = ref<HTMLDivElement | null>(null);
            const modalInstance = ref<bootstrap.Modal | null>(null);

            const editingVacation = ref<Vacation | null>(null);
            const dateDelta = ref<number>(0); // длительность для синхронизации дат
            const form = ref<Vacation>({
                id: 0,
                startDate: '',
                endDate: '',
                totalDays: 0,
                confirmed: false,
                synced: false
            });

            const fetchVacations = () => {
                api.vacations().then(res => {
                    vacations.value = res.data;
                });
            };

            const openModal = (vac?: Vacation) => {
                if (vac) {
                    editingVacation.value = { ...vac };
                    form.value = { ...vac };
                    dateDelta.value = calcDays(form.value.startDate, form.value.endDate);
                } else {
                    const today = moment().format('YYYY-MM-DD');
                    const plus5 = moment(today).add(4, 'days').format('YYYY-MM-DD');
                    editingVacation.value = null;
                    form.value = {
                        id: 0,
                        startDate: today,
                        endDate: plus5,
                        totalDays: 0,
                        confirmed: false,
                        synced: false
                    };
                    dateDelta.value = 5;
                }
                modalInstance.value?.show();
            };

            const saveVacation = () => {
                api.upsertVacation(form.value).then(() => {
                    fetchVacations();
                    modalInstance.value?.hide();
                });
            };

            const deleteVacation = (vac: Vacation) => {
                if (confirm(t('settings.vacations.deleteConfirm') as string)) {
                    api.deleteVacation(vac.id).then(() => {
                        fetchVacations();
                    });
                }
            };

            const calcDays = (start: string, end: string) => {
                if (!start || !end) return 0;
                return moment(end).diff(moment(start), 'days') + 1;
            };

            const syncEndDate = (changed: 'start' | 'end') => {
                if (changed === 'start' && form.value.startDate) {
                    form.value.endDate = moment(form.value.startDate)
                        .add(dateDelta.value - 1, 'days')
                        .format('YYYY-MM-DD');
                }
                dateDelta.value = calcDays(form.value.startDate, form.value.endDate);
            };

            const formatDate = (date: string) => {
                return moment(date).locale(locale.value).format('D MMMM YYYY [года]');
            };

            const relativeTime = (start: string, end: string) => {
                const now = moment();
                if (now.isBefore(start)) {
                    return t('settings.vacations.startsIn', { time: moment(start).fromNow(true) });
                } else if (now.isAfter(end)) {
                    return t('settings.vacations.endedAgo', { time: moment(end).fromNow(true) });
                } else {
                    return t('settings.vacations.nowOnVacation');
                }
            };

            onMounted(() => {
                fetchVacations();
                if (vacationModal.value) {
                    modalInstance.value = new bootstrap.Modal(vacationModal.value);
                }
            });

            return {
                vacations,
                editingVacation,
                vacationModal,
                form,
                openModal,
                saveVacation,
                deleteVacation,
                calcDays,
                syncEndDate,
                formatDate,
                relativeTime
            };
        }
    });
</script>
