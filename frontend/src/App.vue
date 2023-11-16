<template>
  <div spellcheck="false" class="brand" contenteditable="true">&lt;WorkNote&gt;</div>
  <nav>
    <router-link to="/">{{ $t("navbar.home") }}</router-link> |
    <router-link to="/overview">{{ $t("navbar.overview") }}</router-link> |
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
  background: linear-gradient(135deg, #355ccf 0%, #00aede 25%, #1dba41 50%, #00aede 75%, #355ccf 100%);
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
  color: #42b983;
}
</style>
