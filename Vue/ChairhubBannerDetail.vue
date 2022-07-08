<template>
  <div>
    <mq-layout mq="laptop+">
      <zoom-center-transition :duration="500">
        <b-row class="b-row" align-v="center">
          <!-- <div v-show="show"> -->
          <b-col cols="12" md="2">
            <img class="img-fluid" alt="Chairhub logo" src="@/assets/ChairhubLogo.png" />
          </b-col>
          <b-col cols="12" md="10">
            <b-row align-h="left">
              <h1>
                <span class="chairhub_title">Chair<span class="hub">HUB</span></span> -- Discover Other Presentations
              </h1>
            </b-row>
          </b-col>
          <!-- </div> -->
        </b-row>
      </zoom-center-transition>
    </mq-layout>
    <mq-layout mq="mobile">
      <zoom-center-transition :duration="500">
        <b-row class="b-row">
          <!-- <div v-show="show"> -->
          <b-row style="margin-bottom: 20px; width:100%;">
            <b-col align="center">
            <img class="img-fluid" alt="Chairhub logo" src="@/assets/ChairhubLogo.png" />
            </b-col>
          </b-row>
          <b-row align-h="center">
            <h3 class="mobile_words">
              <span class="chairhub_title_mobile">Chair<span class="hub">HUB</span></span>
            </h3>
            <el-divider></el-divider>
            <h2 class="mobile_words">
              Discover Other Presentations
            </h2>
          </b-row>
          <!-- </div> -->
        </b-row>
      </zoom-center-transition>
    </mq-layout>
  </div>
</template>

<script>
import { ZoomCenterTransition } from "vue2-transitions";
export default {
  name: "ChairhubBannerDetail",
  data: () => ({
    show: false,
  }),
  props: {
    msg: String,
  },
  mounted() {
    this.loadBanner();
  },
  methods: {
    loadBanner() {
      this.show = true;
    },
  },
  components: {
    ZoomCenterTransition,
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
img {
  width: 180px;
  height: 180px;
  text-align: center;
  display: block;
  margin: 0 auto;;
}
.b-row {
  background: #1e9fc4;
  padding: 32px;
  margin: 0;
}

.b-col {
  min-width: 500px;
}
h1 {
  color: white;
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 50px;
}

.chairhub_title {
  text-align: center;
  letter-spacing: 20px;
}

.hub {
  color: #ff945b;
}

.mobile_words {
  color: white;
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 30px;
  margin-bottom: 5px;
  text-align: center;
}

.chairhub_title_mobile {
  text-align: center;

  margin-left: 5px;
  letter-spacing: 20px;
}

</style>