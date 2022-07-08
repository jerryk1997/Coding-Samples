<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">

    <radar-chart :chart-data="chartData" :options="options" style="position: relative; height: 200px; width: 200px;"></radar-chart>

  </BasicGridSectionDetail>
</template>

<script>
import RadarChart from '@/components/sectionDetail/chart/RadarChart.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
import {generateBorderColor, generateBackgroundColor} from '@/common/color'

export default {
  name: "RadarChartGridSectionDetail",

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
      partialResult: [],
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
      this.partialResult = result.slice(0, extraData.numOfResultToDisplay);
      // process x axis
      this.labels = this.partialResult.map(record => record[extraData.xAxisFieldName]);

      // process y axis
      this.dataset = {
        borderWidth: 2,
        data: this.partialResult.map(record => record[extraData.yAxisFieldName]),
        backgroundColor: 'rgba(175, 175, 175, 0.3)',
        borderColor: 'rgb(175, 175, 175, 0.6)',
        pointRadius: 4,
        pointBorderColor: generateBorderColor(this.partialResult.length),
        pointBackgroundColor: generateBackgroundColor(this.partialResult.length),
      };

      this.options = {
        legend: false,
        labels: {
          display: false
        },
        responsive: true,
        maintainAspectRatio: false,
        tooltips: {
          display: false
        },
        plugins: {
          datalabels: {
            backgroundColor: generateBackgroundColor(this.partialResult.length),
            anchor: 'end',
            align: 'end'
          }
        },
        scale: {
          ticks: {
            display: false
          },
          pointLabels: {
            callback: value => {
              if (value.toString().length > 24) {
                return value.toString().substr(0, 23) + '...'; //truncate
              } else {
                return value;
              }
            }
          }
        }
      }
    },
  },

  components: {
    BasicGridSectionDetail,
    RadarChart
  }
}
</script>