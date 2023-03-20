(ns status-im.cosmos.account.staking.validator-icon
  (:require [status-im.ui.components.fast-image :as fast-image]
            [status-im.ui.screens.chat.styles.photos :as style]))

(def icon
  (memoize
   (fn [image-url accessibility-label _]
     [fast-image/fast-image {:source              {:uri image-url}
                             :style               (style/photo 24)
                             :accessibility-label accessibility-label}])))
