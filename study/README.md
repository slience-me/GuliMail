
1. 先安装webpack
- npm install webpack -g

2. 安装vue脚手架
- npm install -g @vue/cli    vue3.0
- npm install -g vue-cli     vue2.0

3. 创建项目
- vue init webpack vue-demo

4. 启动
- npm run dev

---

安装element-ui
- npm i element-ui -S

模板
```vue
<template>
  <div></div>
</template>

<script>
//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
//例如：import 《组件名称》 from '《组件路径》';

export default {
  //import引入的组件需要注入到对象中才能使用
  components: {},
  props: {},
  data() {
    //这里存放数据
    return {
    };
  },
  //计算属性 类似于data概念
  computed: {},
  //监控data中的数据变化
  watch: {},
  //方法集合
  methods: {},
  //生命周期 - 创建完成（可以访问当前this实例）
  created() {},
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期-创建之前
  beforeMount() {}, //生命周期-挂载之前
  beforeUpdate() {}, //生命周期-更新之前
  updated() {}, //生命周期-更新之后
  beforeDestroy() {}, //生命周期-销毁之前
  destroyed() {}, //生命周期-销毁完成
  activated() {} //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style lang="scss" scoped>
// @import url($3); 引入公共css类
</style>
```
