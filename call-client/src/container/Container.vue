<template>
  <div class="app">
    <el-container>
      <el-aside class="app-side app-side-left"
                :class="isCollapse ? 'app-side-collapsed' : 'app-side-expanded'">
        <div>
          <el-menu :default-active="defaultActive"
        router
          class="el-menu-vertical-demo"
           @select="handleSelect"
          :collapse="isCollapse">
            <template v-for="route in $router.options.routes">
                <template v-for="item in route.children" >
                  <el-menu-item
                    :key="route.path + '/' + item.path"
                    :index="item.path"
                  >
                    <i :class="item.icon" style="color:white"></i>
                    <span slot="title">{{ item.name }}</span>
                </el-menu-item>
                </template>
            </template>
          </el-menu>
        </div>
      </el-aside>

      <el-container>
        <el-header class="app-header" style="height: 40px;">
          <div style="width: 60px; cursor: pointer; color:white"
               @click.prevent="toggleSideBar">
            <i v-show="!isCollapse" class="el-icon-d-arrow-left"></i>
            <i v-show="isCollapse" class="el-icon-d-arrow-right"></i>
          </div>
          <div class="app-header-userinfo">
            <span>你好：{{ username }}</span>
          </div>
        </el-header>
        <el-main class="app-body">
          <template>
            <router-view/>
          </template>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'Container',
  data () {
    return {
      username: '管理员',
      isCollapse: false,
      defaultActive: ''
    }
  },
  created () {
    this.defaultActive = this.$route.path
  },
  methods: {
    toggleSideBar () {
      this.isCollapse = !this.isCollapse
    },
    handleSelect (val) {
      this.defaultActive = val
    }
  }
}
</script>
<style lang="scss">
.el-menu {
  border-right:none;
  background-color:#606266;
}
.el-menu-item {
    color: white;
}
</style>
