<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">
    <pie-chart :chart-data="chartData" :options="options"
               style="position: relative; height: 200px; width: 200px; margin-left: 8px"></pie-chart>

  </BasicGridSectionDetail>
</template>

<script>
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
import PieChart from '@/components/sectionDetail/chart/PieChart.vue'
import {generateBorderColor, generateBackgroundColor} from '@/common/color'

export default {
  name: "PieChartGridSectionDetail",

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
      labels: [],
      dataset: {},
      options: {},
    }
  },

  computed: {
    hasData() {
      return this.labels.length !== 0;
    },

    chartData() {
      return {
        labels: this.labels,
        datasets: [this.dataset]
      }
    }
  },

  methods: {
    updateVisualisation({result, extraData}) {
      let displayedResult = result.slice(0, extraData.numOfResultToDisplay);
      let remainedResult = result.slice(extraData.numOfResultToDisplay, result.length);

      // process category
      this.labels = displayedResult.map(record => record[extraData.categoryFieldName]);
      if (remainedResult.length !== 0) {
        this.labels.push("Other");
      }

      // process value
      let data = displayedResult.map(record => record[extraData.valueFieldName]);
      if (remainedResult.length !== 0) {
        // take sum of the rest
        data.push(remainedResult.map(record => record[extraData.valueFieldName]).reduce((a, b) => a + b, 0));
      }

      // generate dataset
      this.dataset = {
        borderWidth: 1,
        label: extraData.dataSetLabel,
        data,
        backgroundColor: generateBackgroundColor(data.length),
        borderColor: generateBorderColor(data.length),
      };

      // process options
      this.options = {
        legend: {
          display: false,
          position: 'bottom'
        },
        layout: {
          padding: {
            top: 15,
          }
        },
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          datalabels: {
            // show both value and percentage
            formatter: (value, ctx) => {
              let sum = 0;
              let dataArr = ctx.chart.data.datasets[0].data;
              dataArr.map(data => {
                sum += data;
              });
              let percentage = (value * 100 / sum).toFixed(2) + "%";
              return `${percentage} (${value})`;
            },
          }
        }
      }
    }
  },

  components: {
    BasicGridSectionDetail,
    PieChart
  }
}
</script>

<style scoped>

</style>