(ns status-im.cosmos.views.account.governance.proposal-detail
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require
    [quo.animated :as animated]
    [quo.core :as quo]
    [quo.design-system.colors :as colors]
    [quo.react-native :as rn]
    [status-im.cosmos.views.account.governance.proposal-chip :as chip]
    [status-im.i18n.i18n :as i18n]
    [status-im.ui.components.react :as react]
    [status-im.ui.screens.wallet.transactions.styles :as styles]
    [status-im.utils.datetime :as datetime]
    [status-im.utils.re-frame :as rf]))





(defn vote-info-view [{:keys [vote percentage highlight?]}]

  [rn/view {:style {:flex-direction :row
                    :background-color (if highlight? colors/blue nil)
                    :height 56
                    :border-radius 4
                    :border-width 1
                    :border-color colors/gray-lighter
                    :width 140
                    :margin-bottom 10


                    }}

   ; a column with a text
   [rn/view {:style {
                     :background-color colors/gray-lighter
                     :align-items :flex-start
                     }}
    [quo/text {:weight          :regular
               :color           :secondary} " "]
    ]


   [rn/view {:style {
                     :padding-left 4

                     }}
;a row with a text

   [rn/view {:style {:flex-direction :row
                     :align-items :flex-start

                     }}
   [quo/text {:weight          :regular
              :color           :secondary} vote]
   ]
   [rn/view {:style {:flex-direction :row
                     :align-items :flex-start

                     }}

   [quo/text {:weight :medium} (str percentage "%")]]]



   ])


(defn vote-info-container [tally-ratio]

  [:<>
   [rn/view {:style {:flex-direction  :row
                     :justify-content :space-between

                     }}

    [vote-info-view {:vote "Yes" :percentage 50 :highlight? false}]
    [vote-info-view {:vote "No" :percentage 50 :highlight? true}]



    ]
   [rn/view {:style {:flex-direction  :row
                     :justify-content :space-between

                     }}

    [vote-info-view {:vote "No with Veto" :percentage 50 :highlight? false}]
    [vote-info-view {:vote "Abstain" :percentage 50 :highlight? false}]


    ]]
 )
(defn progress-bar [progress failed?]
  [react/view {:style styles/progress-bar}
   [react/view {:style (styles/progress-bar-done progress failed?)}
    ]
   [react/view {:style (styles/progress-bar-todo (- 100 progress) failed?)}]])


(defn proposal-by-id []
  (let [{:keys [proposal-id]} (rf/sub [:get-screen-params])]
    (fn []
      (let [
            {:keys [id title tally-ratio voting-start-time voting-end-time status description]} (rf/sub [:keplr-store/proposal-by-id proposal-id])
            turn-out 53.4
            formatted-voting-start (-> voting-start-time
                                     (datetime/parse-date-to-ms)
                                     (datetime/timestamp->long-date))
            formatted-voting-end (-> voting-end-time
                                     (datetime/parse-date-to-ms)
                                     (datetime/timestamp->long-date))
            ]

        (prn tally-ratio)
        ;get total votes and calculate percentage

        [rn/view {:style {
                      :padding  16 }}

         [rn/view {:style {:flex-direction  :row
                           :justify-content :space-between
                           }}
          [quo/text {:weight :medium} (str "#" id )]
          [chip/chip-status status]

          ]

         [rn/view {:style {:flex-direction  :row
                           :justify-content :flex-start
                           }}
          [quo/text {:weight :medium} title]]

         [rn/view {:style {:flex-direction  :row
                           :justify-content :space-between
                           :padding-top 15
                           }}



          [quo/text {:weight :medium} (i18n/label :t/turn-out)]
          [quo/text {:weight :medium} (str turn-out "%")]
          ; progress bar

          ]

         [rn/view
          [progress-bar turn-out false]
          ]

        [vote-info-container tally-ratio]

         [rn/view {:style {:flex-direction  :row
                           :justify-content :space-between
                           }}

          ;yes no , no with veto , abstain
          [quo/text {:weight :medium }  (i18n/label :t/voting-starts)]
          [quo/text {:weight :medium} (i18n/label :t/voting-ends)]



          ]
         [rn/view {:style {:flex-direction  :row
                           :justify-content :space-between
                           }}

          ;yes no , no with veto , abstain
          [quo/text {:weight :regular
                     :size :small
                     } formatted-voting-start]
          [quo/text {:weight :regular
                     :size :small
                     } formatted-voting-end]



          ]


         [rn/view {:style {
                           :margin-top 10
                           }}

          ;yes no , no with veto , abstain
          [quo/text {:weight :medium  } "Description"]


          [quo/text {:weight :regular
                     :color :secondary


                     } description]



          ]


         ;[quo/list-item {:title "Description"  :subtitle description :subtitle-max-lines 100}]
         [quo/button
          {
           :on-press #(prn "clicked")}
          (i18n/label :t/vote)]



         ; an image and a text aligned left



         ]



        ))))