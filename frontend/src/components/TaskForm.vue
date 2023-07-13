<template>
    <form @submit.prevent="$emit('on-submit', form)">
        <div class="row">
            <div class="col-sm-3 mb-3">
                <label for="type" class="form-label">Тип</label>
                <select id="type" v-model="form.type" class="form-select">
                    <option value="" disabled>Выберите активность</option>
                    <option v-for="(metaItem, key) in taskTypeMeta" :key="key" :value="key">
                        {{ metaItem.icon }} {{ metaItem.description }}
                    </option>
                </select>
            </div>
            <div class="col-sm-2 mb-3">
                <label for="code" class="form-label">Проект</label>
                <input id="code" v-model="form.code" type="text" class="form-control" required placeholder="Код проекта" />
            </div>
            <div class="col-sm-1 mb-3">
                <label for="id" class="form-label">ID</label>
                <input id="id" v-model="form.id" type="text" class="form-control" required />
            </div>
            <div class="col-sm-6 mb-3">
                <label for="title" class="form-label">Название</label>
                <input id="title" v-model="form.title" type="text" class="form-control" required
                    placeholder="Название задачи" />
            </div>
            <div class="col-sm-12 mb-3">
                <label for="examples" class="form-label">Примеры</label>
                <textarea id="examples" v-model="form.examples" class="form-control" rows="3"
                    placeholder="Введите ключевые слова, примеры списаний, относящихся к этой задаче"></textarea>
            </div>
        </div>
        <button class="btn btn-primary" type="submit">
            {{ editing ? 'Обновить' : 'Создать' }}
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