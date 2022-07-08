<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                          :dataPackage="dataPackage"
                          @update-visualisation="updateVisualisation">
    <el-table
        :data="tableData"
        stripe

        style="width: 200px;">

      <el-table-column
          prop="type"
          label="Type" width="auto">
      </el-table-column>

      <el-table-column
          prop="value"
          label="Value" width="auto">
        <template slot-scope="scope">
          <el-popover trigger="hover" placement="top-start">
            <p> No. of Reviewers: {{ scope.row.numIds }}</p>
            <div slot="reference" class="name-wrapper">
              <el-button class="hovered-text" type="text"> {{ scope.row.value }} </el-button>
            </div>
          </el-popover>
        </template>
      </el-table-column>

      <el-table-column
          prop="reviewer"
          label="Corresponding Reviewer ID"
          width="auto">
        <template slot-scope="scope">
          {{ scope.row.reviewer }}
        </template>
      </el-table-column>

    </el-table>

  </BasicGridSectionDetail>
</template>

<script>
import {max, mean, median, min, standardDeviation, sum} from 'simple-statistics'
import BasicSectionDetail from '@/components/sectionDetail/BasicSectionDetail.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
export default {
  name: "StatsGridSectionDetail",
  props: {
    sectionDetail: {
      type: Object,
      required: true
    },
    presentationId: {
      type: String,
      required: true
    },
    dataPackage: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      tableData: []
    }
  },
  computed: {
    hasData() {
      return this.tableData.length !== 0;
    }
  },
  methods: {
    updateVisualisation({result, selections, extraData}) {
      this.tableData = [];
      if (result.length === 0) {
        return
      }
      let data = result.map(r => r[selections[1].rename]);
      extraData.types.forEach(t => {
        var rIds = [];
        var rId;
        var stringBuilder;
        switch (t) {
          case 'min':
            stringBuilder = "";
            rIds = result.filter(r => r[selections[1].rename] == min(data)).map(
                r => r[selections[0].rename]);
            for (rId of rIds) {
              stringBuilder = stringBuilder + rId + ", ";
            }
            stringBuilder = stringBuilder.substring(0, stringBuilder.length-2);
            this.tableData.push({
              type: 'Min',
              value: min(data),
              numIds: rIds.length,
              reviewer: stringBuilder,
            });
            break;
          case 'max':
            stringBuilder = "";
            rIds = result.filter(r => r[selections[1].rename] == max(data)).map(
                r => r[selections[0].rename]);
            for (rId of rIds) {
              stringBuilder = stringBuilder + rId + ", ";
            }
            stringBuilder = stringBuilder.substring(0, stringBuilder.length-2);
            this.tableData.push({
              type: 'Max',
              value: max(data),
              numIds: rIds.length,
              reviewer: stringBuilder,
            });
            break;
          case 'sum':
            this.tableData.push({
              type: 'Sum',
              value: sum(data),
              numIds: result.length,
              reviewer: "-",
            });
            break;
          case 'avg':
            this.tableData.push({
              type: 'Mean',
              value: mean(data).toFixed(2),
              numIds: result.length,
              reviewer: "-",
            });
            break;
          case 'median':
            this.tableData.push({
              type: 'Median',
              value: median(data),
              numIds: result.length,
              reviewer: "-",
            });
            break;
          case 'std':
            this.tableData.push({
              type: 'ST.DEV',
              value: standardDeviation(data).toFixed(2),
              numIds: result.length,
              reviewer: "-",
            });
            break;
        }
      })
    }
  },
  components: {
    BasicGridSectionDetail,
  }
}
</script>

<style scoped>
.hovered-text {
  color: #606266
}
</style>