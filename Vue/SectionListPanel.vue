<template>
  <div>
    <mq-layout mq="mobile">
      <b-button class="fab btn-lg" variant="primary" v-b-modal.modal-1
        ><b-icon icon="plus"> </b-icon
      ></b-button>
      <div
        v-loading="isLoadingDBMetaData || isLoadingSectionList"
        v-if="!isNewPresentation"
      >
        <b-modal
          ref="modal-1"
          centered
          id="modal-1"
          title="Add Section"
          hide-footer="true"
        >
          <b-row class="px-3" v-if="!isSectionListEmpty">
            <div class="selectDataOptionHeader">
              Convention:
              <el-select
                class="selectField dataOptionInput"
                v-model="selectedConventionId"
                placeholder="Please select a convention"
              >
                <el-option
                  v-for="c in conventionList"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                >
                </el-option>
              </el-select>
            </div>
            <div
              class="selectField selectDataOptionHeader"
              v-if="isConventionSelected"
            >
              Conference Type:
              <el-select
                class="dataOptionInput"
                v-model="selectedConferenceType"
                placeholder="Please select a conference type"
              >
                <el-option
                  v-for="t in conferenceTypes"
                  :key="t"
                  :label="t"
                  :value="t"
                >
                </el-option>
              </el-select>
            </div>
            <div
              class="selectField selectDataOptionHeader"
              v-if="isConferenceTypeSelected"
            >
              Version:
              <el-select
                class="dataOptionInput"
                v-model="presentationFormVersion"
                placeholder="Please select a version"
              >
                <el-option v-for="v in versions" :key="v" :label="v" :value="v">
                </el-option>
              </el-select>
            </div>
            <el-button
              class="selectionInputButton"
              v-if="haveSufficientInformation"
              icon="el-icon-check"
              type="success"
              @click="useData"
              style="margin-top: 10px"
            >
              Use data
            </el-button>
          </b-row>
          <b-row class="px-3" v-if="isDataSaved">
            <div slot="header" class="clearfix">
              <span> Add section </span>
            </div>
            <el-select
              class="selectionInputMobile"
              v-model="selectedNewSection"
              placeholder="Please select a section to add"
              filterable
            >
              <el-option-group
                v-for="group in predefinedSections"
                :key="group.label"
                :label="group.label"
              >
                <el-option
                  v-for="item in group.options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-option-group>
            </el-select>
            <el-button
              class="selectionInputButton"
              icon="el-icon-plus"
              type="success"
              @click="addNewSection"
              >Add New Section</el-button
            >
          </b-row>
        </b-modal>
        <br />
        <el-alert
          v-if="isSectionListApiError"
          :title="sectionListApiErrorMsg"
          type="error"
          show-icon
        >
        </el-alert>

        <abstract-section-detail
          class="presentation-section mx-4"
          v-for="section in sectionList"
          :sectionDetail="section"
          :key="section.id"
          :presentationId="presentationId"
          :dataPackage="dataPackage"
        />
        <EmptySection v-if="isSectionListEmpty" />
      </div>
    </mq-layout>
    <mq-layout mq="laptop+">
      <div
        v-loading="isLoadingDBMetaData || isLoadingSectionList"
        v-if="!isNewPresentation"
      >
        <el-aside width="300px" class="addRowRightAlign" v-if="isLogin">
          <el-card>
            <div slot="header" class="clearfix">
              <span> Select Data </span>
            </div>
            <div class="selectDataOptionHeader">
              Convention:
              <el-select
                class="selectField dataOptionInput"
                v-model="selectedConventionId"
                placeholder="Please select a convention"
              >
                <el-option
                  v-for="c in conventionList"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                >
                </el-option>
              </el-select>
            </div>
            <div
              class="selectField selectDataOptionHeader"
              v-if="isConventionSelected"
            >
              Conference Type:
              <el-select
                class="dataOptionInput"
                v-model="selectedConferenceType"
                placeholder="Please select a conference type"
              >
                <el-option
                  v-for="t in conferenceTypes"
                  :key="t"
                  :label="t"
                  :value="t"
                >
                </el-option>
              </el-select>
            </div>
            <div
              class="selectField selectDataOptionHeader"
              v-if="isConferenceTypeSelected"
            >
              Version:
              <el-select
                class="dataOptionInput"
                v-model="presentationFormVersion"
                placeholder="Please select a version"
              >
                <el-option v-for="v in versions" :key="v" :label="v" :value="v">
                </el-option>
              </el-select>
            </div>
            <el-button
              class="selectionInputButton"
              v-if="haveSufficientInformation"
              icon="el-icon-check"
              type="success"
              @click="useData"
              style="margin-top: 10px"
            >
              Use data
            </el-button>
          </el-card>
          <el-card v-if="isDataSaved">
            <div slot="header" class="clearfix">
              <span> Add section </span>
            </div>
            <el-select
              class="selectionInput"
              v-model="selectedNewSection"
              placeholder="Please select a section to add"
              filterable
            >
              <el-option-group
                v-for="group in predefinedSections"
                :key="group.label"
                :label="group.label"
              >
                <el-option
                  v-for="item in group.options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-option-group>
            </el-select>
            <el-button
              class="selectionInputButton"
              icon="el-icon-plus"
              type="success"
              @click="addNewSection"
              >Add New Section</el-button
            >
          </el-card>
          <el-button
            class="saveSectionsArrangementButton"
            type="success"
            v-if="isSectionsRearranged && !isSectionListEmpty && isListView"
            @click="saveNewSectionArrangement"
          >
            <b-icon icon="file-earmark-plus" />
            Save Arrangement
          </el-button>
        </el-aside>
        <br />
        <el-alert
          v-if="isSectionListApiError"
          :title="sectionListApiErrorMsg"
          type="error"
          show-icon
        >
        </el-alert>
        <el-card shadow="hover">
          <b-col>
            <b-button
              class="gridViewButton"
              variant="light"
              @click="toGridView"
            >
              <b-icon icon="grid3x3-gap-fill" />
            </b-button>

            <b-button
              class="listViewButton"
              variant="light"
              @click="toListView"
            >
              <b-icon icon="list-ul" />
            </b-button>
          </b-col>

          <el-divider></el-divider>

          <b-row
            class="gridView"
            v-model="sectionList"
            v-if="isGridView"
            style="overflow: hidden;"
          >
            <b-col class="gridCols" cols="9" md="3">
              <b-row v-for="sect in sectionListFirstCol" :key="sect.id">
                <b-card class="gridViewCards mx-auto">
                  <AbstractGridSectionDetail
                    class="sections mx-auto"
                    :sectionDetail="sect"
                    :presentationId="presentationId"
                    :data-package="dataPackage"
                  >
                  </AbstractGridSectionDetail>
                </b-card>
              </b-row>
            </b-col>
            <b-col class="gridCols" cols="9" md="3">
              <b-row v-for="sect in sectionListSecondCol" :key="sect.id">
                <b-card class="gridViewCards mx-auto">
                  <AbstractGridSectionDetail
                    class="sections mx-auto"
                    :sectionDetail="sect"
                    :presentationId="presentationId"
                    :data-package="dataPackage"
                  >
                  </AbstractGridSectionDetail>
                </b-card>
              </b-row>
            </b-col>
            <b-col class="gridCols" cols="9" md="3">
              <b-row v-for="sect in sectionListThirdCol" :key="sect.id">
                <b-card class="gridViewCards mx-auto">
                  <AbstractGridSectionDetail
                    class="sections mx-auto"
                    :sectionDetail="sect"
                    :presentationId="presentationId"
                    :data-package="dataPackage"
                  >
                  </AbstractGridSectionDetail>
                </b-card>
              </b-row>
            </b-col>
          </b-row>

          <SortableList lockAxis="y" v-model="sectionList" v-if="isListView">
            <SortableItem
              v-for="(section, index) in sectionList"
              :key="section.id"
              :index="index"
              :section="section"
              :data-package="dataPackage"
              :presentation-id="presentationId"
            >
            </SortableItem>
          </SortableList>
          <EmptySection v-if="isSectionListEmpty" />
        </el-card>
      </div>
    </mq-layout>
  </div>
</template>

<script>
import AbstractSectionDetail from "@/components/AbstractSectionDetail.vue";
import { ID_NEW_PRESENTATION } from "@/common/const";
import PredefinedQueries from "@/store/data/predefinedQueries";
import EmptySection from "@/components/emptyStates/EmptySection.vue";
import { ContainerMixin, ElementMixin } from "vue-slicksort";
import AbstractGridSectionDetail from "@/components/AbstractGridSectionDetail";

const SortableList = {
  mixins: [ContainerMixin],
  template: `
    <ul class="list">
      <slot />
    </ul>
  `,
};

const SortableItem = {
  mixins: [ElementMixin],
  props: ["section", "presentationId", "dataPackage"],
  components: {
    AbstractSectionDetail,
  },
  template: `
      <abstract-section-detail class="presentation-section"  :sectionDetail="section"
                               :presentationId="presentationId" :dataPackage="dataPackage"/>
  `,
};

export default {
  props: {
    presentationId: String,
  },

  watch: {
    presentationId: "fetchSectionList",

    selectedConventionId() {
      if (this.isConventionSelected === true) {
        this.selectedConferenceType = "";
        this.isConferenceTypeSelected = false;
        this.presentationFormVersion = "";
        this.isPresentationFormVersionSelected = false;
        this.haveSufficientInformation = false;
      }
      if (this.isConferenceTypeSelected) {
        this.$store.dispatch("getConventionVersionList", {
          conventionId: this.selectedConventionId,
          confType: this.selectedConferenceType,
        });
      }
      this.isConventionSelected = true;
    },

    selectedConferenceType() {
      if (this.isConferenceTypeSelected === true) {
        this.isPresentationFormVersionSelected = false;
        this.haveSufficientInformation = false;
        this.presentationFormVersion = "";
      }
      if (this.isConventionSelected) {
        this.$store.dispatch("getConventionVersionList", {
          conventionId: this.selectedConventionId,
          confType: this.selectedConferenceType,
        });
        this.isConferenceTypeSelected = true;
      }
      if (this.selectedConferenceType !== "") {
        this.isConferenceTypeSelected = true;
      }
    },

    presentationFormVersion() {
      if (this.presentationFormVersion !== "") {
        this.isPresentationFormVersionSelected = true;
        this.haveSufficientInformation = true;
      }
    },
    dataPackage() {
      this.updateDataPackage();
    },
  },
  data() {
    return {
      selectedNewSection: "",

      selectedConventionId: "",
      isConventionSelected: false,

      selectedConferenceType: "",
      isConferenceTypeSelected: false,

      presentationFormVersion: "",
      isPresentationFormVersionSelected: false,

      haveSufficientInformation: false,

      dataPackage: [],
      isDataSaved: false,

      isSectionsRearranged: false,
      isGridView: false,
      isListView: true,
      currentSectionArrangement: [],
    };
  },
  computed: {
    isLogin() {
      return this.$store.state.userInfo.isLogin;
    },

    isPresentationEditable() {
      return this.$store.state.presentation.isPresentationEditable;
    },

    predefinedSections() {
      let sectionOptionsGroup = {};
      // grouping the predefined queries
      for (let key in PredefinedQueries) {
        if (!PredefinedQueries.hasOwnProperty(key)) {
          continue;
        }
        let groupName = PredefinedQueries[key].group;
        if (sectionOptionsGroup[groupName] === undefined) {
          sectionOptionsGroup[groupName] = [];
        }
        sectionOptionsGroup[groupName].push({
          value: key,
          label: PredefinedQueries[key].name,
        });
      }

      // generate to format that element ui requires
      let sectionOptions = [];
      for (let groupName in sectionOptionsGroup) {
        if (!sectionOptionsGroup.hasOwnProperty(groupName)) {
          continue;
        }
        sectionOptions.push({
          label: groupName,
          options: sectionOptionsGroup[groupName],
        });
      }
      return sectionOptions;
    },

    isNewPresentation() {
      return this.presentationId === ID_NEW_PRESENTATION;
    },
    sectionList: {
      get: function() {
        return this.$store.state.section.sectionList;
      },
      set: function(newSectionList) {
        this.$store.commit("setSectionList", newSectionList);
        this.currentSectionArrangement = newSectionList;
        this.rearrangedSections();
      },
    },

    sectionListFirstCol() {
      let firstColSections = [];
      for (let i = 0; i < this.sectionList.length; i++) {
        if (i % 3 === 0) {
          firstColSections.push(this.sectionList[i]);
        }
      }
      return firstColSections;
    },

    sectionListSecondCol() {
      let secondColSections = [];
      for (let i = 0; i < this.sectionList.length; i++) {
        if (i % 3 === 1) {
          secondColSections.push(this.sectionList[i]);
        }
      }
      return secondColSections;
    },

    sectionListThirdCol() {
      let thirdColSections = [];
      for (let i = 0; i < this.sectionList.length; i++) {
        if (i % 3 === 2) {
          thirdColSections.push(this.sectionList[i]);
        }
      }
      return thirdColSections;
    },

    isSectionListEmpty() {
      return this.$store.state.section.sectionList.length <= 0;
    },
    isLoadingSectionList() {
      return this.$store.state.section.sectionListStatus.isLoading;
    },
    isSectionListApiError() {
      return this.$store.state.section.sectionListStatus.isApiError;
    },
    sectionListApiErrorMsg() {
      return this.$store.state.section.sectionListStatus.apiErrorMsg;
    },
    isLoadingDBMetaData() {
      return this.$store.state.dbMetaData.entitiesStatus.isLoading;
    },

    conventionList() {
      return this.$store.state.convention.conventionList;
    },
    versions() {
      return Array.from(
        new Set(
          this.$store.state.convention.conventionVersionList
            .filter(
              (v) =>
                v.conferenceType === this.selectedConferenceType &&
                v.conventionId === this.selectedConventionId
            )
            .map((v) => v.versionId)
        )
      );
    },
    conferenceTypes() {
      return ["EASYCHAIR", "SOFTCONF"];
    },
  },
  components: {
    AbstractGridSectionDetail,
    AbstractSectionDetail,
    EmptySection,
    SortableItem,
    SortableList,
  },
  async mounted() {
    this.fetchSectionList();
    this.$store.dispatch("fetchDBMetaDataEntities");
    this.$store.dispatch("getConventionList");
    this.$store.dispatch("getVersionList");
    await this.$store.dispatch("getPresentation", this.presentationId);
    this.setDefaultValueForVersionList(
      this.$store.state.presentation.presentationForm.version
    );
    this.setDefaultValueForConferenceType(
      this.$store.state.presentation.presentationForm.conferenceType
    );
    this.setDefaultValueForConventionId(
      this.$store.state.presentation.presentationForm.conventionId
    );
    this.saveData();
  },
  methods: {
    updateVersion() {
      let value = this.presentationFormVersion;
      if (value === undefined) {
        value = this.versions[0];
      }
      this.$store.commit("setPresentationFormField", {
        field: "version",
        value,
      });
    },

    updateConventionId() {
      let value = this.selectedConventionId;

      if (value === undefined) {
        value = this.conventionList[0].id;
      }

      this.$store.commit("setPresentationFormField", {
        field: "conventionId",
        value,
      });
    },

    updateConferenceType() {
      let value = this.selectedConferenceType;

      this.$store.commit("setPresentationFormField", {
        field: "conferenceType",
        value,
      });
    },

    updatePresentation() {
      this.$store.dispatch("updatePresentation");
    },

    setDataPackage(conventionId, conferenceType, version) {
      let newDataPackage = [];
      newDataPackage[0] = conventionId;
      newDataPackage[1] = conferenceType;
      newDataPackage[2] = version;
      this.dataPackage = newDataPackage;
    },

    setDefaultValueForVersionList(value) {
      this.presentationFormVersion = value;
    },

    setDefaultValueForConventionId(value) {
      this.selectedConventionId = value;
    },

    setDefaultValueForConferenceType(value) {
      this.selectedConferenceType = value;
    },

    saveData() {
      this.setDataPackage(
        this.selectedConventionId,
        this.selectedConferenceType,
        this.presentationFormVersion
      );
    },

    useData() {
      this.updateConventionId();
      this.updateConferenceType();
      this.updateVersion();
      this.updatePresentation();
      this.saveData();

      this.isDataSaved = true;
    },

    rearrangedSections() {
      this.isSectionsRearranged = true;
    },

    fetchSectionList() {
      if (this.isNewPresentation) {
        this.$store.commit("clearSectionList");
      } else {
        this.$store.dispatch("fetchSectionList", this.presentationId);
      }
    },

    addNewSection() {
      if (this.selectedNewSection.length === 0) {
        this.$notify.error({
          title: "Error",
          message: "Please select a section to add into presentation.",
        });

        return;
      }
      this.$store
        .dispatch("addSectionDetail", {
          presentationId: this.presentationId,
          selectedNewSection: this.selectedNewSection,
          dataSet: this.$store.state.userInfo.userEmail,
        })
        .then(() => {
          this.selectedNewSection = "";
        });
      this.$refs["modal-1"].hide();
    },

    saveNewSectionArrangement() {
      this.$store.dispatch(
        "deleteSectionByPresentationId",
        this.presentationId
      );
      this.$store.commit("clearSectionList");
      const add = async (newSectionList) => {
        for (let i = newSectionList.length - 1; i >= 0; i--) {
          let sect = newSectionList[i];
          await this.$store.dispatch("addSectionDetail", {
            presentationId: this.presentationId,
            selectedNewSection: sect.graphKey,
            dataSet: this.$store.state.userInfo.userEmail,
          });
        }
      };
      add(this.currentSectionArrangement);
      this.currentSectionArrangement = [];
      this.isSectionsRearranged = false;
    },

    toListView() {
      this.isListView = true;
      this.isGridView = false;
    },

    toGridView() {
      this.isGridView = true;
      this.isListView = false;
    },
  },
};
</script>

<style scoped>
.textBold {
  font-weight: bold;
}
.dataOptionInput {
  display: inline-block;
  width: 100%;
}
.selectionInput {
  display: inline-block;
  width: 100%;
  margin-bottom: 16px;
}
.selectionInputMobile {
  display: inline-block;
  width: 100%;
  margin-bottom: 16px;
}
.selectionInputButton {
  display: inline-block;
  width: 100%;
}
.saveSectionsArrangementButton {
  display: inline-block;
  width: 100%;
}
.addRowRightAlign {
  float: right;
  margin-top: 18px;
  margin-left: 16px;
}
.addRowRightAlign .el-card {
  margin-bottom: 16px;
}

.gridViewButton {
  margin-right: 2px;
}

.gridViewCards {
  width: 350px;
  height: 300px;
  margin: 10px;
  transition: 0.6s;
  border-radius: 40px;
  overflow: hidden;
}
.gridViewCards .sections {
  overflow: auto;
  position: center;
}

.gridViewCards:hover {
  box-shadow: 0 0 20px rgba(33, 33, 33, 0.2);
}

.gridCols {
  margin-left: auto;
  margin-right: auto;
  margin-top: 3px;
  width: auto;
  margin-bottom: -99999px;
  padding-bottom: 99999px;
}

.selectField {
  margin: 5px 0;
}

.fab {
  border-radius: 50%;
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 101;
  padding: 0.8rem 1rem;
}
</style>
