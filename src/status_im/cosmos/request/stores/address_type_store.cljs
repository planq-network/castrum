(ns status-im.cosmos.request.stores.address-type-store
  (:require
   [status-im.utils.fx :as fx]))

(fx/defn set-selected-address-id
  {:events [:address-types/set]}
  [{:keys [db]} selected-address-id]
  {:db (assoc db :address-types/selected selected-address-id)})

(comment)

