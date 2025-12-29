<template>
    <div>
        <!-- <task-form :task="editedTask" @on-submit="upsertTask" class="mb-3" /> -->

        <ol class="list-group list-group-numbered w-50">
            <li
                v-for="project in projects"
                :key="project.shortCode"
                class="list-group-item d-flex justify-content-between align-items-start"
            >
                <div class="ms-2 me-auto">
                    <div class="fw-bold">{{ project.name }}</div>
                    {{ project.code }} ({{ project.shortCode }})
                    <button
                        type="button"
                        class="btn btn-outline-light btn-sm mx-2"
                        @click="editProject(project)"
                    >
                        Edit
                    </button>
                </div>
                <span
                    type="button"
                    @click="openCloseProject(project)"
                    :class="{
                        'bg-primary': project.active,
                        'bg-black': !project.active,
                    }"
                    class="badge bg-primary rounded-pill"
                >
                    {{ project.active ? "Active" : "Disabled" }}
                </span>
            </li>
        </ol>
    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import api, { Project } from "@/api/backend-api";

export default defineComponent({
    name: "ProjectsTable",
    data() {
        return {
            projects: [] as Project[],
            editedProject: {} as Project,
        };
    },
    methods: {
        async fetchProjects() {
            try {
                const response = await api.projects();
                this.projects = response.data;
            } catch (error) {
                console.error(error);
            }
        },
        editProject(project: Project) {
            console.log("Edit project ", project.code);
            this.editedProject = { ...project };
        },
        openCloseProject(project: Project) {
            console.log("Open/close project ", project.code);
            project.active = !project.active;
            this.upsertProject(project);
        },
        upsertProject(projectForm: Project) {
            api.upsertProject(projectForm).then((response) => {
                console.log(
                    "Project was upserted. Response status: " + response.status
                );
                this.editedProject = {} as Project;
                const project = response.data;
                var foundIndex = this.projects.findIndex(
                    (item) => item.code == project.code
                );
                if (foundIndex == -1) {
                    this.projects = [
                        { ...project } as Project,
                        ...this.projects,
                    ];
                }
            });
        },
        humanDateTime(dateTime: string) {
            if (Number.isNaN(new Date(dateTime).getTime())) {
                return "";
            }

            const options = {
                hour: "numeric",
                minute: "numeric",
                year: "numeric",
                month: "long",
                day: "numeric",
            } as Intl.DateTimeFormatOptions;

            // @ts-ignore
            const locale = this.$i18n.locale;
            // @ts-ignore
            const formatter = new Intl.DateTimeFormat(locale, options);
            return formatter.format(new Date(dateTime));
        },
        // async deleteProject(projectId: number) {
        //     // Логика удаления проекта
        //     console.log(projectId);
        // },
    },
    mounted() {
        this.fetchProjects();
    },
});
</script>

<style>
.middle-vertical-children > * {
    vertical-align: middle;
}
</style>
