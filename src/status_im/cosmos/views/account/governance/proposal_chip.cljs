(ns status-im.cosmos.views.account.governance.proposal-chip

  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [quo.components.list.item :as list-item]
            [status-im.ui.components.react :as react]))





(defn status->style [status]

  (let [rules {:PASSED         {:color "#0000cd" :background-color "#e6e6fa" :mode "fill"}
               :REJECTED       {:color "white" :background-color "orange" :mode "fill"}
               :FAILED         {:color "white" :background-color "red" :mode "fill"}
               :DEPOSIT_PERIOD {:color "blue" :background-color "blue" :mode "outline"}
               :VOTING_PERIOD  {:color "white" :background-color "blue" :mode "fill"}
               :UNSPECIFIED    {:color "white" :background-color "red" :mode "fill"}}]

    (get-in rules [(keyword status)] {:color "white" :background-color "red" :mode "fill"})

    ))


(defn chip [{:keys [color
                    background-color
                    mode
                    content]

             :or   {color            "white"
                    background-color "blue"
                    mode             "fill"
                    }
             }]



  [rn/view {:style {

                    :background-color   (if (= mode "outline")
                                          "transparent"
                                          background-color)
                    :padding-horizontal 10
                    :padding-vertical 1
                    :border-radius      25
                    :border-color       background-color
                    :border-width       1
                    }}
   [quo/text {:style {:color color :font-size 10}}
    content]]

  )

(defn chip-status [status]
  (let [formatted-status status
        color (status->style formatted-status)]
    [chip (merge color {:content formatted-status})]))