<template>
    <form @submit.prevent="$emit('on-submit', form)">
        <div class="row">
            <div class="col-sm-2 mb-3">
                <label for="type" class="form-label">{{ $t("settings.tasks.form.type.label") }}</label>
                <select id="type" v-model="form.type" class="form-select">
                    <option value="" disabled>{{ $t("settings.tasks.form.type.placeholder") }}</option>
                    <option v-for="(metaItem, key) in taskTypeMeta" :key="key" :value="key">
                        {{ metaItem.icon }} {{ $t("taskTypes." + key ) }}
                    </option>
                </select>
            </div>
            <div class="col-sm-1 mb-3">
                <label for="code" class="form-label">{{ $t("settings.tasks.form.projectCode.label") }}</label>
                <input id="code" v-model="form.code" type="text" class="form-control" required
                    :placeholder="$t('settings.tasks.form.projectCode.placeholder')" />
            </div>
            <div class="col-sm-1 mb-3">
                <label for="id" class="form-label">{{ $t("settings.tasks.form.id.label") }}</label>
                <input id="id" v-model="form.id" type="text" class="form-control" required />
            </div>
            <div class="col-sm-4 mb-3">
                <label for="title" class="form-label">{{ $t("settings.tasks.form.title.label") }}</label>
                <input id="title" v-model="form.title" type="text" class="form-control" required
                    :placeholder="$t('settings.tasks.form.title.placeholder')" />
            </div>
            <div class="col-sm-4 mb-3">
                <label for="defaultValue" class="form-label">{{ $t("settings.tasks.form.defaultValue.label") }}</label>
                <input id="defaultValue" v-model="form.defaultValue" type="text" class="form-control" required
                    :placeholder="$t('settings.tasks.form.defaultValue.placeholder')" />
            </div>
            <div class="col-sm-12 mb-3">
                <label for="examples" class="form-label">{{ $t("settings.tasks.form.examples.label") }}</label>
                <textarea id="examples" v-model="form.examples" class="form-control" rows="3"
                :placeholder="$t('settings.tasks.form.examples.placeholder')"></textarea>
            </div>
        </div>
        <button class="btn btn-primary" type="submit">
            {{ editing ? $t('settings.tasks.updateButton') : $t('settings.tasks.createButton') }}
        </button>
    </form>
</template>
  
<script lang="ts">
import { defineComponent, ref, reactive, watch } from 'vue';
import { Task } from "@/api/backend-api";
import { taskTypeMeta } from "@/constants";

export default defineComponent({
    name: 'TaskForm',
    props: {
        task: Object as () => Task
    },
    setup(props) {
        const form = reactive({
            code: '',
            id: 0,
            type: '',
            title: '',
            defaultValue: '',
            examples: ''
        });

        let editing = ref(false);

        // Наблюдатель для отслеживания изменений пропса "task"
        watch(
            () => props.task,
            (newTask) => {
                if (newTask) {
                    form.code = newTask.code;
                    form.id = newTask.id;
                    form.type = newTask.type;
                    form.title = newTask.title;
                    form.defaultValue = newTask.defaultValue;
                    form.examples = newTask.examples;
                    editing.value = newTask.entityId !== undefined;
                }
            }
        );

        return {
            form, editing, taskTypeMeta
        }
    }
});
</script>