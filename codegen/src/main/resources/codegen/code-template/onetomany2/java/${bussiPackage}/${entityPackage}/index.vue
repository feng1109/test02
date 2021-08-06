<!-- 一对一 -->

<template>
  <basic-container class="pubOut3">
    <div class="userPUB" style="height: 100%;background:#f0f3f4;overflow: auto;">
      <el-row class="pubOut3top">
        <div class="selNameOutDiv">
          <span class="selName">区域：</span>
          <avue-input style="display:inline-block;" class="pubInput" size="small" v-model="searchForm.areaBm" type="tree"
            placeholder="请选择区域" :dic="getAllAreaList" :props="{label: 'label',value: 'extra'}"></avue-input>
        </div>
        <div class="selNameOutDiv">
          <span class="selName">抄表员：</span>
          <el-select class="pubInput" v-model="searchForm.cbry" placeholder="请选择" size='small'>
            <el-option v-for="item in getUserList" :key="item.userId" :label="item.xm" :value="item.userId">
            </el-option>
          </el-select>
        </div>
        <div class="selNameOutDiv" v-if="searchShowIs">
          <span class="selName">户号：</span>
          <el-input class="pubInput" placeholder="请输入内容" v-model="searchForm.userNo" clearable size='small'>
          </el-input>
        </div>
        <div class="selNameOutDiv" v-if="searchShowIs">
          <span class="selName">户号：</span>
          <el-input class="pubInput" placeholder="请输入内容" v-model="searchForm.userNo" clearable size='small'>
          </el-input>
        </div>
        <div class="selNameOutDiv">
          <el-button class="pubButton butSearch" icon="iconfont iconchaxun-copy" @click="search"> 查询</el-button>
          <el-button class="pubButton butReset" icon="iconfont iconicon-_zhongzhi" @click='reset'> 重置</el-button>
          <el-button class="" type="text" style="margin-left:20px" @click="searchShow">展开
            <i v-show="searchShowIs" class="el-icon-arrow-up"></i>
            <i v-show="!searchShowIs" class="el-icon-arrow-down"></i>
          </el-button>
        </div>
      </el-row>

      <div class="lineEEE"></div>

      <div class="pubout3center pubout3centerForm" style="height: calc(100% - 105px);">

        <sdy-table ref="tableOne" :tableDataPost="tableDataPost" :tableDataPostParam="searchForm" :option="option"
          @selection-change="handleSelectionChange" handleSelection="radio" style="height: calc(50% - 90px);">
        </sdy-table>

        <el-row style="margin-top: 60px;padding: 0;">
          <el-tabs v-model="editableTabsValue" type="card" @tab-click="handleClick">
            <el-tab-pane v-for="(item, index) in editableTabs" :key="index" :label="item.title" :name="item.name"></el-tab-pane>
          </el-tabs>
        </el-row>

        <sdy-table ref="tableTow" :tableDataPost="tableDataPost2" :tableDataPostParam="searchForm2" :option="option2"
          handleSelection="none" style="height: calc(50% - 60px);padding: 0;">
          <el-table-column fixed="right" label="操作" align='center' show-overflow-tooltip width="150">
            <template slot-scope="scope">
              <el-button type="text" @click="handleEdit(scope.row,scope.$index)">编辑</el-button>
              <el-button type="text" @click="handleDel(scope.row,scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </sdy-table>

      </div>

      <sdy-dialog ref="dialogOne" :dialogData="dialogData" :dialogTitle="dialogTitle" @dialog-save="dialogSave" width="1000px"></sdy-dialog>



    </div>
  </basic-container>
</template>

<script>
  import sdyTable from '@/views/ysxt/test/component/sdyTable';
  import sdyDialog from '@/views/ysxt/test/component/sdyDialog';
  import {
    getBatchCalculatePage
  } from '@/api/ysxt/meterReading/machinery'; //抄表复核示例

  export default {
    components: {
      "sdy-table": sdyTable,
      "sdy-dialog": sdyDialog
    },
    data() {
      return {
        editableTabsValue: '1',
        editableTabs: [{
          title: 'Tab 1',
          name: '1',
        }, {
          title: 'Tab 2',
          name: '2',
        }],
        searchShowIs: false,
        searchForm: {},
        getAllAreaList: [],
        getUserList: [],
        handleSelects: [],
        option: [{
          label: "你好",
          prop: "cbry",
        }, {
          label: "你2好",
          prop: "nsrsbh",
        }, {
          label: "你3好",
          prop: "nsrsbh",
        }, {
          label: "你4好",
          prop: "nsrsbh",
          width: "800"
        }, {
          label: "你好",
          prop: "nsrsbh",
          width: "800"
        }], //配置表头名称
        tableDataPost: getBatchCalculatePage,
        searchForm2: {
          bookId: "0d4d2bc3f89ec32de618ded44a6bedd7"
        },
        option2: [{
          label: "你4好",
          prop: "cbry",
          width: "800"
        }, {
          label: "你好",
          prop: "nsrsbh",
          width: "800"
        }], //配置表头名称
        tableDataPost2: getBatchCalculatePage,
        dialogData: {},
        dialogTitle: "编辑"
      }
    },
    created() {

    },
    mounted() {

    },
    methods: {
      handleClick(tab) { //切换表格
        console.log(tab, this.editableTabsValue);
        if (this.editableTabsValue == "1") {
          this.tableDataPost2 = getBatchCalculatePage; //切换请求接口
          this.searchForm2 = {
            bookId: "book108"
          }; //切换上传参数
          this.$refs.tableTow.search();
        } else if (this.editableTabsValue == "2") {
          this.tableDataPost2 = getBatchCalculatePage; //切换请求接口
          this.searchForm2 = {
            bookId: "book141"
          }; //切换上传参数
          this.$refs.tableTow.search();
        }
      },
      radioChange(row, index) {
        console.log(row, index)
      },
      handleDel(row, index) { //

        this.$confirm('是否确认删除', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(data => {

          this.tableData.splice(index, 1);

        }).catch(function(err) {})
      },
      handleEdit(row, index) { //编辑
        //console.log(row, index)
        this.dialogData = JSON.parse(JSON.stringify(row));
        this.dialogTitle="编辑";
        this.$refs.dialogOne.dialogIS = true;
      },
      dialogSave(datas) {
        console.log(datas)
      },
      searchShow() {
        this.searchShowIs = !this.searchShowIs
      },
      search() {
        this.$refs.tableOne.search(); //触发子组件的表格搜索  tableOne未ref 定义的子组件名
      },
      reset() {
        this.searchForm = {}
      },
      handleSelectionChange(val) {
        console.log(val)
        this.$refs.tableTow.search();

      },

    }
  }
</script>

<style>
  .userPUB .pubOut3top .selNameOutDiv {
    display: inline-block;
    margin-bottom: 10px;
  }

  .userPUB .pubOut3top .selNameOutDiv .pubInput {
    width: auto;
  }

  .userPUB .el-tabs .el-tabs__header {
    margin-bottom: 0;
  }

  .userPUB .pubOut3rightTableRow {
    padding: 0;
  }

  .userPUB .el-dialog-child {
    width: 100%;
  }
</style>
