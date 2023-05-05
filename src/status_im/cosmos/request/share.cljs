(ns status-im.cosmos.request.share
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require
   [quo.core :as quo]
   [quo.design-system.colors :as colors]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [status-im.ethereum.eip55 :as eip55]
   [status-im.ethereum.eip681 :as eip681]
   [status-im.i18n.i18n :as i18n]
   [status-im.ui.components.copyable-text :as copyable-text]
   [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
   [status-im.ui.components.react :as react]))

(defview select-address-type-sheet []
  (views/letsubs [selected-type [:address-types/selected]
                  available-types [:address-types/available]]

    [:<>
     [quo/header {:title "Addresses" :border-bottom false}]
     (for [current-type available-types]
       ^{:key current-type}
       [quo/list-item
        {:title    (str  current-type)
         :accessibility-label (str current-type)
         :accessory           :radio
         :on-press            #(do (re-frame/dispatch [:bottom-sheet/hide])
                                   (re-frame/dispatch [:address-types/set current-type]))
         :active            (= current-type selected-type)}])]))

;A dummy container so that layout will not get affected, when the bottom sheet is shown
(defn copyable-container-view
  [{:keys [label copied-text container-style]}]
  (let [cue-atom     (reagent/atom false)
        background-color (or (get container-style :background-color) colors/white)]
    [react/view {:style (if container-style container-style {})}
     [react/text
      {:style
       {:font-size     13
        :line-height   18
        :font-weight   "500"
        :color         colors/gray
        :margin-bottom 4}}
      (i18n/label label)]
     [react/touchable-highlight
      {:active-opacity (if @cue-atom 1 0.85)
       :underlay-color colors/black}
      [react/view {:background-color background-color}
       [quo/text {:number-of-lines     1
                  :ellipsize-mode      :middle
                  :accessibility-label :address-text
                  :monospace           true}
        copied-text]]]]))

(defview show-selected-address [address]
  (views/letsubs [all-accounts [:wallet/accounts]
                  selected-type [:address-types/selected]
                  showing-bottom-sheet? [:bottom-sheet/show?]]

    (let [cue-atom     (reagent/atom false)
          show-select-address-type      #(re-frame/dispatch [:bottom-sheet/show-sheet   {:content select-address-type-sheet}])
          label       (if (= selected-type "Ethereum") :t/ethereum-address :t/bech32-address)
          copied-text (if (= selected-type "Ethereum") (eip55/address->checksum address) (get-in all-accounts [address :bech32-addr]))]

      [:<>
       [react/touchable-highlight
        {:active-opacity (if @cue-atom 1 0.85)
         :on-press       show-select-address-type
         :on-long-press  show-select-address-type}
        [react/view {}
         [quo/list-item
          {:size                   :small
           :title                   "Address Type"
           :accessibility-label     :backup-enabled
           :container-margin-bottom 8
           :on-press     show-select-address-type
           :on-long-press  show-select-address-type
           :accessory :text
           :accessory-text    selected-type
           :chevron             true}]]]

                           ;WorkAround for copyable text view re-render, subscribe a variable to stop render on show bottom sheet
       (if (= false showing-bottom-sheet?)
         [copyable-text/copyable-text-view
          {:label           label
           :container-style {:margin-top 12 :margin-bottom 4}
           :copied-text      copied-text}
          [quo/text {:number-of-lines     1
                     :ellipsize-mode      :middle
                     :accessibility-label :address-text
                     :monospace           true}
           copied-text]]
                             ;WorkAround for copyable text view re-render, add a dummy container view so that layout is not affected , when bottom sheet is shown
         [copyable-container-view
          {:label           label
           :container-style {:margin-top 12 :margin-bottom 4}
           :copied-text      copied-text}])])))

(views/defview share-all-address []
  (views/letsubs [{:keys [address]} [:popover/popover]
                  chain-id    [:chain-id]
                  all-accounts [:wallet/accounts]
                  selected-type [:address-types/selected]
                  width       (reagent/atom nil)
                  showing-bottom-sheet? [:bottom-sheet/show?]
                  selected-type [:address-types/selected]]

    [react/view {:on-layout #(reset! width (-> ^js % .-nativeEvent .-layout .-width))}

     (let [qr-code-text (if (= selected-type "Ethereum") (eip681/generate-uri address {:chain-id chain-id}) (get-in all-accounts [address :bech32-addr]))]
       [react/view {:style {:padding-top 16 :padding-horizontal 16}}
        [:<>

         (when @width
           [qr-code-viewer/qr-code-view
            (- @width 32)
            qr-code-text])]

        [show-selected-address address]])

     [react/view {:padding-top        12
                  :padding-horizontal 16
                  :padding-bottom     16}
      [quo/button
       {:on-press            #(re-frame/dispatch [:wallet.accounts/share address])
        :accessibility-label :share-address-button}
       (i18n/label :t/share-address)]]]))