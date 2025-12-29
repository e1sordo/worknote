<template>
    <div class="container">
        <form @submit.prevent>
            <div class="mb-3 text-start position-relative">
                <input
                    type="text"
                    id="autocomplete-input"
                    v-model="autocompleteValue"
                    class="form-control w-100"
                    autocomplete="off"
                    @keydown="onAutocompleteKeyDown"
                    @keydown.esc="showAutocomplete = false"
                    :placeholder="$t('worklog.form.task.placeholder')"
                />
                <div
                    v-if="showAutocomplete"
                    class="autocomplete-dropdown bg-body border-bottom border-start border-end border-secondary rounded-bottom"
                >
                    <ul>
                        <li
                            v-for="(suggestion, index) in autocompleteSuggestions"
                            :key="suggestion.entityId"
                            @click="selectAutocompleteSuggestion(suggestion)"
                            :class="{ 'bg-primary text-white': index === highlightedIndex }"
                        >
                            <p>
                                {{ taskTypeMeta[suggestion.type].icon }}
                                <span class="badge bg-primary" :class="{ 'mx-2': !suggestion.new }">
                                    {{ suggestion.code }}-{{ suggestion.id }}
                                </span>
                                <span v-if="suggestion.new" class="badge bg-success mx-2">New</span>
                                <span
                                    v-html="suggestion.title"
                                    :class="{ 'text-decoration-line-through': suggestion.closed }"
                                ></span>
                            </p>
                            <p>
                                <small class="text-muted" v-html="suggestion.examples"></small>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="d-flex gap-3 justify-content-between align-items-center w-100">
                <input
                    type="text"
                    id="text-input"
                    v-model="textValue"
                    class="form-control flex-grow-1"
                    :placeholder="$t('worklog.form.summary.placeholder')"
                />

                <input type="time" id="time-input" v-model="timeValue" step="900" class="form-control" />

                <input
                    type="text"
                    id="spent-input"
                    v-model="spentValue"
                    class="form-control spent-input"
                    :placeholder="$t('worklog.form.spent.placeholder')"
                    :title="$t('worklog.form.spent.title')"
                    @keydown.enter="spentValue.trim() !== '' && submitCreate()"
                />

                <button type="button" @click="submitCreate" class="btn btn-outline-primary text-nowrap">
                    {{ $t('worklog.submitButton') }}
                </button>
            </div>
        </form>
    </div>
</template>

<script lang="ts">
    import backendApi from '@/api/backend-api';
    import clientApi, { IssueResponse } from '@/api/client-api';
    import { taskTypeMeta } from '@/constants';
    import { convertTimeToMinutes } from '@/utils/convertTimeToMinutes';
    import { SetupContext, computed, ref, watch, defineComponent } from 'vue';
    import { useI18n } from 'vue-i18n';

    interface TaskSuggestion {
        entityId: number;
        code: string;
        id: number;
        title: string;
        defaultValue: string;
        type: string;
        examples: string;
        closed: boolean;
        new: boolean;
    }

    export default defineComponent({
        name: 'WorklogForm',
        props: {
            date: {
                type: String,
                required: true,
                default: () => ''
            }
        },
        setup(props: { date: string }, context: SetupContext) {
            const { t } = useI18n();
            const textValue = ref('');
            const timeValue = ref('');
            const spentValue = ref('');
            const autocompleteValue = ref('');
            const selectedValue = ref('');
            const showAutocomplete = ref(false);
            const autocompleteSuggestions = ref<TaskSuggestion[]>([]);

            const highlightedIndex = ref(-1);
            const onAutocompleteKeyDown = (e: KeyboardEvent) => {
                if (!showAutocomplete.value || autocompleteSuggestions.value.length === 0) {
                    return;
                }

                if (e.key === 'ArrowDown') {
                    e.preventDefault();
                    highlightedIndex.value = (highlightedIndex.value + 1) % autocompleteSuggestions.value.length;
                } else if (e.key === 'ArrowUp') {
                    e.preventDefault();
                    highlightedIndex.value =
                        (highlightedIndex.value - 1 + autocompleteSuggestions.value.length) %
                        autocompleteSuggestions.value.length;
                } else if (e.key === 'Enter') {
                    if (highlightedIndex.value >= 0 && highlightedIndex.value < autocompleteSuggestions.value.length) {
                        e.preventDefault();
                        selectAutocompleteSuggestion(autocompleteSuggestions.value[highlightedIndex.value]);
                    }
                }
            };

            let timer: NodeJS.Timeout | null = null;

            watch(autocompleteValue, async newValue => {
                if (timer) {
                    clearTimeout(timer);
                }

                timer = setTimeout(async () => {
                    if (selectedValue.value == autocompleteValue.value) {
                        showAutocomplete.value = false;
                    } else {
                        if (newValue.trim().length === 0) {
                            showAutocomplete.value = false;
                            return;
                        }
                        try {
                            const response = await backendApi.searchTasks(newValue);
                            autocompleteSuggestions.value = response.data.map(task => {
                                return {
                                    entityId: task.entityId,
                                    code: task.code,
                                    id: task.id,
                                    title: task.title,
                                    defaultValue: task.defaultValue,
                                    type: task.type,
                                    examples: task.examples,
                                    closed: task.closed,
                                    new: false
                                };
                            });

                            // try to find in Jira
                            if (autocompleteSuggestions.value.length == 0) {
                                const jiraKeyNumberMatch = newValue.trim().match(/^\d*(\.\d+)?$/);
                                if (jiraKeyNumberMatch && jiraKeyNumberMatch[0]) {
                                    try {
                                        const activeProjectResponse = await backendApi.activeProjectCode();
                                        const activeProjectCode = activeProjectResponse.data;

                                        const jiraKey = activeProjectCode + '-' + jiraKeyNumberMatch[0];

                                        const response = await clientApi.getIssue(jiraKey);
                                        if (response.data) {
                                            const jiraIssue = response.data;
                                            autocompleteSuggestions.value.push(
                                                mapJiraTaskToSuggestion(jiraKey, jiraIssue)
                                            );
                                        }
                                    } catch (error) {
                                        console.error(error);
                                    }
                                }

                                const jiraKeyMatch = newValue.match(/\(([^)]+)\)/);
                                if (jiraKeyMatch && jiraKeyMatch[1]) {
                                    try {
                                        const jiraKey = jiraKeyMatch[1];

                                        const response = await clientApi.getIssue(jiraKey);
                                        if (response.data) {
                                            const jiraIssue = response.data;
                                            autocompleteSuggestions.value.push(
                                                mapJiraTaskToSuggestion(jiraKey, jiraIssue)
                                            );
                                        }
                                    } catch (error) {
                                        console.error(error);
                                    }
                                }
                            }

                            if (autocompleteSuggestions.value.length > 0) {
                                showAutocomplete.value = true;
                            }
                        } catch (error) {
                            console.error(error);
                            showAutocomplete.value = false;
                        }
                    }
                }, 500);
            });

            const mapJiraTaskToSuggestion = (jiraKey: string, jiraIssue: IssueResponse) => {
                const codeWithId = jiraKey.split('-');
                return {
                    entityId: jiraIssue.id,
                    code: codeWithId[0].toUpperCase(),
                    id: parseInt(codeWithId[1]),
                    title: jiraIssue.fields.summary,
                    defaultValue: 'Разработка',
                    type: 'DEVELOPMENT',
                    examples: '',
                    closed: false,
                    new: true
                } as TaskSuggestion;
            };

            const selectAutocompleteSuggestion = (suggestion: TaskSuggestion) => {
                const suggestionTitleWithoutHtmlTags = suggestion.title.replace(/<[^>]*>/g, '');
                const textSuggestion = `(${suggestion.code}-${suggestion.id}) ${suggestionTitleWithoutHtmlTags}`;
                autocompleteValue.value = textSuggestion;
                textValue.value = suggestion.defaultValue;
                selectedValue.value = textSuggestion;
                showAutocomplete.value = false;

                highlightedIndex.value = -1;

                const textInputElement = document.getElementById('text-input');
                if (textInputElement) {
                    textInputElement.focus();
                }
            };

            const submitCreate = () => {
                try {
                    backendApi
                        .createWorklog(
                            props.date,
                            timeValue.value,
                            convertTimeToMinutes(spentValue.value),
                            textValue.value,
                            autocompleteValue.value
                        )
                        .then(response => {
                            context.emit('createWorklog', response.data);
                            textValue.value = '';
                            timeValue.value = '';
                            spentValue.value = '';
                            selectedValue.value = '';
                            autocompleteValue.value = '';
                            autocompleteSuggestions.value = [];
                        });
                } catch (ex) {
                    spentValue.value = '';
                }

                // focus to enter new worklog
                const autocompleteInputElement = document.getElementById('autocomplete-input');
                if (autocompleteInputElement) {
                    autocompleteInputElement.focus();
                }
            };

            const formattedTimeValue = computed(() => {
                if (timeValue.value) {
                    const [hours, minutes] = timeValue.value.split(':');
                    return `${hours}:${minutes}`;
                }
                return '';
            });

            return {
                t,
                taskTypeMeta,
                textValue,
                timeValue,
                spentValue,
                autocompleteValue,
                showAutocomplete,
                selectedValue,
                autocompleteSuggestions,
                selectAutocompleteSuggestion,
                submitCreate,
                formattedTimeValue,
                highlightedIndex,
                onAutocompleteKeyDown
            };
        }
    });
</script>

<style scoped>
    .autocomplete-dropdown {
        text-align: left;
        position: absolute;
        z-index: 1;
        padding: 4px;
        width: 100%;
        max-height: 340px;
        overflow-y: auto;
    }

    .autocomplete-dropdown ul {
        list-style: none;
        margin: 0;
        padding: 0;
    }

    .autocomplete-dropdown li {
        cursor: pointer;
        padding: 6px;
    }

    .autocomplete-dropdown li:hover {
        background-color: #f4f4f445;
    }

    .autocomplete-dropdown li p {
        margin-bottom: 0;
    }

    .form-control {
        width: auto;
    }

    .spent-input {
        width: 12%;
    }
</style>
