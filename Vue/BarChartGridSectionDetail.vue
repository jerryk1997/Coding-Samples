<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">
    <bar-chart :chart-data="chartData" :options="options" style="position: center; height: 200px; width: 200px;"></bar-chart>


  </BasicGridSectionDetail>
</template>

<script>
import BarChart from '@/components/sectionDetail/chart/BarChart.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
import {generateBorderColor, generateBackgroundColor} from '@/common/color'

export default {
  name: "BarChartGridSectionDetail",

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
        borderWidth: 1,
        label: extraData.dataSetLabel,
        data: this.partialResult.map(record => record[extraData.yAxisFieldName]),
        backgroundColor: generateBackgroundColor(this.partialResult.length),
        borderColor: generateBorderColor(this.partialResult.length),
      };

      // generate color
      if (extraData.isColorfulBar) {
        this.dataset.backgroundColor = generateBackgroundColor(this.partialResult.length);
        this.dataset.borderColor = generateBorderColor(this.partialResult.length);
      } else {
        // choose a color in random
        this.dataset.backgroundColor = generateBackgroundColor(this.partialResult.length)[this.partialResult.length - 1];
        this.dataset.borderColor = generateBorderColor(this.partialResult.length)[this.partialResult.length - 1];
      }

      // to display more data
      let toolTipFooterCallback = (tooltipItems) => {
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
              autoSkip: false,
              display: false
            }
          }]
        },
        legend: {
          display: false,
          position: 'bottom'
        },

        responsive: true,
        maintainAspectRatio: false,
        tooltips: {
          callbacks: {
            footer: toolTipFooterCallback
          }
        },
        plugins: {
          datalabels: {
            anchor: 'end',
            align: 'end',
            display: false
          },
        },
        annotation: {
          drawTime: 'afterDatasetsDraw',
          annotations: [
            {
              drawTime: 'afterDraw',
              type: 'line',
              id: 'hLine',
              mode: 'horizontal',
              scaleID: 'y-axis-0',
              value: '35',  // data-value at which the line is drawn
              borderWidth: 2,
              borderColor: 'black',
              borderDash: [2, 2],
            }
          ]
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

    addAnnotation() {

    },

    removeAnnotation() {

    }
  },

  components: {
    BasicGridSectionDetail,
    BarChart
  }
}
</script>

<style scoped>

</style>