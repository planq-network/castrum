(ns status-im.cosmos.account.governance.proposals.proposal-list-view
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.cosmos.account.governance.proposals.common.components.proposal-chip :as chip]
            [status-im.cosmos.common.components.containers :as containers]
            [status-im.ui.components.react :as react]
            [status-im.utils.re-frame :as rf]))

(defn navigate-to-detail [proposal-id]
  (re-frame/dispatch [:keplr-store/update-proposal-details-for-id proposal-id])
  (re-frame/dispatch [:navigate-to :proposal-detail {:proposal-id proposal-id}]))

(defn build-proposal-row [{:keys [id title proposal-end-detail status days-left-for-voting]}]

  [rn/touchable-opacity {:style         {:margin-vertical 4
                                         :padding         10}
                         :on-press      #(navigate-to-detail id)
                         :on-long-press #(navigate-to-detail id)} [rn/view {:style {:padding-vertical    1
                                                                                    :padding-horizontal  1
                                                                                    :border-bottom-width 1
                                                                                    :border-bottom-color (:ui-01 @colors/theme)}}

                                                                   [rn/view {:style {:flex-direction  :row
                                                                                     :justify-content :space-between}}
                                                                    [quo/text {:weight :medium} (str "#" id " " title)]]
                                                                   [rn/view {:style {:flex-direction  :row
                                                                                     :justify-content :space-between}}
                                                                    [quo/text {:weight :regular :color :secondary} proposal-end-detail]]
                                                                   [rn/view {:style {:flex-direction   :row
                                                                                     :justify-content  :space-between
                                                                                     :padding-vertical 2}}

                                                                    [quo/text {:weight :regular :color :secondary} days-left-for-voting]
                                                                    [chip/chip-status status]]]])

(views/defview list-proposals [address]
  (views/letsubs [input [:keplr-store/proposals]]
    [containers/dynamic-data-container input
     ^{:key "list-proposals"}
     [react/view
      (doall
       (for [proposal (get-in input [:data])]
         ^{:key (get-in proposal [:id])}
         [build-proposal-row proposal]))]]))
