(ns status-im.cosmos.views.account.staking.validator-detail
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.utils.re-frame :as rf]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.utils.money :as money]))


(defn validator-detail []
  (let [{:keys [operator-address]} (rf/sub [:get-screen-params])]
    (fn []
      (let [

            {:keys [identity name commission-rate description tokens]} (rf/sub [:keplr-store/validator-by-operator-address operator-address])
            image-url (rf/sub [:validator-thumbnail-store/image-by-validator-identity identity])]

        [:<>

         ; an image and a text aligned left
          [react/view {:style {:flex-direction :row
                               :flex-grow      0
                               :flex-shrink    1
                               :padding-left  16
                               :margin-top 20

                               }}

           [react/image {:source {:uri image-url  }
                         :style
                         {:width  24
                          :height   24}
                         }] [quo/text {:style {:font-size 14
                                                          :font-weight :bold
                                                          :padding-left  10
                                                          }} name]]



         ; a list of items with value Commision , Voting Power and Description
          [quo/list-item {:title "Commission" :accessory :text :accessory-text commission-rate}]
          [quo/list-item {:title "Voting Power" :accessory :text :accessory-text tokens}]


         ; A paragraph of text with a header

         [quo/list-item {:title "Description"  :subtitle description :subtitle-max-lines 10}]


         ;a button with text stake on a row full

         [quo/button
          {
           :on-press #(prn "clicked")}
          (i18n/label :t/stake)]



          ;[quo/button {:style    {:margin-top        20
          ;                        :margin-horizontal 16
          ;                        :height            50
          ;                        :border-radius     10
          ;                        :background-color  (:button-primary @colors/theme)
          ;                        :align-items       :center
          ;                        :justify-content   :center
          ;                        }
          ;             :on-press #(prn "clicked")}
          ;  [quo/text {:style {:font-size 14
          ;                    :font-weight :bold
          ;                    }} "Stake"]]



         ]



        ))))



(comment

  (get-valid-amount "30143464713824483112001")              ; 30143.464713824484




  )

