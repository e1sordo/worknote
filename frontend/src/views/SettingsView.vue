<template>
  <div class="home">
    <div class="m-4 text-start">
      <h2>Настройки Jira</h2>
      <form @submit.prevent="saveSettings">
        <div class="mb-3">
          <label for="proxyUrlInput" class="form-label">URL рабочего прокси</label>
          <input type="text" id="proxyUrlInput" class="form-control" v-model="proxyUrl">
        </div>
        <div class="mb-3">
          <label for="serverUrlInput" class="form-label">URL сервера Jira</label>
          <input type="text" id="serverUrlInput" class="form-control" v-model="serverUrl">
        </div>
        <div class="mb-3">
          <label for="usernameInput" class="form-label">Логин</label>
          <input type="text" id="usernameInput" class="form-control" v-model="username">
        </div>
        <div class="mb-3">
          <label for="passwordInput" class="form-label">Пароль</label>
          <input type="password" id="passwordInput" class="form-control" v-model="password">
        </div>
        <button type="submit" class="btn btn-primary">Сохранить</button>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

// interface State {
//   msg: string;
//   backendResponse: string;
//   errors: AxiosError[]
// }

const jiraSettignsKey = "settings:jira";

export default defineComponent({
  name: 'HomeView',
  components: {
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
    this.loadSettings();
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
    loadSettings() {
      const savedSettings = localStorage.getItem(jiraSettignsKey);
      if (savedSettings) {
        const { proxyUrl, serverUrl, username, password } = JSON.parse(savedSettings);
        this.proxyUrl = proxyUrl;
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
      }
    },
    saveSettings() {
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