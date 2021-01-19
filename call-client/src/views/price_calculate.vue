<template>
<div>
<el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="150px" class="price-ruleForm" size="small" v-loading="loading" element-loading-text="正在拼命修改价格">
  <el-form-item label="文件根路径" prop="filePath">
    <el-input v-model="ruleForm.filePath"></el-input>
  </el-form-item>
  <el-form-item label="图片文件夹名称" prop="property">
    <el-input v-model="ruleForm.property" :placeholder="propertyTxt" ></el-input>
  </el-form-item>
  <el-form-item label="价格修改方式" prop="calPattern">
    <el-input type="textarea" rows="12" :placeholder="priceTxt" v-model="ruleForm.calPattern"></el-input>
  </el-form-item>
  <el-form-item class="content_button">
    <el-button type="primary" @click="submitForm('ruleForm')">修改价格</el-button>
  </el-form-item>
</el-form>
<div v-if="errorFolder.length>0">
 <p style="color:#F56C6C;text-align:center">修改失败的文件夹</p>
 <div v-for="(item,index) in errorFolder" :key="index">
    <span co>{{item}}</span>
  </div>
</div>
</div>
</template>
<script>
import { Message } from 'element-ui'
import { priceCalculate } from '@/api'
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
      loading: false,
      ruleForm: {
        filePath: '',
        property: '属性图',
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
          { required: true, message: '请输入价格修改方式', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    submitForm (formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          this.loading = true
          const res = await priceCalculate(this.ruleForm)
          this.loading = false
          if (res.status) {
            Message({
              message: '修改价格成功',
              type: 'success',
              duration: 3 * 1000,
              showClose: true
            })
            if (res.result) {
              this.errorFolder = res.result
            }
          } else {
            Message({
              message: this.result,
              type: 'error',
              duration: 3 * 1000,
              showClose: true
            })
          }
        } else {
          return false
        }
      })
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
