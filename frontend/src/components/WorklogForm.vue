<template>
    <div class="container">
        <form @submit.prevent>
            <div class="mb-3 text-start position-relative">
                <input type="text" id="autocomplete-input" v-model="autocompleteValue" class="form-control w-100"
                    autocomplete="off" @keydown.esc="showAutocomplete = false"
                    :placeholder="$t('worklog.form.task.placeholder')">
                <div v-if="showAutocomplete"
                    class="autocomplete-dropdown bg-body border-bottom border-start border-end border-secondary rounded-bottom">
                    <ul>
                        <li v-for="suggestion in autocompleteSuggestions" :key="suggestion.entityId"
                            @click="selectAutocompleteSuggestion(suggestion)">
                            <p>
                                {{ taskTypeMeta[suggestion.type].icon }}
                                <span class="badge bg-primary mx-2">{{ suggestion.code }}-{{ suggestion.id }}</span>
                                <span v-html="suggestion.title"></span>
                            </p>
                            <p>
                                <small class="text-muted" v-html="suggestion.examples"></small>
                            </p>
                        </li>
                    </ul>
                </div>
            </div>
            <div class="d-flex gap-3 justify-content-between align-items-center w-100">
                <input type="text" id="text-input" v-model="textValue" class="form-control flex-grow-1"
                    :placeholder="$t('worklog.form.summary.placeholder')">

                <input type="time" id="time-input" v-model="timeValue" step="900" class="form-control">

                <input type="text" id="spent-input" v-model="spentValue" class="form-control spent-input"
                    :placeholder="$t('worklog.form.spent.placeholder')" :title="$t('worklog.form.spent.title')">

                <button type="button" @click="submitCreate" class="btn btn-outline-primary text-nowrap">
                    {{ $t('worklog.submitButton') }}
                </button>
            </div>

        </form>
    </div>
</template>
  
<script lang="ts">
import api from "@/api/backend-api";
import { taskTypeMeta } from '@/constants';
import { convertTimeToMinutes } from '@/utils/convertTimeToMinutes';
import { SetupContext, computed, ref, watch } from 'vue';

interface TaskSuggestion {
    entityId: number;
    code: string;
    id: number;
    title: string;
    type: string;
    examples: string;
}

export default {
    name: 'WorklogForm',
    props: {
        date: {
            type: String,
            required: true,
            default: () => ('')
        }
    },
    setup(props: { date: string }, context: SetupContext) {
        const textValue = ref('');
        const timeValue = ref('');
        const spentValue = ref('');
        const autocompleteValue = ref('');
        const selectedValue = ref('');
        const showAutocomplete = ref(false);
        const autocompleteSuggestions = ref<TaskSuggestion[]>([]);

        let timer: NodeJS.Timeout | null = null;

        watch(autocompleteValue, async (newValue) => {
            if (timer) {
                clearTimeout(timer);
            }

            timer = setTimeout(async () => {
                if (selectedValue.value == autocompleteValue.value) {
                    showAutocomplete.value = false;
                } else {
                    try {
                        const response = await api.searchTasks(newValue);
                        autocompleteSuggestions.value = response.data.map(task => {
                            return {
                                entityId: task.entityId,
                                code: task.code,
                                id: task.id,
                                title: task.title,
                                type: task.type,
                                examples: task.examples
                            }
                        });

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

        const selectAutocompleteSuggestion = (suggestion: TaskSuggestion) => {
            const suggestionTitleWithoutHtmlTags = suggestion.title.replace(/<[^>]*>/g, '')
            const textSuggestion = `(${suggestion.code}-${suggestion.id}) ${suggestionTitleWithoutHtmlTags}`
            autocompleteValue.value = textSuggestion;
            selectedValue.value = textSuggestion;
            showAutocomplete.value = false;

            const textInputElement = document.getElementById("text-input");
            if (textInputElement) {
                textInputElement.focus();
            }
        };

        const submitCreate = () => {
            try {
                api.createWorklog(props.date, timeValue.value, convertTimeToMinutes(spentValue.value), textValue.value, autocompleteValue.value)
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
        };

        const formattedTimeValue = computed(() => {
            if (timeValue.value) {
                const [hours, minutes] = timeValue.value.split(':');
                return `${hours}:${minutes}`;
            }
            return '';
        });

        return {
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
            formattedTimeValue
        };
    }
};
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
