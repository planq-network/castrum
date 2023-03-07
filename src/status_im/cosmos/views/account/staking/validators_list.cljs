(ns status-im.cosmos.views.account.staking.validators-list
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require
    [quo.components.list.item :as list-item]

    [quo.components.text :as text]
    [quo.core :as quo]
    [quo.design-system.colors :as colors]
    [quo.design-system.spacing :as spacing]
    [quo.react-native :as rn]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.i18n.i18n :as i18n]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.react :as react]
    [status-im.utils.platform :as platform]
    [status-im.utils.re-frame :as rf]))




(defview select-validator-status-sheet []
         (views/letsubs [selected-type [:keplr-store/selected-validator-status]
                         available-types [:keplr-store/available-validator-statuses]]
                        [:<>
                         [quo/header {:title (i18n/label :t/validator-status) :border-bottom false}]
                         (for [current-type available-types]
                           ^{:key current-type}
                           [quo/list-item
                            {:title               (str current-type)
                             :accessibility-label (str current-type)
                             :accessory           :radio
                             :on-press            #(do (re-frame/dispatch [:bottom-sheet/hide])
                                                       (re-frame/dispatch [:keplr-store/set-validator-status current-type]))
                             :active              (= current-type selected-type)}])]))

(defview show-selected-validator-status []
         (views/letsubs [selected-type [:keplr-store/selected-validator-status]
                         showing-bottom-sheet? [:bottom-sheet/show?]]

                        (let [cue-atom (reagent/atom false)
                              show-select-validator-status #(re-frame/dispatch [:bottom-sheet/show-sheet {:content select-validator-status-sheet}])]
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



(defn navigate-to-detail [operator-address]
  (re-frame/dispatch [:navigate-to :validator-detail {:operator-address operator-address}]))

(defn right-side [{:keys [chevron accessory-text accessory-style]}]
  [rn/view {:style (merge {:align-items     :center
                           :justify-content :flex-end
                           :flex-direction  :row
                           ;; Grow to occupy full space, shrink when need be, but always maitaining 16px left gutter
                           :flex-grow       1
                           :flex-shrink     0
                           :margin-left     16
                           ;; When the left-side leaves no room for right-side, the rendered element is pushed out. A flex-basis ensures that there is some room reserved.
                           ;; The number 80px was determined by trial and error.
                           :flex-basis      80}
                          accessory-style)}
   [rn/view {:style (:tiny spacing/padding-horizontal)}
    [text/text {:color           :secondary
                :ellipsize-mode  :middle
                :number-of-lines 1}
     accessory-text]
    ]
   (when (and chevron platform/ios?)
     [rn/view {:style {:padding-right (:tiny spacing/spacing)}}
      [icons/icon :main-icons/next {:container-style {:opacity         0.4
                                                      :align-items     :center
                                                      :justify-content :center}
                                    :resize-mode     :center
                                    :color           (:icon-02 @colors/theme)}]])])





(defn build-validator-row [index {:keys [identity name tokens operator-address]}]
  (let [image-url (rf/sub [:validator-thumbnail-store/image-by-validator-identity identity])]

    [rn/touchable-opacity {:style         {:margin-vertical 10
                                           :padding         10}
                           :on-press      #(navigate-to-detail operator-address)
                           :on-long-press #(navigate-to-detail operator-address)
                           }

     ;container view
     [rn/view {:style {
                       :flex-direction      :row
                       :justify-content     :space-between
                       :align-items         :center
                       :padding-vertical    1
                       :padding-horizontal  1
                       :border-bottom-width 1
                       :border-bottom-color (:ui-01 @colors/theme)
                       }}
      ; row with left
      [rn/view {:style {:flex-direction :row
                        ;; Occupy only content width, never grow, but shrink if need be
                        :flex-grow      0
                        :flex-shrink    1
                        :padding-right  16
                        :align-items    :center}}
       [quo/text {:weight :medium} (str "#" (inc index) "  ")]
       [react/image {:source {:uri image-url }
                     :style
                     {:width  24
                      :height 24}
                     }]


       ;
       [list-item/title-column {:title                     name
                                :text-color                "black"
                                :title-accessibility-label name
                                :right-side-present?       true
                                }]]
      ; row with right

      [list-item/right-side {
                    :accessory :text
                   :accessory-text (str tokens)
                   :chevron        true }]]]))

(views/defview list-validators [address]
               (views/letsubs [images [:validator-thumbnail-store/all-thumbnails]
                               validators [:keplr-store/validators]]
                              [:<>
                               ;[show-selected-validator-status]

                               [react/view
                                (doall
                                  (for [[index current-data] (map-indexed vector validators)]
                                    ^{:key index}
                                    [build-validator-row index current-data]))]]))
