<template>
<div>
<el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="150px" class="price-ruleForm" size="small">
  <el-form-item label="文件根路径" prop="filePath">
    <el-input v-model="ruleForm.filePath"></el-input>
  </el-form-item>
  <el-form-item label="图片文件夹名称" prop="property">
    <el-input v-model="ruleForm.property" :placeholder="propertyTxt" ></el-input>
  </el-form-item>
  <el-form-item label="价格取值方式" prop="valueType">
    <el-radio-group v-model="ruleForm.valueType">
      <el-radio :label="0">优先使用促销价格</el-radio>
      <el-radio :label="1">强制使用完整价格</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="区间价格取值" prop="intervalType">
    <el-radio-group v-model="ruleForm.intervalType">
      <el-radio :label="0">最小值</el-radio>
      <el-radio :label="1">中间值</el-radio>
      <el-radio :label="2">最大值</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="价格计算公式" prop="calPattern">
    <el-input type="textarea" rows="10" :placeholder="priceTxt" v-model="ruleForm.calPattern"></el-input>
  </el-form-item>
  <el-form-item class="content_button">
    <el-button type="primary" @click="submitForm('ruleForm')">计算价格</el-button>
  </el-form-item>
    <el-alert type = 'warning' center show-icon effect="dark" v-show="showRes == 1" title="正在拼命计算价格，请耐心等待"></el-alert>
    <el-alert type = 'success' center show-icon effect="dark" v-show="showRes == 2" title="价格全部计算成功"></el-alert>
    <el-alert type = 'error' center show-icon effect="dark" v-show="showRes == 3" title="价格部分计算失败，失败的文件夹如下"></el-alert>
    <div v-if="errorFolder.length > 0">
       <div v-for="(item,index) in errorFolder" :key="index">
        <span>{{item}}</span>
       </div>
    </div>
</el-form>
</div>
</template>
<script>
import { priceCalculate, calculateResult } from '@/api'
export default {
  data () {
    return {
      priceTxt: '<4:14.97\n' +
        '4-5:15.97\n' +
        '6-7:16.97\n' +
        '8:17.97\n' +
        '9:18.97\n' +
        '10:19.97\n' +
        '11:20.97\n' +
        '12:21.97\n' +
        '13:22.97\n' +
        '14:23.97\n' +
        '15:24.97\n' +
        '16:25.97\n' +
        '17:26.97\n' +
        '18:27.97\n' +
        '19:28.97\n' +
        '20:29.97',
      propertyTxt: '多个文件夹以逗号,分隔',
      errorFolder: [],
      result: '',
      showRes: 0,
      calculateTimer: '',
      ruleForm: {
        filePath: '',
        property: '属性图',
        valueType: 0,
        intervalType: 2,
        calPattern: '<4:14.97\n' +
        '4-5:15.97\n' +
        '6-7:16.97\n' +
        '8:17.97\n' +
        '9:18.97\n' +
        '10:19.97\n' +
        '11:20.97\n' +
        '12:21.97\n' +
        '13:22.97\n' +
        '14:23.97\n' +
        '15:24.97\n' +
        '16:25.97\n' +
        '17:26.97\n' +
        '18:27.97\n' +
        '19:28.97\n' +
        '20:29.97'
      },
      rules: {
        filePath: [
          { required: true, message: '请输入文件根路径', trigger: 'blur' }
        ],
        property: [
          { required: true, message: '请输入图片文件夹名称', trigger: 'blur' }
        ],
        calPattern: [
          { required: true, message: '请输入价格计算公式', trigger: 'blur' }
        ]
      }
    }
  },
  // 销毁定时器
  beforeDestroy () {
    if (this.calculateTimer) {
      clearInterval(this.calculateTimer)
    }
  },
  methods: {
    submitForm (formName) {
      this.errorFolder = []
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          const res = await priceCalculate(this.ruleForm)
          if (res.status) {
            this.showRes = 1
            this.calculateTimer = setInterval(() => {
              this.getCalculateResult()
            }, 2000)
          }
        } else {
          return false
        }
      })
    },
    async getCalculateResult () {
      const res = await calculateResult({'filePath': this.ruleForm.filePath})
      if (res.result) {
        if (!res.result.running) {
          if (!res.result.failName || res.result.failName.length === 0) {
            this.showRes = 2
          } else {
            this.errorFolder = res.result.failName
            this.showRes = 3
          }
          clearInterval(this.calculateTimer)
        }
      }
    }
  }
}
</script>
<style lang="scss">
.price-ruleForm{
  width: 800px;
  margin: 0 auto;
  .content_button{
    margin-left: 260px;
  }
}
</style>
