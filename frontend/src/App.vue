<template>
  <div spellcheck="false" class="brand" contenteditable="true">&lt;WorkNote&gt;</div>
  <nav>
    <router-link to="/">{{ $t("navbar.home") }}</router-link> |
    <router-link to="/overview">{{ $t("navbar.overview") }}</router-link> |
    <router-link to="/weekly">{{ $t("navbar.weekly") }}</router-link> |
    <router-link to="/settings">{{ $t("navbar.settings") }}</router-link> |
    <a href="/logout" class="link-secondary">{{ $t("navbar.logout") }}</a>
  </nav>
  <router-view />
</template>

<style>
#app {
  /* font-family: Avenir, Helvetica, Arial, sans-serif; */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

@media (prefers-color-scheme: light) {
  body {
    background-color: rgba(30, 32, 32, 0.03) !important;
  }
}

.brand {
  margin-top: 20px;
  font-size: 54px;
  font-weight: 800;
  outline: none;
  text-transform: uppercase;
  background: linear-gradient(135deg, #cf3566 0%, #de005d 25%, #a11dba 50%, #de005d 75%, #cf3566 100%);
  background-size: 400%;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: animate 20s linear infinite;
}

@keyframes animate {
  to {
    background-position: 400%;
  }
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: #777;
}

nav a.router-link-exact-active {
  color: #b94242;
}
</style>
