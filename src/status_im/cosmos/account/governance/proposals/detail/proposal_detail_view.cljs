(ns status-im.cosmos.account.governance.proposals.detail.proposal-detail-view
  (:require
   [quo.core :as quo]
   [quo.design-system.colors :as colors]
   [quo.react-native :as rn]
   [re-frame.core :as re-frame]
   [status-im.cosmos.account.governance.proposals.common.components.proposal-chip :as chip]
   [status-im.i18n.i18n :as i18n]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.wallet.transactions.styles :as styles]
   [status-im.cosmos.common.components.containers :as containers]
   [status-im.utils.datetime :as datetime]
   [status-im.utils.re-frame :as rf]))

(defn vote-info-view [{:keys [vote percentage highlight?]}]

  [rn/view {:style {:flex-direction   :row
                    :background-color (if highlight? colors/blue nil)
                    :height           56
                    :border-radius    4
                    :border-width     1
                    :border-color     colors/gray-lighter
                    :width            140
                    :margin-bottom    10}}

   ; a column with a text
   [rn/view {:style {:background-color colors/gray-lighter
                     :align-items      :flex-start}}
    [quo/text {:weight :regular
               :color  :secondary} " "]] [rn/view {:style {:padding-left 4}}
    ;a row with a text

                                          [rn/view {:style {:flex-direction :row
                                                            :align-items    :flex-start}}
                                           [quo/text {:weight :regular
                                                      :color  :secondary} vote]]
                                          [rn/view {:style {:flex-direction :row
                                                            :align-items    :flex-start}}

                                           [quo/text {:weight :medium} (str percentage "%")]]]])

(defn vote-info-container [tally-ratio voted]

  (let [{:keys [yes no abstain no_with_veto]}  tally-ratio]
    [:<>
     [rn/view {:style {:flex-direction  :row
                       :justify-content :space-between}} [vote-info-view {:vote  (i18n/label :t/yes) :percentage yes :highlight? (= voted  "Yes")}]
      [vote-info-view {:vote (i18n/label :t/no) :percentage no :highlight? (= voted "No")}]]
     [rn/view {:style {:flex-direction  :row
                       :justify-content :space-between}}

      [vote-info-view {:vote (i18n/label :t/no-with-veto) :percentage no_with_veto :highlight? (= voted "NoWithVeto")}]
      [vote-info-view {:vote (i18n/label :t/abstain) :percentage abstain :highlight? (= voted "Abstain")}]]]))
(defn progress-bar [progress failed?]
  [react/view {:style styles/progress-bar}
   [react/view {:style (styles/progress-bar-done progress failed?)}]
   [react/view {:style (styles/progress-bar-todo (- 100 progress) failed?)}]])

(defn build-proposal-by-id [input]
  (let [{:keys [id title tally-ratio voting-start-time voting-end-time status description total voted turnout]} input
        formatted-voting-start (-> voting-start-time
                                   (datetime/parse-date-to-ms)
                                   (datetime/timestamp->long-date))
        formatted-voting-end (-> voting-end-time
                                 (datetime/parse-date-to-ms)
                                 (datetime/timestamp->long-date))

        vote-enabled? (= status "VOTING_PERIOD")
        vote-text (cond
                    (= status "DEPOSIT_PERIOD")  (i18n/label :t/vote-not-started)
                    (= status "VOTING_PERIOD")  (i18n/label :t/vote)
                    :else (i18n/label :t/vote-ended))]
;(prn vote-text)
;(prn vote-enabled?)

    [rn/scroll-view {:padding 16}

  ;
  ;[rn/view {:style {
  ;                  :padding 16}}

     [rn/view {:style {:flex-direction  :row
                       :justify-content :space-between}}
      [quo/text {:weight :medium} (str "#" id)]
      [chip/chip-status status]]

     [rn/view {:style {:flex-direction  :row
                       :justify-content :flex-start}}
      [quo/text {:weight :medium} title]]

     [rn/view {:style {:flex-direction  :row
                       :justify-content :space-between
                       :padding-top     15}}
      [quo/text {:weight :medium} (i18n/label :t/turnout)]
      [quo/text {:weight :medium} (str turnout "%")]]

     [rn/view
      [progress-bar  turnout false]]

     [vote-info-container tally-ratio voted]

     [rn/view {:style {:flex-direction  :row
                       :justify-content :space-between}}
      [quo/text {:weight :medium} (i18n/label :t/voting-starts)]
      [quo/text {:weight :medium} (i18n/label :t/voting-ends)]] [rn/view {:style {:flex-direction  :row
                                                                                  :justify-content :space-between}}
    ;yes no , no with veto , abstain
                                                                 [quo/text {:weight :regular
                                                                            :size   :small} formatted-voting-start]
                                                                 [quo/text {:weight :regular
                                                                            :size   :small} formatted-voting-end]]

     [rn/view {:style {:margin-top 10}}
      [quo/text {:weight :medium} "Description"]
      [quo/text {:weight :regular
                 :color  :secondary} description]] (if vote-enabled?
                                                     [quo/button {:type            :primary
                                                                  :accessibility-label status
                                                                  :weight          :medium
                                                                  :number-of-lines 1
                                                                  :style           {:line-height 18}
                                                                  :on-press        #(re-frame/dispatch [:chat.ui/cancel-contact-request])}
                                                      vote-text]

                                                     [quo/button
                                                      {:theme           :monocromatic
                                                       :accessibility-label status
                                                       :weight          :medium
                                                       :number-of-lines 1
                                                       :style           {:line-height 18
                                                                         :background-color colors/gray-lighter
                                                                         :color           colors/gray
                                                                         :text-color           colors/gray}}
                                                      vote-text])]))

(defn proposal-by-id []
  (let [{:keys [proposal-id]} (rf/sub [:get-screen-params])]
    (fn []
      (let [{:keys [data] :as input}  (rf/sub [:keplr-store/proposal-details-by-id proposal-id])]
        [containers/dynamic-data-container input
         ^{:key proposal-id}
         [build-proposal-by-id data]]))))
