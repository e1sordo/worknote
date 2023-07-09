<template>
    <div class="container">
        <form @submit.prevent>
            <div class="mb-3 text-start">
                <input type="text" id="autocomplete-input" v-model="autocompleteValue" class="form-control w-100"
                    autocomplete="off" @keydown.esc="showAutocomplete = false"
                    placeholder="Начните описывать задачу (её номер, название, тип)">
                <div v-if="showAutocomplete" class="autocomplete-dropdown">
                    <ul>
                        <li v-for="suggestion in autocompleteSuggestions" :key="suggestion"
                            @click="selectAutocompleteSuggestion(suggestion)">
                            {{ suggestion }}
                        </li>
                    </ul>
                </div>
            </div>
            <div class="d-flex justify-content-between align-items-center w-100 mb-3">
                <input type="text" id="text-input" placeholder="Что было сделано" v-model="textValue"
                    class="form-control w-50">

                <input type="time" id="time-input" v-model="timeValue" step="900" class="form-control">

                <input type="text" id="spent-input" placeholder="Затрачено" v-model="spentValue"
                    class="form-control spent-input"
                    title="Формат '2ч 45м'. Допускается вводить числа: если меньше 5 -- это часы, если больше -- минуты">

                <button type="button" @click="submitCreate" class="btn btn-primary w-25">Добавить</button>
            </div>

        </form>
    </div>
</template>
  
<script lang="ts">
import { computed, ref, watch, SetupContext } from 'vue';
import api from "@/api/backend-api";
import { convertTimeToMinutes } from '@/constants';

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
        const autocompleteSuggestions = ref<string[]>([]);

        watch(autocompleteValue, async (newValue) => {
            if (selectedValue.value == autocompleteValue.value) {
                showAutocomplete.value = false;
            } else {
                try {
                    const response = await api.tasks(newValue);
                    autocompleteSuggestions.value = response.data.map(task => `(${task.code}-${task.id}) ${task.title}`);

                    if (autocompleteSuggestions.value.length > 0) {
                        showAutocomplete.value = true;
                    }
                } catch (error) {
                    console.error(error);
                    showAutocomplete.value = false;
                }
            }
        });

        const selectAutocompleteSuggestion = (suggestion: string) => {
            autocompleteValue.value = suggestion;
            selectedValue.value = suggestion;
            showAutocomplete.value = false;
        };

        const submitCreate = () => {
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
        };

        const formattedTimeValue = computed(() => {
            if (timeValue.value) {
                const [hours, minutes] = timeValue.value.split(':');
                return `${hours}:${minutes}`;
            }
            return '';
        });

        return {
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
    background-color: #fff;
    border: 1px solid #ddd;
    padding: 4px;
    width: 80%;
    max-height: 200px;
    overflow-y: auto;
}

.autocomplete-dropdown ul {
    list-style: none;
    margin: 0;
    padding: 0;
}

.autocomplete-dropdown li {
    cursor: pointer;
    padding: 2px 4px;
}

.autocomplete-dropdown li:hover {
    background-color: #f4f4f4;
}

.form-control {
    width: auto;
}

.spent-input {
    width: 12%;
}
</style>