webpackJsonp([3],{XPpU:function(e,r){},qDh8:function(e,r,t){"use strict";Object.defineProperty(r,"__esModule",{value:!0});var l=t("lC5x"),a=t.n(l),n=t("J0Oq"),s=t.n(n),o=t("gyMJ"),i={data:function(){return{priceTxt:"<4:14.97\n4-5:15.97\n6-7:16.97\n8:17.97\n9:18.97\n10:19.97\n11:20.97\n12:21.97\n13:22.97\n14:23.97\n15:24.97\n16:25.97\n17:26.97\n18:27.97\n19:28.97\n20:29.97",propertyTxt:"多个文件夹以逗号,分隔",errorFolder:[],result:"",showRes:0,calculateTimer:"",ruleForm:{filePath:"",property:"属性图",valueType:0,intervalType:2,calPattern:"<4:14.97\n4-5:15.97\n6-7:16.97\n8:17.97\n9:18.97\n10:19.97\n11:20.97\n12:21.97\n13:22.97\n14:23.97\n15:24.97\n16:25.97\n17:26.97\n18:27.97\n19:28.97\n20:29.97"},rules:{filePath:[{required:!0,message:"请输入文件根路径",trigger:"blur"}],property:[{required:!0,message:"请输入图片文件夹名称",trigger:"blur"}],calPattern:[{required:!0,message:"请输入价格修改方式",trigger:"blur"}]}}},beforeDestroy:function(){this.calculateTimer&&clearInterval(this.calculateTimer)},methods:{submitForm:function(e){var r,t=this;this.errorFolder=[],this.$refs[e].validate((r=s()(a.a.mark(function e(r){return a.a.wrap(function(e){for(;;)switch(e.prev=e.next){case 0:if(!r){e.next=7;break}return e.next=3,Object(o.b)(t.ruleForm);case 3:e.sent.status&&(t.showRes=1,t.calculateTimer=setInterval(function(){t.getCalculateResult()},2e3)),e.next=8;break;case 7:return e.abrupt("return",!1);case 8:case"end":return e.stop()}},e,t)})),function(e){return r.apply(this,arguments)}))},getCalculateResult:function(){var e=this;return s()(a.a.mark(function r(){var t;return a.a.wrap(function(r){for(;;)switch(r.prev=r.next){case 0:return r.next=2,Object(o.a)({filePath:e.ruleForm.filePath});case 2:(t=r.sent).result&&(t.result.running||(t.result.failName&&0!==t.result.failName.length?(e.errorFolder=t.result.failName,e.showRes=3):e.showRes=2,clearInterval(e.calculateTimer)));case 4:case"end":return r.stop()}},r,e)}))()}}},u={render:function(){var e=this,r=e.$createElement,t=e._self._c||r;return t("div",[t("el-form",{ref:"ruleForm",staticClass:"price-ruleForm",attrs:{model:e.ruleForm,rules:e.rules,"label-width":"150px",size:"small"}},[t("el-form-item",{attrs:{label:"文件根路径",prop:"filePath"}},[t("el-input",{model:{value:e.ruleForm.filePath,callback:function(r){e.$set(e.ruleForm,"filePath",r)},expression:"ruleForm.filePath"}})],1),e._v(" "),t("el-form-item",{attrs:{label:"图片文件夹名称",prop:"property"}},[t("el-input",{attrs:{placeholder:e.propertyTxt},model:{value:e.ruleForm.property,callback:function(r){e.$set(e.ruleForm,"property",r)},expression:"ruleForm.property"}})],1),e._v(" "),t("el-form-item",{attrs:{label:"价格取值方式",prop:"valueType"}},[t("el-radio-group",{model:{value:e.ruleForm.valueType,callback:function(r){e.$set(e.ruleForm,"valueType",r)},expression:"ruleForm.valueType"}},[t("el-radio",{attrs:{label:0}},[e._v("优先使用促销价格")]),e._v(" "),t("el-radio",{attrs:{label:1}},[e._v("强制使用完整价格")])],1)],1),e._v(" "),t("el-form-item",{attrs:{label:"区间价格取值",prop:"intervalType"}},[t("el-radio-group",{model:{value:e.ruleForm.intervalType,callback:function(r){e.$set(e.ruleForm,"intervalType",r)},expression:"ruleForm.intervalType"}},[t("el-radio",{attrs:{label:0}},[e._v("最小值")]),e._v(" "),t("el-radio",{attrs:{label:1}},[e._v("中间值")]),e._v(" "),t("el-radio",{attrs:{label:2}},[e._v("最大值")])],1)],1),e._v(" "),t("el-form-item",{attrs:{label:"价格转换",prop:"calPattern"}},[t("el-input",{attrs:{type:"textarea",rows:"10",placeholder:e.priceTxt},model:{value:e.ruleForm.calPattern,callback:function(r){e.$set(e.ruleForm,"calPattern",r)},expression:"ruleForm.calPattern"}})],1),e._v(" "),t("el-form-item",{staticClass:"content_button"},[t("el-button",{attrs:{type:"primary"},on:{click:function(r){return e.submitForm("ruleForm")}}},[e._v("修改价格")])],1),e._v(" "),t("el-alert",{directives:[{name:"show",rawName:"v-show",value:1==e.showRes,expression:"showRes == 1"}],attrs:{type:"warning",center:"","show-icon":"",effect:"dark",title:"正在拼命修改价格，请耐心等待"}}),e._v(" "),t("el-alert",{directives:[{name:"show",rawName:"v-show",value:2==e.showRes,expression:"showRes == 2"}],attrs:{type:"success",center:"","show-icon":"",effect:"dark",title:"价格全部修改成功"}}),e._v(" "),t("el-alert",{directives:[{name:"show",rawName:"v-show",value:3==e.showRes,expression:"showRes == 3"}],attrs:{type:"error",center:"","show-icon":"",effect:"dark",title:"价格部分修改失败，失败的文件夹如下"}}),e._v(" "),e.errorFolder.length>0?t("div",e._l(e.errorFolder,function(r,l){return t("div",{key:l},[t("span",[e._v(e._s(r))])])}),0):e._e()],1)],1)},staticRenderFns:[]};var c=t("C7Lr")(i,u,!1,function(e){t("XPpU")},null,null);r.default=c.exports}});
//# sourceMappingURL=3.99abda3dba5f4a01ba4f.js.map