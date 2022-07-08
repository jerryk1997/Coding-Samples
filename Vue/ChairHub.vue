<template>
  <div>
    <mq-layout mq="laptop+">
      <el-alert
        title="You need to login-in to view the page"
        type="error"
        v-if="!isLogin && !isAppLoading"
      >
        &nbsp;<el-button
          type="warning"
          plain
          size="mini"
          @click="navigateToHomePage"
          >Return to the Home Page</el-button
        >
      </el-alert>
      <el-main>
        <ChairhubBannerDetail></ChairhubBannerDetail>
        <b-row>
          <h1 style="position:relative; left:50px; top:20px;">
            Presentations
          </h1>
          <el-divider></el-divider>
        </b-row>
    <!--    <b-row >
          &lt;!&ndash; <b-col cols="24" md="0.5" >
           <b-button
             class="button"
             @click="filter"
             size="xsm"
             >
             <img src="@/assets/filter.png/" height="80%" width="100%"/>
           </b-button>
           </b-col> &ndash;&gt;
           <b-col cols="24" md="0.5" offset="8">
             <b-button
              class="button"
              @click="search"
              size="sm"
              >
              <img src="@/assets/search.png/" height="70%" width="100%"/>
            </b-button>
           </b-col>
        </b-row>-->

        <div class="infinite-list-wrapper" v-if="isLogin">
        <b-row>

          <b-col  width="100%">
            <b-card v-if="isPresentationListEmpty">
              <EmptyChairhub />
            </b-card>

            <ul class="infinite-list" v-infinite-scroll="loadMorePresentation" infinite-scroll-disabled="disabled" v-loading="isLoading" >
              <li v-for="(presentation, index) in presentations" :key="presentation.id" style="padding-bottom: 20px;">
                <zoom-center-transition :duration="500" :delay="100 * (index - 1)">
                  <b-button type="text" class="presentationCardButton" v-show="show" @click="viewPresentation(presentation.id)">
                  <b-card class="presentationCard">
                    <p class="presentation-name">
                      {{ presentation.name }}
                    </p>

                    <hr class="half-rule"/>

                    <p class="presentation-description">
                      {{ presentation.description }}

                    </p>
                    <p class="presentation-creator">
                      Created by: {{ presentation.creatorIdentifier}}
                    </p>
                  </b-card>
                  </b-button>
                </zoom-center-transition>
              </li>
            </ul>
          </b-col>
        </b-row>
        </div>
      </el-main>
    </mq-layout>
    <mq-layout mq="mobile">
     <el-alert
        title="You need to login-in to view the page"
        type="error"
        v-if="!isLogin && !isAppLoading"
      >
        &nbsp;<el-button
          type="warning"
          plain
          size="mini"
          @click="navigateToHomePage"
          >Return to the Home Page</el-button
        >
      </el-alert>
      <el-main>
        <ChairhubBannerDetail></ChairhubBannerDetail>
        <b-row>
          <h3 style="position:relative; left:50px; top:20px;">
            Presentations
          </h3>
          <el-divider></el-divider>
        </b-row>
        <!--    <b-row >
              &lt;!&ndash; <b-col cols="24" md="0.5" >
               <b-button
                 class="button"
                 @click="filter"
                 size="xsm"
                 >
                 <img src="@/assets/filter.png/" height="80%" width="100%"/>
               </b-button>
               </b-col> &ndash;&gt;
               <b-col cols="24" md="0.5" offset="8">
                 <b-button
                  class="button"
                  @click="search"
                  size="sm"
                  >
                  <img src="@/assets/search.png/" height="70%" width="100%"/>
                </b-button>
               </b-col>
            </b-row>-->

        <div class="infinite-list-wrapper" v-if="isLogin">
          <b-row>

            <b-col  width="100%">
              <b-card v-if="isPresentationListEmpty">
                <EmptyChairhub />
              </b-card>

              <ul class="infinite-list" v-infinite-scroll="loadMorePresentation" infinite-scroll-disabled="disabled" v-loading="isLoading" >
                <li v-for="(presentation, index) in presentations" :key="presentation.id" style="padding-bottom: 20px;">
                  <zoom-center-transition :duration="500" :delay="100 * (index - 1)">
                    <b-button type="text" class="presentationCardButton" v-show="show" @click="viewPresentation(presentation.id)">
                      <b-card class="presentationCard">
                        <p class="presentation-name-mobile">
                          {{ presentation.name }}
                        </p>

                        <hr class="half-rule"/>

                        <p class="presentation-description-mobile">
                          {{ presentation.description }}

                        </p>
                        <p class="presentation-creator-mobile">
                          Created by: {{ presentation.creatorIdentifier}}
                        </p>
                      </b-card>
                    </b-button>
                  </zoom-center-transition>
                </li>
              </ul>
            </b-col>
          </b-row>
        </div>
      </el-main>
    </mq-layout>
  </div>
</template>

<script>
  import ChairhubBannerDetail from "@/components/chairHub/ChairhubBannerDetail.vue";
  import {ZoomCenterTransition} from "vue2-transitions";
  import EmptyChairhub from "@/components/emptyStates/EmptyChairhub.vue";

export default {
  name: 'Chairhub',
  props: {
    id: String,
  },
  data() {
    return {
      show: false,
      count: 0,
      readMoreActivated: false
    }
  },
  watch: {
    'isError'() {
      if (!this.isError) {
        return
      }
      this.$notify.error({
        title: 'Chairhub Presentation list API request fail',
        message: this.$store.state.presentation.presentationListStatus.apiErrorMsg,
        duration: 0
      });
    }
  },
  computed: {
    isLogin() {
      return this.$store.state.userInfo.isLogin
    },
    isAppLoading() {
      return this.$store.state.isPageLoading
    },
    isLoading() {
      return this.$store.state.presentation.presentationListStatus.isLoading
    },
    presentations() {
      return this.$store.state.presentation.presentationList;
    },
    isPresentationListEmpty() {
      return this.$store.state.presentation.presentationList.length <= 0;
    },
    isError() {
      return this.$store.state.presentation.presentationListStatus.isApiError
    },
  },
  components: {
    EmptyChairhub,
    ChairhubBannerDetail,
    ZoomCenterTransition
  },
  methods: {
    loadPresentations() {
      this.show = true;
    },
    loadMorePresentation () {
      this.count += 5
    },
    viewPresentation(id) {
      this.$router.push("/chairhub/" + id);
    },
    activateReadMore() {
      this.readMoreActivated = true;
    },
    navigateToHomePage() {
      this.$router.replace("/home");
    },
  },
  mounted() {
    this.$store.dispatch('getChairhubPresentationList')
    this.loadPresentations();
  }
}
</script>

<style scoped>
h1 {
  font-family: 'IBM Plex Sans', sans-serif;
}

h3 {
   font-family: 'IBM Plex Sans', sans-serif;
 }

.button {
  background: transparent;
  border: transparent;
  position: relative;
  height: 50px;
  width: 50px;
}

/* List formatting */

.presentationCardButton {
  background-color: transparent;
  border-color: transparent;
  width: 100%;
}

li {
  display: flex;
  justify-content: space-around;
}

.infinite-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* List formatting end */

/* PRESENTATION CARD */
.presentationCard {
  font-family: 'IBM Plex Sans', sans-serif;
  background-color: #ececec;
  transition: .6s;
  max-width: 100%;
  max-height: 350px;
}

.presentationCard p {
  color: #2D889C;
  max-width: 1200px;
  display: block;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.presentation-name {
  font-size: 27px;
  text-align: left;
}

.half-rule {
  background-color: #2D889C;
}

.presentation-description {
  font-size: 18px;
  text-align: left;
  max-height: 150px;

}

.presentation-creator {
  text-align: right;
  font-size: 20px;
}

.presentationCard:hover {
  box-shadow: 0 0 40px rgba(33,33,33,.2);
  background-color: #2D889C;
}

.presentationCard:hover p{
  color: white;
}

.presentationCard:hover hr{
  background-color: white;
}

/* PRESENTATION CARD END */

.presentation-name-mobile {
  font-size: 20px;
  text-align: left;
}

.presentation-description-mobile {
  font-size: 18px;
  text-align: left;
  max-height: 150px;

}

.presentation-creator-mobile {
  text-align: right;
  font-size: 12px;
}
</style>