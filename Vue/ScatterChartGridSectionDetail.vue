<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">

    <scatter-chart :chart-data="chartData" :options="options"
                   style="position: relative; height: 200px; width: 200px;"></scatter-chart>

  </BasicGridSectionDetail>
</template>

<script>
import ScatterChart from '@/components/sectionDetail/chart/ScatterChart.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
import {generateBorderColor, generateBackgroundColor} from '@/common/color'

export default {
  name: "ScatterChartGridSectionDetail",

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

      var xyArray = [];
      for(var i = 0; i < this.partialResult.length; i++) {
        xyArray.push({
          x: this.partialResult[i][extraData.xAxisFieldName],
          y: this.partialResult[i][extraData.yAxisFieldName]
        });
      }
      // process y axis
      this.dataset = {
        borderWidth: 1,
        label: extraData.dataSetLabel,
        data: xyArray,
        pointRadius: 5,
        pointBackgroundColor: generateBackgroundColor(this.partialResult.length),
        pointBorderColor: generateBorderColor(this.partialResult.length),
      };
      // generate color
      if (extraData.isColorfulBar) {
        this.dataset.pointBackgroundColor = generateBackgroundColor(this.partialResult.length);
        this.dataset.pointBorderColor = generateBorderColor(this.partialResult.length);
      } else {
        // choose a color in random
        this.dataset.pointBackgroundColor = generateBackgroundColor(this.partialResult.length)[this.partialResult.length - 1];
        this.dataset.pointBorderColor = generateBorderColor(this.partialResult.length)[this.partialResult.length - 1];
      }

      // to display more data
      let toolTipLabelCallback = (tooltipItems) => {
        return extraData.dataSetLabel + ": " + tooltipItems.yLabel;
      }
      let toolTipTitleCallback = (tooltipItems) => {
        let currentIndex = tooltipItems[0].index;
        return extraData.fieldsShownInToolTips.map(f => `${f.label}: ${this.partialResult[currentIndex][f.field]}`);
      };

      // process tooltip callback
      this.options = {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            },
            gridLines: {
              display: true
            }
          }],
          xAxes: [{
            gridLines: {
              display: false
            },
            ticks: {
              beginAtZero: true,
              stepSize: 1
            }
          }]
        },
        legend: {
          display: false,
          position: 'bottom'
        },

        responsive: true,
        maintainAspectRatio: true,
        tooltips: {
          mode: 'index',
          callbacks: {
            title: toolTipTitleCallback,
            label: toolTipLabelCallback
          }
        },
        plugins: {
          datalabels: {
            anchor: 'end',
            align: 'end',
            // show both value and percentage
            formatter: () => {
              return ``;
            },
          }
        }
      }
    },

    addTooltip(tooltips) {
      tooltips.push({
        label: '',
        field: '',
      })
    },

    removeTooltip(tooltips, tooltipToRemove) {
      let index = tooltips.indexOf(tooltipToRemove);
      tooltips.splice(index, 1)
    },
  },

  components: {
    BasicGridSectionDetail,
    ScatterChart
  }
}
</script>

<style scoped>
</style>