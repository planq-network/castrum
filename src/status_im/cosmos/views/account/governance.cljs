(ns status-im.cosmos.views.account.governance
  (:require-macros [status-im.utils.views :as views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]))

(views/defview governance [address]
  (views/letsubs [data [:wallet.transactions.history/screen address]
                  {:keys [governance-proposals]} [:keplr-store]]
    [react/view
     (for [{:keys [proposal_id content voting_end_time status]} governance-proposals]
       ^{:key proposal_id}
       [quo/list-item
        {:title              [quo/text {:weight :medium}
                              [quo/text {:weight :inherit}
                               (str "#" proposal_id ". " (get-in content [:title]))]]
         :subtitle           (str (get-in content [:description]) "\n" status)
         :subtitle-max-lines 5}])]))

