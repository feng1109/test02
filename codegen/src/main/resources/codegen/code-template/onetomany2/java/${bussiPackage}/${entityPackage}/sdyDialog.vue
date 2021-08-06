<template>
  <el-dialog :title="dialogTitle" :width="width" :visible.sync="dialogIS" class="dialog-dialogTableJe">
    <el-form :model="dialogData" :rules="dialogDataRules" ref="dialogData" label-position="right" label-width="135px"
      class="demo-ruleForm area-ruleForm">
      <div class="dialogContent">
        <el-row>
          <el-col :span="8">
            <el-form-item label="支付宝AppID" prop="alipayAppid">
              <el-input class="el-dialog-child" placeholder="请输入内容" v-model="dialogData.alipayAppid" clearable size="small"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付宝商户私钥" prop="alipayMerchantPrivateKey">
              <el-input class="el-dialog-child" placeholder="请输入内容" v-model="dialogData.alipayMerchantPrivateKey"
                clearable size="small"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付宝商户公钥" prop="alipayPublicKey">
              <el-input class="el-dialog-child" placeholder="请输入内容" v-model="dialogData.alipayPublicKey" clearable size="small"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="支付宝回调地址" prop="alipayNotifyUrl">
              <el-input class="el-dialog-child" placeholder="请输入内容" v-model="dialogData.alipayNotifyUrl" clearable size="small"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付宝重定向URl" prop="alipayReturnUrl">
              <el-input class="el-dialog-child" placeholder="请输入内容" v-model="dialogData.alipayReturnUrl" clearable size="small"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      <el-form-item class="pubDialogFooter dialogSub" style="margin-bottom: 0;padding: 5px 20px;">
        <el-button class="pubButton butSure" icon="iconfont icongou-copy" @click="submitFormDialog('dialogData')">
          保存</el-button>
        <el-button class="pubButton butClose" icon="iconfont iconguanbi" @click="resetFormDialog('dialogData')">
          取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
  export default {
    props: {
      dialogData: {
        type: Object,
        default: () => {
          return {}
        }
      },
      dialogTitle: {
        type: String,
      },
      width: {
        type: String,
        default: "900px"
      }
    },
    data() {
      return {
        dialogIS: false,
        dialogDataRules: {
          'alipayAppid': [{
            required: true,
            message: '租户名称不能为空',
            trigger: 'change'
          }]
        },
      }
    },
    created() {

    },
    methods: {
      submitFormDialog(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {

            this.$emit('dialog-save', this.dialogData);
            this.dialogIS = false;

          }
        });
      },
      resetFormDialog(formName) {
        this.dialogIS = false;

      },

    },
  }
</script>

<style>
</style>
