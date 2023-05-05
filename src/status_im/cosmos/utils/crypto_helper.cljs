(ns status-im.cosmos.utils.crypto-helper
  (:require
    [cljs.core.async :as async]
    [cljs.core.async.interop :as interop]
    [status-im.cosmos.common.assertions :as assertions]
    [status-im.utils.types :as types]
    [re-frame.core :as re-frame]
    [re-frame.db :as re-frame.db]
    ["expo-random" :as expo-random]
    ["react-native-scrypt" :default scrypt]
    ["buffer/" :as app-buffer :refer (Buffer )]
    ["typedarray" :as typedarray]
    ))

;import { Buffer } from "buffer/";
(defn- byte-array->uint8-array [byte-array]
  (prn (js-keys byte-array))
  (typedarray/Uint8Array. (.-buffer byte-array)
                          (.-byteOffset byte-array)
                          (.-byteLength byte-array)))

(defn to-valid-uint8-array [byte-array]
  ;keys will have #js ["BYTES_PER_ELEMENT" "_pack" "_unpack" "_getter" "get" "_setter" "set" "subarray"]

  ;invalid key will be 
  ;iterate over byte-array and convert to uint8array
  (if (.-byteLength byte-array)
    byte-array
    (typedarray/Uint8Array. byte-array)))
(defn get-random-bytes-async
  [input]

  (let [array (to-valid-uint8-array input)]
    (js/Promise. (fn [resolve reject]
                 (-> (expo-random/getRandomBytesAsync (.-byteLength array))
                     (.then (fn [random]
                              (resolve random)))
                     (.catch (fn [err]
                               (prn "get-random-bytes-async error" err)
                               (reject err))))))))


(defn text-to-hex [text]
  (-> (.from Buffer text)
       (.toString "hex")))
(comment


  ; (.toString (.from Buffer "halow how are you") "hex")

  (text-to-hex "halow how are you")


  (-> (.from Buffer "halow how are you")
       (.toString "hex"))

  )
(defn build-scrypt [text params]
  (prn "build-scrypt start" text params)
  (js/Promise. (fn [resolve reject]
                 (prn "build-scrypt params" params)
                 (let [buffer-from-text (text-to-hex text)
                       {:keys [salt n r p dklen]} (types/js->clj  params) ]
                   (-> (scrypt buffer-from-text salt n r p dklen "hex")
                       (.then (fn [result]
                                (let [response (.from Buffer result "hex")]
                                  (prn "response" response)
                                  (resolve response))
                                ))
                       (.catch (fn [err]
                                 (prn "build-scrypt error" err)
                                 (reject err))))))))




(defn build-common-crypto []
  #js{
   :rng    get-random-bytes-async
   :scrypt build-scrypt
   }

  )
(comment

  (let [
        input (typedarray/Uint8Array. 2)
        ]
    ;[Uint8Array 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    (do
      (aset input 0 2)
      (aset input 1 4)
      (prn input)
      (prn "len" (.-length input))
      (assertions/expect-promise-to-be-resolved (get-random-bytes-async input))
      ))

  (re-frame/dispatch [:keplr-store/init])


  (let [
        input (typedarray/Uint8Array. [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0])
        ]
    ;[Uint8Array 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
    (do

      (prn input)
      (prn "len" (.-length input))
      (assertions/expect-promise-to-be-resolved (get-random-bytes-async input))
      ))


  (assertions/expect-promise-to-be-resolved (build-scrypt "Hello How are you"
                                                          (.parse js/JSON   (types/clj->json {:salt "salt" :n 16384 :r 8 :p 1 :dklen 64}))  ))


  )

; _$$_REQUIRE(_dependencyMap[1], "@babel/runtime/helpers/createClass")(AutoLockAccountService, [{
;      key: "init",
;      value: function init(keyringService) {
;        return __awaiter(this, void 0, void 0, _$$_REQUIRE(_dependencyMap[2], "@babel/runtime/regenerator").mark(function _callee() {
;          var _this = this;
;
;          return _$$_REQUIRE(_dependencyMap[2], "@babel/runtime/regenerator").wrap(function _callee$(_context) {
;            while (1) {
;              switch (_context.prev = _context.next) {
;                case 0:
;                  this.keyringService = keyringService;
;                  browser.idle.onStateChanged.addListener(function (idle) {
;                    _this.stateChangedHandler(idle);
;                  });
;                  _context.next = 4;
;                  return this.loadDuration();