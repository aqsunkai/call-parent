<template>
<div>
<!-- <h2 style="text-align: center;">小米跨境电商ERP系统</h2> -->
<el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="150px" class="xiaomi-ruleForm" size="small" >
  <el-form-item label="文件根路径" prop="filePath">
    <el-input v-model="ruleForm.filePath"></el-input>
  </el-form-item>
  <el-form-item label="列表文件夹名称" prop="property">
    <el-input v-model="ruleForm.property"></el-input>
  </el-form-item>
  <el-form-item label="详情文件夹名称" prop="attachProperty">
    <el-input v-model="ruleForm.attachProperty"></el-input>
  </el-form-item>
  <el-form-item label="文件夹关系" prop="attachType">
    <el-radio-group v-model="ruleForm.attachType">
      <el-radio :label="0">列表文件夹和详情文件夹平级，即两个文件夹都作为主图</el-radio>
      <br>
      <el-radio :label="1" style="margin-top: 8px">列表文件夹作为主图，详情文件夹作为附图</el-radio>
      <br>
      <el-radio :label="2" style="margin-top: 8px">列表文件夹和详情文件夹第一张作为主图，详情文件夹剩下的图片作为附图</el-radio>
      <br>
      <el-radio :label="3" style="margin-top: 8px">列表文件夹和详情文件夹第一张作为主图，详情文件夹剩下的图片丢弃</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="产品名称" prop="type">
    <el-radio-group v-model="ruleForm.type">
      <el-radio :label="0">使用图片名称</el-radio>
      <el-radio :label="1">读取txt文件</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="txt文件名" prop="productNameFile" v-if="ruleForm.type==1">
    <el-input v-model="ruleForm.productNameFile"></el-input>
  </el-form-item>
  <el-form-item label="cookie" prop="cookie">
    <el-input type="textarea" rows="12" v-model="ruleForm.cookie"></el-input>
  </el-form-item>
  <el-form-item class="content_button">
    <el-button type="primary" @click="submitForm('ruleForm')">创建产品</el-button>
    <el-button  @click="resetForm('ruleForm')">重置</el-button>
  </el-form-item>
  <el-dialog
  title="正在刷新创建的产品"
  :visible.sync="dialogVisible"
  width="40%" center>
  <p v-if="running" style="color:#409EFF;text-align:center">正在创建</p>
  <p v-if="!running" style="color:#67C23A;text-align:center">创建完成</p>
  <p style="color:#909399;text-align:center">创建成功产品</p>
  <div v-for="(item,index) in uploadName" :key="1000+index">
    <span>{{item}}</span>
  </div>
  <p style="color:#F56C6C;text-align:center">创建失败产品</p>
  <div v-for="(item,index) in failName" :key="2000+index">
    <span>{{item}}</span>
  </div>
  <span slot="footer" class="dialog-footer">
    <el-button size="mini" type="primary" @click="dialogVisible = false">关闭</el-button>
  </span>
</el-dialog>
</el-form>
</div>
</template>
<script>
import { Message } from 'element-ui'
import { erpProductSend, productResult } from '@/api'
export default {
  data () {
    return {
      dialogVisible: false,
      runningStatusTimer: '',
      running: true,
      failName: [],
      uploadName: [],
      ruleForm: {
        type: 1,
        attachType: '',
        filePath: '',
        property: '属性图',
        attachProperty: '主图',
        productNameFile: '下图记录',
        cookie: ''
      },
      rules: {
        filePath: [
          { required: true, message: '请输入文件根路径', trigger: 'blur' }
        ],
        property: [
          { required: true, message: '请输入列表文件夹名称', trigger: 'blur' }
        ],
        attachProperty: [
          { required: true, message: '请输入详情列表文件夹名称', trigger: 'blur' }
        ],
        attachType: [
          { required: true, message: '请选择文件夹关系', trigger: 'blur' }
        ],
        cookie: [
          { required: true, message: '请输入cookie', trigger: 'blur' }
        ]
      }
    }
  },
  // 销毁定时器
  beforeDestroy () {
    if (this.runningStatusTimer) {
      clearInterval(this.runningStatusTimer)
    }
  },
  methods: {
    submitForm (formName) {
      if (this.ruleForm.type === 1 && !this.ruleForm.productNameFile) {
        Message({
          message: 'txt文件名',
          type: 'error',
          duration: 3 * 1000,
          showClose: true
        })
        return
      }
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const res = await erpProductSend(this.ruleForm)
          if (res.status) {
            this.dialogVisible = true
            this.running = true
            this.failName = []
            this.uploadName = []
            this.runningStatusTimer = setInterval(() => {
              this.getProcessRunningStatus()
            }, 5000)
          }
        } else {
          return false
        }
      })
    },
    async getProcessRunningStatus () {
      const res = await productResult(this.ruleForm)
      if (res.result) {
        this.running = res.result.running
        this.failName = res.result.failName
        this.uploadName = res.result.uploadName
        if (!res.result.running) {
          clearInterval(this.runningStatusTimer)
        }
      }
    },
    resetForm (formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>
<style lang="scss">
.xiaomi-ruleForm{
  width: 800px;
  margin: 0 auto;
  .content_button{
    margin-left: 260px;
  }
  .content_radio{
    margin: 8px;
    float: left;
  }
}
</style>
