<template>
  <router-view></router-view>
</template>

<style>
/* 全局锁死滚动条，重置样式 */
html, body, #app {
  margin: 0; padding: 0; width: 100%; height: 100%;
  overflow: hidden; 
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
* { box-sizing: inherit; }
</style>