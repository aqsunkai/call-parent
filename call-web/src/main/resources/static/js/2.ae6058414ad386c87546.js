webpackJsonp([2],{Kw70:function(e,t,r){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var l=r("lC5x"),o=r.n(l),a=r("J0Oq"),n=r.n(a),i=r("c+XW"),u=r("gyMJ"),s={data:function(){return{dialogVisible:!1,runningStatusTimer:"",running:!0,failName:[],uploadName:[],ruleForm:{type:1,filePath:"",property:"属性图",attachProperty:"主图",productNameFile:"",cookie:""},rules:{filePath:[{required:!0,message:"请输入文件根路径",trigger:"blur"}],property:[{required:!0,message:"请输入",trigger:"blur"}],attachProperty:[{required:!0,message:"请输入",trigger:"blur"}],cookie:[{required:!0,message:"请输入cookie",trigger:"blur"}]}}},beforeDestroy:function(){this.runningStatusTimer&&clearInterval(this.runningStatusTimer)},methods:{submitForm:function(e){var t,r=this;1!==this.ruleForm.type||this.ruleForm.productNameFile?this.$refs[e].validate((t=n()(o.a.mark(function e(t){return o.a.wrap(function(e){for(;;)switch(e.prev=e.next){case 0:if(!t){e.next=7;break}return e.next=3,Object(u.a)(r.ruleForm);case 3:e.sent.status&&(r.dialogVisible=!0,r.running=!0,r.failName=[],r.uploadName=[],r.runningStatusTimer=setInterval(function(){r.getProcessRunningStatus()},5e3)),e.next=8;break;case 7:return e.abrupt("return",!1);case 8:case"end":return e.stop()}},e,r)})),function(e){return t.apply(this,arguments)})):Object(i.Message)({message:"请输入产品&价格文件名",type:"error",duration:3e3,showClose:!0})},getProcessRunningStatus:function(){var e=this;return n()(o.a.mark(function t(){var r;return o.a.wrap(function(t){for(;;)switch(t.prev=t.next){case 0:return t.next=2,Object(u.b)(e.ruleForm);case 2:(r=t.sent).result&&(e.running=r.result.running,e.failName=r.result.failName,e.uploadName=r.result.uploadName,r.result.running||clearInterval(e.runningStatusTimer));case 4:case"end":return t.stop()}},t,e)}))()},resetForm:function(e){this.$refs[e].resetFields()}}},c={render:function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("el-form",{ref:"ruleForm",staticClass:"xiaomi-ruleForm",attrs:{model:e.ruleForm,rules:e.rules,"label-width":"130px",size:"small"}},[r("el-form-item",{attrs:{label:"产品名称",prop:"type"}},[r("el-radio-group",{staticClass:"content_radio",model:{value:e.ruleForm.type,callback:function(t){e.$set(e.ruleForm,"type",t)},expression:"ruleForm.type"}},[r("el-radio",{attrs:{label:0}},[e._v("使用图片名称")]),e._v(" "),r("el-radio",{attrs:{label:1}},[e._v("读取txt文件")])],1)],1),e._v(" "),r("el-form-item",{attrs:{label:"文件根路径",prop:"filePath"}},[r("el-input",{model:{value:e.ruleForm.filePath,callback:function(t){e.$set(e.ruleForm,"filePath",t)},expression:"ruleForm.filePath"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"属性图名称",prop:"property"}},[r("el-input",{model:{value:e.ruleForm.property,callback:function(t){e.$set(e.ruleForm,"property",t)},expression:"ruleForm.property"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"主图名称",prop:"attachProperty"}},[r("el-input",{model:{value:e.ruleForm.attachProperty,callback:function(t){e.$set(e.ruleForm,"attachProperty",t)},expression:"ruleForm.attachProperty"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"产品&价格文件名",prop:"productNameFile"}},[r("el-input",{model:{value:e.ruleForm.productNameFile,callback:function(t){e.$set(e.ruleForm,"productNameFile",t)},expression:"ruleForm.productNameFile"}})],1),e._v(" "),r("el-form-item",{attrs:{label:"cookie",prop:"cookie"}},[r("el-input",{attrs:{type:"textarea",rows:"2"},model:{value:e.ruleForm.cookie,callback:function(t){e.$set(e.ruleForm,"cookie",t)},expression:"ruleForm.cookie"}})],1),e._v(" "),r("el-form-item",{staticClass:"content_button"},[r("el-button",{attrs:{type:"primary"},on:{click:function(t){return e.submitForm("ruleForm")}}},[e._v("创建产品")]),e._v(" "),r("el-button",{on:{click:function(t){return e.resetForm("ruleForm")}}},[e._v("重置")])],1),e._v(" "),r("el-dialog",{attrs:{title:"正在刷新创建的产品",visible:e.dialogVisible,width:"40%",center:""},on:{"update:visible":function(t){e.dialogVisible=t}}},[e.running?r("p",{staticStyle:{color:"#409EFF","text-align":"center"}},[e._v("正在创建")]):e._e(),e._v(" "),e.running?e._e():r("p",{staticStyle:{color:"#67C23A","text-align":"center"}},[e._v("创建完成")]),e._v(" "),r("p",{staticStyle:{color:"#909399","text-align":"center"}},[e._v("创建成功产品")]),e._v(" "),e._l(e.uploadName,function(t,l){return r("div",{key:1e3+l},[r("span",[e._v(e._s(t))])])}),e._v(" "),r("p",{staticStyle:{color:"#F56C6C","text-align":"center"}},[e._v("创建失败产品")]),e._v(" "),e._l(e.failName,function(t,l){return r("div",{key:2e3+l},[r("span",[e._v(e._s(t))])])}),e._v(" "),r("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[r("el-button",{attrs:{size:"mini",type:"primary"},on:{click:function(t){e.dialogVisible=!1}}},[e._v("关闭")])],1)],2)],1)},staticRenderFns:[]};var m=r("C7Lr")(s,c,!1,function(e){r("UtRw")},null,null);t.default=m.exports},UtRw:function(e,t){}});
//# sourceMappingURL=2.ae6058414ad386c87546.js.map