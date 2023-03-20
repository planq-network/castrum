(ns status-im.cosmos.account.staking.validator-detail
  (:require [quo.core :as quo]
            [status-im.cosmos.account.staking.validator-icon :as validator-icon]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.utils.re-frame :as rf]))

(defn validator-detail []
  (let [{:keys [operator-address]} (rf/sub [:get-screen-params])]
    (fn []
      (let [{:keys [identity name commission-rate description tokens]} (rf/sub [:keplr-store/validator-by-operator-address operator-address])
            image-url (rf/sub [:validator-thumbnail-store/image-by-validator-identity identity])] [:<>

         ; an image and a text aligned left
                                                                                                   [react/view {:style {:flex-direction :row
                                                                                                                        :flex-grow      0
                                                                                                                        :flex-shrink    1
                                                                                                                        :padding-left   16
                                                                                                                        :margin-top     20}}

                                                                                                    [validator-icon/icon image-url name]
                                                                                                    [quo/text {:style {:font-size    14
                                                                                                                       :font-weight  :bold
                                                                                                                       :padding-left 10}} name]]; a list of items with value Commision , Voting Power and Description
                                                                                                   [quo/list-item {:title "Commission" :accessory :text :accessory-text commission-rate}]
                                                                                                   [quo/list-item {:title "Voting Power" :accessory :text :accessory-text tokens}]; A paragraph of text with a header

                                                                                                   [quo/list-item {:title "Description" :subtitle description :subtitle-max-lines 10}];a button with text stake on a row full

                                                                                                   [quo/button
                                                                                                    {:on-press #(prn "clicked")}
                                                                                                    (i18n/label :t/stake)]]))))

(comment

  (get-valid-amount "30143464713824483112001")              ; 30143.464713824484
)

