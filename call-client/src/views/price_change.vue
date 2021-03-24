<template>
<div>
<el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="150px" class="price-ruleForm" size="small" v-loading="loading" element-loading-text="正在拼命修改价格">
  <el-form-item label="价格转换公式" prop="calPattern">
    <el-input type="textarea" rows="10" :placeholder="priceTxt" v-model="ruleForm.calPattern"></el-input>
  </el-form-item>
  <el-form-item label="转换前价格" prop="frontPrice">
    <el-input type="textarea" rows="10" :placeholder="frontPriceTxt" v-model="ruleForm.frontPrice"></el-input>
  </el-form-item>
  <el-form-item label="转换后价格" prop="afterPrice">
    <el-input type="textarea" rows="10" v-model="afterPriceTxt"></el-input>
  </el-form-item>
  <el-form-item class="content_button">
    <el-button type="primary" @click="submitForm('ruleForm')">转换价格</el-button>
  </el-form-item>
</el-form>
</div>
</template>
<script>
import { Message } from 'element-ui'
import { priceChange } from '@/api'
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
        '20:29.97\n' +
        '21-100:原价',
      frontPriceTxt: '1\n' +
        '2\n' +
        '3\n' +
        '4\n' +
        '5\n' +
        '6',
      afterPriceTxt: '',
      loading: false,
      ruleForm: {
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
        '20:29.97\n' +
        '21-100:原价'
      },
      frontPrice: '',

      rules: {
        calPattern: [
          { required: true, message: '请输入价格转换方式', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    submitForm (formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          this.afterPriceTxt = ''
          this.loading = true
          const res = await priceChange(this.ruleForm)
          this.loading = false
          if (res.status) {
            this.afterPriceTxt = res.result
            Message({
              message: '价格转换成功',
              type: 'success',
              duration: 3 * 1000,
              showClose: true
            })
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
