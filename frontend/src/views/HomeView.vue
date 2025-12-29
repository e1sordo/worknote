<template>
    <div class="home">
        <img alt="Vue logo" src="../assets/logo.png" />

        <button @click="callHelloApi()">CALL Spring Boot REST backend service</button>

        <h4>Backend response: {{ backendResponse }}</h4>

        <HelloWorld msg="Welcome to Your Vue.js + TypeScript App" />
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue';
    import HelloWorld from '@/components/HelloWorld.vue'; // @ is an alias to /src
    import api from '../api/backend-api';
    import { AxiosError } from 'axios';

    interface State {
        msg: string;
        backendResponse: string;
        errors: AxiosError[];
    }

    export default defineComponent({
        name: 'HomeView',
        components: {
            HelloWorld
        },

        data: (): State => {
            return {
                msg: 'HowTo call REST-Services:',
                backendResponse: '',
                errors: []
            };
        },
        methods: {
            // Fetches posts when the component is created.
            callHelloApi() {
                api.weeks(4)
                    .then(response => {
                        // this.backendResponse = response.data;
                        console.log(response.data);
                    })
                    .catch((error: AxiosError) => {
                        this.errors.push(error);
                    });
            }
        }
    });
</script>
