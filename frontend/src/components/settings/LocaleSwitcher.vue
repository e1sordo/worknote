<template>
    <select v-model="$i18n.locale" @change="updateLocale()" class="form-select">
        <option v-for="(locale, i) in locales" :key="`locale-${i}`" :value="locale">
            {{ locale }}
        </option>
    </select>
</template>
  
<script>
export default {
    name: "LocaleSwitcher",
    data() {
        return { locales: ["ru", "en", "es"] };
    },
    methods: {
        updateLocale() {
            localStorage.setItem("locale", this.$i18n.locale);
        },
    }
};
</script>
  