<template>
  <div class="home">
    <div class="m-4 text-start">
      <h2>{{ $t("settings.tasks.title") }}</h2>
      <task-table />
    </div>

    <hr />

    <div class="m-4 text-start">
      <h2>{{ $t("settings.language.title") }}</h2>
      <locale-switcher />
    </div>

    <hr />

    <div class="m-4 text-start">
      <h2>{{ $t("settings.workSince.title") }}</h2>
      <work-since-setting />
    </div>

    <hr />

    <div class="m-4 text-start">
      <h2>{{ $t("settings.jira.title") }}</h2>
      <form @submit.prevent="saveJiraSettings">
        <div class="row">
          <div class="col-sm-6 mb-3">
            <label for="proxyUrlInput" class="form-label">{{ $t("settings.jira.form.proxyUrl.label") }}</label>
            <input type="text" id="proxyUrlInput" class="form-control" v-model="proxyUrl">
          </div>
          <div class="col-sm-6 mb-3">
            <label for="serverUrlInput" class="form-label">{{ $t("settings.jira.form.serverUrl.label") }}</label>
            <input type="text" id="serverUrlInput" class="form-control" v-model="serverUrl">
          </div>
          <div class="col-sm-6 mb-3">
            <label for="usernameInput" class="form-label">{{ $t("settings.jira.form.login.label") }}</label>
            <input type="text" id="usernameInput" class="form-control" v-model="username">
          </div>
          <div class="col-sm-6 mb-3">
            <label for="passwordInput" class="form-label">{{ $t("settings.jira.form.password.label") }}</label>
            <input type="password" id="passwordInput" class="form-control" v-model="password">
          </div>
        </div>
        <button type="submit" class="btn btn-primary">{{ $t("settings.jira.submitButton") }}</button>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import TaskTable from '@/components/TaskTable.vue';
import LocaleSwitcher from '@/components/LocaleSwitcher.vue';
import WorkSinceSetting from '@/components/WorkSinceSetting.vue';

// interface State {
//   msg: string;
//   backendResponse: string;
//   errors: AxiosError[]
// }

const jiraSettignsKey = "settings:jira";

export default defineComponent({
  name: 'SettingsView',
  components: {
    TaskTable, LocaleSwitcher, WorkSinceSetting
  },

  // data: (): State => {
  //   return {
  //     msg: 'HowTo call REST-Services:',
  //     backendResponse: '',
  //     errors: []
  //   }
  // },

  data() {
    return {
      proxyUrl: '',
      serverUrl: '',
      username: '',
      password: '',
    };
  },
  created() {
    this.loadJiraSettings();
  },

  methods: {
    // Fetches posts when the component is created.
    // callHelloApi() {
    //   api.weeks().then(response => {
    //     // this.backendResponse = response.data;
    //     console.log(response.data)
    //   })
    //     .catch((error: AxiosError) => {
    //       this.errors.push(error)
    //     })
    // },
    loadJiraSettings() {
      const savedSettings = localStorage.getItem(jiraSettignsKey);
      if (savedSettings) {
        const { proxyUrl, serverUrl, username, password } = JSON.parse(savedSettings);
        this.proxyUrl = proxyUrl;
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
      }
    },
    saveJiraSettings() {
      const settings = {
        proxyUrl: this.proxyUrl,
        serverUrl: this.serverUrl,
        username: this.username,
        password: this.password,
      };
      localStorage.setItem(jiraSettignsKey, JSON.stringify(settings));
      alert('Настройки сохранены!');
    }
  }
});
</script>