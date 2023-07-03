<template>
    <div class="container">
        <form @submit.prevent>
            <div class="row mb-3 text-start">
                <label for="autocomplete-input" class="col-sm-2 col-form-label">Задача в Jira</label>
                <div class="col-sm-10">
                    <input type="text" id="autocomplete-input" v-model="autocompleteValue" class="form-control"
                        autocomplete="off" @keydown.esc="showAutocomplete = false">
                    <div v-if="showAutocomplete" class="autocomplete-dropdown">
                        <ul>
                            <li v-for="suggestion in autocompleteSuggestions" :key="suggestion"
                                @click="selectAutocompleteSuggestion(suggestion)">
                                {{ suggestion }}
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-5 mb-3">
                    <input type="text" id="text-input" placeholder="Что было сделано" v-model="textValue"
                        class="form-control">
                </div>
                <div class="col-sm-2 mb-3">
                    <input type="time" id="time-input" v-model="timeValue" step="900" class="form-control">
                </div>
                <div class="col-sm-3 mb-3">
                    <input type="number" id="number-input" placeholder="Затрачено минут" v-model.number="numberValue"
                        class="form-control">
                </div>
                <div class="col-sm-2 mb-3">
                    <button type="button" @click="submitCreate" class="btn btn-primary w-100">Добавить</button>
                </div>
            </div>

        </form>
    </div>
</template>
  
<script lang="ts">
import { computed, ref, watch, SetupContext } from 'vue';
import api from "../api/backend-api";

export default {
    name: 'WorklogForm',
    props: {
        date: {
            type: Date,
            required: true,
            default: () => (new Date())
        }
    },
    setup(props: { date: Date }, context: SetupContext) {
        const textValue = ref('');
        const numberValue = ref();
        const timeValue = ref('');
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
            api.createWorklog(props.date, timeValue.value, numberValue.value, textValue.value, autocompleteValue.value)
                .then(response => {
                    context.emit('createWorklog', response.data);
                    textValue.value = '';
                    timeValue.value = '';
                    numberValue.value = 0;
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
            numberValue,
            timeValue,
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
</style>