<template>
  <div>
    <el-tree
      :data="menus"
      show-checkbox
      :props="defaultProps"
      :expand-on-click-node="false"
      node-key="catId"
      :default-expanded-keys="expandedKey">
    <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)">
            Append
          </el-button>
          <el-button
            v-if="node.childNodes.length === 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)">
            Delete
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog
      title="提示"
      :visible.sync="dialogVisible"
      width="30%"
      :before-close="handleClose">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="addCategory">确 定</el-button>
  </span>
    </el-dialog>
  </div>
</template>

<script>
// 这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
// 例如：import 《组件名称》 from '《组件路径》';

export default {
  // import引入的组件需要注入到对象中才能使用
  components: {},
  props: {},
  data () {
    // 这里存放数据
    return {
      menus: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      expandedKey: [],
      dialogVisible: false,
      category: {
        name: ''
      }
    }
  },
  // 计算属性 类似于data概念
  computed: {},
  // 监控data中的数据变化
  watch: {},
  // 方法集合
  methods: {
    handleClose (done) {
      this.$confirm('确认关闭？')
        .then(_ => {
          done()
        })
        .catch(_ => {})
    },
    getMenus () {
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get'
      }).then(({data}) => {
        this.menus = data.data
      })
    },
    handleNodeClick (data) {
      console.log(data)
    },
    append (data) {
      this.dialogVisible = true
      console.log('append', data)
    },
    remove (node, data) {
      const ids = [data.catId]
      this.$confirm(`是否删除【${data.name}】菜单 ?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'post',
          data: this.$http.adornData(ids, false)
        }).then(({data}) => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getMenus()
          this.expandedKey = [node.parent.data.catId]
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    addCategory () {
      console.log(this.category)
    }
  },
  // 生命周期 - 创建完成（可以访问当前this实例）
  created () {
    this.getMenus()
  },
  // 生命周期 - 挂载完成（可以访问DOM元素）
  mounted () {
  },
  beforeCreate () {
  }, // 生命周期-创建之前
  beforeMount () {
  }, // 生命周期-挂载之前
  beforeUpdate () {
  }, // 生命周期-更新之前
  updated () {
  }, // 生命周期-更新之后
  beforeDestroy () {
  }, // 生命周期-销毁之前
  destroyed () {
  }, // 生命周期-销毁完成
  activated () {
  } // 如果页面有keep-alive缓存功能，这个函数会触发
}
</script>
<style scoped>
</style>
