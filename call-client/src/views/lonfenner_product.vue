<template>
<div>
<h2 style="text-align: center;">洛菲纳跨境电商智慧生态系统</h2>
<el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="150px" class="lonfenner-product-ruleForm" size="small" >
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
  <el-form-item label="产品价格" prop="priceType">
    <el-radio-group v-model="ruleForm.priceType">
      <el-radio :label="0">使用图片名称,取(前数字</el-radio>
      <el-radio :label="1">使用图片名称,放入品牌名</el-radio>
      <el-radio :label="2">默认无价格</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="商品变体" prop="customDefs">
    <div>
      <el-checkbox v-model="ruleForm.customDefCheck">使用</el-checkbox>
      <el-input v-model="ruleForm.customDefs.code" style="width: 140px;margin-left: 20px;"></el-input>
      <el-input v-model="ruleForm.customDefs.name" style="width: 140px"></el-input>
      <el-input v-model="ruleForm.customDefs.valueOptions" style="width: 286px"></el-input>
    </div>
  </el-form-item>
  <el-form-item label="上传完成播放音乐" prop="playMusic">
    <el-radio-group v-model="ruleForm.playMusic">
      <el-radio :label="0">否</el-radio>
      <el-radio :label="1">是</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="Authorization" prop="cookie">
    <el-input type="textarea" rows="12" v-model="ruleForm.cookie"></el-input>
  </el-form-item>
  <el-form-item class="content_button">
    <el-button type="primary" @click="submitForm('ruleForm')">创建产品</el-button>
    <el-button  @click="resetForm('ruleForm')">重置</el-button>
  </el-form-item>
  <el-dialog
  title="正在刷新创建的产品"
  :visible.sync="dialogVisible"
  :close-on-click-modal="false"
  :close-on-press-escape="false"
  width="50%" center>
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
import { productSend, productResult } from '@/api'
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
        priceType: 2,
        playMusic: 1,
        attachType: '',
        filePath: '',
        property: '属性图',
        attachProperty: '主图',
        productNameFile: '下图记录',
        cookie: '',
        customDefCheck: false,
        customDefs: {
          code: 'Size',
          name: '尺寸',
          valueOptions: 'M,L'
        }
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
          { required: true, message: '请输入Authorization', trigger: 'blur' }
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
          message: '读取txt文件时需要输入txt文件名',
          type: 'error',
          duration: 3 * 1000,
          showClose: true
        })
        return
      }
      if (this.ruleForm.customDefCheck === true && !(this.ruleForm.customDefs.code && this.ruleForm.customDefs.name && this.ruleForm.customDefs.valueOptions)) {
        Message({
          message: '商品变体需要全部输入',
          type: 'error',
          duration: 3 * 1000,
          showClose: true
        })
        return
      }
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          if (this.ruleForm.customDefCheck !== true && (this.ruleForm.customDefs.code || this.ruleForm.customDefs.name || this.ruleForm.customDefs.valueOptions)) {
            this.$confirm('商品变体输入了内容，但未选择使用，确定后则不会创建商品变体', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              this.sendProduct()
            }).catch(() => {
            })
          } else {
            this.sendProduct()
          }
        } else {
          return false
        }
      })
    },
    async sendProduct () {
      const res = await productSend(this.ruleForm)
      if (res.status) {
        this.dialogVisible = true
        this.running = true
        this.failName = []
        this.uploadName = []
        this.runningStatusTimer = setInterval(() => {
          this.getProcessRunningStatus()
        }, 5000)
      }
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
.lonfenner-product-ruleForm{
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
