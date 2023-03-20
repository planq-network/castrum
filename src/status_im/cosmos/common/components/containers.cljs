(ns status-im.cosmos.common.components.containers
  (:require
   [quo.core :as quo]
   [quo.react-native :as rn]
   ["react-native" :as react-native]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [status-im.ui.components.react :as react]))

(defn dispatch-reload [query]
  ;(prn "dispatch-reload" query)
  (re-frame/dispatch query))

;We need to use the react-native VirtualizedList component, as the numbers are huge and the default list component is not performant enough
(defn virtualized-list
  [props]
  (fn [{:keys [data renderItem] :as props}]
    (let [clj-fns {:getItem      nth
                   :getItemCount count
                   :keyExtractor #(str %2)
                   :renderItem   #(reagent/as-element (renderItem {:item  (aget % "item")
                                                                   :index (aget % "index")}))}
          dissoc-props (apply dissoc props [:data :renderItem])
          merged-props (merge clj-fns dissoc-props)
          merged-props-js (clj->js merged-props)
          _ (aset merged-props-js "data" data)]
      (reagent/create-element (.-VirtualizedList ^js react-native) merged-props-js))))

;All Errors should display an error , and a retry button if query is not nil
; we can set query for read operations, but not for write operations
(defn error-container [error reload-query]
  ;(prn "error-container" error reload-query)
  [rn/view
   [quo/text {:color :negative
              :align :center
              :size  :small}
    error]

   ;when query is not nil, show retry button
   (when reload-query
     [rn/view {:margin-top 8}
      [quo/button {:on-press

                   #(dispatch-reload reload-query)}
       "Retry"]])])
(defn dynamic-data-container [input & children]
  [:<>
   [rn/view
    (let [{:keys [status error reload-query]} input]
      (cond
        (= status :loading) [react/small-loading-indicator]
        (= status :error) [error-container error reload-query]
        (= status :loaded) children))]])
