<template>
  <BasicGridSectionDetail :section-detail="sectionDetail" :presentation-id="presentationId" :has-data="hasData"
                        :dataPackage="dataPackage"
                        @update-visualisation="updateVisualisation">
    <word-cloud
        :data="words" style="position: relative; height: 200px; width: 200px; margin-left: 8px">
    </word-cloud>
  </BasicGridSectionDetail>
</template>

<script>
import WordCloud from '@/components/sectionDetail/chart/WordCloud.vue'
import BasicGridSectionDetail from "@/components/gridSectionDetail/BasicGridSectionDetail";

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
      required: true
    }
  },

  data() {
    return {
      // word cloud related field
      words: [],
    }
  },

  computed: {
    hasData() {
      return this.words.length !== 0;
    }
  },

  methods: {
    updateVisualisation({result, selections, extraData}) {
      let fieldName = selections[0].rename;
      let wordsCount = {};
      let delimiterRegex = new RegExp(extraData.delimiters.join('|'), 'g');
      // will only require at least one selection
      // count the occurrence of word
      result.forEach(r => {
        r[fieldName].split(delimiterRegex)
            .filter(w => !extraData.ignoreWords.includes(w.toLowerCase())) // filter ignoreWords
            .forEach(w => {
              // skip empty string
              if (w.length === 0) {
                return
              }
              // normalized word e.g. 'digital world' -> `Digital World`
              let normalizedW = this.capitalizeFirstWord(w);
              // put in the count map
              if (wordsCount.hasOwnProperty(normalizedW)) {
                wordsCount[normalizedW]++
              } else {
                wordsCount[normalizedW] = 1;
              }
            })
      });
      // generate format as VueWordCloud required
      let words = [];
      for (let word in wordsCount) {
        if (wordsCount.hasOwnProperty(word)) {
          words.push([word, wordsCount[word]])
        }
      }
      // sort and keep the first twenty words
      words.sort((a, b) => {
        return b[1] - a[1]
      });
      words = words.slice(0, 20);
      this.words = words;
    },

    capitalizeFirstWord(str) {
      return str.replace(/\b\w/g, l => l.toUpperCase());
    }
  },

  components: {
    BasicGridSectionDetail,
    WordCloud,
  }
}
</script>