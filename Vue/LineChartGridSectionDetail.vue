<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">
    <line-chart :chart-data="chartData" :options="options"
                style="position: relative; height: 200px; width: 200px;"></line-chart>

  </BasicGridSectionDetail>
</template>

<script>
import LineChart from '@/components/sectionDetail/chart/LineChart.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
import {generateBorderColor, generateBackgroundColor} from '@/common/color'

export default {
  name: "LineChartGridSectionDetail",

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
      // process x axis
      this.labels = result.map(record => record[extraData.xAxisFieldName]);

      // process y axis
      this.dataset = {
        borderWidth: 1,
        label: extraData.dataSetLabel,
        data: result.map(record => record[extraData.yAxisFieldName]),
        backgroundColor: generateBackgroundColor(2)[1],
        borderColor: generateBorderColor(2)[1],
      };

      this.options = {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: false,
            },
            gridLines: {
              display: true
            }
          }],
          xAxes: [{
            ticks: {
              autoSkip: false
            },
            gridLines: {
              display: false
            },
          }]
        },
        legend: {
          display: false,
          position: 'bottom'
        },
        layout: {
          padding: {
            top: 30,
          }
        },
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          datalabels: {
            anchor: 'end',
            align: 'end'
          }
        }
      }
    }
  },

  components: {
    BasicGridSectionDetail,
    LineChart
  }
}
</script>

<style scoped>

</style>