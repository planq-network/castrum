(ns status-im.cosmos.views.wallet.request.share  (:require [re-frame.core :as re-frame]
                                            [reagent.core :as reagent]
                                            [status-im.ethereum.eip55 :as eip55]
                                            [status-im.ethereum.eip681 :as eip681]
                                            [quo.core :as quo]
                                                           [status-im.utils.re-frame :as rf]
                                            [status-im.i18n.i18n :as i18n]
                                                           [status-im.cosmos.stores.address-type-store :as address-type-store]
                                            [status-im.ui.components.copyable-text :as copyable-text]
                                            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
                                            [status-im.ui.components.react :as react])
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]]))

(defn hide-sheet-and-select [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch [:address-types/set event])
 (prn event)

  )


(comment

  (rf/sub [:address-types/selected])
  )

(defview select-address-type-sheet []
         (views/letsubs [
                         selected-type [:address-types/selected]
                         available-types [:address-types/available]
                         ]
                  [:<>
                   [quo/header {:title "Addresses" :border-bottom false}]
                    (for [current-type available-types]
                      ^{:key current-type}
                       [quo/list-item
                        {:title    (str  current-type)
                         :accessibility-label (str current-type)
                         :accessory           :radio
                         :on-press            #(hide-sheet-and-select current-type)
                          :selected            (= current-type selected-type)}])]))



;TODO not working , need to fix copyable text view, as its not being updated ??
(defview show-selected-address [copied-ethereum-text  copied-bech32-text]
         (views/letsubs [all-accounts [:wallet/accounts]
                         selected-type [:address-types/selected]
                         ]

                        (let [
                              cue-atom     (reagent/atom false)
                              copy-fn
                              #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                   {:content select-address-type-sheet}]

                                                  )

                              ]
                          [:<>
                          [react/touchable-highlight
                           {:active-opacity (if @cue-atom 1 0.85)
                            :on-press       copy-fn
                            :on-long-press  copy-fn}
                           [react/view {}
                             [quo/list-item
                              {:size                   :small
                               :title                   "Address Type"
                               :accessibility-label     :backup-enabled
                               :container-margin-bottom 8
                               :on-press     copy-fn
                               :on-long-press  copy-fn
                               :accessory :text
                               :accessory-text    selected-type
                               :chevron             true }]
                            ]]

                           [copyable-text/copyable-text-view
                            {:label           (if (= selected-type "Ethereum") :t/ethereum-address :t/btc-address)
                             :container-style {:margin-top 12 :margin-bottom 4}
                             :copied-text      (if (= selected-type "Ethereum") copied-ethereum-text copied-bech32-text)   }
                            [quo/text {:number-of-lines     1
                                       :ellipsize-mode      :middle
                                       :accessibility-label :address-text
                                       :monospace           true}
                             (if (= selected-type "Ethereum") copied-ethereum-text copied-bech32-text) ]]


                           ]




                          )))


(views/defview share-all-address []
               (views/letsubs [{:keys [address]} [:popover/popover]
                               chain-id    [:chain-id]
                               all-accounts [:wallet/accounts]
                               selected-type [:address-types/selected]
                               width       (reagent/atom nil)


                               ]

                              (let [
                                    copied-ethereum-text (eip55/address->checksum address)
                                    copied-bech32-text (get-in all-accounts [ address :bech32-addr])

                                    ]

                              [react/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}
                               [react/view {:style {:padding-top 16 :padding-horizontal 16}}

                                [:<>

                                 (when @width
                                   [qr-code-viewer/qr-code-view
                                    (- @width 32)
                                    (eip681/generate-uri address {:chain-id chain-id})])
                                 ]

                                [show-selected-address copied-ethereum-text  copied-bech32-text]




                                ]
                               [react/view {:padding-top        12
                                            :padding-horizontal 16
                                            :padding-bottom     16}
                                [quo/button
                                 {:on-press            #(re-frame/dispatch [:wallet.accounts/share address])
                                  :accessibility-label :share-address-button}
                                 (i18n/label :t/share-address)]]])))