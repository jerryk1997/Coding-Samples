<template>
  <div>
    <mq-layout mq="laptop+">
      <el-row v-loading="sectionDetail.status.isLoading">
          <div class="title">
            {{ sectionDetail.title }}
          </div>
          <el-alert
              v-if="sectionDetail.status.isApiError"
              :title="sectionDetail.status.apiErrorMsg"
              :description="sectionDetail.status.apiErrorMsgDetail"
              show-icon
              type="error"
              class="errorMessage"
          >
          </el-alert>
          <el-alert
              v-if="!this.hasData"
              title="No Data to display"
              type="info"
              class="noDataToDisplay"
          >
          </el-alert>
          <slot v-else></slot>
      </el-row>
    </mq-layout>
    <mq-layout mq="mobile">
      <b-card>
        <el-row v-loading="sectionDetail.status.isLoading">
          <div class="title">
            {{ sectionDetail.title }}
          </div>
          <el-alert
              v-if="sectionDetail.status.isApiError"
              :title="sectionDetail.status.apiErrorMsg"
              :description="sectionDetail.status.apiErrorMsgDetail"
              show-icon
              type="error"
              class="errorMessage"
          >
          </el-alert>
          <el-alert
              v-if="!this.hasData"
              title="No Data to display"
              type="info"
              class="noDataToDisplay"
          >
          </el-alert>
          <slot v-else></slot>
        </el-row>
      </b-card>
    </mq-layout>
  </div>
</template>

<script>
import { deepCopy } from "@/common/utility";

export default {
  props: {
    sectionDetail: {
      type: Object,
      required: true,
    },
    presentationId: {
      type: String,
      required: true,
    },
    dataPackage: {
      type: Array,
      required: true,
    },
    hasData: {
      type: Boolean,
      required: true,
    },
  },
  watch: {
    version() {
      this.sendAnalysisRequest();
    },
  },
  created() {
    this.sendAnalysisRequest();
  },


  methods: {
    sendAnalysisRequest() {
      this.$store
          .dispatch("sendAnalysisRequest", {
            id: this.sectionDetail.id,
            presentationId: this.presentationId,
            version: this.dataPackage[2],
            conventionId: this.dataPackage[0],
            conferenceType: this.dataPackage[1]
          })
          .then(() => {
            this.$emit("update-visualisation", {
              presentationId: this.presentationId,
              selections: this.sectionDetail.selections,
              involvedRecords: this.sectionDetail.involvedRecords,
              filters: this.sectionDetail.filters,
              joiners: this.sectionDetail.joiners,
              result: this.sectionDetail.result,
              groupers: this.sectionDetail.groupers,
              sorters: this.sectionDetail.sorters,
              extraData: this.sectionDetail.extraData,
              versionId: this.dataPackage[2],
              conventionId: this.dataPackage[0],
              conferenceType: this.dataPackage[1]
            });
          });
    },
  },
};
</script>

<style scoped>
.title {
  font-family: 'IBM Plex Sans';
  font-weight: bold;
  font-size: 13px;
  text-align: center;
  padding-bottom: 15px;
  color: #2D889C;
}

.noDataToDisplay {
  margin-top: 10px;
  margin-bottom: 10px;
}

.errorMessage {
  margin-top: 10px;
}

</style>
