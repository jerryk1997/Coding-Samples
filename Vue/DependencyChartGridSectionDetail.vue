<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">
    <dependency-chart :chart-data="chartData" style="position: center; height: 200px; width: 200px; margin-left: 8px"></dependency-chart>


  </BasicGridSectionDetail>
</template>

<script>
import DependencyChart from '@/components/sectionDetail/chart/DependencyChart.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";
import {generateBorderColor, generateBackgroundColor} from '@/common/color'

export default {
  name: "DependencyChartGridSectionDetail",

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
      labels2: [],
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
        labels2: this.labels2,
        datasets: [this.dataset]
      }
    }
  },

  methods: {
    updateVisualisation({result, extraData}) {
      this.partialResult = result.slice(0, extraData.numOfResultToDisplay);


      // process x axis
      this.labels = this.partialResult.map(record => record[extraData.xAxisFieldName]);
      this.labels2 = this.partialResult.map(record => record[extraData.xAxisFieldName2]);

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
              autoSkip: false
            }
          }]
        },
        legend: {
          display: true,
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
            align: 'end'
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
    DependencyChart
  }
}
</script>

<style scoped>

</style>