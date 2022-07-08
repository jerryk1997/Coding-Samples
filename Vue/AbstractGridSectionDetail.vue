<template>
  <el-row class="sectionDetail">
    <div v-if="dataPackage[2] !== undefined && typeof dataPackage[2] != 'undefined'">
      <div v-if="sectionDetail.type === WORD_CLOUD">
        <WordCloudGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === BAR_CHART">
        <BarChartGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === PIE_CHART">
        <PieChartGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === LINE_CHART">
        <LineChartGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === STATS">
        <StatsGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === SCATTER_CHART">
        <ScatterChartGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === GRAPH_NETWORK">
        <GraphNetworkGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === RADAR_CHART">
        <RadarChartGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else-if="sectionDetail.type === DEPENDENCY_CHART">
        <DependencyChartGridSectionDetail :sectionDetail="sectionDetail" :presentationId="presentationId" :dataPackage="dataPackage"/>
      </div>
      <div v-else>
        <el-alert
            :title="`Unexpected Section Detail Type: ${sectionDetail.type}`"
            type="error">
        </el-alert>
      </div>
    </div>
    <div v-else>
      <el-alert
          :title="`Unexpected version`"
          type="error">
      </el-alert>
    </div>
  </el-row>
</template>

<script>
import WordCloudGridSectionDetail from "@/components/gridSectionDetail/WordCloudGridSectionDetail";
import BarChartGridSectionDetail from "@/components/gridSectionDetail/BarChartGridSectionDetail";
import PieChartGridSectionDetail from "@/components/gridSectionDetail/PieChartGridSectionDetail";
import LineChartGridSectionDetail from "@/components/gridSectionDetail/LineChartGridSectionDetail";
import StatsGridSectionDetail from "@/components/gridSectionDetail/StatsGridSectionDetail";
import ScatterChartGridSectionDetail from "@/components/gridSectionDetail/ScatterChartGridSectionDetail";
import GraphNetworkGridSectionDetail from "@/components/gridSectionDetail/GraphNetworkGridSectionDetail";
import RadarChartGridSectionDetail from "@/components/gridSectionDetail/RadarChartGridSectionDetail";
import DependencyChartGridSectionDetail from "@/components/gridSectionDetail/DependencyChartGridSectionDetail";
import {
  SECTION_TYPE_BAR_CHART,
  SECTION_TYPE_LINE_CHART,
  SECTION_TYPE_PIE_CHART,
  SECTION_TYPE_STATS,
  SECTION_TYPE_WORD_CLOUD,
  SECTION_TYPE_SCATTER_CHART,
  SECTION_TYPE_GRAPH_NETWORK,
  SECTION_TYPE_RADAR_CHART,
  SECTION_TYPE_DEPENDENCY_CHART
} from "@/common/const";

export default {
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
    }
  },
  data() {
    return {
      // constant
      WORD_CLOUD: SECTION_TYPE_WORD_CLOUD,
      BAR_CHART: SECTION_TYPE_BAR_CHART,
      PIE_CHART: SECTION_TYPE_PIE_CHART,
      LINE_CHART: SECTION_TYPE_LINE_CHART,
      STATS: SECTION_TYPE_STATS,
      SCATTER_CHART: SECTION_TYPE_SCATTER_CHART,
      GRAPH_NETWORK: SECTION_TYPE_GRAPH_NETWORK,
      RADAR_CHART: SECTION_TYPE_RADAR_CHART,
      DEPENDENCY_CHART: SECTION_TYPE_DEPENDENCY_CHART
    }
  },
  components: {
    DependencyChartGridSectionDetail,
    RadarChartGridSectionDetail,
    GraphNetworkGridSectionDetail,
    ScatterChartGridSectionDetail,
    StatsGridSectionDetail,
    LineChartGridSectionDetail,
    PieChartGridSectionDetail,
    BarChartGridSectionDetail,
    WordCloudGridSectionDetail,
  }
}
</script>

<style scoped>
div {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>