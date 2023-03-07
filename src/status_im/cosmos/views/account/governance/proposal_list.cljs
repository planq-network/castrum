(ns status-im.cosmos.views.account.governance.proposal-list
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.cosmos.views.account.governance.proposal-chip  :as chip]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [quo.components.list.item :as list-item]
            [status-im.ui.components.react :as react]))






(defn navigate-to-detail [proposal-id]
  (re-frame/dispatch [:navigate-to :proposal-detail {:proposal-id proposal-id}]
                     ))

(defn build-proposal-row [{:keys [id title proposal-end-detail status days-left-for-voting]}]

    [rn/touchable-opacity {:style         {:margin-vertical 4
                                           :padding         10}
                           :on-press      #(navigate-to-detail id)
                           :on-long-press #(navigate-to-detail id)
                           }


     [rn/view {:style {:padding-vertical    1
                       :padding-horizontal  1
                       :border-bottom-width 1
                       :border-bottom-color (:ui-01 @colors/theme)
                       }}

      [rn/view {:style {:flex-direction  :row
                        :justify-content :space-between
                        }}
       [quo/text {:weight :medium} (str "#" id " " title)] ]
      [rn/view {:style {:flex-direction  :row
                        :justify-content :space-between}}
       [quo/text {:weight :regular :color :secondary} proposal-end-detail]
       ]
      [rn/view {:style {:flex-direction  :row
                        :justify-content :space-between
                        :padding-vertical    2
                        }}

       [quo/text {:weight :regular :color :secondary} days-left-for-voting]
       [chip/chip-status status]]]])


(views/defview list-proposals [address]
               (views/letsubs [
                               governance-proposals [:keplr-store/proposals]
                               ]
                              [react/view
                               (doall
                                 (for [input governance-proposals]
                                   ^{:key (get-in input [:id]) }
                                   [build-proposal-row input]


                                   ))]))

; List Item Index  [left] type of status
; Description
;Voting Ends :date and time in utc   [Left]  x days left or empty
