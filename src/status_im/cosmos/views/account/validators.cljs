(ns status-im.cosmos.views.account.validators
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.utils.money :as money]))



;eth has the base 18 by default
(defn get-valid-amount [input]
(money/to-fixed  (money/wei-> :eth input) 2))



(defview select-validator-status-sheet []
  (views/letsubs [selected-type [:keplr-store/selected-validator-status]
                  available-types [:keplr-store/available-validator-statuses]]
    [:<>
     [quo/header {:title  (i18n/label :t/validator-status) :border-bottom false}]
     (for [current-type available-types]
       ^{:key current-type}
       [quo/list-item
        {:title    (str  current-type)
         :accessibility-label (str current-type)
         :accessory           :radio
         :on-press            #(do (re-frame/dispatch [:bottom-sheet/hide])
                                   (re-frame/dispatch [:keplr-store/set-validator-status current-type]))
         :active            (= current-type selected-type)}])]))

(defview show-selected-validator-status []
  (views/letsubs [selected-type [:keplr-store/selected-validator-status]
                  showing-bottom-sheet? [:bottom-sheet/show?]]

    (let [cue-atom     (reagent/atom false)
          show-select-validator-status      #(re-frame/dispatch [:bottom-sheet/show-sheet   {:content select-validator-status-sheet}])]
      [:<>
       [react/touchable-highlight
        {:active-opacity (if @cue-atom 1 0.85)
         :on-press       show-select-validator-status
         :on-long-press  show-select-validator-status}
        [react/view {}
         [quo/list-item
          {:size                    :small
           :title                   (i18n/label :t/validator-status)
           :accessibility-label     :backup-enabled
           :container-margin-bottom 8
           :on-press                show-select-validator-status
           :on-long-press           show-select-validator-status
           :accessory               :text
           :accessory-text          selected-type
           :chevron                 true}]]]])))

(defn planq-tokens-from [{:keys [tokens]}]
  (-> tokens
      (get-valid-amount )
      ))
(defn generate-key [{:keys [description]}]
  (str (get-in description [:identity])  (get-in description [:moniker])))

(defn description-from [{:keys [description]}]
  (get-in description [:moniker]))

(defn commission-from [{:keys [commission]}]
  (-> commission
      (get-in [:commission_rates :rate] "0")
      (js/parseInt)
      (.toFixed 2)
      (str " %")))

(views/defview staking [address]
  (views/letsubs [data [:wallet.transactions.history/screen address]
                  {:keys [validators]} [:keplr-store]]

    [:<>

     [show-selected-validator-status]

     [react/view

      (for [[index  validator] (map-indexed vector validators)]
        (let [validator-name (description-from validator)
              commission-rate (commission-from validator)               ;(.toFixed (js/parseInt (get-in commission [:commission_rates :rate] "0"))  2 )
              formatted-token (planq-tokens-from validator)                 ;(format-planq-tokens tokens)
]
          ^{:key index}
          [quo/list-item

           {:title              [quo/text {:weight :medium}
                                 [quo/text {:weight :inherit}
                                  (str "#" (inc index) " " validator-name)]]
            :subtitle           (str "Tokens:"  formatted-token "\n" "Commission:" commission-rate)
            :subtitle-max-lines 5}]))]]))

(comment

  (get-valid-amount "30143464713824483112001") ; 30143.464713824484



  (->> (get-in @re-frame.db/app-db [:keplr-store :validators])
       (map planq-tokens-from))

  (->> (get-in @re-frame.db/app-db [:keplr-store :validators])
       (map generate-key))

  (->> (get-in @re-frame.db/app-db [:keplr-store :validators])
       (map description-from)))