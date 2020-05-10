<template>
<el-form :model="ruleForm" :rules="rules" ref="ruleForm" label-width="140px" class="file-ruleForm" size="small" >
  <el-form-item label="表头行数" prop="headerRowNum">
    <el-input v-model="ruleForm.headerRowNum"></el-input>
  </el-form-item>
  <el-form-item label="拆分单个excel条数" prop="fileNum">
    <el-input v-model="ruleForm.fileNum"></el-input>
  </el-form-item>
  <el-form-item label="sheet名称" prop="sheetName">
    <el-input v-model="ruleForm.sheetName"></el-input>
  </el-form-item>
  <el-form-item label="拆分开始日期" prop="fileDate">
    <el-input v-model="ruleForm.fileDate"></el-input>
  </el-form-item>
  <el-form-item label="excel文件" prop="file">
    <el-upload
      ref="upload"
      :action="uploadUrl"
      :limit="1"
       accept=".xlsx,.xls,.xlsm"
       :data="ruleForm"
       :on-success="handleSuccessUpload"
      :auto-upload="false">
      <el-button slot="trigger" size="small">选取文件</el-button>
      <span class="el-upload__tip content_upload__tip" slot="tip">支持xls/xlsx文件</span>
    </el-upload>
  </el-form-item>
  <el-form-item class="content_button" v-loading="loading">
    <el-button type="primary" @click="submitForm('ruleForm')">拆分文件</el-button>
  </el-form-item>
</el-form>
</template>
<script>
import { Message } from 'element-ui'
import * as env from '@/api/base_url'
import { util } from '@/utils/util'
const baseURL = env.baseURL

export default {
  data () {
    return {
      uploadUrl: '',
      loading: false,
      ruleForm: {
        headerRowNum: 3,
        fileNum: 200,
        sheetName: 'Template',
        fileDate: ''
      },
      rules: {
        headerRowNum: [
          { required: true, message: '请输入表头行数', trigger: 'blur' }
        ],
        fileNum: [
          { required: true, message: '请输入单个excel条数', trigger: 'blur' }
        ],
        sheetName: [
          { required: true, message: 'sheet名称', trigger: 'blur' }
        ],
        fileDate: [
          { required: true, message: '请输入拆分开始日期', trigger: 'blur' }
        ]
      }
    }
  },
  created () {
    this.uploadUrl = baseURL + '/api/file/split'
    this.ruleForm.fileDate = util.dateToStr(new Date(), 1)
  },
  methods: {
    submitForm (formName) {
      this.$refs[formName].validate(async (valid) => {
        if (valid) {
          this.$refs.upload.submit()
          this.loading = true
        } else {
          return false
        }
      })
    },
    handleSuccessUpload (res) {
      if (res.status) {
        Message({
          message: '拆分成功！',
          type: 'success',
          duration: 3 * 1000,
          showClose: true
        })
      } else {
        Message({
          message: res.message,
          type: 'error',
          duration: 3 * 1000,
          showClose: true
        })
      }
      this.loading = false
    }
  }
}
</script>
<style lang="scss">
.file-ruleForm{
  width: 400px;
  margin: 0 auto;
  .content_button{
    margin-left: 80px;
  }
  .content_upload__tip{
    margin-left: 10px;
  }
}
</style>
